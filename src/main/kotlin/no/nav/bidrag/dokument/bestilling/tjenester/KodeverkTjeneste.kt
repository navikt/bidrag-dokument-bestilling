package no.nav.bidrag.dokument.bestilling.tjenester

import no.nav.bidrag.dokument.bestilling.konsumer.KodeverkConsumer
import org.springframework.stereotype.Service

@Service
class KodeverkTjeneste(val kodeverkConsumer: KodeverkConsumer) {

    fun hentLandFullnavnForKode(landkode: String): String? {
        val landkoder = kodeverkConsumer.hentLandkoder()
        return landkoder?.hentFraKode(landkode)?.hentNorskNavn()
    }
}