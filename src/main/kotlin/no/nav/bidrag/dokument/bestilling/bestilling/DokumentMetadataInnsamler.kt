package no.nav.bidrag.dokument.bestilling.bestilling

import no.nav.bidrag.dokument.bestilling.api.dto.DokumentBestillingForespørsel
import no.nav.bidrag.dokument.bestilling.bestilling.dto.Adresse
import no.nav.bidrag.dokument.bestilling.bestilling.dto.Barn
import no.nav.bidrag.dokument.bestilling.bestilling.dto.DokumentBestilling
import no.nav.bidrag.dokument.bestilling.bestilling.dto.EnhetKontaktInfo
import no.nav.bidrag.dokument.bestilling.bestilling.dto.Gjelder
import no.nav.bidrag.dokument.bestilling.bestilling.dto.Mottaker
import no.nav.bidrag.dokument.bestilling.bestilling.dto.PartInfo
import no.nav.bidrag.dokument.bestilling.konfigurasjon.SaksbehandlerInfoManager
import no.nav.bidrag.dokument.bestilling.konsumer.dto.HentSakResponse
import no.nav.bidrag.dokument.bestilling.konsumer.dto.RolleType
import no.nav.bidrag.dokument.bestilling.konsumer.dto.fornavnEtternavn
import no.nav.bidrag.dokument.bestilling.konsumer.dto.isDod
import no.nav.bidrag.dokument.bestilling.konsumer.dto.isKode6
import no.nav.bidrag.dokument.bestilling.model.BRUKSHENETSNUMMER_STANDARD
import no.nav.bidrag.dokument.bestilling.model.FantIkkeEnhetException
import no.nav.bidrag.dokument.bestilling.model.FantIkkeSakException
import no.nav.bidrag.dokument.bestilling.model.Ident
import no.nav.bidrag.dokument.bestilling.model.LANDKODE3_NORGE
import no.nav.bidrag.dokument.bestilling.model.ManglerGjelderException
import no.nav.bidrag.dokument.bestilling.model.Saksbehandler
import no.nav.bidrag.dokument.bestilling.model.SpråkKoder
import no.nav.bidrag.dokument.bestilling.model.erDødfødt
import no.nav.bidrag.dokument.bestilling.tjenester.KodeverkTjeneste
import no.nav.bidrag.dokument.bestilling.tjenester.OrganisasjonTjeneste
import no.nav.bidrag.dokument.bestilling.tjenester.PersonTjeneste
import no.nav.bidrag.dokument.bestilling.tjenester.SakTjeneste
import no.nav.bidrag.domain.enums.Adressetype
import no.nav.bidrag.domain.ident.PersonIdent
import no.nav.bidrag.domain.string.Adresselinje1
import no.nav.bidrag.domain.string.Adresselinje2
import no.nav.bidrag.domain.string.Adresselinje3
import no.nav.bidrag.domain.string.Bruksenhetsnummer
import no.nav.bidrag.domain.string.FulltNavn
import no.nav.bidrag.domain.string.Landkode2
import no.nav.bidrag.domain.string.Landkode3
import no.nav.bidrag.domain.string.Postnummer
import no.nav.bidrag.domain.string.Poststed
import no.nav.bidrag.transport.person.PersonAdresseDto
import no.nav.bidrag.transport.person.PersonDto
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
@Scope("prototype")
class DokumentMetadataInnsamler(
    val personTjeneste: PersonTjeneste,
    val sakTjeneste: SakTjeneste,
    val kodeverkTjeneste: KodeverkTjeneste,
    val saksbehandlerInfoManager: SaksbehandlerInfoManager,
    val organisasjonTjeneste: OrganisasjonTjeneste
) {

    private lateinit var forespørsel: DokumentBestillingForespørsel
    private lateinit var enhet: String
    private lateinit var sak: HentSakResponse
    private lateinit var dokumentBestilling: DokumentBestilling

    fun init(forespørsel: DokumentBestillingForespørsel): DokumentMetadataInnsamler {
        this.dokumentBestilling = DokumentBestilling()
        this.forespørsel = forespørsel
        dokumentBestilling.dokumentreferanse = forespørsel.dokumentreferanse ?: forespørsel.dokumentReferanse
        dokumentBestilling.tittel = forespørsel.tittel
        dokumentBestilling.saksnummer = forespørsel.saksnummer
        dokumentBestilling.spraak = forespørsel.hentRiktigSpråkkode()
        dokumentBestilling.saksbehandler = hentSaksbehandler(forespørsel)

        sak = sakTjeneste.hentSak(forespørsel.saksnummer) ?: throw FantIkkeSakException("Fant ikke sak ${forespørsel.saksnummer}")
        // TODO: Legg til sak opprettet dato (mangler fra sak respons)

        this.enhet = forespørsel.enhet ?: sak.eierfogd ?: "9999"
        dokumentBestilling.enhet = enhet
        dokumentBestilling.rmISak = sak.roller.any { it.rolleType == RolleType.RM }

        return this
    }

    fun leggTilMottakerGjelder(): DokumentMetadataInnsamler {
        leggTilMottaker()
        leggTilGjelder()
        return this
    }

    fun leggTilRoller(): DokumentMetadataInnsamler {
        val bidragspliktig = hentBidragspliktig()
        val bidragsmottaker = hentBidragsmottaker()

        val bidragspliktigAdresse = hentBidragspliktigAdresse()
        val bidragsmottakerAdresse = hentBidragsmottakerAdresse()

        if (bidragsmottaker != null) {
            dokumentBestilling.roller.add(
                PartInfo(
                    rolle = RolleType.BM,
                    fodselsnummer = bidragsmottaker.ident.verdi,
                    navn = if (bidragsmottaker.isKode6()) "" else bidragsmottaker.fornavnEtternavn(),
                    fodselsdato = hentFodselsdato(bidragsmottaker),
                    doedsdato = bidragsmottaker.dødsdato?.verdi,
                    landkode = bidragsmottakerAdresse?.land?.verdi,
                    landkode3 = bidragsmottakerAdresse?.land3?.verdi
                )
            )
        }

        if (bidragspliktig != null) {
            dokumentBestilling.roller.add(
                PartInfo(
                    rolle = RolleType.BP,
                    fodselsnummer = bidragspliktig.ident.verdi,
                    navn = if (bidragspliktig.isKode6()) "" else bidragspliktig.fornavnEtternavn(),
                    fodselsdato = hentFodselsdato(bidragspliktig),
                    doedsdato = bidragspliktig.dødsdato?.verdi,
                    landkode = bidragspliktigAdresse?.land?.verdi,
                    landkode3 = bidragspliktigAdresse?.land3?.verdi
                )
            )
        }

        val barn = sak.roller.filter { it.rolleType == RolleType.BA }
        barn.filter { !it.foedselsnummer.isNullOrEmpty() }.forEach {
            val barnInfo = if (it.foedselsnummer!!.erDødfødt) null else personTjeneste.hentPerson(it.foedselsnummer, "Barn")
            if (barnInfo == null || !barnInfo.isDod()) {
                // TODO: Legg til RM fødselsnummer (mangler fra sak respons)
                dokumentBestilling.roller.add(
                    Barn(
                        fodselsnummer = it.foedselsnummer,
                        navn = barnInfo?.let { if (barnInfo.isKode6()) hentKode6NavnBarn(barnInfo) else barnInfo.fornavnEtternavn() } ?: "",
                        fodselsdato = barnInfo?.let { hentFodselsdato(barnInfo) },
                        fornavn = barnInfo?.let { if (barnInfo.isKode6()) hentKode6NavnBarn(barnInfo) else barnInfo.fornavn?.verdi }
                    )
                )
            }
        }

        return this
    }

    private fun leggTilGjelder(): DokumentMetadataInnsamler {
        dokumentBestilling.gjelder = Gjelder(
            fodselsnummer = forespørsel.actualGjelderId,
            rolle = hentRolle(forespørsel.actualGjelderId)
        )
        return this
    }

    private fun leggTilMottaker(): DokumentMetadataInnsamler {
        if (forespørsel.erMottakerSamhandler() && !forespørsel.harMottakerKontaktinformasjon() && forespørsel.samhandlerInformasjon != null) {
            val samhandler = forespørsel.samhandlerInformasjon!!
            val adresse = samhandler.adresse
            dokumentBestilling.mottaker = Mottaker(
                fodselsnummer = forespørsel.mottakerIdent,
                fodselsdato = null,
                rolle = null,
                navn = samhandler.navn ?: "Ukjent",
                spraak = samhandler.spraak ?: "NB",
                adresse = Adresse(
                    adresselinje1 = adresse?.adresselinje1 ?: "",
                    adresselinje2 = adresse?.adresselinje2 ?: "",
                    adresselinje3 = "${adresse?.postnummer} ${adresse?.adresselinje3?.take(25) ?: ""}".trim(),
                    postnummer = adresse?.postnummer,
                    landkode = adresse?.landkode,
                    landkode3 = adresse?.landkode,
                    land = adresse?.landkode?.let { kodeverkTjeneste.hentLandFullnavnForKode(it) }
                )
            )
        } else {
            val mottaker = hentMottaker()
            val adresse = hentMottakerAdresse()

            dokumentBestilling.mottaker = Mottaker(
                fodselsnummer = mottaker.ident.verdi,
                fodselsdato = mottaker.fødselsdato?.verdi,
                navn = mottaker.kortnavn?.verdi ?: mottaker.navn?.verdi ?: "",
                rolle = hentRolle(mottaker.ident.verdi),
                spraak = forespørsel.mottaker?.språk ?: personTjeneste.hentSpråk(mottaker.ident.verdi),
                adresse = if (adresse != null) {
                    val postnummerSted = if (adresse.postnummer?.verdi.isNullOrEmpty() && adresse.poststed?.verdi.isNullOrEmpty()) {
                        null
                    } else {
                        "${adresse.postnummer ?: ""} ${adresse.poststed ?: ""}".trim()
                    }
                    val landNavn = adresse.land3.verdi.let { kodeverkTjeneste.hentLandFullnavnForKode(it) }
                    val adresselinje3 = if (forespørsel.erMottakerSamhandler()) {
                        "${adresse.postnummer} ${adresse.adresselinje3?.verdi?.take(25) ?: ""}".trim()
                    } else {
                        adresse.adresselinje3?.verdi ?: postnummerSted
                    }
                    Adresse(
                        adresselinje1 = adresse.adresselinje1?.verdi ?: "",
                        adresselinje2 = adresse.adresselinje2?.verdi,
                        adresselinje3 = adresselinje3,
                        adresselinje4 = if (!adresse.land3.verdi.isEmpty() && adresse.land3.verdi != LANDKODE3_NORGE && !forespørsel.erMottakerSamhandler()) landNavn else null,
                        bruksenhetsnummer = if (adresse.bruksenhetsnummer?.verdi == BRUKSHENETSNUMMER_STANDARD) null else adresse.bruksenhetsnummer?.verdi,
                        poststed = adresse.poststed?.verdi,
                        postnummer = adresse.postnummer?.verdi,
                        landkode = adresse.land.verdi,
                        landkode3 = adresse.land3.verdi,
                        land = landNavn
                    )
                } else {
                    null
                }
            )
        }

        return this
    }

    fun leggTilEnhetKontaktInfo(): DokumentMetadataInnsamler {
        val enhetKontaktInfo = organisasjonTjeneste.hentEnhetKontaktInfo(enhet, dokumentBestilling.spraak)
            ?: throw FantIkkeEnhetException("Fant ikke enhet $enhet for spraak ${dokumentBestilling.spraak}")

        dokumentBestilling.kontaktInfo = EnhetKontaktInfo(
            navn = enhetKontaktInfo.navn?.verdi ?: "",
            telefonnummer = enhetKontaktInfo.telefonnummer?.verdi ?: "",
            postadresse = Adresse(
                adresselinje1 = enhetKontaktInfo.postadresse?.adresselinje1?.verdi ?: "",
                adresselinje2 = enhetKontaktInfo.postadresse?.adresselinje2?.verdi,
                poststed = enhetKontaktInfo.postadresse?.poststed?.verdi,
                postnummer = enhetKontaktInfo.postadresse?.postnummer?.verdi,
                land = enhetKontaktInfo.postadresse?.land?.verdi
            ),
            enhetId = enhet
        )
        return this
    }

    fun withVedtak(): DokumentMetadataInnsamler {
        return this
    }

    fun hentBestillingData(): DokumentBestilling = dokumentBestilling

    private val DokumentBestillingForespørsel.actualGjelderId
        get() = (if (hentRolle(this.gjelderId) != null) this.gjelderId else hentGjelderFraRoller())
            ?: throw ManglerGjelderException("Fant ingen gjelder")

    private fun hentFodselsdato(person: PersonDto): LocalDate? {
        return if (person.isKode6()) null else person.fødselsdato?.verdi
    }

    private fun hentKode6NavnBarn(person: PersonDto): String {
        val fodtaar = person.fødselsdato?.verdi?.year
        return when (dokumentBestilling.spraak) {
            SpråkKoder.BOKMAL -> if (fodtaar == null) "(BARN)" else "(BARN FØDT I $fodtaar)"
            SpråkKoder.NYNORSK -> if (fodtaar == null) "(BARN)" else "(BARN FØDD I $fodtaar)"
            else -> if (fodtaar == null) "(CHILD)" else "(CHILD BORN IN $fodtaar)"
        }
    }

    private fun hentRolle(ident: String?): RolleType? {
        return sak.roller.find { it.foedselsnummer == ident }?.rolleType
    }

    private fun hentIdentForRolle(rolle: RolleType): String? {
        return sak.roller.find { it.rolleType == rolle }?.foedselsnummer
    }

    private fun hentBidragsmottaker(): PersonDto? {
        val bmFnr = hentIdentForRolle(RolleType.BM)
        return if (!bmFnr.isNullOrEmpty()) personTjeneste.hentPerson(bmFnr, "Bidragsmottaker") else null
    }

    private fun hentBidragspliktig(): PersonDto? {
        val fnr = hentIdentForRolle(RolleType.BP)
        return if (!fnr.isNullOrEmpty()) personTjeneste.hentPerson(fnr, "Bidragspliktig") else null
    }

    private fun hentBidragspliktigAdresse(): PersonAdresseDto? {
        val fnr = hentIdentForRolle(RolleType.BP)
        return if (!fnr.isNullOrEmpty()) personTjeneste.hentPersonAdresse(fnr, "Bidragspliktig adresse") else null
    }

    private fun hentBidragsmottakerAdresse(): PersonAdresseDto? {
        val fnr = hentIdentForRolle(RolleType.BM)
        return if (!fnr.isNullOrEmpty()) personTjeneste.hentPersonAdresse(fnr, "Bidragsmottaker adresse") else null
    }

    private fun hentMottaker(): PersonDto {
        return if (forespørsel.erMottakerSamhandler()) {
            val mottaker = forespørsel.mottaker!!
            PersonDto(
                navn = FulltNavn(mottaker.navn!!),
                ident = PersonIdent(mottaker.ident)
            )
        } else {
            personTjeneste.hentPerson(forespørsel.mottakerIdent, "Mottaker")
        }
    }

    private fun hentMottakerAdresse(): PersonAdresseDto? {
        return if (forespørsel.harMottakerKontaktinformasjon()) {
            val adresse = forespørsel.mottaker!!.adresse!!
            PersonAdresseDto(
                adresselinje1 = Adresselinje1(adresse.adresselinje1),
                adresselinje2 = adresse.adresselinje2?.let { Adresselinje2(it) },
                adresselinje3 = adresse.adresselinje3?.let { Adresselinje3(it) },
                bruksenhetsnummer = adresse.bruksenhetsnummer?.let { Bruksenhetsnummer(it) },
                poststed = adresse.poststed?.let { Poststed(it) },
                postnummer = adresse.postnummer?.let { Postnummer(it) },
                land = Landkode2(adresse.landkode),
                land3 = Landkode3(adresse.landkode3),
                adressetype = Adressetype.BOSTEDSADRESSE
            )
        } else if (!forespørsel.erMottakerSamhandler()) {
            personTjeneste.hentPersonAdresse(forespørsel.mottakerIdent, "Mottaker")
        } else {
            null
        }
    }

    private fun hentGjelderFraRoller(): Ident? {
        return hentIdentForRolle(RolleType.BM) ?: hentIdentForRolle(RolleType.BP) ?: hentIdentForRolle(RolleType.BA)
    }

    private fun hentSaksbehandler(request: DokumentBestillingForespørsel): Saksbehandler {
        if (request.saksbehandler != null && !request.saksbehandler.ident.isNullOrEmpty()) {
            val saksbehandlerNavn = request.saksbehandler.navn ?: saksbehandlerInfoManager.hentSaksbehandler(request.saksbehandler.ident)?.navn
                ?: saksbehandlerInfoManager.hentSaksbehandlerBrukerId()
            return Saksbehandler(request.saksbehandler.ident, saksbehandlerNavn)
        }

        val saksbehandler = saksbehandlerInfoManager.hentSaksbehandler()
        val saksbehandlerId = saksbehandler?.ident ?: ""
        val saksbehandlerNavn = saksbehandler?.fornavnEtternavn ?: ""
        return Saksbehandler(saksbehandlerId, saksbehandlerNavn)
    }
}
