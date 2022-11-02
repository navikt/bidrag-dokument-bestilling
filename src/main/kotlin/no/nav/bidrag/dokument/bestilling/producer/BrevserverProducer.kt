package no.nav.bidrag.dokument.bestilling.producer

import no.nav.bidrag.dokument.bestilling.config.SaksbehandlerInfoManager
import no.nav.bidrag.dokument.bestilling.consumer.BidragDokumentConsumer
import no.nav.bidrag.dokument.bestilling.model.BRUKSHENETSNUMMER_STANDARD
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
        val saksbehandlerNavn = saksbehandlerInfoManager.hentSaksbehandler().orElse(null)?.navn ?: saksbehandlerInfoManager.hentSaksbehandlerBrukerId()
        return brevbestilling {
            val roller = dokumentBestilling.roller
            val bp = roller.bidragspliktig
            val bm = roller.bidragsmottaker

            malpakke = "BI01.${brevKode.name}"
            passord = brevPassord
            saksbehandler = saksbehandlerInfoManager.hentSaksbehandlerBrukerId()
            brev {
                brevref = dokumentBestilling.dokumentReferanse!!
                spraak = dokumentSpraak
                tknr = dokumentBestilling.enhet!!
                mottaker = mapBrevmottaker(this, dokumentBestilling.mottaker!!, dokumentSpraak)
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
                    bmlandkode = if (bm?.landkode.isNullOrEmpty() || bm?.landkode == "NO") null else bm?.landkode
                    bplandkode = if (bp?.landkode.isNullOrEmpty() || bp?.landkode == "NO") null else bp?.landkode
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
                adresselinje2 = kontaktInfo.postadresse.adresselinje1
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
    fun mapBrevmottaker(brev: Brev, mottaker: Mottaker, brevSpraak: String): BrevMottaker {
        return brev.brevmottaker {
            navn = mottaker.navn
            spraak = brevSpraak
            fodselsnummer = mottaker.fodselsnummer
            rolle = when(mottaker.rolle){
                RolleType.BM -> "01"
                RolleType.BP -> "02"
//                RolleType.BA -> "03"
                else -> "00"
            }
            fodselsdato = mottaker.fodselsdato

            val adresse = mottaker.adresse
            val postnummerSted = "${adresse.postnummer} ${adresse.poststed ?: ""}"
            adresselinje1 = adresse.adresselinje1
            adresselinje2 = adresse.adresselinje2
            adresselinje3 = adresse.adresselinje3 ?: postnummerSted
            adresselinje4 = if (adresselinje3 == postnummerSted) null else postnummerSted
            boligNr = if (adresse.bruksenhetsnummer == BRUKSHENETSNUMMER_STANDARD) null else adresse.bruksenhetsnummer
            postnummer = adresse.postnummer
            landkode = adresse.landkode
        }
    }
}