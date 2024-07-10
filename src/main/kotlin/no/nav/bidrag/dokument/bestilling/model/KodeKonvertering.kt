package no.nav.bidrag.dokument.bestilling.model

import no.nav.bidrag.domene.enums.beregning.Resultatkode
import no.nav.bidrag.domene.enums.diverse.Språk
import no.nav.bidrag.domene.enums.inntekt.Inntektsrapportering
import no.nav.bidrag.domene.util.visningsnavn

fun Inntektsrapportering.tilLegacyKode() =
    when (this) {
        Inntektsrapportering.KONTANTSTØTTE -> "KONT"
        Inntektsrapportering.LIGNINGSINNTEKT -> "LIGS"
        Inntektsrapportering.KAPITALINNTEKT -> "KAPS"
        Inntektsrapportering.KAPITALINNTEKT_EGNE_OPPLYSNINGER -> "KIEO"
        Inntektsrapportering.DOKUMENTASJON_MANGLER_SKJØNN -> "MDOK"
        Inntektsrapportering.MANGLENDE_BRUK_AV_EVNE_SKJØNN -> "EVNE"
        Inntektsrapportering.SYKEPENGER -> "SP"
        Inntektsrapportering.PENSJON -> "PE"
        Inntektsrapportering.INNTEKTSOPPLYSNINGER_FRA_ARBEIDSGIVER -> "AG"
        Inntektsrapportering.SMÅBARNSTILLEGG -> "ESBT"
        Inntektsrapportering.PERSONINNTEKT_EGNE_OPPLYSNINGER -> "PIEO"
        Inntektsrapportering.SAKSBEHANDLER_BEREGNET_INNTEKT -> "SAK"
        Inntektsrapportering.UTVIDET_BARNETRYGD -> "UBAT"
        Inntektsrapportering.OVERGANGSSTØNAD -> "EFOS"
        else -> name
    }

val visningsnavnSomKreverÅrstall = listOf(Inntektsrapportering.LIGNINGSINNTEKT)

fun Inntektsrapportering.visningsnavnBruker(
    språk: Språk,
    årstall: Int?,
) = if (visningsnavnSomKreverÅrstall.contains(this)) {
    "${visningsnavn.bruker[språk]} ${årstall ?: ""}".trim()
} else {
    visningsnavn.bruker[språk] ?: ""
}

val Resultatkode.legacyKodeBrev
    get() =
        run {
            legacyKode.ifEmpty {
                when (type.first()) {
                    Resultatkode.ResultatkodeType.AVSLAG -> "A"
                    Resultatkode.ResultatkodeType.OPPHØR -> "OH"
                    else -> ""
                }
            }
        }
