package potfur.es

import dev.forkhandles.result4k.flatMap
import dev.forkhandles.result4k.strikt.isFailure
import dev.forkhandles.result4k.strikt.isSuccess
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.util.UUID
import kotlin.test.Test


abstract class PersistenceTestCase {

    abstract val persistence: Persistence<UUID, Event>

    @Test
    fun `returns events for given stream id`() {
        val result = persistence
            .store(UUID(1,1), listOf(Event(1), Event(2)))
            .flatMap { persistence.read(UUID(1,1)) }

        expectThat(result).isSuccess<List<Event>>()
            .and { get { value.size }.isEqualTo(2) }
    }

    @Test
    fun `returns failure when stream for given id does not exist`() {
        val result = persistence.read(UUID(0,0))

        expectThat(result).isFailure<StreamNotFound>()
    }

    @Test
    fun `returns revision number for stream`() {
        val result = persistence
            .store(UUID(2,2), listOf(Event(1), Event(2)))
            .flatMap { persistence.revision(UUID(2,2)) }

        expectThat(result).isSuccess<Int>()
            .and { get { value }.isEqualTo(2) }
    }

    @Test
    fun `returns revision equal to 0 for non existing stream`() {
        val result = persistence.revision(UUID(0,0))

        expectThat(result).isSuccess<Int>()
            .and { get { value }.isEqualTo(0) }
    }

}

