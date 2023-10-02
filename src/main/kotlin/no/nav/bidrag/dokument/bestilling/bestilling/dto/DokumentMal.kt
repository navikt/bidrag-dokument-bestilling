package no.nav.bidrag.dokument.bestilling.bestilling.dto

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import org.springframework.core.io.ClassPathResource
import java.io.IOException
import java.nio.charset.StandardCharsets

object EnhetKode {
    const val FARSKAP = "4860"
    const val UTLAND = "4865"
}

object BestillingSystem {
    const val BREVSERVER = "BREVSERVER"
    const val BUCKET = "BUCKET"
}

enum class DokumentType {
    UTGÅENDE,
    NOTAT
}


enum class InnholdType {
    VARSEL,
    VEDTAK,
    VEDLEGG_VEDTAK,
    VEDLEGG_VARSEL,
    SKJEMA,
}
typealias BestillingSystemType = String

data class DokumentDataGrunnlag(
    val vedtak: Boolean = false,
    val behandling: Boolean = false,
    val roller: Boolean = true,
    val enhetKontaktInfo: Boolean = true
)

enum class StøttetSpråk {
    NB,
    EN,
    NN,
    DE,
    PL,
    FR
}


interface DokumentMal {
    val kode: String
    val beskrivelse: String
    val tittel: String
    val dokumentType: DokumentType
    val bestillingSystem: BestillingSystemType
    val batchbrev: Boolean
    val enabled: Boolean
    val kreverDataGrunnlag: DokumentDataGrunnlag?
    val innholdType: InnholdType
    val redigerbar: Boolean
}


data class DokumentMalBucketUtland(
    override val folderName: String = "vedlegg_utland",
    override val kode: String,
    override val beskrivelse: String,
    override val språk: StøttetSpråk,
    override val tittel: String = beskrivelse,
    override val tilhørerEnheter: List<String> = listOf(EnhetKode.UTLAND),
    override val innholdType: InnholdType = InnholdType.VARSEL
) : DokumentMalBucket(
    kode = kode,
    beskrivelse = beskrivelse,
    folderName = folderName,
    tittel = tittel,
    tilhørerEnheter = tilhørerEnheter,
    språk = språk,
    innholdType = innholdType
)

data class DokumentMalBucketFarskap(
    override val folderName: String = "vedlegg_farskap",
    override val kode: String,
    override val beskrivelse: String,
    override val tittel: String = beskrivelse,
    override val tilhørerEnheter: List<String> = listOf(EnhetKode.FARSKAP),
    override val innholdType: InnholdType = InnholdType.VARSEL
) : DokumentMalBucket(
    kode = kode,
    beskrivelse = beskrivelse,
    folderName = folderName,
    tittel = tittel,
    tilhørerEnheter = tilhørerEnheter,
    innholdType = innholdType
)


open class DokumentMalBucket(
    override val kode: String,
    override val beskrivelse: String,
    override val tittel: String,
    override val batchbrev: Boolean = false,
    override val enabled: Boolean = true,
    override val dokumentType: DokumentType = DokumentType.UTGÅENDE,
    override val redigerbar: Boolean = false,
    override val innholdType: InnholdType = InnholdType.SKJEMA,
    override val kreverDataGrunnlag: DokumentDataGrunnlag? = null,
    override val bestillingSystem: BestillingSystemType = BestillingSystem.BUCKET,
    open val folderName: String,
    open val tilhørerEnheter: List<String> = emptyList(),
    open val språk: StøttetSpråk = StøttetSpråk.NB
) : DokumentMal {
    val filePath get() = "$folderName/$kode.pdf"
}

data class DokumentMalBrevserver(
    override val kode: String,
    override val tittel: String,
    override val beskrivelse: String = tittel,
    override val batchbrev: Boolean = false,
    override val enabled: Boolean = false,
    override val redigerbar: Boolean = !batchbrev,
    override var dokumentType: DokumentType = DokumentType.UTGÅENDE,
    override val innholdType: InnholdType = InnholdType.VARSEL,
    override val kreverDataGrunnlag: DokumentDataGrunnlag = DokumentDataGrunnlag(),
    override val bestillingSystem: BestillingSystemType = BestillingSystem.BREVSERVER,
    val støttetSpråk: List<StøttetSpråk> = listOf(StøttetSpråk.NB),
) : DokumentMal

private inline fun <reified T> lastDokumentMalerFraFil(filnavn: String): List<T> {
    return try {
        val objectMapper = ObjectMapper(YAMLFactory())
        objectMapper.findAndRegisterModules()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        val inputstream = ClassPathResource("files/dokumentmaler/$filnavn.json").inputStream
        val text = String(inputstream.readAllBytes(), StandardCharsets.UTF_8)
        val listType: JavaType =
            objectMapper.typeFactory.constructCollectionType(MutableList::class.java, T::class.java)
        objectMapper.readValue(
            text,
            listType
        )
    } catch (e: IOException) {
        throw RuntimeException("Kunne ikke laste fil", e)
    }
}

val dokumentmalerBrevserver: List<DokumentMalBrevserver> = lastDokumentMalerFraFil("brevserver")
val dokumentmalerBucket: List<DokumentMalBucketUtland> = lastDokumentMalerFraFil("vedlegg_utland")
val alleDokumentmaler = dokumentmalerBrevserver + dokumentmalerBucket

fun hentDokumentMal(kode: String): DokumentMal? = alleDokumentmaler.find { it.kode == kode }