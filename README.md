This project is a RESTful API for managing bank accounts, supporting multi-currency balances, deposits, withdrawals, and
currency exchange.

### Technologies Used

- **Java 21**
- **Spring Boot 4.0.3**
- **Spring Data JPA**
- **Liquibase** (Database migrations)
- **PostgreSQL**
- **Lombok**
- **Docker**

### Getting Started

#### Prerequisites

- JDK 21
- Docker and Docker Compose

#### Running the Application

1. **Start the Database**:
   ```bash
   docker-compose up -d
   ```
2. **Run the Spring Boot application**:
   ```bash
   ./gradlew bootRun
   ```

### API Endpoints

The base URL for all endpoints is `/api/v1/bankaccount`.

#### 1. Create Account

Creates a new bank account.

- **URL**: `POST /api/v1/bankaccount`
- **Body**:
  ```json
  {
    "name": "John Doe"
  }
  ```
- **Returns**: `Long` (The ID of the created account)

#### 2. Deposit Money

Adds money to a specific account in a given currency.

- **URL**: `POST /api/v1/bankaccount/{id}/deposit`
- **Body**:
  ```json
  {
    "amount": 100.00,
    "currency": "EUR"
  }
  ```

#### 3. Withdraw Money (Debit)

Removes money from a specific account in a given currency.

- **URL**: `POST /api/v1/bankaccount/{id}/debit`
- **Body**:
  ```json
  {
    "amount": 50.00,
    "currency": "EUR"
  }
  ```

#### 4. Get Account Balances

Retrieves all currency balances for a specific account.

- **URL**: `GET /api/v1/bankaccount/{id}`
- **Returns**: A list of balances.
  ```json
  [
    {
      "currency": "EUR",
      "amount": 50.00
    },
    {
      "currency": "USD",
      "amount": 0.00
    }
  ]
  ```

#### 5. Currency Exchange

Exchanges an amount from one currency to another within the account.

- **URL**: `POST /api/v1/bankaccount/{id}/currency`
- **Body**:
  ```json
  {
    "fromCurrency": "EUR",
    "toCurrency": "USD",
    "amount": 10.00
  }
  ```

### Supported Currencies

- `EUR`
- `USD`
- `SEK`
- `GBP`

### Database

The application uses PostgreSQL. Configuration can be found in `src/main/resources/application.properties` and
`docker-compose.yml`. Database schema is managed via Liquibase migrations in `src/main/resources/db/changelog`.