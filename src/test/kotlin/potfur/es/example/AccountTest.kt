package potfur.es.example

import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.flatMap
import potfur.es.example.domain.Account
import potfur.es.example.domain.AccountClosed
import potfur.es.example.domain.AccountEvent
import potfur.es.example.domain.IBAN
import potfur.es.example.domain.MonetaryValue.Amount
import potfur.es.example.domain.MonetaryValue.Balance
import potfur.es.example.domain.NotEnoughResources
import strikt.api.expectThat
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import strikt.assertions.isTrue
import kotlin.test.Test

class AccountTest {
    val iban = IBAN("IE12BOFI90000112345678")

    @Test
    fun `opened account has balance equal to 0`() {
        val result = Account.open(iban)

        expectThat(result).isA<Success<Account>>()
            .and { get { value.balance }.isEqualTo(Balance(0)) }
    }

    @Test
    fun `deposit adds amount to the balance`() {
        val result = Account.open(iban)
            .flatMap { it.deposit(Amount(1000)) }

        expectThat(result).isA<Success<Account>>()
            .and { get { value.stream.any { it is AccountEvent.Deposited } }.isTrue() }
            .and { get { value.balance }.isEqualTo(Balance(1000)) }
    }

    @Test
    fun `withdrawal requires sufficient amount on account`() {
        val result = Account.open(iban)
            .flatMap { it.withdraw(Amount(1000)) }

        expectThat(result).isA<Failure<NotEnoughResources>>()
    }

    @Test
    fun `withdrawal is possible there is enough money`() {
        val result = Account.open(iban)
            .flatMap { it.deposit(Amount(1000)) }
            .flatMap { it.withdraw(Amount(1000)) }

        expectThat(result).isA<Success<Account>>()
            .and { get { value.stream.any { it is AccountEvent.Withdrawn } }.isTrue() }
            .and { get { value.balance }.isEqualTo(Balance(0)) }
    }

    @Test
    fun `withdrawal is possible there is more than enough money`() {
        val result = Account.open(iban)
            .flatMap { it.deposit(Amount(2000)) }
            .flatMap { it.withdraw(Amount(1000)) }

        expectThat(result).isA<Success<Account>>()
            .and { get { value.stream.any { it is AccountEvent.Withdrawn } }.isTrue() }
            .and { get { value.balance }.isEqualTo(Balance(1000)) }
    }

    @Test
    fun `closing block deposit`() {
        val result = Account.open(iban)
            .flatMap { it.close() }
            .flatMap { it.deposit(Amount(1000)) }

        expectThat(result).isA<Failure<AccountClosed>>()
    }

    @Test
    fun `closing block withdrawal`() {
        val result = Account.open(iban)
            .flatMap { it.close() }
            .flatMap { it.withdraw(Amount(1000)) }

        expectThat(result).isA<Failure<AccountClosed>>()
    }
}
