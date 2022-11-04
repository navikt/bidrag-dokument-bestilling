package no.nav.bidrag.dokument.bestilling.model

data class DokumentBestillingRequest(
    val mottakerId: Ident,
    val samhandlerInformasjon: SamhandlerInformasjon? = null,
    val saksbehandler: Saksbehandler? = null,
    val gjelderId: Ident? = null,
    val saksnummer: String,
    val vedtaksId: String? = null,
    val dokumentReferanse: String? = null,
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