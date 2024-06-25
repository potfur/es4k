package potfur.es

import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.flatMap
import dev.forkhandles.result4k.map

interface Persistence<ID, T> {
    fun read(id: ID): Result4k<List<T>, Exception>
    fun store(id: ID, events: List<T>): Result4k<Unit, Exception>
    fun revision(id: ID): Result4k<Int, Exception>
}

class RevisionMismatch(id: String, expected: Number, actual: Number) :
    Exception("Revision mismatch for stream $id, expected $expected, got $actual")

class EventStoreWithRevisionCheck<ID, T>(
    private val persistence: Persistence<ID, T>,
) : EventStore<ID, T> {
    override fun fetch(id: ID) =
        persistence.read(id).map { EventStream.open(id, it) }

    override fun store(stream: EventStream<ID, T>) =
        persistence.revision(stream.id)
            .flatMap {
                if (it == stream.revision) Success(stream)
                else Failure(RevisionMismatch(stream.id.toString(), it, stream.revision))
            }
            .flatMap { persistence.store(stream.id, stream.pending).map { stream.commit() } }
            .map { EventStream(stream.id, stream.all, emptyList()) }
}
