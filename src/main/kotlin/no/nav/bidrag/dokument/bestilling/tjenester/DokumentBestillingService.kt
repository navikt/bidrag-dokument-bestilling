package no.nav.bidrag.dokument.bestilling.tjenester

import no.nav.bidrag.dokument.bestilling.api.dto.DokumentBestillingForespørsel
import no.nav.bidrag.dokument.bestilling.api.dto.DokumentBestillingResponse
import no.nav.bidrag.dokument.bestilling.bestilling.DokumentBestillingManager
import no.nav.bidrag.dokument.bestilling.bestilling.dto.BestillingSystem
import no.nav.bidrag.dokument.bestilling.bestilling.dto.BrevKode
import no.nav.bidrag.transport.dokument.DokumentArkivSystemDto
import org.springframework.stereotype.Service

@Service
class DokumentBestillingService(val dokumentBestillingManager: DokumentBestillingManager) {

    fun bestill(bestillingRequest: DokumentBestillingForespørsel, brevKode: BrevKode): DokumentBestillingResponse {
        val result = dokumentBestillingManager.bestill(bestillingRequest, brevKode)
        return DokumentBestillingResponse(
            dokumentId = result.dokumentReferanse,
            journalpostId = result.journalpostId,
            arkivSystem = when (result.bestillingSystem) {
                BestillingSystem.BREVSERVER -> DokumentArkivSystemDto.MIDLERTIDLIG_BREVLAGER
                else -> null
            }
        )
    }
}
