package no.nav.bidrag.dokument.bestilling.consumer

import no.nav.bidrag.commons.security.service.SecurityTokenService
import no.nav.bidrag.dokument.bestilling.SECURE_LOGGER
import no.nav.bidrag.dokument.bestilling.config.CacheConfig.Companion.PERSON_ADRESSE_CACHE
import no.nav.bidrag.dokument.bestilling.config.CacheConfig.Companion.PERSON_CACHE
import no.nav.bidrag.dokument.bestilling.model.HentPersonResponse
import no.nav.bidrag.dokument.bestilling.model.HentPostadresseRequest
import no.nav.bidrag.dokument.bestilling.model.HentPostadresseResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.Cacheable
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.util.Optional

@Service
class BidragPersonConsumer(
    @Value("\${BIDRAG_PERSON_URL}") bidragPersonUrl: String, baseRestTemplate: RestTemplate,
    securityTokenService: SecurityTokenService
): DefaultConsumer("bidrag-person", bidragPersonUrl, baseRestTemplate, securityTokenService) {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(BidragPersonConsumer::class.java)
    }

    @Cacheable(PERSON_CACHE)
    fun hentPerson(personId: String): Optional<HentPersonResponse> {
        SECURE_LOGGER.info("Henter person med id $personId")
        LOGGER.info("Henter person")
        val hentPersonResponse =
            restTemplate.exchange("/informasjon/$personId", HttpMethod.GET, null, HentPersonResponse::class.java)
        return Optional.ofNullable(hentPersonResponse.body)
    }

    @Cacheable(value = [PERSON_ADRESSE_CACHE], unless = "#result == null")
    fun hentAdresse(id: String): Optional<HentPostadresseResponse> {
        return Optional.ofNullable(restTemplate.exchange(
            "/adresse/post", HttpMethod.POST, HttpEntity(HentPostadresseRequest(id)),
            HentPostadresseResponse::class.java
        ).body)
    }

}
