package no.nav.bidrag.dokument.bestilling.service

import no.nav.bidrag.dokument.bestilling.consumer.BidragOrganisasjonConsumer
import no.nav.bidrag.dokument.bestilling.model.EnhetInfo
import no.nav.bidrag.dokument.bestilling.model.EnhetKontaktInfoDto
import org.springframework.stereotype.Service
import java.util.Optional

@Service
class OrganisasjonService(private var bidragOrganisasjonConsumer: BidragOrganisasjonConsumer) {

    fun hentEnhetInfo(enhetId: String): Optional<EnhetInfo> {
       return bidragOrganisasjonConsumer.hentEnhetInfo(enhetId)
    }

    fun hentEnhetKontaktInfo(enhetId: String, spraak: String?): Optional<EnhetKontaktInfoDto> {
        return bidragOrganisasjonConsumer.hentEnhetKontaktinfo(enhetId, spraak?:"NB")
    }
}