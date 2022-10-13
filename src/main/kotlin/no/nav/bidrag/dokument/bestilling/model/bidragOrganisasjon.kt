package no.nav.bidrag.dokument.bestilling.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import io.swagger.v3.oas.annotations.media.Schema

@JsonIgnoreProperties(ignoreUnknown = true)
data class EnhetInfo(var enhetIdent: String, var enhetNavn: String)

@JsonIgnoreProperties(ignoreUnknown = true)
data class SaksbehandlerInfoResponse(var ident: String, var navn: String)


@JsonIgnoreProperties(ignoreUnknown = true)
data class EnhetKontaktInfoDto(
    var enhetIdent: String? = null,
    var enhetNavn: String? = null,
    var telefonnummer: String? = null,
    var postadresse: EnhetPostadresseDto? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class EnhetPostadresseDto(
    var postnummer: String? = null,
    var postboksnummer: String? = null,
    var postboksanlegg: String? = null,
    var adresselinje: String? = null,
)