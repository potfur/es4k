package potfur.es.example.domain

import java.time.Instant
import java.util.UUID

interface DomainEvent {
    val id: UUID
    val timestamp: Instant
    val name: String get() = this.javaClass.simpleName
}
