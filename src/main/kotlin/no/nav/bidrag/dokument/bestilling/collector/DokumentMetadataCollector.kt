package no.nav.bidrag.dokument.bestilling.collector

import no.nav.bidrag.dokument.bestilling.SECURE_LOGGER
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
import no.nav.bidrag.dokument.bestilling.model.Rolle
import no.nav.bidrag.dokument.bestilling.model.RolleType
import no.nav.bidrag.dokument.bestilling.model.SoknadsPart
import no.nav.bidrag.dokument.bestilling.service.OrganisasjonService
import no.nav.bidrag.dokument.bestilling.service.PersonService
import no.nav.bidrag.dokument.bestilling.service.SakService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
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
    lateinit var mottakerData: PersonData
    lateinit var gjelderData: PersonData
    var dokumentBestilling: DokumentBestilling = DokumentBestilling()

    fun init(request: DokumentBestillingRequest, enhet: String): DokumentMetadataCollector {
        this.enhet = enhet
        this.request = request
        dokumentBestilling.dokumentReferanse = request.dokumentReferanse
        dokumentBestilling.tittel = request.tittel
        dokumentBestilling.saksnummer = request.saksnummer
        dokumentBestilling.enhet = enhet

        sak = sakService.hentSak(request.saksnummer)
            .orElseThrow { FantIkkeSakException("Fant ikke sak ${request.saksnummer}") }

        val mottaker = hentMottaker()
        val gjelder = hentGjelder()
        val gadresse = hentGjelderAdresse()
        val madresse = hentMottakerAdresse()

        mottakerData = PersonData(madresse, mottaker)
        gjelderData = PersonData(gadresse, gjelder)

        return this
    }

    private fun hentRolle(fnr: String): RolleType? {
        return sak.roller.find { it.foedselsnummer == fnr }?.rolleType
    }

    fun addPartOgBarn(): DokumentMetadataCollector {
        val mottaker = mottakerData.person
        val gjelder = gjelderData.person
        val bidragspliktig =
            if (hentRolle(mottaker.ident) == RolleType.BP) mottaker else if (hentRolle(gjelder.ident) == RolleType.BP) gjelder else null
        val bidragsmottaker =
            if (hentRolle(mottaker.ident) == RolleType.BM) mottaker else if (hentRolle(gjelder.ident) == RolleType.BM) gjelder else null

        val barn = sak.roller.filter { it.rolleType == RolleType.BA }
        if (bidragsmottaker != null) dokumentBestilling.roller.add(
            PartInfo(
                rolle = RolleType.BM,
                fodselsnummer = bidragsmottaker.ident,
                navn = bidragsmottaker.navn,
                fodselsdato = bidragsmottaker.foedselsdato
            )
        )

        if (bidragspliktig != null) dokumentBestilling.roller.add(PartInfo(
                    rolle = RolleType.BP,
                    fodselsnummer = bidragspliktig.ident,
                    navn = bidragspliktig.navn,
                    fodselsdato = bidragspliktig.foedselsdato
                )
        )

        barn.filter { !it.foedselsnummer.isNullOrEmpty() }.forEach{
            val barnInfo = personService.hentPerson(it.foedselsnummer!!, "Barn")
            dokumentBestilling.roller.add(Barn(
                    fodselsnummer = barnInfo.ident,
                    navn = barnInfo.navn,
                    fodselsdato = barnInfo.foedselsdato
            ))
        }

        return this
    }

    fun addGjelder(): DokumentMetadataCollector {
        val person = gjelderData.person
        val adresse = gjelderData.adresse

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
        val person = mottakerData.person
        val adresse = mottakerData.adresse

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
                adresselinje1 = enhetKontaktInfo.postadresse?.adresselinje ?: "",
                postnummer = enhetKontaktInfo.postadresse?.postnummer
            ),
            enhetId = enhet
        )
        return this
    }

    fun withVedtak(): DokumentMetadataCollector {
        return this
    }

    fun getBestillingData(): DokumentBestilling = dokumentBestilling


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

data class PersonData(
    var adresse: HentPostadresseResponse,
    var person: HentPersonResponse
)