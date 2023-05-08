package no.nav.bidrag.dokument.bestilling.tjenester

import no.nav.bidrag.behandling.felles.dto.vedtak.VedtakDto
import no.nav.bidrag.behandling.felles.enums.StonadType
import no.nav.bidrag.behandling.felles.grunnlag.BarnInfo
import no.nav.bidrag.dokument.bestilling.bestilling.dto.BarnIHustandPeriode
import no.nav.bidrag.dokument.bestilling.bestilling.dto.BostatusPeriode
import no.nav.bidrag.dokument.bestilling.bestilling.dto.GrunnlagForskuddPeriode
import no.nav.bidrag.dokument.bestilling.bestilling.dto.GrunnlagInntektType
import no.nav.bidrag.dokument.bestilling.bestilling.dto.InntektPeriode
import no.nav.bidrag.dokument.bestilling.bestilling.dto.SivilstandPeriode
import no.nav.bidrag.dokument.bestilling.bestilling.dto.VedtakBarn
import no.nav.bidrag.dokument.bestilling.bestilling.dto.VedtakBarnDetaljer
import no.nav.bidrag.dokument.bestilling.bestilling.dto.VedtakDetaljer
import no.nav.bidrag.dokument.bestilling.bestilling.dto.VedtakPeriode
import no.nav.bidrag.dokument.bestilling.consumer.BidragVedtakConsumer
import no.nav.bidrag.dokument.bestilling.model.SoknadFra
import no.nav.bidrag.dokument.bestilling.model.fantIkkeVedtak
import no.nav.bidrag.dokument.bestilling.model.hentBarnIHustand
import no.nav.bidrag.dokument.bestilling.model.hentBarnInfo
import no.nav.bidrag.dokument.bestilling.model.hentBeregningsgrunnlag
import no.nav.bidrag.dokument.bestilling.model.hentBostatus
import no.nav.bidrag.dokument.bestilling.model.hentInntekter
import no.nav.bidrag.dokument.bestilling.model.hentSivilstand
import no.nav.bidrag.dokument.bestilling.model.hentSluttberegninger
import no.nav.bidrag.dokument.bestilling.model.hentSoknadInfo
import no.nav.bidrag.dokument.bestilling.model.hentSoknadsBarnInfo
import no.nav.bidrag.dokument.bestilling.model.hentVedtakInfo
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class VedtakService(private val bidragVedtakConsumer: BidragVedtakConsumer, private val sjablongService: SjablongService) {

    fun hentVedtak(vedtakId: String): VedtakDto {
        return bidragVedtakConsumer.hentVedtak(vedtakId) ?: fantIkkeVedtak(vedtakId)
    }

    fun hentVedtakDetaljer(vedtakId: String): VedtakDetaljer {
        val vedtakDto = hentVedtak(vedtakId)
        val vedtakInfo = vedtakDto.hentVedtakInfo()
        val soknadInfo = vedtakDto.hentSoknadInfo()
        val vedtakBarnInfo = vedtakDto.hentBarnInfo()
        return VedtakDetaljer(
            virkningÅrsakKode = vedtakInfo?.kodeVirkningAarsak,
            virkningDato = vedtakInfo?.virkningDato,
            soknadDato = soknadInfo?.soknadDato,
            soktFraDato = soknadInfo?.soktFraDato,
            vedtattDato = vedtakInfo?.vedtakDato,
            kilde = vedtakDto.kilde,
            vedtakType = vedtakDto.type,
            søknadType = vedtakDto.stonadsendringListe.firstOrNull()?.type,
            søknadFra = SoknadFra.BIDRAGSMOTTAKER,
            grunnlagForskuddPerioder = hentGrunnlagForskudd(vedtakDto),
            sivilstandPerioder = vedtakDto.hentSivilstand().map {
                SivilstandPeriode(
                    fomDato = it.datoFom,
                    tomDato = it.datoTil,
                    sivilstandKode = it.sivilstandKode,
                    sivilstandBeskrivelse = it.beskrivelse
                )
            },
            vedtakBarn = vedtakBarnInfo.filter { it.medIBeregning == true }.distinctBy { it.fnr }.map { mapVedtakBarn(it, vedtakDto) },
            barnIHustandPerioder = vedtakDto.hentBarnIHustand().map { BarnIHustandPeriode(it.datoFom, it.datoTil, it.antall.toInt()) }
        )
    }

    fun hentGrunnlagForskudd(vedtak: VedtakDto): List<GrunnlagForskuddPeriode> {
        val vedtakInfo = vedtak.hentVedtakInfo()
        val erForskudd = vedtak.stonadsendringListe.any { it.type == StonadType.FORSKUDD }
        if (!erForskudd || vedtakInfo == null) return emptyList()
        return sjablongService.hentSjablonGrunnlagForskudd(vedtakInfo.virkningDato)
    }

    fun mapVedtakBarn(barnInfo: BarnInfo, vedtak: VedtakDto): VedtakBarn {
        val bostatus = vedtak.hentBostatus(barnInfo.fnr)
        return VedtakBarn(
            fodselsnummer = barnInfo.fnr,
            navn = barnInfo.navn,
            harSammeAdresse = barnInfo.harSammeAdresse ?: false,
            bostatusPerioder = bostatus.map { BostatusPeriode(it.datoFom, it.datoTil, it.bostatusKode) },
            vedtakDetaljer = hentVedtakListe(barnInfo.fnr, vedtak)
        )
    }
    fun hentVedtakListe(barnFodselsnummer: String, vedtakDto: VedtakDto): List<VedtakBarnDetaljer> {
        return vedtakDto.stonadsendringListe.filter { it.kravhaverId == barnFodselsnummer }.map { vedtak ->
            VedtakBarnDetaljer(
                type = vedtak.type,
                vedtakPerioder = vedtak.periodeListe.map { periode ->
                    val inntekter = vedtakDto.hentSluttberegninger(periode.grunnlagReferanseListe, periode.resultatkode).flatMap { sluttBeregning ->
                        val soknadBarnInfo = sluttBeregning.hentSoknadsBarnInfo(vedtakDto)
                        sluttBeregning.hentInntekter(vedtakDto).map {
                            InntektPeriode(
                                fomDato = it.datoFom,
                                tomDato = sluttBeregning.datoTil,
                                beløpType = GrunnlagInntektType(it.inntektType),
                                beløpÅr = it.gjelderAar.toInt(),
                                rolle = it.rolle,
                                fodselsnummer = soknadBarnInfo.fnr,
                                beløp = it.belop
                            )
                        }
                    }

                    VedtakPeriode(
                        fomDato = periode.fomDato,
                        tomDato = if (periode.resultatkode == "AHI") inntekter[0].tomDato else periode.tilDato, //TODO: Er dette riktig??
                        beløp = periode.belop ?: BigDecimal(0),
                        resultatKode = periode.resultatkode,
                        inntektPerioder = inntekter + inntekter.hentBeregningsgrunnlag()
                    )
                }
            )
        }
    }
}
