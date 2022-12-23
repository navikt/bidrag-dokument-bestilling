package no.nav.bidrag.dokument.bestilling.api.dto

import no.nav.bidrag.dokument.bestilling.model.FeilSpråkKoder
import no.nav.bidrag.dokument.bestilling.model.Saksbehandler
import no.nav.bidrag.dokument.bestilling.model.SpråkKoder
import no.nav.bidrag.dokument.bestilling.model.erSamhandler

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.bidrag.dokument.bestilling.model.Ident

@Schema(description = "Metadata som brukes ved bestilling av ny dokument")
data class DokumentBestillingForespørsel(
    val mottakerId: Ident,
    @Schema(description = "Informasjon samhandler hvis mottakerid er en samhandlerid. Påkrevd hvis mottaker er en samhandler", deprecated = true)
    val samhandlerInformasjon: SamhandlerInformasjon? = null,
    @Schema(description = "Informasjon om saksbehandler som skal brukes ved opprettelse av dokument")
    val saksbehandler: Saksbehandler? = null,
    val gjelderId: Ident? = null,
    val saksnummer: String,
    val vedtaksId: String? = null,
    @Schema(deprecated = true)
    val dokumentReferanse: String? = null,
    @Schema(description = "Dokumentreferanse dokumentet skal bli opprettet med. Det vil ikke bli opprettet ny journalpost hvis dette er satt.")
    val dokumentreferanse: String? = null,
    val tittel: String? = null,
    val enhet: String? = null,
    @Schema(deprecated = true)
    val spraak: String? = null,
    val språk: String? = null,
){
    fun hentSpråk() = språk ?: spraak
    fun erMottakerSamhandler() = mottakerId.erSamhandler
    fun hentRiktigSpråkkode(): String {
        val språk = hentSpråk()
        if (språk.isNullOrEmpty()){
            return SpråkKoder.BOKMAL
        }
        return when(språk){
            FeilSpråkKoder.BOKMAL -> SpråkKoder.BOKMAL
            FeilSpråkKoder.NYNORSK -> SpråkKoder.NYNORSK
            FeilSpråkKoder.TYSK -> SpråkKoder.TYSK
            else -> språk
        }

    }
}

data class DokumentBestillingResponse(
    val dokumentId: String,
    val journalpostId: String,
    val arkivSystem: DokumentArkivSystemTo? = null,
)

enum class DokumentArkivSystemTo {
    MIDLERTIDLIG_BREVLAGER
}

data class SamhandlerAdresse(
    val adresselinje1: String? = null,
    val adresselinje2: String? = null,
    val adresselinje3: String? = null,
    val postnummer: String? = null,
    val landkode: String? = null,
)

data class SamhandlerInformasjon(
    val navn: String? = null,
    val spraak: String? = null,
    val adresse: SamhandlerAdresse? = null
)