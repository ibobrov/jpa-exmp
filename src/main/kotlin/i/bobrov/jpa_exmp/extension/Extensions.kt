package i.bobrov.jpa_exmp.extension

import java.util.function.Consumer
import java.util.function.Function

fun <K, V, R> Map<K, V>.stubMissedKeys(
    keys: Set<K>,
    stubFunction: Function<K, R>,
    logConsumer: Consumer<Set<K>>
): List<R> {
    val notFoundIds = (keys - this.keys)
    var stubValues = listOf<R>()

    if (notFoundIds.isNotEmpty()) {
        logConsumer.accept(notFoundIds)
        stubValues = notFoundIds.map { stubFunction.apply(it) }
    }

    return stubValues
}