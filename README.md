# Expense Tracker API

A production-grade REST API for tracking expenses, managing budgets, and analyzing spending patterns built with Spring Boot.

## Technology Stack

- **Backend**: Spring Boot 3.2.2
- **Database**: MongoDB (Atlas)
- **Cache**: Redis
- **Authentication**: JWT
- **Email**: SendGrid SMTP
- **Documentation**: Swagger/OpenAPI
- **Rate Limiting**: Bucket4j

## Prerequisites

- Java 17+
- Maven 3.8+
- MongoDB (Atlas connection provided)
- Redis (local or cloud)

## Quick Start

1. **Install Redis** (required for caching):
   ```bash
   # Windows: Download from https://github.com/microsoftarchive/redis/releases
   # Or use Docker: docker run -d -p 6379:6379 redis
   ```

2. **Build the project**:
   ```bash
   mvn clean install
   ```

3. **Run the application**:
   ```bash
   mvn spring-boot:run
   ```

4. **Access the API**:
   - API Base URL: `http://localhost:8080`
   - Swagger UI: `http://localhost:8080/swagger-ui.html`

## API Endpoints

### Authentication
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/auth/register` | Register new user |
| POST | `/api/v1/auth/login` | Login & get JWT |
| POST | `/api/v1/auth/refresh` | Refresh JWT token |

### Users
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/users/me` | Get current user |
| PUT | `/api/v1/users/me` | Update profile |
| PUT | `/api/v1/users/me/password` | Change password |

### Expenses
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/expenses` | Create expense |
| GET | `/api/v1/expenses` | List expenses (paginated) |
| GET | `/api/v1/expenses/{id}` | Get expense by ID |
| PUT | `/api/v1/expenses/{id}` | Update expense |
| DELETE | `/api/v1/expenses/{id}` | Delete expense |
| POST | `/api/v1/expenses/{id}/receipt` | Upload receipt |
| DELETE | `/api/v1/expenses/{id}/receipt` | Delete receipt |

### Categories
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/categories` | Create category |
| GET | `/api/v1/categories` | List categories |
| GET | `/api/v1/categories/{id}` | Get category |
| PUT | `/api/v1/categories/{id}` | Update category |
| DELETE | `/api/v1/categories/{id}` | Delete category |

### Budgets
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/budgets` | Create budget |
| GET | `/api/v1/budgets` | List budgets |
| GET | `/api/v1/budgets/{id}` | Get budget |
| PUT | `/api/v1/budgets/{id}` | Update budget |
| DELETE | `/api/v1/budgets/{id}` | Delete budget |
| GET | `/api/v1/budgets/{id}/status` | Get budget status |

### Analytics
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/analytics/summary` | Expense summary |
| GET | `/api/v1/analytics/by-category` | Category breakdown |
| GET | `/api/v1/analytics/by-month` | Monthly trends |
| GET | `/api/v1/analytics/trends` | Trend analysis |

### Currency
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/currency/rates` | Get exchange rates |
| GET | `/api/v1/currency/convert` | Convert currency |

## Features Implemented

### Mandatory Features
- [x] User registration & login
- [x] JWT authentication with refresh tokens
- [x] Role-based access (USER, ADMIN)
- [x] CRUD operations for expenses, categories, budgets
- [x] Pagination and sorting
- [x] Filtering by date, category, amount
- [x] Redis caching
- [x] File upload (receipts)
- [x] Email notifications (SendGrid)
- [x] API rate limiting (100 req/min)
- [x] Analytics APIs
- [x] Global exception handling
- [x] Input validation
- [x] Swagger documentation

### External Integrations
- [x] ExchangeRate-API (currency conversion)
- [x] SendGrid (email service)

## Project Structure

```
src/main/java/com/expensetracker/
├── config/          # Security, Redis, Swagger configs
├── controller/      # REST API controllers
├── dto/             # Request/Response DTOs
├── exception/       # Custom exceptions & handler
├── filter/          # JWT & Rate limit filters
├── model/           # MongoDB documents
├── repository/      # MongoDB repositories
├── service/         # Business logic
└── util/            # JWT utilities
```

## Testing the API

1. **Register a user**:
   ```bash
   curl -X POST http://localhost:8080/api/v1/auth/register \
     -H "Content-Type: application/json" \
     -d '{"email":"test@example.com","password":"password123","firstName":"Test","lastName":"User"}'
   ```

2. **Login**:
   ```bash
   curl -X POST http://localhost:8080/api/v1/auth/login \
     -H "Content-Type: application/json" \
     -d '{"email":"test@example.com","password":"password123"}'
   ```

3. **Create expense** (use token from login):
   ```bash
   curl -X POST http://localhost:8080/api/v1/expenses \
     -H "Authorization: Bearer YOUR_TOKEN" \
     -H "Content-Type: application/json" \
     -d '{"amount":50.00,"description":"Lunch","expenseDate":"2025-01-31"}'
   ```

## Configuration

Environment variables can override `application.yml`:
- `MONGODB_URI` - MongoDB connection string
- `REDIS_HOST` - Redis host
- `REDIS_PORT` - Redis port
- `SENDGRID_API_KEY` - SendGrid API key
- `JWT_SECRET` - JWT signing secret
- `EXCHANGE_RATE_API_KEY` - Currency API key

## Demo Video Checklist

1. User registration and login (JWT token shown)
2. Create/Update/Delete expenses
3. Category management
4. Budget creation and alerts
5. File upload (receipt)
6. Analytics APIs demonstration
7. Email notification (budget alert)
8. Rate limiting demonstration
9. Currency conversion API
10. Swagger documentation walkthrough
