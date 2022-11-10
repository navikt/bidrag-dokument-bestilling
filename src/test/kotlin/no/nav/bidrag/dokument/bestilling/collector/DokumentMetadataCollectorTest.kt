package no.nav.bidrag.dokument.bestilling.collector

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import com.ninjasquad.springmockk.SpykBean
import io.kotest.assertions.assertSoftly
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import no.nav.bidrag.dokument.bestilling.config.SaksbehandlerInfoManager
import no.nav.bidrag.dokument.bestilling.consumer.KodeverkConsumer
import no.nav.bidrag.dokument.bestilling.model.BrevKode
import no.nav.bidrag.dokument.bestilling.model.DISREKSJONSKODE_KODE_6
import no.nav.bidrag.dokument.bestilling.model.DokumentBestilling
import no.nav.bidrag.dokument.bestilling.model.DokumentBestillingRequest
import no.nav.bidrag.dokument.bestilling.model.FantIkkeSakException
import no.nav.bidrag.dokument.bestilling.model.KodeverkResponse
import no.nav.bidrag.dokument.bestilling.model.RolleType
import no.nav.bidrag.dokument.bestilling.model.SakRolle
import no.nav.bidrag.dokument.bestilling.model.Saksbehandler
import no.nav.bidrag.dokument.bestilling.model.SaksbehandlerInfoResponse
import no.nav.bidrag.dokument.bestilling.model.SamhandlerInformasjon
import no.nav.bidrag.dokument.bestilling.model.SamhandlerManglerKontaktinformasjon
import no.nav.bidrag.dokument.bestilling.service.KodeverkService
import no.nav.bidrag.dokument.bestilling.service.OrganisasjonService
import no.nav.bidrag.dokument.bestilling.service.PersonService
import no.nav.bidrag.dokument.bestilling.service.SakService
import no.nav.bidrag.dokument.bestilling.utils.ANNEN_MOTTAKER
import no.nav.bidrag.dokument.bestilling.utils.BARN1
import no.nav.bidrag.dokument.bestilling.utils.BARN2
import no.nav.bidrag.dokument.bestilling.utils.BARN3
import no.nav.bidrag.dokument.bestilling.utils.BM1
import no.nav.bidrag.dokument.bestilling.utils.BP1
import no.nav.bidrag.dokument.bestilling.utils.DEFAULT_SAKSNUMMER
import no.nav.bidrag.dokument.bestilling.utils.DEFAULT_TITLE_DOKUMENT
import no.nav.bidrag.dokument.bestilling.utils.SAKSBEHANDLER_IDENT
import no.nav.bidrag.dokument.bestilling.utils.SAKSBEHANDLER_NAVN
import no.nav.bidrag.dokument.bestilling.utils.SAMHANDLER_IDENT
import no.nav.bidrag.dokument.bestilling.utils.SAMHANDLER_INFO
import no.nav.bidrag.dokument.bestilling.utils.createEnhetKontaktInformasjon
import no.nav.bidrag.dokument.bestilling.utils.createPostAdresseResponse
import no.nav.bidrag.dokument.bestilling.utils.createSakResponse
import no.nav.bidrag.dokument.bestilling.utils.readFile
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.LocalDate


@ExtendWith(MockKExtension::class)
internal class DokumentMetadataCollectorTest {

    @MockK
    lateinit var personService: PersonService
    @MockK
    lateinit var sakService: SakService
    @MockK
    lateinit var kodeverkConsumer: KodeverkConsumer
    @MockK
    lateinit var saksbehandlerInfoManager: SaksbehandlerInfoManager
    @MockK
    lateinit var organisasjonService: OrganisasjonService
    @InjectMockKs
    lateinit var kodeverkService: KodeverkService
    @InjectMockKs
    lateinit var metadataCollector: DokumentMetadataCollector


    @BeforeEach
    fun initMocks(){
        val kodeverkResponse = ObjectMapper().findAndRegisterModules().readValue(readFile("api/landkoder.json"), KodeverkResponse::class.java)
        every { kodeverkConsumer.hentLandkoder() } returns kodeverkResponse
    }

    @AfterEach
    fun resetMocks(){
       clearAllMocks()
    }

    fun mockDefaultValues(){
        every { personService.hentPerson(BM1.ident, any()) } returns BM1
        every { personService.hentPerson(BP1.ident, any()) } returns BP1
        every { personService.hentPerson(BARN1.ident, any()) } returns BARN1
        every { personService.hentPerson(BARN2.ident, any()) } returns BARN2
        every { personService.hentSpraak(any()) } returns "NB"
        every { personService.hentPersonAdresse(any(), any()) } returns createPostAdresseResponse()

        every { organisasjonService.hentEnhetKontaktInfo(any(), any()) } returns createEnhetKontaktInformasjon()
        every { sakService.hentSak(any()) } returns createSakResponse()
        every { saksbehandlerInfoManager.hentSaksbehandler() } returns Saksbehandler(SAKSBEHANDLER_IDENT, SAKSBEHANDLER_NAVN)
        every { saksbehandlerInfoManager.hentSaksbehandlerBrukerId() } returns SAKSBEHANDLER_IDENT
    }

