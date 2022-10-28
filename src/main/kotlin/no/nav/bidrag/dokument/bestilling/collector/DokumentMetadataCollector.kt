package no.nav.bidrag.dokument.bestilling.collector

import no.nav.bidrag.dokument.bestilling.model.Adresse
import no.nav.bidrag.dokument.bestilling.model.Barn
import no.nav.bidrag.dokument.bestilling.model.DokumentBestilling
import no.nav.bidrag.dokument.bestilling.model.DokumentBestillingRequest
import no.nav.bidrag.dokument.bestilling.model.EnhetKontaktInfo
import no.nav.bidrag.dokument.bestilling.model.FantIkkePersonException
import no.nav.bidrag.dokument.bestilling.model.FantIkkeSakException
import no.nav.bidrag.dokument.bestilling.model.Gjelder
import no.nav.bidrag.dokument.bestilling.model.HentPersonResponse
import no.nav.bidrag.dokument.bestilling.model.HentPostadresseResponse
import no.nav.bidrag.dokument.bestilling.model.HentSakResponse
import no.nav.bidrag.dokument.bestilling.model.Mottaker
import no.nav.bidrag.dokument.bestilling.model.PartInfo
import no.nav.bidrag.dokument.bestilling.model.RolleType
import no.nav.bidrag.dokument.bestilling.model.SakRolle
import no.nav.bidrag.dokument.bestilling.model.SamhandlerManglerKontaktinformasjon
import no.nav.bidrag.dokument.bestilling.model.SpraakKoder
import no.nav.bidrag.dokument.bestilling.service.OrganisasjonService
import no.nav.bidrag.dokument.bestilling.service.PersonService
import no.nav.bidrag.dokument.bestilling.service.SakService
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope("prototype")
class DokumentMetadataCollector(
    var personService: PersonService,
    var sakService: SakService,
    var organisasjonService: OrganisasjonService
) {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(DokumentMetadataCollector::class.java)
    }

    lateinit var request: DokumentBestillingRequest
    lateinit var enhet: String
    lateinit var sak: HentSakResponse
    lateinit var dokumentBestilling: DokumentBestilling

    fun init(request: DokumentBestillingRequest): DokumentMetadataCollector {
        this.dokumentBestilling = DokumentBestilling()
        this.request = request
        dokumentBestilling.dokumentReferanse = request.dokumentReferanse
        dokumentBestilling.tittel = request.tittel
        dokumentBestilling.saksnummer = request.saksnummer
        dokumentBestilling.spraak = request.hentRiktigSpraakkode()

        sak = sakService.hentSak(request.saksnummer)
            .orElseThrow { FantIkkeSakException("Fant ikke sak ${request.saksnummer}") }

        this.enhet = request.enhet ?: sak.eierfogd ?: "9999"
        dokumentBestilling.enhet = enhet

        return this
    }

    fun addRoller(): DokumentMetadataCollector {
        val bidragspliktig = hentBidragspliktig()
        val bidragsmottaker = hentBidragsmottaker()

        if (bidragsmottaker != null) dokumentBestilling.roller.add(
            PartInfo(
                rolle = RolleType.BM,
                fodselsnummer = bidragsmottaker.ident,
                navn = if (bidragsmottaker.isKode6) "" else bidragsmottaker.fornavnEtternavn,
                fodselsdato = bidragsmottaker.foedselsdato
            )
        )

        if (bidragspliktig != null) dokumentBestilling.roller.add(
            PartInfo(
                    rolle = RolleType.BP,
                    fodselsnummer = bidragspliktig.ident,
                    navn = if (bidragspliktig.isKode6) "" else bidragspliktig.fornavnEtternavn,
                    fodselsdato = bidragspliktig.foedselsdato
                )
        )

        val barn = sak.roller.filter { it.rolleType == RolleType.BA }
        barn.filter { !it.foedselsnummer.isNullOrEmpty() }.forEach{
            val barnInfo = personService.hentPerson(it.foedselsnummer!!, "Barn")
            dokumentBestilling.roller.add(Barn(
                    fodselsnummer = barnInfo.ident,
                    navn = if (barnInfo.isKode6) hentKode6NavnBarn(barnInfo) else barnInfo.fornavnEtternavn,
                    fodselsdato = barnInfo.foedselsdato,
                    fornavn = if (barnInfo.isKode6) hentKode6NavnBarn(barnInfo) else barnInfo.fornavn
            ))
        }

        return this
    }

    fun addGjelder(): DokumentMetadataCollector {
        val person = hentGjelder()
        val adresse = hentGjelderAdresse()
        dokumentBestilling.gjelder = Gjelder(
            fodselsnummer = person.ident,
            navn = person.navn,
            rolle = hentRolle(person.ident),
            adresse = Adresse(
                adresselinje1 = adresse.adresselinje1!!,
                adresselinje2 = adresse.adresselinje2,
                adresselinje3 = adresse.adresselinje3,
                poststed = adresse.poststed,
                postnummer = adresse.postnummer,
                landkode = adresse.land
            )
        )
        return this
    }

    fun addMottaker(): DokumentMetadataCollector {
        if (request.isMottakerSamhandler()){
            val kontaktInfo = request.mottakerKontaktInformasjon ?: throw SamhandlerManglerKontaktinformasjon("Samhandler med id ${request.mottakerId} mangler kontaktinformasjon")
            dokumentBestilling.mottaker = Mottaker(
                fodselsnummer = request.mottakerId,
                fodselsdato = null,
                rolle = null,
                navn = kontaktInfo.navn ?: "Ukjent",
                adresse = Adresse(
                    adresselinje1 = kontaktInfo.adresselinje1 ?: "",
                    adresselinje2 = kontaktInfo.adresselinje2 ?: "",
                    adresselinje3 = kontaktInfo.adresselinje3 ?: "",
                    postnummer = kontaktInfo.postnummer,
                    landkode = kontaktInfo.landkode
                )
            )
        } else {
            val person = hentMottaker()
            val adresse = hentMottakerAdresse()

            dokumentBestilling.mottaker = Mottaker(
                fodselsnummer = person.ident,
                fodselsdato = person.foedselsdato,
                navn = person.navn,
                rolle = hentRolle(person.ident),
                adresse = Adresse(
                    adresselinje1 = adresse.adresselinje1!!,
                    adresselinje2 = adresse.adresselinje2,
                    adresselinje3 = adresse.adresselinje3,
                    poststed = adresse.poststed,
                    postnummer = adresse.postnummer,
                    landkode = adresse.land
                )
            )
        }

        return this
    }

    fun addKontaktInfo(): DokumentMetadataCollector {
        val enhetKontaktInfo = organisasjonService.hentEnhetKontaktInfo(enhet).orElseThrow {
            FantIkkePersonException("Fant ikke enhet $enhet")
        }

        dokumentBestilling.kontaktInfo = EnhetKontaktInfo(
            navn = enhetKontaktInfo.enhetNavn ?: "",
            telefonnummer = enhetKontaktInfo.telefonnummer ?: "",
            returAdresse = Adresse(
                adresselinje1 = enhetKontaktInfo.postadresse?.adresselinje ?: ""
            ),
            enhetId = enhet
        )
        return this
    }

    fun withVedtak(): DokumentMetadataCollector {
        return this
    }

    fun getBestillingData(): DokumentBestilling = dokumentBestilling

    private fun hentKode6NavnBarn(person: HentPersonResponse): String {
        val fodtaar = person.foedselsdato?.year
        return when(dokumentBestilling.spraak){
            SpraakKoder.BOKMAL -> if(fodtaar==null) "(BARN)" else "(BARN FØDT I $fodtaar)"
            SpraakKoder.NYNORSK -> if(fodtaar==null) "(BARN)" else "(BARN FØDD I $fodtaar)"
            else -> if(fodtaar==null) "(CHILD)" else "(CHILD BORN IN $fodtaar)"
        }
    }
    private fun hentRolle(fnr: String): RolleType? {
        return sak.roller.find { it.foedselsnummer == fnr }?.rolleType
    }

    private fun hentIdentForRolle(rolle: RolleType): String? {
        return sak.roller.find { it.rolleType == rolle }?.foedselsnummer
    }
    private fun hentBidragsmottaker(): HentPersonResponse? {
        val bmFnr = hentIdentForRolle(RolleType.BM) ?: hentIdentForRolle(RolleType.RM)
        return if(!bmFnr.isNullOrEmpty()) personService.hentPerson(bmFnr, "Bidragsmottaker") else null
    }

    private fun hentBidragspliktig(): HentPersonResponse? {
        val fnr = hentIdentForRolle(RolleType.BP)
        return if(!fnr.isNullOrEmpty()) personService.hentPerson(fnr, "Bidragspliktig") else null
    }

    private fun hentMottaker(): HentPersonResponse {
        return personService.hentPerson(request.mottakerId, "Mottaker")
    }

    private fun hentMottakerAdresse(): HentPostadresseResponse {
        return personService.hentPersonAdresse(request.mottakerId, "Mottaker")
    }

    private fun hentGjelder(): HentPersonResponse {
        if (request.mottakerId == request.gjelderId) {
            return hentMottaker()
        }
        return personService.hentPerson(request.gjelderId, "Gjelder")
    }

    private fun hentGjelderAdresse(): HentPostadresseResponse {
        if (request.mottakerId == request.gjelderId) {
            return hentMottakerAdresse()
        }
        return personService.hentPersonAdresse(request.gjelderId, "Gjelder")
    }
}

typealias PersonIdent = String
data class PersonData(
    var adresse: Map<PersonIdent, HentPersonResponse>,
    var personer: Map<PersonIdent, HentPersonResponse>,
    var roller: List<SakRolle>
)