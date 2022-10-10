package no.nav.bidrag.dokument.bestilling.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import no.nav.bidrag.commons.web.EnhetFilter
import no.nav.bidrag.dokument.bestilling.model.BrevKode
import no.nav.bidrag.dokument.bestilling.model.DokumentBestillingRequest
import no.nav.bidrag.dokument.bestilling.model.DokumentBestillingResponse
import no.nav.bidrag.dokument.bestilling.service.DokumentBestillingService
import no.nav.security.token.support.core.api.Protected
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController

@RestController
@Protected
class DokumentBestillingController(
    var dokumentBestillingService: DokumentBestillingService
) {

    @PostMapping("/bestill/{brevKode}")
    @Operation(
        description = "Bestiller dokument for oppgitt brevkode",
        security = [SecurityRequirement(name = "bearer-key")],
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Hentet person data"),
            ApiResponse(responseCode = "404", description = "Fant ikke person"),
            ApiResponse(responseCode = "401", description = "Sikkerhetstoken er ikke gyldig"),
            ApiResponse(
                responseCode = "403",
                description = "Sikkerhetstoken er ikke gyldig, eller det er ikke gitt adgang til kode 6 og 7 (nav-ansatt)"
            ),
        ]
    )
    fun hentDialog(@RequestBody request: DokumentBestillingRequest, @PathVariable brevKode: BrevKode, @RequestHeader(EnhetFilter.X_ENHET_HEADER) enhet: String): DokumentBestillingResponse? {
        return dokumentBestillingService.bestill(request, brevKode, enhet)
    }

}