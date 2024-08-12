package potfur.es

import java.util.UUID


class InMemoryPersistenceTest: PersistenceTestCase() {
    override val persistence = InMemoryPersistence<UUID, Event>()
}

