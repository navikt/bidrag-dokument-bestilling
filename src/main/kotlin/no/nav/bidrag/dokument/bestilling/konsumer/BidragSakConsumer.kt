package no.nav.bidrag.dokument.bestilling.konsumer

import no.nav.bidrag.commons.security.service.SecurityTokenService
import no.nav.bidrag.dokument.bestilling.model.HentSakFeiletException
import no.nav.bidrag.dokument.bestilling.konsumer.dto.HentSakResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestTemplate

@Service
class BidragSakConsumer(
    @Value("\${BIDRAG_SAK_URL}") bidragSakUrl: String, baseRestTemplate: RestTemplate,
    securityTokenService: SecurityTokenService
): DefaultConsumer("bidrag-sak", bidragSakUrl, baseRestTemplate, securityTokenService) {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(BidragSakConsumer::class.java)
    }
    @Retryable(maxAttempts = 3, backoff = Backoff(delay = 500, maxDelay = 1500, multiplier = 2.0))
    fun hentSak(saksnr: String): HentSakResponse? {
        try {
            val hentPersonResponse =
                restTemplate.exchange("/sak/$saksnr", HttpMethod.GET, null, HentSakResponse::class.java)
            LOGGER.info("Hentet sak med id $saksnr")
            return hentPersonResponse.body
        } catch (e: HttpStatusCodeException){
            if (e.statusCode == HttpStatus.NOT_FOUND){
                return null
            }
            throw HentSakFeiletException("Henting av sak $saksnr feilet", e)
        }

    }

}
