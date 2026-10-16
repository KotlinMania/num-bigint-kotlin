// port-lint: tests ci/big_rand/src/lib.rs

/**
 * Test randomization of [BigUint] and [BigInt].
 *
 * This test is in a completely separate module so thread_rng()
 * can be available without infecting the rest of the build with
 * rand's default features, especially not rand/std.
 */
package io.github.kotlinmania.numbigint

import io.github.kotlinmania.rand.Rng
import io.github.kotlinmania.rand.SeedableRng
import io.github.kotlinmania.rand.distr.StandardUniform
import io.github.kotlinmania.rand.distr.Uniform
import io.github.kotlinmania.rand.distr.sampleIter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import io.github.kotlinmania.rand.rng as threadRng

internal interface SeedBytesRng :
    Rng,
    SeedableRng {
    val seedSize: Int

    fun reseedFromBytes(seed: ByteArray)
}

internal class ChaChaRng(
    seed: ByteArray = ByteArray(32),
) : SeedBytesRng {
    override val seedSize: Int = 32
    private val key = UIntArray(8)
    private var blockCounter: ULong = 0uL
    private val results = UIntArray(16)
    private var index: Int = 16

    init {
        reseedFromBytes(seed)
    }

    override fun reseedFromBytes(seed: ByteArray) {
        require(seed.size == 32)
        for (i in 0 until 8) {
            val off = i * 4
            key[i] = seed[off].toUByte().toUInt() or
                (seed[off + 1].toUByte().toUInt() shl 8) or
                (seed[off + 2].toUByte().toUInt() shl 16) or
                (seed[off + 3].toUByte().toUInt() shl 24)
        }
        blockCounter = 0uL
        index = 16
    }

    override fun seedFromU64(seed: ULong) {
        reseedFromBytes(pcg32SeedBytes(seed, 32))
    }

    override fun nextU32(): UInt {
        if (index >= 16) {
            refill()
        }
        return results[index++]
    }

    override fun nextU64(): ULong {
        val lo = nextU32().toULong()
        val hi = nextU32().toULong()
        return (hi shl 32) or lo
    }

    override fun fillBytes(dest: ByteArray) {
        fillBytesViaNext(this, dest)
    }

    private fun refill() {
        val state = UIntArray(16)
        state[0] = 0x61707865u
        state[1] = 0x3320646eu
        state[2] = 0x79622d32u
        state[3] = 0x6b206574u
        for (i in 0 until 8) {
            state[4 + i] = key[i]
        }
        state[12] = blockCounter.toUInt()
        state[13] = (blockCounter shr 32).toUInt()
        state[14] = 0u
        state[15] = 0u
        blockCounter++

        val working = state.copyOf()
        repeat(10) {
            quarterRound(working, 0, 4, 8, 12)
            quarterRound(working, 1, 5, 9, 13)
            quarterRound(working, 2, 6, 10, 14)
            quarterRound(working, 3, 7, 11, 15)
            quarterRound(working, 0, 5, 10, 15)
            quarterRound(working, 1, 6, 11, 12)
            quarterRound(working, 2, 7, 8, 13)
            quarterRound(working, 3, 4, 9, 14)
        }
        for (i in 0 until 16) {
            results[i] = working[i] + state[i]
        }
        index = 0
    }

    private fun quarterRound(st: UIntArray, a: Int, b: Int, c: Int, d: Int) {
        st[a] = st[a] + st[b]
        st[d] = (st[d] xor st[a]).rotateLeft(16)
        st[c] = st[c] + st[d]
        st[b] = (st[b] xor st[c]).rotateLeft(12)
        st[a] = st[a] + st[b]
        st[d] = (st[d] xor st[a]).rotateLeft(8)
        st[c] = st[c] + st[d]
        st[b] = (st[b] xor st[c]).rotateLeft(7)
    }
}