    @Test
    fun `should map to bestilling request`(){
        mockDefaultValues()
        val adresseResponse = createPostAdresseResponse()
        val defaultKontaktinfo = createEnhetKontaktInformasjon()
        every { personService.hentPersonAdresse(any(), any()) } returns adresseResponse
        val request = DokumentBestillingRequest(
            mottakerId = BM1.ident,
            gjelderId = BM1.ident,
            saksnummer = DEFAULT_SAKSNUMMER,
            tittel = DEFAULT_TITLE_DOKUMENT,
            enhet = "4806",
            spraak = "NB"
        )
        val bestilling = mapToBestillingsdata(request)
        assertSoftly {
            bestilling.mottaker?.spraak shouldBe "NB"
            bestilling.mottaker?.navn shouldBe BM1.navn
            bestilling.mottaker?.fodselsnummer shouldBe BM1.ident
            bestilling.mottaker?.rolle shouldBe RolleType.BM
            bestilling.mottaker?.fodselsdato shouldBe BM1.foedselsdato
            bestilling.mottaker?.adresse?.adresselinje1 shouldBe adresseResponse.adresselinje1
            bestilling.mottaker?.adresse?.adresselinje2 shouldBe adresseResponse.adresselinje2
            bestilling.mottaker?.adresse?.adresselinje3 shouldBe "3030 Drammen"
            bestilling.mottaker?.adresse?.adresselinje4 shouldBe null
            bestilling.mottaker?.adresse?.bruksenhetsnummer shouldBe adresseResponse.bruksenhetsnummer
            bestilling.mottaker?.adresse?.postnummer shouldBe adresseResponse.postnummer
            bestilling.mottaker?.adresse?.poststed shouldBe adresseResponse.poststed
            bestilling.mottaker?.adresse?.landkode shouldBe adresseResponse.land
            bestilling.mottaker?.adresse?.landkode3 shouldBe adresseResponse.land3
            bestilling.mottaker?.adresse?.land shouldBe "NORGE"

            bestilling.gjelder?.fodselsnummer shouldBe BM1.ident
            bestilling.gjelder?.rolle shouldBe RolleType.BM

            bestilling.kontaktInfo?.navn shouldBe defaultKontaktinfo.enhetNavn
            bestilling.kontaktInfo?.telefonnummer shouldBe defaultKontaktinfo.telefonnummer
            bestilling.kontaktInfo?.enhetId shouldBe defaultKontaktinfo.enhetIdent
            bestilling.kontaktInfo?.postadresse?.adresselinje1 shouldBe defaultKontaktinfo.postadresse?.adresselinje1
            bestilling.kontaktInfo?.postadresse?.adresselinje2 shouldBe defaultKontaktinfo.postadresse?.adresselinje2
            bestilling.kontaktInfo?.postadresse?.postnummer shouldBe defaultKontaktinfo.postadresse?.postnummer
            bestilling.kontaktInfo?.postadresse?.poststed shouldBe defaultKontaktinfo.postadresse?.poststed
            bestilling.kontaktInfo?.postadresse?.land shouldBe "Norge"

            bestilling.saksbehandler?.ident shouldBe SAKSBEHANDLER_IDENT
            bestilling.saksbehandler?.navn shouldBe SAKSBEHANDLER_NAVN

            bestilling.roller shouldHaveSize 4
            bestilling.roller.bidragsmottaker?.fodselsnummer shouldBe BM1.ident
            bestilling.roller.bidragsmottaker?.navn shouldBe BM1.fornavnEtternavn
            bestilling.roller.bidragsmottaker?.fodselsdato shouldBe BM1.foedselsdato
            bestilling.roller.bidragsmottaker?.landkode shouldBe "NO"
            bestilling.roller.bidragsmottaker?.landkode3 shouldBe "NOR"

            bestilling.roller.bidragspliktig?.fodselsnummer shouldBe BP1.ident
            bestilling.roller.bidragspliktig?.navn shouldBe BP1.fornavnEtternavn
            bestilling.roller.bidragspliktig?.fodselsdato shouldBe BP1.foedselsdato
            bestilling.roller.bidragspliktig?.landkode shouldBe "NO"
            bestilling.roller.bidragspliktig?.landkode3 shouldBe "NOR"

            bestilling.roller.barn shouldHaveSize 2
            bestilling.roller.barn[0].fodselsnummer shouldBe BARN2.ident
            bestilling.roller.barn[0].fodselsdato shouldBe BARN2.foedselsdato
            bestilling.roller.barn[0].fornavn shouldBe BARN2.fornavn
            bestilling.roller.barn[0].navn shouldBe BARN2.fornavnEtternavn

            bestilling.roller.barn[1].fodselsnummer shouldBe BARN1.ident
            bestilling.roller.barn[1].fodselsdato shouldBe BARN1.foedselsdato
            bestilling.roller.barn[1].fornavn shouldBe BARN1.fornavn
            bestilling.roller.barn[1].navn shouldBe BARN1.fornavnEtternavn

            bestilling.spraak shouldBe "NB"
            bestilling.saksnummer shouldBe DEFAULT_SAKSNUMMER
            bestilling.tittel shouldBe DEFAULT_TITLE_DOKUMENT
            bestilling.enhet shouldBe "4806"
            bestilling.rmISak shouldBe false
        }
    }

