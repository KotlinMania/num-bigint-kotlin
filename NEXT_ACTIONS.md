# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Files Present:** 23/27 (85.2%)
- **Function parity:** 270/376 matched (target 597) — 71.8%
- **Class/type parity:** 13/41 matched (target 22) — 31.7%
- **Combined symbol parity:** 283/417 matched (target 619) — 67.9%
- **Average inline-code cosine:** 0.48 (function body across 23 matched files)
- **Average documentation cosine:** 0.38 (doc text across 23 matched files)
- **Cheat-zeroed Files:** 0
- **Critical Issues:** 18 files with <0.60 function similarity

## Priority 1: Fix Incomplete High-Dependency Files

No incomplete high-dependency files detected.

## Priority 2: Port Missing High-Value Files

Critical missing files (>10 dependencies):

No missing high-value files detected.

## Detailed Work Items

Every matched file is listed below with function and type symbol parity.

### 1. biguint

- **Target:** `biguint.BigUint`
- **Similarity:** 0.59
- **Dependents:** 7
- **Priority Score:** 7138004.0
- **Functions:** 64/76 matched (target 82)
- **Missing functions:** `hash`, `eq`, `partial_cmp`, `cmp`, `default`, `fmt`, `from_be_bytes`, `from_le_bytes`, `to_be_bytes`, `to_le_bytes`, `test_u32_u128`, `test_u128_u32_roundtrip`
- **Types:** 3/4 matched
- **Missing types:** `Bytes`
- **Tests:** 0/2 matched

### 2. bigint

- **Target:** `bigint.BigInt`
- **Similarity:** 0.53
- **Dependents:** 6
- **Priority Score:** 6259804.5
- **Functions:** 71/91 matched (target 125)
- **Missing functions:** `neg`, `hash`, `eq`, `partial_cmp`, `cmp`, `default`, `fmt`, `extended_gcd_lcm`, `digits`, `digits_mut`, `capacity`, `len`, `from_be_bytes`, `from_le_bytes`, `to_be_bytes`, `to_le_bytes`, `test_from_biguint`, `check`, `test_from_slice`, `test_assign_from_slice`
- **Types:** 2/7 matched (target 2)
- **Missing types:** `Output`, `UnsignedAbs`, `CheckedUnsignedAbs`, `ToBigInt`, `Bytes`
- **Tests:** 0/3 matched

### 3. biguint.convert

- **Target:** `biguint.Convert`
- **Similarity:** 0.27
- **Dependents:** 0
- **Priority Score:** 223507.3
- **Functions:** 13/33 matched (target 39)
- **Missing functions:** `from_str`, `from_radix_be`, `from_radix_le`, `from_str_radix`, `to_i64`, `to_i128`, `to_u64`, `to_u128`, `to_f32`, `to_f64`, `from_i64`, `from_i128`, `from_u64`, `from_u128`, `from_f64`, `from`, `to_radix_le`, `generate_radix_bases`, `test_radix_bases`, `test_half_radix_bases`
- **Types:** 0/2 matched (target 0)
- **Missing types:** `Err`, `FromStrRadixErr`
- **Tests:** 0/2 matched

### 4. bigint.convert

- **Target:** `bigint.Convert`
- **Similarity:** 0.36
- **Dependents:** 0
- **Priority Score:** 92706.4
- **Functions:** 18/24 matched
- **Missing functions:** `to_i128`, `to_u128`, `from_i128`, `from_u128`, `from`, `try_from`
- **Types:** 0/3 matched (target 0)
- **Missing types:** `Err`, `FromStrRadixErr`, `Error`

### 5. biguint.multiplication

- **Target:** `biguint.Multiplication`
- **Similarity:** 0.30
- **Dependents:** 0
- **Priority Score:** 71407.0
- **Functions:** 7/13 matched (target 16)
- **Missing functions:** `bigint_from_slice`, `sub_sign`, `mul`, `mul_assign`, `test_sub_sign`, `sub_sign_i`
- **Types:** 0/1 matched (target 0)
- **Missing types:** `Output`
- **Tests:** 0/1 matched

### 6. lib

- **Target:** `numbigint.Lib`
- **Similarity:** 0.28
- **Dependents:** 0
- **Priority Score:** 51507.2
- **Functions:** 5/10 matched (target 13)
- **Missing functions:** `__description`, `fmt`, `new`, `get_hi`, `get_lo`
- **Types:** 5/5 matched (target 8)
- **Missing types:** _none_

### 7. biguint.power

- **Target:** `biguint.Power`
- **Similarity:** 0.19
- **Dependents:** 0
- **Priority Score:** 40608.1
- **Functions:** 2/5 matched (target 7)
- **Missing functions:** `modpow`, `test_plain_modpow`, `test_pow_biguint`
- **Types:** 0/1 matched (target 0)
- **Missing types:** `Output`
- **Tests:** 0/2 matched

