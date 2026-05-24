# Bakery Product Requirements Document (PRD)

## 1. Overview

Bakery is a sample bakery operations application for managing orders, products,
users, and delivery analytics. The current implementation is built with Vaadin 8,
Spring Boot, Spring Security, and Spring Data JPA, but this PRD focuses on
observable behavior and core business rules rather than framework-specific APIs.

This document is based on the current implementation in this repository and on
the running application available at `http://localhost:8080/`.

Terminology in this PRD follows the glossary in
[Ubiquitous Language](UbiquitousLanguage.md).

## 2. Goals and Non-Functional Requirements

Functional goals inferred from the implementation:

- Provide a role-based UI for day-to-day bakery order handling.
- Allow staff to search upcoming orders and create new ones.
- Allow staff to edit orders, add order items, and advance order state.
- Provide an admin-only area for maintaining products and users.
- Provide a dashboard with operational delivery and sales statistics.
- Maintain an audit trail of order lifecycle changes.

Non-functional requirements implied by the current codebase:

- The application must require authentication for all app routes except static
  assets and the login page.
- The UI must support keyboard-driven interaction for core flows such as login,
  search, and form editing.
- The system must tolerate concurrent edits through optimistic locking on all
  persisted entities.
- Monetary values must avoid floating-point rounding errors by storing prices as
  integer cents.
- The application should remain self-contained in development with seeded demo
  data and an embedded H2 database.

## 3. High-Level Architecture

See also:

- [Application architecture](ApplicationArchitecture.md)
- [Data model](DataModel.md)
- [Ubiquitous language](UbiquitousLanguage.md)

At a high level, Bakery consists of:

- A Vaadin application shell with role-aware navigation.
- Views for storefront, dashboard, order editing, and admin CRUD screens.
- Services that encapsulate order logic, CRUD behavior, password encoding, and
  lookup data.
- Spring Data JPA repositories for persistence and dashboard queries.
- A relational data model centered on `Order`, `OrderItem`, `Product`, `User`,
  `Customer`, `PickupLocation`, and `HistoryItem`.

## 4. Authentication and Default Routing

### 4.1 Login Flow

- The system shall present an unauthenticated login page at `/login.html`.
- The login page shall include:
  - Email input.
  - Password input.
  - Sign in action.
  - A static credential hint panel.
- The currently implemented credential hint panel displays:
  - `admin@vaadin.com + admin`
  - `barista@vaadin.com + barista`
- Authentication shall be form-based and session-backed.
- Failed authentication shall redirect back to the login page with the configured
  failure URL.
- Successful authentication shall redirect through a custom success handler.

### 4.2 Route Protection

- Static Vaadin resources under `/VAADIN/**` shall be publicly accessible.
- All other application URLs shall require an authenticated user with one of the
  configured authorities: `admin`, `baker`, or `barista`.
- Admin CRUD views shall require the `admin` role through Vaadin secured-view
  access control.
- If a user tries to open a secured view without permission, the application
  shall show the configured access denied view.

### 4.3 Default Landing Rules

- When the user opens the base route without an explicit fragment:
  - `admin` users shall be sent to the Dashboard view.
  - non-admin users shall be sent to the Storefront view.
- If a specific route fragment already exists, the application shall preserve it
  instead of forcing the default route.

## 5. Application Shell and Navigation

- The main shell shall show the application title `Bakery`.
- The shell shall expose menu actions for:
  - Storefront
  - Dashboard
  - Users
  - Products
  - Log out
- Menu item visibility shall depend on whether the current user has access to
  the target view.
- The shell shall show the active view name when a view is displayed.
- Logging out shall invalidate the HTTP session and reload the page.
- Before logout or view changes that leave editable forms, the application shall
  respect leave-confirmation logic when unsaved changes exist.

Observed role-dependent navigation in the running app:

- Admin sees Storefront, Dashboard, Users, Products, and Log out.
- Barista sees Storefront, Dashboard, and Log out.

## 6. Storefront

The Storefront is the operational order queue for non-admin users and is also
accessible to admins.

### 6.1 Entry Behavior

- Entering the Storefront shall show a search toolbar and an orders grid.
- For non-admin users, Storefront is the default landing view after login.

### 6.2 Search and Filtering

- The toolbar shall include:
  - A search field with placeholder `Search`.
  - A search action button.
  - An `Include past` checkbox.
- Search shall filter orders by customer full name.
- When `Include past` is not selected, the order list shall be limited to orders
  after the current date.
- When `Include past` is selected, older orders shall also be included.
- Pressing `Enter` inside the search panel shall trigger the search action.
- The current search state shall be reflected in the view parameters.

### 6.3 Order List Behavior

