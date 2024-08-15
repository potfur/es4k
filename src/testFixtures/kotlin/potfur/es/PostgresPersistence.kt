package potfur.es

import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.javatime.timestamp
import org.jetbrains.exposed.sql.json.jsonb
import org.jetbrains.exposed.sql.selectAll
import java.util.UUID

interface SerDe<T> {
    fun serialize(value: T): String
    fun deserialize(name: String, value: String): T
}

class PostgresPersistence(
    tableName: String,
    val serde: SerDe<Event>,
) : Persistence<UUID, Event>, IdTable<UUID>(tableName) {
    override val id = uuid(name = "id").entityId()
    val streamId = uuid(name = "stream_id")
    val name = text(name = "name")
    val offset = integer(name = "offset")
    val payload = jsonb("payload", { it }, { it })
    val timestamp = timestamp("timestamp")
    override val primaryKey = PrimaryKey(id)

    init {
        index(false, id, streamId)
        uniqueIndex(streamId, offset)
    }

    override fun read(id: UUID) =
        selectAll().where { streamId eq id }
            .orderBy(offset, SortOrder.ASC)
            .map { serde.deserialize(it[name], it[payload]) }
            .takeIf { it.isNotEmpty() }
            ?.let { Success(it) }
            ?: Failure(StreamNotFound("Stream not found"))

    override fun store(id: UUID, revision: Int, events: List<Event>) =
        events
            .forEachIndexed { i, event ->
                insert {
                    it[this.id] = event.id
                    it[this.streamId] = id
                    it[this.name] = event::class.simpleName ?: "unknown"
                    it[this.offset] = revision + i
                    it[this.payload] = serde.serialize(event)
                    it[this.timestamp] = event.timestamp
                }
            }
            .let { Success(Unit) }

    override fun revision(id: UUID) =
        selectAll().where { streamId eq id }.count().let { Success(it.toInt()) }

}
