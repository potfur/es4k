package potfur.es

import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.peekFailure
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.BeforeClass
import potfur.es.example.Transactor
import java.util.UUID
import kotlin.test.BeforeTest

private fun Env(name: String, default: String = "") = System.getenv(name) ?: default

private val url = Env("DATABASE_URL", "0.0.0.0:5432/postgres")
private val user = Env("DATABASE_USER", "postgres")
private val pass = Env("DATABASE_PASS", "")

class ExposedTransactor(private val database: Database) : Transactor {
    override fun <T, E> primary(fn: () -> Result4k<T, E>) =
        transaction(database) { fn().peekFailure { rollback() } }
}

class PostgresPersistenceTest : PersistenceTestCase() {
    override val streamId = UUID.randomUUID()
    override val transactor = ExposedTransactor(connection)
    override val persistence = PostgresPersistence("event_store", SimpleClassNameSerDe<Event>())

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
}

