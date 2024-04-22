package no.nav.bidrag.dokument.bestilling.bestilling.produksjon

import no.nav.bidrag.dokument.bestilling.bestilling.dto.BestillingSystem
import no.nav.bidrag.dokument.bestilling.bestilling.dto.DokumentBestilling
import no.nav.bidrag.dokument.bestilling.bestilling.dto.DokumentBestillingResult
import no.nav.bidrag.dokument.bestilling.bestilling.dto.DokumentMal
import no.nav.bidrag.dokument.bestilling.bestilling.dto.DokumentType
import no.nav.bidrag.dokument.bestilling.bestilling.dto.EnhetKontaktInfo
import no.nav.bidrag.dokument.bestilling.bestilling.dto.Mottaker
import no.nav.bidrag.dokument.bestilling.bestilling.dto.fraVerdi
import no.nav.bidrag.dokument.bestilling.bestilling.dto.tilVerdi
import no.nav.bidrag.dokument.bestilling.bestilling.produksjon.dto.Brev
import no.nav.bidrag.dokument.bestilling.bestilling.produksjon.dto.BrevBestilling
import no.nav.bidrag.dokument.bestilling.bestilling.produksjon.dto.BrevKontaktinfo
import no.nav.bidrag.dokument.bestilling.bestilling.produksjon.dto.BrevMottaker
import no.nav.bidrag.dokument.bestilling.bestilling.produksjon.dto.behandlingType
import no.nav.bidrag.dokument.bestilling.bestilling.produksjon.dto.brevbestilling
import no.nav.bidrag.dokument.bestilling.bestilling.produksjon.dto.soknadType
import no.nav.bidrag.dokument.bestilling.bestilling.produksjon.dto.toKode
import no.nav.bidrag.dokument.bestilling.consumer.BidragDokumentConsumer
import no.nav.bidrag.dokument.bestilling.model.BehandlingType
import no.nav.bidrag.dokument.bestilling.model.MAX_DATE
import no.nav.bidrag.dokument.bestilling.model.ResultatKoder
import no.nav.bidrag.dokument.bestilling.model.tilLocalDateFom
import no.nav.bidrag.dokument.bestilling.model.tilLocalDateTil
import no.nav.bidrag.domene.enums.beregning.Resultatkode
import no.nav.bidrag.domene.enums.diverse.Språk
import no.nav.bidrag.domene.enums.person.Sivilstandskode
import no.nav.bidrag.domene.util.visningsnavn
import no.nav.bidrag.transport.dokument.AvsenderMottakerDto
import no.nav.bidrag.transport.dokument.JournalpostType
import no.nav.bidrag.transport.dokument.OpprettDokumentDto
import no.nav.bidrag.transport.dokument.OpprettJournalpostRequest
import no.nav.bidrag.transport.dokument.isNumeric
import org.springframework.beans.factory.annotation.Value
import org.springframework.jms.core.JmsTemplate
import org.springframework.stereotype.Component

