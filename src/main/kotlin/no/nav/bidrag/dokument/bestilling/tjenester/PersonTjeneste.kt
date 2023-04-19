package no.nav.bidrag.dokument.bestilling.tjenester

import no.nav.bidrag.dokument.bestilling.SIKKER_LOGG
import no.nav.bidrag.dokument.bestilling.konsumer.BidragPersonKonsumer
import no.nav.bidrag.dokument.bestilling.model.FantIkkePersonException
import no.nav.bidrag.transport.person.PersonAdresseDto
import no.nav.bidrag.transport.person.PersonDto
import org.springframework.stereotype.Service

@Service
class PersonTjeneste(private val bidragPersonKonsumer: BidragPersonKonsumer) {

    fun hentPerson(personId: String, rolle: String? = "UKJENT"): PersonDto {
        return bidragPersonKonsumer.hentPerson(personId) ?: run {
            SIKKER_LOGG.warn("Fant ikke person med fnr $personId og rolle $rolle")
            throw FantIkkePersonException("Fant ikke person med rolle $rolle")
        }
    }

    fun hentPersonAdresse(personId: String, rolle: String? = "UKJENT"): PersonAdresseDto? {
        return bidragPersonKonsumer.hentAdresse(personId) ?: run {
            SIKKER_LOGG.warn("Fant ikke adresse for person $personId med rolle $rolle")
            null
        }
    }

    fun hentSpråk(personId: String): String {
        return bidragPersonKonsumer.hentSpraak(personId)?.uppercase() ?: "NB"
    }
}
