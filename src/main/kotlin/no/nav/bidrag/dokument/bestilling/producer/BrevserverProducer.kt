package no.nav.bidrag.dokument.bestilling.producer

import no.nav.bidrag.dokument.bestilling.config.SaksbehandlerInfoManager
import no.nav.bidrag.dokument.bestilling.consumer.BidragDokumentConsumer
import no.nav.bidrag.dokument.bestilling.model.BREV_DATETIME_FORMAT
import no.nav.bidrag.dokument.bestilling.model.BestillingSystem
import no.nav.bidrag.dokument.bestilling.model.BrevBestilling
import no.nav.bidrag.dokument.bestilling.model.BrevKode
import no.nav.bidrag.dokument.bestilling.model.BrevKontaktinfo
import no.nav.bidrag.dokument.bestilling.model.BrevMottaker
import no.nav.bidrag.dokument.bestilling.model.BrevSaksbehandler
import no.nav.bidrag.dokument.bestilling.model.BrevType
import no.nav.bidrag.dokument.bestilling.model.DokumentBestilling
import no.nav.bidrag.dokument.bestilling.model.DokumentBestillingResult
import no.nav.bidrag.dokument.bestilling.model.EnhetKontaktInfo
import no.nav.bidrag.dokument.bestilling.model.Mottaker
import no.nav.bidrag.dokument.bestilling.model.Parter
import no.nav.bidrag.dokument.bestilling.model.RolleType
import no.nav.bidrag.dokument.bestilling.model.Saksbehandler
import no.nav.bidrag.dokument.bestilling.model.SoknadsPart
import no.nav.bidrag.dokument.bestilling.model.brev
import no.nav.bidrag.dokument.bestilling.model.brevKontaktinfo
import no.nav.bidrag.dokument.bestilling.model.brevSaksbehandler
import no.nav.bidrag.dokument.bestilling.model.brevbestilling
import no.nav.bidrag.dokument.bestilling.model.brevmottaker
import no.nav.bidrag.dokument.bestilling.model.parter
import no.nav.bidrag.dokument.bestilling.model.soknad
import no.nav.bidrag.dokument.dto.AktorDto
import no.nav.bidrag.dokument.dto.AvsenderMottakerDto
import no.nav.bidrag.dokument.dto.JournalpostType
import no.nav.bidrag.dokument.dto.OpprettDokumentDto
import no.nav.bidrag.dokument.dto.OpprettJournalpostRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.jms.core.JmsTemplate
import org.springframework.stereotype.Component
import java.time.format.DateTimeFormatter

