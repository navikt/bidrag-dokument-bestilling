package no.nav.bidrag.dokument.bestilling.api

import com.ninjasquad.springmockk.SpykBean
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.verify
import no.nav.bidrag.dokument.bestilling.api.dto.DokumentBestillingForespørsel
import no.nav.bidrag.dokument.bestilling.api.dto.DokumentBestillingResponse
import no.nav.bidrag.dokument.bestilling.bestilling.dto.BrevKode
import no.nav.bidrag.dokument.bestilling.bestilling.dto.BrevType
import no.nav.bidrag.dokument.bestilling.bestilling.produksjon.DokumentProdusent
import no.nav.bidrag.dokument.bestilling.konsumer.dto.RolleType
import no.nav.bidrag.dokument.bestilling.konsumer.dto.fornavnEtternavn
import no.nav.bidrag.dokument.bestilling.utils.BARN1
import no.nav.bidrag.dokument.bestilling.utils.BARN2
import no.nav.bidrag.dokument.bestilling.utils.BM1
import no.nav.bidrag.dokument.bestilling.utils.BP1
import no.nav.bidrag.dokument.bestilling.utils.SAKSBEHANDLER_IDENT
import no.nav.bidrag.dokument.bestilling.utils.createEnhetKontaktInformasjon
import no.nav.bidrag.dokument.bestilling.utils.createOpprettJournalpostResponse
import no.nav.bidrag.dokument.bestilling.utils.createPostAdresseResponse
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus

class DokumentBestillingBrevkodeTest : AbstractControllerTest() {

    @SpykBean
    lateinit var dokumentProducer: DokumentProdusent

    companion object {
        @JvmStatic
        fun brevkoderUtgaaende() = BrevKode.values().filter { it.brevtype == BrevType.UTGÅENDE }

        @JvmStatic
        fun brevkoderEnhetKontaktinfo(): List<BrevKode> {
            val brevkoder = brevkoderUtgaaende().toMutableList()
            brevkoder.add(BrevKode.BI01P11)
            return brevkoder
        }
    }

    @ParameterizedTest(name = "{index} - Should add default values with sak, saksbehandler, mottaker and gjelder for brevkode {argumentsWithNames}")
    @EnumSource(value = BrevKode::class)
    fun `Should add default values with sak, saksbehandler, mottaker and gjelder`(brevKode: BrevKode) {
        if (!brevKode.enabled) {
            print("brevkode ${brevKode.name} ikke støttet, ignorerer testing")
            return
        }
        stubDefaultValues()
        val bmAdresse = createPostAdresseResponse()
        val tittel = "Tittel på dokument"
        val saksnummer = "123213"
        val mottakerId = BM1.ident
        val gjelderId = BP1.ident

        stubUtils.stubHentAdresse(postAdresse = bmAdresse)

        stubUtils.stubOpprettJournalpost(createOpprettJournalpostResponse(dokumentReferanse = "DOKREF_1"))

        val request = DokumentBestillingForespørsel(
            mottakerId = mottakerId.verdi,
            gjelderId = gjelderId.verdi,
            saksnummer = saksnummer,
            tittel = tittel,
            enhet = "4806",
            spraak = "NB"

        )

        val response = httpHeaderTestRestTemplate.exchange(
            "${rootUri()}/bestill/${brevKode.name}",
            HttpMethod.POST,
            HttpEntity(request),
            DokumentBestillingResponse::class.java
        )

        response.statusCode shouldBe HttpStatus.OK

        verify(exactly = 1) {
            dokumentProducer.produser(
                withArg { bestilling ->
                    assertSoftly {
                        bestilling.mottaker?.spraak shouldBe "NB"
                        bestilling.mottaker?.navn shouldBe BM1.navn
                        bestilling.mottaker?.fodselsnummer shouldBe BM1.ident
                        bestilling.mottaker?.rolle shouldBe RolleType.BM
                        bestilling.mottaker?.fodselsdato shouldBe BM1.fødselsdato
                        bestilling.mottaker?.adresse shouldNotBe null

                        bestilling.gjelder?.fodselsnummer shouldBe gjelderId
                        bestilling.gjelder?.rolle shouldBe RolleType.BP

                        bestilling.saksbehandler?.ident shouldBe SAKSBEHANDLER_IDENT
                        bestilling.saksbehandler?.navn shouldBe "Saksbehandler Mellomnavn Saksbehandlersen"

                        bestilling.spraak shouldBe "NB"
                        bestilling.saksnummer shouldBe saksnummer
                        bestilling.tittel shouldBe tittel
                        bestilling.enhet shouldBe "4806"
                        bestilling.rmISak shouldBe false
                    }
                },
                brevKode
            )
        }
    }

