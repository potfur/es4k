package potfur.es

data class EventStream<ID, E>(
    val id: ID,
    val commited: List<E>,
    val pending: List<E>,
) : List<E> {
    companion object {
        fun <ID, E> create(id: ID) = EventStream<ID, E>(id, emptyList(), emptyList())
        fun <ID, E> open(id: ID, commited: List<E>) = EventStream(id, commited, emptyList())
    }

    val all: List<E> = commited + pending

    val revision = commited.size

    fun add(vararg events: E) = EventStream(this.id, commited, pending + events)

    fun commit() = EventStream(id, all, emptyList())

    override val size = all.size

    override fun get(index: Int): E = all[index]

    override fun isEmpty() = all.isEmpty()

    override fun iterator() = all.iterator()

    override fun listIterator() = all.listIterator()

    override fun listIterator(index: Int) = all.listIterator(index)

    override fun subList(fromIndex: Int, toIndex: Int) = all.subList(fromIndex, toIndex)

    override fun lastIndexOf(element: E) = all.lastIndexOf(element)

    override fun indexOf(element: E) = all.indexOf(element)

    override fun containsAll(elements: Collection<E>) = all.containsAll(elements)

    override fun contains(element: E) = all.contains(element)
}
