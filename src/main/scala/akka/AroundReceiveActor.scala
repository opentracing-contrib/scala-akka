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
package akka

import akka.actor.Actor

trait AroundReceiveActor extends Actor {

  override protected[akka] def aroundReceive(receive: Receive, msg: Any): Unit = {
    traceBeforeReceive(receive, msg)
    traceAfterReceive(receive, msg)
  }

  def superAroundReceive(receive: Receive, msg: Any): Unit = {
    super.aroundReceive(receive, msg)
  }

  protected def traceBeforeReceive(receive: Receive, msg: Any): Unit = {}

  protected def traceAfterReceive(receive: Receive, msg: Any): Unit = {}
}
