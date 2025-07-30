package no.nav.bidrag.dokument.bestilling.tjenester

import com.google.common.collect.ImmutableList
import no.nav.bidrag.commons.util.secureLogger
import no.nav.bidrag.dokument.bestilling.consumer.BidragReskontroConsumer
import no.nav.bidrag.domene.sak.Stønadsid
import no.nav.bidrag.transport.felles.commonObjectmapper
import no.nav.bidrag.transport.reskontro.response.transaksjoner.TransaksjonDto
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDate

@Service
class ReskontroService(
    private val reskontroConsumer: BidragReskontroConsumer,
) {
    private val redusertBidragBMSkylderBp: String = "B4"
    private val redusertBidragB18SkylderBP: String = "D4"
    private val transaksjonskodeAvregning: MutableCollection<String?> = ImmutableList.of<String?>(redusertBidragBMSkylderBp, redusertBidragB18SkylderBP)

    fun hentSumAvregningForStønad(
        stønadsid: Stønadsid,
        vedtakstidspunkt: LocalDate,
    ): BigDecimal {
        try {
            val transaksjoner = reskontroConsumer.transaksjoner(stønadsid.sak.verdi) ?: return BigDecimal.ZERO

            val sumAvregning =
                transaksjoner.transaksjoner
                    .filter { it.skyldner == stønadsid.skyldner && it.barn == stønadsid.kravhaver && it.dato == vedtakstidspunkt }
                    .filter { isAvskrivning(it) }
                    .sumOf { it.beløp ?: BigDecimal.ZERO }

            secureLogger.info { "Fant transaksjoner ${commonObjectmapper.writeValueAsString(transaksjoner)} og sum avregning $sumAvregning for stønad $stønadsid og vedtakstidspunkt $vedtakstidspunkt" }
            return sumAvregning
        } catch (e: Exception) {
            secureLogger.error(e) { "Det skejdde en feil ved uthenting av sum avregning for stønad $stønadsid og vedtakstidspunkt $vedtakstidspunkt" }
            return BigDecimal.ZERO
        }
    }

    private fun isAvskrivning(transaksjon: TransaksjonDto): Boolean = transaksjonskodeAvregning.contains(transaksjon.transaksjonskode)
}
