package no.nav.bidrag.dokument.bestilling.tjenester

import no.nav.bidrag.behandling.felles.dto.vedtak.StonadsendringDto
import no.nav.bidrag.behandling.felles.dto.vedtak.VedtakDto
import no.nav.bidrag.behandling.felles.enums.Innkreving
import no.nav.bidrag.behandling.felles.enums.InntektType
import no.nav.bidrag.behandling.felles.enums.StonadType
import no.nav.bidrag.behandling.felles.grunnlag.SoknadsbarnInfo
import no.nav.bidrag.dokument.bestilling.bestilling.dto.BarnIHustandPeriode
import no.nav.bidrag.dokument.bestilling.bestilling.dto.BostatusPeriode
import no.nav.bidrag.dokument.bestilling.bestilling.dto.ForskuddInntektgrensePeriode
import no.nav.bidrag.dokument.bestilling.bestilling.dto.GrunnlagInntektType
import no.nav.bidrag.dokument.bestilling.bestilling.dto.InntektPeriode
import no.nav.bidrag.dokument.bestilling.bestilling.dto.PeriodeFraTom
import no.nav.bidrag.dokument.bestilling.bestilling.dto.SivilstandPeriode
import no.nav.bidrag.dokument.bestilling.bestilling.dto.VedtakBarn
import no.nav.bidrag.dokument.bestilling.bestilling.dto.VedtakBarnStonad
import no.nav.bidrag.dokument.bestilling.bestilling.dto.VedtakDetaljer
import no.nav.bidrag.dokument.bestilling.bestilling.dto.VedtakPeriode
import no.nav.bidrag.dokument.bestilling.consumer.BidragVedtakConsumer
import no.nav.bidrag.dokument.bestilling.model.MAX_DATE
import no.nav.bidrag.dokument.bestilling.model.SoknadFra
import no.nav.bidrag.dokument.bestilling.model.fantIkkeVedtak
import no.nav.bidrag.dokument.bestilling.model.getLastDayOfPreviousMonth
import no.nav.bidrag.dokument.bestilling.model.hentBarnIHustand
import no.nav.bidrag.dokument.bestilling.model.hentBarnInfoForFnr
import no.nav.bidrag.dokument.bestilling.model.hentBeregningsgrunnlag
import no.nav.bidrag.dokument.bestilling.model.hentBostatus
import no.nav.bidrag.dokument.bestilling.model.hentInntekter
import no.nav.bidrag.dokument.bestilling.model.hentKapitalInntekter
import no.nav.bidrag.dokument.bestilling.model.hentNettoKapitalinntekter
import no.nav.bidrag.dokument.bestilling.model.hentPersonInfo
import no.nav.bidrag.dokument.bestilling.model.hentSaksbehandler
import no.nav.bidrag.dokument.bestilling.model.hentSivilstand
import no.nav.bidrag.dokument.bestilling.model.hentSluttberegninger
import no.nav.bidrag.dokument.bestilling.model.hentSoknadInfo
import no.nav.bidrag.dokument.bestilling.model.hentSøknadBarnInfo
import no.nav.bidrag.dokument.bestilling.model.hentVedtakInfo
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class VedtakService(private val bidragVedtakConsumer: BidragVedtakConsumer, private val sjablongService: SjablongService, private val personService: PersonService) {

    fun hentVedtak(vedtakId: String): VedtakDto {
        return bidragVedtakConsumer.hentVedtak(vedtakId) ?: fantIkkeVedtak(vedtakId)
    }
    fun hentVedtakSoknadsbarnFodselsnummer(vedtakId: String): List<String> {
        val vedtakDto = hentVedtak(vedtakId)
        val vedtakBarnInfo = vedtakDto.hentSøknadBarnInfo()
        return vedtakBarnInfo.map { it.fnr }
    }

    fun hentVedtakDetaljer(vedtakId: String): VedtakDetaljer {
        val vedtakDto = hentVedtak(vedtakId)
        val vedtakInfo = vedtakDto.hentVedtakInfo()
        val soknadInfo = vedtakDto.hentSoknadInfo()
        val vedtakBarnInfo = vedtakDto.hentSøknadBarnInfo()
        return VedtakDetaljer(
            virkningÅrsakKode = vedtakInfo?.kodeVirkningAarsak,
            virkningDato = vedtakInfo?.virkningDato,
            soknadDato = soknadInfo?.soknadDato,
            soktFraDato = soknadInfo?.soktFraDato,
            vedtattDato = vedtakInfo?.vedtakDato,
            kilde = vedtakDto.kilde,
            vedtakType = vedtakDto.type,
            saksbehandlerInfo = vedtakDto.hentSaksbehandler(),
            engangsbelopType = vedtakDto.engangsbelopListe.firstOrNull()?.type,
            stønadType = vedtakDto.stonadsendringListe.firstOrNull()?.type,
            søknadFra = SoknadFra.BIDRAGSMOTTAKER,
            sivilstandPerioder = vedtakDto.hentSivilstand().map {
                SivilstandPeriode(
                    fomDato = it.datoFom,
                    tomDato = it.datoTil,
                    sivilstandKode = it.sivilstandKode,
                    sivilstandBeskrivelse = it.beskrivelse
                )
            },
            vedtakBarn = vedtakBarnInfo.distinctBy { it.fnr }.sortedBy { it.fnr }.map { mapVedtakBarn(it, vedtakDto) },
            barnIHustandPerioder = vedtakDto.hentBarnIHustand().map { BarnIHustandPeriode(it.datoFom, it.datoTil, it.antall.toInt()) }
        )
    }

    fun hentGrunnlagForskudd(stonadsendringListe: List<StonadsendringDto>): List<ForskuddInntektgrensePeriode> {
        val erForskudd = stonadsendringListe.any { it.type == StonadType.FORSKUDD }
        if (!erForskudd) return emptyList()
        val perioder = stonadsendringListe.flatMap { it.periodeListe.map { periode -> PeriodeFraTom(periode.fomDato, periode.tilDato ?: MAX_DATE) } }
        val fraDato = perioder.minByOrNull { it.fraDato }!!.fraDato
        val tomDato = perioder.sortedByDescending { it.tomDato }.first().tomDato
        return sjablongService.hentForskuddInntektgrensePerioder(fraDato, tomDato)
    }

    fun mapVedtakBarn(soknadBarn: SoknadsbarnInfo, vedtak: VedtakDto): VedtakBarn {
        val bostatus = vedtak.hentBostatus(soknadBarn.fnr)
        val barnInfo = vedtak.hentBarnInfoForFnr(soknadBarn.fnr)
        val personInfo = personService.hentPerson(soknadBarn.fnr)
        return VedtakBarn(
            fodselsnummer = soknadBarn.fnr,
            navn = personInfo.kortnavn?.verdi,
            harSammeAdresse = barnInfo?.harSammeAdresse ?: true,
            bostatusPerioder = bostatus.map { BostatusPeriode(it.datoFom, it.datoTil, it.bostatusKode) },
            stonader = hentStonader(soknadBarn.fnr, vedtak)
        )
    }
    fun hentStonader(barnFodselsnummer: String, vedtakDto: VedtakDto): List<VedtakBarnStonad> {
        val stonadListeBarn = vedtakDto.stonadsendringListe.filter { it.kravhaverId == barnFodselsnummer }
        return stonadListeBarn.map { vedtak ->
            val vedtakPerioder = vedtak.periodeListe.map { periode ->
                val nettoKapitalInntekt = vedtakDto.hentSluttberegninger(periode.grunnlagReferanseListe, periode.resultatkode).flatMap { sluttBeregning ->
                    sluttBeregning.hentKapitalInntekter(vedtakDto)
                        .filter { it.valgt }
                        .hentNettoKapitalinntekter(periode, vedtakDto)
                }
                val inntekter = vedtakDto.hentSluttberegninger(periode.grunnlagReferanseListe, periode.resultatkode).flatMap { sluttBeregning ->
                    sluttBeregning.hentInntekter(vedtakDto).filter { it.valgt }
                        .filter { it.inntektType != InntektType.KAPITALINNTEKT_EGNE_OPPLYSNINGER }
                        .map {
                            val rollePersonInfo = vedtakDto.hentPersonInfo(it.rolle)
                            InntektPeriode(
                                fomDato = it.datoFom,
                                tomDato = it.datoTil,
                                periodeFomDato = periode.fomDato,
                                periodeTomDato = periode.tilDato,
                                beløpType = GrunnlagInntektType(it.inntektType),
                                beløpÅr = it.gjelderAar.toInt(),
                                rolle = it.rolle,
                                fodselsnummer = rollePersonInfo?.fnr,
                                beløp = it.belop
                            )
                        } + nettoKapitalInntekt
                }

                VedtakPeriode(
                    fomDato = periode.fomDato,
                    tomDato = if (periode.resultatkode == "AHI") inntekter[0].tomDato else periode.tilDato, // TODO: Er dette riktig??
                    beløp = periode.belop ?: BigDecimal(0),
                    resultatKode = periode.resultatkode,
                    inntekter = inntekter + inntekter.hentBeregningsgrunnlag(),
                    inntektGrense = sjablongService.hentInntektGrenseForPeriode(getLastDayOfPreviousMonth(periode.tilDato)),
                    maksInntekt = sjablongService.hentMaksInntektForPeriode(getLastDayOfPreviousMonth(periode.tilDato))
                )
            }
            VedtakBarnStonad(
                type = vedtak.type,
                innkreving = vedtak.innkreving == Innkreving.JA,
                forskuddInntektgrensePerioder = hentGrunnlagForskudd(stonadListeBarn),
                vedtakPerioder = vedtakPerioder
            )
        }
    }
}
