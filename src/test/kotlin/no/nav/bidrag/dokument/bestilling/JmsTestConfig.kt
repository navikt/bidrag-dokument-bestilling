package no.nav.bidrag.dokument.bestilling

import jakarta.jms.ConnectionFactory
import jakarta.jms.Queue
import org.apache.activemq.ActiveMQConnectionFactory
import org.apache.activemq.command.ActiveMQQueue
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.cache.CacheManager
import org.springframework.cache.support.NoOpCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile

@TestConfiguration
class JmsTestConfig {
    @Bean
    @Profile("!nais")
    fun mqQueueConnectionFactory(): ConnectionFactory {
        return ActiveMQConnectionFactory("vm://localhost?broker.persistent=false")
    }

    @Bean
    fun onlineBrevQueue(
        @Value("\${BREVSERVER_ONLINEBREV_QUEUE}") queuename: String,
    ): Queue {
        return ActiveMQQueue(queuename)
    }

    @Bean
    @Profile("test")
    fun cacheManager(): CacheManager {
        return NoOpCacheManager()
    }
}
