package no.nav.bidrag.dokument.bestilling.tjenester

import no.nav.bidrag.dokument.bestilling.konsumer.BidragOrganisasjonKonsumer
import no.nav.bidrag.transport.organisasjon.EnhetKontaktinfoDto
import org.springframework.stereotype.Service

@Service
class OrganisasjonTjeneste(private val bidragOrganisasjonKonsumer: BidragOrganisasjonKonsumer) {

    fun hentEnhetKontaktInfo(enhetId: String, spraak: String?): EnhetKontaktinfoDto? {
        return bidragOrganisasjonKonsumer.hentEnhetKontaktinfo(enhetId, spraak ?: "NB")
    }
}
