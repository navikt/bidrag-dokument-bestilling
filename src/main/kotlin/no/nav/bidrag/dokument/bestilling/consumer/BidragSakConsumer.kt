package no.nav.bidrag.dokument.bestilling.consumer

import no.nav.bidrag.commons.security.service.SecurityTokenService
import no.nav.bidrag.dokument.bestilling.SECURE_LOGGER
import no.nav.bidrag.dokument.bestilling.config.CacheConfig.Companion.PERSON_ADRESSE_CACHE
import no.nav.bidrag.dokument.bestilling.config.CacheConfig.Companion.PERSON_CACHE
import no.nav.bidrag.dokument.bestilling.model.HentPersonResponse
import no.nav.bidrag.dokument.bestilling.model.HentPostadresseRequest
import no.nav.bidrag.dokument.bestilling.model.HentPostadresseResponse
import no.nav.bidrag.dokument.bestilling.model.HentSakResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.Cacheable
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.util.Optional

@Service
class BidragSakConsumer(
    @Value("\${BIDRAG_SAK_URL}") bidragSakUrl: String, baseRestTemplate: RestTemplate,
    securityTokenService: SecurityTokenService
): DefaultConsumer("bidrag-sak", bidragSakUrl, baseRestTemplate, securityTokenService) {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(BidragSakConsumer::class.java)
    }

    fun hentSak(saksnr: String): Optional<HentSakResponse> {
        LOGGER.info("Henter sak med id $saksnr")
        val hentPersonResponse =
            restTemplate.exchange("/sak/$saksnr", HttpMethod.GET, null, HentSakResponse::class.java)
        return Optional.ofNullable(hentPersonResponse.body)
    }

}
