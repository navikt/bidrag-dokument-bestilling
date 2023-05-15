package no.nav.bidrag.dokument.bestilling.config

import com.fasterxml.jackson.annotation.JsonInclude
import no.nav.bidrag.commons.security.api.EnableSecurityConfiguration
import no.nav.bidrag.commons.web.config.RestOperationsAzure
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.retry.annotation.EnableRetry


@Configuration
@EnableSecurityConfiguration
@EnableRetry
@Import(RestOperationsAzure::class)
class RestConfig {

    @Bean
    fun jackson2ObjectMapperBuilder(): Jackson2ObjectMapperBuilder {
        return Jackson2ObjectMapperBuilder().serializationInclusion(JsonInclude.Include.NON_NULL).failOnUnknownProperties(false)
    }
}
