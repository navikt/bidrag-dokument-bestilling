package no.nav.bidrag.dokument.bestilling.producer

import no.nav.bidrag.dokument.bestilling.consumer.BidragDokumentConsumer
import no.nav.bidrag.dokument.bestilling.model.BestillingSystem
import no.nav.bidrag.dokument.bestilling.model.Brev
import no.nav.bidrag.dokument.bestilling.model.BrevBestilling
import no.nav.bidrag.dokument.bestilling.model.BrevKode
import no.nav.bidrag.dokument.bestilling.model.BrevKontaktinfo
import no.nav.bidrag.dokument.bestilling.model.BrevMottaker
import no.nav.bidrag.dokument.bestilling.model.BrevType
import no.nav.bidrag.dokument.bestilling.model.DokumentBestilling
import no.nav.bidrag.dokument.bestilling.model.DokumentBestillingResult
import no.nav.bidrag.dokument.bestilling.model.EnhetKontaktInfo
import no.nav.bidrag.dokument.bestilling.model.LANDKODE3_NORGE
import no.nav.bidrag.dokument.bestilling.model.Mottaker
import no.nav.bidrag.dokument.bestilling.model.RolleType
import no.nav.bidrag.dokument.bestilling.model.brevbestilling
import no.nav.bidrag.dokument.dto.AktorDto
import no.nav.bidrag.dokument.dto.AvsenderMottakerDto
import no.nav.bidrag.dokument.dto.JournalpostType
import no.nav.bidrag.dokument.dto.OpprettDokumentDto
import no.nav.bidrag.dokument.dto.OpprettJournalpostRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.jms.core.JmsTemplate
import org.springframework.stereotype.Component

@Component(BestillingSystem.BREVSERVER)
class BrevserverProducer(
    var onlinebrevTemplate: JmsTemplate,
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
                },
                saksbehandlerIdent = dokumentBestilling.saksbehandler?.ident
            ))

            dokumentBestilling.dokumentReferanse = response?.dokumenter?.get(0)?.dokumentreferanse
            return response?.journalpostId!!
        }
        return ""
    }

    private fun mapToBrevserverMessage(dokumentBestilling: DokumentBestilling, brevKode: BrevKode): BrevBestilling {
        val dokumentSpraak = dokumentBestilling.spraak ?: "NB"
        val saksbehandlerNavn = dokumentBestilling.saksbehandler?.navn
        return brevbestilling {
            val roller = dokumentBestilling.roller
            val bp = roller.bidragspliktig
            val bm = roller.bidragsmottaker

            malpakke = "BI01.${brevKode.name}"
            passord = brevPassord
            saksbehandler = dokumentBestilling.saksbehandler?.ident!!
            brev {
                brevref = dokumentBestilling.dokumentReferanse!!
                spraak = dokumentSpraak
                tknr = dokumentBestilling.enhet!!
                mottaker = dokumentBestilling.mottaker?.let { mapBrevmottaker(this, it) }
                kontaktInfo = mapKontaktInfo(this, dokumentBestilling.kontaktInfo)
                soknad {
                    saksnr = dokumentBestilling.saksnummer
                    rmISak = dokumentBestilling.rmISak
                    sakstype = "E" // "X" hvis det er en ukjent part i saken, "U" hvis parter levde adskilt, "E" i alle andre tilfeller
                }
                parter {
                    bpfnr = bp?.fodselsnummer
                    bpnavn = bp?.navn
                    bpfodselsdato = bp?.fodselsdato
                    bmfnr = bm?.fodselsnummer
                    bmnavn = bm?.navn
                    bmfodselsdato = bm?.fodselsdato
                    bmlandkode = bm?.landkode3
                    bplandkode = bp?.landkode3
                    bpdatodod = bp?.doedsdato
                    bmdatodod = bm?.doedsdato
                }
                brevSaksbehandler {
                    navn = saksbehandlerNavn
                }
                dokumentBestilling.roller.barn.forEach {
                    barnISak {
                        fnr = it.fodselsnummer
                        navn = it.navn
                        fDato = it.fodselsdato
                        fornavn = it.fornavn
                    }
                }
            }
        }
    }

    fun mapKontaktInfo(brev: Brev, _kontaktInfo: EnhetKontaktInfo?): BrevKontaktinfo? {
        val kontaktInfo = _kontaktInfo ?: return null
        return brev.brevKontaktinfo {
            returOgPostadresse {
                enhet = kontaktInfo.enhetId
                navn = kontaktInfo.navn
                adresselinje1 = kontaktInfo.postadresse.adresselinje1
                adresselinje2 = kontaktInfo.postadresse.adresselinje2
                postnummer = kontaktInfo.postadresse.postnummer
                poststed = kontaktInfo.postadresse.poststed
                land = kontaktInfo.postadresse.land
            }
            avsender = avsender {
                navn = kontaktInfo.navn
            }
            tlfAvsender = tlfAvsender {
                telefonnummer = kontaktInfo.telefonnummer
            }
        }
    }
    fun mapBrevmottaker(brev: Brev, mottaker: Mottaker): BrevMottaker {
        return brev.brevmottaker {
            navn = mottaker.navn
            spraak = mottaker.spraak
            fodselsnummer = mottaker.fodselsnummer
            rolle = when(mottaker.rolle){
                RolleType.BM -> "01"
                RolleType.BP -> "02"
                RolleType.RM -> "RM"
                else -> "00"
            }
            fodselsdato = mottaker.fodselsdato

            val adresse = mottaker.adresse
            if (adresse != null){
                adresselinje1 = adresse.adresselinje1
                adresselinje2 = adresse.adresselinje2
                adresselinje3 = adresse.adresselinje3
                adresselinje4 = adresse.adresselinje4
                boligNr = adresse.bruksenhetsnummer
                postnummer = adresse.postnummer ?: ""
//            landkode = adresse.landkode3
            }
        }
    }
}