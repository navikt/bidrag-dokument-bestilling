package no.nav.bidrag.dokument.bestilling.tjenester

import no.nav.bidrag.dokument.bestilling.konsumer.KodeverkKonsumer
import org.springframework.stereotype.Service

@Service
class KodeverkTjeneste(val kodeverkKonsumer: KodeverkKonsumer) {

    fun hentLandFullnavnForKode(landkode: String): String? {
        val landkoder = kodeverkKonsumer.hentLandkoder()
        return landkoder?.hentFraKode(landkode)?.hentNorskNavn()
    }
}
