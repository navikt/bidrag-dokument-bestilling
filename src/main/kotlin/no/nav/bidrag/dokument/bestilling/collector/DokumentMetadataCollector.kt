package no.nav.bidrag.dokument.bestilling.collector

import no.nav.bidrag.dokument.bestilling.config.SaksbehandlerInfoManager
import no.nav.bidrag.dokument.bestilling.model.Adresse
import no.nav.bidrag.dokument.bestilling.model.BRUKSHENETSNUMMER_STANDARD
import no.nav.bidrag.dokument.bestilling.model.Barn
import no.nav.bidrag.dokument.bestilling.model.DokumentBestilling
import no.nav.bidrag.dokument.bestilling.model.DokumentBestillingRequest
import no.nav.bidrag.dokument.bestilling.model.EnhetKontaktInfo
import no.nav.bidrag.dokument.bestilling.model.FantIkkeEnhetException
import no.nav.bidrag.dokument.bestilling.model.FantIkkeSakException
import no.nav.bidrag.dokument.bestilling.model.Gjelder
import no.nav.bidrag.dokument.bestilling.model.HentPersonResponse
import no.nav.bidrag.dokument.bestilling.model.HentPostadresseResponse
import no.nav.bidrag.dokument.bestilling.model.HentSakResponse
import no.nav.bidrag.dokument.bestilling.model.Ident
import no.nav.bidrag.dokument.bestilling.model.LANDKODE3_NORGE
import no.nav.bidrag.dokument.bestilling.model.ManglerGjelderException
import no.nav.bidrag.dokument.bestilling.model.Mottaker
import no.nav.bidrag.dokument.bestilling.model.PartInfo
import no.nav.bidrag.dokument.bestilling.model.RolleType
import no.nav.bidrag.dokument.bestilling.model.Saksbehandler
import no.nav.bidrag.dokument.bestilling.model.SamhandlerManglerKontaktinformasjon
import no.nav.bidrag.dokument.bestilling.model.SpraakKoder
import no.nav.bidrag.dokument.bestilling.model.isDodfodt
import no.nav.bidrag.dokument.bestilling.service.KodeverkService
import no.nav.bidrag.dokument.bestilling.service.OrganisasjonService
import no.nav.bidrag.dokument.bestilling.service.PersonService
import no.nav.bidrag.dokument.bestilling.service.SakService
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
@Scope("prototype")
class DokumentMetadataCollector(
    val personService: PersonService,
    val sakService: SakService,
    val kodeverkService: KodeverkService,
    val saksbehandlerInfoManager: SaksbehandlerInfoManager,
    val organisasjonService: OrganisasjonService
) {

    private lateinit var request: DokumentBestillingRequest
    private lateinit var enhet: String
    private lateinit var sak: HentSakResponse
    private lateinit var dokumentBestilling: DokumentBestilling

    fun init(request: DokumentBestillingRequest): DokumentMetadataCollector {
        this.dokumentBestilling = DokumentBestilling()
        this.request = request
        dokumentBestilling.dokumentReferanse = request.dokumentReferanse
        dokumentBestilling.tittel = request.tittel
        dokumentBestilling.saksnummer = request.saksnummer
        dokumentBestilling.spraak = request.hentRiktigSpraakkode()
        dokumentBestilling.saksbehandler = hentSaksbehandler(request)

        sak = sakService.hentSak(request.saksnummer) ?: throw FantIkkeSakException("Fant ikke sak ${request.saksnummer}")
        // TODO: Legg til sak opprettet dato (mangler fra sak respons)

        this.enhet = request.enhet ?: sak.eierfogd ?: "9999"
        dokumentBestilling.enhet = enhet
        dokumentBestilling.rmISak = sak.roller.any { it.rolleType == RolleType.RM }

        return this
    }

    fun addMottakerGjelder(): DokumentMetadataCollector {
        addMottaker()
        addGjelder()
        return this
    }

    fun addRoller(): DokumentMetadataCollector {
        val bidragspliktig = hentBidragspliktig()
        val bidragsmottaker = hentBidragsmottaker()

        val bidragspliktigAdresse = hentBidragspliktigAdresse()
        val bidragsmottakerAdresse = hentBidragsmottakerAdresse()

        if (bidragsmottaker != null) dokumentBestilling.roller.add(
            PartInfo(
                rolle = RolleType.BM,
                fodselsnummer = bidragsmottaker.ident,
                navn = if (bidragsmottaker.isKode6) "" else bidragsmottaker.fornavnEtternavn,
                fodselsdato = hentFodselsdato(bidragsmottaker),
                doedsdato = bidragsmottaker.doedsdato,
                landkode = bidragsmottakerAdresse?.land,
                landkode3 = bidragsmottakerAdresse?.land3
            )
        )

        if (bidragspliktig != null) dokumentBestilling.roller.add(
            PartInfo(
                rolle = RolleType.BP,
                fodselsnummer = bidragspliktig.ident,
                navn = if (bidragspliktig.isKode6) "" else bidragspliktig.fornavnEtternavn,
                fodselsdato = hentFodselsdato(bidragspliktig),
                doedsdato = bidragspliktig.doedsdato,
                landkode = bidragspliktigAdresse?.land,
                landkode3 = bidragspliktigAdresse?.land3
            )
        )

        val barn = sak.roller.filter { it.rolleType == RolleType.BA }
        barn.filter { !it.foedselsnummer.isNullOrEmpty() }.forEach {
            val barnInfo = if (it.foedselsnummer!!.isDodfodt) null else personService.hentPerson(it.foedselsnummer, "Barn")
            if (barnInfo == null || !barnInfo.isDod) {
                // TODO: Legg til RM fødselsnummer (mangler fra sak respons)
                dokumentBestilling.roller.add(Barn(
                    fodselsnummer = it.foedselsnummer,
                    navn = barnInfo?.let { if (barnInfo.isKode6) hentKode6NavnBarn(barnInfo) else barnInfo.fornavnEtternavn } ?: "",
                    fodselsdato = barnInfo?.let { hentFodselsdato(barnInfo) },
                    fornavn = barnInfo?.let { if (barnInfo.isKode6) hentKode6NavnBarn(barnInfo) else barnInfo.fornavn }
                ))
            }

        }

        return this
    }

    private fun addGjelder(): DokumentMetadataCollector {
        dokumentBestilling.gjelder = Gjelder(
            fodselsnummer = request.actualGjelderId,
            rolle = hentRolle(request.actualGjelderId)
        )
        return this
    }

    private fun addMottaker(): DokumentMetadataCollector {
        if (request.isMottakerSamhandler()) {
            val samhandler = request.samhandlerInformasjon ?: throw SamhandlerManglerKontaktinformasjon("Samhandler med id ${request.mottakerId} mangler kontaktinformasjon")
            val adresse = samhandler.adresse
            dokumentBestilling.mottaker = Mottaker(
                fodselsnummer = request.mottakerId,
                fodselsdato = null,
                rolle = null,
                navn = samhandler.navn ?: "Ukjent",
                spraak = samhandler.spraak ?: "NB",
                adresse = Adresse(
                    adresselinje1 = adresse?.adresselinje1 ?: "",
                    adresselinje2 = adresse?.adresselinje2 ?: "",
                    adresselinje3 = "${adresse?.postnummer} ${adresse?.adresselinje3?.substring(0, adresse.adresselinje3?.length?.coerceAtMost(25) ?: 0) ?: ""}".trim(),
                    postnummer = adresse?.postnummer,
                    landkode = adresse?.landkode,
                    landkode3 = adresse?.landkode,
                    land = adresse?.landkode?.let { kodeverkService.hentLandFullnavnForKode(it) }
                )
            )
        } else {
            val mottaker = hentMottaker()
            val adresse = hentMottakerAdresse()

            dokumentBestilling.mottaker = Mottaker(
                fodselsnummer = mottaker.ident,
                fodselsdato = mottaker.foedselsdato,
                navn = mottaker.kortNavn ?: mottaker.navn,
                rolle = hentRolle(mottaker.ident),
                spraak = personService.hentSpraak(mottaker.ident),
                adresse = if (adresse != null) {
                    val postnummerSted = if (adresse.postnummer.isNullOrEmpty() && adresse.poststed.isNullOrEmpty()) null
                                        else "${adresse.postnummer ?: ""} ${adresse.poststed ?: ""}".trim()
                    val landNavn = adresse.land3?.let { kodeverkService.hentLandFullnavnForKode(it) }
                    Adresse(
                        adresselinje1 = adresse.adresselinje1 ?: "",
                        adresselinje2 = adresse.adresselinje2,
                        adresselinje3 = adresse.adresselinje3 ?: postnummerSted,
                        adresselinje4 = if (!adresse.land3.isNullOrEmpty() && adresse.land3 != LANDKODE3_NORGE) landNavn else null,
                        bruksenhetsnummer = if (adresse.bruksenhetsnummer == BRUKSHENETSNUMMER_STANDARD) null else adresse.bruksenhetsnummer,
                        poststed = adresse.poststed,
                        postnummer = adresse.postnummer,
                        landkode = adresse.land,
                        landkode3 = adresse.land3,
                        land = landNavn
                    )
                } else null
            )
        }

        return this
    }

    fun addEnhetKontaktInfo(): DokumentMetadataCollector {
        val enhetKontaktInfo = organisasjonService.hentEnhetKontaktInfo(enhet, dokumentBestilling.spraak) ?: throw FantIkkeEnhetException("Fant ikke enhet $enhet for spraak ${dokumentBestilling.spraak}")

        dokumentBestilling.kontaktInfo = EnhetKontaktInfo(
            navn = enhetKontaktInfo.enhetNavn ?: "",
            telefonnummer = enhetKontaktInfo.telefonnummer ?: "",
            postadresse = Adresse(
                adresselinje1 = enhetKontaktInfo.postadresse?.adresselinje1 ?: "",
                adresselinje2 = enhetKontaktInfo.postadresse?.adresselinje2,
                poststed = enhetKontaktInfo.postadresse?.poststed,
                postnummer = enhetKontaktInfo.postadresse?.postnummer,
                land = enhetKontaktInfo.postadresse?.land,
            ),
            enhetId = enhet
        )
        return this
    }

    fun withVedtak(): DokumentMetadataCollector {
        return this
    }

    fun getBestillingData(): DokumentBestilling = dokumentBestilling

    private val DokumentBestillingRequest.actualGjelderId get() = (if (hentRolle(this.gjelderId) != null) this.gjelderId else hentGjelderFraRoller()) ?: throw ManglerGjelderException("Fant ingen gjelder")

    private fun hentFodselsdato(person: HentPersonResponse): LocalDate? {
        return if (person.isKode6) null else person.foedselsdato
    }

    private fun hentKode6NavnBarn(person: HentPersonResponse): String {
        val fodtaar = person.foedselsdato?.year
        return when (dokumentBestilling.spraak) {
            SpraakKoder.BOKMAL -> if (fodtaar == null) "(BARN)" else "(BARN FØDT I $fodtaar)"
            SpraakKoder.NYNORSK -> if (fodtaar == null) "(BARN)" else "(BARN FØDD I $fodtaar)"
            else -> if (fodtaar == null) "(CHILD)" else "(CHILD BORN IN $fodtaar)"
        }
    }

    private fun hentRolle(ident: String?): RolleType? {
        return sak.roller.find { it.foedselsnummer == ident }?.rolleType
    }

    private fun hentIdentForRolle(rolle: RolleType): String? {
        return sak.roller.find { it.rolleType == rolle }?.foedselsnummer
    }

    private fun hentBidragsmottaker(): HentPersonResponse? {
        val bmFnr = hentIdentForRolle(RolleType.BM)
        return if (!bmFnr.isNullOrEmpty()) personService.hentPerson(bmFnr, "Bidragsmottaker") else null
    }

    private fun hentBidragspliktig(): HentPersonResponse? {
        val fnr = hentIdentForRolle(RolleType.BP)
        return if (!fnr.isNullOrEmpty()) personService.hentPerson(fnr, "Bidragspliktig") else null
    }

    private fun hentBidragspliktigAdresse(): HentPostadresseResponse? {
        val fnr = hentIdentForRolle(RolleType.BP)
        return if (!fnr.isNullOrEmpty()) personService.hentPersonAdresse(fnr, "Bidragspliktig adresse") else null
    }

    private fun hentBidragsmottakerAdresse(): HentPostadresseResponse? {
        val fnr = hentIdentForRolle(RolleType.BM)
        return if (!fnr.isNullOrEmpty()) personService.hentPersonAdresse(fnr, "Bidragsmottaker adresse") else null
    }

    private fun hentMottaker(): HentPersonResponse {
        return personService.hentPerson(request.mottakerId, "Mottaker")
    }

    private fun hentMottakerAdresse(): HentPostadresseResponse? {
        return personService.hentPersonAdresse(request.mottakerId, "Mottaker")
    }

    private fun hentGjelderFraRoller(): Ident? {
        return hentIdentForRolle(RolleType.BM) ?: hentIdentForRolle(RolleType.BP) ?: hentIdentForRolle(RolleType.BA)
    }

    private fun hentSaksbehandler(request: DokumentBestillingRequest): Saksbehandler {
        if (request.saksbehandler != null && !request.saksbehandler.ident.isNullOrEmpty()) {
            val saksbehandlerNavn = request.saksbehandler.navn ?: saksbehandlerInfoManager.hentSaksbehandler()?.navn ?: saksbehandlerInfoManager.hentSaksbehandlerBrukerId()
            return Saksbehandler(request.saksbehandler.ident, saksbehandlerNavn)
        }

        val saksbehandler = saksbehandlerInfoManager.hentSaksbehandler()
        val saksbehandlerId = saksbehandler?.ident ?: ""
        val saksbehandlerNavn = saksbehandler?.fornavnEtternavn ?: ""
        return Saksbehandler(saksbehandlerId, saksbehandlerNavn)
    }
}
