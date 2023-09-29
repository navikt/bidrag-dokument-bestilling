package no.nav.bidrag.dokument.bestilling.model

import mu.KotlinLogging
import no.nav.bidrag.dokument.bestilling.bestilling.dto.DokumentDataGrunnlag
import no.nav.bidrag.dokument.bestilling.bestilling.dto.DokumentMalBrevserver
import no.nav.bidrag.dokument.bestilling.bestilling.dto.DokumentMalEnum
import no.nav.bidrag.dokument.bestilling.bestilling.dto.DokumentType
import org.junit.jupiter.api.Test

private val LOGGER = KotlinLogging.logger {}

class DokumentMalTest {


    @Test
    fun `skal mappe dokumentmaler`() {
        val dokumentmalerConverted = DokumentMalEnum.values().map {
            DokumentMalBrevserver(
                kode = it.name,
                beskrivelse = it.beskrivelse,
                batchbrev = it.batchbrev,
                enabled = it.enabled,
                kreverDataGrunnlag = it.kreverDataGrunnlag ?: DokumentDataGrunnlag(),
                støttetSpråk = it.støttetSpråk,
                dokumentType = it.brevtype
            )
        }


        val text = dokumentmalerConverted.map {
            """
                    
                    DokumentMalBrevserver(
                        kode = "${it.kode}",
                        beskrivelse = "${it.beskrivelse}" ${if (it.dokumentType == DokumentType.NOTAT) ",\n\tdokumentType = DokumentType.${it.dokumentType}" else ""} ${if (it.enabled) ",\n\tenabled = true" else ""} ${if (it.batchbrev) ",\n\tbatchbrev = true" else ""}
                    )
            """.trimIndent()
        }
        LOGGER.info { text }
    }

}