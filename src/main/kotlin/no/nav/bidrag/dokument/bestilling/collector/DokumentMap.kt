package no.nav.bidrag.dokument.bestilling.collector

import no.nav.bidrag.dokument.bestilling.model.BrevKode
import no.nav.bidrag.dokument.bestilling.model.DokumentBestillingRequest
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component
import java.util.EnumMap

typealias DokumentMetadataCollectorFun = (dokumentBestilling: DokumentBestillingRequest) -> DokumentMetadataCollector

@Component
class DokumentMap(val applicationContext: ApplicationContext): MutableMap<BrevKode, DokumentMetadataCollectorFun> by EnumMap(BrevKode::class.java) {
    private final fun add(pair: Pair<BrevKode, DokumentMetadataCollectorFun>){
        put(pair.first, pair.second)
    }
    init {
        add(BrevKode.BI01P11 to {
           withMetadataCollector(it)
               .addMottakerGjelder()
               .addEnhetKontaktInfo()
        })
        add(BrevKode.BI01P18 to {
            withMetadataCollector(it)
                .addMottakerGjelder()
                .addEnhetKontaktInfo()
        })
        add(BrevKode.BI01X01 to {
            withMetadataCollector(it)
                .addMottakerGjelder()
                .addEnhetKontaktInfo()
        })
        add(BrevKode.BI01X02 to {
            withMetadataCollector(it)
                .addMottakerGjelder()
                .addEnhetKontaktInfo()
        })
        add(BrevKode.BI01S10 to {
            withMetadataCollector(it)
                .addMottakerGjelder()
                .addEnhetKontaktInfo()
                .addRoller()
        })
        add(BrevKode.BI01S67 to {
            withMetadataCollector(it)
                .addMottakerGjelder()
                .addEnhetKontaktInfo()
                .addRoller()
        })
        add(BrevKode.BI01S02 to {
            withMetadataCollector(it)
                .addMottakerGjelder()
                .addEnhetKontaktInfo()
                .addRoller()
        })
    }

    private fun withMetadataCollector(dokumentBestilling: DokumentBestillingRequest): DokumentMetadataCollector {
        return applicationContext.getBean(DokumentMetadataCollector::class.java).init(dokumentBestilling)
    }
}
