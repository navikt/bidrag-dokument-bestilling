package no.nav.bidrag.dokument.bestilling.bestilling.produksjon.dto

import no.nav.bidrag.behandling.felles.enums.Rolle
import no.nav.bidrag.dokument.bestilling.bestilling.dto.GrunnlagInntektType
import no.nav.bidrag.domain.enums.Rolletype

fun Rolletype.toKode() = when (this) {
    Rolletype.BM -> "01"
    Rolletype.BP -> "02"
    Rolletype.RM -> "RM"
    else -> "00"
}

//TODO: Dekode rollekodene til riktige verdier!!
fun Rolle.toKode() = when (this) {
    Rolle.BIDRAGSMOTTAKER -> "02"
    Rolle.BIDRAGSPLIKTIG -> "01"
    Rolle.SOKNADSBARN -> "03"
    else -> "00"
}