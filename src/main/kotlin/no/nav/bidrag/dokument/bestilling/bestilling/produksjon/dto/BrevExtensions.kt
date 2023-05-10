package no.nav.bidrag.dokument.bestilling.bestilling.produksjon.dto

import no.nav.bidrag.behandling.felles.enums.Rolle
import no.nav.bidrag.behandling.felles.enums.SivilstandKode
import no.nav.bidrag.dokument.bestilling.bestilling.dto.VedtakDetaljer
import no.nav.bidrag.dokument.bestilling.model.BehandlingType
import no.nav.bidrag.dokument.bestilling.model.SoknadType
import no.nav.bidrag.domain.enums.Rolletype

fun Rolletype.toKode() = when (this) {
    Rolletype.BM -> "01"
    Rolletype.BP -> "02"
    Rolletype.RM -> "RM"
    else -> "00"
}

// TODO: Dekode rollekodene til riktige verdier!!
fun Rolle.toKode() = when (this) {
    Rolle.BIDRAGSMOTTAKER -> "02"
    Rolle.BIDRAGSPLIKTIG -> "01"
    Rolle.SOKNADSBARN -> "03"
    else -> "00"
}
fun SivilstandKode.toKode() = when (this) {
    SivilstandKode.ENKE_ELLER_ENKEMANN -> "ENKE"
    SivilstandKode.GIFT -> "GIFT"
    SivilstandKode.GJENLEVENDE_PARTNER -> "GJPA"
//                                    SivilstandKode.GIFT_LEVER_ADSKILT -> "GLAD"
    SivilstandKode.REGISTRERT_PARTNER -> "REPA"
    SivilstandKode.SAMBOER -> "SAMB"
    SivilstandKode.SEPARERT_PARTNER -> "SEPA"
    SivilstandKode.SEPARERT -> "SEPR"
    SivilstandKode.SKILT, SivilstandKode.ENSLIG -> "SKIL"
    SivilstandKode.SKILT_PARTNER -> "SKPA"
    SivilstandKode.UGIFT -> "UGIF"
    else -> "NULL"
}

val VedtakDetaljer.behandlingType get(): BehandlingType? = stønadType?.let { type -> BehandlingType.valueOf(type.name) }
val VedtakDetaljer.soknadType get(): SoknadType? = vedtakType.let { type -> SoknadType.valueOf(type.name) }
