/*
 * Copyright 2018-2019 The OpenTracing Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package io.opentracing.contrib.akka

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.pattern.Patterns.ask
import akka.util.Timeout
import io.opentracing.mock.MockTracer
import io.opentracing.util.{GlobalTracer, GlobalTracerTestUtil, ThreadLocalScopeManager}
import org.scalatest.{BeforeAndAfter, FunSuite}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class TracedAbstractActorTest extends FunSuite with BeforeAndAfter {
  val mockTracer = new MockTracer(new ThreadLocalScopeManager)
  var system: ActorSystem = _

  before {
    mockTracer.reset()
    GlobalTracer.registerIfAbsent(mockTracer)
    system = ActorSystem.create("testSystem")
  }

  after {
    GlobalTracerTestUtil.resetGlobalTracer()
    Await.result(system.terminate, getDefaultDuration)
  }

  test("testNoActiveSpan") {
    val actorRef = system.actorOf(SpanNullCheckActor.props, "one")
    val timeout = new Timeout(getDefaultDuration)
    val future = ask(actorRef, TracedMessage.wrap("foo"), timeout)

    val isSpanNull = Await.result(future, getDefaultDuration)
    assert(isSpanNull === true)
  }

  test("testActiveSpan") {
    val actorRef = system.actorOf(SpanCheckActor.props, "actorOne")
    val timeout = new Timeout(getDefaultDuration)

    val span = mockTracer.buildSpan("one").start()
    val scope = mockTracer.activateSpan(span)
    val message = TracedMessage.wrap(span /* message */)
    val future = ask(actorRef, message, timeout)
    scope.close()
    span.finish()

    val isSpanSame = Await.result(future, getDefaultDuration)
    assert(isSpanSame === true)
  }

  test("testNoWrapMessage") {
    val actorRef = system.actorOf(SpanCheckActor.props, "actorOne")
    val timeout = new Timeout(getDefaultDuration)

    val span = mockTracer.buildSpan("one").start()
    val scope = mockTracer.activateSpan(span)
    val future = ask(actorRef, span, timeout)
    scope.close()
    span.finish()

    val isSpanSame = Await.result(future, getDefaultDuration)
    assert(isSpanSame === false)
  }

  test("testExplicitTracer") {
    val myTracer = new MockTracer(new ThreadLocalScopeManager)

    val actorRef = system.actorOf(TracerCheckActor.props(myTracer), "one")
    val timeout = new Timeout(getDefaultDuration)

    val scope = myTracer.buildSpan("one").startActive(true)
    val future = ask(actorRef, myTracer, timeout)
    scope.close()

    val isTracerSame = Await.result(future, getDefaultDuration)
    assert(isTracerSame === true)
  }

  test("testGlobalTracer") {
    val actorRef = system.actorOf(TracerCheckActor.props, "one")
    val timeout = new Timeout(getDefaultDuration)

    val scope = mockTracer.buildSpan("one").startActive(true)
    val future = ask(actorRef, GlobalTracer.get, timeout)
    scope.close()

    val isTracerSame = Await.result(future, getDefaultDuration)
    assert(isTracerSame === true)
  }

  def getDefaultDuration = Duration(3, TimeUnit.SECONDS)
}