internal class XorShiftRng(
    seed: ByteArray = ByteArray(16),
) : SeedBytesRng {
    override val seedSize: Int = 16
    private var x: UInt = 0u
    private var y: UInt = 0u
    private var z: UInt = 0u
    private var w: UInt = 0u

    init {
        reseedFromBytes(seed)
    }

    override fun reseedFromBytes(seed: ByteArray) {
        require(seed.size == 16)
        val words = UIntArray(4)
        for (i in 0 until 4) {
            val off = i * 4
            words[i] = seed[off].toUByte().toUInt() or
                (seed[off + 1].toUByte().toUInt() shl 8) or
                (seed[off + 2].toUByte().toUInt() shl 16) or
                (seed[off + 3].toUByte().toUInt() shl 24)
        }
        if (words.all { it == 0u }) {
            x = 0xbad_5eedu
            y = 0xbad_5eedu
            z = 0xbad_5eedu
            w = 0xbad_5eedu
        } else {
            x = words[0]
            y = words[1]
            z = words[2]
            w = words[3]
        }
    }

    override fun seedFromU64(seed: ULong) {
        reseedFromBytes(pcg32SeedBytes(seed, 16))
    }

    override fun nextU32(): UInt {
        val x0 = x
        val t = x0 xor (x0 shl 11)
        x = y
        y = z
        z = w
        val w0 = w
        w = w0 xor (w0 shr 19) xor (t xor (t shr 8))
        return w
    }

    override fun nextU64(): ULong {
        val lo = nextU32().toULong()
        val hi = nextU32().toULong()
        return (hi shl 32) or lo
    }

    override fun fillBytes(dest: ByteArray) {
        fillBytesViaNext(this, dest)
    }

    companion object {
        fun seedFromU64(seed: ULong): XorShiftRng =
            XorShiftRng().apply { seedFromU64(seed) }
    }
}

internal class IsaacRng(
    seed: ByteArray = ByteArray(32),
) : SeedBytesRng {
    override val seedSize: Int = 32
    private val mem = UIntArray(256)
    private val rsl = UIntArray(256)
    private var a: UInt = 0u
    private var b: UInt = 0u
    private var c: UInt = 0u
    private var index: Int = 0

    init {
        reseedFromBytes(seed)
    }

    override fun reseedFromBytes(seed: ByteArray) {
        require(seed.size == 32)
        mem.fill(0u)
        rsl.fill(0u)
        for (i in 0 until 8) {
            val off = i * 4
            rsl[i] = seed[off].toUByte().toUInt() or
                (seed[off + 1].toUByte().toUInt() shl 8) or
                (seed[off + 2].toUByte().toUInt() shl 16) or
                (seed[off + 3].toUByte().toUInt() shl 24)
        }
        a = 0u
        b = 0u
        c = 0u
        initIsaac()
    }

    override fun seedFromU64(seed: ULong) {
        reseedFromBytes(pcg32SeedBytes(seed, 32))
    }

    override fun nextU32(): UInt {
        if (index >= 256) {
            isaac()
        }
        return rsl[index++]
    }

    override fun nextU64(): ULong {
        val lo = nextU32().toULong()
        val hi = nextU32().toULong()
        return (hi shl 32) or lo
    }

    override fun fillBytes(dest: ByteArray) {
        fillBytesViaNext(this, dest)
    }

    private fun initIsaac() {
        val golden = 0x9e3779b9u
        var n0 = golden
        var n1 = golden
        var n2 = golden
        var n3 = golden
        var n4 = golden
        var n5 = golden
        var n6 = golden
        var n7 = golden

        fun mix() {
            n0 = n0 xor (n1 shl 11)
            n3 += n0
            n1 += n2
            n1 = n1 xor (n2 shr 2)
            n4 += n1
            n2 += n3
            n2 = n2 xor (n3 shl 8)
            n5 += n2
            n3 += n4
            n3 = n3 xor (n4 shr 16)
            n6 += n3
            n4 += n5
            n4 = n4 xor (n5 shl 10)
            n7 += n4
            n5 += n6
            n5 = n5 xor (n6 shr 4)
            n0 += n5
            n6 += n7
            n6 = n6 xor (n7 shl 8)
            n1 += n6
            n7 += n0
            n7 = n7 xor (n0 shr 9)
            n2 += n7
            n0 += n1
        }

        repeat(4) { mix() }

        for (pass in 0 until 2) {
            val src = if (pass == 0) rsl else mem
            var i = 0
            while (i < 256) {
                n0 += src[i]
                n1 += src[i + 1]
                n2 += src[i + 2]
                n3 += src[i + 3]
                n4 += src[i + 4]
                n5 += src[i + 5]
                n6 += src[i + 6]
                n7 += src[i + 7]
                mix()
                mem[i] = n0
                mem[i + 1] = n1
                mem[i + 2] = n2
                mem[i + 3] = n3
                mem[i + 4] = n4
                mem[i + 5] = n5
                mem[i + 6] = n6
                mem[i + 7] = n7
                i += 8
            }
        }

        isaac()
    }

    private fun isaac() {
        c += 1u
        var bAcc = b + c
        var aAcc = a

        fun rngstep(mixVal: UInt, mIdx: Int, m2Idx: Int, rIdx: Int) {
            val x = mem[mIdx]
            aAcc = (aAcc xor mixVal) + mem[m2Idx]
            val y = mem[((x shr 2) and 0xffu).toInt()] + aAcc + bAcc
            mem[mIdx] = y
            bAcc = mem[((y shr 10) and 0xffu).toInt()] + x
            rsl[255 - rIdx] = bAcc
        }

        for (base in 0 until 128 step 4) {
            rngstep(aAcc shl 13, base + 0, base + 128 + 0, base + 0)
            rngstep(aAcc shr 6, base + 1, base + 128 + 1, base + 1)
            rngstep(aAcc shl 2, base + 2, base + 128 + 2, base + 2)
            rngstep(aAcc shr 16, base + 3, base + 128 + 3, base + 3)
        }
        for (base in 0 until 128 step 4) {
            rngstep(aAcc shl 13, base + 128 + 0, base + 0, base + 128 + 0)
            rngstep(aAcc shr 6, base + 128 + 1, base + 1, base + 128 + 1)
            rngstep(aAcc shl 2, base + 128 + 2, base + 2, base + 128 + 2)
            rngstep(aAcc shr 16, base + 128 + 3, base + 3, base + 128 + 3)
        }

        a = aAcc
        b = bAcc
        index = 0
    }
}

