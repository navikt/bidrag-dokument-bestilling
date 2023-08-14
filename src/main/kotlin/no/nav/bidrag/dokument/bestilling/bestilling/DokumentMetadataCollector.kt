package no.nav.bidrag.dokument.bestilling.bestilling

import no.nav.bidrag.dokument.bestilling.api.dto.DokumentBestillingForespørsel
import no.nav.bidrag.dokument.bestilling.bestilling.dto.Adresse
import no.nav.bidrag.dokument.bestilling.bestilling.dto.Barn
import no.nav.bidrag.dokument.bestilling.bestilling.dto.DokumentBestilling
import no.nav.bidrag.dokument.bestilling.bestilling.dto.DokumentMal
import no.nav.bidrag.dokument.bestilling.bestilling.dto.EnhetKontaktInfo
import no.nav.bidrag.dokument.bestilling.bestilling.dto.Gjelder
import no.nav.bidrag.dokument.bestilling.bestilling.dto.Mottaker
import no.nav.bidrag.dokument.bestilling.bestilling.dto.PartInfo
import no.nav.bidrag.dokument.bestilling.bestilling.dto.Roller
import no.nav.bidrag.dokument.bestilling.bestilling.dto.SakDetaljer
import no.nav.bidrag.dokument.bestilling.bestilling.dto.VedtakDetaljer
import no.nav.bidrag.dokument.bestilling.config.SaksbehandlerInfoManager
import no.nav.bidrag.dokument.bestilling.consumer.dto.fornavnEtternavn
import no.nav.bidrag.dokument.bestilling.consumer.dto.isDod
import no.nav.bidrag.dokument.bestilling.consumer.dto.isKode6
import no.nav.bidrag.dokument.bestilling.model.BRUKSHENETSNUMMER_STANDARD
import no.nav.bidrag.dokument.bestilling.model.FantIkkeEnhetException
import no.nav.bidrag.dokument.bestilling.model.Ident
import no.nav.bidrag.dokument.bestilling.model.LANDKODE3_NORGE
import no.nav.bidrag.dokument.bestilling.model.ManglerGjelderException
import no.nav.bidrag.dokument.bestilling.model.Saksbehandler
import no.nav.bidrag.dokument.bestilling.model.SpråkKoder
import no.nav.bidrag.dokument.bestilling.model.erDødfødt
import no.nav.bidrag.dokument.bestilling.model.fantIkkeSak
import no.nav.bidrag.dokument.bestilling.model.manglerVedtakId
import no.nav.bidrag.dokument.bestilling.tjenester.KodeverkService
import no.nav.bidrag.dokument.bestilling.tjenester.OrganisasjonService
import no.nav.bidrag.dokument.bestilling.tjenester.PersonService
import no.nav.bidrag.dokument.bestilling.tjenester.SakService
import no.nav.bidrag.dokument.bestilling.tjenester.SjablongService
import no.nav.bidrag.dokument.bestilling.tjenester.VedtakService
import no.nav.bidrag.domain.enums.Adressetype
import no.nav.bidrag.domain.enums.Rolletype
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
import no.nav.bidrag.transport.sak.BidragssakDto
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
@Scope("prototype")
class DokumentMetadataCollector(
    val personService: PersonService,
    val sakService: SakService,
    val kodeverkService: KodeverkService,
    val vedtakService: VedtakService,
    val sjablongService: SjablongService,
    val saksbehandlerInfoManager: SaksbehandlerInfoManager,
    val organisasjonService: OrganisasjonService
) {

    private lateinit var enhet: String
    private lateinit var sak: BidragssakDto
    fun collect(
        forespørsel: DokumentBestillingForespørsel,
        dokumentMal: DokumentMal
    ): DokumentBestilling {
        val kreverDataGrunnlag = dokumentMal.kreverDataGrunnlag
        this.sak = sakService.hentSak(forespørsel.saksnummer) ?: fantIkkeSak(forespørsel.saksnummer)
        this.enhet = forespørsel.enhet ?: sak.eierfogd?.verdi ?: "9999"
        return DokumentBestilling(
            dokumentreferanse = forespørsel.dokumentreferanse ?: forespørsel.dokumentReferanse,
            tittel = forespørsel.tittel,
            saksnummer = forespørsel.saksnummer,
            spraak = forespørsel.hentRiktigSpråkkode(),
            saksbehandler = hentSaksbehandler(forespørsel),
            enhet = enhet,
            datoSakOpprettet = sak.opprettetDato.verdi,
            rmISak = sak.roller.any { it.type == Rolletype.RM },
            sjablonDetaljer = sjablongService.hentSjablonDetaljer(),
            sakDetaljer = SakDetaljer(
                harUkjentPart = sak.ukjentPart.verdi,
                levdeAdskilt = sak.levdeAdskilt.verdi
            ),
            mottaker = hentMottakerData(forespørsel),
            gjelder = hentGjelderData(forespørsel),
            kontaktInfo = kreverDataGrunnlag.takeIf { it.enhetKontaktInfo }
                ?.let { hentEnhetKontakInfo(forespørsel) },
            roller = kreverDataGrunnlag.takeIf { it.roller }?.let { hentRolleData(forespørsel) }
                ?: Roller(),
            vedtakDetaljer = kreverDataGrunnlag.takeIf { it.vedtak }
                ?.let { hentVedtakData(forespørsel.vedtakId) }
        )
    }

    private fun hentVedtakData(vedtakId: String?): VedtakDetaljer {
        if (vedtakId.isNullOrEmpty()) manglerVedtakId()
        return vedtakService.hentVedtakDetaljer(vedtakId)
    }

    private fun hentRolleData(forespørsel: DokumentBestillingForespørsel): Roller {
        val roller = Roller()
        val bidragspliktig = hentBidragspliktig()
        val bidragsmottaker = hentBidragsmottaker()

        val bidragspliktigAdresse = hentBidragspliktigAdresse()
        val bidragsmottakerAdresse = hentBidragsmottakerAdresse()

        if (bidragsmottaker != null) {
            roller.add(
                PartInfo(
                    rolle = Rolletype.BM,
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
            roller.add(
                PartInfo(
                    rolle = Rolletype.BP,
                    fodselsnummer = bidragspliktig.ident.verdi,
                    navn = if (bidragspliktig.isKode6()) "" else bidragspliktig.fornavnEtternavn(),
                    fodselsdato = hentFodselsdato(bidragspliktig),
                    doedsdato = bidragspliktig.dødsdato?.verdi,
                    landkode = bidragspliktigAdresse?.land?.verdi,
                    landkode3 = bidragspliktigAdresse?.land3?.verdi
                )
            )
        }

        val soknadsbarn = mutableListOf<String>()
        forespørsel.vedtakId?.let {
            soknadsbarn.addAll(
                vedtakService.hentVedtakSoknadsbarnFodselsnummer(
                    it
                )
            )
        }

        val barn = sak.roller.filter { it.type == Rolletype.BA }
        barn.filter { it.fødselsnummer != null && (soknadsbarn.isEmpty() || soknadsbarn.contains(it.fødselsnummer?.verdi)) }
            .forEach {
                val barnInfo =
                    if (it.fødselsnummer!!.verdi.erDødfødt) null else personService.hentPerson(
                        it.fødselsnummer!!.verdi,
                        "Barn"
                    )
                if (barnInfo == null || !barnInfo.isDod()) {
                    roller.add(
                        Barn(
                            fodselsnummer = it.fødselsnummer!!.verdi,
                            navn = barnInfo?.let {
                                if (barnInfo.isKode6()) hentKode6NavnBarn(
                                    barnInfo,
                                    forespørsel
                                ) else barnInfo.fornavnEtternavn()
                            } ?: "",
                            fodselsdato = barnInfo?.let { hentFodselsdato(barnInfo) },
                            fornavn = barnInfo?.let {
                                if (barnInfo.isKode6()) hentKode6NavnBarn(
                                    barnInfo,
                                    forespørsel
                                ) else barnInfo.fornavn?.verdi
                            },
                            fodselsnummerRm = it.rmFødselsnummer()?.verdi
                        )
                    )
                }
            }

        return roller
    }

    private fun hentGjelderData(forespørsel: DokumentBestillingForespørsel): Gjelder {
        return Gjelder(
            fodselsnummer = forespørsel.actualGjelderId,
            rolle = hentRolle(forespørsel.actualGjelderId)
        )
    }

    private fun hentMottakerData(forespørsel: DokumentBestillingForespørsel): Mottaker {
        if (forespørsel.erMottakerSamhandler() && !forespørsel.harMottakerKontaktinformasjon() && forespørsel.samhandlerInformasjon != null) {
            val samhandler = forespørsel.samhandlerInformasjon!!
            val adresse = samhandler.adresse
            return Mottaker(
                fodselsnummer = forespørsel.mottakerIdent!!,
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
                    land = adresse?.landkode?.let { kodeverkService.hentLandFullnavnForKode(it) }
                )
            )
        } else {
            val mottaker = hentMottaker(forespørsel)
            val adresse = hentMottakerAdresse(forespørsel)

            return Mottaker(
                fodselsnummer = mottaker.ident.verdi,
                fodselsdato = mottaker.fødselsdato?.verdi,
                navn = mottaker.kortnavn?.verdi ?: mottaker.navn?.verdi ?: "",
                rolle = hentRolle(mottaker.ident.verdi),
                spraak = forespørsel.mottaker?.språk
                    ?: personService.hentSpråk(mottaker.ident.verdi),
                adresse = if (adresse != null) {
                    val postnummerSted =
                        if (adresse.postnummer?.verdi.isNullOrEmpty() && adresse.poststed?.verdi.isNullOrEmpty()) {
                            null
                        } else {
                            "${adresse.postnummer ?: ""} ${adresse.poststed ?: ""}".trim()
                        }
                    val landNavn =
                        adresse.land3.verdi.let { kodeverkService.hentLandFullnavnForKode(it) }
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
    }

    fun hentEnhetKontakInfo(forespørsel: DokumentBestillingForespørsel): EnhetKontaktInfo {
        val enhetKontaktInfo =
            organisasjonService.hentEnhetKontaktInfo(enhet, forespørsel.hentRiktigSpråkkode())
                ?: throw FantIkkeEnhetException("Fant ikke enhet $enhet for spraak ${forespørsel.hentRiktigSpråkkode()}")

        val land = enhetKontaktInfo.postadresse?.land
        val erNorge = listOf("Norway", "Norge").any { it.equals(land, ignoreCase = true) }
        return EnhetKontaktInfo(
            navn = enhetKontaktInfo.enhetNavn ?: "",
            telefonnummer = enhetKontaktInfo.telefonnummer ?: "",
            postadresse = Adresse(
                adresselinje1 = enhetKontaktInfo.postadresse?.adresselinje1 ?: "",
                adresselinje2 = enhetKontaktInfo.postadresse?.adresselinje2,
                poststed = enhetKontaktInfo.postadresse?.poststed,
                postnummer = enhetKontaktInfo.postadresse?.postnummer,
                land = if (!erNorge) land else null
            ),
            enhetId = enhet
        )
    }

    private val DokumentBestillingForespørsel.actualGjelderId
        get() = (if (hentRolle(this.gjelderId) != null) this.gjelderId else hentGjelderFraRoller())
            ?: throw ManglerGjelderException("Fant ingen gjelder")

    private fun hentFodselsdato(person: PersonDto): LocalDate? {
        return if (person.isKode6()) null else person.fødselsdato?.verdi
    }

    private fun hentKode6NavnBarn(
        person: PersonDto,
        forespørsel: DokumentBestillingForespørsel
    ): String {
        val fodtaar = person.fødselsdato?.verdi?.year
        return when (forespørsel.hentRiktigSpråkkode()) {
            SpråkKoder.BOKMAL -> if (fodtaar == null) "(BARN)" else "(BARN FØDT I $fodtaar)"
            SpråkKoder.NYNORSK -> if (fodtaar == null) "(BARN)" else "(BARN FØDD I $fodtaar)"
            else -> if (fodtaar == null) "(CHILD)" else "(CHILD BORN IN $fodtaar)"
        }
    }

    private fun hentRolle(ident: String?): Rolletype? {
        return sak.roller.find { it.fødselsnummer?.verdi == ident }?.type
    }

    private fun hentIdentForRolle(rolle: Rolletype): String? {
        return sak.roller.find { it.type == rolle }?.fødselsnummer?.verdi
    }

    private fun hentBidragsmottaker(): PersonDto? {
        val bmFnr = hentIdentForRolle(Rolletype.BM)
        return if (!bmFnr.isNullOrEmpty()) personService.hentPerson(
            bmFnr,
            "Bidragsmottaker"
        ) else null
    }

    private fun hentBidragspliktig(): PersonDto? {
        val fnr = hentIdentForRolle(Rolletype.BP)
        return if (!fnr.isNullOrEmpty()) personService.hentPerson(fnr, "Bidragspliktig") else null
    }

    private fun hentBidragspliktigAdresse(): PersonAdresseDto? {
        val fnr = hentIdentForRolle(Rolletype.BP)
        return if (!fnr.isNullOrEmpty()) personService.hentPersonAdresse(
            fnr,
            "Bidragspliktig adresse"
        ) else null
    }

    private fun hentBidragsmottakerAdresse(): PersonAdresseDto? {
        val fnr = hentIdentForRolle(Rolletype.BM)
        return if (!fnr.isNullOrEmpty()) personService.hentPersonAdresse(
            fnr,
            "Bidragsmottaker adresse"
        ) else null
    }

    private fun hentMottaker(forespørsel: DokumentBestillingForespørsel): PersonDto {
        val mottakerIdent = forespørsel.mottakerIdent
        return if (forespørsel.erMottakerSamhandler() || mottakerIdent.isNullOrEmpty()) {
            PersonDto(
                navn = FulltNavn(forespørsel.mottaker?.navn ?: ""),
                ident = PersonIdent(mottakerIdent ?: "")
            )
        } else {
            personService.hentPerson(mottakerIdent, "Mottaker")
        }
    }

    private fun hentMottakerAdresse(forespørsel: DokumentBestillingForespørsel): PersonAdresseDto? {
        val mottakerIdent = forespørsel.mottakerIdent
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
                land3 = Landkode3(adresse.landkode3 ?: ""),
                adressetype = Adressetype.BOSTEDSADRESSE
            )
        } else if (!forespørsel.erMottakerSamhandler() && mottakerIdent != null) {
            personService.hentPersonAdresse(mottakerIdent, "Mottaker")
        } else {
            null
        }
    }

    private fun hentGjelderFraRoller(): Ident? {
        return hentIdentForRolle(Rolletype.BM) ?: hentIdentForRolle(Rolletype.BP)
        ?: hentIdentForRolle(Rolletype.BA)
    }

    private fun hentSaksbehandler(request: DokumentBestillingForespørsel): Saksbehandler {
        if (request.saksbehandler != null && !request.saksbehandler.ident.isNullOrEmpty()) {
            val saksbehandlerNavn = request.saksbehandler.navn
                ?: saksbehandlerInfoManager.hentSaksbehandler(request.saksbehandler.ident)?.navn
                ?: saksbehandlerInfoManager.hentSaksbehandlerBrukerId()
            return Saksbehandler(request.saksbehandler.ident, saksbehandlerNavn)
        }

        val saksbehandler = saksbehandlerInfoManager.hentSaksbehandler()
        val saksbehandlerId = saksbehandler?.ident ?: ""
        val saksbehandlerNavn = saksbehandler?.fornavnEtternavn ?: ""
        return Saksbehandler(saksbehandlerId, saksbehandlerNavn)
    }
}
