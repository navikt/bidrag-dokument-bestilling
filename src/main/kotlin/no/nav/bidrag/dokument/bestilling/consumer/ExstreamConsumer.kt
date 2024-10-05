package no.nav.bidrag.dokument.bestilling.consumer

import no.nav.bidrag.commons.web.HttpHeaderRestTemplate
import no.nav.bidrag.dokument.bestilling.consumer.dto.ExstreamHtmDataContent
import no.nav.bidrag.dokument.bestilling.consumer.dto.ExstreamHtmlResponseDto
import no.nav.bidrag.dokument.bestilling.consumer.dto.ExstreamTokenRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.DefaultUriBuilderFactory
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@Service
class ExstreamConsumer(
    @Value("\${EXSTREAM_URL}") val url: String,
    @Value("\${EXSTREAM_USERNAME}") val username: String,
    @Value("\${EXSTREAM_PASSWORD}") val password: String,
) {
    private final val restTemplate: RestTemplate

    init {
        val restTemplate = HttpHeaderRestTemplate()
        restTemplate.uriTemplateHandler = DefaultUriBuilderFactory(url)
        restTemplate.addHeaderGenerator(HttpHeaders.CONTENT_TYPE) { MediaType.APPLICATION_JSON_VALUE }
        restTemplate.addHeaderGenerator("OTDSTicket") { hentToken() }
        this.restTemplate = restTemplate
    }

    fun hentToken(): String {
        val restTemplate = HttpHeaderRestTemplate()
        restTemplate.uriTemplateHandler = DefaultUriBuilderFactory(url)
        val response =
            restTemplate.postForObject(
                "/otdstenant/tenant5/otdsws/rest/authentication/credentials",
                HttpEntity(
                    ExstreamTokenRequest(
                        username,
                        password,
                    ),
                ),
                TokenResponse::class.java,
            )!!
        return response.ticket
    }

    @OptIn(ExperimentalEncodingApi::class)
    fun hentBrevHtml(xml: String): ExstreamHtmlResponseDto? =
        restTemplate
            .postForEntity(
                "/tenant5/sgw/v1/communications?name=bidrag_xml_html&version=1",
                HttpEntity(
                    ExstreamHtmDataContent(
                        contentType = "text/xml",
                        data = Base64.encode(xml.toByteArray()),
                    ),
                ),
                ExstreamHtmlResponseDto::class.java,
            ).body

    companion object {
        private const val DEFAULT_CACHE = "DEFAULT"
    }
}

data class TokenResponse(
    val ticket: String,
)
