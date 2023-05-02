package no.nav.bidrag.dokument.bestilling.konfigurasjon

import com.fasterxml.jackson.annotation.JsonInclude
import no.nav.bidrag.commons.CorrelationId
import no.nav.bidrag.commons.security.api.EnableSecurityConfiguration
import no.nav.bidrag.commons.web.HttpHeaderRestTemplate
import no.nav.bidrag.commons.web.config.RestOperationsAzure
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.actuate.metrics.web.client.MetricsRestTemplateCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Scope
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.retry.annotation.EnableRetry

@Configuration
@EnableSecurityConfiguration
@EnableRetry
@Import(RestOperationsAzure::class)
class RestConfig {

    @Bean
    fun jackson2ObjectMapperBuilder(): Jackson2ObjectMapperBuilder {
        return Jackson2ObjectMapperBuilder().serializationInclusion(JsonInclude.Include.NON_NULL)
    }
}
