package no.nav.bidrag.dokument.bestilling.api

import io.micrometer.core.annotation.Timed
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import no.nav.bidrag.dokument.bestilling.SIKKER_LOGG
import no.nav.bidrag.dokument.bestilling.api.dto.DokumentBestillingForespørsel
import no.nav.bidrag.dokument.bestilling.api.dto.DokumentBestillingResponse
import no.nav.bidrag.dokument.bestilling.api.dto.DokumentMalDetaljer
import no.nav.bidrag.dokument.bestilling.bestilling.dto.DokumentMal
import no.nav.bidrag.dokument.bestilling.tjenester.DokumentBestillingService
import no.nav.security.token.support.core.api.Protected
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@RestController
@Protected
@Timed
class DokumentBestillingKontroller(val dokumentBestillingService: DokumentBestillingService) {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(DokumentBestillingKontroller::class.java)
    }

    @PostMapping("/bestill/{dokumentMal}")
    @Operation(
        description = "Bestiller dokument for oppgitt brevkode/dokumentKode",
        security = [SecurityRequirement(name = "bearer-key")]
    )
    @ApiResponses(value = [ApiResponse(responseCode = "400", description = "Dokument ble bestilt med ugyldig data")])
    fun bestillBrev(@RequestBody bestillingRequest: DokumentBestillingForespørsel, @PathVariable dokumentMal: DokumentMal): DokumentBestillingResponse {
        LOGGER.info("Bestiller dokument for brevkode $dokumentMal og enhet ${bestillingRequest.enhet}")
        SIKKER_LOGG.info("Bestiller dokument for brevkode $dokumentMal med data $bestillingRequest og enhet ${bestillingRequest.enhet}")
        val result = dokumentBestillingService.bestill(bestillingRequest, dokumentMal)
        LOGGER.info("Bestilt dokument for brevkode $dokumentMal og enhet ${bestillingRequest.enhet} med respons $result")
        return result
    }

    @RequestMapping("/brevkoder", method = [RequestMethod.OPTIONS])
    @Operation(
        description = "Henter brevkoder som er støttet av applikasjonen",
        security = [SecurityRequirement(name = "bearer-key")]
    )
    fun hentStottedeBrevkoder(): List<String> {
        return DokumentMal.values().filter { it.enabled }.map { it.name }.let {
            LOGGER.info("Hentet støttede brevkoder $it")
            it
        }
    }

    @GetMapping("/dokumentmal/detaljer")
    @Operation(
        description = "Henter detaljer om alle støttede dokumentmaler",
        security = [SecurityRequirement(name = "bearer-key")]
    )
    fun hentDokumentmalDetaljer(): Map<String, DokumentMalDetaljer> {
        return DokumentMal.values()
            .associate { it.name to DokumentMalDetaljer(it.beskrivelse, it.brevtype) }
    }
}
