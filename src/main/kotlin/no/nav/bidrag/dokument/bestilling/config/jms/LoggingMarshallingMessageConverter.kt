package no.nav.bidrag.dokument.bestilling.config.jms

import com.ibm.mq.constants.CMQC
import com.ibm.mq.jms.MQQueue
import com.ibm.msg.client.jms.JmsConstants
import com.ibm.msg.client.jms.JmsDestination
import com.ibm.msg.client.jms.JmsQueue
import no.nav.bidrag.dokument.bestilling.SECURE_LOGGER
import org.slf4j.LoggerFactory
import org.springframework.jms.support.converter.MarshallingMessageConverter
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
    companion object {
        private val LOGGER = LoggerFactory.getLogger(LoggingMarshallingMessageConverter::class.java)
    }


    override fun marshalToBytesMessage(o: Any, session: Session, marshaller: Marshaller): BytesMessage {
        val bos = ByteArrayOutputStream(1024)
        val streamResult = StreamResult(bos)
        marshaller.marshal(o, streamResult)
        val message = session.createBytesMessage()
        val barray = bos.toByteArray()
        SECURE_LOGGER.info("Sending message ${String(barray)}")
        message.writeBytes(barray)
        val rq = MQQueue("MRQ1", replyQueue)
        rq.targetClient = 1
        message.jmsReplyTo = rq
        message.jmsDestination = null
        message.setIntProperty(JmsConstants.JMS_IBM_ENCODING, CMQC.MQENC_S390)
        message.setStringProperty(JmsConstants.JMS_IBM_CHARACTER_SET, "IBM277")
        return message
    }
    override fun marshalToTextMessage(o: Any, session: Session, marshaller: Marshaller): TextMessage {
        val writer = StringWriter(1024)
        val result: Result = StreamResult(writer)
        marshaller.marshal(o, result)
        val messageString = writer.toString()
        SECURE_LOGGER.info("Sending message $messageString")
        return session.createTextMessage(messageString)
    }
}