package potfur.es

import dev.forkhandles.result4k.flatMap
import dev.forkhandles.result4k.strikt.isFailure
import dev.forkhandles.result4k.strikt.isSuccess
import potfur.es.example.Transactor
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.util.UUID
import kotlin.test.Test


abstract class PersistenceTestCase {
    abstract val streamId: UUID
    abstract val transactor: Transactor
    abstract val persistence: Persistence<UUID, Event>

    @Test
    fun `returns events for given stream id`() {
        val result = transactor.primary {
            persistence
                .store(streamId, 0, listOf(Event(), Event()))
                .flatMap { persistence.read(streamId) }
        }

        expectThat(result).isSuccess<List<Event>>()
            .and { get { value.size }.isEqualTo(2) }
    }

    @Test
    fun `returns failure when stream for given id does not exist`() {
        val result = transactor.primary {
            persistence.read(streamId)
        }

        expectThat(result).isFailure<StreamNotFound>()
    }

    @Test
    fun `returns revision number for stream`() {
        val result = transactor.primary {
            persistence
                .store(streamId, 0, listOf(Event(), Event()))
                .flatMap { persistence.revision(streamId) }
        }

        expectThat(result).isSuccess<Int>()
            .and { get { value }.isEqualTo(2) }
    }

    @Test
    fun `returns revision equal to 0 for non existing stream`() {
        val result = transactor.primary {
            persistence.revision(streamId)
        }

        expectThat(result).isSuccess<Int>()
            .and { get { value }.isEqualTo(0) }
    }

}

