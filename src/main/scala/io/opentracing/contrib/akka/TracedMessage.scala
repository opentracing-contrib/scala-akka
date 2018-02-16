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

import io.opentracing.Span
import io.opentracing.util.GlobalTracer

class TracedMessage[T](val message: T, val activeSpan: Span) {

}

object TracedMessage {
  def wrap(message: Any): Any = wrap(GlobalTracer.get.activeSpan, message)

  def wrap[T](activeSpan: Span, message: T): Any = {
    if (message == null) throw new IllegalArgumentException("message cannot be null")
    if (activeSpan == null) return message
    new TracedMessage[T](message, activeSpan)
  }
}