- The storefront grid shall show order rows with due date, due time, customer,
  and a summary of line items.
- Selecting an order row shall navigate to the order edit view for that order.
- The `New` action shall navigate to the order creation flow.

## 7. Order Editing and Lifecycle

The order editor is the core transactional screen of the application and is
available at the `order` view route.

### 7.1 Order Form

- The order form shall allow editing:
  - Due date
  - Due time
  - Pickup location
  - Customer full name
  - Customer phone number
  - Customer details
  - Order items
  - Order state where applicable
- Due date shall not allow dates before today.
- Due time choices shall be limited to hourly values between `07:00` and
  `16:00` inclusive.
- A new order shall default to:
  - state `NEW`
  - due date of tomorrow
  - due time `08:00`
  - the default pickup location
  - an empty customer and item collection

### 7.2 Order Items

- An order may contain one or more order items.
- Each order item shall include:
  - Product
  - Quantity
  - Optional comment
- Quantity shall be constrained to the implemented validation range.
- The UI shall recalculate and display the order total when order items change.

### 7.3 Modes and Save Flow

- The order editor shall support distinct modes for editing, confirmation, and
  reporting/progression.
- Creating a new order with valid data shall first move to a confirmation step
  before the order is saved.
- Saving a new order shall create an initial history entry `Order placed` in
  state `NEW`.
- Opening an existing order shall load its persisted history and current state.
- The persisted order model contains a paid flag, but the current order editor
  does not expose that field as a primary user-editable control.

### 7.4 State Progression and History

- The order lifecycle states are:
  - `NEW`
  - `CONFIRMED`
  - `READY`
  - `DELIVERED`
  - `PROBLEM`
  - `CANCELLED`
- The normal happy-path progression shall be:
  - `NEW -> CONFIRMED -> READY -> DELIVERED`
- Changing state shall append a history item with the acting user and timestamp.
- History shall also support free-form comments added by the user.

## 8. Dashboard

The Dashboard provides an operational overview and trend reporting.

### 8.1 Summary Cards

- The dashboard shall show summary cards for:
  - due today
  - not available today
  - new orders
  - due tomorrow

### 8.2 Charts

- The dashboard shall show a chart for deliveries in the current month.
- The dashboard shall show a chart for deliveries in the current year.
- The dashboard shall show a multi-year sales trend chart.
- The dashboard shall show a product split chart for delivered products in the
  current month.

### 8.3 Due Orders Grid

- The dashboard shall include a due orders grid for operational follow-up.
- Selecting an order from the due grid shall navigate to that order in the order
  editor.

## 9. Admin - Product Management

The product administration view is admin-only.

- The view shall present a grid of products.
- The grid shall expose at least the columns:
  - name
  - price
- The form shall support creating, updating, and deleting products.
- Product price shall be edited through a currency-aware converter while being
  stored internally as integer cents.
- The view shall support text filtering through the shared CRUD search field.
- Unsaved changes shall trigger the shared CRUD leave-confirmation behavior.

## 10. Admin - User Management

The user administration view is admin-only.

- The view shall present a grid of users.
- The grid shall expose at least the columns:
  - email
  - name
  - role
- The form shall support creating, updating, and deleting users.
- Password handling shall behave as follows:
  - For new users, password is required.
  - For existing users, leaving password empty means the password remains
    unchanged.
- User email shall be unique.
- The form shall include the user lock state.
- Unsaved changes shall trigger the shared CRUD leave-confirmation behavior.

## 11. Accessibility, Feedback, and Error Handling

- The UI shall use assistive notification configuration for accessibility
  messages.
- Key form components and charts shall include ARIA-related attributes where the
  implementation provides them.
- Search and form interactions shall be keyboard-operable.
- Application errors during requests shall be logged through the UI error
  handler.
- Unauthorized view access shall render an `Access denied` screen.

## 12. Seed Data and Demo Assumptions

The default development experience depends on seeded demo data.

- If the database already contains users, demo data generation shall be skipped.
- Otherwise the application shall seed:
  - three demo users: admin, baker, and barista
  - ten generated products
  - two pickup locations: `Store` and `Bakery`
  - roughly two years of generated order history plus near-future orders
- Generated orders shall include:
  - randomized customers
  - randomized line items and quantities
  - a generated lifecycle history aligned with the selected final state

## 13. Current Implementation Constraints

- The login page currently exposes only admin and barista example credentials,
  even though a baker user is also seeded.
- The app uses a single-module Maven build rather than separate UI/backend
  modules.
- The current implementation is server-side rendered with Vaadin 8 rather than a
  client-heavy SPA architecture.
- Localization and external integrations are not a major concern in the current
  implementation.
