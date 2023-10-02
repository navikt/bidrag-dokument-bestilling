package no.nav.bidrag.dokument.bestilling.api.dto

import no.nav.bidrag.dokument.bestilling.bestilling.dto.DokumentType
import no.nav.bidrag.dokument.bestilling.bestilling.dto.InnholdType

data class DokumentMalDetaljer(
    val tittel: String,
    val beskrivelse: String = tittel,
    val type: DokumentType,
    val kanBestilles: Boolean,
    val statiskInnhold: Boolean = false,
    val innholdType: InnholdType,
    val tilhorerEnheter: List<String> = emptyList(),
)
