package potfur.es.example.projections

import potfur.es.example.domain.AccountEvent.Deposited
import potfur.es.example.domain.AccountEvent.Withdrawn
import potfur.es.example.domain.AccountEventStream
import potfur.es.example.domain.IBAN
import potfur.es.example.domain.MonetaryValue.Balance
import potfur.es.example.domain.minus
import potfur.es.example.domain.plus
import java.time.Instant

data class LogEntry(val name: String, val timestamp: Instant)

data class Account(val iban: IBAN, val balance: Balance, val logEntry: List<LogEntry>) {
    companion object {
        fun fromStream(stream: AccountEventStream): Account = stream
            .fold(Balance(0) to listOf<LogEntry>()) { (balance, logs), it ->
                when (it) {
                    is Deposited -> balance + it.amount
                    is Withdrawn -> balance - it.amount
                    else -> balance
                } to (logs + LogEntry(it.name, it.timestamp))
            }
            .let { (balance, logs) -> Account(stream.id, balance, logs) }
    }
}
