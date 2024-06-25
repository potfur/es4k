# es4k

ES4K is a simple - but not naive - implementation of event stream for event sourced systems.
Its scope is to store and read streams for event store in a way, 
that can be used later to build more elaborate systems with their own event based process managers, 
state machines and for running projections.

## Event Stream

The `EventStream` represents stream of events.
It tracks events in event stream, those read from event store, and those added during processing.
Both in separate lists `commited` and `pending` accordingly.

To create new event stream:
```kotlin
val stream = EventStream.create(someId)
```

or, when recreating from event store:
```kotlin
val stream = EventStream.open(someId, readEvents)
```

`EventStream` can implements `List` interface, so it allows for using all extension functions of it.

```kotlin
val event = EventStream.open(someId, readEvents).lastOrNull { it is EventClass }
```


## Event Store

Event store is responsible for persisting event streams.
The `EventStream` interface comes with two methods, `EventStream.fetch` event stream from persistence, 
and `EventStream.store` to persist it.

The provided implementation, can be used with any persistence system, SQL and NoSQL alike, 
as long as access to it can be implemented as `Persistence` interface.

### Persistence
The `Persistence::read` and `Persistence::store` methods can be considered as self-explanatory, 
the `Persistence::revision` requires a bit of comment.

In more _traditional_ systems, row can be selected for update (lock row), a version value can be checked on update (MVCC). 
Since event store is additive only - only pending events will be stored - a different way of handling concurrency is needed.

The provided implementation `EventStoreWithRevisionCheck` depends on keeping track of _revision_ of the event stream,
in other words, the number of commited events.
The `EventStoreWithRevisionCheck` will compare persisted revision and the one in event stream that is being stored. 
If both are same - all good, this means that stream did not change in the meantime.
If different - something happened between reading event stream and storing it.

There are several ways of implementing revisions, and `Persistance` interface is not enforcing any of those.

#### Index Revision

Every stored event is stored with its index in the event stream, just like if event stream would be a list.
So, the first event would have `revision=0`, second would have `revision=1` and so on.
In case of adding new events to the existing stream, the revision for first events would be `revision=commited+1`,
for second `revision=commited+2` etc.

Biggest benefit of this solution is that there is no need of counting how many events stream has, 
getting the highest `revision` value for given stream is enough. 
Additionally, `revision` becomes natural value for ordering events.

Thus, this solution may be better for RDBMS, where count operation may be heavy, especially on large streams,
and fetching value from single row, or even index when defined will be significantly faster.

#### Count Revision

In this approach instead of storing revision explicitly, the implementation counts already stored events.
It may be more suitable for NoSQL systems, where stream may be stored as single document with list of events, 
thus fetching revision would be just fetching size of such list.

