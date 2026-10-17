# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Files Present:** 25/27 (92.6%)
- **Function parity:** 498/520 matched (target 978) — 95.8%
- **Class/type parity:** 15/36 matched (target 25) — 41.7%
- **Combined symbol parity:** 513/556 matched (target 1003) — 92.3%
- **Average inline-code cosine:** 0.27 (function body across 25 matched files)
- **Average documentation cosine:** 0.37 (doc text across 25 matched files)
- **Cheat-zeroed Files:** 0
- **Critical Issues:** 25 files with <0.60 function similarity
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
- **Similarity:** 0.28
- **Dependents:** 7
- **Priority Score:** 7018907.0
- **Functions:** 85/85 matched (target 141)
- **Missing functions:** _none_
- **Types:** 3/4 matched
- **Missing types:** `Bytes`
- **Tests:** 2/2 matched

### 2. bigint

- **Target:** `bigint.BigInt`
- **Similarity:** 0.31
- **Dependents:** 6
- **Priority Score:** 6031107.0
- **Functions:** 104/104 matched (target 263)
- **Missing functions:** _none_
- **Types:** 5/7 matched
- **Missing types:** `Output`, `Bytes`
- **Tests:** 3/3 matched

### 3. biguint.subtraction

- **Target:** `biguint.Subtraction`
- **Similarity:** 0.05
- **Dependents:** 0
- **Priority Score:** 91309.5
- **Functions:** 4/12 matched (target 20)
- **Missing functions:** `sub2rev`, `sub`, `sub_assign`, `sub`, `sub`, `sub_assign`, `sub`, `sub`
- **Types:** 0/1 matched (target 0)
- **Missing types:** `Output`

### 4. biguint.addition

- **Target:** `biguint.Addition`
- **Similarity:** 0.10
- **Dependents:** 0
- **Priority Score:** 71109.0
- **Functions:** 4/10 matched (target 20)
- **Missing functions:** `add`, `add_assign`, `add`, `add_assign`, `add`, `add`
- **Types:** 0/1 matched (target 0)
- **Missing types:** `Output`

### 5. biguint.multiplication

- **Target:** `biguint.Multiplication`
- **Similarity:** 0.27
- **Dependents:** 0
- **Priority Score:** 51607.3
- **Functions:** 11/15 matched (target 26)
- **Missing functions:** `mul`, `mul_assign`, `mul`, `mul`
- **Types:** 0/1 matched
- **Missing types:** `Output`
- **Tests:** 1/1 matched

### 6. bigint.convert

- **Target:** `bigint.Convert`
- **Similarity:** 0.33
- **Dependents:** 0
- **Priority Score:** 33406.7
- **Functions:** 31/31 matched (target 37)
- **Missing functions:** _none_
- **Types:** 0/3 matched (target 0)
- **Missing types:** `Err`, `FromStrRadixErr`, `Error`

### 7. bigint.bits

- **Target:** `bigint.Bits`
- **Similarity:** 0.19
- **Dependents:** 0
- **Priority Score:** 32008.1
- **Functions:** 17/19 matched (target 17)
- **Missing functions:** `bitand`, `bitor`
- **Types:** 0/1 matched (target 0)
- **Missing types:** `Output`

### 8. biguint.division

- **Target:** `biguint.Division`
- **Similarity:** 0.24
- **Dependents:** 0
- **Priority Score:** 23907.6
- **Functions:** 37/38 matched (target 43)
- **Missing functions:** `div_wide`
- **Types:** 0/1 matched (target 0)
- **Missing types:** `Output`

### 9. biguint.convert

- **Target:** `biguint.Convert`
- **Similarity:** 0.32
- **Dependents:** 0
- **Priority Score:** 23706.8
- **Functions:** 35/35 matched (target 64)
- **Missing functions:** _none_
- **Types:** 0/2 matched (target 1)
- **Missing types:** `Err`, `FromStrRadixErr`
- **Tests:** 2/2 matched

### 10. bigrand

- **Target:** `numbigint.BigRand`
- **Similarity:** 0.43
- **Dependents:** 0
- **Priority Score:** 22205.7
- **Functions:** 16/16 matched (target 24)
- **Missing functions:** _none_
- **Types:** 4/6 matched (target 5)
- **Missing types:** `X`, `Sampler`

### 11. biguint.bits

- **Target:** `biguint.Bits`
- **Similarity:** 0.09
- **Dependents:** 0
- **Priority Score:** 20809.1
- **Functions:** 6/7 matched (target 6)
- **Missing functions:** `bitand`
- **Types:** 0/1 matched (target 0)
- **Missing types:** `Output`

