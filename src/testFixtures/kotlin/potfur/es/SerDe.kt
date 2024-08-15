package potfur.es

import com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES
import com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES
import com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlin.reflect.KClass

class SimpleClassNameSerDe<T : Any>(private val type: KClass<T>) : SerDe<T> {
    companion object {
        inline operator fun <reified T : Any> invoke() = SimpleClassNameSerDe(T::class)
    }

    private val json: ObjectMapper = jacksonObjectMapper()
        .registerModules(
            KotlinModule.Builder().build(),
            JavaTimeModule()
        )
        .deactivateDefaultTyping()
        .configure(FAIL_ON_NULL_FOR_PRIMITIVES, true)
        .configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
        .configure(FAIL_ON_IGNORED_PROPERTIES, false)

    private val subclasses = type.collectSubclasses()

    override fun serialize(value: T): String = json.writeValueAsString(value)

    override fun deserialize(name: String, value: String): T =
        subclasses.single { it.simpleName == name }
            .let { json.readValue(value, type.java) }

    private fun <T : Any> KClass<T>.collectSubclasses(): List<KClass<out T>> =
        if (isSealed) sealedSubclasses.flatMap { if (it.isSealed) it.collectSubclasses() else listOf(it) }
        else listOf(this)
}
