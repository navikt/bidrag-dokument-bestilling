package no.nav.bidrag.dokument.bestilling.konfigurasjon

import com.ibm.mq.constants.CMQC
import com.ibm.mq.jms.MQQueueConnectionFactory
import com.ibm.msg.client.jms.JmsConstants
import com.ibm.msg.client.wmq.common.CommonConstants
import no.nav.bidrag.dokument.bestilling.konfigurasjon.jms.LoggingMarshallingMessageConverter
import no.nav.bidrag.dokument.bestilling.konfigurasjon.jms.MQProperties
import no.nav.bidrag.dokument.bestilling.bestilling.produksjon.dto.BrevBestilling
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.context.annotation.Scope
import org.springframework.jms.annotation.EnableJms
import org.springframework.jms.connection.UserCredentialsConnectionFactoryAdapter
import org.springframework.jms.core.JmsTemplate
import org.springframework.oxm.jaxb.Jaxb2Marshaller
import java.util.Locale
import javax.jms.ConnectionFactory
import javax.jms.JMSException
import javax.xml.bind.Marshaller

@Configuration
@EnableJms
@ConfigurationPropertiesScan
class JMSKonfigurasjon(private val mqProperties: MQProperties) {

    @Bean
    @Scope("prototype")
    fun baseJmsTemplate(mqQueueConnectionFactory: ConnectionFactory): JmsTemplate{
        val template = JmsTemplate()
        template.connectionFactory = mqQueueConnectionFactory
        return template
    }
    @Bean
    @Throws(JMSException::class)
    fun onlinebrevTemplate(baseJmsTemplate: JmsTemplate, @Value("\${BREVSERVER_ONLINEBREV_QUEUE}") queueName: String, @Value("\${BREVSERVER_KVITTERING_QUEUE}") replyQueueName: String): JmsTemplate {
        baseJmsTemplate.defaultDestinationName = queueName
        val jaxb2Marshaller = Jaxb2Marshaller()
        jaxb2Marshaller.setClassesToBeBound(BrevBestilling::class.java)
        jaxb2Marshaller.setMarshallerProperties(mapOf(Marshaller.JAXB_ENCODING to "ISO-8859-1"))
        baseJmsTemplate.messageConverter = LoggingMarshallingMessageConverter(jaxb2Marshaller, replyQueueName)
        return baseJmsTemplate
    }

    @Bean
    @Profile("nais")
    @Throws(JMSException::class)
    fun mqQueueConnectionFactory(@Value("\${BREVSERVER_KVITTERING_QUEUE}") replyQueueName: String): ConnectionFactory {
        val connectionFactory = MQQueueConnectionFactory()
        connectionFactory.hostName = mqProperties.hostname
        connectionFactory.port = mqProperties.port
        connectionFactory.setBooleanProperty(JmsConstants.USER_AUTHENTICATION_MQCSP, true)
        connectionFactory.queueManager = mqProperties.name
        connectionFactory.channel = mqProperties.channel.uppercase(Locale.getDefault())
        connectionFactory.transportType = CommonConstants.WMQ_CM_CLIENT
        connectionFactory.setIntProperty(JmsConstants.JMS_IBM_ENCODING, CMQC.MQENC_S390)
        val credentialQueueConnectionFactory = UserCredentialsConnectionFactoryAdapter()
        credentialQueueConnectionFactory.setUsername(mqProperties.username)
        credentialQueueConnectionFactory.setPassword(mqProperties.password)
        credentialQueueConnectionFactory.setTargetConnectionFactory(connectionFactory)
        return credentialQueueConnectionFactory
    }
}