package no.nav.bidrag.dokument.bestilling.service

import no.nav.bidrag.dokument.bestilling.consumer.BidragPersonConsumer
import no.nav.bidrag.dokument.bestilling.model.HentPersonResponse
import no.nav.bidrag.dokument.bestilling.model.HentPostadresseResponse
import org.springframework.stereotype.Service
import java.util.Optional

@Service
class PersonService(private var bidragPersonConsumer: BidragPersonConsumer) {

    fun hentPerson(personId: String): Optional<HentPersonResponse> {
       return bidragPersonConsumer.hentPerson(personId)
    }

    fun hentPersonAdresse(personId: String): Optional<HentPostadresseResponse> {
        return bidragPersonConsumer.hentAdresse(personId)
    }
}