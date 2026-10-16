# sample-ci-test-order

[![CI](https://github.com/parttimenerd/sample-ci-test-order/actions/workflows/ci.yml/badge.svg)](https://github.com/parttimenerd/sample-ci-test-order/actions)

A mini e-commerce order system demonstrating [test-order](https://github.com/parttimenerd/test-order)'s
intelligent CI workflow — affected tests run first, then the rest only if they passed.

## What is this?

This repo shows how `test-order` prioritizes tests in CI:

- **Affected tests first**: change-impacted tests run immediately via dependency analysis
- **Remaining tests after**: the rest of the suite runs only if affected tests passed
- **Fail fast**: a bug in changed code stops CI before running unrelated tests

When a PR introduces a bug, **affected tests catch it in seconds** instead of running the full suite.

## Demo Results

| PR | Description | Affected | Remaining | Result |
|----|-------------|----------|-----------|--------|
| [#5](https://github.com/parttimenerd/sample-ci-test-order/pull/5) | Add overnight shipping | 2 tests | 15 tests | ✓ all green |
| [#6](https://github.com/parttimenerd/sample-ci-test-order/pull/6) (buggy) | Money precision "optimization" | 11 tests — **caught!** | skipped | ✗ fail |
| [#6](https://github.com/parttimenerd/sample-ci-test-order/pull/6) (fixed) | Restore 2dp precision | 11 tests | 6 tests | ✓ all green |

**Key takeaway:** PR #6's bug was caught by the affected step alone — `ShippingCalculatorTest` failed
because it transitively depends on `Money.multiply()`. The remaining 6 tests never ran.

## CI Workflow

Two steps on every PR, one learn run on every main push:

```yaml
      # On main: build the dependency index
      - name: "Learn: full instrumented run"
        if: github.ref == 'refs/heads/main'
        run: mvn test -Dtestorder.mode=learn

      # On PRs: affected first, then the rest
      - name: Run affected tests
        if: github.ref != 'refs/heads/main'
        run: |
          mvn test-order:affected test \
            -Dtestorder.changeMode=since-last-commit

      - name: Run remaining tests (if affected passed)
        if: github.ref != 'refs/heads/main'
        run: mvn test-order:run-remaining test
```

See [`.github/workflows/ci.yml`](.github/workflows/ci.yml) for the full workflow.
[`.github/workflows/ci-tiered.yml`](.github/workflows/ci-tiered.yml) shows the same pattern as a named reference.

## Architecture

```
me.bechberger.shop
├── model/          Money, Product, Customer, Address, OrderItem, Order
├── service/        ProductCatalog, PricingEngine, InventoryManager,
│                   ShippingCalculator, OrderProcessor, NotificationService
├── validation/     EmailValidator, AddressValidator, OrderValidator
└── util/           MoneyFormatter, StringSanitizer
```

17 source classes, 17 test classes, 118 test methods.

## How test-order selects affected tests

`test-order` records which test classes loaded which production classes during a learn run on main.
When a PR changes `Money.java`, the affected step automatically runs every test that
transitively depends on `Money` — no configuration needed.

```
Change Money.java
  → Affected: MoneyTest, PricingEngineTest, ShippingCalculatorTest,
              OrderProcessorTest, MoneyFormatterTest, NotificationServiceTest, OrderItemTest, ...
  → Remaining: AddressTest, CustomerTest, StringSanitizerTest, ...
```
