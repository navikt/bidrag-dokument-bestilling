package no.nav.bidrag.dokument.bestilling.model

import no.nav.bidrag.behandling.felles.enums.EngangsbelopType
import no.nav.bidrag.behandling.felles.enums.StonadType
import no.nav.bidrag.behandling.felles.enums.VedtakType

enum class SoknadType(val kode: String) {
    ENDRING("EN"),
    ENDRING_MOTTAKER("EN"),
    EGET_TILTAK("ET"),
    SOKNAD("FA"),
    INNKREVINGSGRUNNL("IG"),
    INNKREVING("IK"),
    INDEKSREGULERING("IR"),
    KLAGE_BEGR_SATS("KB"),
    KLAGE("KL"),
    FOLGER_KLAGE("KM"),
    KONVERTERING("KV"),
    OMGJORING_BEGR_SATS("OB"),
    OPPJUST_FORSK("OF"),
    OPPHØR("OH"),
    OMGJORING("OM"),
    PRIVAT_AVTALE("PA"),
    BEGR_REVURD("RB"),
    REVURDERING("RF"),
    KORRIGERING("KR"),
    ;

    companion object {
        fun fromKode(kode: String): SoknadType? {
            return SoknadType.values().find { it.kode == kode }
        }

        fun fromVedtakType(vedtakType: VedtakType): SoknadType {
            return when (vedtakType) {
                VedtakType.INDEKSREGULERING -> INDEKSREGULERING
                VedtakType.FASTSETTELSE -> SOKNAD
                VedtakType.ENDRING, VedtakType.ENDRING_MOTTAKER -> ENDRING
                VedtakType.INNKREVING -> INNKREVINGSGRUNNL // Kan være INNKREVINGSGRUNNL, PRIVAT_AVTALE
                VedtakType.KLAGE -> KLAGE // Kan være KLAGE_BEGR_SATS, KLAGE, FOLGER_KLAGE
                VedtakType.REVURDERING -> REVURDERING // Kan være REVURDERING, BEGR_REVURD, EGET_TILTAK
                VedtakType.ALDERSOPPHØR, VedtakType.OPPHØR -> OPPHØR
                VedtakType.ALDERSJUSTERING -> OPPJUST_FORSK // Kan være EGET_TILTAK, OPPJUST_FORSK
                else -> ENDRING
            }
        }
    }
}

enum class SoknadFra(val kode: String) {
    BM_I_ANNEN_SAK("AS"),
    BARN_18("BB"),
    BIDRAGSENHET("ET"), // TK
    FYLKESNEMDA("FN"),
    NAV_INTERNASJONALT("IN"),
    KOMMUNE("KU"),
    KONVERTERING("KV"),
    BIDRAGSMOTTAKER("MO"),
    NORSKE_MYNDIGH("NM"),
    BIDRAGSPLIKTIG("PL"),
    UTENLANDSKE_MYNDIGH("UM"),
    VERGE("VE"),
    TRYGDEETATEN_INNKREVING("TI"),
    KLAGE_ENHET("FK"), // FTK
    ;

    companion object {
        fun fromKode(kode: String): SoknadFra? {
            return SoknadFra.values().find { it.kode == kode }
        }
    }
}

enum class BehandlingType(val kode: String) {
    AVSKRIVNING("AV"),
    EKTEFELLEBIDRAG("EB"),
    BIDRAG_18_AR("18"),
    BIDRAG18AAR("18"),
    BIDRAG("BI"),
    BIDRAG_TILLEGGSBIDRAG("BT"),
    DIREKTE_OPPGJOR("DO"),
    ETTERGIVELSE("EG"),
    ERSTATNING("ER"),
    FARSKAP("FA"),
    FORSKUDD("FO"),
    GEBYR("GB"),
    INNKREVING("IK"),
    MOTREGNING("MR"),
    REFUSJON_BIDRAG("RB"),
    SAKSOMKOSTNINGER("SO"),
    SARTILSKUDD("ST"),
    BIDRAG_18_AR_TILLEGGSBBI("T1"),
    TILLEGGSBIDRAG("TB"),
    TILBAKEKR_ETTERGIVELSE("TE"),
    TILBAKEKREVING("TK"),
    OPPFOSTRINGSBIDRAG("OB"),
    MORSKAP("MO"),
    KUNNSKAP_BIOLOGISK_FAR("FB"),
    BARNEBORTFORING("BF"),
    KONVERTERING("KV"),
    REISEKOSTNADER("RK"),
    ;

    companion object {
        fun fromKode(kode: String): BehandlingType? {
            return BehandlingType.values().find { it.kode == kode }
        }

        fun from(
            stonadType: StonadType,
            engangsbelopType: EngangsbelopType?,
        ): BehandlingType? {
            return when (stonadType) {
                StonadType.FORSKUDD -> FORSKUDD
                StonadType.BIDRAG -> BIDRAG // Inneholder BIDRAG, BIDRAG_TILLEGGSBIDRAG, TILLEGGSBIDRAG
                StonadType.BIDRAG18AAR -> BIDRAG_18_AR // Inneholder BIDRAG_18_AR_TILLEGGSBBI, BIDRAG_18_AR
                StonadType.EKTEFELLEBIDRAG -> EKTEFELLEBIDRAG
                StonadType.MOTREGNING -> MOTREGNING
                StonadType.OPPFOSTRINGSBIDRAG -> OPPFOSTRINGSBIDRAG
                else ->
                    when (engangsbelopType) {
                        EngangsbelopType.SAERTILSKUDD -> SARTILSKUDD
                        EngangsbelopType.GEBYR_SKYLDNER, EngangsbelopType.GEBYR_MOTTAKER -> GEBYR
                        EngangsbelopType.ETTERGIVELSE -> ETTERGIVELSE
                        EngangsbelopType.TILBAKEKREVING -> TILBAKEKREVING
                        EngangsbelopType.ETTERGIVELSE_TILBAKEKREVING -> TILBAKEKR_ETTERGIVELSE
                        else -> null
                    }
            }
        }
    }
}
