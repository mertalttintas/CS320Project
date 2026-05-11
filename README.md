# 🚗 CRS — Car Rental System

A Java desktop application for managing vehicle reservations, developed as part of the CS320 Software Engineering course at Ozyegin University.

---

## 📋 Table of Contents

- [About](#about)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Getting Started](#getting-started)
- [Project Structure](#project-structure)
- [Architecture](#architecture)
- [Testing](#testing)
- [Known Limitations](#known-limitations)
- [Team](#team)

---

## About

CRS is a role-based car rental management system that supports three user roles: **Customer**, **Fleet Manager**, and **Admin**. The system allows customers to search and reserve vehicles, fleet managers to manage listings and approve reservations, and administrators to oversee users, listings, and generate platform-wide reports.

---

## Features

### 👤 Customer
- Register and log in with role-based access
- Search vehicles by location, price range, type, and date range
- Make, view, and cancel reservations
- Process payments (credit card / installment)
- Leave ratings and reviews for completed rentals
- View reservation history and total spending

### 🚙 Fleet Manager
- Add, edit, and archive vehicle listings
- Approve or reject incoming reservation requests
- View financial dashboard with total earnings

### 🛠️ Admin
- Manage user accounts (add, edit, deactivate, delete)
- View and suspend/remove vehicle listings
- Access platform-wide reports (total revenue, most rented brand, total users, pending reservations)
- Export reports as `.txt` files

### 🔒 Security
- Passwords hashed with **SHA-256** before database storage
- Account lockout after **3 consecutive failed login attempts**
- Role-Based Access Control (RBAC) — each role sees only its own dashboard
- SQL injection prevention via **PreparedStatements** throughout all DAO classes

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| UI | Java Swing |
| Database | MySQL Server 8.0+ |
| DB Driver | mysql-connector-j 9.7.0 |
| IDE | IntelliJ IDEA / Eclipse |
| Unit Testing | JUnit 5, EclEmma |
| Static Analysis | PMD |

---

## Getting Started

### Prerequisites

- JDK 17+
- MySQL Server 8.0+
- IntelliJ IDEA or Eclipse

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/your-username/CRS.git
   cd CRS
   ```

2. **Set up the database**
   - Create a MySQL database named `db_crs`
   - Run `DDL.sql` to create the schema, then `DML.sql` to seed initial data:
   ```sql
   source path/to/DDL.sql
   source path/to/DML.sql
   ```

3. **Configure the database connection**

   Open `DBConnection.java` and update your credentials:
   ```java
   private static final String URL = "jdbc:mysql://localhost:3306/db_crs?allowPublicKeyRetrieval=true&useSSL=false";
   private static final String USER = "your_username";
   private static final String PASSWORD = "your_password";
   ```

4. **Add the MySQL driver to your classpath**

   The `mysql-connector-j-9.7.0.jar` is included in the project root. Add it to your IDE's build path.

5. **Run the application**
   - Run `Main.java` — the login screen will launch automatically.

---

## Project Structure

```
CS320Project-main/
├── Main.java                    # Entry point — launches LoginScreen
├── LoginScreen.java             # Login UI + AuthController integration
├── RegisterScreen.java          # User registration UI
│
├── AdminDashboard.java          # Admin UI (user mgmt, reports, listings)
├── AdminController.java         # Admin business logic (stats queries)
│
├── CustomerDashboard.java       # Customer UI (search, reserve, history)
│
├── FleetManagerDashboard.java   # Fleet Manager UI (vehicles, reservations)
├── FleetController.java         # Fleet business logic (add/update vehicle)
│
├── AvailabilityCalendar.java    # Date availability UI component
│
├── AuthController.java          # Login, role verification, hashing
├── PasswordHasher.java          # SHA-256 password hashing utility
├── ReservationManager.java      # Reservation logic (conflict check, pricing)
│
├── DBConnection.java            # MySQL connection singleton
│
├── ReservationDAO.java          # DB operations for reservations
├── UserDAO.java                 # DB operations for users
├── VehicleDAO.java              # DB operations for vehicles
│
├── IBusinessLogic.java          # Marker interface for controllers
├── IReservationDAO.java         # Interface for ReservationDAO
├── IUserDAO.java                # Interface for UserDAO
├── IVehicleDAO.java             # Interface for VehicleDAO
├── IUserInterface.java          # Interface for UI screens
│
├── Reservation.java             # Reservation model
├── User.java                    # User model
├── Vehicle.java                 # Vehicle model
├── Review.java                  # Review model
│
├── DDL.sql                      # Database schema
├── DML.sql                      # Seed data
└── mysql-connector-j-9.7.0.jar  # MySQL JDBC driver
```

---

## Architecture

The project follows a **layered architecture**:

```
UI Layer         →  *Dashboard.java, LoginScreen, RegisterScreen
Controller Layer →  AuthController, AdminController, FleetController, ReservationManager
DAO Layer        →  UserDAO, VehicleDAO, ReservationDAO
Model Layer      →  User, Vehicle, Reservation, Review
Database         →  MySQL (db_crs)
```

Key design decisions:
- **DAO pattern** with interfaces (`IUserDAO`, `IVehicleDAO`, `IReservationDAO`) for loose coupling
- **PreparedStatements** used exclusively — no string-concatenated SQL
- **ACID-compliant transactions** in `ReservationDAO.saveReservation()` via manual commit/rollback
- **Pricing formula** in `ReservationManager`: `(basePrice × days + $50 insurance) × 1.18 tax`

---

## Testing

### Unit Tests (JUnit 5)

All **10 unit tests pass** with 0 errors and 0 failures:

| Test Class | Result |
|---|---|
| MainTest | ✅ Pass |
| UserTest | ✅ Pass |
| VehicleTest | ✅ Pass |
| ReservationTest | ✅ Pass |
| LoginScreenTest | ✅ Pass |
| RegisterScreenTest | ✅ Pass |
| AvailabilityCalendarTest | ✅ Pass |
| AdminDashboardTest | ✅ Pass |
| CustomerDashboardTest | ✅ Pass |
| FleetManagerDashboardTest | ✅ Pass |

### Manual Tests (STR)

**27 manual functional tests** were executed — **96.3% passed (26/27)**. One test (T-CRS-024, concurrent load with JMeter) was marked Not Run due to local deployment constraints.

Two bugs were found and fixed during testing:
- **Major**: Password reset stored plain text — fixed by enforcing SHA-256 hashing before storage
- **Minor**: Account lockout not implemented after 3 failed attempts — feature added and retested

### Coverage

Overall instruction coverage: **15.1%** (EclEmma). Low due to large Swing GUI and database-dependent DAO classes which cannot be exercised by standard unit tests. Effective coverage of testable business logic: **~58.6%**.

---

## Known Limitations

- **No live server deployment** — runs as a local Java desktop app connected to a local MySQL instance
- **Concurrent user testing not performed** — Apache JMeter load test was not applicable in the local environment
- **DAO classes require a live DB** — `ReservationDAO`, `UserDAO`, `VehicleDAO` need a real MySQL connection; Mockito integration would improve testability
- **DB credentials hardcoded** in `DBConnection.java` — move to a config file or environment variables before any production use

---

## Team

Developed by students of Ozyegin University — CS320 Software Engineering, Spring 2026.

| Name |
|---|
| Kaan Gönenli |
| Fatih Kaan Bostan |
| Mert Altıntaş |
| Çetin Hoşafçı |
| Berrin Aydoğmuş |

---

*Ozyegin University — School of Engineering, CS320 Software Engineering*
