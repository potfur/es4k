package potfur.es.example

import potfur.es.EventStream
import potfur.es.example.domain.AccountEvent
import potfur.es.example.domain.IBAN
import potfur.es.example.domain.MonetaryValue.Amount
import potfur.es.example.domain.MonetaryValue.Balance
import potfur.es.example.projections.Account
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import kotlin.test.Test

class AccountProjectionTest {
    private val iban = IBAN("IE12BOFI90000112345678")
    private val stream = EventStream(
        iban,
        listOf(
            AccountEvent.Deposited(Amount(1000)),
            AccountEvent.Withdrawn(Amount(500)),
            AccountEvent.Deposited(Amount(1000)),
        ),
        emptyList()
    )
    private val projection = Account.fromStream(stream)

    @Test
    fun `is identified by IBAN`() {
        expectThat(projection.iban).isEqualTo(iban)
    }

    @Test
    fun `balance is calculated from operations`() {
        expectThat(projection.balance).isEqualTo(Balance(1500))
    }
}