    @Test
    fun `should add saksbehandler from request when available`(){
        mockDefaultValues()
        val saksbehandlerId = "Z123213"
        every { saksbehandlerInfoManager.hentSaksbehandler() } returns Saksbehandler(saksbehandlerId, "Navn saksbehandler")

        val request = DokumentBestillingRequest(
            mottakerId = BM1.ident,
            gjelderId = BM1.ident,
            saksnummer = DEFAULT_SAKSNUMMER,
            tittel = DEFAULT_TITLE_DOKUMENT,
            enhet = "4806",
            spraak = "NB",
            saksbehandler = Saksbehandler(saksbehandlerId)
        )
        val bestilling = mapToBestillingsdata(request)
        assertSoftly {
            bestilling.saksbehandler?.ident shouldBe saksbehandlerId
            bestilling.saksbehandler?.navn shouldBe "Navn saksbehandler"
        }
    }

    @Test
    fun `should add saksbehandler ident and name from request when available`(){
        mockDefaultValues()
        val saksbehandlerId = "Z123213"
        val request = DokumentBestillingRequest(
            mottakerId = BM1.ident,
            gjelderId = BM1.ident,
            saksnummer = DEFAULT_SAKSNUMMER,
            tittel = DEFAULT_TITLE_DOKUMENT,
            enhet = "4806",
            spraak = "NB",
            saksbehandler = Saksbehandler(saksbehandlerId, "Navn fra request")
        )
        val bestilling = mapToBestillingsdata(request)
        assertSoftly {
            bestilling.saksbehandler?.ident shouldBe saksbehandlerId
            bestilling.saksbehandler?.navn shouldBe "Navn fra request"
        }
    }

    @Test
    fun `should map mottaker adresse with bruksenhetsnummer standard value`(){
        mockDefaultValues()
        val adresseResponse = createPostAdresseResponse()
            .copy(bruksenhetsnummer = "H0101")
        every { personService.hentPersonAdresse(any(), any()) } returns adresseResponse
        val request = DokumentBestillingRequest(
            mottakerId = BM1.ident,
            gjelderId = BM1.ident,
            saksnummer = DEFAULT_SAKSNUMMER,
            tittel = DEFAULT_TITLE_DOKUMENT,
            enhet = "4806",
            spraak = "NB"
        )
        val bestilling = mapToBestillingsdata(request)
        assertSoftly {
            bestilling.mottaker?.adresse?.adresselinje1 shouldBe adresseResponse.adresselinje1
            bestilling.mottaker?.adresse?.adresselinje2 shouldBe adresseResponse.adresselinje2
            bestilling.mottaker?.adresse?.adresselinje3 shouldBe "3030 Drammen"
            bestilling.mottaker?.adresse?.adresselinje4 shouldBe null
            bestilling.mottaker?.adresse?.bruksenhetsnummer shouldBe null
            bestilling.mottaker?.adresse?.postnummer shouldBe adresseResponse.postnummer
            bestilling.mottaker?.adresse?.poststed shouldBe adresseResponse.poststed
            bestilling.mottaker?.adresse?.landkode shouldBe adresseResponse.land
            bestilling.mottaker?.adresse?.landkode3 shouldBe adresseResponse.land3
            bestilling.mottaker?.adresse?.land shouldBe "NORGE"
        }
    }

    @Test
    fun `should map mottaker add country name to adresselinje 4 when land not Norway`(){
        mockDefaultValues()
        val adresseResponse = createPostAdresseResponse()
            .copy(bruksenhetsnummer = null, land = "TR", land3 = "TUR")
        every { personService.hentPersonAdresse(any(), any()) } returns adresseResponse
        val request = DokumentBestillingRequest(
            mottakerId = BM1.ident,
            gjelderId = BM1.ident,
            saksnummer = DEFAULT_SAKSNUMMER,
            tittel = DEFAULT_TITLE_DOKUMENT,
            enhet = "4806",
            spraak = "NB"
        )
        val bestilling = mapToBestillingsdata(request)
        assertSoftly {
            bestilling.mottaker?.adresse?.adresselinje1 shouldBe adresseResponse.adresselinje1
            bestilling.mottaker?.adresse?.adresselinje2 shouldBe adresseResponse.adresselinje2
            bestilling.mottaker?.adresse?.adresselinje3 shouldBe "3030 Drammen"
            bestilling.mottaker?.adresse?.adresselinje4 shouldBe "TYRKIA"
            bestilling.mottaker?.adresse?.bruksenhetsnummer shouldBe null
            bestilling.mottaker?.adresse?.postnummer shouldBe adresseResponse.postnummer
            bestilling.mottaker?.adresse?.poststed shouldBe adresseResponse.poststed
            bestilling.mottaker?.adresse?.landkode shouldBe adresseResponse.land
            bestilling.mottaker?.adresse?.landkode3 shouldBe adresseResponse.land3
            bestilling.mottaker?.adresse?.land shouldBe "TYRKIA"
        }
    }

    @Test
    fun `should map mottaker with kortnavn when availabe`(){
        mockDefaultValues()
        val mottaker = BM1.copy(kortNavn = "Etternavn, Kortnavn")
        every { personService.hentPerson(mottaker.ident, any()) } returns mottaker
        val request = DokumentBestillingRequest(
            mottakerId = mottaker.ident,
            gjelderId = BM1.ident,
            saksnummer = DEFAULT_SAKSNUMMER,
            tittel = DEFAULT_TITLE_DOKUMENT,
            enhet = "4806",
            spraak = "NB"
        )
        val bestilling = mapToBestillingsdata(request)
        bestilling.mottaker?.navn shouldBe mottaker.kortNavn
    }

