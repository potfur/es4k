package potfur.es

import dev.forkhandles.result4k.flatMap
import dev.forkhandles.result4k.strikt.isFailure
import dev.forkhandles.result4k.strikt.isSuccess
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import kotlin.test.Test

class EventStoreWithRevisionCheckTest {

    private val persistence = InMemoryPersistence<String, Event>()
    private val eventStore = EventStoreWithRevisionCheck(persistence)

    @Test
    fun `fetches stream from event store`() {
        val result = persistence
            .store("1", 0, listOf(Event(), Event()))
            .flatMap { eventStore.fetch("1") }

        expectThat(result).isSuccess<EventStream<String, Event>>()
            .and { get { value.revision }.isEqualTo(2) }
    }

    @Test
    fun `stores pending events in event store`() {
        val stream = EventStream.create<String, Event>("1")
            .add(Event(), Event())

        val result = eventStore
            .store(stream)
            .flatMap { persistence.read("1") }

        expectThat(result).isSuccess<List<Event>>()
            .and { get { value.size }.isEqualTo(2) }
    }

    @Test
    fun `storing stream commits all pending events`() {
        val stream = EventStream.create<String, Event>("1")
            .add(Event(), Event())

        val result = eventStore
            .store(stream)

        expectThat(result).isSuccess<EventStream<String, Event>>()
            .and { get { value.commited.size }.isEqualTo(2) }
            .and { get { value.pending.size }.isEqualTo(0) }
    }

    @Test
    fun `throws error on revision mismatch`() {
        val stream = EventStream.open("1", listOf(Event(), Event()))

        val result = eventStore
            .store(stream)

        expectThat(result).isFailure<RevisionMismatch>()
    }
}
