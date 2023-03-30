package no.nav.bidrag.dokument.bestilling.konfigurasjon.jms

import org.slf4j.LoggerFactory
import javax.jms.ExceptionListener
import javax.jms.JMSException

class JmsExceptionListener : ExceptionListener {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(JmsExceptionListener::class.java)
    }

    override fun onException(e: JMSException) {
        LOGGER.error("Det skjedde en feil ved parsing av JMS melding", e)
    }
}
