package no.nav.bidrag.dokument.bestilling.model

data class DokumentBestillingRequest(
    var mottakerId: String,
    var gjelderId: String,
    var saksnummer: String,
    var vedtaksId: String? = null,
    var dokumentReferanse: String? = null,
    var tittel: String? = null,
)

data class DokumentBestillingResponse(
    var dokumentId: String,
    var journalpostId: String,
    var arkivSystem: String? = null,
)