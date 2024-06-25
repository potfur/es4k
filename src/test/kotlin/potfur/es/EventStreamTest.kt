package potfur.es

import strikt.api.expectThat
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo
import strikt.assertions.isNotEmpty
import kotlin.test.Test


class EventStreamTest {

    @Test
    fun `created event stream is empty`() {
        val stream = EventStream.create<String, Event>("ID")

        expectThat(stream).isEmpty()
        expectThat(stream.commited).isEmpty()
        expectThat(stream.pending).isEmpty()
    }

    @Test
    fun `re-opened event stream contains commited events`() {
        val stream = EventStream.open("ID", listOf(Event(1), Event(2)))

        expectThat(stream).isNotEmpty()
        expectThat(stream.commited).isNotEmpty()
        expectThat(stream.pending).isEmpty()
    }

    @Test
    fun `adding events adds them to pending list`() {
        val stream = EventStream.create<String, Event>("ID")
            .add(Event(1), Event(2))

        expectThat(stream.commited).isEmpty()
        expectThat(stream.pending).isNotEmpty()
    }

    @Test
    fun `committing events moves pending events to commited`() {
        val stream = EventStream.create<String, Event>("ID")
            .add(Event(1), Event(2))
            .commit()

        expectThat(stream.commited).isNotEmpty()
        expectThat(stream.pending).isEmpty()
    }

    @Test
    fun `revision is equal to number of commited events`() {
        val stream = EventStream.create<String, Event>("ID")
            .add(Event(1), Event(2))
            .commit()

        expectThat(stream.revision).isEqualTo(stream.commited.size)
    }
}
