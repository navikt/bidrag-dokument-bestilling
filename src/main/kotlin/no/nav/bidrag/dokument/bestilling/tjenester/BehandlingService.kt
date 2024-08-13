package no.nav.bidrag.dokument.bestilling.tjenester

import no.nav.bidrag.dokument.bestilling.bestilling.dto.VedtakDetaljer
import no.nav.bidrag.dokument.bestilling.bestilling.dto.VedtakSaksbehandlerInfo
import no.nav.bidrag.dokument.bestilling.consumer.BidragBehandlingConsumer
import no.nav.bidrag.dokument.bestilling.consumer.dto.BehandlingDetaljerDtoV2
import no.nav.bidrag.dokument.bestilling.model.fantIkkeVedtak
import no.nav.bidrag.domene.enums.rolle.Rolletype
import org.springframework.stereotype.Service

@Service
class BehandlingService(
    private val bidragBehandlingConsumer: BidragBehandlingConsumer,
) {
    fun hentBehandling(behandlingId: String): BehandlingDetaljerDtoV2 = bidragBehandlingConsumer.hentBehandling(behandlingId) ?: fantIkkeVedtak(behandlingId)

    fun hentIdentSøknadsbarn(behandlingId: String): List<String> {
        val behandlingDto = hentBehandling(behandlingId)
        return behandlingDto.roller.filter { it.rolletype == Rolletype.BARN }.map { it.ident!! }
    }

    fun hentVedtakDetaljer(behandlingId: String): VedtakDetaljer {
        val behandlingDto = hentBehandling(behandlingId)
        return VedtakDetaljer(
            årsakKode = behandlingDto.årsak,
            avslagsKode = behandlingDto.avslag,
            type = behandlingDto.type,
            virkningstidspunkt = behandlingDto.virkningstidspunkt,
            mottattDato = behandlingDto.mottattdato,
            søknadFra = behandlingDto.søktAv,
            soktFraDato = behandlingDto.søktFomDato,
            saksbehandlerInfo =
                VedtakSaksbehandlerInfo(
                    navn = behandlingDto.opprettetAv.navn ?: "",
                    ident = behandlingDto.opprettetAv.ident,
                ),
            engangsbelopType = behandlingDto.engangsbeløptype,
            stønadType = behandlingDto.stønadstype,
            vedtakType = behandlingDto.vedtakstype,
        )
    }
}
