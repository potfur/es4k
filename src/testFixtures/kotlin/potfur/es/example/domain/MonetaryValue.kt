package potfur.es.example.domain

import potfur.es.example.domain.MonetaryValue.Balance

sealed interface MonetaryValue {
    val value: Int

    @JvmInline
    value class Balance(override val value: Int) : MonetaryValue

    @JvmInline
    value class Amount(override val value: Int) : MonetaryValue {
        init {
            require(value > 0)
        }
    }

}

operator fun MonetaryValue.plus(other: MonetaryValue): Balance = Balance(this.value + other.value)

operator fun MonetaryValue.minus(other: MonetaryValue): Balance = Balance(this.value - other.value)

operator fun MonetaryValue.compareTo(other: MonetaryValue): Int = this.value.compareTo(other.value)
