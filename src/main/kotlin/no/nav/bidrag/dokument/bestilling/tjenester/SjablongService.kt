package no.nav.bidrag.dokument.bestilling.tjenester

import no.nav.bidrag.dokument.bestilling.bestilling.dto.BeløpFraTil
import no.nav.bidrag.dokument.bestilling.bestilling.dto.ForsorgerType
import no.nav.bidrag.dokument.bestilling.bestilling.dto.GrunnlagForskuddPeriode
import no.nav.bidrag.dokument.bestilling.bestilling.dto.PeriodeFraTom
import no.nav.bidrag.dokument.bestilling.bestilling.dto.SjablonDetaljer
import no.nav.bidrag.dokument.bestilling.consumer.SjablonConsumer
import no.nav.bidrag.dokument.bestilling.consumer.dto.SjablongType
import no.nav.bidrag.dokument.bestilling.consumer.dto.hentPerioderForSjabloner
import no.nav.bidrag.dokument.bestilling.consumer.dto.hentSisteSjablong
import no.nav.bidrag.dokument.bestilling.consumer.dto.hentSjablongForTomDato
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDate

@Service
class SjablongService(val sjablonConsumer: SjablonConsumer) {

    fun hentSjablonDetaljer(): SjablonDetaljer {
        val sjablonger = sjablonConsumer.hentSjablonger()!!
        return SjablonDetaljer(
            multiplikatorInntekstgrenseForskudd = sjablonger.hentSisteSjablong(SjablongType.MULTIPLIKATOR_MAKS_INNTGRENSE_FORSKUDD_MOTTAKER)?.verdi ?: BigDecimal(0),
            fastsettelseGebyr = sjablonger.hentSisteSjablong(SjablongType.BELØP_FASTSETTELSESGEBYR)?.verdi ?: BigDecimal(0),
            forskuddInntektIntervall = sjablonger.hentSisteSjablong(SjablongType.INNTEKTSINTERVALL_FORSKUDD)?.verdi ?: BigDecimal(0),
            forskuddSats = sjablonger.hentSisteSjablong(SjablongType.FORSKUDDSSATS)?.verdi ?: BigDecimal(0),
            multiplikatorInnteksinslagBarn = sjablonger.hentSisteSjablong(SjablongType.MULTIPLIKATOR_INNTEKTSINNSLAG_BIDRAGSBARN)?.verdi ?: BigDecimal(0)
        )
    }

    fun hentInntektGrenseForPeriode(periodeTomDato: LocalDate?): BigDecimal {
        val sjablonger = sjablonConsumer.hentSjablonger()!!
        val forskuddSats = sjablonger.hentSjablongForTomDato(SjablongType.FORSKUDDSSATS, periodeTomDato)!!
        val multiplikatorInnteksinslagBarn = sjablonger.hentSjablongForTomDato(SjablongType.MULTIPLIKATOR_INNTEKTSINNSLAG_BIDRAGSBARN, periodeTomDato)!!
        return forskuddSats.verdi * multiplikatorInnteksinslagBarn.verdi

    }
    fun hentSjablonGrunnlagForskudd(fraDato: LocalDate, tomDato: LocalDate? = null): List<GrunnlagForskuddPeriode> {
        val sjablonger = sjablonConsumer.hentSjablonger() ?: return emptyList()

        val sjablongTyper = listOf(
            SjablongType.INNTEKTSINTERVALL_FORSKUDD,
            SjablongType.ØVRE_INNTEKTSGRENSE_FOR_FULLT_FORSKUDD,
            SjablongType.ØVRE_INNTEKTSGRENSE_FOR_75PROSENT_FORSKUDD_ENSLIG,
            SjablongType.ØVRE_INNTEKTSGRENSE_FOR_75PROSENT_FORSKUDD_GIFT_SAMB,
            SjablongType.MULTIPLIKATOR_MAKS_INNTGRENSE_FORSKUDD_MOTTAKER,
            SjablongType.FORSKUDDSSATS
        )
        return sjablonger.hentPerioderForSjabloner(sjablongTyper, fraDato, tomDato).flatMap { periodeFraTom ->
            hentSjablonGrunnlagForskuddSjablongForPeriode(periodeFraTom)
        }
    }

