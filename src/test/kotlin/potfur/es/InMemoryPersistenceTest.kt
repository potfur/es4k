package potfur.es

import potfur.es.example.InMemoryTransactor
import java.util.UUID


class InMemoryPersistenceTest : PersistenceTestCase() {
    override val streamId = UUID.randomUUID()
    override val transactor = InMemoryTransactor()
    override val persistence = InMemoryPersistence<UUID, Event>()
}
