package no.nav.bidrag.dokument.bestilling.service

import no.nav.bidrag.dokument.bestilling.model.BrevKode
import no.nav.bidrag.dokument.bestilling.model.DokumentBestillingRequest
import no.nav.bidrag.dokument.bestilling.model.DokumentBestillingResponse
import no.nav.bidrag.dokument.bestilling.collector.DokumentBestillingManager
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class DokumentBestillingService(var dokumentBestillingManager: DokumentBestillingManager) {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(DokumentBestillingService::class.java)
    }

    fun bestill(bestillingRequest: DokumentBestillingRequest, brevKode: BrevKode, enhet: String): DokumentBestillingResponse {
        LOGGER.info("Bestiller dokument for brevkode=$brevKode")
        val result = dokumentBestillingManager.bestill(bestillingRequest, brevKode, enhet)
        return DokumentBestillingResponse(
            dokumentId = result.dokumentReferanse,
            journalpostId =result.journalpostId
        )
    }
}