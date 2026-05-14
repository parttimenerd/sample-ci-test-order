# Demo Story: test-order Three-Tiered CI

This document scripts the complete demo from empty repo to a fully
demonstrated three-tiered CI workflow, including intentionally failing PRs
that show test-order catching bugs fast.

---

## Application: Mini E-Commerce Order System

A realistic order processing system with natural cross-package dependency
chains — exactly the kind of codebase where intelligent test selection
shines.

### Architecture

```
me.bechberger.shop
├── model/                        ← Pure domain objects
│   ├── Money          value object: amount + currency, arithmetic
│   ├── Product        id, name, price, category, weight
│   ├── Customer       id, name, email, shipping address, loyalty
│   ├── Address        street/city/state/zip/country, shipping zone
│   ├── OrderItem      product + quantity, line total, weight
│   └── Order          customer, items, status lifecycle, totals
│
├── service/                      ← Business logic
│   ├── ProductCatalog       in-memory CRUD, search, filter
│   ├── PricingEngine        subtotal, bulk discount, tax
│   ├── InventoryManager     stock levels, reserve/release/confirm
│   ├── ShippingCalculator   zone-based rates, free-shipping threshold
│   ├── OrderProcessor       orchestrator: validate→price→reserve→ship→confirm
│   └── NotificationService  confirmation emails, receipts, shipping notices
│
├── validation/                   ← Input validation
│   ├── EmailValidator       RFC-lite pattern, domain extraction
│   ├── AddressValidator     required fields, country-specific ZIP rules
│   └── OrderValidator       stock + limits + address + email checks
│
└── util/                         ← Shared utilities
    ├── MoneyFormatter       locale-aware currency formatting
    └── StringSanitizer      HTML strip, special char escape, whitespace normalize
```

### Key Dependency Chains

These chains are what makes test-order's tiered selection valuable.
When a "leaf" class changes, all tests for classes that depend on it
get pulled into tier 1.

```
OrderProcessor ──→ PricingEngine ──→ Money, Product, MoneyFormatter
       │──→ InventoryManager
       │──→ ShippingCalculator ──→ Address, Money
       │──→ OrderValidator ──→ AddressValidator, EmailValidator, InventoryManager
       └──→ NotificationService ──→ MoneyFormatter, StringSanitizer

Example: change Money.java
  → Tier 1 picks up: MoneyTest, PricingEngineTest, ShippingCalculatorTest,
    OrderProcessorTest, MoneyFormatterTest, NotificationServiceTest
  → Tier 2/3: the rest (AddressTest, CustomerTest, ProductCatalogTest, ...)
```

### Test Matrix

16 test classes, ~5-8 tests each ≈ 90+ test methods.

| Test Class                | Tests | Depends On (via source) |
|---------------------------|-------|-------------------------|
| MoneyTest                 | 8     | Money |
| ProductTest               | 5     | Product, Money |
| CustomerTest              | 5     | Customer, Address |
| AddressTest               | 6     | Address |
| OrderItemTest             | 5     | OrderItem, Product, Money |
| OrderTest                 | 7     | Order, Customer, OrderItem, ... |
| ProductCatalogTest        | 6     | ProductCatalog, Product, Money |
| PricingEngineTest         | 7     | PricingEngine, Money, Order, MoneyFormatter |
| InventoryManagerTest      | 7     | InventoryManager |
| ShippingCalculatorTest    | 6     | ShippingCalculator, Address, Money, Order |
| OrderProcessorTest        | 8     | OrderProcessor, ALL services, ALL validators |
| NotificationServiceTest   | 6     | NotificationService, Order, MoneyFormatter, StringSanitizer |
| EmailValidatorTest        | 6     | EmailValidator |
| AddressValidatorTest      | 6     | AddressValidator, Address |
| OrderValidatorTest        | 7     | OrderValidator, AddressValidator, EmailValidator, InventoryManager |
| MoneyFormatterTest        | 5     | MoneyFormatter, Money |
| StringSanitizerTest       | 5     | StringSanitizer |

---

## CI Workflow

