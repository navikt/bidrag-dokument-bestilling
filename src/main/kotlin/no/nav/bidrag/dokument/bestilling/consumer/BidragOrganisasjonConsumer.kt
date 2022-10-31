package no.nav.bidrag.dokument.bestilling.consumer

import no.nav.bidrag.commons.security.service.SecurityTokenService
import no.nav.bidrag.dokument.bestilling.config.CacheConfig.Companion.ENHETINFO_CACHE
import no.nav.bidrag.dokument.bestilling.config.CacheConfig.Companion.ENHETKONTAKTINFO_CACHE
import no.nav.bidrag.dokument.bestilling.config.CacheConfig.Companion.SAKSBEHANDLERINFO_CACHE
import no.nav.bidrag.dokument.bestilling.model.EnhetInfo
import no.nav.bidrag.dokument.bestilling.model.EnhetKontaktInfoDto
import no.nav.bidrag.dokument.bestilling.model.SaksbehandlerInfoResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.Cacheable
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.util.Optional

@Service
class BidragOrganisasjonConsumer(
    @Value("\${BIDRAG_ORGANISASJON_URL}") bidragOrgUrl: String,
    baseRestTemplate: RestTemplate,
    securityTokenService: SecurityTokenService
): DefaultConsumer("bidrag-organisasjon", bidragOrgUrl, baseRestTemplate, securityTokenService) {

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
    fun hentEnhetInfo(enhetId: String): Optional<EnhetInfo> {
        return Optional.ofNullable(restTemplate.exchange(
            "/enhet/info/$enhetId",
            HttpMethod.GET,
            null,
            EnhetInfo::class.java
        ).body)
    }

    @Cacheable(ENHETKONTAKTINFO_CACHE)
    fun hentEnhetKontaktinfo(enhetId: String, spraak: String): Optional<EnhetKontaktInfoDto> {
        return Optional.ofNullable(restTemplate.exchange(
            "/enhet/kontaktinfo/$enhetId/$spraak",
            HttpMethod.GET,
            null,
            EnhetKontaktInfoDto::class.java
        ).body)
    }

    companion object {
        const val ARBEIDSFORDELING_URL = "/arbeidsfordeling/enhetsliste/geografisktilknytning/%s"
        const val SAKSBEHANDLER_INFO = "/saksbehandler/info/%s"
    }
}