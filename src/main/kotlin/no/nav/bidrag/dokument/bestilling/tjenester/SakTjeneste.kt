package no.nav.bidrag.dokument.bestilling.tjenester

import no.nav.bidrag.dokument.bestilling.konsumer.BidragSakKonsumer
import no.nav.bidrag.dokument.bestilling.konsumer.dto.HentSakResponse
import org.springframework.stereotype.Service

@Service
class SakTjeneste(private val bidragSakKonsumer: BidragSakKonsumer) {

    fun hentSak(saksnr: String): HentSakResponse? {
        return bidragSakKonsumer.hentSak(saksnr)
    }
}
