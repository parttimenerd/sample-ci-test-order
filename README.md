# sample-ci-test-order

A mini e-commerce order system demonstrating [test-order](https://github.com/parttimenerd/test-order)'s
three-tiered CI workflow.

## What is this?

This repo shows how `test-order` intelligently prioritizes tests in CI:

- **On main**: every push runs all tests in **learn mode**, building a dependency index
- **On PRs**: tests run in **three tiers** — change-affected first, then top-scored, then the rest
- **Locally**: developers download the CI-built index with `mvn test-order:download`

When a PR introduces a bug, **tier 1 catches it in seconds** instead of running the full suite.

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

17 source classes, 16 test classes, ~90 test methods.

_More details and CI run links coming soon._
