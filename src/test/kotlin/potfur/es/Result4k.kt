package potfur.es

import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success

import strikt.api.Assertion
import strikt.assertions.isA

inline fun <reified E> Assertion.Builder<*>.isSuccessOf() =
    isA<Success<E>>().and { get { value }.isA<E>() }

inline fun <reified E> Assertion.Builder<*>.isFailureOf() =
    isA<Failure<E>>().and { get { reason }.isA<E>() }
