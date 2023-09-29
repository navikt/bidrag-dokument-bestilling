package no.nav.bidrag.dokument.bestilling.api.dto

import no.nav.bidrag.dokument.bestilling.bestilling.dto.DokumentType

data class DokumentMalDetaljer(
    val beskrivelse: String,
    val tittel: String = beskrivelse,
    val type: DokumentType,
    val kanBestilles: Boolean
)
