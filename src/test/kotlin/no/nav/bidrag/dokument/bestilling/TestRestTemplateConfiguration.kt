package no.nav.bidrag.dokument.bestilling

import no.nav.bidrag.commons.web.test.HttpHeaderTestRestTemplate
import no.nav.bidrag.dokument.bestilling.utils.SAKSBEHANDLER_IDENT
import no.nav.security.mock.oauth2.MockOAuth2Server
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpHeaders

@Configuration
@Profile("test")
class TestRestTemplateConfiguration {
    @Autowired
    lateinit var mockOAuth2Server: MockOAuth2Server

    @Value("\${AZURE_APP_CLIENT_ID}")
    private lateinit var clientId: String

    @Bean
    fun httpHeaderTestRestTemplate(): HttpHeaderTestRestTemplate {
        val testRestTemplate = TestRestTemplate(RestTemplateBuilder())
        val httpHeaderTestRestTemplate = HttpHeaderTestRestTemplate(testRestTemplate)
        httpHeaderTestRestTemplate.add(HttpHeaders.AUTHORIZATION) { generateBearerToken() }
        return httpHeaderTestRestTemplate
    }

    private fun generateBearerToken(): String {
        val token = mockOAuth2Server.issueToken("aad", SAKSBEHANDLER_IDENT, clientId, claims = mapOf("azp_name" to "bidrag-dokument-bestilling-test"))
        return "Bearer " + token.serialize()
    }
}
