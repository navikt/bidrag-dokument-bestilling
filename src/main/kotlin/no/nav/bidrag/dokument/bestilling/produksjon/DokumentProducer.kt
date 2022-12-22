package no.nav.bidrag.dokument.bestilling.produksjon

import no.nav.bidrag.dokument.bestilling.model.BrevKode
import no.nav.bidrag.dokument.bestilling.model.DokumentBestilling
import no.nav.bidrag.dokument.bestilling.model.DokumentBestillingResult

interface DokumentProducer {


    fun produce(dokumentBestilling: DokumentBestilling, brevKode: BrevKode): DokumentBestillingResult
}