    @ParameterizedTest(name = "{index} - Should add roller to utgaaende brev with brevkode {argumentsWithNames}")
    @MethodSource("brevkoderUtgaaende")
    fun `Should add roller to utgaaende brev`(brevKode: BrevKode) {
        if (!brevKode.enabled) {
            print("brevkode ${brevKode.name} ikke støttet, ignorerer testing")
            return
        }
        stubDefaultValues()
        val bmAdresse = createPostAdresseResponse()
        val tittel = "Tittel på dokument"
        val saksnummer = "123213"
        val mottakerId = BM1.ident
        val gjelderId = BP1.ident

        stubUtils.stubHentAdresse(postAdresse = bmAdresse)

        stubUtils.stubOpprettJournalpost(createOpprettJournalpostResponse(dokumentReferanse = "DOKREF_1"))

        val request = DokumentBestillingForespørsel(
            mottakerId = mottakerId.verdi,
            gjelderId = gjelderId.verdi,
            saksnummer = saksnummer,
            tittel = tittel,
            enhet = "4806",
            spraak = "NB"

        )

        val response = httpHeaderTestRestTemplate.exchange(
            "${rootUri()}/bestill/${brevKode.name}",
            HttpMethod.POST,
            HttpEntity(request),
            DokumentBestillingResponse::class.java
        )

        response.statusCode shouldBe HttpStatus.OK

        verify(exactly = 1) {
            dokumentProducer.produser(
                withArg { bestilling ->
                    assertSoftly {
                        bestilling.roller shouldHaveSize 4
                        bestilling.roller.bidragsmottaker?.fodselsnummer shouldBe BM1.ident
                        bestilling.roller.bidragsmottaker?.navn shouldBe BM1.fornavnEtternavn()
                        bestilling.roller.bidragsmottaker?.fodselsdato shouldBe BM1.fødselsdato
                        bestilling.roller.bidragsmottaker?.landkode shouldBe "NO"
                        bestilling.roller.bidragsmottaker?.landkode3 shouldBe "NOR"

                        bestilling.roller.bidragspliktig?.fodselsnummer shouldBe BP1.ident
                        bestilling.roller.bidragspliktig?.navn shouldBe BP1.fornavnEtternavn()
                        bestilling.roller.bidragspliktig?.fodselsdato shouldBe BP1.fødselsdato
                        bestilling.roller.bidragspliktig?.landkode shouldBe "NO"
                        bestilling.roller.bidragspliktig?.landkode3 shouldBe "NOR"

                        bestilling.roller.barn shouldHaveSize 2
                        bestilling.roller.barn[0].fodselsnummer shouldBe BARN2.ident
                        bestilling.roller.barn[0].fodselsdato shouldBe BARN2.fødselsdato
                        bestilling.roller.barn[0].fornavn shouldBe BARN2.fornavn
                        bestilling.roller.barn[0].navn shouldBe BARN2.fornavnEtternavn()

                        bestilling.roller.barn[1].fodselsnummer shouldBe BARN1.ident
                        bestilling.roller.barn[1].fodselsdato shouldBe BARN1.fødselsdato
                        bestilling.roller.barn[1].fornavn shouldBe BARN1.fornavn
                        bestilling.roller.barn[1].navn shouldBe BARN1.fornavnEtternavn()
                    }
                },
                brevKode
            )
        }
    }

    @ParameterizedTest(name = "{index} - Should add enhet kontaktinfo for brevkode {argumentsWithNames}")
    @MethodSource("brevkoderEnhetKontaktinfo")
    fun `Should add enhet kontaktinfo`(brevKode: BrevKode) {
        if (!brevKode.enabled) {
            print("brevkode ${brevKode.name} ikke støttet, ignorerer testing")
            return
        }
        stubDefaultValues()
        val bmAdresse = createPostAdresseResponse()
        val tittel = "Tittel på dokument"
        val saksnummer = "123213"
        val mottakerId = BM1.ident
        val gjelderId = BP1.ident
        val enhetKontaktInfo = createEnhetKontaktInformasjon()

        stubUtils.stubHentAdresse(postAdresse = bmAdresse)

        stubUtils.stubOpprettJournalpost(createOpprettJournalpostResponse(dokumentReferanse = "DOKREF_1"))

        val request = DokumentBestillingForespørsel(
            mottakerId = mottakerId.verdi,
            gjelderId = gjelderId.verdi,
            saksnummer = saksnummer,
            tittel = tittel,
            enhet = "4806",
            spraak = "NB"

        )
        val response = httpHeaderTestRestTemplate.exchange(
            "${rootUri()}/bestill/${brevKode.name}",
            HttpMethod.POST,
            HttpEntity(request),
            DokumentBestillingResponse::class.java
        )

        response.statusCode shouldBe HttpStatus.OK

        verify(exactly = 1) {
            dokumentProducer.produser(
                withArg { bestilling ->
                    assertSoftly {
                        bestilling.kontaktInfo?.navn shouldBe enhetKontaktInfo.enhetNavn
                        bestilling.kontaktInfo?.telefonnummer shouldBe enhetKontaktInfo.telefonnummer
                        bestilling.kontaktInfo?.enhetId shouldBe enhetKontaktInfo.enhetIdent
                        bestilling.kontaktInfo?.postadresse?.adresselinje1 shouldBe enhetKontaktInfo.postadresse?.adresselinje1
                        bestilling.kontaktInfo?.postadresse?.adresselinje2 shouldBe enhetKontaktInfo.postadresse?.adresselinje2
                        bestilling.kontaktInfo?.postadresse?.postnummer shouldBe enhetKontaktInfo.postadresse?.postnummer
                        bestilling.kontaktInfo?.postadresse?.poststed shouldBe enhetKontaktInfo.postadresse?.poststed
                        bestilling.kontaktInfo?.postadresse?.land shouldBe "Norge"
                    }
                },
                brevKode
            )
        }
    }
}
