package no.nav.bidrag.dokument.bestilling.bestilling.produksjon

import no.nav.bidrag.behandling.felles.enums.SivilstandKode
import no.nav.bidrag.dokument.bestilling.bestilling.dto.BestillingSystem
import no.nav.bidrag.dokument.bestilling.bestilling.dto.BrevKode
import no.nav.bidrag.dokument.bestilling.bestilling.dto.BrevType
import no.nav.bidrag.dokument.bestilling.bestilling.dto.DokumentBestilling
import no.nav.bidrag.dokument.bestilling.bestilling.dto.DokumentBestillingResult
import no.nav.bidrag.dokument.bestilling.bestilling.dto.EnhetKontaktInfo
import no.nav.bidrag.dokument.bestilling.bestilling.dto.ForsorgerType
import no.nav.bidrag.dokument.bestilling.bestilling.dto.Mottaker
import no.nav.bidrag.dokument.bestilling.bestilling.dto.fraVerdi
import no.nav.bidrag.dokument.bestilling.bestilling.dto.tilVerdi
import no.nav.bidrag.dokument.bestilling.bestilling.produksjon.dto.Brev
import no.nav.bidrag.dokument.bestilling.bestilling.produksjon.dto.BrevBestilling
import no.nav.bidrag.dokument.bestilling.bestilling.produksjon.dto.BrevKontaktinfo
import no.nav.bidrag.dokument.bestilling.bestilling.produksjon.dto.BrevMottaker
import no.nav.bidrag.dokument.bestilling.bestilling.produksjon.dto.brevbestilling
import no.nav.bidrag.dokument.bestilling.bestilling.produksjon.dto.toKode
import no.nav.bidrag.dokument.bestilling.consumer.BidragDokumentConsumer
import no.nav.bidrag.dokument.bestilling.model.BehandlingType
import no.nav.bidrag.dokument.bestilling.model.SoknadType
import no.nav.bidrag.dokument.dto.AvsenderMottakerDto
import no.nav.bidrag.dokument.dto.JournalpostType
import no.nav.bidrag.dokument.dto.OpprettDokumentDto
import no.nav.bidrag.dokument.dto.OpprettJournalpostRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.jms.core.JmsTemplate
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component(BestillingSystem.BREVSERVER)
class BrevserverProducer(
    val onlinebrevTemplate: JmsTemplate,
    val bidragDokumentConsumer: BidragDokumentConsumer,
    @Value("\${BREVSERVER_PASSORD}") val brevPassord: String
) : DokumentProducer {

    override fun produser(
        dokumentBestilling: DokumentBestilling,
        brevKode: BrevKode
    ): DokumentBestillingResult {
        val journalpostId = opprettJournalpost(dokumentBestilling, brevKode)

        onlinebrevTemplate.convertAndSend(mapToBrevserverMessage(dokumentBestilling, brevKode))
        // TODO: Error handling
        return DokumentBestillingResult(
            dokumentReferanse = dokumentBestilling.dokumentreferanse!!,
            journalpostId = journalpostId,
            bestillingSystem = BestillingSystem.BREVSERVER
        )
    }

    fun opprettJournalpost(dokumentBestilling: DokumentBestilling, brevKode: BrevKode): String {
        if (dokumentBestilling.dokumentreferanse.isNullOrEmpty()) {
            val tittel = dokumentBestilling.tittel ?: brevKode.beskrivelse
            val response = bidragDokumentConsumer.opprettJournalpost(
                OpprettJournalpostRequest(
                    tittel = tittel,
                    journalførendeEnhet = dokumentBestilling.enhet,
                    tilknyttSaker = listOf(dokumentBestilling.saksnummer!!),
                    dokumenter = listOf(
                        OpprettDokumentDto(
                            tittel = tittel,
                            brevkode = brevKode.name
                        )
                    ),
                    gjelderIdent = dokumentBestilling.gjelder?.fodselsnummer!!,
                    avsenderMottaker = AvsenderMottakerDto(
                        dokumentBestilling.mottaker?.navn,
                        dokumentBestilling.mottaker?.fodselsnummer!!
                    ),
                    journalposttype = when (brevKode.brevtype) {
                        BrevType.UTGÅENDE -> JournalpostType.UTGÅENDE
                        BrevType.NOTAT -> JournalpostType.NOTAT
                    },
                    saksbehandlerIdent = dokumentBestilling.saksbehandler?.ident
                )
            )

            dokumentBestilling.dokumentreferanse = response?.dokumenter?.get(0)?.dokumentreferanse
            return response?.journalpostId!!
        }
        return ""
    }

    private fun mapToBrevserverMessage(
        dokumentBestilling: DokumentBestilling,
        brevKode: BrevKode
    ): BrevBestilling {
        val dokumentSpraak = dokumentBestilling.spraak ?: "NB"
        val saksbehandlerNavn = dokumentBestilling.saksbehandler?.navn
        val defaultToDate = LocalDate.parse("9999-12-31")
        val vedtakInfo = dokumentBestilling.vedtakDetaljer
        val antallVedtakBarn = vedtakInfo?.vedtakBarn?.size ?: 0
        return brevbestilling {
            val roller = dokumentBestilling.roller
            val bp = roller.bidragspliktig
            val bm = roller.bidragsmottaker

            malpakke = "BI01.${brevKode.name}"
            passord = brevPassord
            saksbehandler = dokumentBestilling.saksbehandler?.ident!!
            brev {
                brevref = dokumentBestilling.dokumentreferanse!!
                spraak = dokumentSpraak
                tknr = dokumentBestilling.enhet!!
                mottaker = dokumentBestilling.mottaker?.let { mapBrevmottaker(this, it) }
                kontaktInfo = mapKontaktInfo(this, dokumentBestilling.kontaktInfo)
                soknadBost {
                    saksnr = dokumentBestilling.saksnummer
                    rmISak = dokumentBestilling.rmISak
                    datoSakReg = dokumentBestilling.datoSakOpprettet
                    hgKode = "FO"
                    ugKode = "E"
                    sakstype =
                        "E" // "X" hvis det er en ukjent part i saken, "U" hvis parter levde adskilt, "E" i alle andre tilfeller
                    gebyrsats = dokumentBestilling.sjablonDetaljer.fastsettelseGebyr
                    vedtakInfo?.let {
                        soknGrKode = it.søknadType?.let { type -> BehandlingType.valueOf(type.name).kode }
                        soknFraKode = it.søknadFra?.kode
                        soknType = it.vedtakType.let { type -> SoknadType.valueOf(type.name).kode }
                    }
                    forskUtBet = vedtakInfo != null
                    resKode = if (antallVedtakBarn > 1) "FB" else null
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
                vedtakInfo?.let {
                    soknad {
                        aarsakKd = it.virkningÅrsakKode
                        undergrp = "E"
                        type = it.søknadType?.let { BehandlingType.valueOf(it.name).kode }

                        vedtattDato = it.vedtattDato
                        virkningDato = it.virkningDato
                        saksnr = dokumentBestilling.saksnummer
                    }
                }

                vedtakInfo?.vedtakBarn?.forEach { vedtakBarn ->

                    bidragBarn {
                        barn {
                            navn = vedtakBarn.navn
                            fnr = vedtakBarn.fodselsnummer
                            saksnr = dokumentBestilling.saksnummer
                        }
                        forskuddBarn {
                            fomDato = vedtakInfo.virkningDato
                            antallBarn = vedtakInfo.vedtakBarn.size
                        }
                        vedtakInfo.sivilstandPerioder.forEach { sivilstand ->
                            forskuddSivilstandPeriode {
                                fomDato = sivilstand.fomDato
                                tomDato = sivilstand.tomDato ?: defaultToDate
                                kode = when (sivilstand.sivilstandKode) {
                                    SivilstandKode.ENSLIG -> "UGIF"
                                    SivilstandKode.GIFT -> "GIFT"
                                    SivilstandKode.SAMBOER -> "SAMB"
                                    else -> "UGIF"
                                }
                                beskrivelse = sivilstand.sivilstandKode.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
                            }
                        }
                        vedtakBarn.vedtakDetaljer.forEach { detaljer ->
                            detaljer.vedtakPerioder.forEach { vedtakPeriode ->
                                forskuddVedtakPeriode {
                                    fomDato = vedtakPeriode.fomDato
                                    tomDato = vedtakPeriode.tomDato ?: defaultToDate
                                    fnr = vedtakBarn.fodselsnummer
                                    resultatKode = vedtakPeriode.resultatKode
                                    beløp = vedtakPeriode.beløp
                                    prosent = "100" // TODO: Hvordan skal dette beregnes?
                                    maksInntekt = vedtakPeriode.beløp * dokumentBestilling.sjablonDetaljer.multiplikatorInntekstgrenseForskudd
                                }
                                vedtakInfo.grunnlagForskuddPerioder.forEach {
                                    inntektGrunnlagForskuddPeriode {
                                        fomDato = it.fomDato
                                        tomDato = it.tomDato ?: defaultToDate
                                        antallBarn = it.antallBarn
                                        forsorgerKode = when (it.forsorgerType) {
                                            ForsorgerType.ENSLIG -> "EN"
                                            ForsorgerType.GIFT_SAMBOER -> "GS"
                                        }
                                        belop50fra = it.beløp50Prosent.fraVerdi()
                                        belop50til = it.beløp50Prosent.tilVerdi()
                                        belop75fra = it.beløp75Prosent.fraVerdi()
                                        belop75til = it.beløp75Prosent.tilVerdi()
                                    }
                                }

                                vedtakPeriode.inntektPerioder.forEach {
                                    inntektPeriode {
                                        fomDato = it.fomDato
                                        tomDato = it.tomDato ?: defaultToDate
                                        belopType = it.beløpType.belopstype
                                        belopÅrsinntekt = it.beløp
                                        beskrivelse = it.beløpType.beskrivelse
                                        rolle = it.rolle.toKode()
                                        inntektGrense = dokumentBestilling.sjablonDetaljer.forskuddInnteksintervall
                                    }
                                }
                            }
                        }
                    }
                    vedtakBarn.vedtakDetaljer.forEach { detaljer ->
                        detaljer.vedtakPerioder.forEach {
                            vedtak {
                                fomDato = it.fomDato
                                tomDato = it.tomDato ?: defaultToDate
                                fnr = vedtakBarn.fodselsnummer
                                belopBidrag = it.beløp
                                resultatKode = it.resultatKode
                            }
                            forskuddVedtak {
                                fomDato = it.fomDato
                                tomDato = it.tomDato ?: defaultToDate
                                fnr = vedtakBarn.fodselsnummer
                                resultatKode = it.resultatKode
                                beløp = it.beløp
                                prosent = "100" // TODO: Hvordan skal dette beregnes?
                                maksInntekt = it.beløp * dokumentBestilling.sjablonDetaljer.multiplikatorInntekstgrenseForskudd
                            }
                        }
                    }
                }
                dokumentBestilling.roller.barn.forEach {
                    barnISak {
                        fnr = it.fodselsnummer
                        navn = it.navn
                        fDato = it.fodselsdato
                        fornavn = it.fornavn
                        belForskudd = it.fodselsnummer?.let { it1 -> vedtakInfo?.hentForskuddBarn(it1) }
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
            rolle = mottaker.rolle?.toKode()
            fodselsdato = mottaker.fodselsdato

            val adresse = mottaker.adresse
            if (adresse != null) {
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
