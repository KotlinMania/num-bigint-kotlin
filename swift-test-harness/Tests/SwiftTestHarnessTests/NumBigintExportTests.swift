import Testing
import NumBigint

@Suite struct NumBigintExportTests {
    @Test func swiftModuleLoads() {
        #expect(Bool(true))
    }
}