@Component(BestillingSystem.BREVSERVER)
class BrevserverProducer(
    var onlinebrevTemplate: JmsTemplate,
    var saksbehandlerInfoManager: SaksbehandlerInfoManager,
    var bidragDokumentConsumer: BidragDokumentConsumer,
    @Value("\${BREVSERVER_PASSORD}") val brevPassord: String
): DokumentProducer {


    override fun produce(dokumentBestilling: DokumentBestilling, brevKode: BrevKode): DokumentBestillingResult {
        val journalpostId = opprettJournalpost(dokumentBestilling, brevKode)

        onlinebrevTemplate.convertAndSend(mapToBrevserverMessage(dokumentBestilling, brevKode))
         // TODO: Error handling
        return DokumentBestillingResult(
            dokumentReferanse = dokumentBestilling.dokumentReferanse!!,
            journalpostId = journalpostId
        )
    }

    fun opprettJournalpost(dokumentBestilling: DokumentBestilling, brevKode: BrevKode): String {
        if (dokumentBestilling.dokumentReferanse.isNullOrEmpty()){
            val tittel = dokumentBestilling.tittel ?: brevKode.beskrivelse
            val response = bidragDokumentConsumer.opprettJournalpost(OpprettJournalpostRequest(
                tittel = tittel,
                journalfoerendeEnhet = dokumentBestilling.enhet,
                tilknyttSaker = listOf(dokumentBestilling.saksnummer!!),
                dokumenter = listOf(OpprettDokumentDto(
                    tittel = tittel,
                    brevkode = brevKode.name
                )),
                gjelder = AktorDto(dokumentBestilling.gjelder?.fodselsnummer!!),
                avsenderMottaker = AvsenderMottakerDto(dokumentBestilling.mottaker?.navn, dokumentBestilling.mottaker?.fodselsnummer!!),
                journalposttype = when(brevKode.brevtype){
                    BrevType.UTGAAENDE -> JournalpostType.UTGAAENDE
                    BrevType.NOTAT -> JournalpostType.NOTAT
                }
            ))

            dokumentBestilling.dokumentReferanse = response?.dokumenter?.get(0)?.dokumentreferanse
            return response?.journalpostId!!
        }
        return ""
    }

    private fun mapToBrevserverMessage(dokumentBestilling: DokumentBestilling, brevKode: BrevKode): BrevBestilling {
        val dokumentSpraak = dokumentBestilling.spraak ?: "NB"
        val saksbehandlerNavn = saksbehandlerInfoManager.hentSaksbehandler().orElse(Saksbehandler("", "")).navn
        return brevbestilling {
            malpakke = "BI01.${brevKode.name}"
            passord = brevPassord
            saksbehandler = saksbehandlerInfoManager.hentSaksbehandlerBrukerId()
            brev = brev {
                brevref = dokumentBestilling.dokumentReferanse!!
                spraak = dokumentSpraak
                tknr = dokumentBestilling.enhet!!
                mottaker = mapBrevmottaker(dokumentBestilling.mottaker!!, dokumentSpraak)
                kontaktInfo = mapKontaktInfo(dokumentBestilling.kontaktInfo)
                soknad = soknad {
                    saksnr = dokumentBestilling.saksnummer
                    sakstype = "E"
                }
                parter = mapPart(dokumentBestilling.parter)
                saksbehandler = brevSaksbehandler {
                    navn = saksbehandlerNavn
                }
            }
        }
    }

    fun mapPart(_part: List<SoknadsPart>): Parter? {
        val part = if (_part.isNotEmpty()) _part[0] else return null
        val bp = part.bidragsPliktigInfo
        val bm = part.bidragsMottakerInfo

        return parter {
            bpfnr = bp?.fnr
            bpnavn = bp?.navn
            bpnavn = bp?.fodselsdato?.format(BREV_DATETIME_FORMAT)
            bmfnr = bm?.fnr
            bmnavn = bm?.navn
            bmfodselsdato = bm?.fodselsdato?.format(BREV_DATETIME_FORMAT) ?: ""
        }
    }
    fun mapKontaktInfo(_kontaktInfo: EnhetKontaktInfo?): BrevKontaktinfo? {
        val kontaktInfo = _kontaktInfo ?: return null
        return brevKontaktinfo {
            val mappedAdresse = adresse {
                enhet = kontaktInfo.enhetId
                navn = kontaktInfo.navn.substring(0, kontaktInfo.navn.length.coerceAtMost(30))
                telefon = if(kontaktInfo.navn.length > 30) kontaktInfo.navn.substring(30, kontaktInfo.navn.length) else null
                adresselinje2 = kontaktInfo.returAdresse.adresselinje1
                postnummer = kontaktInfo.returAdresse.postnummer
                poststed = kontaktInfo.returAdresse.poststed
                land = kontaktInfo.returAdresse.landkode
            }
            avsender = avsender {
                navn = kontaktInfo.navn
            }
            tlfAvsender = tlfAvsender {
                telefonnummer = kontaktInfo.telefonnummer
            }
            returAdresse = mappedAdresse
            postadresse = mappedAdresse
        }
    }
    fun mapBrevmottaker(mottaker: Mottaker, brevSpraak: String): BrevMottaker {
        return brevmottaker {
            navn = mottaker.navn
            spraak = brevSpraak
            fodselsnummer = mottaker.fodselsnummer
            rolle = when(mottaker.rolle){
                RolleType.BP -> "01"
                RolleType.BM -> "02"
                RolleType.BA -> "03"
                RolleType.RM -> "04"
                RolleType.FR -> "05"
                else -> null
            }
            fodselsdato = mottaker.fodselsdato?.format(BREV_DATETIME_FORMAT)

            val adresse = mottaker.adresse
            val postnummerSted = "${adresse.postnummer} ${adresse.poststed}"
            adresselinje1 = adresse.adresselinje1
            adresselinje2 = adresse.adresselinje2
            adresselinje3 = adresse.adresselinje3 ?: postnummerSted
            adresselinje4 = if (adresselinje3 == postnummerSted) null else postnummerSted
            boligNr = adresse.boligNr
            postnummer = adresse.postnummer
            landkode = adresse.landkode
        }
    }
}