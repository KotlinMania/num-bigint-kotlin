# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Files Present:** 23/27 (85.2%)
- **Function parity:** 346/534 matched (target 599) — 64.8%
- **Class/type parity:** 13/41 matched (target 20) — 31.7%
- **Combined symbol parity:** 359/575 matched (target 619) — 62.4%
- **Average inline-code cosine:** 0.20 (function body across 23 matched files)
- **Average documentation cosine:** 0.37 (doc text across 23 matched files)
- **Cheat-zeroed Files:** 0
- **Critical Issues:** 23 files with <0.60 function similarity
- **Needs Review:** 0 files with 0.60-0.84 function similarity
- **Excellent:** 0 files with >=0.85 function similarity

## Priority 1: Fix Incomplete High-Dependency Files

No incomplete high-dependency files detected.

## Priority 2: Port Missing High-Value Files

Critical missing files (>10 dependencies):

No missing high-value files detected.

## Detailed Work Items

Every matched file is listed below with function and type symbol parity.

### 1. biguint

- **Target:** `biguint.BigUint`
- **Similarity:** 0.24
- **Dependents:** 7
- **Priority Score:** 7218907.5
- **Functions:** 65/85 matched (target 84)
- **Missing functions:** `hash`, `eq`, `partial_cmp`, `cmp`, `default`, `fmt`, `fmt`, `fmt`, `fmt`, `fmt`, `fmt`, `sqrt`, `cbrt`, `nth_root`, `from_be_bytes`, `from_le_bytes`, `to_be_bytes`, `to_le_bytes`, `test_u32_u128`, `test_u128_u32_roundtrip`
- **Types:** 3/4 matched
- **Missing types:** `Bytes`
- **Tests:** 0/2 matched

### 2. bigint

- **Target:** `bigint.BigInt`
- **Similarity:** 0.20
- **Dependents:** 6
- **Priority Score:** 6391108.0
- **Functions:** 71/104 matched (target 129)
- **Missing functions:** `neg`, `hash`, `eq`, `partial_cmp`, `cmp`, `default`, `fmt`, `fmt`, `fmt`, `fmt`, `fmt`, `fmt`, `not`, `neg`, `neg`, `extended_gcd_lcm`, `digits`, `digits_mut`, `capacity`, `len`, `sqrt`, `cbrt`, `nth_root`, `from_be_bytes`, `from_le_bytes`, `to_be_bytes`, `to_le_bytes`, `test_from_biguint`, `check`, `test_from_slice`, `check`, `test_assign_from_slice`, `check`
- **Types:** 2/7 matched (target 2)
- **Missing types:** `Output`, `UnsignedAbs`, `CheckedUnsignedAbs`, `ToBigInt`, `Bytes`
- **Tests:** 0/3 matched

### 3. biguint.convert

- **Target:** `biguint.Convert`
- **Similarity:** 0.09
- **Dependents:** 0
- **Priority Score:** 243709.1
- **Functions:** 13/35 matched (target 39)
- **Missing functions:** `from_str`, `from_radix_be`, `from_radix_le`, `from_str_radix`, `to_i64`, `to_i128`, `to_u64`, `to_u128`, `to_f32`, `to_f64`, `from_i64`, `from_i128`, `from_u64`, `from_u128`, `from_f64`, `from`, `from`, `from`, `to_radix_le`, `generate_radix_bases`, `test_radix_bases`, `test_half_radix_bases`
- **Types:** 0/2 matched (target 0)
- **Missing types:** `Err`, `FromStrRadixErr`
- **Tests:** 0/2 matched

### 4. bigint.convert

- **Target:** `bigint.Convert`
- **Similarity:** 0.15
- **Dependents:** 0
- **Priority Score:** 153408.5
- **Functions:** 19/31 matched (target 24)
- **Missing functions:** `to_i128`, `to_u128`, `from_i128`, `from_u128`, `from`, `from`, `from`, `from`, `from`, `try_from`, `try_from`, `from`
- **Types:** 0/3 matched (target 0)
- **Missing types:** `Err`, `FromStrRadixErr`, `Error`

### 5. bigint.division

- **Target:** `bigint.Division`
- **Similarity:** 0.28
- **Dependents:** 0
- **Priority Score:** 134807.2
- **Functions:** 35/47 matched (target 35)
- **Missing functions:** `div`, `div_assign`, `div`, `div`, `div_assign`, `div`, `rem`, `rem_assign`, `rem`, `rem`, `rem_assign`, `rem`
- **Types:** 0/1 matched (target 0)
- **Missing types:** `Output`

### 6. bigint.subtraction

- **Target:** `bigint.Subtraction`
- **Similarity:** 0.16
- **Dependents:** 0
- **Priority Score:** 102508.4
- **Functions:** 15/24 matched (target 30)
- **Missing functions:** `sub`, `sub`, `sub`, `sub`, `sub_assign`, `sub`, `sub`, `sub_assign`, `sub`
- **Types:** 0/1 matched (target 0)
- **Missing types:** `Output`

### 7. lib

- **Target:** `numbigint.BigDigit`
- **Similarity:** 0.10
- **Dependents:** 0
- **Priority Score:** 91909.0
- **Functions:** 5/14 matched (target 13)
- **Missing functions:** `__description`, `fmt`, `description`, `new`, `__description`, `description`, `fmt`, `get_hi`, `get_lo`
- **Types:** 5/5 matched (target 8)
- **Missing types:** _none_

### 8. biguint.multiplication