private fun pcg32SeedBytes(seed: ULong, size: Int): ByteArray {
    var state = seed
    val mul = 6364136223846793005uL
    val inc = 11634580027462260723uL
    val out = ByteArray(size)
    var pos = 0
    while (pos + 4 <= size) {
        state = state * mul + inc
        val xorshifted = (((state shr 18) xor state) shr 27).toUInt()
        val rot = (state shr 59).toInt()
        val word = xorshifted.rotateRight(rot)
        out[pos] = word.toByte()
        out[pos + 1] = (word shr 8).toByte()
        out[pos + 2] = (word shr 16).toByte()
        out[pos + 3] = (word shr 24).toByte()
        pos += 4
    }
    return out
}

private fun fillBytesViaNext(rng: Rng, dest: ByteArray) {
    var offset = 0
    while (offset + 8 <= dest.size) {
        val chunk = rng.nextU64()
        for (i in 0 until 8) {
            dest[offset + i] = ((chunk shr (i * 8)) and 0xffuL).toByte()
        }
        offset += 8
    }
    val rem = dest.size - offset
    if (rem > 4) {
        val chunk = rng.nextU64()
        for (i in 0 until rem) {
            dest[offset + i] = ((chunk shr (i * 8)) and 0xffuL).toByte()
        }
    } else if (rem > 0) {
        val chunk = rng.nextU32()
        for (i in 0 until rem) {
            dest[offset + i] = ((chunk shr (i * 8)) and 0xffu).toByte()
        }
    }
}

class BigUintRandTest {
    @Test
    fun testRand() {
        val rng = threadRng()
        val n: BigUint = rng.genBiguint(137uL)
        assertTrue(n.bits() <= 137uL)
        assertTrue(rng.genBiguint(0uL).isZero())
    }

    @Test
    fun testRandBits() {
        val rng = threadRng()
        val n: BigUint = rng.sample(RandomBits.new(137uL))
        assertTrue(n.bits() <= 137uL)
        val z: BigUint = rng.sample(RandomBits.new(0uL))
        assertTrue(z.isZero())
    }

    @Test
    fun testRandRange() {
        val rng = threadRng()

        repeat(10) {
            assertEquals(
                rng.genBiguintRange(BigUint.from(236u), BigUint.from(237u)),
                BigUint.from(236u),
            )
        }

        val l = BigUint.from(403469000u + 2352u)
        val u = BigUint.from(403469000u + 3513u)
        repeat(1000) {
            val nB: BigUint = rng.genBiguintBelow(u)
            assertTrue(nB < u)

            val n: BigUint = rng.genBiguintRange(l, u)
            assertTrue(n >= l)
            assertTrue(n < u)
        }
    }

    @Test
    fun testZeroRandRange() {
        assertFailsWith<IllegalArgumentException> {
            threadRng().genBiguintRange(BigUint.from(54u), BigUint.from(54u))
        }
    }

    @Test
    fun testNegativeRandRange() {
        val rng = threadRng()
        val l = BigUint.from(2352u)
        val u = BigUint.from(3513u)
        // Switching u and l should fail:
        assertFailsWith<IllegalArgumentException> {
            rng.genBiguintRange(u, l)
        }
    }

