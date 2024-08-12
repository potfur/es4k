package potfur.es

import strikt.api.expectThat
import strikt.api.expectThrows
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
        val stream = EventStream.open("ID", listOf(Event(), Event()))

        expectThat(stream).isNotEmpty()
        expectThat(stream.commited).isNotEmpty()
        expectThat(stream.pending).isEmpty()
    }

    @Test
    fun `adding events adds them to pending list`() {
        val stream = EventStream.create<String, Event>("ID")
            .add(Event(), Event())

        expectThat(stream.commited).isEmpty()
        expectThat(stream.pending).isNotEmpty()
    }

    @Test
    fun `committing events moves pending events to commited`() {
        val stream = EventStream.create<String, Event>("ID")
            .add(Event(), Event())
            .commit()

        expectThat(stream.commited).isNotEmpty()
        expectThat(stream.pending).isEmpty()
    }

    @Test
    fun `revision is equal to number of commited events`() {
        val stream = EventStream.create<String, Event>("ID")
            .add(Event(), Event())
            .commit()

        expectThat(stream.revision).isEqualTo(stream.commited.size)
    }

    @Test
    fun `adding single event add it to pending list, just like stream would be a list`() {
        val stream = EventStream.create<String, Event>("ID") + Event()

        expectThat(stream.commited).isEmpty()
        expectThat(stream.pending).isNotEmpty()
    }

    @Test
    fun `adding list of events adds them to pending list, just like stream would be a list`() {
        val stream = EventStream.create<String, Event>("ID") + listOf(Event(), Event())

        expectThat(stream.commited).isEmpty()
        expectThat(stream.pending).isNotEmpty()
    }

    @Test
    fun `removing single event is forbidden`() {
        expectThrows<NotImplementedError> {
            EventStream.create<String, Event>("ID") - Event()
        }
    }

    @Test
    fun `removing list of events is forbidden`() {
        expectThrows<NotImplementedError> {
            EventStream.create<String, Event>("ID") - listOf(Event())
        }
    }
}
