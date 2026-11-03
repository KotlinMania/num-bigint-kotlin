// port-lint: tests tests/modpow.rs
package io.github.kotlinmania.numbigint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private const val BIG_B: String =
    "efac3c0a_0de55551_fee0bfe4_67fa017a_1a898fa1_6ca57cb1" +
        "ca9e3248_cacc09a9_b99d6abc_38418d0f_82ae4238_d9a68832" +
        "aadec7c1_ac5fed48_7a56a71b_67ac59d5_afb28022_20d9592d" +
        "247c4efc_abbd9b75_586088ee_1dc00dc4_232a8e15_6e8191dd" +
        "675b6ae0_c80f5164_752940bc_284b7cee_885c1e10_e495345b" +
        "8fbe9cfd_e5233fe1_19459d0b_d64be53c_27de5a02_a829976b" +
        "33096862_82dad291_bd38b6a9_be396646_ddaf8039_a2573c39" +
        "1b14e8bc_2cb53e48_298c047e_d9879e9c_5a521076_f0e27df3" +
        "990e1659_d3d8205b_6443ebc0_9918ebee_6764f668_9f2b2be3" +
        "b59cbc76_d76d0dfc_d737c3ec_0ccf9c00_ad0554bf_17e776ad" +
        "b4edf9cc_6ce540be_76229093_5c53893b"

private const val BIG_E: String =
    "be0e6ea6_08746133_e0fbc1bf_82dba91e_e2b56231_a81888d2" +
        "a833a1fc_f7ff002a_3c486a13_4f420bf3_a5435be9_1a5c8391" +
        "774d6e6c_085d8357_b0c97d4d_2bb33f7c_34c68059_f78d2541" +
        "eacc8832_426f1816_d3be001e_b69f9242_51c7708e_e10efe98" +
        "449c9a4a_b55a0f23_9d797410_515da00d_3ea07970_4478a2ca" +
        "c3d5043c_bd9be1b4_6dce479d_4302d344_84a939e6_0ab5ada7" +
        "12ae34b2_30cc473c_9f8ee69d_2cac5970_29f5bf18_bc8203e4" +
        "f3e895a2_13c94f1e_24c73d77_e517e801_53661fdd_a2ce9e47" +
        "a73dd7f8_2f2adb1e_3f136bf7_8ae5f3b8_08730de1_a4eff678" +
        "e77a06d0_19a522eb_cbefba2a_9caf7736_b157c5c6_2d192591" +
        "17946850_2ddb1822_117b68a0_32f7db88"

// This modulus is the prime from the 2048-bit MODP DH group:
// https://tools.ietf.org/html/rfc3526#section-3
private const val BIG_M: String =
    "FFFFFFFF_FFFFFFFF_C90FDAA2_2168C234_C4C6628B_80DC1CD1" +
        "29024E08_8A67CC74_020BBEA6_3B139B22_514A0879_8E3404DD" +
        "EF9519B3_CD3A431B_302B0A6D_F25F1437_4FE1356D_6D51C245" +
        "E485B576_625E7EC6_F44C42E9_A637ED6B_0BFF5CB6_F406B7ED" +
        "EE386BFB_5A899FA5_AE9F2411_7C4B1FE6_49286651_ECE45B3D" +
        "C2007CB8_A163BF05_98DA4836_1C55D39A_69163FA8_FD24CF5F" +
        "83655D23_DCA3AD96_1C62F356_208552BB_9ED52907_7096966D" +
        "670C354E_4ABC9804_F1746C08_CA18217C_32905E46_2E36CE3B" +
        "E39E772C_180E8603_9B2783A2_EC07A28F_B5C55DF0_6F4C52C9" +
        "DE2BCBF6_95581718_3995497C_EA956AE5_15D22618_98FA0510" +
        "15728E5A_8AACAA68_FFFFFFFF_FFFFFFFF"

private const val BIG_R: String =
    "a1468311_6e56edc9_7a98228b_5e924776_0dd7836e_caabac13" +
        "eda5373b_4752aa65_a1454850_40dc770e_30aa8675_6be7d3a8" +
        "9d3085e4_da5155cf_b451ef62_54d0da61_cf2b2c87_f495e096" +
        "055309f7_77802bbb_37271ba8_1313f1b5_075c75d1_024b6c77" +
        "fdb56f17_b05bce61_e527ebfd_2ee86860_e9907066_edd526e7" +
        "93d289bf_6726b293_41b0de24_eff82424_8dfd374b_4ec59542" +
        "35ced2b2_6b195c90_10042ffb_8f58ce21_bc10ec42_64fda779" +
        "d352d234_3d4eaea6_a86111ad_a37e9555_43ca78ce_2885bed7" +
        "5a30d182_f1cf6834_dc5b6e27_1a41ac34_a2e91e11_33363ff0" +
        "f88a7b04_900227c9_f6e6d06b_7856b4bb_4e354d61_060db6c8" +
        "109c4735_6e7db425_7b5d74c7_0b709508"

