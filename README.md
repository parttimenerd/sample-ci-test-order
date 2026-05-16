# sample-ci-test-order

[![CI](https://github.com/parttimenerd/sample-ci-test-order/actions/workflows/ci.yml/badge.svg)](https://github.com/parttimenerd/sample-ci-test-order/actions)

A mini e-commerce order system demonstrating [test-order](https://github.com/parttimenerd/test-order)'s
three-tiered CI workflow.

## What is this?

This repo shows how `test-order` intelligently prioritizes tests in CI:

- **On main**: every push runs all tests in **learn mode**, building a dependency index
- **On PRs**: tests run in **three tiers** — change-affected first, then top-scored, then the rest
- **Locally**: developers download the CI-built index with `mvn test-order:download`

When a PR introduces a bug, **tier 1 catches it in seconds** instead of running the full suite.

## Demo Results

| PR | Description | Tier 1 | Tier 2 | Tier 3 | Time |
|----|-------------|--------|--------|--------|------|
| [#1](https://github.com/parttimenerd/sample-ci-test-order/pull/1) | Express shipping | ✓ | ✓ | ✓ | 37s |
| [#2](https://github.com/parttimenerd/sample-ci-test-order/pull/2) (buggy) | Money.percentage API change | ✗ **caught!** | skipped | skipped | 18s |
| [#2](https://github.com/parttimenerd/sample-ci-test-order/pull/2) (fixed) | Restore percentage division | ✓ | ✓ | ✓ | 28s |
| [#3](https://github.com/parttimenerd/sample-ci-test-order/pull/3) | Validation overhaul (3 files) | ✓ | ✓ | ✓ | 22s |
| [#4](https://github.com/parttimenerd/sample-ci-test-order/pull/4) (buggy) | Order cancellation (no inv. release) | ✗ **caught!** | skipped | skipped | ~20s |
| [#4](https://github.com/parttimenerd/sample-ci-test-order/pull/4) (fixed) | Release inventory on cancel | ✓ | ✓ | ✓ | 28s |

**Key takeaway:** both bugs were caught in **tier 1** within seconds. Tiers 2 and 3 were skipped entirely — no wasted CI time on unrelated tests.

## How the Three-Tiered Workflow Works

```
PR opened
  │
  ▼
Tier 1: run change-affected tests (via dependency analysis)
  │ FAIL? → stop immediately, report failure
  ▼
Tier 2: run top-scored remaining tests (by execution duration budget)
  │ FAIL? → stop
  ▼
Tier 3: run everything else
  │
  ▼
All green ✓
```

`test-order` uses instrumented learn runs on `main` to build a **dependency index** —
which test classes load which production classes. When a PR changes `Money.java`,
tier 1 automatically includes `MoneyTest`, `PricingEngineTest`, `ShippingCalculatorTest`,
`OrderProcessorTest`, and any other test that transitively depends on `Money`.

## Quick Start

```bash
# Clone and build
git clone https://github.com/parttimenerd/sample-ci-test-order.git
cd sample-ci-test-order

# Download the CI-built dependency index
mvn test-order:download

# Run tests — they'll be ordered by relevance to your changes
mvn test

# See the current test ranking
mvn test-order:show
```

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

## CI Workflow

See [`.github/workflows/ci.yml`](.github/workflows/ci.yml) for the full workflow.

The CI summary tab on each PR run shows the test-order tier breakdown —
check the [Actions tab](https://github.com/parttimenerd/sample-ci-test-order/actions) to see it in action.
