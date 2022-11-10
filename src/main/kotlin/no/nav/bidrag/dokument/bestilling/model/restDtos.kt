package no.nav.bidrag.dokument.bestilling.model

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Metadata som brukes ved bestilling av ny dokument")
data class DokumentBestillingRequest(
    val mottakerId: Ident,
    @Schema(description = "Informasjon samhandler hvis mottakerid er en samhandlerid. Påkrevd hvis mottaker er en samhandler") val samhandlerInformasjon: SamhandlerInformasjon? = null,
    @Schema(description = "Informasjon om saksbehandler som skal brukes ved opprettelse av dokument") val saksbehandler: Saksbehandler? = null,
    val gjelderId: Ident? = null,
    val saksnummer: String,
    val vedtaksId: String? = null,
    @Schema(description = "Dokumentreferanse dokumentet skal bli opprettet med. Det vil ikke bli opprettet ny journalpost hvis dette er satt.") val dokumentReferanse: String? = null,
    val tittel: String? = null,
    val enhet: String? = null,
    val spraak: String? = null,
){
    fun isMottakerSamhandler() = mottakerId.isSamhandler
    fun hentRiktigSpraakkode(): String {
        if (spraak.isNullOrEmpty()){
            return SpraakKoder.BOKMAL
        }
       return when(spraak){
           FeilSpraakKoder.BOKMAL -> SpraakKoder.BOKMAL
           FeilSpraakKoder.NYNORSK -> SpraakKoder.NYNORSK
           FeilSpraakKoder.TYSK -> SpraakKoder.TYSK
           else -> spraak
       }

    }
}

data class DokumentBestillingResponse(
    var dokumentId: String,
    var journalpostId: String,
    var arkivSystem: String? = null,
)

data class SamhandlerAdresse(
    var adresselinje1: String? = null,
    var adresselinje2: String? = null,
    var adresselinje3: String? = null,
    var postnummer: String? = null,
    var landkode: String? = null,
)

data class SamhandlerInformasjon(
    var navn: String? = null,
    var spraak: String? = null,
    var adresse: SamhandlerAdresse? = null
)