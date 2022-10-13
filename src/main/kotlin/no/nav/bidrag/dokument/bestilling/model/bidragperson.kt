package no.nav.bidrag.dokument.bestilling.model

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

data class HentPersonResponse(
    val ident: String,
    val navn: String,
    val foedselsdato: LocalDate?,
    val aktoerId: String
)

data class HentPostadresseRequest(
    var ident: String
)

data class HentPostadresseResponse(
    var adresselinje1: String?,
    var adresselinje2: String?,
    var adresselinje3: String?,
    var postnummer: String?,
    var poststed: String?,
    var land: String?
)