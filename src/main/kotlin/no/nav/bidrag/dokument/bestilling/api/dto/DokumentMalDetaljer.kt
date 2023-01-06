package no.nav.bidrag.dokument.bestilling.api.dto

import no.nav.bidrag.dokument.bestilling.bestilling.dto.BrevType

data class DokumentMalDetaljer(
    val beskrivelse: String,
    val type: BrevType
)