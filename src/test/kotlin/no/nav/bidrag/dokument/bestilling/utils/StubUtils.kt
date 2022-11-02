import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.matching.ContainsPattern
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder
import no.nav.bidrag.dokument.bestilling.model.EnhetInfo
import no.nav.bidrag.dokument.bestilling.model.EnhetKontaktInfo
import no.nav.bidrag.dokument.bestilling.model.EnhetKontaktInfoDto
import no.nav.bidrag.dokument.bestilling.model.HentPersonResponse
import no.nav.bidrag.dokument.bestilling.model.HentPostadresseResponse
import no.nav.bidrag.dokument.bestilling.model.HentSakResponse
import no.nav.bidrag.dokument.bestilling.model.SaksbehandlerInfoResponse
import no.nav.bidrag.dokument.bestilling.utils.BM_PERSON_ID_1
import no.nav.bidrag.dokument.bestilling.utils.BM_PERSON_NAVN_1
import no.nav.bidrag.dokument.bestilling.utils.createEnhetKontaktInformasjon
import no.nav.bidrag.dokument.bestilling.utils.createOpprettJournalpostResponse
import no.nav.bidrag.dokument.bestilling.utils.createPostAdresseResponse
import no.nav.bidrag.dokument.bestilling.utils.createSakResponse
import no.nav.bidrag.dokument.dto.OpprettJournalpostResponse
import org.junit.Assert
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import java.util.Arrays


@Component
class StubUtils {

    @Autowired
    lateinit var objectMapper: ObjectMapper

    companion object {
            fun aClosedJsonResponse(): ResponseDefinitionBuilder {
                return aResponse()
                    .withHeader(HttpHeaders.CONNECTION, "close")
                    .withHeader(HttpHeaders.CONTENT_TYPE, "application/json")
            }
    }

    fun stubHentPerson(fnr: String = ".*", personResponse: HentPersonResponse){
        WireMock.stubFor(
            WireMock.get(WireMock.urlMatching("/person/informasjon/$fnr")).willReturn(
                aClosedJsonResponse()
                    .withStatus(HttpStatus.OK.value())
                    .withBody(convertObjectToString(personResponse))
            )
        )
    }

    fun stubHentAdresse(postAdresse: HentPostadresseResponse = createPostAdresseResponse()){
        WireMock.stubFor(
            WireMock.post(WireMock.urlMatching("/person/adresse/post")).willReturn(
                aClosedJsonResponse()
                    .withStatus(HttpStatus.OK.value())
                    .withBody(convertObjectToString(postAdresse))
            )
        )
    }

    fun stubHentSak(sak: HentSakResponse = createSakResponse()){
        WireMock.stubFor(
            WireMock.get(WireMock.urlMatching("/sak/.*")).willReturn(
                aClosedJsonResponse()
                    .withStatus(HttpStatus.OK.value())
                    .withBody(convertObjectToString(sak))
            )
        )
    }
    fun stubOpprettJournalpost(response: OpprettJournalpostResponse = createOpprettJournalpostResponse()){
        WireMock.stubFor(
            WireMock.post(WireMock.urlMatching("/dokument/journalpost/BIDRAG")).willReturn(
                aClosedJsonResponse()
                    .withStatus(HttpStatus.OK.value())
                    .withBody(convertObjectToString(response))
            )
        )
    }

    fun stubEnhetInfo(response: EnhetInfo = EnhetInfo("4806", "Nav Drammen")){
        WireMock.stubFor(
            WireMock.get(WireMock.urlMatching("/organisasjon/enhet/info/.*")).willReturn(
                aClosedJsonResponse()
                    .withStatus(HttpStatus.OK.value())
                    .withBody(convertObjectToString(response))
            )
        )
    }

    fun stubEnhetKontaktInfo(response: EnhetKontaktInfoDto = createEnhetKontaktInformasjon()){
        WireMock.stubFor(
            WireMock.get(WireMock.urlMatching("/organisasjon/enhet/kontaktinfo/.*")).willReturn(
                aClosedJsonResponse()
                    .withStatus(HttpStatus.OK.value())
                    .withBody(convertObjectToString(response))
            )
        )
    }

    fun stubHentSaksbehandlerInfo(response: SaksbehandlerInfoResponse = SaksbehandlerInfoResponse(BM_PERSON_ID_1, BM_PERSON_NAVN_1)){
        WireMock.stubFor(
            WireMock.get(WireMock.urlMatching("/organisasjon/saksbehandler/info/.*")).willReturn(
                aClosedJsonResponse()
                    .withStatus(HttpStatus.OK.value())
                    .withBody(convertObjectToString(response))
            )
        )
    }

    inner class Verify {

        fun verifyHentPersonCalled(fnr: String?){
            val verify = WireMock.getRequestedFor(
                WireMock.urlMatching("/person/informasjon/$fnr")
            )
            WireMock.verify(verify)
        }
        fun verifyHentEnhetKontaktInfoCalledWith(spraak: String? = "NB", vararg contains: String) {
            val verify = WireMock.getRequestedFor(
                WireMock.urlMatching("/organisasjon/enhet/kontaktinfo/.*/$spraak")
            )
            verifyContains(verify, *contains)
        }

        private fun verifyContains(verify: RequestPatternBuilder, vararg contains: String){
            Arrays.stream(contains).forEach { verify.withRequestBody(ContainsPattern(it)) }
            WireMock.verify(verify)
        }
    }

    fun <T> convertObjectToString(o: T): String {
        return try {
            objectMapper.writeValueAsString(o)
        } catch (e: JsonProcessingException) {
            Assert.fail(e.message)
            ""
        }
    }
}