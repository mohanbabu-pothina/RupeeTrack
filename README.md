# RupeeTrack — Personal Expense Tracker

A personal expense tracker with an Angular frontend, Spring Boot REST API, and PostgreSQL database.

## Requirements
- JDK 17 or newer (JDK 21 recommended; your JDK 24 should work with Spring Boot 3.5)
- Node.js 20.19+ or 22.12+
- PostgreSQL 15+
- Maven (or use the included Maven wrapper if you add one)

## 1. Create the database
Open PostgreSQL / pgAdmin and run:

```sql
CREATE DATABASE rupeetrack;
```

Use a dedicated database user if possible. For local development, you can use an existing PostgreSQL username/password.

## 2. Configure the backend
Edit `backend/src/main/resources/application.properties` and set your PostgreSQL username/password:

```properties
spring.datasource.username=postgres
spring.datasource.password=YOUR_POSTGRES_PASSWORD
```

Do not commit real credentials to a public repository.

## 3. Run the backend
Open a terminal in `RupeeTrack/backend`:

```bash
mvn spring-boot:run
```

Backend runs at `http://localhost:8080`.
Test it at `http://localhost:8080/api/expenses`.

## 4. Run the frontend
Open another terminal in `RupeeTrack/frontend`:

```bash
npm install
npm start
```

Open `http://localhost:4200`.

## API endpoints
- `GET /api/expenses` — list expenses
- `GET /api/expenses/{id}` — get one expense
- `POST /api/expenses` — create expense
- `PUT /api/expenses/{id}` — update expense
- `DELETE /api/expenses/{id}` — delete expense

## Notes
- Expenses are stored in PostgreSQL.
- The monthly budget is currently a frontend setting (`₹20,000`) for the first version. Budget persistence and settings can be added next.
- This first version is intended for local personal use and has no login/authentication. Do not expose it publicly until authentication and security are added.
