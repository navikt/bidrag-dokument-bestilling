package no.nav.bidrag.dokument.bestilling.service

import no.nav.bidrag.dokument.bestilling.SECURE_LOGGER
import no.nav.bidrag.dokument.bestilling.consumer.BidragPersonConsumer
import no.nav.bidrag.dokument.bestilling.model.FantIkkePersonException
import no.nav.bidrag.dokument.bestilling.model.HentPersonResponse
import no.nav.bidrag.dokument.bestilling.model.HentPostadresseResponse
import org.springframework.stereotype.Service
import java.util.Optional

@Service
class PersonService(private var bidragPersonConsumer: BidragPersonConsumer) {

    fun hentPerson(personId: String, rolle: String? = "UKJENT"): HentPersonResponse {
       return bidragPersonConsumer.hentPerson(personId).orElseThrow {
           SECURE_LOGGER.warn("Fant ikke person med fnr $personId og rolle $rolle")
           FantIkkePersonException("Fant ikke person med rolle $rolle")
       }
    }

    fun hentPersonAdresse(personId: String, rolle: String? = "UKJENT"): HentPostadresseResponse {
        return bidragPersonConsumer.hentAdresse(personId).orElseThrow {
            SECURE_LOGGER.warn("Fant ikke adresse for person $personId med rolle $rolle")
            FantIkkePersonException("Fant ikke adresse for person med rolle $rolle")
        }
    }
}