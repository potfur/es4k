# es4k

ES4K is a simple - but not naive - implementation of event stream for event-sourced systems.
Its scope is to store and read streams for event store in a way,
that can be used later to build more elaborate systems with their own event-based process managers,
state machines, and running projections.

## Event Stream

The `EventStream` represents a stream of events.
It tracks events in the event stream, those read from the event store, and those added during processing.
Both are in separate lists `committed` and `pending` accordingly.

To create new event stream:
```kotlin
val stream = EventStream.create(someId)
```

or, when recreating from event store:
```kotlin
val stream = EventStream.open(someId, readEvents)
```

`EventStream` can implement `List` interface, so it allows for using all extension functions.

```kotlin
val event = EventStream.open(someId, readEvents).lastOrNull { it is EventClass }
```


## Event Store

The event store is responsible for persisting event streams.
The `EventStream` interface comes with two methods, `EventStream.fetch` event stream from persistence,
and `EventStream.store` to persist it.

The provided implementation can be used with any persistence system, SQL and NoSQL alike,
as long as access to it can be implemented as a `Persistence` interface.

### Persistence
The `Persistence::read` and `Persistence::store` methods can be considered as self-explanatory,
the `Persistence::revision` requires a bit of comment.

In more _traditional_ systems, a row can be selected for update (lock row), and a version value can be checked on update (MVCC).
Since the event store is additive only - only pending events will be stored - a different way of handling concurrency is needed.

The provided implementation `EventStoreWithRevisionCheck` depends on keeping track of _revision_ of the event stream,
in other words, the number of committed events.
The `EventStoreWithRevisionCheck` will compare the persisted revision and the one in the event stream that is being stored.
If both are the same - all good, this means that the stream did not change.
If different - something happened between reading the event stream and storing it.

There are several ways of implementing revisions, and `Persistence` interface is not enforcing any of those.

#### Index Revision

Every stored event is stored with its index in the event stream, just like if the event stream were a list.
So, the first event would have `revision=0`, the second would have `revision=1`, and so on.
In case of adding new events to the existing stream, the revision for the first events would be `revision=commited+1`,
for second `revision=commited+2` etc.

The biggest benefit of this solution is that there is no need to count how many events the stream has,
getting the highest `revision` value for the given stream is enough.
Additionally, `revision` becomes the natural value for ordering events.

Thus, this solution may be better for RDBMS, where count operation may be heavy, especially on large streams,
and fetching value from a single row, or even index when defined will be significantly faster.

#### Count Revision

In this approach instead of storing revision explicitly, the implementation counts already stored events.
It may be more suitable for NoSQL systems, where the stream may be stored as a single document with a list of events,
thus fetching revision would be just fetching the size of such a list.

## Example

In [testFixtures](./src/testFixtures/kotlin/potfur/es/example) there is example implementation of typical event sourced problem - bank account.

The tests with behaviour specification are located in [tests](./src/test/kotlin/potfur/es/example)

### Account
Is the entity that uses event stream to track its state, 
it exposes several basic operations that result in append events to stream and return new instance.

### Transfer
Sample operation that transfers money between two account - thus blocking resources on source, 
depositing on target and releasing blockade when deposit and withdraw worked.

### Projection
Example also includes simple implementation of Account projection, to showcase possible usages.
