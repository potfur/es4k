package potfur.es.example.domain

import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.map
import potfur.es.EventStream
import potfur.es.example.domain.MonetaryValue.Amount
import potfur.es.example.domain.MonetaryValue.Balance

open class AccountException(override val message: String?, override val cause: Throwable? = null) :
    Exception(message, cause)

class AccountClosed : AccountException("Account is closed")
class NotEnoughResources : AccountException("Not enough resources to withdraw")

typealias AccountEventStream = EventStream<IBAN, AccountEvent>

class Account(val stream: AccountEventStream) {

    companion object {
        fun open(accountNumber: IBAN) =
            Success(Account(EventStream.create<IBAN, AccountEvent>(accountNumber) + AccountEvent.Opened()))
    }

    val balance = stream.fold(Balance(0) as MonetaryValue) { acc, e ->
        when (e) {
            is AccountEvent.Deposited -> acc + e.amount
            is AccountEvent.Withdrawn -> acc - e.amount
            else -> acc
        }
    }

    fun deposit(amount: Amount) = whenOpen {
        Success(it + AccountEvent.Deposited(amount))
    }

    fun withdraw(amount: Amount) = whenOpen {
        if (balance >= amount) Success(it + AccountEvent.Withdrawn(amount))
        else Failure(NotEnoughResources())
    }

    fun close() = whenOpen {
        Success(it + AccountEvent.Closed())
    }

    private fun whenOpen(fn: Account.(AccountEventStream) -> Result4k<AccountEventStream, Exception>) =
        if (stream.lastOrNull { it is AccountEvent.Closed } != null) Failure(AccountClosed())
        else fn(this, stream).map { Account(it) }
}
