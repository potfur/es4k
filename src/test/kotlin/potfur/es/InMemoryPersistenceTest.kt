package potfur.es

import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.flatMap
import strikt.api.expectThat
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import kotlin.test.Test


class InMemoryPersistenceTest {

    private val persistence = InMemoryPersistence<String, Event>()

    @Test
    fun `returns events for given stream id`() {
        val result = persistence
            .store("1", listOf(Event(1), Event(2)))
            .flatMap { persistence.read("1") }

        expectThat(result).isA<Success<List<Event>>>()
            .and { get { value.size }.isEqualTo(2) }
    }

    @Test
    fun `returns failure when stream for given id does not exist`() {
        val result = persistence.read("0")

        expectThat(result).isA<Failure<StreamNotFound>>()
    }

    @Test
    fun `returns revision number for stream`() {
        val result = persistence
            .store("2", listOf(Event(1), Event(2)))
            .flatMap { persistence.revision("2") }

        expectThat(result).isA<Success<Int>>()
            .and { get { value }.isEqualTo(2) }
    }

    @Test
    fun `returns revision equal to 0 for non existing stream`() {
        val result = persistence.revision("0")

        expectThat(result).isA<Success<Int>>()
            .and { get { value }.isEqualTo(0) }
    }

}

