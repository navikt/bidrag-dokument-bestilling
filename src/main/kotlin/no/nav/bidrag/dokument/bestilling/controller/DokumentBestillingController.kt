package no.nav.bidrag.dokument.bestilling.controller

import com.ibm.mq.constants.CMQC
import com.ibm.msg.client.jms.JmsConstants
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import no.nav.bidrag.dokument.bestilling.model.BrevKode
import no.nav.bidrag.dokument.bestilling.model.DokumentBestillingRequest
import no.nav.bidrag.dokument.bestilling.model.DokumentBestillingResponse
import no.nav.bidrag.dokument.bestilling.service.DokumentBestillingService
import no.nav.security.token.support.core.api.Protected
import no.nav.security.token.support.core.api.Unprotected
import no.nav.security.token.support.spring.SpringTokenValidationContextHolder
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.jms.core.JmsTemplate
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@RestController
@Protected
class DokumentBestillingController(
    var dokumentBestillingService: DokumentBestillingService,
    var onlinebrevTemplate: JmsTemplate,
    @Value("\${BREVSERVER_KVITTERING_QUEUE}") var replyQueueName: String
    ) {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(DokumentBestillingController::class.java)
    }

    @PostMapping("/bestill/{brevKode}")
    @Operation(
        description = "Bestiller dokument for oppgitt brevkode/dokumentKode",
        security = [SecurityRequirement(name = "bearer-key")],
    )
    @ApiResponses(value = [ApiResponse(responseCode = "400", description = "Dokument ble bestilt med ugyldig data")])
    fun bestillBrev(@RequestBody request: DokumentBestillingRequest, @PathVariable brevKode: BrevKode): DokumentBestillingResponse {
        return dokumentBestillingService.bestill(request, brevKode)
    }

    @RequestMapping("/brevkoder", method = [RequestMethod.OPTIONS])
    @Operation(
        description = "Henter brevkoder som er støttet av applikasjonen",
        security = [SecurityRequirement(name = "bearer-key")],
    )
    fun hentStottedeBrevkoder(): List<String> {
        LOGGER.info("Henter støttede brevkoder")
        return BrevKode.values().filter { it.enabled }.map { it.name }
    }

    @PostMapping("/bestill/raw")
    fun bestillRaw(@RequestBody request: String): ResponseEntity<Void> {
        onlinebrevTemplate.send {
            val message = it.createTextMessage()
            val rq = com.ibm.mq.jms.MQQueue(replyQueueName)
            message.jmsReplyTo = rq
            message.setIntProperty(JmsConstants.JMS_IBM_ENCODING, CMQC.MQENC_S390)
            message.setIntProperty(JmsConstants.JMS_IBM_CHARACTER_SET, 277)
            message.setIntProperty(JmsConstants.JMS_IBM_MSGTYPE, CMQC.MQMT_DATAGRAM)
            message.setIntProperty(JmsConstants.JMS_IBM_PUTAPPLTYPE, CMQC.MQAT_CICS)
            message.text = request
            message
        }
        return ResponseEntity.ok().build()
    }

}