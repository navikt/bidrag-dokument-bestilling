package no.nav.bidrag.dokument.bestilling.hendelse

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import mu.KotlinLogging
import no.nav.bidrag.dokument.bestilling.SIKKER_LOGG
import no.nav.bidrag.dokument.bestilling.model.SendingAvHendelseFeilet
import no.nav.bidrag.dokument.dto.DokumentHendelse
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component

private val LOGGER = KotlinLogging.logger {}

@Component
class DokumentHendelseProduser(
    @Value("\${TOPIC_DOKUMENT}") val topic: String,
    val kafkaTemplate: KafkaTemplate<String, String>,
    val objectMapper: ObjectMapper) {

    @Retryable(value = [Exception::class], maxAttempts = 10, backoff = Backoff(delay = 1000, maxDelay = 12000, multiplier = 2.0))
    fun sendHendelse(hendelse: DokumentHendelse){
        try {
            val message = objectMapper.writeValueAsString(hendelse)
            SIKKER_LOGG.info("Publiserer hendelse {}", message)
            LOGGER.info("Publiserer hendelse med forsendelseId=${hendelse.forsendelseId} og dokumentreferanse ${hendelse.dokumentreferanse}")
            kafkaTemplate.send(topic, hendelse.dokumentreferanse, message)
        } catch (e: JsonProcessingException) {
            throw SendingAvHendelseFeilet(e.message!!, e)
        }

    }
}