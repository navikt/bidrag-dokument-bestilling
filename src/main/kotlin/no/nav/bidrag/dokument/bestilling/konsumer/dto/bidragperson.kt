package no.nav.bidrag.dokument.bestilling.konsumer.dto

import no.nav.bidrag.dokument.bestilling.model.DISREKSJONSKODE_KODE_6
import java.time.LocalDate

data class HentPersonResponse(
    val ident: String,
    val navn: String? = null,
    val kortNavn: String? = null,
    val foedselsdato: LocalDate? = null,
    val doedsdato: LocalDate? = null,
    val aktoerId: String? = null,
    val diskresjonskode: String? = null,
) {

    val isKode6 = diskresjonskode == DISREKSJONSKODE_KODE_6
    val isDod = doedsdato != null
    val fornavnEtternavn
        get(): String = run {
            if (navn.isNullOrEmpty()) return ""
            val navnSplit = navn.split(",")
            val fornavnMellomnavn = if (navnSplit.size == 2) navnSplit[1] else navnSplit[0]
            val etternavn = if (navnSplit.size == 2) navnSplit[0] else ""
            "$fornavnMellomnavn $etternavn"
        }

    val fornavn
        get(): String = run {
            if (navn.isNullOrEmpty()) return ""
            val navnSplit = navn.split(",")
            val fornavnMellomnavn = if (navnSplit.size == 2) navnSplit[1] else navnSplit[0]
            fornavnMellomnavn.trim().split(" ")[0]
        }
}

data class HentPersonInfoRequest(
    var ident: String
)

data class HentPostadresseResponse(
    val adresselinje1: String?,
    val adresselinje2: String?,
    val adresselinje3: String?,
    val bruksenhetsnummer: String?,
    val postnummer: String?,
    val poststed: String?,
    val land: String?,
    val land3: String? = null
)