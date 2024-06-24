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