    @Test
    fun `should map with samhandler mottaker`(){
        mockDefaultValues()

        val request = DokumentBestillingRequest(
            mottakerId = SAMHANDLER_IDENT,
            gjelderId = BM1.ident,
            saksnummer = DEFAULT_SAKSNUMMER,
            tittel = DEFAULT_TITLE_DOKUMENT,
            enhet = "4806",
            spraak = "NB",
            samhandlerInformasjon = SAMHANDLER_INFO
        )
        val bestilling = mapToBestillingsdata(request)
        val adresseResponse = SAMHANDLER_INFO.adresse
        assertSoftly {
            bestilling.mottaker?.spraak shouldBe "NB"
            bestilling.mottaker?.navn shouldBe SAMHANDLER_INFO.navn
            bestilling.mottaker?.fodselsnummer shouldBe SAMHANDLER_IDENT
            bestilling.mottaker?.rolle shouldBe null
            bestilling.mottaker?.fodselsdato shouldBe null
            bestilling.mottaker?.adresse?.adresselinje1 shouldBe adresseResponse?.adresselinje1
            bestilling.mottaker?.adresse?.adresselinje2 shouldBe adresseResponse?.adresselinje2
            bestilling.mottaker?.adresse?.adresselinje3 shouldBe "3000 Samhandler adresselinje 3"
            bestilling.mottaker?.adresse?.adresselinje4 shouldBe null
            bestilling.mottaker?.adresse?.bruksenhetsnummer shouldBe null
            bestilling.mottaker?.adresse?.postnummer shouldBe adresseResponse?.postnummer
            bestilling.mottaker?.adresse?.poststed shouldBe null
            bestilling.mottaker?.adresse?.landkode shouldBe adresseResponse?.landkode
            bestilling.mottaker?.adresse?.landkode3 shouldBe adresseResponse?.landkode
            bestilling.mottaker?.adresse?.land shouldBe "NORGE"
        }
    }

    @Test
    fun `should map with samhandler mottaker having utenlandsk adresse and long adresselinje3`(){
        mockDefaultValues()

        val samhandlerInfo = SAMHANDLER_INFO.copy(
            adresse = SAMHANDLER_INFO.adresse?.copy(
                adresselinje3 = "En veldig lang adresselinje3 som er litt lenger",
                landkode = "TUR"
            )
        )
        val request = DokumentBestillingRequest(
            mottakerId = SAMHANDLER_IDENT,
            gjelderId = BM1.ident,
            saksnummer = DEFAULT_SAKSNUMMER,
            tittel = DEFAULT_TITLE_DOKUMENT,
            enhet = "4806",
            spraak = "NB",
            samhandlerInformasjon = samhandlerInfo
        )
        val bestilling = mapToBestillingsdata(request)
        val adresseResponse = samhandlerInfo.adresse
        assertSoftly {
            bestilling.mottaker?.spraak shouldBe "NB"
            bestilling.mottaker?.navn shouldBe SAMHANDLER_INFO.navn
            bestilling.mottaker?.fodselsnummer shouldBe SAMHANDLER_IDENT
            bestilling.mottaker?.rolle shouldBe null
            bestilling.mottaker?.fodselsdato shouldBe null
            bestilling.mottaker?.adresse?.adresselinje1 shouldBe adresseResponse?.adresselinje1
            bestilling.mottaker?.adresse?.adresselinje2 shouldBe adresseResponse?.adresselinje2
            bestilling.mottaker?.adresse?.adresselinje3 shouldBe "3000 En veldig lang adresselin"
            bestilling.mottaker?.adresse?.adresselinje4 shouldBe null
            bestilling.mottaker?.adresse?.bruksenhetsnummer shouldBe null
            bestilling.mottaker?.adresse?.postnummer shouldBe adresseResponse?.postnummer
            bestilling.mottaker?.adresse?.poststed shouldBe null
            bestilling.mottaker?.adresse?.landkode shouldBe adresseResponse?.landkode
            bestilling.mottaker?.adresse?.landkode3 shouldBe adresseResponse?.landkode
            bestilling.mottaker?.adresse?.land shouldBe "TYRKIA"
        }
    }

    @Test
    fun `should map with mottakerid not in roller`(){
        mockDefaultValues()
        every { personService.hentPerson(ANNEN_MOTTAKER.ident, any()) } returns ANNEN_MOTTAKER

        val request = DokumentBestillingRequest(
            mottakerId = ANNEN_MOTTAKER.ident,
            gjelderId = BM1.ident,
            saksnummer = DEFAULT_SAKSNUMMER,
            tittel = DEFAULT_TITLE_DOKUMENT,
            enhet = "4806",
            spraak = "NB",
        )
        val bestilling = mapToBestillingsdata(request)
        assertSoftly {
            bestilling.mottaker?.spraak shouldBe "NB"
            bestilling.mottaker?.navn shouldBe ANNEN_MOTTAKER.navn
            bestilling.mottaker?.fodselsnummer shouldBe ANNEN_MOTTAKER.ident
            bestilling.mottaker?.rolle shouldBe null
            bestilling.mottaker?.fodselsdato shouldBe ANNEN_MOTTAKER.foedselsdato
        }
    }

