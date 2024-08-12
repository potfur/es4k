package potfur.es

import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.flatMap
import dev.forkhandles.result4k.flatMapFailure


class InMemoryPersistence<ID, T>(vararg streams: Pair<ID, List<T>>) : Persistence<ID, T> {
    private val map = streams.toMap().toMutableMap()

    override fun read(id: ID) =
        map[id]?.let { Success(it) } ?: Failure(StreamNotFound("Stream not found"))

    override fun store(id: ID, revision: Int, events: List<T>): Result4k<Unit, Exception> =
        Success(Unit).also { map[id] = (map[id] ?: emptyList()) + events }

    override fun revision(id: ID) =
        read(id)
            .flatMap { Success(it.size) }
            .flatMapFailure { Success(0) }
}
