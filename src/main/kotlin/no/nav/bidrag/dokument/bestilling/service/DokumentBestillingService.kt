package no.nav.bidrag.dokument.bestilling.service

import no.nav.bidrag.dokument.bestilling.collector.DokumentBestillingManager
import no.nav.bidrag.dokument.bestilling.model.BrevKode
import no.nav.bidrag.dokument.bestilling.model.DokumentBestillingRequest
import no.nav.bidrag.dokument.bestilling.model.DokumentBestillingResponse
import org.springframework.stereotype.Service

@Service
class DokumentBestillingService(var dokumentBestillingManager: DokumentBestillingManager) {

    fun bestill(bestillingRequest: DokumentBestillingRequest, brevKode: BrevKode): DokumentBestillingResponse {

        val result = dokumentBestillingManager.bestill(bestillingRequest, brevKode)
        return DokumentBestillingResponse(
            dokumentId = result.dokumentReferanse,
            journalpostId = result.journalpostId
        )
    }
}