    @Test
    fun `should pick gjelder from roller when gjelderId is null`(){
        mockDefaultValues()
        every { personService.hentPerson(ANNEN_MOTTAKER.ident, any()) } returns ANNEN_MOTTAKER

        val request = DokumentBestillingRequest(
            mottakerId = ANNEN_MOTTAKER.ident,
            gjelderId = null,
            saksnummer = DEFAULT_SAKSNUMMER,
            tittel = DEFAULT_TITLE_DOKUMENT,
            enhet = "4806",
            spraak = "NB",
        )
        val bestilling = mapToBestillingsdata(request)
        assertSoftly {
            bestilling.gjelder?.fodselsnummer shouldBe BM1.ident
            bestilling.gjelder?.rolle shouldBe RolleType.BM
        }
    }

    @Test
    fun `should pick gjelder from roller when gjelderId is not in roller`(){
        mockDefaultValues()
        every { personService.hentPerson(ANNEN_MOTTAKER.ident, any()) } returns ANNEN_MOTTAKER

        val request = DokumentBestillingRequest(
            mottakerId = ANNEN_MOTTAKER.ident,
            gjelderId = "3123213213",
            saksnummer = DEFAULT_SAKSNUMMER,
            tittel = DEFAULT_TITLE_DOKUMENT,
            enhet = "4806",
            spraak = "NB",
        )
        val bestilling = mapToBestillingsdata(request)
        assertSoftly {
            bestilling.gjelder?.fodselsnummer shouldBe BM1.ident
            bestilling.gjelder?.rolle shouldBe RolleType.BM
        }
    }

    @Test
    fun `should pick gjelder as BP from roller when gjelderId is not in roller and BM not exists`(){
        mockDefaultValues()
        val saksnummer = "22222"
        val barn1Dod = BARN1.copy(doedsdato = LocalDate.parse("2022-01-01"))
        val sak = createSakResponse().copy(
            roller = listOf(
                SakRolle(
                    foedselsnummer = BP1.ident,
                    rolleType = RolleType.BP
                ),
                SakRolle(
                    foedselsnummer = barn1Dod.ident,
                    rolleType = RolleType.BA
                ),
                SakRolle(
                    foedselsnummer = BARN2.ident,
                    rolleType = RolleType.BA
                )
            )
        )
        every { sakService.hentSak(saksnummer) } returns sak
        every { personService.hentPerson(barn1Dod.ident, any()) } returns barn1Dod

        val request = DokumentBestillingRequest(
            mottakerId = BM1.ident,
            gjelderId = "213213213213213",
            saksnummer = saksnummer,
            tittel = DEFAULT_TITLE_DOKUMENT,
            enhet = "4806",
            spraak = "NB",
            samhandlerInformasjon = null
        )
        val bestilling = mapToBestillingsdata(request)
        assertSoftly {
            bestilling.gjelder?.fodselsnummer shouldBe BP1.ident
            bestilling.gjelder?.rolle shouldBe RolleType.BP
        }
    }

    @Test
    fun `should use enhet from sak when request is missing enhet`(){
        mockDefaultValues()
        val sakresponse = createSakResponse().copy(eierfogd = "4888")
        every { sakService.hentSak(any()) } returns sakresponse
        val request = DokumentBestillingRequest(
            mottakerId = BM1.ident,
            saksnummer = DEFAULT_SAKSNUMMER,
            tittel = DEFAULT_TITLE_DOKUMENT,
            enhet = null,
            spraak = "NB",
            samhandlerInformasjon = null
        )
        val bestilling = mapToBestillingsdata(request)
        bestilling.enhet shouldBe "4888"
    }

    @Test
    fun `should throw when mottaker is samhandler but is missing samhandlerInformasjon`(){
        mockDefaultValues()

        val request = DokumentBestillingRequest(
            mottakerId = SAMHANDLER_IDENT,
            gjelderId = BM1.ident,
            saksnummer = DEFAULT_SAKSNUMMER,
            tittel = DEFAULT_TITLE_DOKUMENT,
            enhet = "4806",
            spraak = "NB",
            samhandlerInformasjon = null
        )
       shouldThrow<SamhandlerManglerKontaktinformasjon> {  mapToBestillingsdata(request) }
    }

    @Test
    fun `should throw when sak is not found`(){
        mockDefaultValues()
        val saksnummer = "111111"
        every { sakService.hentSak(saksnummer) } returns null

        val request = DokumentBestillingRequest(
            mottakerId = "123213213",
            saksnummer = saksnummer,
            tittel = DEFAULT_TITLE_DOKUMENT,
            enhet = "4806",
            spraak = "NB",
            samhandlerInformasjon = null
        )
        shouldThrow<FantIkkeSakException> {  mapToBestillingsdata(request) }
    }

