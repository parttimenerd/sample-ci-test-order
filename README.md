# sample-ci-test-order

[![CI](https://github.com/parttimenerd/sample-ci-test-order/actions/workflows/ci.yml/badge.svg)](https://github.com/parttimenerd/sample-ci-test-order/actions)

A mini e-commerce order system demonstrating [test-order](https://github.com/parttimenerd/test-order)'s
intelligent CI workflow — affected tests run first, then the rest only if they passed.

## What is this?

This repo shows how `test-order` prioritizes tests in CI:

- **Affected tests first**: change-impacted tests run immediately via dependency analysis
- **Remaining tests after**: the rest of the suite runs only if affected tests passed
- **Fail fast**: a bug in changed code stops CI before running unrelated tests
- **Locally**: developers download the CI-built index with `mvn test-order:download`

When a PR introduces a bug, **affected tests catch it in seconds** instead of running the full suite first.

## Quick Start

```bash
git clone https://github.com/parttimenerd/sample-ci-test-order.git
cd sample-ci-test-order

# Download the CI-built dependency index
mvn test-order:download

# Run tests — affected tests run first
mvn test

# See the current test ranking
mvn test-order:show
```

## CI Workflow

Two steps on every PR:

```yaml
- name: Run affected tests
  run: |
    mvn test-order:affected test \
      -Dtestorder.changeMode=since-last-commit

- name: Run remaining tests (if affected passed)
  run: mvn test-order:run-remaining test
```

The index is built on main pushes and cached — PRs restore it automatically.
See [`.github/workflows/ci.yml`](.github/workflows/ci.yml) for the full workflow.

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
When a PR changes `Money.java`, the affected step automatically runs `MoneyTest`,
`PricingEngineTest`, `ShippingCalculatorTest`, `OrderProcessorTest`, and any other
test that transitively depends on `Money` — without any configuration.

```
Change Money.java
  → Affected: MoneyTest, PricingEngineTest, ShippingCalculatorTest,
              OrderProcessorTest, MoneyFormatterTest, NotificationServiceTest, OrderItemTest
  → Remaining: AddressTest, CustomerTest, ProductCatalogTest, ...
```
