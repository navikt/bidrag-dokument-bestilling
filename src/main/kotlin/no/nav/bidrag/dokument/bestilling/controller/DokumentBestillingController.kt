package no.nav.bidrag.dokument.bestilling.controller

import io.micrometer.core.annotation.Timed
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import no.nav.bidrag.dokument.bestilling.SECURE_LOGGER
import no.nav.bidrag.dokument.bestilling.model.BrevKode
import no.nav.bidrag.dokument.bestilling.model.DokumentBestillingRequest
import no.nav.bidrag.dokument.bestilling.model.DokumentBestillingResponse
import no.nav.bidrag.dokument.bestilling.service.DokumentBestillingService
import no.nav.security.token.support.core.api.Protected
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@RestController
@Protected
@Timed
class DokumentBestillingController(var dokumentBestillingService: DokumentBestillingService) {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(DokumentBestillingController::class.java)
    }

    @PostMapping("/bestill/{brevKode}")
    @Operation(
        description = "Bestiller dokument for oppgitt brevkode/dokumentKode",
        security = [SecurityRequirement(name = "bearer-key")],
    )
    @ApiResponses(value = [ApiResponse(responseCode = "400", description = "Dokument ble bestilt med ugyldig data")])
    fun bestillBrev(@RequestBody bestillingRequest: DokumentBestillingRequest, @PathVariable brevKode: BrevKode): DokumentBestillingResponse {
        LOGGER.info("Bestiller dokument for brevkode $brevKode og enhet ${bestillingRequest.enhet}")
        SECURE_LOGGER.info("Bestiller dokument for brevkode $brevKode med data $bestillingRequest og enhet ${bestillingRequest.enhet}")
        return dokumentBestillingService.bestill(bestillingRequest, brevKode)
    }

    @RequestMapping("/brevkoder", method = [RequestMethod.OPTIONS])
    @Operation(
        description = "Henter brevkoder som er støttet av applikasjonen",
        security = [SecurityRequirement(name = "bearer-key")],
    )
    fun hentStottedeBrevkoder(): List<String> {
        return BrevKode.values().filter { it.enabled }.map { it.name }.let {
            LOGGER.info("Hentet støttede brevkoder $it")
            it
        }
    }

}