    @Test
    fun `should map with roller having kode6 diskresjon`(){
        mockDefaultValues()
        val barn1Kode6 = BARN1.copy(diskresjonskode = DISREKSJONSKODE_KODE_6)
        val barn2Kode6 = BARN2.copy(diskresjonskode = DISREKSJONSKODE_KODE_6, foedselsdato = null)
        val bmKode6 = BM1.copy(diskresjonskode = DISREKSJONSKODE_KODE_6)
        val bpKode6 = BP1.copy(diskresjonskode = DISREKSJONSKODE_KODE_6)
        every { personService.hentPerson(BM1.ident, any()) } returns bmKode6
        every { personService.hentPerson(BP1.ident, any()) } returns bpKode6
        every { personService.hentPerson(BARN1.ident, any()) } returns barn1Kode6
        every { personService.hentPerson(BARN2.ident, any()) } returns barn2Kode6

        val request = DokumentBestillingRequest(
            mottakerId = BM1.ident,
            gjelderId = BM1.ident,
            saksnummer = DEFAULT_SAKSNUMMER,
            tittel = DEFAULT_TITLE_DOKUMENT,
            enhet = "4806",
            spraak = "NB"
        )
        val bestilling = mapToBestillingsdata(request)
        val adresseResponse = createPostAdresseResponse()
        assertSoftly {
            bestilling.mottaker?.spraak shouldBe "NB"
            bestilling.mottaker?.navn shouldBe bmKode6.navn
            bestilling.mottaker?.fodselsnummer shouldBe bmKode6.ident
            bestilling.mottaker?.rolle shouldBe RolleType.BM
            bestilling.mottaker?.fodselsdato shouldBe BM1.foedselsdato
            bestilling.mottaker?.adresse?.adresselinje1 shouldBe adresseResponse.adresselinje1
            bestilling.mottaker?.adresse?.adresselinje2 shouldBe adresseResponse.adresselinje2
            bestilling.mottaker?.adresse?.adresselinje3 shouldBe "3030 Drammen"
            bestilling.mottaker?.adresse?.adresselinje4 shouldBe null
            bestilling.mottaker?.adresse?.bruksenhetsnummer shouldBe adresseResponse.bruksenhetsnummer
            bestilling.mottaker?.adresse?.postnummer shouldBe adresseResponse.postnummer
            bestilling.mottaker?.adresse?.poststed shouldBe adresseResponse.poststed
            bestilling.mottaker?.adresse?.landkode shouldBe adresseResponse.land
            bestilling.mottaker?.adresse?.landkode3 shouldBe adresseResponse.land3
            bestilling.mottaker?.adresse?.land shouldBe "NORGE"

            bestilling.roller shouldHaveSize 4
            bestilling.roller.bidragsmottaker?.fodselsnummer shouldBe bmKode6.ident
            bestilling.roller.bidragsmottaker?.navn shouldBe ""
            bestilling.roller.bidragsmottaker?.fodselsdato shouldBe null
            bestilling.roller.bidragsmottaker?.landkode shouldBe "NO"
            bestilling.roller.bidragsmottaker?.landkode3 shouldBe "NOR"

            bestilling.roller.bidragspliktig?.fodselsnummer shouldBe bpKode6.ident
            bestilling.roller.bidragspliktig?.navn shouldBe ""
            bestilling.roller.bidragspliktig?.fodselsdato shouldBe null
            bestilling.roller.bidragspliktig?.landkode shouldBe "NO"
            bestilling.roller.bidragspliktig?.landkode3 shouldBe "NOR"

            bestilling.roller.barn shouldHaveSize 2
            bestilling.roller.barn[0].fodselsnummer shouldBe barn1Kode6.ident
            bestilling.roller.barn[0].fodselsdato shouldBe null
            bestilling.roller.barn[0].fornavn shouldBe "(BARN FØDT I ${barn1Kode6.foedselsdato?.year})"
            bestilling.roller.barn[0].navn shouldBe "(BARN FØDT I ${barn1Kode6.foedselsdato?.year})"

            bestilling.roller.barn[1].fodselsnummer shouldBe barn2Kode6.ident
            bestilling.roller.barn[1].fodselsdato shouldBe null
            bestilling.roller.barn[1].fornavn shouldBe "(BARN)"
            bestilling.roller.barn[1].navn shouldBe "(BARN)"
        }
    }

    @Test
    fun `should map with roller having kode6 diskresjon with english language`(){
        mockDefaultValues()
        val barn1Kode6 = BARN1.copy(diskresjonskode = DISREKSJONSKODE_KODE_6)
        val barn2Kode6 = BARN2.copy(diskresjonskode = DISREKSJONSKODE_KODE_6, foedselsdato = null)
        val bmKode6 = BM1.copy(diskresjonskode = DISREKSJONSKODE_KODE_6)
        val bpKode6 = BP1.copy(diskresjonskode = DISREKSJONSKODE_KODE_6)
        every { personService.hentPerson(BM1.ident, any()) } returns bmKode6
        every { personService.hentPerson(BP1.ident, any()) } returns bpKode6
        every { personService.hentPerson(BARN1.ident, any()) } returns barn1Kode6
        every { personService.hentPerson(BARN2.ident, any()) } returns barn2Kode6

        val request = DokumentBestillingRequest(
            mottakerId = BM1.ident,
            gjelderId = BM1.ident,
            saksnummer = DEFAULT_SAKSNUMMER,
            tittel = DEFAULT_TITLE_DOKUMENT,
            enhet = "4806",
            spraak = "EN"
        )
        val bestilling = mapToBestillingsdata(request)
        assertSoftly {
            bestilling.roller.barn[0].fornavn shouldBe "(CHILD BORN IN ${barn1Kode6.foedselsdato?.year})"
            bestilling.roller.barn[0].navn shouldBe "(CHILD BORN IN ${barn1Kode6.foedselsdato?.year})"

            bestilling.roller.barn[1].fornavn shouldBe "(CHILD)"
            bestilling.roller.barn[1].navn shouldBe "(CHILD)"
        }
    }

