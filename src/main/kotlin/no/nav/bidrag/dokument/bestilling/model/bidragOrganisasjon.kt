package no.nav.bidrag.dokument.bestilling.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class EnhetInfo(var enhetIdent: String, var enhetNavn: String)

@JsonIgnoreProperties(ignoreUnknown = true)
data class SaksbehandlerInfoResponse(var ident: String, var navn: String)