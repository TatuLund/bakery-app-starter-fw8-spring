# Ubiquitous Language

This glossary defines the preferred business vocabulary for Bakery.

Use the canonical term in new requirements, documentation, and discussions.
Where the UI or implementation uses a slightly different label, the related
wording column captures that variation.

## Core terms

| Canonical term | Related terms / UI wording | Definition |
| --- | --- | --- |
| Bakery | Application, app | The overall order-management application in this repository. |
| Storefront | Order queue, operational list | The view where staff search upcoming orders and start new ones. Despite the name, it is not a public e-commerce storefront. |
| Dashboard | Analytics, stats view | The landing view for admins that summarizes deliveries, sales, and due orders. |
| Order | Bakery order | The central business aggregate representing a customer request for bakery products. |
| Order item | Line item | A single product entry inside an order, including quantity and optional comment. |
| Customer | Order customer | The person whose details are captured on an order. In the current model, customer data is stored per order rather than as a shared master profile. |
| Product | Bakery product | A sellable bakery item such as a cake, pastry, tart, or bread. |
| Pickup location | Pickup point | The place where the customer will collect the order, such as `Store` or `Bakery`. |
| Due date | Pickup date | The calendar date when the order is expected to be ready or collected. |
| Due time | Pickup time | The scheduled time slot for the order. |
| Paid flag | Paid | A boolean indicator stating whether the order has been paid. |
| Order history | Audit trail, history | The chronological record of comments and state transitions attached to an order. |
| History item | Audit entry | A single entry in the order history, including message, timestamp, actor, and optionally resulting state. |
| Order state | Status, lifecycle state | The current lifecycle position of an order. |
| Happy path | Normal flow | The intended forward progression `NEW -> CONFIRMED -> READY -> DELIVERED`. |
| Problem order | Problem | An order that cannot proceed normally and is marked with the `PROBLEM` state. |
| Cancelled order | Cancelled | An order that will not be fulfilled. |
| New order | New | An order that has been created but not yet confirmed by bakery staff. |
| Confirmed order | Confirmed | An order accepted for preparation. |
| Ready order | Ready | An order prepared and ready for pickup. |
| Delivered order | Delivered | An order that has been handed over to the customer. |
| Barista | Front-of-house user | A user role oriented toward storefront operations such as searching orders and creating new ones. |
| Baker | Bakery operations user | A user role oriented toward production-side order handling and status progression. |
| Admin | Administrator | A user role with access to admin CRUD screens in addition to operational views. |
| Locked user | Locked account | A persisted boolean state on a user record. |
| User management | Users admin | The admin screen for maintaining application users. |
| Product management | Products admin | The admin screen for maintaining products and prices. |
| Search term | Search | The free-text customer-name filter used in the storefront. |
| Include past | Include past | The storefront option that expands search results to include historical orders. |
| Confirmation mode | Confirmation step | The order editor mode shown before saving a newly created order. |
| Report mode | Progress mode | The order editor mode used for viewing an existing order and advancing its lifecycle. |

## Naming guidance

- Prefer `order` over broader retail terms such as `purchase` or `request`.
- Prefer `product` over narrower UI-specific labels such as item or article.
- Prefer `pickup location` when discussing fulfillment rather than store unless
  the specific seeded location is meant.
- Prefer role names `admin`, `baker`, and `barista` when discussing access
  control because those are the persisted authority values.
- Prefer `order history` for the audit trail and `history item` for a single
  entry.
- Use `Storefront` as the canonical view name because that is the visible menu
  label, while clarifying that it is an internal staff-facing view.
