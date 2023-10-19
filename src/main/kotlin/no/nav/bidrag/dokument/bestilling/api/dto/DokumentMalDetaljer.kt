package no.nav.bidrag.dokument.bestilling.api.dto

import no.nav.bidrag.dokument.bestilling.bestilling.dto.DokumentType
import no.nav.bidrag.dokument.bestilling.bestilling.dto.InnholdType
import no.nav.bidrag.dokument.bestilling.bestilling.dto.StøttetSpråk

data class DokumentMalDetaljer(
    val tittel: String,
    val beskrivelse: String = tittel,
    val type: DokumentType,
    val kanBestilles: Boolean,
    val redigerbar: Boolean,
    val språk: List<StøttetSpråk>,
    val statiskInnhold: Boolean = false,
    val innholdType: InnholdType,
    val gruppeVisningsnavn: String? = null,
    val tilhorerEnheter: List<String> = emptyList(),
)
