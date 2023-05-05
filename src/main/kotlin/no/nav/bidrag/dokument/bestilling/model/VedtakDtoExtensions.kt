package no.nav.bidrag.dokument.bestilling.model

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.bidrag.behandling.felles.dto.vedtak.VedtakDto
import no.nav.bidrag.behandling.felles.enums.GrunnlagType
import no.nav.bidrag.behandling.felles.grunnlag.BarnInfo
import no.nav.bidrag.behandling.felles.grunnlag.Sivilstand
import no.nav.bidrag.behandling.felles.grunnlag.SoknadsbarnInfo
import no.nav.bidrag.behandling.felles.grunnlag.VedtakInfo
import no.nav.bidrag.behandling.felles.grunnlag.delberegning.SluttberegningBBM
import no.nav.bidrag.behandling.felles.grunnlag.inntekt.Inntekt

val objectMapper = ObjectMapper().findAndRegisterModules()
var reader = ObjectMapper().readerFor(object : TypeReference<List<String>>() {})
fun VedtakDto.hentInntekter(referanser: List<String>): List<Inntekt> {
    val sluttBeregninger = hentGrunnagDetaljer(GrunnlagType.SLUTTBEREGNING_BBM, SluttberegningBBM::class.java, referanser)

    val referanserGrunnlag: List<String> = sluttBeregninger.flatMap { it.grunnlagReferanseListe }
    return hentGrunnagDetaljer(GrunnlagType.INNTEKT, Inntekt::class.java, referanserGrunnlag)
}

fun VedtakDto.hentSluttberegning(referanser: List<String>): List<SluttberegningBBM> {
    return hentGrunnagDetaljer(GrunnlagType.SLUTTBEREGNING_BBM, SluttberegningBBM::class.java, referanser)
}

fun VedtakDto.hentSoknadBarnInfo(referanser: List<String>): List<SoknadsbarnInfo> {
    val sluttBeregninger = hentGrunnagDetaljer(GrunnlagType.SLUTTBEREGNING_BBM, SluttberegningBBM::class.java, referanser)

    val referanserGrunnlag: List<String> = sluttBeregninger.flatMap { it.grunnlagReferanseListe }
    return hentGrunnagDetaljer(GrunnlagType.SOKNADSBARN_INFO, SoknadsbarnInfo::class.java, referanserGrunnlag)
}

fun <T> VedtakDto.hentGrunnagDetaljer(grunnlagType: GrunnlagType, clazz: Class<T>, referanser: List<String> = emptyList()): List<T> = grunnlagListe
    .filter { it.type == grunnlagType }
    .filter { referanser.isEmpty() || referanser.contains(it.referanse) }
    .map { objectMapper.readValue(it.innhold.toString(), clazz) }
fun VedtakDto.hentBarnInfo(): List<BarnInfo> = hentGrunnagDetaljer(GrunnlagType.BARN_INFO, BarnInfo::class.java)
fun VedtakDto.hentVedtakInfo(): VedtakInfo? = grunnlagListe.find { it.type == GrunnlagType.VEDTAK_INFO }?.let { objectMapper.readValue(it.innhold.toString(), VedtakInfo::class.java) }
fun VedtakDto.hentSivilstand(): List<Sivilstand> = hentGrunnagDetaljer(GrunnlagType.SIVILSTAND, Sivilstand::class.java)
