package no.nav.bidrag.dokument.bestilling.konsumer

import no.nav.bidrag.commons.security.service.SecurityTokenService
import no.nav.bidrag.dokument.bestilling.konfigurasjon.CacheKonfig.Companion.ENHETINFO_CACHE
import no.nav.bidrag.dokument.bestilling.konfigurasjon.CacheKonfig.Companion.ENHETKONTAKTINFO_CACHE
import no.nav.bidrag.dokument.bestilling.konfigurasjon.CacheKonfig.Companion.SAKSBEHANDLERINFO_CACHE
import no.nav.bidrag.dokument.bestilling.konsumer.dto.EnhetInfo
import no.nav.bidrag.dokument.bestilling.konsumer.dto.EnhetKontaktInfoDto
import no.nav.bidrag.dokument.bestilling.konsumer.dto.SaksbehandlerInfoResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.Cacheable
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class BidragOrganisasjonKonsumer(
    @Value("\${BIDRAG_ORGANISASJON_URL}") bidragOrgUrl: String,
    baseRestTemplate: RestTemplate,
    securityTokenService: SecurityTokenService
): DefaultKonsumer("bidrag-organisasjon", bidragOrgUrl, baseRestTemplate, securityTokenService) {

    @Cacheable(SAKSBEHANDLERINFO_CACHE)
    fun hentSaksbehandlerInfo(saksbehandlerIdent: String): SaksbehandlerInfoResponse? {
        return restTemplate.exchange(
            String.format(SAKSBEHANDLER_INFO, saksbehandlerIdent),
            HttpMethod.GET,
            null,
            SaksbehandlerInfoResponse::class.java
        ).body
    }

    @Cacheable(ENHETINFO_CACHE)
    fun hentEnhetInfo(enhetId: String): EnhetInfo? {
        return restTemplate.exchange(
            "/enhet/info/$enhetId",
            HttpMethod.GET,
            null,
            EnhetInfo::class.java
        ).body
    }

    @Cacheable(ENHETKONTAKTINFO_CACHE)
    fun hentEnhetKontaktinfo(enhetId: String, spraak: String): EnhetKontaktInfoDto? {
        return restTemplate.exchange(
            "/enhet/kontaktinfo/$enhetId/$spraak",
            HttpMethod.GET,
            null,
            EnhetKontaktInfoDto::class.java
        ).body
    }

    companion object {
        const val SAKSBEHANDLER_INFO = "/saksbehandler/info/%s"
    }
}