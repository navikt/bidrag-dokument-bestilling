package no.nav.bidrag.dokument.bestilling.tjenester

import no.nav.bidrag.dokument.bestilling.consumer.BidragSakConsumer
import no.nav.bidrag.dokument.bestilling.consumer.dto.HentSakResponse
import org.springframework.stereotype.Service

@Service
class SakService(private val bidragSakConsumer: BidragSakConsumer) {

    fun hentSak(saksnr: String): HentSakResponse? {
        return bidragSakConsumer.hentSak(saksnr)
    }
}