    @Test
    fun testRandUniform() {
        val rng = threadRng()

        val tiny = Uniform.new(BigUint.from(236u), BigUint.from(237u))
        repeat(10) {
            assertEquals(rng.sample(tiny), BigUint.from(236u))
        }

        val l = BigUint.from(403469000u + 2352u)
        val u = BigUint.from(403469000u + 3513u)
        val below = Uniform.new(BigUint.zero(), u.clone())
        val range = Uniform.new(l.clone(), u.clone())
        repeat(1000) {
            val nB: BigUint = rng.sample(below)
            assertTrue(nB < u)

            val n: BigUint = rng.sample(range)
            assertTrue(n >= l)
            assertTrue(n < u)
        }
    }

    private fun <R : SeedBytesRng> seededValueStability(
        rngFactory: (ByteArray) -> R,
        expected: Array<String>,
        seedSize: Int = 32,
    ) {
        val seed = ByteArray(seedSize)
        for (i in seed.indices) {
            seed[i] = (i.toUByte() * 191u).toByte()
        }
        val rng = rngFactory(seed)
        for ((i, s) in expected.withIndex()) {
            val n: BigUint = BigUint.fromStrRadix(s, 10u).getOrThrow()
            val r = rng.genBiguint((1uL shl i) + i.toULong())
            assertEquals(n, r)
        }
    }

    @Test
    fun testChachaValueStability() {
        val expected =
            arrayOf(
                "0",
                "0",
                "52",
                "84",
                "23780",
                "86502865016",
                "187057847319509867386",
                "34045731223080904464438757488196244981910",
                "23813754422987836414755953516143692594193066497413249270287126597896871975915808",
                "57401636903146945411652549098818446911814352529449356393690984105383482703074355" +
                    "67088360974672291353736011718191813678720755501317478656550386324355699624671",
            )
        seededValueStability(::ChaChaRng, expected)
    }

    @Test
    fun testIsaacValueStability() {
        val expected =
            arrayOf(
                "1",
                "4",
                "3",
                "649",
                "89116",
                "7730042024",
                "20773149082453254949",
                "35999009049239918667571895439206839620281",
                "10191757312714088681302309313551624007714035309632506837271600807524767413673006",
                "37805949268912387809989378008822038725134260145886913321084097194957861133272558" +
                    "43458183365174899239251448892645546322463253898288141861183340823194379722556",
            )
        seededValueStability(::IsaacRng, expected)
    }

    @Test
    fun testXorshiftValueStability() {
        val expected =
            arrayOf(
                "1",
                "0",
                "37",
                "395",
                "181116",
                "122718231117",
                "1068467172329355695001",
                "28246925743544411614293300167064395633287",
                "12750053187017853048648861493745244146555950255549630854523304068318587267293038",
                "53041498719137109355568081064978196049094604705283682101683207799515709404788873" +
                    "53417136457745727045473194367732849819278740266658219147356315674940229288531",
            )
        seededValueStability(::XorShiftRng, expected, seedSize = 16)
    }

    @Test
    fun testRootsRand() {
        fun check(x: BigUint, n: UInt) {
            val root = x.nthRoot(n)

            if (n == 2u) {
                assertEquals(root, x.sqrt())
            } else if (n == 3u) {
                assertEquals(root, x.cbrt())
            }

            val lo = root.pow(n)
            assertTrue(lo <= x)
            assertEquals(lo.nthRoot(n), root)
            if (!lo.isZero()) {
                assertEquals((lo - 1u).nthRoot(n), root - 1u)
            }

            val hi = (root + 1u).pow(n)
            assertTrue(hi > x)
            assertEquals(hi.nthRoot(n), root + 1u)
            assertEquals((hi - 1u).nthRoot(n), root)
        }

        val rng = threadRng()
        val bitRange = Uniform.uLong(0uL, 2048uL)
        val sampleBits =
            bitRange
                .sampleIter(rng)
                .asSequence()
                .take(100)
                .toList()
        for (bits in sampleBits) {
            val x = rng.genBiguint(bits)
            for (n in 2u..<11u) {
                check(x.clone(), n)
            }
            check(x.clone(), 100u)
        }
    }
}

class BigIntRandTest {
    @Test
    fun testRand() {
        val rng = threadRng()
        val n: BigInt = rng.genBigint(137uL)
        assertTrue(n.bits() <= 137uL)
        assertTrue(rng.genBigint(0uL).isZero())
    }

    @Test
    fun testRandBits() {
        val rng = threadRng()
        val n: BigInt = rng.sample(RandomBits.new(137uL).toBigIntDistribution())
        assertTrue(n.bits() <= 137uL)
        val z: BigInt = rng.sample(RandomBits.new(0uL).toBigIntDistribution())
        assertTrue(z.isZero())
    }

