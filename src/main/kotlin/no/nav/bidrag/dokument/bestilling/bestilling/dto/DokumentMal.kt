package no.nav.bidrag.dokument.bestilling.bestilling.dto

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import org.springframework.core.io.ClassPathResource
import java.io.IOException
import java.nio.charset.StandardCharsets

object EnhetKode {
    const val FARSKAP = "4860"
    const val UTLAND = "4865"
    const val EGENANSATT = "4883"
}

object BestillingSystem {
    const val BREVSERVER = "BREVSERVER"
    const val BUCKET = "BUCKET"
}

enum class DokumentType {
    UTGÅENDE,
    NOTAT,
}

enum class InnholdType {
    VARSEL,
    VEDTAK,
    VEDLEGG,
    VEDLEGG_VEDTAK,
    VEDLEGG_VARSEL,
    SKJEMA,
}
typealias BestillingSystemType = String

data class DokumentDataGrunnlag(
    val vedtak: Boolean = false,
    val behandling: Boolean = false,
    val roller: Boolean = true,
    val enhetKontaktInfo: Boolean = true,
)

enum class StøttetSpråk {
    NB,
    EN,
    NN,
    DE,
    PL,
    FR,
}

abstract class DokumentMal(
    open val kode: String,
    open val beskrivelse: String,
    open val tittel: String,
    open val dokumentType: DokumentType,
    open val bestillingSystem: BestillingSystemType,
    open val batchbrev: Boolean,
    open val enabled: Boolean,
    open val kreverDataGrunnlag: DokumentDataGrunnlag?,
    open val innholdType: InnholdType,
    open val redigerbar: Boolean,
)

data class DokumentMalBucketUtland(
    override val folderName: String = "vedlegg_utland",
    override val kode: String,
    override val beskrivelse: String,
    override val språk: StøttetSpråk,
    override val tittel: String = beskrivelse,
    override val tilhørerEnheter: List<String> = listOf(EnhetKode.UTLAND, EnhetKode.EGENANSATT),
    override val innholdType: InnholdType = InnholdType.VARSEL,
    override val gruppeVisningsnavn: String,
) : DokumentMalBucket(
        kode = kode,
        beskrivelse = beskrivelse,
        folderName = folderName,
        tittel = tittel,
        tilhørerEnheter = tilhørerEnheter,
        språk = språk,
        innholdType = innholdType,
        gruppeVisningsnavn = gruppeVisningsnavn,
    )

data class DokumentMalBucketFarskap(
    override val folderName: String = "vedlegg_farskap",
    override val kode: String,
    override val tittel: String,
    override val beskrivelse: String = tittel,
    override val tilhørerEnheter: List<String> = listOf(EnhetKode.FARSKAP),
    override val innholdType: InnholdType = InnholdType.VARSEL,
    override val gruppeVisningsnavn: String,
) : DokumentMalBucket(
        kode = kode,
        beskrivelse = beskrivelse,
        folderName = folderName,
        tittel = tittel,
        tilhørerEnheter = tilhørerEnheter,
        innholdType = innholdType,
        gruppeVisningsnavn = gruppeVisningsnavn,
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
    open val filsti: String? = null,
    open val tilhørerEnheter: List<String> = emptyList(),
    open val språk: StøttetSpråk = StøttetSpråk.NB,
    open val gruppeVisningsnavn: String? = null,
) : DokumentMal(
        kode = kode,
        beskrivelse = beskrivelse,
        tittel = tittel,
        dokumentType = dokumentType,
        bestillingSystem = bestillingSystem,
        batchbrev = batchbrev,
        enabled = enabled,
        kreverDataGrunnlag = kreverDataGrunnlag,
        innholdType = innholdType,
        redigerbar = redigerbar,
    ) {
    private val bucketFilename get() = filsti?.substringBefore(".pdf") ?: kode
    val filePath get() = "$folderName/$bucketFilename.pdf"
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
) : DokumentMal(
        kode = kode,
        beskrivelse = beskrivelse,
        tittel = tittel,
        dokumentType = dokumentType,
        bestillingSystem = bestillingSystem,
        batchbrev = batchbrev,
        enabled = enabled,
        kreverDataGrunnlag = kreverDataGrunnlag,
        innholdType = innholdType,
        redigerbar = redigerbar,
    )

enum class FilType {
    JSON,
    YAML,
}

private inline fun <reified T : DokumentMal> lastDokumentMalerFraFil(
    filnavn: String,
    prefiks: String? = null,
    type: FilType = FilType.JSON,
    withGroupname: Boolean = false,
): List<T> {
    return try {
        val fileending = if (type == FilType.JSON) "json" else "yaml"
        val objectMapper = ObjectMapper(YAMLFactory())
        objectMapper.findAndRegisterModules()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        val inputstream = ClassPathResource("files/dokumentmaler/$filnavn.$fileending").inputStream
        val text = String(inputstream.readAllBytes(), StandardCharsets.UTF_8)
        val textConverted =
            if (withGroupname) {
                konverterGruppeNavnTilParameter(text, "gruppeVisningsnavn")
            } else {
                text
            }
        val textWithPrefiks =
            if (prefiks != null) {
                val json = objectMapper.readTree(textConverted)
                json.asSequence().forEach {
                    (it as ObjectNode).put("kode", "${prefiks}_${it.get("kode").asText()}")
                }
                json.toString()
            } else {
                textConverted
            }

        val listType: JavaType =
            objectMapper.typeFactory.constructCollectionType(MutableList::class.java, T::class.java)
        val dokumentmalerMap: List<T> =
            objectMapper.readValue(
                textWithPrefiks,
                listType,
            )

        dokumentmalerMap.map { it }
    } catch (e: IOException) {
        throw RuntimeException("Kunne ikke laste fil", e)
    }
}

private fun konverterGruppeNavnTilParameter(
    payload: String,
    parameterName: String,
): String {
    return try {
        val objectMapper = ObjectMapper(YAMLFactory())
        objectMapper.findAndRegisterModules()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true)
        val mapType: JavaType =
            objectMapper.typeFactory.constructMapType(
                Map::class.java,
                String::class.java,
                ArrayNode::class.java,
            )
        val payloadMap: Map<String, ArrayNode> =
            objectMapper.readValue(
                payload,
                mapType,
            )

        objectMapper.writeValueAsString(
            payloadMap.keys.flatMap { key ->
                payloadMap[key]?.map {
                    (it as ObjectNode).put(parameterName, key)
                } ?: emptyList()
            },
        )
    } catch (e: IOException) {
        throw RuntimeException("Kunne ikke laste fil", e)
    }
}

val dokumentmalerBrevserver: List<DokumentMalBrevserver> = lastDokumentMalerFraFil("brevserver")
val dokumentmalerUtland: List<DokumentMalBucketUtland> =
    lastDokumentMalerFraFil("vedlegg_utland", "UTLAND", type = FilType.YAML, true)
val dokumentmalerFarskap: List<DokumentMalBucketFarskap> =
    lastDokumentMalerFraFil("vedlegg_farskap", "FARSKAP", type = FilType.YAML, true)
val alleDokumentmaler = dokumentmalerBrevserver + dokumentmalerUtland + dokumentmalerFarskap

fun hentDokumentMal(kode: String): DokumentMal? = alleDokumentmaler.find { it.kode == kode }