- **Target:** `biguint.Multiplication`
- **Similarity:** 0.11
- **Dependents:** 0
- **Priority Score:** 91608.9
- **Functions:** 7/15 matched (target 16)
- **Missing functions:** `bigint_from_slice`, `sub_sign`, `mul`, `mul_assign`, `mul`, `mul`, `test_sub_sign`, `sub_sign_i`
- **Types:** 0/1 matched (target 0)
- **Missing types:** `Output`
- **Tests:** 0/1 matched

### 9. biguint.subtraction

- **Target:** `biguint.Subtraction`
- **Similarity:** 0.05
- **Dependents:** 0
- **Priority Score:** 91309.5
- **Functions:** 4/12 matched (target 14)
- **Missing functions:** `sub2rev`, `sub`, `sub_assign`, `sub`, `sub`, `sub_assign`, `sub`, `sub`
- **Types:** 0/1 matched (target 0)
- **Missing types:** `Output`

### 10. biguint.division

- **Target:** `biguint.Division`
- **Similarity:** 0.20
- **Dependents:** 0
- **Priority Score:** 83908.0
- **Functions:** 31/38 matched (target 31)
- **Missing functions:** `div_wide`, `div`, `div_assign`, `rem`, `rem`, `rem_assign`, `rem`
- **Types:** 0/1 matched (target 0)
- **Missing types:** `Output`

### 11. biguint.addition

- **Target:** `biguint.Addition`
- **Similarity:** 0.10
- **Dependents:** 0
- **Priority Score:** 81109.0
- **Functions:** 3/10 matched (target 13)
- **Missing functions:** `add2`, `add`, `add_assign`, `add`, `add_assign`, `add`, `add`
- **Types:** 0/1 matched (target 0)
- **Missing types:** `Output`

### 12. biguint.power

- **Target:** `biguint.Power`
- **Similarity:** 0.06
- **Dependents:** 0
- **Priority Score:** 70909.4
- **Functions:** 2/8 matched (target 7)
- **Missing functions:** `pow`, `pow`, `pow`, `modpow`, `test_plain_modpow`, `test_pow_biguint`
- **Types:** 0/1 matched (target 0)
- **Missing types:** `Output`
- **Tests:** 0/2 matched

### 13. bigint.addition

- **Target:** `bigint.Addition`
- **Similarity:** 0.17
- **Dependents:** 0
- **Priority Score:** 41908.3
- **Functions:** 15/18 matched (target 32)
- **Missing functions:** `add_assign`, `add`, `add_assign`
- **Types:** 0/1 matched (target 0)
- **Missing types:** `Output`

### 14. biguint.iter

- **Target:** `biguint.Iter`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 40510.0
- **Functions:** 1/5 matched (target 15)
- **Missing functions:** `test_iter_u32_digits`, `test_iter_u64_digits`, `test_iter_u32_digits_be`, `test_iter_u64_digits_be`
- **Types:** 0/0 matched (target 2)
- **Missing types:** _none_
- **Tests:** 0/4 matched

### 15. bigint.multiplication

- **Target:** `bigint.Multiplication`
- **Similarity:** 0.21
- **Dependents:** 0
- **Priority Score:** 21507.9
- **Functions:** 13/14 matched (target 32)
- **Missing functions:** `mul_assign`
- **Types:** 0/1 matched (target 0)
- **Missing types:** `Output`

### 16. biguint.bits

- **Target:** `biguint.Bits`
- **Similarity:** 0.09
- **Dependents:** 0
- **Priority Score:** 20809.1
- **Functions:** 6/7 matched (target 6)
- **Missing functions:** `bitand`
- **Types:** 0/1 matched (target 0)
- **Missing types:** `Output`

### 17. bigint.bits

- **Target:** `bigint.Bits`
- **Similarity:** 0.09
- **Dependents:** 0
- **Priority Score:** 12009.1
- **Functions:** 19/19 matched (target 27)
- **Missing functions:** _none_
- **Types:** 0/1 matched
- **Missing types:** `Output`

### 18. biguint.monty

- **Target:** `biguint.Monty`
- **Similarity:** 0.47
- **Dependents:** 0
- **Priority Score:** 905.3
- **Functions:** 8/8 matched (target 9)
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_

### 19. biguint.serde

- **Target:** `biguint.Serde`
- **Similarity:** 0.34
- **Dependents:** 0
- **Priority Score:** 506.6
- **Functions:** 3/3 matched (target 6)
- **Missing functions:** _none_
- **Types:** 2/2 matched
- **Missing types:** _none_

### 20. biguint.shift

- **Target:** `biguint.Shift`
- **Similarity:** 0.27
- **Dependents:** 0
- **Priority Score:** 407.3
- **Functions:** 4/4 matched (target 9)
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 21. bigint.serde

- **Target:** `bigint.Serde`
- **Similarity:** 0.33
- **Dependents:** 0
- **Priority Score:** 406.7
- **Functions:** 4/4 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 22. bigint.power

- **Target:** `bigint.Power`
- **Similarity:** 0.57
- **Dependents:** 0
- **Priority Score:** 204.3
- **Functions:** 2/2 matched (target 7)
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 23. bigint.shift

- **Target:** `bigint.Shift`
- **Similarity:** 0.34
- **Dependents:** 0
- **Priority Score:** 106.6
- **Functions:** 1/1 matched (target 17)
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

## Success Criteria

For each file to be considered "complete":
- **Similarity ≥ 0.85** (Excellent threshold)
- All public APIs ported
- All tests ported
- Documentation ported
- port-lint header present

