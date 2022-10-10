package no.nav.bidrag.dokument.bestilling.service

import no.nav.bidrag.dokument.bestilling.consumer.BidragSakConsumer
import no.nav.bidrag.dokument.bestilling.model.HentSakResponse
import org.springframework.stereotype.Service
import java.util.Optional

@Service
class SakService(private var bidragSakConsumer: BidragSakConsumer) {

    fun hentSak(saksnr: String): Optional<HentSakResponse> {
       return bidragSakConsumer.hentSak(saksnr)
    }

}