package no.nav.bidrag.dokument.bestilling.service

import no.nav.bidrag.dokument.bestilling.SECURE_LOGGER
import no.nav.bidrag.dokument.bestilling.model.BrevKode
import no.nav.bidrag.dokument.bestilling.model.DokumentBestillingRequest
import no.nav.bidrag.dokument.bestilling.model.DokumentBestillingResponse
import no.nav.bidrag.dokument.bestilling.collector.DokumentBestillingManager
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.context.annotation.RequestScope

@Service
class DokumentBestillingService(var dokumentBestillingManager: DokumentBestillingManager) {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(DokumentBestillingService::class.java)
    }

    fun bestill(bestillingRequest: DokumentBestillingRequest, brevKode: BrevKode): DokumentBestillingResponse {
        LOGGER.info("Bestiller dokument for brevkode $brevKode og enhet ${bestillingRequest.enhet}")
        SECURE_LOGGER.info("Bestiller dokument for brevkode $brevKode med data $bestillingRequest og enhet ${bestillingRequest.enhet}")
        val result = dokumentBestillingManager.bestill(bestillingRequest, brevKode)
        return DokumentBestillingResponse(
            dokumentId = result.dokumentReferanse,
            journalpostId =result.journalpostId
        )
    }
}