package potfur.es.example

import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.flatMap
import potfur.es.example.domain.Account
import potfur.es.example.domain.AccountClosed
import potfur.es.example.domain.AccountEvent
import potfur.es.example.domain.AlreadyBlocked
import potfur.es.example.domain.IBAN
import potfur.es.example.domain.MonetaryValue.Amount
import potfur.es.example.domain.MonetaryValue.Balance
import potfur.es.example.domain.NoBlockage
import potfur.es.example.domain.NotEnoughMoney
import potfur.es.example.domain.Operation
import potfur.es.example.domain.PendingBlockages
import strikt.api.Assertion
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.isA
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo
import strikt.assertions.isTrue
import kotlin.test.Test

class AccountTest {
    val iban = IBAN("IE12BOFI90000112345678")

    @Test
    fun `opened account has balance equal to 0`() {
        val result = Account.open(iban)

        expectThat(result).isSuccessOf<Account>()
            .and { get { value.balance }.isEqualTo(Balance(0)) }
    }

    @Test
    fun `deposit adds amount to the balance`() {
        val result = Account.open(iban)
            .flatMap { it.deposit(Amount(1000)) }

        expectThat(result).isSuccessOf<Account>()
            .and { get { value.stream.any { it is AccountEvent.Deposited } }.isTrue() }
            .and { get { value.balance }.isEqualTo(Balance(1000)) }
    }

    @Test
    fun `withdrawal requires sufficient amount on account`() {
        val result = Account.open(iban)
            .flatMap { it.withdraw(Amount(1000)) }

        expectThat(result).isFailureOf<NotEnoughMoney>()
    }

    @Test
    fun `withdrawal is possible when there is enough money`() {
        val result = Account.open(iban)
            .flatMap { it.deposit(Amount(1000)) }
            .flatMap { it.withdraw(Amount(1000)) }

        expectThat(result).isSuccessOf<Account>()
            .and { get { value.stream.any { it is AccountEvent.Withdrawn } }.isTrue() }
            .and { get { value.balance }.isEqualTo(Balance(0)) }
    }

    @Test
    fun `withdrawal is possible when there is more than enough money`() {
        val result = Account.open(iban)
            .flatMap { it.deposit(Amount(2000)) }
            .flatMap { it.withdraw(Amount(1000)) }

        expectThat(result).isSuccessOf<Account>()
            .and { get { value.stream.any { it is AccountEvent.Withdrawn } }.isTrue() }
            .and { get { value.balance }.isEqualTo(Balance(1000)) }
    }

    @Test
    fun `money can not be blocked when account closed`() {
        val result = Account.open(iban)
            .flatMap { it.deposit(Amount(1000)) }
            .flatMap { it.close() }
            .flatMap { it.block(Amount(500), Operation("because")) }

        expectThat(result).isFailureOf<AccountClosed>()
    }

    @Test
    fun `money can not be blocked for operation when there is not enough of them`() {
        val result = Account.open(iban)
            .flatMap { it.block(Amount(500), Operation("because")) }

        expectThat(result).isFailureOf<NotEnoughMoney>()
    }

    @Test
    fun `money can be blocked for operation when there is enough of them`() {
        val result = Account.open(iban)
            .flatMap { it.deposit(Amount(500)) }
            .flatMap { it.block(Amount(500), Operation("because")) }

        expectThat(result).isSuccessOf<Account>()
            .and { get { value.stream.any { it is AccountEvent.Blocked } }.isTrue() }
            .and { get { value.blockages.map { it.operation } }.contains(Operation("because")) }
    }

    @Test
    fun `money can be blocked for operation when there more than enough of them`() {
        val result = Account.open(iban)
            .flatMap { it.deposit(Amount(1000)) }
            .flatMap { it.block(Amount(500), Operation("because")) }

        expectThat(result).isSuccessOf<Account>()
            .and { get { value.stream.any { it is AccountEvent.Blocked } }.isTrue() }
            .and { get { value.blockages.map { it.operation } }.contains(Operation("because")) }
    }

    @Test
    fun `money can not be blocked for already blocking operation`() {
        val result = Account.open(iban)
            .flatMap { it.deposit(Amount(500)) }
            .flatMap { it.block(Amount(500), Operation("because")) }
            .flatMap { it.block(Amount(500), Operation("because")) }

        expectThat(result).isFailureOf<AlreadyBlocked>()
    }

    @Test
    fun `money can not be unblocked when there was no blockage`() {
        val result = Account.open(iban)
            .flatMap { it.unblock(Operation("because")) }

        expectThat(result).isFailureOf<NoBlockage>()
    }

    @Test
    fun `money can be unblocked`() {
        val result = Account.open(iban)
            .flatMap { it.deposit(Amount(1000)) }
            .flatMap { it.block(Amount(1000), Operation("because")) }
            .flatMap { it.unblock(Operation("because")) }

        expectThat(result).isSuccessOf<Account>()
            .and { get { value.stream.any { it is AccountEvent.Blocked } }.isTrue() }
            .and { get { value.blockages }.isEmpty() }
    }

    @Test
    fun `money can not be unblocked when already unblocked`() {
        val result = Account.open(iban)
            .flatMap { it.unblock(Operation("because")) }

        expectThat(result).isFailureOf<NoBlockage>()
    }

    @Test
    fun `blocked money can be withdrawn`() {
        val result = Account.open(iban)
            .flatMap { it.deposit(Amount(1000)) }
            .flatMap { it.block(Amount(1000), Operation("because")) }
            .flatMap { it.withdraw(Operation("because")) }

        expectThat(result).isSuccessOf<Account>()
            .and { get { value.stream.any { it is AccountEvent.Blocked } }.isTrue() }
            .and { get { value.balance }.isEqualTo(Balance(0)) }
            .and { get { value.blockages }.isEmpty() }
    }

    @Test
    fun `withdrawing blocked money removes blockage`() {
        val result = Account.open(iban)
            .flatMap { it.deposit(Amount(1000)) }
            .flatMap { it.withdraw(Operation("because")) }

        expectThat(result).isFailureOf<NoBlockage>()
    }

    @Test
    fun `closing is not possible when there is blockage`() {
        val result = Account.open(iban)
            .flatMap { it.deposit(Amount(500)) }
            .flatMap { it.block(Amount(500), Operation("because")) }
            .flatMap { it.close() }

        expectThat(result).isFailureOf<PendingBlockages>()
    }

    @Test
    fun `closing blocks deposit`() {
        val result = Account.open(iban)
            .flatMap { it.close() }
            .flatMap { it.deposit(Amount(1000)) }

        expectThat(result).isFailureOf<AccountClosed>()
    }

    @Test
    fun `closing blocks withdrawal`() {
        val result = Account.open(iban)
            .flatMap { it.close() }
            .flatMap { it.withdraw(Amount(1000)) }

        expectThat(result).isFailureOf<AccountClosed>()
    }
}

private inline fun <reified E> Assertion.Builder<*>.isSuccessOf() =
    isA<Success<E>>().and { get { value }.isA<E>() }

private inline fun <reified E> Assertion.Builder<*>.isFailureOf() =
    isA<Failure<E>>().and { get { reason }.isA<E>() }
