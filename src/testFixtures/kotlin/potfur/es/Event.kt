package potfur.es

import java.time.Instant
import java.util.UUID

data class Event(
    val id: UUID = UUID.randomUUID(),
    val timestamp: Instant = Instant.now(),
)
