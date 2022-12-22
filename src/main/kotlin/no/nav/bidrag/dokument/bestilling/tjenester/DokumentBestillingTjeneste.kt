package no.nav.bidrag.dokument.bestilling.tjenester

import no.nav.bidrag.dokument.bestilling.api.dto.DokumentBestillingForespørsel
import no.nav.bidrag.dokument.bestilling.api.dto.DokumentBestillingResponse
import no.nav.bidrag.dokument.bestilling.bestilling.DokumentBestillingManager
import no.nav.bidrag.dokument.bestilling.model.BrevKode
import org.springframework.stereotype.Service

@Service
class DokumentBestillingTjeneste(val dokumentBestillingManager: DokumentBestillingManager) {

    fun bestill(bestillingRequest: DokumentBestillingForespørsel, brevKode: BrevKode): DokumentBestillingResponse {

        val result = dokumentBestillingManager.bestill(bestillingRequest, brevKode)
        return DokumentBestillingResponse(
            dokumentId = result.dokumentReferanse,
            journalpostId = result.journalpostId
        )
    }
}