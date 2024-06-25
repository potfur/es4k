package potfur.es

import dev.forkhandles.result4k.Result4k


interface EventStore<ID, T> {
    fun fetch(id: ID): Result4k<EventStream<ID, T>, Exception>
    fun store(stream: EventStream<ID, T>): Result4k<EventStream<ID, T>, Exception>
}
