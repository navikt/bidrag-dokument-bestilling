package no.nav.bidrag.dokument.bestilling.konsumer

import no.nav.bidrag.commons.security.service.SecurityTokenService
import no.nav.bidrag.dokument.bestilling.konfigurasjon.CacheKonfig.Companion.ENHETKONTAKTINFO_CACHE
import no.nav.bidrag.dokument.bestilling.konfigurasjon.CacheKonfig.Companion.SAKSBEHANDLERINFO_CACHE
import no.nav.bidrag.organisasjon.dto.SaksbehandlerDto
import no.nav.bidrag.transport.organisasjon.EnhetKontaktinfoDto
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.Cacheable
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.exchange

@Service
class BidragOrganisasjonKonsumer(
    @Value("\${BIDRAG_ORGANISASJON_URL}") bidragOrgUrl: String,
    baseRestTemplate: RestTemplate,
    securityTokenService: SecurityTokenService
) : DefaultKonsumer("bidrag-organisasjon", bidragOrgUrl, baseRestTemplate, securityTokenService) {

    @Cacheable(SAKSBEHANDLERINFO_CACHE)
    fun hentSaksbehandlerInfo(saksbehandlerIdent: String): SaksbehandlerDto? {
        return restTemplate.exchange<SaksbehandlerDto>(
            String.format(SAKSBEHANDLER_INFO, saksbehandlerIdent),
            HttpMethod.GET,
            null
        ).body
    }

    @Cacheable(ENHETKONTAKTINFO_CACHE)
    fun hentEnhetKontaktinfo(enhetId: String, spraak: String): EnhetKontaktinfoDto? {
        return restTemplate.exchange<EnhetKontaktinfoDto>(
            "/enhet/kontaktinfo/$enhetId/$spraak",
            HttpMethod.GET,
            null
        ).body
    }

    companion object {
        const val SAKSBEHANDLER_INFO = "/saksbehandler/info/%s"
    }
}
