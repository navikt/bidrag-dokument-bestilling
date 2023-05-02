package no.nav.bidrag.dokument.bestilling.bestilling

import no.nav.bidrag.dokument.bestilling.api.dto.DokumentBestillingForespørsel
import no.nav.bidrag.dokument.bestilling.bestilling.dto.BrevKode
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component
import java.util.EnumMap

typealias DokumentMetadataCollectorFun = (dokumentBestilling: DokumentBestillingForespørsel) -> DokumentMetadataCollector

@Component
class DokumentMap(val applicationContext: ApplicationContext) : MutableMap<BrevKode, DokumentMetadataCollectorFun> by EnumMap(BrevKode::class.java) {
    private final fun add(pair: Pair<BrevKode, DokumentMetadataCollectorFun>) {
        put(pair.first, pair.second)
    }
    init {
        add(
            BrevKode.BI01P11 to {
                medMetadataInnsamler(it)
                    .leggTilMottakerGjelder()
                    .leggTilEnhetKontaktInfo()
            }
        )
        add(
            BrevKode.BI01P18 to {
                medMetadataInnsamler(it)
                    .leggTilMottakerGjelder()
            }
        )
        add(
            BrevKode.BI01X01 to {
                medMetadataInnsamler(it)
                    .leggTilMottakerGjelder()
            }
        )
        add(
            BrevKode.BI01X02 to {
                medMetadataInnsamler(it)
                    .leggTilMottakerGjelder()
            }
        )
        add(
            BrevKode.BI01S10 to {
                medMetadataInnsamler(it)
                    .leggTilMottakerGjelder()
                    .leggTilEnhetKontaktInfo()
                    .leggTilRoller()
            }
        )
        add(
            BrevKode.BI01S67 to {
                medMetadataInnsamler(it)
                    .leggTilMottakerGjelder()
                    .leggTilEnhetKontaktInfo()
                    .leggTilRoller()
            }
        )
        add(
            BrevKode.BI01S02 to {
                medMetadataInnsamler(it)
                    .leggTilMottakerGjelder()
                    .leggTilEnhetKontaktInfo()
                    .leggTilRoller()
            }
        )
    }

    private fun medMetadataInnsamler(dokumentBestilling: DokumentBestillingForespørsel): DokumentMetadataCollector {
        return applicationContext.getBean(DokumentMetadataCollector::class.java).init(dokumentBestilling)
    }
}
