package potfur.es

import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.flatMap
import dev.forkhandles.result4k.orThrow
import dev.forkhandles.result4k.peekFailure
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.BeforeClass
import potfur.es.example.Transactor
import strikt.api.expectThrows
import java.util.UUID
import kotlin.test.BeforeTest
import kotlin.test.Test

private fun Env(name: String, default: String = "") = System.getenv(name) ?: default

private val url = Env("DATABASE_URL", "0.0.0.0:5432/postgres")
private val user = Env("DATABASE_USER", "postgres")
private val pass = Env("DATABASE_PASS", "")

class ExposedTransactor(private val database: Database) : Transactor {
    override fun <T, E> primary(fn: () -> Result4k<T, E>) =
        transaction(database) { fn().peekFailure { rollback() } }
}

class PostgresPersistenceTest : PersistenceTestCase() {
    private val serde = SimpleClassNameSerDe<Event>()

    override val streamId = UUID.randomUUID()
    override val transactor = ExposedTransactor(connection)
    override val persistence = PostgresPersistence("event_store", serde)

    companion object {
        lateinit var connection: Database

        @JvmStatic
        @BeforeClass
        fun connect() {
            connection = Database.connect(
                url = "jdbc:postgresql://$url",
                driver = "org.postgresql.Driver",
                user = user,
                password = pass
            )
        }
    }

    @BeforeTest
    fun setup() {
        transaction {
            SchemaUtils.drop(persistence)
            SchemaUtils.create(persistence)
        }
    }

    @Test
    fun `it raises exception when stream was modified during transaction`() {
        transactor.primary {
            persistence.store(streamId, 0, listOf(Event()))
        }.orThrow()

        expectThrows<ExposedSQLException> {
            transactor.primary {
                persistence.read(streamId)
                    .flatMap {
                        persistence.insertEventDirectly(streamId, it.size, Event())
                        Success(it)
                    }
                    .flatMap { persistence.store(streamId, it.size, listOf(Event())) }
            }
        }
    }

    private fun PostgresPersistence.insertEventDirectly(streamId: UUID, offset: Int, event: Event) =
        insert {
            it[this.id] = event.id
            it[this.streamId] = streamId
            it[this.name] = event::class.simpleName ?: "unknown"
            it[this.offset] = offset
            it[this.payload] = serde.serialize(event)
            it[this.timestamp] = event.timestamp
        }
}

