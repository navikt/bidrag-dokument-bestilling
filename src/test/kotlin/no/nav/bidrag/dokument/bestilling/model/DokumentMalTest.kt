package no.nav.bidrag.dokument.bestilling.model

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider
import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe
import mu.KotlinLogging
import no.nav.bidrag.dokument.bestilling.bestilling.dto.DokumentMalBrevserver
import no.nav.bidrag.dokument.bestilling.bestilling.dto.alleDokumentmaler
import no.nav.bidrag.dokument.bestilling.bestilling.dto.dokumentmalerBrevserver
import no.nav.bidrag.dokument.bestilling.bestilling.dto.dokumentmalerBucket
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

private val LOGGER = KotlinLogging.logger {}

class DokumentMalTest {

    @Test
    @Disabled
    fun `skal mappe dokumentmaler to json`() {
        val filter = SimpleFilterProvider()
            .addFilter(
                "myFilter", SimpleBeanPropertyFilter
                    .serializeAllExcept(
                        "filePath",
                        "innholdtype",
                        "tittel",
                        "bestillingSystem",
                        "folderName",
                        "tilhørerEnheter",
                    )
            )

        LOGGER.info {
            ObjectMapper().findAndRegisterModules()
                .writer(filter)
                .writeValueAsString(dokumentmalerBrevserver.filter { it is DokumentMalBrevserver })
        }

        val filter2 = SimpleFilterProvider()
            .addFilter(
                "myFilter", SimpleBeanPropertyFilter
                    .serializeAllExcept(
                        "filePath",
                        "innholdtype",
                        "bestillingSystem",
                        "folderName",
                        "batchbrev",
                        "enabled",
                        "redigerbar",
                        "kreverDataGrunnlag",
                        "tilhørerEnheter",
                        "dokumentType",
                    )
            )

        LOGGER.info {
            ObjectMapper().findAndRegisterModules()
                .writer(filter2)
                .writeValueAsString(dokumentmalerBucket)
        }
    }


    @Test
    fun `Should not have duplicate dokumentmal kode`() {

        alleDokumentmaler.forEach { mal ->
            val antall = alleDokumentmaler.filter { it.kode == mal.kode }.size
            withClue("Flere dokumentmaler har kode ${mal.kode}. Dokumentmal kode må være unik") {
                antall shouldBe 1
            }
        }
    }

}