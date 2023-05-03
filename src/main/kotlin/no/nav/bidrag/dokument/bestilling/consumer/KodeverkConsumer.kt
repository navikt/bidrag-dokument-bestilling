package no.nav.bidrag.dokument.bestilling.consumer

import no.nav.bidrag.commons.CorrelationId
import no.nav.bidrag.commons.web.HttpHeaderRestTemplate
import no.nav.bidrag.dokument.bestilling.config.CacheConfig.Companion.LANDKODER_CACHE
import no.nav.bidrag.dokument.bestilling.consumer.dto.KodeverkResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RootUriTemplateHandler
import org.springframework.cache.CacheManager
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import javax.annotation.PostConstruct

@Service
class KodeverkConsumer(@Value("\${KODEVERK_URL}") kodeverkUrl: String, val cacheManager: CacheManager) {
    private val restTemplate: RestTemplate

    init {
        val restTemplate = HttpHeaderRestTemplate()
        restTemplate.uriTemplateHandler = RootUriTemplateHandler("$kodeverkUrl/api/v1/kodeverk")
        restTemplate.addHeaderGenerator("Nav-Call-Id") { CorrelationId.generateTimestamped("bidrag-dokument-bestilling").get() }
        restTemplate.addHeaderGenerator("Nav-Consumer-Id") { "bidrag-dokument-forsendelse" }
        this.restTemplate = restTemplate
    }

    @PostConstruct
    fun preloadKodeverkValues() {
        loadLandkoder()
    }

    private fun loadLandkoder() {
        LOGGER.info("Henter Landkoder fra kodeverk")
        val response = restTemplate.exchange("/Landkoder/koder/betydninger?spraak=nb", HttpMethod.GET, null, KodeverkResponse::class.java)
        val kommuner = response.body
        cacheManager.getCache(LANDKODER_CACHE)?.put(DEFAULT_CACHE, kommuner)
    }

    fun hentLandkoder(): KodeverkResponse? {
        return cacheManager.getCache(LANDKODER_CACHE)?.get(DEFAULT_CACHE, KodeverkResponse::class.java)
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(KodeverkConsumer::class.java)
        private const val DEFAULT_CACHE = "DEFAULT"
    }
}