    fun hentSjablonGrunnlagForskuddSjablongForPeriode(periodeFraTom: PeriodeFraTom): List<GrunnlagForskuddPeriode> {
        val sjablonger = sjablonConsumer.hentSjablonger()!!
        val periodeFra = periodeFraTom.fraDato
        val periodTomDato = periodeFraTom.tomDato
        val grenseFullForskudd = sjablonger.hentSjablongForTomDato(SjablongType.ØVRE_INNTEKTSGRENSE_FOR_FULLT_FORSKUDD, periodTomDato)!!
        val forskudd75ProsentEnslig = sjablonger.hentSjablongForTomDato(SjablongType.ØVRE_INNTEKTSGRENSE_FOR_75PROSENT_FORSKUDD_ENSLIG, periodTomDato)!!
        val forskudd75ProsentGift = sjablonger.hentSjablongForTomDato(SjablongType.ØVRE_INNTEKTSGRENSE_FOR_75PROSENT_FORSKUDD_GIFT_SAMB, periodTomDato)!!
        val inntekstIntervallForskudd = sjablonger.hentSjablongForTomDato(SjablongType.INNTEKTSINTERVALL_FORSKUDD, periodTomDato)!!
        val multiplikatorForskudd = sjablonger.hentSjablongForTomDato(SjablongType.MULTIPLIKATOR_MAKS_INNTGRENSE_FORSKUDD_MOTTAKER, periodTomDato)!!
        val forskuddSats = sjablonger.hentSjablongForTomDato(SjablongType.FORSKUDDSSATS, periodTomDato)!!

        val forskuddMaksVerdi = forskuddSats.verdi * multiplikatorForskudd.verdi
        val forskuddEnslig1Barn = GrunnlagForskuddPeriode(
            fomDato = periodeFra,
            tomDato = periodTomDato,
            antallBarn = 1,
            forsorgerType = ForsorgerType.ENSLIG,
            beløp50Prosent = BeløpFraTil(grenseFullForskudd.verdi + BigDecimal(1), forskudd75ProsentEnslig.verdi),
            beløp75Prosent = BeløpFraTil(forskudd75ProsentEnslig.verdi + BigDecimal(1), forskuddMaksVerdi)
        )
        val forskuddGift1Barn = GrunnlagForskuddPeriode(
            fomDato = periodeFra,
            tomDato = periodTomDato,
            antallBarn = 1,
            forsorgerType = ForsorgerType.GIFT_SAMBOER,
            beløp50Prosent = BeløpFraTil(grenseFullForskudd.verdi + BigDecimal(1), forskudd75ProsentGift.verdi),
            beløp75Prosent = BeløpFraTil(forskudd75ProsentEnslig.verdi + BigDecimal(1), forskuddMaksVerdi)
        )
        return listOf(
            forskuddEnslig1Barn,
            forskuddEnslig1Barn.copy(
                antallBarn = 2,
                beløp50Prosent = BeløpFraTil(grenseFullForskudd.verdi + BigDecimal(1), forskudd75ProsentEnslig.verdi + inntekstIntervallForskudd.verdi),
                beløp75Prosent = BeløpFraTil(forskudd75ProsentEnslig.verdi + inntekstIntervallForskudd.verdi + BigDecimal(1), forskuddMaksVerdi)
            ),
            forskuddEnslig1Barn.copy(
                antallBarn = 3,
                beløp50Prosent = BeløpFraTil(grenseFullForskudd.verdi + BigDecimal(1), forskuddMaksVerdi),
                beløp75Prosent = BeløpFraTil(forskuddMaksVerdi, forskuddMaksVerdi)
            ),
            forskuddEnslig1Barn.copy(
                antallBarn = 4,
                beløp50Prosent = BeløpFraTil(grenseFullForskudd.verdi + BigDecimal(1), forskuddMaksVerdi),
                beløp75Prosent = BeløpFraTil(forskuddMaksVerdi, forskuddMaksVerdi)
            ),

            forskuddGift1Barn,
            forskuddGift1Barn.copy(
                antallBarn = 2,
                beløp50Prosent = BeløpFraTil(grenseFullForskudd.verdi + BigDecimal(1), forskudd75ProsentGift.verdi + inntekstIntervallForskudd.verdi),
                beløp75Prosent = BeløpFraTil(forskudd75ProsentEnslig.verdi + inntekstIntervallForskudd.verdi + BigDecimal(1), forskuddMaksVerdi)
            ),
            forskuddGift1Barn.copy(
                antallBarn = 3,
                beløp50Prosent = BeløpFraTil(grenseFullForskudd.verdi + BigDecimal(1), forskudd75ProsentGift.verdi + inntekstIntervallForskudd.verdi * BigDecimal(2)),
                beløp75Prosent = BeløpFraTil(forskuddMaksVerdi, forskuddMaksVerdi)
            ),
            forskuddGift1Barn.copy(
                antallBarn = 4,
                beløp50Prosent = BeløpFraTil(grenseFullForskudd.verdi + BigDecimal(1), forskuddMaksVerdi),
                beløp75Prosent = BeløpFraTil(forskuddMaksVerdi, forskuddMaksVerdi)
            )
        )
    }
}