    @Test
    fun testRandRange() {
        val rng = threadRng()

        repeat(10) {
            assertEquals(
                rng.genBigintRange(BigInt.from(236), BigInt.from(237)),
                BigInt.from(236),
            )
        }

        fun check(l: BigInt, u: BigInt) {
            val localRng = threadRng()
            repeat(1000) {
                val n: BigInt = localRng.genBigintRange(l, u)
                assertTrue(n >= l)
                assertTrue(n < u)
            }
        }
        val l: BigInt = BigInt.from(403469000 + 2352)
        val u: BigInt = BigInt.from(403469000 + 3513)
        check(l.clone(), u.clone())
        check(-l.clone(), u.clone())
        check(-u, -l)
    }

    @Test
    fun testZeroRandRange() {
        assertFailsWith<IllegalArgumentException> {
            threadRng().genBigintRange(BigInt.from(54), BigInt.from(54))
        }
    }

    @Test
    fun testNegativeRandRange() {
        val rng = threadRng()
        val l = BigInt.from(2352)
        val u = BigInt.from(3513)
        // Switching u and l should fail:
        assertFailsWith<IllegalArgumentException> {
            rng.genBigintRange(u, l)
        }
    }

    @Test
    fun testRandUniform() {
        val rng = threadRng()

        val tiny = Uniform.new(BigInt.from(236u), BigInt.from(237u))
        repeat(10) {
            assertEquals(rng.sample(tiny), BigInt.from(236u))
        }

        fun check(l: BigInt, u: BigInt) {
            val localRng = threadRng()
            val range = Uniform.new(l.clone(), u.clone())
            repeat(1000) {
                val n: BigInt = localRng.sample(range)
                assertTrue(n >= l)
                assertTrue(n < u)
            }
        }
        val l: BigInt = BigInt.from(403469000 + 2352)
        val u: BigInt = BigInt.from(403469000 + 3513)
        check(l.clone(), u.clone())
        check(-l.clone(), u.clone())
        check(-u, -l)
    }

    private fun <R : SeedBytesRng> seededValueStability(
        rngFactory: (ByteArray) -> R,
        expected: Array<String>,
        seedSize: Int = 32,
    ) {
        val seed = ByteArray(seedSize)
        for (i in seed.indices) {
            seed[i] = (i.toUByte() * 191u).toByte()
        }
        val rng = rngFactory(seed)
        for ((i, s) in expected.withIndex()) {
            val n: BigInt = BigInt.fromStrRadix(s, 10u).getOrThrow()
            val r = rng.genBigint((1uL shl i) + i.toULong())
            assertEquals(n, r)
        }
    }

    @Test
    fun testChachaValueStability() {
        val expected =
            arrayOf(
                "0",
                "-6",
                "-1",
                "1321",
                "-147247",
                "8486373526",
                "-272736656290199720696",
                "2731152629387534140535423510744221288522",
                "-28820024790651190394679732038637785320661450462089347915910979466834461433196572",
                "501454570554170484799723603981439288209930393334472085317977614690773821680884844" +
                    "8530978478667288338327570972869032358120588620346111979053742269317702532328",
            )
        seededValueStability(::ChaChaRng, expected)
    }

    @Test
    fun testIsaacValueStability() {
        val expected =
            arrayOf(
                "1",
                "0",
                "5",
                "113",
                "-132240",
                "-36348760761",
                "-365690596708430705434",
                "-14090753008246284277803606722552430292432",
                "-26313941628626248579319341019368550803676255307056857978955881718727601479436059",
                "-14563174552421101848999036239003801073335703811160945137332228646111920972691151" +
                    "88341090358094331641182310792892459091016794928947242043358702692294695845817",
            )
        seededValueStability(::IsaacRng, expected)
    }

    @Test
    fun testXorshiftValueStability() {
        val expected =
            arrayOf(
                "-1",
                "-4",
                "11",
                "-1802",
                "966495",
                "-62592045703",
                "-602281783447192077116",
                "-34335811410223060575607987996861632509125",
                "29156580925282215857325937227200350542000244609280383263289720243118706105351199",
                "49920038676141573457451407325930326489996232208489690499754573826911037849083623" +
                    "24546142615325187412887314466195222441945661833644117700809693098722026764846",
            )
        seededValueStability(::XorShiftRng, expected, seedSize = 16)
    }

    @Test
    fun testRandomShr() {
        val rng = threadRng()

        repeat(1000) {
            val p = StandardUniform.sampleLong(rng)
            val big = BigInt.from(p)
            val bigger = big shl 1000
            assertEquals(bigger shr 1000, big)
            for (i in 0..<64) {
                val answer = BigInt.from(p shr i)
                assertEquals(big shr i, answer)
                assertEquals(bigger shr (1000 + i), answer)
            }
        }
    }
}
