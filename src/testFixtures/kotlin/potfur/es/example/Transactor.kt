package potfur.es.example

import dev.forkhandles.result4k.Result4k

interface Transactor {
    fun <T, E> primary(fn: () -> Result4k<T, E>): Result4k<T, E>
}

class InMemoryTransactor : Transactor {
    override fun <T, E> primary(fn: () -> Result4k<T, E>) = fn()
}
