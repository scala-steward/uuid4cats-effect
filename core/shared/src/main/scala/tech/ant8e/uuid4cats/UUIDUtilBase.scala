/*
 * Copyright 2023 Antoine Comte
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tech.ant8e.uuid4cats

import java.util.UUID

/** Base trait for implementing platform specific utils
  *
  * @tparam T
  *   Platform specific type representing a timestamp
  */
trait UUIDUtilBase[T] {

  def epochMillisToTimestamp(epochMillis: Long): T

  /** Extract a timestamp from an UUID when it exists */
  def extractTimestamp(uuid: UUID): Option[T] = {
    import Extractors.*
    (uuid match {
      case UUIDv1(ts) => Some(ts)
      case UUIDv6(ts) => Some(ts)
      case UUIDv7(ts) => Some(ts)
      case _          => None
    }).map(epochMillisToTimestamp)
  }

  private object Extractors {
    object UUIDv1 {
      def unapply(uuid: UUID): Option[Long] =
        Option.when(uuid.version == 1) {
          val msb = uuid.getMostSignificantBits
          val time_low = msb >>> 32
          val time_mid = (msb & 0xffff_ffffL) >>> 16
          val time_high = msb & 0xfffL
          val time = time_high << 48 | time_mid << 32 | time_low
          UUIDBuilder.fromUUIDTimestamp(time)
        }
    }

    object UUIDv6 {
      def unapply(uuid: UUID): Option[Long] =
        Option.when(uuid.version == 6) {
          val msb = uuid.getMostSignificantBits
          val time_low = msb & 0xfff
          val time_high_and_mid = msb >>> 16
          val time = time_high_and_mid << 12 | time_low
          UUIDBuilder.fromUUIDTimestamp(time)
        }
    }

    object UUIDv7 {
      def unapply(uuid: UUID): Option[Long] = Option.when(uuid.version == 7) {
        uuid.getMostSignificantBits >>> 16
      }
    }
  }
}
