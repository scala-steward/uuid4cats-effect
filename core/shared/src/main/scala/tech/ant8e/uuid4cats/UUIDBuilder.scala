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

object UUIDBuilder {
  val Variant = 0x2L

  def buildUUIDv1(epochMillis: Long, sequence: Long, random: Long): UUID = {
    val Version = 0x1L
    val gregorianTimestamp = toUUIDTimestamp(epochMillis)
    val time_high =
      gregorianTimestamp >>> 48 // 12 most significant bits of the timestamp
    val time__mid =
      (gregorianTimestamp >>> 32) & 0xffff // 16 middle bits of the timestamp
    val time_low =
      gregorianTimestamp & 0xffff_ffff // 32 least significant bits of the timestamp
    val node =
      ((random << 16) >>> 16) | (0x1L << 40) // 48 bits (MAC address with the unicast bit set to 1)
    val clock_seq = sequence & 0x3fff // 14 bits
    val msb = (time_low << 32) | time__mid << 16 | (Version << 12) | time_high
    val lsb = (Variant << 62) | clock_seq << 48 | node
    new UUID(msb, lsb)
  }

  def buildUUIDv4(randomHigh: Long, randomLow: Long): UUID = {
    val Version = 0x4L
    val msb = randomHigh & ~(0xf << 12) | (Version << 12)
    val lsb = (Variant << 62) | (randomLow << 2 >>> 2)
    new UUID(msb, lsb)
  }

  def buildUUIDv6(epochMillis: Long, sequence: Long, random: Long): UUID = {
    val Version = 0x6L
    val gregorianTimestamp = toUUIDTimestamp(epochMillis)
    val time_high_and_mid =
      gregorianTimestamp >>> 12 // 48 most significant bits of the timestamp
    val time_low =
      gregorianTimestamp & 0xfff // 12 least significant bits of the timestamp
    val node = (random << 16) >>> 16 // 48 bits
    val clock_seq = sequence & 0x3fff // 14 bits
    val msb = (time_high_and_mid << 16) | (Version << 12) | time_low
    val lsb = (Variant << 62) | clock_seq << 48 | node
    new UUID(msb, lsb)
  }

  def buildUUIDV7(epochMillis: Long, sequence: Long, random: Long): UUID = {
    val Version = 0x7L
    val rand_a = sequence & 0xfffL // 12 bits
    val rand_b = (random << 2) >>> 2 // we need only 62 bits of randomness
    val msb = (epochMillis << 16) | (Version << 12) | rand_a
    val lsb = (Variant << 62) | rand_b
    new UUID(msb, lsb)
  }

  /** number of 100 nanosecond intervals since the beginning of the gregorian
    * calendar (15-oct-1582) to Unix Epoch
    */
  private val UnixEpochClockOffset = 0x01b21dd213814000L

  @inline
  final def toUUIDTimestamp(epochMillis: Long): Long = {
    val ClockMultiplier =
      10000L //  count of 100 nanosecond intervals in a milli
    val ts = epochMillis * ClockMultiplier + UnixEpochClockOffset
    (ts << 4) >>> 4 // Keeping only the 60 least significant bits
  }

  @inline
  final def fromUUIDTimestamp(ts: Long): Long = {
    val ClockMultiplier =
      10000L //  count of 100 nanosecond intervals in a milli
    (ts - UnixEpochClockOffset) / ClockMultiplier
  }
}
