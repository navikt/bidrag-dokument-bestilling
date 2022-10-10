package no.nav.bidrag.dokument.bestilling.service

import no.nav.bidrag.dokument.bestilling.consumer.BidragOrganisasjonConsumer
import no.nav.bidrag.dokument.bestilling.consumer.BidragPersonConsumer
import no.nav.bidrag.dokument.bestilling.model.EnhetInfo
import no.nav.bidrag.dokument.bestilling.model.HentPersonResponse
import no.nav.bidrag.dokument.bestilling.model.HentPostadresseResponse
import org.springframework.stereotype.Service
import java.util.Optional

@Service
class OrganisasjonService(private var bidragOrganisasjonConsumer: BidragOrganisasjonConsumer) {

    fun hentEnhetInfo(enhetId: String): Optional<EnhetInfo> {
       return bidragOrganisasjonConsumer.hentEnhetInfo(enhetId)
    }
}