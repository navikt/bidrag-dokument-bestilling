package no.nav.bidrag.dokument.bestilling.service

import no.nav.bidrag.dokument.bestilling.consumer.BidragSakConsumer
import no.nav.bidrag.dokument.bestilling.model.HentSakResponse
import org.springframework.stereotype.Service

@Service
class SakService(private var bidragSakConsumer: BidragSakConsumer) {

    fun hentSak(saksnr: String): HentSakResponse? {
       return bidragSakConsumer.hentSak(saksnr)
    }

}