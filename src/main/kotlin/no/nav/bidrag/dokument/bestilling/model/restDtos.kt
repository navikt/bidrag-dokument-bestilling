package no.nav.bidrag.dokument.bestilling.model

data class DokumentBestillingRequest(
    val mottakerId: String,
    val mottakerKontaktInformasjon: Kontaktinformasjon? = null,
    val gjelderId: String,
    val saksnummer: String,
    val vedtaksId: String? = null,
    val dokumentReferanse: String? = null,
    val tittel: String? = null,
    val enhet: String? = null,
    val spraak: String? = null,
){
    fun isMottakerSamhandler() = mottakerId.matches("^[8-9][0-9]{10}$".toRegex())
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

data class Kontaktinformasjon(
    var navn: String? = null,
    var adresselinje1: String? = null,
    var adresselinje2: String? = null,
    var adresselinje3: String? = null,
    var postnummer: String? = null,
    var landkode: String? = null,
    var spraak: String? = null
)
