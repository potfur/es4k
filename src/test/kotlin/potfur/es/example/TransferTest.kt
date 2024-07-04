package potfur.es.example

import dev.forkhandles.result4k.flatMap
import dev.forkhandles.result4k.orThrow
import potfur.es.example.domain.Account
import potfur.es.example.domain.AccountEventStream
import potfur.es.example.domain.IBAN
import potfur.es.example.domain.MonetaryValue.Amount
import potfur.es.example.domain.MonetaryValue.Balance
import potfur.es.example.domain.Operation
import potfur.es.example.domain.Transfer
import potfur.es.isSuccessOf
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import kotlin.test.Test


class TransferTest {

    private val source = Account.open(IBAN("IE12BOFI90000112345678")).flatMap { it.deposit(Amount(1000)) }.orThrow()
    private val target = Account.open(IBAN("ES9121000418450200051332")).orThrow()
    private val amount = Amount(500)
    private val operation = Operation("Because")

    @Test
    fun `transfers money between accounts`() {
        val result = Transfer(source, target, amount, operation)

        expectThat(result).isSuccessOf<Pair<Account, Account>>()
            .and { get { value.first.balance }.isEqualTo(Balance(500)) }
            .and { get { value.second.balance }.isEqualTo(Balance(500)) }
    }

    @Test
    fun `transfer blocks money before withdraw`() {
        val result = Transfer(source, target, amount, operation)

        expectThat(result).isSuccessOf<Pair<Account, Account>>()
            .and {
                get { value.first.stream.names }.isEqualTo(listOf("Opened", "Deposited", "Blocked", "Withdrawn", "Unblocked"))
            }
            .and {
                get { value.second.stream.names }.isEqualTo(listOf("Opened", "Deposited"))
            }
    }

    private val AccountEventStream.names get() = all.map { it.name }
}
