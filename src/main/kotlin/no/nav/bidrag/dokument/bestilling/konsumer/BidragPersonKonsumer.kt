package no.nav.bidrag.dokument.bestilling.konsumer

import no.nav.bidrag.commons.cache.BrukerCacheable
import no.nav.bidrag.commons.security.service.SecurityTokenService
import no.nav.bidrag.dokument.bestilling.konfigurasjon.CacheKonfig.Companion.PERSON_ADRESSE_CACHE
import no.nav.bidrag.dokument.bestilling.konfigurasjon.CacheKonfig.Companion.PERSON_CACHE
import no.nav.bidrag.dokument.bestilling.konfigurasjon.CacheKonfig.Companion.PERSON_SPRAAK_CACHE
import no.nav.bidrag.dokument.bestilling.model.HentPersonFeiletException
import no.nav.bidrag.domain.ident.PersonIdent
import no.nav.bidrag.transport.person.PersonAdresseDto
import no.nav.bidrag.transport.person.PersonDto
import no.nav.bidrag.transport.person.PersonRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestTemplate

@Service
class BidragPersonKonsumer(
    @Value("\${BIDRAG_PERSON_URL}") bidragPersonUrl: String,
    baseRestTemplate: RestTemplate,
    securityTokenService: SecurityTokenService
) : DefaultKonsumer("bidrag-person", bidragPersonUrl, baseRestTemplate, securityTokenService) {

    @Retryable(maxAttempts = 3, backoff = Backoff(delay = 500, maxDelay = 1500, multiplier = 2.0))
    @BrukerCacheable(PERSON_CACHE)
    fun hentPerson(personId: String): PersonDto? {
        try {
            val hentPersonResponse =
                restTemplate.exchange("/informasjon", HttpMethod.POST, HttpEntity(PersonRequest(PersonIdent(personId))), PersonDto::class.java)
            return hentPersonResponse.body
        } catch (e: HttpStatusCodeException) {
            if (e.statusCode == HttpStatus.NOT_FOUND) {
                return null
            }
            throw HentPersonFeiletException("Henting av person $personId feilet", e)
        }
    }

    @Retryable(maxAttempts = 3, backoff = Backoff(delay = 500, maxDelay = 1500, multiplier = 2.0))
    @BrukerCacheable(PERSON_ADRESSE_CACHE)
    fun hentAdresse(id: String): PersonAdresseDto? {
        return restTemplate.exchange(
            "/adresse/post",
            HttpMethod.POST,
            HttpEntity(PersonRequest(PersonIdent(id))),
            PersonAdresseDto::class.java
        ).body
    }

    @Retryable(maxAttempts = 3, backoff = Backoff(delay = 500, maxDelay = 1500, multiplier = 2.0))
    @BrukerCacheable(PERSON_SPRAAK_CACHE)
    fun hentSpraak(id: String): String? {
        return restTemplate.exchange(
            "/spraak",
            HttpMethod.POST,
            HttpEntity(PersonRequest(PersonIdent(id))),
            String::class.java
        ).body
    }
}
