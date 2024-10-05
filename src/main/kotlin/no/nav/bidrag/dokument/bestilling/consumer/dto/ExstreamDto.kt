package no.nav.bidrag.dokument.bestilling.consumer.dto

import com.fasterxml.jackson.annotation.JsonAlias
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

data class ExstreamTokenRequest(
    val userName: String,
    val password: String,
)

data class ExstreamHtmlResponseDto(
    val status: String,
    val data: ExstreamHtmlData,
)

data class ExstreamHtmlData(
    val id: String,
    val attributeMap: Map<String, String>,
    val result: List<ExstreamHtmDataResult>,
)

data class ExstreamHtmDataResult(
    @JsonAlias("Content-Disposition")
    val contentDisposition: List<String>,
    val content: ExstreamHtmDataContent,
)

@OptIn(ExperimentalEncodingApi::class)
data class ExstreamHtmDataContent(
    val data: String,
    val contentType: String,
    val streamingFilePath: String? = null,
    val empty: Boolean = false,
) {
    val dataParsed get() = Base64.decode(data)
}
