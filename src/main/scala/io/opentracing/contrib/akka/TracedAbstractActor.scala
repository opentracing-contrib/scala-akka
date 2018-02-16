/*
 * Copyright 2018 The OpenTracing Authors
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

import akka.AroundReceiveActor
import akka.actor.Actor
import io.opentracing.Tracer
import io.opentracing.util.GlobalTracer


trait TracedAbstractActor extends Actor with AroundReceiveActor {

  override protected def traceBeforeReceive(receive: Receive, msg: Any): Unit = {
    if (!msg.isInstanceOf[TracedMessage[_]]) {
      superAroundReceive(receive, msg)
      return
    }

    val tracedMessage = msg.asInstanceOf[TracedMessage[_]]
    tracer().scopeManager.activate(tracedMessage.activeSpan, false)
    superAroundReceive(receive, tracedMessage.message)
  }

  override protected def traceAfterReceive(receive: Receive, msg: Any): Unit = {
    if (tracer().scopeManager().active() != null) {
      GlobalTracer.get().scopeManager().active().close()
    }
  }

  def tracer(): Tracer = GlobalTracer.get()
}