```yaml
on:
  push:
    branches: [main]     # → Learn mode: full instrumented run, builds index
  pull_request:           # → Three-tiered: fail-fast with prioritized tests
```

### On main push (learn)

```
mvn test -Dtestorder.mode=learn
```

Runs all tests with instrumentation, records which test classes load which
production classes. Uploads the dependency index as a CI artifact + cache.

### On pull request (three-tiered)

```
Step 1: mvn test-order:tiered-select test    ← Tier 1: change-affected
Step 2: mvn test-order:run-tier test -Dtier=2 ← Tier 2: top-scored remaining
Step 3: mvn test-order:run-tier test -Dtier=3 ← Tier 3: everything else
```

Each step only runs if the previous passed. A bug in tier 1 stops the
pipeline immediately — no wasted time on unrelated tests.

Change detection uses `since-last-commit` (git-based) so it sees exactly
what the PR commit changed.

Show output is written to `$GITHUB_STEP_SUMMARY` so the tier breakdown
is visible on the Actions run page.

---

## Execution Script

### Phase 1: Bootstrap on main (2 commits)

#### Commit 1 — "Initial project: mini e-commerce order system"

Everything lands at once:
- 16 source classes (model + service + validation + util)
- 16 test classes
- CI workflow, `.gitignore`, `download-config.yml`, `pom.xml`
- Skeleton `README.md`

```bash
git add -A && git commit -m "Initial project: mini e-commerce order system"
git push origin main
# Wait for CI learn run → green ✓
```

**CI does:** learn mode, records all dependency chains, uploads index.

#### Commit 2 — "Add loyalty discount to PricingEngine"

Small feature: customers with 5+ orders get 10% off.

Changes:
- `PricingEngine.java`: add `calculateLoyaltyDiscount(Customer, Money)`,
  call it in `priceOrder()`
- `PricingEngineTest.java`: add `testLoyaltyDiscount()` and
  `testNoLoyaltyDiscountForNewCustomer()`

```bash
git add -A && git commit -m "feat: add loyalty discount for repeat customers"
git push origin main
# Wait for CI learn run → green ✓
```

**CI does:** learn mode again. Now the index has 2 runs of data,
PricingEngine→Customer dependency is recorded.

---

### Phase 2: Pull Requests (the actual demo)

#### PR 1 — `feat/express-shipping` (all green ✓)

**Story:** Add express shipping (2x standard rate) to ShippingCalculator.

**Changes:**
- `ShippingCalculator.java`: add `calculateExpress(Order, Address)` method
- `ShippingCalculatorTest.java`: add 2 test methods for express rates

**Expected tier split:**
- Tier 1 (change-affected): `ShippingCalculatorTest` (direct change),
  `OrderProcessorTest` (depends on ShippingCalculator)
- Tier 2: top-scored remaining (~6 tests)
- Tier 3: rest (~6 tests)
- **All green** ✓✓✓

```bash
git checkout -b feat/express-shipping
# make changes
git add -A && git commit -m "feat: add express shipping option"
git push -u origin feat/express-shipping
gh pr create --title "feat: add express shipping option" --body "..."
# Wait for CI → all 3 tiers green
gh pr merge --squash --delete-branch
# Wait for learn run on main
```

**What this demonstrates:** basic three-tiered workflow working. Tier 1
correctly identifies ShippingCalculatorTest + OrderProcessorTest as
change-affected via dependency analysis.

---

#### PR 2 — `fix/pricing-rounding` (BUG → tier 1 catches it!)

**Story:** "Fix" a rounding issue in `Money.add()` — but accidentally
introduce an off-by-one that breaks `PricingEngine` totals.

**Changes (intentionally buggy):**
- `Money.java`: change rounding in `add()` — use `HALF_DOWN` instead
  of `HALF_UP`, which causes 1-cent differences in some edge cases
- `MoneyTest.java`: update one test to match the new (wrong) behavior

