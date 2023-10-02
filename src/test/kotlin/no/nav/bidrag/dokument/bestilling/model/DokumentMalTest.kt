package no.nav.bidrag.dokument.bestilling.model

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider
import mu.KotlinLogging
import no.nav.bidrag.dokument.bestilling.bestilling.dto.DokumentMalBrevserver
import no.nav.bidrag.dokument.bestilling.bestilling.dto.dokumentmalerBrevserver
import no.nav.bidrag.dokument.bestilling.bestilling.dto.dokumentmalerBucket
import org.junit.jupiter.api.Test

private val LOGGER = KotlinLogging.logger {}

class DokumentMalTest {

    @Test
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


}