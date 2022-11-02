package no.nav.bidrag.dokument.bestilling.model

import java.time.LocalDate

data class HentPersonResponse(
    val ident: String,
    val navn: String,
    val foedselsdato: LocalDate?,
    val doedsdato: LocalDate?,
    val aktoerId: String,
    val diskresjonskode: String? = null,
    ){

    val isKode6 = diskresjonskode == DISREKSJONSKODE_KODE_6
    val fornavnEtternavn get () = run {
            val navnSplit = navn.split(",")
            val fornavnMellomnavn = if (navnSplit.size == 2) navnSplit[1] else navnSplit[0]
            val etternavn = if (navnSplit.size == 2) navnSplit[0] else ""
            "$fornavnMellomnavn $etternavn"
    }

    val fornavn get() = run {
        val navnSplit = navn.split(",")
        val fornavnMellomnavn = if (navnSplit.size == 2) navnSplit[1] else navnSplit[0]
        fornavnMellomnavn.trim().split(" ")[0]
    }
}

data class HentPostadresseRequest(
    var ident: String
)

data class HentPostadresseResponse(
    var adresselinje1: String?,
    var adresselinje2: String?,
    var adresselinje3: String?,
    var bruksenhetsnummer: String?,
    var postnummer: String?,
    var poststed: String?,
    var land: String?
)