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

import io.opentracing.mock.MockTracer
import io.opentracing.util.{GlobalTracer, GlobalTracerTestUtil}
import org.scalatest.{BeforeAndAfter, FunSuite}

class TracedMessageTest extends FunSuite with BeforeAndAfter {
  val mockTracer = new MockTracer()

  before {
    mockTracer.reset()
  }

  after {
    GlobalTracerTestUtil.resetGlobalTracer()
  }

  test("null message") {
    intercept[IllegalArgumentException] {
      TracedMessage.wrap(null)
    }
  }

  test("testExplicitNoActiveSpan") {
    val originalMessage = "foo"
    val message = TracedMessage.wrap(null, originalMessage)
    assert(message == originalMessage)
  }

  test("testImplicitNoActiveSpan") {
    val originalMessage = "foo"
    val message = TracedMessage.wrap(originalMessage)
    assert(message == originalMessage)
  }

  test("testImplicitActiveSpan") {
    GlobalTracer.registerIfAbsent(mockTracer)

    val originalMessage = "foo"
    var message: Any = null
    val span = mockTracer.buildSpan("one").start

    val scope = mockTracer.scopeManager.activate(span)
    message = TracedMessage.wrap(originalMessage)
    scope.close()

    assert(message.isInstanceOf[TracedMessage[_]])

    val tracedMessage = message.asInstanceOf[TracedMessage[_]]
    assert(span == tracedMessage.activeSpan)
    assert(originalMessage == tracedMessage.message)
  }

  test("testExplicitActiveSpan") {
    val originalMessage = "foo"
    val span = mockTracer.buildSpan("one").start

    val message = TracedMessage.wrap(span, originalMessage)
    assert(message.isInstanceOf[TracedMessage[_]])

    val tracedMessage = message.asInstanceOf[TracedMessage[_]]
    assert(span == tracedMessage.activeSpan)
    assert(originalMessage == tracedMessage.message)
  }

}