@Component(BestillingSystem.BREVSERVER)
class BrevserverProducer(
    val onlinebrevTemplate: JmsTemplate,
    val bidragDokumentConsumer: BidragDokumentConsumer,
    val hgUgKodeService: HgUgKodeService,
    @Value("\${BREVSERVER_PASSORD}") val brevPassord: String,
) : DokumentProducer {
    override fun produser(
        dokumentBestilling: DokumentBestilling,
        dokumentMal: DokumentMal,
    ): DokumentBestillingResult {
        val journalpostId = opprettJournalpost(dokumentBestilling, dokumentMal)

        onlinebrevTemplate.convertAndSend(
            mapToBrevserverMessage(
                dokumentBestilling,
                dokumentMal,
            ),
        )
        // TODO: Error handling
        return DokumentBestillingResult(
            dokumentReferanse = dokumentBestilling.dokumentreferanse!!,
            journalpostId = journalpostId,
            bestillingSystem = BestillingSystem.BREVSERVER,
        )
    }

    fun opprettJournalpost(
        dokumentBestilling: DokumentBestilling,
        dokumentMal: DokumentMal,
    ): String {
        if (dokumentBestilling.dokumentreferanse.isNullOrEmpty()) {
            val tittel = dokumentBestilling.tittel ?: dokumentMal.beskrivelse
            val response =
                bidragDokumentConsumer.opprettJournalpost(
                    OpprettJournalpostRequest(
                        tittel = tittel,
                        journalførendeEnhet = dokumentBestilling.enhet,
                        tilknyttSaker = listOf(dokumentBestilling.saksnummer!!),
                        dokumenter =
                            listOf(
                                OpprettDokumentDto(
                                    tittel = tittel,
                                    brevkode = dokumentMal.kode,
                                ),
                            ),
                        gjelderIdent = dokumentBestilling.gjelder?.fodselsnummer!!,
                        avsenderMottaker =
                            AvsenderMottakerDto(
                                dokumentBestilling.mottaker?.navn,
                                dokumentBestilling.mottaker?.fodselsnummer!!,
                            ),
                        journalposttype =
                            when (dokumentMal.dokumentType) {
                                DokumentType.UTGÅENDE -> JournalpostType.UTGÅENDE
                                DokumentType.NOTAT -> JournalpostType.NOTAT
                            },
                        saksbehandlerIdent = dokumentBestilling.saksbehandler?.ident,
                    ),
                )

            dokumentBestilling.dokumentreferanse = response?.dokumenter?.get(0)?.dokumentreferanse
            return response?.journalpostId!!
        }
        return ""
    }

    private fun mapToBrevserverMessage(
        dokumentBestilling: DokumentBestilling,
        dokumentMal: DokumentMal,
    ): BrevBestilling {
        val dokumentSpraak = dokumentBestilling.spraak ?: "NB"
        val saksbehandlerNavn = dokumentBestilling.saksbehandler?.navn
        val vedtakInfo = dokumentBestilling.vedtakDetaljer
        return brevbestilling {
            val roller = dokumentBestilling.roller
            val bp = roller.bidragspliktig
            val bm = roller.bidragsmottaker
            val hgUgDto =
                vedtakInfo?.let {
                    hgUgKodeService.findHgUg(
                        it.soknadType,
                        it.søknadFra,
                        it.behandlingType,
                    )
                }
            malpakke = "BI01.${dokumentMal.kode}"
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
                    hgKode = hgUgDto?.hg
                    ugKode = hgUgDto?.ug
                    sakstype =
                        if (dokumentBestilling.sakDetaljer.harUkjentPart) {
                            "X"
                        } else if (dokumentBestilling.sakDetaljer.levdeAdskilt) {
                            "U"
                        } else {
                            "E"
                        }
                    gebyrsats = dokumentBestilling.sjablonDetaljer.fastsettelseGebyr
                    vedtakInfo?.let {
                        soknGrKode = it.behandlingType?.kode
                        soknFraKode = it.søknadFra?.kode
                        soknType = it.soknadType?.kode
                        virkningsDato = it.virkningstidspunkt
                        mottatDato = it.mottattDato
                    }
                    forskUtBet = vedtakInfo != null
                    // Kode fra beslutningårsak i Bisys.
                    val vedtakPerioder =
                        vedtakInfo?.vedtakBarn?.flatMap { it.stønadsendringer }
                            ?.flatMap { it.vedtakPerioder } ?: emptyList()
                    val antallVedtakPerioder = vedtakPerioder.size
                    resKode =
                        if (antallVedtakPerioder > 1) {
                            ResultatKoder.FLERE_BESLUTNING_LINJER
                        } else if (antallVedtakPerioder == 1) {
                            val resultatKode = vedtakPerioder[0].resultatKode
                            val erInnkrevingPrivatAvtale =
                                resultatKode == ResultatKoder.PRIVAT_AVTALE && "BI01G01" == dokumentMal.kode
                            val erInnkreving =
                                listOf(
                                    ResultatKoder.INNVILGET_VEDTAK,
                                    ResultatKoder.UTENLANDSK_AVGJØRELSE,
                                ).contains(resultatKode)
                            if (erInnkreving || erInnkrevingPrivatAvtale) ResultatKoder.VEDTAK_VANLIG_INNKREVING else resultatKode
                        } else {
                            null
                        }
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
                        aarsakKd = it.årsakKode?.legacyKode ?: it.avslagsKode?.legacyKode // TODO: Oversett til riktig kode
                        undergrp = hgUgDto?.ug
                        type = it.stønadType?.let { BehandlingType.valueOf(it.name).kode }

                        vedtattDato = it.vedtattDato
                        virkningDato = it.virkningstidspunkt
                        soknDato = it.mottattDato
                        sendtDato = it.vedtattDato // TODO: Er dette riktig?
                        saksnr = dokumentBestilling.saksnummer
                    }
                }

                vedtakInfo?.vedtakBarn?.forEach { vedtakBarn ->

                    bidragBarn {
                        barn {
                            navn = vedtakBarn.navn
                            fnr = vedtakBarn.fødselsnummer
                            saksnr = dokumentBestilling.saksnummer
                        }

                        vedtakInfo.barnIHusstandPerioder.forEach {
                            forskuddBarnPeriode {
                                fomDato = it.periode.tilLocalDateFom()
                                tomDato = it.periode.tilLocalDateTil() ?: MAX_DATE
                                antallBarn = it.antall
                            }
                        }

                        vedtakInfo.sivilstandPerioder.forEach { sivilstand ->
                            forskuddSivilstandPeriode {
                                fomDato = sivilstand.periode.tilLocalDateFom()
                                tomDato = sivilstand.periode.tilLocalDateTil() ?: MAX_DATE
                                kode = sivilstand.sivilstand.toKode()
                                beskrivelse = sivilstand.sivilstand.visningsnavn.bruker[Språk.NB]
                            }
                        }
                        vedtakBarn.stønadsendringer.forEach { detaljer ->
                            detaljer.forskuddInntektgrensePerioder.forEach {
                                inntektGrunnlagForskuddPeriode {
                                    fomDato = it.fomDato
                                    tomDato = it.tomDato ?: MAX_DATE
                                    antallBarn = it.antallBarn
                                    forsorgerKode =
                                        when (it.forsorgerType) {
                                            Sivilstandskode.ENSLIG -> "EN"
                                            Sivilstandskode.GIFT_SAMBOER -> "GS"
                                            else -> ""
                                        }
                                    belop50fra = it.beløp50Prosent.fraVerdi()
                                    belop50til = it.beløp50Prosent.tilVerdi()
                                    belop75fra = it.beløp75Prosent.fraVerdi()
                                    belop75til = it.beløp75Prosent.tilVerdi()
                                }
                            }

                            detaljer.vedtakPerioder.forEach { vedtakPeriode ->
                                forskuddVedtakPeriode {
                                    fomDato = vedtakPeriode.fomDato
                                    tomDato = vedtakPeriode.tomDato ?: MAX_DATE
                                    fnr = vedtakBarn.fødselsnummer
                                    resultatKode = vedtakPeriode.resultatKode
                                    forskKode = Resultatkode.fraKode(vedtakPeriode.resultatKode)?.tilForskuddKode()
                                    prosent = if (vedtakPeriode.resultatKode.isNumeric) vedtakPeriode.resultatKode.padStart(3, '0') else "000"
                                    maksInntekt = vedtakPeriode.maksInntekt
                                }

                                vedtakPeriode.inntekter.forEach {
                                    inntektPeriode {
                                        fomDato = it.periode?.tilLocalDateFom()
                                        tomDato = it.periode.tilLocalDateTil() ?: MAX_DATE
                                        belopType = it.beløpKode
                                        belopÅrsinntekt = it.beløp
                                        beskrivelse = it.beskrivelse
                                        rolle = it.rolle.toKode()
                                        fnr = it.fødselsnummer
                                        inntektGrense = vedtakPeriode.inntektGrense
                                    }
                                }
                            }
                        }
                    }
                    vedtakBarn.stønadsendringer.forEach { detaljer ->
                        detaljer.vedtakPerioder.forEach {
                            vedtak {
                                fomDato = it.fomDato
                                tomDato = it.tomDato ?: MAX_DATE
                                fnr = vedtakBarn.fødselsnummer
                                belopBidrag = it.beløp
                                resultatKode = it.resultatKode
                            }
                            forskuddVedtak {
                                fomDato = it.fomDato
                                tomDato = it.tomDato ?: MAX_DATE
                                fnr = vedtakBarn.fødselsnummer
                                resultatKode = it.resultatKode
                                forskKode = Resultatkode.fraKode(it.resultatKode)?.tilForskuddKode()
                                beløp = it.beløp
                                prosent = if (it.resultatKode.isNumeric) it.resultatKode.padStart(3, '0') else "000"
                                maksInntekt = it.maksInntekt
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
                        belForskudd =
                            it.fodselsnummer?.let { it1 -> vedtakInfo?.hentForskuddBarn(it1) }
                    }
                }
            }
        }
    }

    fun mapKontaktInfo(
        brev: Brev,
        _kontaktInfo: EnhetKontaktInfo?,
    ): BrevKontaktinfo? {
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
            avsender =
                avsender {
                    navn = kontaktInfo.navn
                }
            tlfAvsender =
                tlfAvsender {
                    telefonnummer = kontaktInfo.telefonnummer
                }
        }
    }

    fun mapBrevmottaker(
        brev: Brev,
        mottaker: Mottaker,
    ): BrevMottaker {
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

fun Resultatkode.tilForskuddKode() =
    when (this) {
//        Resultatkode.AVSLAG_OVER_18_ÅR -> "BOA"
//        Resultatkode.AVSLAG_IKKE_REGISTRERT_PÅ_ADRESSE -> "BAF"
        else -> null
    }