**Expected tier split:**
- Tier 1: `MoneyTest` (direct), `PricingEngineTest` (Money dep),
  `MoneyFormatterTest` (Money dep), `ShippingCalculatorTest` (Money dep),
  `OrderProcessorTest` (transitive), `NotificationServiceTest` (transitive),
  `OrderItemTest` (Money dep)
- **`PricingEngineTest.testBulkDiscountCalculation()` FAILS** ✗
  because the rounding change cascades through subtotal calculation
- Tier 2 and 3 are **skipped** (tier 1 failed)

```bash
git checkout main && git pull
git checkout -b fix/pricing-rounding
# make buggy changes
git add -A && git commit -m "fix: correct rounding in Money arithmetic"
git push -u origin fix/pricing-rounding
gh pr create --title "fix: correct rounding in Money arithmetic" --body "..."
# Wait for CI → TIER 1 FAILS ✗ (tier 2, 3 skipped)
```

**What this demonstrates:** 🎯 THE MONEY SHOT.
- Tier 1 caught the bug in ~3-5 seconds by running only the
  change-affected tests.
- Without test-order, the full suite would take ~25 seconds to find
  the same failure — and the failing test would be buried in the middle.
- The CI summary shows exactly which tier failed and which tests ran.

#### PR 2 (fix) — push a fix commit

```bash
# Fix Money.add() back to HALF_UP, update MoneyTest
git add -A && git commit -m "fix: restore HALF_UP rounding"
git push
# Wait for CI → all 3 tiers green ✓✓✓
gh pr merge --squash --delete-branch
# Wait for learn run on main
```

---

#### PR 3 — `refactor/validation-overhaul` (large change, all green ✓)

**Story:** Tighten validation — add phone validation to Customer,
stricter email checks, and refactor AddressValidator to support
more countries.

**Changes:**
- `AddressValidator.java`: add AU/JP ZIP patterns, refactor
  `validateZip()` to use a strategy map
- `EmailValidator.java`: add `isDisposableProvider()` check
- `OrderValidator.java`: add phone number validation using new
  Customer.getPhone() (or just tighten quantity limits)
- `AddressValidatorTest.java`: add 3 new test methods
- `EmailValidatorTest.java`: add 2 new test methods
- `OrderValidatorTest.java`: add 2 new test methods

**Expected tier split:**
- Tier 1 (large!): `AddressValidatorTest`, `EmailValidatorTest`,
  `OrderValidatorTest` (all directly changed), `OrderProcessorTest`
  (depends on validators)
- Tier 2: small (most affected tests already in tier 1)
- Tier 3: unaffected (Money, Product, ProductCatalog, etc.)
- **All green** ✓✓✓

```bash
git checkout main && git pull
git checkout -b refactor/validation-overhaul
# make changes
git add -A && git commit -m "refactor: stricter validation, more country support"
git push -u origin refactor/validation-overhaul
gh pr create --title "refactor: tighten validation rules" --body "..."
# Wait for CI → all 3 tiers green
gh pr merge --squash --delete-branch
# Wait for learn run on main
```

**What this demonstrates:** when many files change, tier 1 is large
and tier 2/3 are small. test-order correctly identifies the blast radius.

---

#### PR 4 — `feat/order-cancellation` (BUG → tier 1 catches it again!)

**Story:** Add order cancellation. But forget to call
`InventoryManager.release()` — cancelled orders don't restore stock.

**Changes (intentionally buggy):**
- `Order.java`: add `cancel()` method that sets status to CANCELLED
- `OrderProcessor.java`: add `cancelOrder(Order)` that calls
  `order.cancel()` but does NOT call `inventoryManager.release()`
- `OrderProcessorTest.java`: add `testCancelOrder()` (status check — passes)
  and `testCancelRestoresStock()` (checks inventory — FAILS because
  release was forgotten)

**Expected tier split:**
- Tier 1: `OrderTest` (Order changed), `OrderProcessorTest` (changed),
  plus transitive deps
- **`OrderProcessorTest.testCancelRestoresStock()` FAILS** ✗
- Tier 2, 3 skipped