class BigUintModpowTest {
    private fun checkModpow(b0: ULong, e0: ULong, m0: ULong, r0: ULong) {
        val b = BigUint.from(b0)
        val e = BigUint.from(e0)
        val m = BigUint.from(m0)
        val r = BigUint.from(r0)

        assertEquals(r, b.modpow(e, m))

        val evenM = m shl 1
        val evenModpow = b.modpow(e, evenM)
        assertTrue(evenModpow < evenM)
        assertEquals(r, evenModpow.modFloor(m))
    }

    @Test
    fun testModpowSingle() {
        checkModpow(1uL, 0uL, 11uL, 1uL)
        checkModpow(0uL, 15uL, 11uL, 0uL)
        checkModpow(3uL, 7uL, 11uL, 9uL)
        checkModpow(5uL, 117uL, 19uL, 1uL)
        checkModpow(20uL, 1uL, 2uL, 0uL)
        checkModpow(20uL, 1uL, 3uL, 2uL)
    }

    @Test
    fun testModpowSmall() {
        for (b in 0uL..<11uL) {
            for (e in 0uL..<11uL) {
                for (m in 1uL..<11uL) {
                    checkModpow(b, e, m, BigUint.from(b).pow(e.toUInt()).toULongOrNull()!! % m)
                }
            }
        }
    }

    @Test
    fun testModpowBig() {
        val b = BigUint.fromStrRadix(BIG_B, 16u).getOrThrow()
        val e = BigUint.fromStrRadix(BIG_E, 16u).getOrThrow()
        val m = BigUint.fromStrRadix(BIG_M, 16u).getOrThrow()
        val r = BigUint.fromStrRadix(BIG_R, 16u).getOrThrow()

        assertEquals(r, b.modpow(e, m))

        val evenM = m shl 1
        val evenModpow = b.modpow(e, evenM)
        assertTrue(evenModpow < evenM)
        assertEquals(r, evenModpow % m)
    }
}

class BigIntModpowTest {
    private fun checkModpow(b0: Any, e0: Any, m0: Any, r0: Any) {
        fun check(b: BigInt, e: BigInt, m: BigInt, r: BigInt) {
            assertEquals(r, b.modpow(e, m), "$b ** $e (mod $m) != $r")

            val evenM = m shl 1
            val evenModpow = b.modpow(e, m)
            assertTrue(evenModpow.abs() < evenM.abs())
            assertEquals(r, evenModpow.modFloor(m))

            // the sign of the result follows the modulus like `modFloor`, not `rem`
            assertEquals(b.modFloor(m), b.modpow(BigInt.one(), m))
        }

        val b = if (b0 is BigInt) b0 else BigInt.from((b0 as Number).toLong())
        val e = if (e0 is BigInt) e0 else BigInt.from((e0 as Number).toLong())
        val m = if (m0 is BigInt) m0 else BigInt.from((m0 as Number).toLong())
        val r = if (r0 is BigInt) r0 else BigInt.from((r0 as Number).toLong())

        val negBR =
            if (e.isOdd()) {
                (-r).modFloor(m)
            } else {
                r.clone()
            }
        val negMR = r.modFloor(-m)
        val negBmR = negBR.modFloor(-m)

        check(b, e, m, r)
        check(-b, e, m, negBR)
        check(b, e, -m, negMR)
        check(-b, e, -m, negBmR)
    }

    @Test
    fun testModpow() {
        checkModpow(1, 0, 11, 1)
        checkModpow(0, 15, 11, 0)
        checkModpow(3, 7, 11, 9)
        checkModpow(5, 117, 19, 1)
        checkModpow(-20, 1, 2, 0)
        checkModpow(-20, 1, 3, 1)
    }

    @Test
    fun testModpowSmall() {
        for (b in -10L..<11L) {
            for (e in 0L..<11L) {
                for (m in -10L..<11L) {
                    if (m == 0L) {
                        continue
                    }
                    checkModpow(
                        b,
                        e,
                        m,
                        BigInt
                            .from(b)
                            .pow(e.toUInt())
                            .modFloor(BigInt.from(m))
                            .toLongOrNull()!!,
                    )
                }
            }
        }
    }

    @Test
    fun testModpowBig() {
        val b = BigInt.fromStrRadix(BIG_B, 16u).getOrThrow()
        val e = BigInt.fromStrRadix(BIG_E, 16u).getOrThrow()
        val m = BigInt.fromStrRadix(BIG_M, 16u).getOrThrow()
        val r = BigInt.fromStrRadix(BIG_R, 16u).getOrThrow()

        checkModpow(b, e, m, r)
    }
}
