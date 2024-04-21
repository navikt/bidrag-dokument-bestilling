package no.nav.bidrag.dokument.bestilling.model

import no.nav.bidrag.domene.enums.inntekt.Inntektsrapportering

fun Inntektsrapportering.tilLegacyKode() =
    when (this) {
        Inntektsrapportering.KONTANTSTØTTE -> "KONT"
        Inntektsrapportering.KAPITALINNTEKT_EGNE_OPPLYSNINGER -> "KIEO"
        Inntektsrapportering.DOKUMENTASJON_MANGLER_SKJØNN -> "MDOK"
        Inntektsrapportering.MANGLENDE_BRUK_EVNE_SKJØNN -> "EVNE"
        Inntektsrapportering.SYKEPENGER -> "SP"
        Inntektsrapportering.PENSJON -> "PE"
        Inntektsrapportering.INNTEKTSOPPLYSNINGER_ARBEIDSGIVER -> "AG"
        Inntektsrapportering.SMÅBARNSTILLEGG -> "ESBT"
        Inntektsrapportering.PERSONINNTEKT_EGNE_OPPLYSNINGER -> "PIEO"
        Inntektsrapportering.SAKSBEHANDLER_BEREGNET_INNTEKT -> "SAK"
        Inntektsrapportering.UTVIDET_BARNETRYGD -> "UBAT"
        Inntektsrapportering.OVERGANGSSTØNAD -> "EFOS"
        else -> name
    }
