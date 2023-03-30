package no.nav.bidrag.dokument.bestilling.konsumer

import no.nav.bidrag.commons.security.service.SecurityTokenService
import org.springframework.boot.web.client.RootUriTemplateHandler
import org.springframework.web.client.RestTemplate

open class DefaultKonsumer(var restTemplate: RestTemplate) {

    constructor(clientId: String, baseUrl: String, restTemplate: RestTemplate, securityTokenService: SecurityTokenService) : this(restTemplate) {
        restTemplate.uriTemplateHandler = RootUriTemplateHandler(baseUrl)
        restTemplate.interceptors.add(securityTokenService.authTokenInterceptor(clientId))
    }
}
