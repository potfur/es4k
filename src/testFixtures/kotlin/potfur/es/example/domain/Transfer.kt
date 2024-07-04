package potfur.es.example.domain

import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.flatMap
import dev.forkhandles.result4k.map
import potfur.es.example.domain.MonetaryValue.Amount

fun Transfer(source: Account, target: Account, amount: Amount, operation: Operation) =
    Success(source to target)
        .flatMap { (source, target) -> source.block(amount, operation).map { it to target } }
        .flatMap { (source, target) -> target.deposit(amount).map { source to it } }
        .flatMap { (source, target) -> source.withdraw(operation).map { it to target } }