```bash
git checkout main && git pull
git checkout -b feat/order-cancellation
# make buggy changes
git add -A && git commit -m "feat: add order cancellation"
git push -u origin feat/order-cancellation
gh pr create --title "feat: add order cancellation" --body "..."
# Wait for CI → TIER 1 FAILS ✗
```

**What this demonstrates:** another real-world catch. A new feature
that's partially implemented — the happy path works but a side effect
(inventory release) was forgotten. Tier 1 catches it because
OrderProcessorTest is change-affected.

#### PR 4 (fix)

```bash
# Add inventoryManager.release() call in cancelOrder()
git add -A && git commit -m "fix: release inventory on cancellation"
git push
# Wait for CI → all green ✓✓✓
gh pr merge --squash --delete-branch
```

---

### Phase 3: Local Download (developer workflow)

After at least one learn run on main, any developer can pull the CI-built
dependency index locally — no need to run learn mode themselves.

```bash
# Download the latest index from CI artifacts
mvn test-order:download

# Now local test runs use intelligent ordering automatically
mvn test
# → tests that touch your changed files run first
```

This works via `.test-order/download-config.yml`:

```yaml
ci:
  github:
    owner: parttimenerd
    repo: sample-ci-test-order
    workflow: ci.yml
    artifact-name: test-order-deps
    branch: main
```

**What to demonstrate:**
1. Clone the repo fresh (or delete `.test-order/test-dependencies.lz4`)
2. Run `mvn test-order:download` — prints the artifact it fetched
3. Run `mvn test-order:show` — shows ranked test order based on CI data
4. Edit a file (e.g. `Money.java`), run `mvn test` — Money-related tests
   run first without ever having run learn mode locally

**What this demonstrates:** the "zero setup" developer experience.
New team members clone, run `mvn test-order:download`, and immediately
get intelligent test ordering from CI's learned dependency data.
They never need to run a slow instrumented learn build locally.

---

### Phase 4: Final README

Update README with:
1. What this repo demonstrates (three-tiered CI with test-order)
2. Architecture diagram (the ASCII tree above)
3. Dependency chain visualization
4. **Local download instructions** — `mvn test-order:download` for zero-setup ordering
5. Links to each CI run:
   - Main learn runs (commits 1-2, post-merge runs)
   - PR 1: green three-tiered run
   - PR 2: **red tier-1 run** + fixed green run
   - PR 3: green with large tier 1
   - PR 4: **red tier-1 run** + fixed green run
5. Time comparison: "tier 1 found the bug in 4s, full suite takes 25s"
6. Copy-paste instructions to adopt the pattern

---

## Summary of CI Runs

| # | Trigger | Type | Result | Key Observation |
|---|---------|------|--------|-----------------|
| 1 | main push | Learn | ✓ | Baseline index created |
| 2 | main push | Learn | ✓ | Index updated with loyalty discount deps |
| 3 | PR 1 | Tiered | ✓✓✓ | Tier 1 correctly picked ShippingCalculator+OrderProcessor |
| 4 | main merge | Learn | ✓ | Index updated with express shipping |
| 5 | PR 2 | Tiered | ✗ skip skip | **Tier 1 caught Money rounding bug** |
| 6 | PR 2 fix | Tiered | ✓✓✓ | Bug fixed, all tiers pass |
| 7 | main merge | Learn | ✓ | Index updated |
| 8 | PR 3 | Tiered | ✓✓✓ | Large tier 1 (many files changed) |
| 9 | main merge | Learn | ✓ | Index updated with validation changes |
| 10 | PR 4 | Tiered | ✗ skip skip | **Tier 1 caught missing inventory release** |
| 11 | PR 4 fix | Tiered | ✓✓✓ | Bug fixed |
| 12 | main merge | Learn | ✓ | Final index |

Total: ~12 CI runs, 2 of which demonstrate tier-1 failure detection.

---

## File Inventory

- 16 source classes + 16 test classes = 32 Java files
- ~2500 lines of Java
- 1 CI workflow, 1 pom.xml, 1 download-config.yml, 1 .gitignore, 1 README
- 4 PRs (2 with fix commits) + 2 direct main pushes
