package no.nav.bidrag.dokument.bestilling.collector

import no.nav.bidrag.dokument.bestilling.model.BrevKode
import no.nav.bidrag.dokument.bestilling.model.DokumentBestillingRequest
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component
import java.util.EnumMap

typealias DokumentMetadataCollectorFun = (dokumentBestilling: DokumentBestillingRequest, enhet: String) -> DokumentMetadataCollector

@Component
class DokumentMap(val applicationContext: ApplicationContext): MutableMap<BrevKode, DokumentMetadataCollectorFun> by EnumMap(BrevKode::class.java) {

    private final fun add(pair: Pair<BrevKode, DokumentMetadataCollectorFun>){
        put(pair.first, pair.second)
    }
    init {
        add(BrevKode.BI01B01 to { bestilling, enhet ->
           withMetadataCollector(bestilling, enhet)
               .addMottaker()
               .addGjelder()
               .addRoller()
               .addKontaktInfo()
        })
        add(BrevKode.BI01P18 to { bestilling, enhet ->
            withMetadataCollector(bestilling, enhet)
                .addMottaker()
                .addGjelder()
                .addRoller()
                .addKontaktInfo()
        })
        add(BrevKode.BI01S02 to { bestilling, enhet ->
            withMetadataCollector(bestilling, enhet)
                .addMottaker()
                .addGjelder()
                .addRoller()
                .addKontaktInfo()
        })
    }

    private fun withMetadataCollector(dokumentBestilling: DokumentBestillingRequest, enhet: String): DokumentMetadataCollector {
        return applicationContext.getBean(DokumentMetadataCollector::class.java).init(dokumentBestilling, enhet)
    }
}