### 8. biguint.iter

- **Target:** `biguint.Iter`
- **Similarity:** 0.05
- **Dependents:** 0
- **Priority Score:** 40509.5
- **Functions:** 1/5 matched (target 15)
- **Missing functions:** `test_iter_u32_digits`, `test_iter_u64_digits`, `test_iter_u32_digits_be`, `test_iter_u64_digits_be`
- **Types:** 0/0 matched (target 2)
- **Missing types:** _none_
- **Tests:** 0/4 matched

### 9. biguint.subtraction

- **Target:** `biguint.Subtraction`
- **Similarity:** 0.22
- **Dependents:** 0
- **Priority Score:** 30707.8
- **Functions:** 4/6 matched (target 14)
- **Missing functions:** `sub`, `sub_assign`
- **Types:** 0/1 matched (target 0)
- **Missing types:** `Output`

### 10. biguint.addition

- **Target:** `biguint.Addition`
- **Similarity:** 0.21
- **Dependents:** 0
- **Priority Score:** 30607.9
- **Functions:** 3/5 matched (target 13)
- **Missing functions:** `add`, `add_assign`
- **Types:** 0/1 matched (target 0)
- **Missing types:** `Output`

### 11. biguint.division

- **Target:** `biguint.Division`
- **Similarity:** 0.58
- **Dependents:** 0
- **Priority Score:** 12004.2
- **Functions:** 19/19 matched (target 31)
- **Missing functions:** _none_
- **Types:** 0/1 matched (target 0)
- **Missing types:** `Output`

### 12. bigint.bits

- **Target:** `bigint.Bits`
- **Similarity:** 0.50
- **Dependents:** 0
- **Priority Score:** 11805.0
- **Functions:** 17/17 matched (target 27)
- **Missing functions:** _none_
- **Types:** 0/1 matched
- **Missing types:** `Output`

### 13. bigint.division

- **Target:** `bigint.Division`
- **Similarity:** 0.50
- **Dependents:** 0
- **Priority Score:** 11205.0
- **Functions:** 11/11 matched (target 35)
- **Missing functions:** _none_
- **Types:** 0/1 matched (target 0)
- **Missing types:** `Output`

### 14. biguint.bits

- **Target:** `biguint.Bits`
- **Similarity:** 0.64
- **Dependents:** 0
- **Priority Score:** 10703.6
- **Functions:** 6/6 matched
- **Missing functions:** _none_
- **Types:** 0/1 matched (target 0)
- **Missing types:** `Output`

### 15. bigint.subtraction

- **Target:** `bigint.Subtraction`
- **Similarity:** 0.44
- **Dependents:** 0
- **Priority Score:** 10405.6
- **Functions:** 3/3 matched (target 30)
- **Missing functions:** _none_
- **Types:** 0/1 matched (target 0)
- **Missing types:** `Output`

### 16. bigint.multiplication

- **Target:** `bigint.Multiplication`
- **Similarity:** 0.52
- **Dependents:** 0
- **Priority Score:** 10404.8
- **Functions:** 3/3 matched (target 32)
- **Missing functions:** _none_
- **Types:** 0/1 matched (target 0)
- **Missing types:** `Output`

### 17. bigint.addition

- **Target:** `bigint.Addition`
- **Similarity:** 0.55
- **Dependents:** 0
- **Priority Score:** 10404.5
- **Functions:** 3/3 matched (target 32)
- **Missing functions:** _none_
- **Types:** 0/1 matched (target 0)
- **Missing types:** `Output`

### 18. biguint.monty

- **Target:** `biguint.Monty`
- **Similarity:** 0.83
- **Dependents:** 0
- **Priority Score:** 901.7
- **Functions:** 8/8 matched (target 9)
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_

### 19. biguint.serde

- **Target:** `biguint.Serde`
- **Similarity:** 0.43
- **Dependents:** 0
- **Priority Score:** 505.7
- **Functions:** 3/3 matched (target 6)
- **Missing functions:** _none_
- **Types:** 2/2 matched
- **Missing types:** _none_

### 20. biguint.shift

- **Target:** `biguint.Shift`
- **Similarity:** 0.82
- **Dependents:** 0
- **Priority Score:** 401.8
- **Functions:** 4/4 matched (target 9)
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 21. bigint.serde

- **Target:** `bigint.Serde`
- **Similarity:** 0.46
- **Dependents:** 0
- **Priority Score:** 205.4
- **Functions:** 2/2 matched (target 8)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 2)
- **Missing types:** _none_

### 22. bigint.power

- **Target:** `bigint.Power`
- **Similarity:** 0.94
- **Dependents:** 0
- **Priority Score:** 200.6
- **Functions:** 2/2 matched (target 7)
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 23. bigint.shift

- **Target:** `bigint.Shift`
- **Similarity:** 0.88
- **Dependents:** 0
- **Priority Score:** 101.2
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
