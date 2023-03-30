package no.nav.bidrag.dokument.bestilling.konsumer

import no.nav.bidrag.commons.security.service.SecurityTokenService
import no.nav.bidrag.dokument.dto.OpprettJournalpostRequest
import no.nav.bidrag.dokument.dto.OpprettJournalpostResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class BidragDokumentKonsumer(
    @Value("\${BIDRAG_DOKUMENT_URL}") bidragDokumentUrl: String,
    baseRestTemplate: RestTemplate,
    securityTokenService: SecurityTokenService
) : DefaultKonsumer("bidrag-dokument", bidragDokumentUrl, baseRestTemplate, securityTokenService) {

    fun opprettJournalpost(opprettJournalpostRequest: OpprettJournalpostRequest): OpprettJournalpostResponse? {
        return restTemplate.exchange(
            "/journalpost/BIDRAG",
            HttpMethod.POST,
            HttpEntity(opprettJournalpostRequest),
            OpprettJournalpostResponse::class.java
        ).body
    }
}
