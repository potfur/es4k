package potfur.es.example.domain

import potfur.es.example.domain.MonetaryValue.Amount
import java.time.Instant
import java.util.UUID

sealed interface AccountEvent : DomainEvent {
    data class Opened(
        override val id: UUID = UUID.randomUUID(),
        override val timestamp: Instant = Instant.now(),
    ) : AccountEvent

    data class Closed(
        override val id: UUID = UUID.randomUUID(),
        override val timestamp: Instant = Instant.now(),
    ) : AccountEvent

    data class Deposited(
        val amount: Amount,
        override val id: UUID = UUID.randomUUID(),
        override val timestamp: Instant = Instant.now(),
    ) : AccountEvent

    data class Withdrawn(
        val amount: Amount,
        override val id: UUID = UUID.randomUUID(),
        override val timestamp: Instant = Instant.now(),
    ) : AccountEvent

    data class Blocked(
        val amount: Amount,
        val operation: Operation,
        override val id: UUID = UUID.randomUUID(),
        override val timestamp: Instant = Instant.now(),
    ) : AccountEvent

    data class Unblocked(
        val operation: Operation,
        override val id: UUID = UUID.randomUUID(),
        override val timestamp: Instant = Instant.now(),
    ) : AccountEvent
}
