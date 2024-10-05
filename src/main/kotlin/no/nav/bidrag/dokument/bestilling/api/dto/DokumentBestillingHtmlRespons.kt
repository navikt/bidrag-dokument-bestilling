package no.nav.bidrag.dokument.bestilling.api.dto

data class DokumentBestillingHtmlRespons(
    val data: List<HtmlData>,
)

data class HtmlData(
    val data: String,
    val contentType: String,
)
