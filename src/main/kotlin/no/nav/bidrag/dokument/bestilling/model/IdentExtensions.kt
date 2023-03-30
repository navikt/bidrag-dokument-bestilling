package no.nav.bidrag.dokument.bestilling.model

typealias Ident = String
val Ident.erDødfødt get() = this.matches("^[0-9]{6}000[0-1][0-9]".toRegex())
val Ident.erSamhandler get() = this.matches("^[8-9][0-9]{10}$".toRegex())
