package potfur.es.example.domain

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
        val amount: MonetaryValue.Amount,
        override val id: UUID = UUID.randomUUID(),
        override val timestamp: Instant = Instant.now(),
    ) : AccountEvent

    data class Withdrawn(
        val amount: MonetaryValue.Amount,
        override val id: UUID = UUID.randomUUID(),
        override val timestamp: Instant = Instant.now(),
    ) : AccountEvent
}
