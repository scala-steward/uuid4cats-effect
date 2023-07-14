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

import cats.data.NonEmptyChain
import cats.syntax.all._
import munit.FunSuite

import java.util.UUID

class TypeIDSuite extends FunSuite {
  // test values from https://github.com/jetpack-io/typeid/tree/main/spec

  test("TypeID should build valid typeIDs") {
    //  nil
    assertValidEncoding(
      typeID = "00000000000000000000000000",
      prefix = "",
      uuid = uuid"00000000-0000-0000-0000-000000000000"
    )

    // one
    assertValidEncoding(
      typeID = "00000000000000000000000001",
      prefix = "",
      uuid = uuid"00000000-0000-0000-0000-000000000001"
    )

    // ten
    assertValidEncoding(
      typeID = "0000000000000000000000000a",
      prefix = "",
      uuid = uuid"00000000-0000-0000-0000-00000000000a"
    )

    // sixteen
    assertValidEncoding(
      typeID = "0000000000000000000000000g",
      prefix = "",
      uuid = uuid"00000000-0000-0000-0000-000000000010"
    )

    // thirty-two
    assertValidEncoding(
      typeID = "00000000000000000000000010",
      prefix = "",
      uuid = uuid"00000000-0000-0000-0000-000000000020"
    )

    // max-valid
    assertValidEncoding(
      typeID = "7zzzzzzzzzzzzzzzzzzzzzzzzz",
      prefix = "",
      uuid = uuid"ffffffff-ffff-ffff-ffff-ffffffffffff"
    )

    // valid-alphabet
    assertValidEncoding(
      typeID = "prefix_0123456789abcdefghjkmnpqrs",
      prefix = "prefix",
      uuid = uuid"0110c853-1d09-52d8-d73e-1194e95b5f19"
    )

    // valid-uuidv7
    assertValidEncoding(
      typeID = "prefix_01h455vb4pex5vsknk084sn02q",
      prefix = "prefix",
      uuid = uuid"01890a5d-ac96-774b-bcce-b302099a8057"
    )
  }

  test("TypeID should decode valid typeIDs") {
    // nil
    assertValidDecoding(
      typeID = "00000000000000000000000000",
      prefix = "",
      uuid = uuid"00000000-0000-0000-0000-000000000000"
    )

    // one
    assertValidDecoding(
      typeID = "00000000000000000000000001",
      prefix = "",
      uuid = uuid"00000000-0000-0000-0000-000000000001"
    )

    // ten
    assertValidDecoding(
      typeID = "0000000000000000000000000a",
      prefix = "",
      uuid = uuid"00000000-0000-0000-0000-00000000000a"
    )

    // sixteen
    assertValidDecoding(
      typeID = "0000000000000000000000000g",
      prefix = "",
      uuid = uuid"00000000-0000-0000-0000-000000000010"
    )

    // thirty-two
    assertValidDecoding(
      typeID = "00000000000000000000000010",
      prefix = "",
      uuid = uuid"00000000-0000-0000-0000-000000000020"
    )

    // max-valid
    assertValidDecoding(
      typeID = "7zzzzzzzzzzzzzzzzzzzzzzzzz",
      prefix = "",
      uuid = uuid"ffffffff-ffff-ffff-ffff-ffffffffffff"
    )

    // valid-alphabet
    assertValidDecoding(
      typeID = "prefix_0123456789abcdefghjkmnpqrs",
      prefix = "prefix",
      uuid = uuid"0110c853-1d09-52d8-d73e-1194e95b5f19"
    )

    // valid-uuidv7
    assertValidDecoding(
      typeID = "prefix_01h455vb4pex5vsknk084sn02q",
      prefix = "prefix",
      uuid = uuid"01890a5d-ac96-774b-bcce-b302099a8057",
      enforceUUIDV7 = true
    )
  }

  test("TypeID should not decode invalid typeIDs") {
    // prefix-uppercase
    assertInvalidDecoding(typeID = "PREFIX_00000000000000000000000000")

    // prefix-numeric
    assertInvalidDecoding(typeID = "12345_00000000000000000000000000")

    // prefix-period
    assertInvalidDecoding(typeID = "pre.fix_00000000000000000000000000")

    // prefix-underscore
    assertInvalidDecoding(typeID = "pre_fix_00000000000000000000000000")

    // prefix-non-ascii
    assertInvalidDecoding(typeID = "préfix_00000000000000000000000000")

    // prefix-spaces
    assertInvalidDecoding(typeID = "    prefix_00000000000000000000000000")

    // prefix-64-chars
    assertInvalidDecoding(typeID =
      "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijkl_00000000000000000000000000"
    )

    // separator-empty-prefix
    assertInvalidDecoding(typeID = "_00000000000000000000000000")

    // separator-empty
    assertInvalidDecoding(typeID = "_")

    //  suffix-short
    assertInvalidDecoding(typeID = "prefix_1234567890123456789012345")

    // suffix-long
    assertInvalidDecoding(typeID = "prefix_123456789012345678901234567")

    // suffix-spaces
    assertInvalidDecoding(typeID = "prefix_1234567890123456789012345 ")

    /* suffix-uppercase */
    assertInvalidDecoding(typeID = "prefix_0123456789ABCDEFGHJKMNPQRS")

    // suffix-hyphens
    assertInvalidDecoding(typeID = "prefix_123456789-123456789-123456")

    // suffix-wrong-alphabet
    assertInvalidDecoding(typeID = "prefix_ooooooiiiiiiuuuuuuulllllll")

    // suffix-ambiguous-crockford
    assertInvalidDecoding(typeID = "prefix_i23456789ol23456789oi23456")

    // suffix-hyphens-crockford
    assertInvalidDecoding(typeID = "prefix_123456789-0123456789-0123456")

    // suffix-overflow
    assertInvalidDecoding(typeID = "prefix_8zzzzzzzzzzzzzzzzzzzzzzzzz")
  }
  private def assertValidEncoding(
      typeID: String,
      prefix: String,
      uuid: UUID
  ): Unit = {
    assertEquals(
      TypeID
        .build(prefix = prefix, uuid = uuid, uuid.version() == 7)
        .map(_.value),
      typeID.valid[TypeID.BuildError].toValidatedNec
    )
  }
  private def assertValidDecoding(
      typeID: String,
      prefix: String,
      uuid: UUID,
      enforceUUIDV7: Boolean = false
  ): Unit = {
    val obtained = TypeID.decode(typeID, enforceUUIDV7)
    val expected = TypeID
      .build(prefix = prefix, uuid = uuid, uuid.version() == 7)
      .leftMap(_ =>
        NonEmptyChain.one(
          TypeID.DecodeError.InvalidTypeID("Test"): TypeID.DecodeError
        )
      )

    assertEquals(obtained, expected)
  }
  private def assertInvalidDecoding(typeID: String): Unit = {
    val obtained = TypeID.decode(typeID)
    assert(obtained.isInvalid, s"$typeID should not decode as a valid TypeID")
  }

  implicit class uuidOps(sc: StringContext) {
    def uuid(args: Any*): UUID = UUID.fromString(sc.s(args: _*))
  }
}