### 12. bigint.division

- **Target:** `bigint.Division`
- **Similarity:** 0.36
- **Dependents:** 0
- **Priority Score:** 14806.4
- **Functions:** 47/47 matched
- **Missing functions:** _none_
- **Types:** 0/1 matched (target 0)
- **Missing types:** `Output`

### 13. bigint.subtraction

- **Target:** `bigint.Subtraction`
- **Similarity:** 0.21
- **Dependents:** 0
- **Priority Score:** 12507.9
- **Functions:** 24/24 matched (target 51)
- **Missing functions:** _none_
- **Types:** 0/1 matched (target 0)
- **Missing types:** `Output`

### 14. bigint.addition

- **Target:** `bigint.Addition`
- **Similarity:** 0.18
- **Dependents:** 0
- **Priority Score:** 11908.2
- **Functions:** 18/18 matched (target 47)
- **Missing functions:** _none_
- **Types:** 0/1 matched (target 0)
- **Missing types:** `Output`

### 15. bigint.multiplication

- **Target:** `bigint.Multiplication`
- **Similarity:** 0.32
- **Dependents:** 0
- **Priority Score:** 11506.8
- **Functions:** 14/14 matched (target 45)
- **Missing functions:** _none_
- **Types:** 0/1 matched (target 0)
- **Missing types:** `Output`

### 16. biguint.power

- **Target:** `biguint.Power`
- **Similarity:** 0.33
- **Dependents:** 0
- **Priority Score:** 10906.7
- **Functions:** 8/8 matched (target 10)
- **Missing functions:** _none_
- **Types:** 0/1 matched
- **Missing types:** `Output`
- **Tests:** 2/2 matched

### 17. biguint.monty

- **Target:** `biguint.Monty`
- **Similarity:** 0.45
- **Dependents:** 0
- **Priority Score:** 905.5
- **Functions:** 8/8 matched (target 9)
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_

### 18. biguint.arbitrary

- **Target:** `biguint.Arbitrary`
- **Similarity:** 0.11
- **Dependents:** 0
- **Priority Score:** 508.9
- **Functions:** 5/5 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 19. biguint.iter

- **Target:** `biguint.Iter`
- **Similarity:** 0.18
- **Dependents:** 0
- **Priority Score:** 508.2
- **Functions:** 5/5 matched (target 19)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 3)
- **Missing types:** _none_
- **Tests:** 4/4 matched

### 20. bigint.arbitrary

- **Target:** `bigint.Arbitrary`
- **Similarity:** 0.27
- **Dependents:** 0
- **Priority Score:** 507.3
- **Functions:** 5/5 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 21. biguint.serde

- **Target:** `biguint.Serde`
- **Similarity:** 0.34
- **Dependents:** 0
- **Priority Score:** 506.6
- **Functions:** 3/3 matched (target 6)
- **Missing functions:** _none_
- **Types:** 2/2 matched
- **Missing types:** _none_

### 22. biguint.shift

- **Target:** `biguint.Shift`
- **Similarity:** 0.28
- **Dependents:** 0
- **Priority Score:** 407.2
- **Functions:** 4/4 matched (target 25)
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 23. bigint.serde

- **Target:** `bigint.Serde`
- **Similarity:** 0.33
- **Dependents:** 0
- **Priority Score:** 406.7
- **Functions:** 4/4 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 24. bigint.power

- **Target:** `bigint.Power`
- **Similarity:** 0.57
- **Dependents:** 0
- **Priority Score:** 204.3
- **Functions:** 2/2 matched (target 11)
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 25. bigint.shift

- **Target:** `bigint.Shift`
- **Similarity:** 0.34
- **Dependents:** 0
- **Priority Score:** 106.6
- **Functions:** 1/1 matched (target 33)
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

## Reexport / Wiring Modules

These files match `reexport_modules` patterns in `.ast_distance_config.json`. They are filtered out of
normal priority and missing-file ladders because they are wiring
modules, not direct logic ports. Consult them for call-site routing;
do not treat them as the next implementation target by default.

### Missing

| Source | Expected target | Deps | Source path | Expected path |
|--------|-----------------|------|-------------|---------------|
| `lib` | `Lib` | 0 | `lib.rs` | `Lib.kt` |
| `macros` | `Macros` | 0 | `macros.rs` | `Macros.kt` |

