package no.nav.bidrag.dokument.bestilling.model

data class DokumentBestillingRequest(
    var mottakerId: String,
    var mottakerKontaktInformasjon: Kontaktinformasjon? = null,
    var gjelderId: String,
    var saksnummer: String,
    var vedtaksId: String? = null,
    var dokumentReferanse: String? = null,
    var tittel: String? = null,
    var enhet: String? = null,
    var spraak: String? = null,
){
    fun isMottakerSamhandler() = mottakerId.matches("^[8-9][0-9]{10}$".toRegex())
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
