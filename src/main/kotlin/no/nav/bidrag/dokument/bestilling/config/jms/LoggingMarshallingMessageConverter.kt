package no.nav.bidrag.dokument.bestilling.config.jms

import com.ibm.mq.constants.CMQC
import com.ibm.mq.headers.CCSID
import com.ibm.mq.jms.MQQueue
import com.ibm.msg.client.jms.JmsConstants
import com.ibm.msg.client.jms.JmsDestination
import com.ibm.msg.client.jms.JmsQueue
import com.ibm.msg.client.wmq.WMQConstants
import no.nav.bidrag.dokument.bestilling.SECURE_LOGGER
import org.slf4j.LoggerFactory
import org.springframework.jms.support.converter.MarshallingMessageConverter
import org.springframework.jms.support.converter.MessageType
import org.springframework.oxm.Marshaller
import org.springframework.oxm.jaxb.Jaxb2Marshaller
import java.io.ByteArrayOutputStream
import java.io.StringWriter
import javax.jms.BytesMessage
import javax.jms.Destination
import javax.jms.Session
import javax.jms.TextMessage
import javax.xml.transform.Result
import javax.xml.transform.stream.StreamResult

class LoggingMarshallingMessageConverter(jaxb2Marshaller: Jaxb2Marshaller, var replyQueue: String) : MarshallingMessageConverter(jaxb2Marshaller) {

    init {
        setTargetType(MessageType.TEXT)
    }

    override fun marshalToTextMessage(o: Any, session: Session, marshaller: Marshaller): TextMessage {
        val rq = MQQueue(replyQueue)
        val writer = StringWriter(1024)
        val result: Result = StreamResult(writer)
        marshaller.marshal(o, result)
        val messageString = writer.toString()
        val cleanedMessageString = messageString
            .replace("xsi:nil=\"true\"", "")
            .replace("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"", "")
            .replace("\\s{2,}".toRegex(), "")
        SECURE_LOGGER.info("Sending message $cleanedMessageString")
        val message =  session.createTextMessage(cleanedMessageString)
        message.setIntProperty(JmsConstants.JMS_IBM_CHARACTER_SET, 277)
        message.setIntProperty(JmsConstants.JMS_IBM_MSGTYPE, CMQC.MQMT_DATAGRAM)
        message.setIntProperty(JmsConstants.JMS_IBM_PUTAPPLTYPE, CMQC.MQAT_CICS)
        message.jmsReplyTo = rq
        return message
    }
}