    @Test
    fun `should map with roller having kode6 diskresjon with nynorsk language`(){
        mockDefaultValues()
        val barn1Kode6 = BARN1.copy(diskresjonskode = DISREKSJONSKODE_KODE_6)
        val barn2Kode6 = BARN2.copy(diskresjonskode = DISREKSJONSKODE_KODE_6, foedselsdato = null)
        val bmKode6 = BM1.copy(diskresjonskode = DISREKSJONSKODE_KODE_6)
        val bpKode6 = BP1.copy(diskresjonskode = DISREKSJONSKODE_KODE_6)
        every { personService.hentPerson(BM1.ident, any()) } returns bmKode6
        every { personService.hentPerson(BP1.ident, any()) } returns bpKode6
        every { personService.hentPerson(BARN1.ident, any()) } returns barn1Kode6
        every { personService.hentPerson(BARN2.ident, any()) } returns barn2Kode6

        val request = DokumentBestillingRequest(
            mottakerId = BM1.ident,
            gjelderId = BM1.ident,
            saksnummer = DEFAULT_SAKSNUMMER,
            tittel = DEFAULT_TITLE_DOKUMENT,
            enhet = "4806",
            spraak = "NN"
        )
        val bestilling = mapToBestillingsdata(request)
        assertSoftly {
            bestilling.roller.barn[0].fornavn shouldBe "(BARN FØDD I ${barn1Kode6.foedselsdato?.year})"
            bestilling.roller.barn[0].navn shouldBe "(BARN FØDD I ${barn1Kode6.foedselsdato?.year})"

            bestilling.roller.barn[1].fornavn shouldBe "(BARN)"
            bestilling.roller.barn[1].navn shouldBe "(BARN)"
        }
    }

    @Nested
    inner class MapRoller {
        @Test
        fun `should set rmISak when sak has rolle RM`(){
            mockDefaultValues()
            val rmIdent = "13123123"
            val saksnummer = "22222"
            val originalSakResponse = createSakResponse()
            val roller = originalSakResponse.roller.toMutableList()
            roller.add(SakRolle(
                foedselsnummer = rmIdent,
                rolleType = RolleType.RM
            ))
            val sak = originalSakResponse.copy(
                roller = roller
            )
            every { sakService.hentSak(saksnummer) } returns sak

            val request = DokumentBestillingRequest(
                mottakerId = BM1.ident,
                saksnummer = saksnummer,
                tittel = DEFAULT_TITLE_DOKUMENT,
                enhet = "4806",
                spraak = "NB",
                samhandlerInformasjon = null
            )
            val bestilling = mapToBestillingsdata(request)
            bestilling.rmISak shouldBe true

        }

        @Test
        fun `should map when bidragsmottaker is missing in sak`(){
            mockDefaultValues()
            val saksnummer = "22222"
            val sak = createSakResponse().copy(
                roller = listOf(
                    SakRolle(
                        foedselsnummer = BP1.ident,
                        rolleType = RolleType.BP
                    ),
                    SakRolle(
                        foedselsnummer = BARN1.ident,
                        rolleType = RolleType.BA
                    ),
                    SakRolle(
                        foedselsnummer = BARN2.ident,
                        rolleType = RolleType.BA
                    )
                )
            )
            every { sakService.hentSak(saksnummer) } returns sak

            val request = DokumentBestillingRequest(
                mottakerId = BM1.ident,
                saksnummer = saksnummer,
                tittel = DEFAULT_TITLE_DOKUMENT,
                enhet = "4806",
                spraak = "NB",
                samhandlerInformasjon = null
            )
            val bestilling = mapToBestillingsdata(request)
            bestilling.roller.bidragsmottaker shouldBe null
            bestilling.roller.bidragspliktig shouldNotBe null
            bestilling.roller.barn shouldHaveSize 2

        }

        @Test
        fun `should map when bidragsplitkig is missing in sak`(){
            mockDefaultValues()
            val saksnummer = "22222"
            val sak = createSakResponse().copy(
                roller = listOf(
                    SakRolle(
                        foedselsnummer = BM1.ident,
                        rolleType = RolleType.BM
                    ),
                    SakRolle(
                        foedselsnummer = BARN1.ident,
                        rolleType = RolleType.BA
                    ),
                    SakRolle(
                        foedselsnummer = BARN2.ident,
                        rolleType = RolleType.BA
                    )
                )
            )
            every { sakService.hentSak(saksnummer) } returns sak

            val request = DokumentBestillingRequest(
                mottakerId = BM1.ident,
                saksnummer = saksnummer,
                tittel = DEFAULT_TITLE_DOKUMENT,
                enhet = "4806",
                spraak = "NB",
                samhandlerInformasjon = null
            )
            val bestilling = mapToBestillingsdata(request)
            bestilling.roller.bidragspliktig shouldBe null
            bestilling.roller.bidragsmottaker shouldNotBe null
            bestilling.roller.barn shouldHaveSize 2
        }

        @Test
        fun `should map when barn is missing in sak`(){
            mockDefaultValues()
            val saksnummer = "22222"
            val sak = createSakResponse().copy(
                roller = listOf(
                    SakRolle(
                        foedselsnummer = BM1.ident,
                        rolleType = RolleType.BM
                    )
                )
            )
            every { sakService.hentSak(saksnummer) } returns sak

            val request = DokumentBestillingRequest(
                mottakerId = BM1.ident,
                saksnummer = saksnummer,
                tittel = DEFAULT_TITLE_DOKUMENT,
                enhet = "4806",
                spraak = "NB",
                samhandlerInformasjon = null
            )
            val bestilling = mapToBestillingsdata(request)
            bestilling.roller.bidragspliktig shouldBe null
            bestilling.roller.bidragsmottaker shouldNotBe null
            bestilling.roller.barn shouldHaveSize 0
        }

        @Test
        fun `should not add dead barn to roller`(){
            mockDefaultValues()
            val saksnummer = "22222"
            val barn1Dod = BARN1.copy(doedsdato = LocalDate.parse("2022-01-01"))
            val sak = createSakResponse().copy(
                roller = listOf(
                    SakRolle(
                        foedselsnummer = BM1.ident,
                        rolleType = RolleType.BM
                    ),
                    SakRolle(
                        foedselsnummer = barn1Dod.ident,
                        rolleType = RolleType.BA
                    ),
                    SakRolle(
                        foedselsnummer = BARN2.ident,
                        rolleType = RolleType.BA
                    )
                )
            )
            every { sakService.hentSak(saksnummer) } returns sak
            every { personService.hentPerson(barn1Dod.ident, any()) } returns barn1Dod

            val request = DokumentBestillingRequest(
                mottakerId = BM1.ident,
                saksnummer = saksnummer,
                tittel = DEFAULT_TITLE_DOKUMENT,
                enhet = "4806",
                spraak = "NB",
                samhandlerInformasjon = null
            )
            val bestilling = mapToBestillingsdata(request)
            bestilling.roller.bidragspliktig shouldBe null
            bestilling.roller.bidragsmottaker shouldNotBe null
            bestilling.roller.barn shouldHaveSize 1
        }
    }

