package potfur.es

import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.flatMap
import dev.forkhandles.result4k.flatMapFailure


class InMemoryPersistence<ID, T>(vararg streams: Pair<ID, List<T>>) : Persistence<ID, T> {
    private val map = streams.toMap().toMutableMap()

    override fun read(id: ID) =
        map[id]?.let { Success(it) } ?: Failure(StreamNotFound("Stream not found"))

    override fun store(id: ID, events: List<T>) =
        Success(Unit).also { map[id] = events }

    override fun revision(id: ID) =
        read(id)
            .flatMap { Success(it.size) }
            .flatMapFailure { Success(0) }
}
