package no.nav.bidrag.dokument.bestilling

import com.github.benmanes.caffeine.cache.Caffeine
import no.nav.bidrag.dokument.bestilling.config.CacheConfig
import org.apache.activemq.ActiveMQConnectionFactory
import org.apache.activemq.command.ActiveMQQueue
import org.junit.ClassRule
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.cache.CacheManager
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.cache.support.NoOpCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile
import org.springframework.jms.config.DefaultJmsListenerContainerFactory
import org.springframework.jms.config.JmsListenerContainerFactory
import java.util.concurrent.TimeUnit
import javax.jms.ConnectionFactory
import javax.jms.Queue


@TestConfiguration
class JmsTestConfig {

    @Bean
    @Profile("!nais")
    fun mqQueueConnectionFactory(): ConnectionFactory {
        return ActiveMQConnectionFactory("vm://localhost?broker.persistent=false")
    }

    @Bean
    fun onlineBrevQueue(@Value("\${BREVSERVER_ONLINEBREV_QUEUE}") queuename: String): Queue {
        return ActiveMQQueue(queuename)
    }

    @Bean
    @Profile("test")
    fun cacheManager(): CacheManager {
        return NoOpCacheManager()
    }
}