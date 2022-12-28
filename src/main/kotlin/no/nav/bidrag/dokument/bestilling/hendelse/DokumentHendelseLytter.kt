package no.nav.bidrag.dokument.bestilling.hendelse

import com.fasterxml.jackson.databind.ObjectMapper
import mu.KotlinLogging
import no.nav.bidrag.dokument.bestilling.api.dto.DokumentBestillingForespørsel
import no.nav.bidrag.dokument.bestilling.api.dto.MottakerAdresseTo
import no.nav.bidrag.dokument.bestilling.api.dto.MottakerTo
import no.nav.bidrag.dokument.bestilling.bestilling.dto.BrevKode
import no.nav.bidrag.dokument.bestilling.konsumer.BidragDokumentKonsumer
import no.nav.bidrag.dokument.bestilling.model.ForsendelseFraHendelseManglerDokument
import no.nav.bidrag.dokument.bestilling.model.ForsendelseFraHendelseManglerNødvendigDetaljer
import no.nav.bidrag.dokument.bestilling.model.Ident
import no.nav.bidrag.dokument.bestilling.model.UgyldigBestillingAvDokument
import no.nav.bidrag.dokument.bestilling.tjenester.DokumentBestillingTjeneste
import no.nav.bidrag.dokument.dto.DokumentHendelse
import no.nav.bidrag.dokument.dto.DokumentHendelseType
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
private val LOGGER = KotlinLogging.logger {}


val DokumentHendelse.erForsendelse get() = forsendelseId?.startsWith("BIF") == true
val DokumentHendelse.erBestilling get() = hendelseType != DokumentHendelseType.BESTILLING
@Component
class DokumentHendelseLytter(val objectMapper: ObjectMapper, val dokumentKonsumer: BidragDokumentKonsumer, val dokumentBestillingService: DokumentBestillingTjeneste) {

    @KafkaListener(groupId = "bidrag-dokument-bestilling", topics = ["\${TOPIC_DOKUMENT}"])
    fun prossesserDokumentHendelse(melding: ConsumerRecord<String, String>){
        val hendelse = tilDokumentHendelseObjekt(melding)
        val erGyldigBestilling = hendelse.erBestilling && hendelse.erForsendelse
        if (!erGyldigBestilling) return

        val forsendelseRespons = dokumentKonsumer.hentJournalpost(hendelse.forsendelseId!!)!!
        val forsendelse = forsendelseRespons.journalpost!!
        val dokument = forsendelse.dokumenter.find{it.dokumentreferanse == hendelse.dokumentreferanse} ?: throw ForsendelseFraHendelseManglerDokument("Fant ikke ${hendelse.dokumentreferanse} i forsendelse ${hendelse.forsendelseId} fra hendelse")
        val brevkode = hentBrevkode(dokument.dokumentmalId) ?: throw UgyldigBestillingAvDokument("Bestilling av forsendelse ${hendelse.forsendelseId} med dokumentreferanse ${hendelse.dokumentreferanse} er ugyldig. Dokumentmal ${dokument.dokumentmalId} er ikke støttet")
        val mottaker = forsendelse.avsenderMottaker ?: throw ForsendelseFraHendelseManglerNødvendigDetaljer("Forsendelse ${hendelse.forsendelseId} mangler mottaker informasjon")
        dokumentBestillingService.bestill(
            DokumentBestillingForespørsel(
                mottaker = MottakerTo(
                    ident = mottaker.ident as Ident,
                    navn = mottaker.navn,
                    adresse = mottaker.adresse?.let {
                        MottakerAdresseTo(
                            adresselinje1 = it.adresselinje1,
                            adresselinje2 = it.adresselinje2,
                            adresselinje3 = it.adresselinje3,
                            bruksenhetsnummer = it.bruksenhetsnummer,
                            postnummer = it.postnummer,
                            poststed = it.poststed,
                            landkode = it.landkode,
                            landkode3 = it.landkode3
                        )
                    }
                ),
                gjelderId = forsendelse.gjelderIdent,
                saksnummer = forsendelse.sakstilknytninger.first(),
                dokumentreferanse = hendelse.dokumentreferanse,
                tittel = dokument.tittel,
                enhet = forsendelse.journalforendeEnhet,
                språk = forsendelse.språk
            ),
            brevkode

        )

    }

    private fun hentBrevkode(dokumentMal: String?): BrevKode? {
        return BrevKode.values().find{ it.name == dokumentMal }
    }


    private fun tilDokumentHendelseObjekt(melding: ConsumerRecord<String, String>): DokumentHendelse {
        try {
            return objectMapper.readValue(melding.value(), DokumentHendelse::class.java)
        } catch (e: Exception){
            LOGGER.error("Det skjedde en feil ved konverting av melding fra hendelse", e)
            throw e
        }
    }
}