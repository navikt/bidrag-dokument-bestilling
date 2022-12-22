package no.nav.bidrag.dokument.bestilling.utils

import no.nav.bidrag.dokument.bestilling.bestilling.produksjon.dto.BrevBestilling
import org.apache.activemq.command.ActiveMQBytesMessage
import org.apache.activemq.command.ActiveMQTextMessage
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.jms.core.JmsTemplate
import org.springframework.oxm.jaxb.Jaxb2Marshaller
import org.springframework.stereotype.Component
import javax.jms.Connection
import javax.jms.MessageConsumer
import javax.jms.Queue
import javax.jms.Session
import javax.xml.bind.JAXB

@Component
@Profile("!nais")
class JmsTestConsumer {
    @Autowired
    private lateinit var onlineBrevQueue: Queue

    @Autowired
    private lateinit var onlinebrevTemplate: JmsTemplate

    fun withOnlinebrev(func: JmsConnection.()->Unit){
        val conn = JmsConnection(onlinebrevTemplate, onlineBrevQueue)
        try {
            func.invoke(conn)
        } finally {
            conn.close()
        }

    }

    class JmsConnection(jmsTemplate: JmsTemplate, queue: Queue) {
        var connection: Connection? = null
        var session: Session? = null
        var consumer: MessageConsumer? = null
        init {
            connection = jmsTemplate.connectionFactory?.createConnection()
            connection?.start()
            session = connection?.createSession(false, Session.AUTO_ACKNOWLEDGE)
            consumer = session?.createConsumer(queue)
        }

        fun close(){
            consumer?.close()
            session?.close()
            connection?.close()
        }

        fun getMessageAsString(): String {
            val message: ActiveMQTextMessage = consumer?.receive(1000) as ActiveMQTextMessage
//            val byteArr = ByteArray(message.bodyLength.toInt())
//            message.readBytes(byteArr)
//            return String(byteArr)
            return message.text
        }

        fun <T> getMessageAsObject(o: Class<T>): T? {
            val message: ActiveMQTextMessage = consumer?.receive(1000) as ActiveMQTextMessage
            return JAXB.unmarshal(message.text.byteInputStream(), o)
        }

        fun hasNoMessage(): Boolean {
            return consumer?.receive(1000) == null

        }
    }

}