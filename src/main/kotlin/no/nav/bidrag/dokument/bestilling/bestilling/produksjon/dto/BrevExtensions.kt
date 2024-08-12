package no.nav.bidrag.dokument.bestilling.bestilling.produksjon.dto

import no.nav.bidrag.dokument.bestilling.bestilling.dto.VedtakDetaljer
import no.nav.bidrag.dokument.bestilling.model.BehandlingType
import no.nav.bidrag.dokument.bestilling.model.SoknadType
import no.nav.bidrag.domene.enums.person.Sivilstandskode
import no.nav.bidrag.domene.enums.rolle.Rolle
import no.nav.bidrag.domene.enums.rolle.Rolletype

fun Rolletype.toKode() =
    when (this) {
        Rolletype.BIDRAGSMOTTAKER -> "02"
        Rolletype.BIDRAGSPLIKTIG -> "01"
        Rolletype.BARN -> "04"
        else -> "00"
    }

// TODO: Dekode rollekodene til riktige verdier!!
fun Rolle.toKode() =
    when (this) {
        Rolle.BIDRAGSMOTTAKER -> "02"
        Rolle.BIDRAGSPLIKTIG -> "01"
        Rolle.SØKNADSBARN -> "03"
        else -> "00"
    }

fun Sivilstandskode.toKode() =
    when (this) {
        Sivilstandskode.ENSLIG -> "ENKE"
        Sivilstandskode.GIFT_SAMBOER -> "GIFT"
        Sivilstandskode.SAMBOER -> "SAMB"
        Sivilstandskode.BOR_ALENE_MED_BARN -> "SEPA"
//        Sivilstandskode.GJENLEVENDE_PARTNER -> "GJPA"
// //                                    SivilstandKode.GIFT_LEVER_ADSKILT -> "GLAD"
//        Sivilstandskode.REGISTRERT_PARTNER -> "REPA"
//        Sivilstandskode.SEPARERT_PARTNER -> "SEPA"
//        Sivilstandskode.SEPARERT -> "SEPR"
//        Sivilstandskode.SKILT -> "SKIL"
//        Sivilstandskode.SKILT_PARTNER -> "SKPA"
//        Sivilstandskode.UGIFT -> "UGIF"
        else -> "NULL"
    }

val VedtakDetaljer.behandlingType get(): BehandlingType? = stønadType?.let { type -> BehandlingType.from(type, engangsbelopType) } ?: engangsbelopType?.let { type -> BehandlingType.from(null, type) }
val VedtakDetaljer.soknadType get(): SoknadType? = vedtakType.let { type -> SoknadType.fromVedtakType(type) }