    @Test
    fun `should sort barn by born date`(){
        mockDefaultValues()
        val saksnummer = "22222"
        val barn1 = BARN1.copy(foedselsdato = LocalDate.parse("2020-01-02"))
        val barn2 = BARN2.copy(foedselsdato = LocalDate.parse("2020-05-15"))
        val barn3 = BARN3.copy(foedselsdato = LocalDate.parse("2021-07-20"))
        val barn4 = BARN3.copy(ident = "1231231233333333", foedselsdato = LocalDate.parse("2022-03-15"))
        val sak = createSakResponse().copy(
            roller = listOf(
                SakRolle(
                    foedselsnummer = BM1.ident,
                    rolleType = RolleType.BM
                ),
                SakRolle(
                    foedselsnummer = barn1.ident,
                    rolleType = RolleType.BA
                ),
                SakRolle(
                    foedselsnummer = barn2.ident,
                    rolleType = RolleType.BA
                ),
                SakRolle(
                    foedselsnummer = barn3.ident,
                    rolleType = RolleType.BA
                ),
                SakRolle(
                    foedselsnummer = barn4.ident,
                    rolleType = RolleType.BA
                )
            )
        )
        every { sakService.hentSak(saksnummer) } returns sak
        every { personService.hentPerson(barn1.ident, any()) } returns barn1
        every { personService.hentPerson(barn2.ident, any()) } returns barn2
        every { personService.hentPerson(barn3.ident, any()) } returns barn3
        every { personService.hentPerson(barn4.ident, any()) } returns barn4

        val request = DokumentBestillingRequest(
            mottakerId = BM1.ident,
            saksnummer = saksnummer,
            tittel = DEFAULT_TITLE_DOKUMENT,
            enhet = "4806",
            spraak = "NB",
            samhandlerInformasjon = null
        )
        val bestilling = mapToBestillingsdata(request)
        assertSoftly {
            bestilling.roller.bidragspliktig shouldBe null
            bestilling.roller.bidragsmottaker shouldNotBe null
            bestilling.roller.barn shouldHaveSize 4
            withClue("Første barn må være eldste barn født ${barn1.foedselsdato}") {
                bestilling.roller.barn[0].fodselsnummer shouldBe barn1.ident
            }
            withClue("Andre barn må være nest eldste barn født ${barn2.foedselsdato}") {
                bestilling.roller.barn[1].fodselsnummer shouldBe barn2.ident
            }
            withClue("Tredje barn må være barn født ${barn3.foedselsdato}") {
                bestilling.roller.barn[2].fodselsnummer shouldBe barn3.ident
            }
            withClue("Fjerde barn må være barn født ${barn4.foedselsdato}") {
                bestilling.roller.barn[3].fodselsnummer shouldBe barn4.ident
            }
        }

    }

    private fun mapToBestillingsdata(request: DokumentBestillingRequest): DokumentBestilling {
        return metadataCollector.init(request)
            .addRoller()
            .addMottakerGjelder()
            .addEnhetKontaktInfo()
            .getBestillingData()
    }
}