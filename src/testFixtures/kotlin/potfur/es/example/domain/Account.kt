package potfur.es.example.domain

import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.map
import potfur.es.EventStream
import potfur.es.example.domain.AccountEvent.Blocked
import potfur.es.example.domain.AccountEvent.Deposited
import potfur.es.example.domain.AccountEvent.Unblocked
import potfur.es.example.domain.AccountEvent.Withdrawn
import potfur.es.example.domain.MonetaryValue.Amount
import potfur.es.example.domain.MonetaryValue.Balance

open class AccountException(override val message: String?, override val cause: Throwable? = null) :
    Exception(message, cause)

class AccountClosed : AccountException("Account is closed")
class NotEnoughMoney : AccountException("Not enough money to withdraw")
class AlreadyBlocked : AccountException("Already blocked for given operation")
class NoBlockage : AccountException("No blockage for given operation")
class PendingBlockages : AccountException("There are pending blockages")

typealias AccountEventStream = EventStream<IBAN, AccountEvent>

class Account(val stream: AccountEventStream) {

    companion object {
        fun open(accountNumber: IBAN) =
            Success(Account(EventStream.create<IBAN, AccountEvent>(accountNumber) + AccountEvent.Opened()))
    }

    val balance = stream.fold(Balance(0) as MonetaryValue) { acc, e ->
        when (e) {
            is Deposited -> acc + e.amount
            is Withdrawn -> acc - e.amount
            else -> acc
        }
    }

    val blockages = stream.fold(setOf<Blocked>()) { acc, e ->
        when (e) {
            is Blocked -> acc + e
            is Unblocked -> acc - acc.single { it.operation == e.operation }
            else -> acc
        }
    }

    fun deposit(amount: Amount) = whenOpen {
        Success(stream + Deposited(amount))
    }

    fun withdraw(amount: Amount) = whenOpen {
        if (balance < amount) Failure(NotEnoughMoney())
        else Success(stream + Withdrawn(amount))
    }

    fun withdraw(operation: Operation) = whenOpen {
        blockages.singleOrNull { it.operation == operation }
            ?.let { Success(stream + Withdrawn(it.amount) + Unblocked(it.operation)) }
            ?: Failure(NoBlockage())
    }

    fun block(amount: Amount, operation: Operation) = whenOpen {
        if (balance < amount) Failure(NotEnoughMoney())
        else if (blockages.any { it.operation == operation }) Failure(AlreadyBlocked())
        else Success(stream + Blocked(amount, operation))
    }

    fun unblock(operation: Operation) = whenOpen {
        if (blockages.any { it.operation == operation }) Success(stream + Unblocked(operation))
        else Failure(NoBlockage())
    }

    fun close() = whenOpen {
        if (blockages.isNotEmpty()) Failure(PendingBlockages())
        else Success(stream + AccountEvent.Closed())
    }

    private fun whenOpen(fn: Account.() -> Result4k<AccountEventStream, Exception>) =
        stream.lastOrNull { it is AccountEvent.Closed }
            ?.let { Failure(AccountClosed()) }
            ?: fn(this).map { Account(it) }

}
