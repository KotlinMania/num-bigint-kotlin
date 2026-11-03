import Testing
import NumBigint

@Suite struct NumBigintExportTests {
    @Test func signedFactoryAndArithmeticAreExported() {
        let value = BigInt.Companion.shared.from(value: Int32(-42))
        #expect(value.toStrRadix(radix: 10) == "-42")
        #expect((-value).toStrRadix(radix: 10) == "42")
        #expect(BigInt.Companion.shared.zero().isZero())
    }

    @Test func parsingReturnsTypedValuesAndInspectableErrors() {
        let signed = BigInt.Companion.shared.fromStrRadix(s0: "-42", radix: 10)
        #expect(signed.isSuccess())
        #expect(signed.value?.toStrRadix(radix: 10) == "-42")
        #expect(signed.error == nil)
        let unsigned = BigUint.Companion.shared.fromStrRadix(s: "42", radix: 10)
        #expect(unsigned.value?.bits() == 6)
        let invalid = BigUint.Companion.shared.fromStrRadix(s: "invalid", radix: 10)
        #expect(invalid.isFailure())
        #expect(invalid.value == nil)
        #expect(invalid.error?.message == "invalid digit found in string")
    }

    @Test func unsignedFactoryAndMethodsAreExported() {
        let value = BigUint.Companion.shared.fromUInt(n: 42)
        #expect(value.toStrRadix(radix: 10) == "42")
        #expect(value.bits() == 6)
        #expect(BigUint.Companion.shared.one().isOne())
    }
}
