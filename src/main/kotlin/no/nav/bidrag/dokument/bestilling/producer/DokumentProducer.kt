package no.nav.bidrag.dokument.bestilling.producer

import no.nav.bidrag.dokument.bestilling.model.BrevKode
import no.nav.bidrag.dokument.bestilling.model.DokumentBestilling
import no.nav.bidrag.dokument.bestilling.model.DokumentBestillingResult

interface DokumentProducer {


    fun produce(dokumentBestilling: DokumentBestilling, brevKode: BrevKode): DokumentBestillingResult
}