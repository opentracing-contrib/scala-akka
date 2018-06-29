[![Build Status][ci-img]][ci] [![Coverage Status][cov-img]][cov] [![Released Version][maven-img]][maven]

# OpenTracing Scala Akka instrumentation
OpenTracing instrumentation for Scala Akka.

## Installation

build.sbt
```sbt
libraryDependencies += "io.opentracing.contrib" % "opentracing-scala-akka" % "VERSION"
```

## Usage

### Tracer registration

Instantiate tracer and register it with `GlobalTracer`:
```scala
val tracer: Tracer = ???
GlobalTracer.register(tracer)
```

### Actor's Span propagation

User is responsible for finishing the `Span`s. For this to work, classes must
inherit from `TracedAbstractActor` instead of `Actor`, and messages must be wrapped using
`TracedMessage.wrap()`:

```scala

class MyActor extends TracedAbstractActor {
  override def receive(): Receive = {
    case _: String =>
      sender().tell("ciao", self)
  }
}

val scope =  tracer.buildSpan("foo").startActive(true)

// scope.span() will be captured as part of TracedMessage.wrap(),
// and MyActor will receive the original 'myMessageObj` instance.
val future = ask(myActorRef, TracedMessage.wrap("hello"), timeout)
...
    
scope.close()
    
}
```

By default, `TracedAbstractActor`/`TracedMessage` use `io.opentracing.util.GlobalTracer`
to activate and fetch the `Span` respectively, but it's possible to manually specify
both the `Tracer` used to activate and the captured `Span`:

```scala
class MyActor(myTracer: Tracer) extends TracedAbstractActor {
  override def receive(): Receive = {
    case _: String =>
      // TracedAbstractActor.tracer() returns the Tracer being used,
      // either GlobalTracer 
      if (tracer().activeSpan() != null) {
        // Use the active Span, to set tags, create children, finish it, etc.
        tracer().activeSpan.finish()
      }
      ...
  }

  override def tracer(): Tracer = myTracer
}

val span = tracer.buildSpan("foo").start()
val future = ask(myActorRef, TracedMessage.wrap(span, "hello"), timeout);
```

[ci-img]: https://travis-ci.org/opentracing-contrib/scala-akka.svg?branch=master
[ci]: https://travis-ci.org/opentracing-contrib/scala-akka
[cov-img]: https://coveralls.io/repos/github/opentracing-contrib/scala-akka/badge.svg?branch=master
[cov]: https://coveralls.io/github/opentracing-contrib/scala-akka?branch=master
[maven-img]: https://img.shields.io/maven-central/v/io.opentracing.contrib/opentracing-scala-akka.svg
[maven]: http://search.maven.org/#search%7Cga%7C1%7Copentracing-scala-akka
