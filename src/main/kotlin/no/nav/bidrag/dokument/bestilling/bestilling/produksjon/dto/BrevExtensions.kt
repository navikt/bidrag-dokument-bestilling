package no.nav.bidrag.dokument.bestilling.bestilling.produksjon.dto

import no.nav.bidrag.behandling.felles.enums.Rolle
import no.nav.bidrag.behandling.felles.enums.SivilstandKode
import no.nav.bidrag.dokument.bestilling.bestilling.dto.VedtakDetaljer
import no.nav.bidrag.dokument.bestilling.model.BehandlingType
import no.nav.bidrag.dokument.bestilling.model.SoknadType
import no.nav.bidrag.domene.enums.Rolletype

fun Rolletype.toKode() =
    when (this) {
        Rolletype.BIDRAGSMOTTAKER -> "02"
        Rolletype.BIDRAGSPLIKTIG -> "01"
        else -> "00"
    }

// TODO: Dekode rollekodene til riktige verdier!!
fun Rolle.toKode() =
    when (this) {
        Rolle.BIDRAGSMOTTAKER -> "02"
        Rolle.BIDRAGSPLIKTIG -> "01"
        Rolle.SOKNADSBARN -> "03"
        else -> "00"
    }

fun SivilstandKode.toKode() =
    when (this) {
        SivilstandKode.ENKE_ELLER_ENKEMANN, SivilstandKode.ENSLIG -> "ENKE"
        SivilstandKode.GIFT -> "GIFT"
        SivilstandKode.GJENLEVENDE_PARTNER -> "GJPA"
//                                    SivilstandKode.GIFT_LEVER_ADSKILT -> "GLAD"
        SivilstandKode.REGISTRERT_PARTNER -> "REPA"
        SivilstandKode.SAMBOER -> "SAMB"
        SivilstandKode.SEPARERT_PARTNER -> "SEPA"
        SivilstandKode.SEPARERT -> "SEPR"
        SivilstandKode.SKILT -> "SKIL"
        SivilstandKode.SKILT_PARTNER -> "SKPA"
        SivilstandKode.UGIFT -> "UGIF"
        else -> "NULL"
    }

val VedtakDetaljer.behandlingType get(): BehandlingType? = stønadType?.let { type -> BehandlingType.from(type, engangsbelopType) }
val VedtakDetaljer.soknadType get(): SoknadType? = vedtakType.let { type -> SoknadType.fromVedtakType(type) }
