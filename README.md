# Full Stack E-Commerce Application

A full stack online store built with **Spring Boot**, **Spring Data JPA**, **MySQL** and a **HTML/CSS/JavaScript** frontend.
The frontend is served by Spring Boot itself, so there is only one application to run.

## Screenshots

| Store | Cart |
|---|---|
| ![Store](docs/screenshots/store.png) | ![Cart](docs/screenshots/cart.png) |

| My Orders | Admin panel |
|---|---|
| ![Orders](docs/screenshots/orders.png) | ![Admin](docs/screenshots/admin.png) |

## Features

- **Products:** list, search, filter by category, stock tracking
- **Categories:** products grouped by category
- **Authentication:** register and login, passwords hashed with BCrypt, roles `USER` and `ADMIN`
- **Cart:** add and remove items per user, with a stock check
- **Orders:** checkout in one `@Transactional` method (validates stock, reduces stock, saves the price at purchase time, clears the cart) and order history
- **Admin panel:** add or delete products, view all orders, update order status
- **Error handling:** clean JSON errors such as `{"error": "Cart is empty"}`
- **Demo data:** 3 categories, 10 products and an admin user are created on first start

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 17, Spring Boot 3, Spring Web, Spring Data JPA, Bean Validation |
| Security | BCrypt password hashing (spring-security-crypto) |
| Database | MySQL 8 (H2 in-memory option for quick demos) |
| Frontend | HTML, CSS, JavaScript (fetch API) |
| Build | Maven (Maven Wrapper included) |

## Architecture

```
Browser (HTML/JS)  ->  Controller  ->  Service  ->  Repository  ->  MySQL
                       (REST/JSON)    (business     (Spring Data
                                       logic)        JPA)
```

```
src/main/java/com/uday/ecommerce
  entity/      Product, Category, User, CartItem, Order, OrderItem
  repository/  Spring Data JPA interfaces
  service/     ProductService, OrderService
  controller/  Product, Category, Auth, Cart, Order
  config/      AppConfig (BCrypt bean), DataSeeder (demo data)
  exception/   GlobalExceptionHandler
src/main/resources
  application.properties       MySQL settings
  application-h2.properties    in-memory database profile
  static/                      index.html, style.css, app.js
```

## Getting Started

### Requirements
- JDK 17 or newer (`java -version`)
- MySQL 8 (or use the H2 option below)

### Run with MySQL
1. Clone the repository:
   ```
   git clone https://github.com/UdayKiranBatchalakuri/ecommerce-springboot.git
   cd ecommerce-springboot
   ```
2. Open `src/main/resources/application.properties` and set your MySQL password:
   ```
   spring.datasource.password=YOUR_MYSQL_PASSWORD
   ```
   The `ecommerce_db` database is created automatically.
3. Start the app:
   - Windows: `.\mvnw.cmd spring-boot:run`
   - Mac/Linux: `./mvnw spring-boot:run`
4. Open http://localhost:8080

### Run with H2 (no MySQL needed)
Data is kept in memory and lost when the app stops.
```
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=h2"
```

### Demo accounts
- **Admin:** `admin@store.com` / `admin123`
- **Customer:** click Register on the login page

## REST API

| Method | URL | Purpose |
|---|---|---|
| GET | `/api/products?q=&page=0&size=12` | List and search products |
| GET | `/api/products/{id}` | Get one product |
| POST / PUT / DELETE | `/api/products[/{id}]` | Create, update, delete a product |
| GET / POST | `/api/categories` | List or create categories |
| POST | `/api/auth/register` | Register a user |
| POST | `/api/auth/login` | Log in |
| GET | `/api/cart/{userId}` | View cart |
| POST | `/api/cart/{userId}/add?productId=1&qty=1` | Add to cart |
| DELETE | `/api/cart/item/{itemId}` | Remove a cart item |
| POST | `/api/orders/{userId}/checkout` | Place an order from the cart |
| GET | `/api/orders/{userId}` | Order history |
| GET | `/api/orders/all` | All orders (admin view) |
| PUT | `/api/orders/{orderId}/status?status=SHIPPED` | Update order status |

## Database Tables

`users`, `categories`, `products`, `cart_items`, `orders`, `order_items`

## Future Improvements

- Spring Security with JWT and role-based access control
- Payment integration (Razorpay test mode)
- DTOs for requests and responses
- Docker and deployment on AWS (EC2 and RDS)
- React frontend

## Author

**Batchalakuri Uday Kiran**
B.Tech CSE, VVIT Guntur
GitHub: [UdayKiranBatchalakuri][https://github.com/UdayKiranBatchalakuri]
