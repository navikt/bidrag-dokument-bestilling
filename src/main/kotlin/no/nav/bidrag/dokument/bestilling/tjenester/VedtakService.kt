package no.nav.bidrag.dokument.bestilling.tjenester

import no.nav.bidrag.behandling.felles.dto.vedtak.VedtakDto
import no.nav.bidrag.behandling.felles.enums.StonadType
import no.nav.bidrag.behandling.felles.grunnlag.BarnInfo
import no.nav.bidrag.dokument.bestilling.bestilling.dto.GrunnlagForskuddPeriode
import no.nav.bidrag.dokument.bestilling.bestilling.dto.InntektPeriode
import no.nav.bidrag.dokument.bestilling.bestilling.dto.SivilstandPeriode
import no.nav.bidrag.dokument.bestilling.bestilling.dto.VedtakBarn
import no.nav.bidrag.dokument.bestilling.bestilling.dto.VedtakBarnDetaljer
import no.nav.bidrag.dokument.bestilling.bestilling.dto.VedtakDetaljer
import no.nav.bidrag.dokument.bestilling.bestilling.dto.VedtakPeriode
import no.nav.bidrag.dokument.bestilling.consumer.BidragVedtakConsumer
import no.nav.bidrag.dokument.bestilling.model.SoknadFra
import no.nav.bidrag.dokument.bestilling.model.fantIkkeVedtak
import no.nav.bidrag.dokument.bestilling.model.hentBarnInfo
import no.nav.bidrag.dokument.bestilling.model.hentInntekter
import no.nav.bidrag.dokument.bestilling.model.hentSivilstand
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
        val vedtakBarnInfo = vedtakDto.hentBarnInfo()
        return VedtakDetaljer(
            virkningÅrsakKode = vedtakInfo?.kodeVirkningAarsak,
            virkningDato = vedtakInfo?.virkningDato,
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
                    sivilstandKode = it.sivilstandKode
                )
            },
            vedtakBarn = vedtakBarnInfo.filter { it.medIBeregning == true }.map { mapVedtakBarn(it, vedtakDto) }
        )
    }

    fun hentGrunnlagForskudd(vedtak: VedtakDto): List<GrunnlagForskuddPeriode> {
        val periode = vedtak.vedtakTidspunkt
        val erForskudd = vedtak.stonadsendringListe.any { it.type == StonadType.FORSKUDD }
        if (!erForskudd) return emptyList()
        return sjablongService.hentSjablonGrunnlagForskudd(periode.toLocalDate())
    }

    fun mapVedtakBarn(barnInfo: BarnInfo, vedtak: VedtakDto): VedtakBarn {
        return VedtakBarn(
            fodselsnummer = barnInfo.fnr,
            navn = barnInfo.navn,
            harSammeAdresse = barnInfo.harSammeAdresse ?: false,
            vedtakDetaljer = hentVedtakListe(barnInfo.fnr, vedtak)
        )
    }
    fun hentVedtakListe(barnFodselsnummer: String, vedtakDto: VedtakDto): List<VedtakBarnDetaljer> {
        return vedtakDto.stonadsendringListe.filter { it.kravhaverId == barnFodselsnummer }.map { vedtak ->
            {}
            VedtakBarnDetaljer(
                type = vedtak.type,
                vedtakPerioder = vedtak.periodeListe.map { periode ->
                    VedtakPeriode(
                        fomDato = periode.fomDato,
                        tomDato = periode.tilDato,
                        beløp = periode.belop ?: BigDecimal(0),
                        resultatKode = periode.resultatkode,
                        inntektPerioder = vedtakDto.hentInntekter(periode.grunnlagReferanseListe).map {
                            InntektPeriode(
                                fomDato = it.datoFom,
                                tomDato = it.datoTil,
                                beløpType = it.inntektType,
                                beløpÅr = it.gjelderAar.toInt(),
                                rolle = it.rolle,
                                inntektsgrense = 0, // INNTEKTSINTERVALL_FORSKUDD,
                                beløp = it.belop
                            )
                        }
                    )
                }
            )
        }
    }
}
