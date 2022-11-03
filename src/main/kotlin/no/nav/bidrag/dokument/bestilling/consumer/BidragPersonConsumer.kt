package no.nav.bidrag.dokument.bestilling.consumer

import no.nav.bidrag.commons.security.service.SecurityTokenService
import no.nav.bidrag.dokument.bestilling.model.HentPersonFeiletException
import no.nav.bidrag.dokument.bestilling.model.HentPersonResponse
import no.nav.bidrag.dokument.bestilling.model.HentPersonInforRequest
import no.nav.bidrag.dokument.bestilling.model.HentPostadresseResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestTemplate
import org.springframework.web.context.annotation.RequestScope
import java.util.Optional

@Service
@RequestScope
class BidragPersonConsumer(
    @Value("\${BIDRAG_PERSON_URL}") bidragPersonUrl: String, baseRestTemplate: RestTemplate,
    securityTokenService: SecurityTokenService
): DefaultConsumer("bidrag-person", bidragPersonUrl, baseRestTemplate, securityTokenService) {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(BidragPersonConsumer::class.java)
    }

    @Retryable(maxAttempts = 3, backoff = Backoff(delay = 500, maxDelay = 1500, multiplier = 2.0))
    fun hentPerson(personId: String): Optional<HentPersonResponse> {
        try {
            val hentPersonResponse =
                restTemplate.exchange("/informasjon/$personId", HttpMethod.GET, null, HentPersonResponse::class.java)
            return Optional.ofNullable(hentPersonResponse.body)
        } catch (e: HttpStatusCodeException){
            if (e.statusCode == HttpStatus.NOT_FOUND){
                return Optional.empty()
            }
            throw HentPersonFeiletException("Henting av person $personId feilet", e)
        }
    }
    @Retryable(maxAttempts = 3, backoff = Backoff(delay = 500, maxDelay = 1500, multiplier = 2.0))
    fun hentAdresse(id: String): Optional<HentPostadresseResponse> {
        return Optional.ofNullable(restTemplate.exchange(
            "/adresse/post", HttpMethod.POST, HttpEntity(HentPersonInforRequest(id)),
            HentPostadresseResponse::class.java
        ).body)
    }

    @Retryable(maxAttempts = 3, backoff = Backoff(delay = 500, maxDelay = 1500, multiplier = 2.0))
    fun hentSpraak(id: String): String? {
        return restTemplate.exchange(
            "/spraak", HttpMethod.POST, HttpEntity(HentPersonInforRequest(id)),
            String::class.java
        ).body
    }

}
