package no.nav.bidrag.dokument.bestilling.tjenester

import no.nav.bidrag.dokument.bestilling.SECURE_LOGGER
import no.nav.bidrag.dokument.bestilling.konsumer.BidragPersonConsumer
import no.nav.bidrag.dokument.bestilling.model.FantIkkePersonException
import no.nav.bidrag.dokument.bestilling.konsumer.dto.HentPersonResponse
import no.nav.bidrag.dokument.bestilling.konsumer.dto.HentPostadresseResponse
import org.springframework.stereotype.Service

@Service
class PersonTjeneste(private val bidragPersonConsumer: BidragPersonConsumer) {

    fun hentPerson(personId: String, rolle: String? = "UKJENT"): HentPersonResponse {
       return bidragPersonConsumer.hentPerson(personId) ?: run {
           SECURE_LOGGER.warn("Fant ikke person med fnr $personId og rolle $rolle")
           throw FantIkkePersonException("Fant ikke person med rolle $rolle")
       }
    }

    fun hentPersonAdresse(personId: String, rolle: String? = "UKJENT"): HentPostadresseResponse? {
        return bidragPersonConsumer.hentAdresse(personId) ?: run {
            SECURE_LOGGER.warn("Fant ikke adresse for person $personId med rolle $rolle")
            null
        }
    }

    fun hentSpraak(personId: String): String {
        return bidragPersonConsumer.hentSpraak(personId)?.uppercase() ?: "NB"
    }
}