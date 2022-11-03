package no.nav.bidrag.dokument.bestilling.config

import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import java.util.concurrent.TimeUnit

@Configuration
@EnableCaching
@Profile(value = ["!test"]) // Ignore cache on tests
class CacheConfig {
    companion object {
        const val LANDKODER_CACHE = "LANDKODER_CACHE"
        const val PERSON_CACHE = "PERSON_CACHE"
        const val PERSON_ADRESSE_CACHE = "PERSON_ADRESSE_CACHE"
        const val SAKSBEHANDLERINFO_CACHE = "SAKSBEHANDLERINFO_CACHE"
        const val ENHETINFO_CACHE = "ENHETINFO_CACHE"
        const val ENHETKONTAKTINFO_CACHE = "ENHETKONTAKTINFO_CACHE"
    }

    @Bean
    fun cacheManager(): CacheManager {
        val caffeineCacheManager = CaffeineCacheManager()
        caffeineCacheManager.registerCustomCache(SAKSBEHANDLERINFO_CACHE, Caffeine.newBuilder().expireAfterWrite(24, TimeUnit.HOURS).build())
        caffeineCacheManager.registerCustomCache(ENHETINFO_CACHE, Caffeine.newBuilder().expireAfterWrite(24, TimeUnit.HOURS).build())
        caffeineCacheManager.registerCustomCache(ENHETKONTAKTINFO_CACHE, Caffeine.newBuilder().expireAfterWrite(24, TimeUnit.HOURS).build())
        return caffeineCacheManager;
    }
}