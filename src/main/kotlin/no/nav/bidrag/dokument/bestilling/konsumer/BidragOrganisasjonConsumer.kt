package no.nav.bidrag.dokument.bestilling.konsumer

import no.nav.bidrag.commons.security.service.SecurityTokenService
import no.nav.bidrag.commons.web.client.AbstractRestClient
import no.nav.bidrag.dokument.bestilling.konfigurasjon.CacheConfig.Companion.ENHETINFO_CACHE
import no.nav.bidrag.dokument.bestilling.konfigurasjon.CacheConfig.Companion.ENHETKONTAKTINFO_CACHE
import no.nav.bidrag.dokument.bestilling.konfigurasjon.CacheConfig.Companion.SAKSBEHANDLERINFO_CACHE
import no.nav.bidrag.dokument.bestilling.konsumer.dto.EnhetInfo
import no.nav.bidrag.dokument.bestilling.konsumer.dto.EnhetKontaktInfoDto
import no.nav.bidrag.dokument.bestilling.konsumer.dto.SaksbehandlerInfoResponse
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.Cacheable
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.web.client.RestOperations
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

@Service
class BidragOrganisasjonConsumer(
    @Value("\${BIDRAG_ORGANISASJON_URL}") val url: URI,
    @Qualifier("azure") private val restTemplate: RestOperations
) : AbstractRestClient(restTemplate, "bidrag-organisasjon"){
    private fun createUri(path: String?) = UriComponentsBuilder.fromUri(url)
        .path(path ?: "").build().toUri()
    @Cacheable(SAKSBEHANDLERINFO_CACHE)
    fun hentSaksbehandlerInfo(saksbehandlerIdent: String): SaksbehandlerInfoResponse? {
        return getForEntity(createUri("/saksbehandler/info/$saksbehandlerIdent"))
    }

    @Cacheable(ENHETINFO_CACHE)
    fun hentEnhetInfo(enhetId: String): EnhetInfo? {
        return getForEntity(createUri("/enhet/info/$enhetId"))
    }

    @Cacheable(ENHETKONTAKTINFO_CACHE)
    fun hentEnhetKontaktinfo(enhetId: String, spraak: String): EnhetKontaktInfoDto? {
        return getForEntity(createUri("/enhet/kontaktinfo/$enhetId/$spraak"))
    }
}
