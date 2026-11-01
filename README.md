# Full Stack E-Commerce Application

Spring Boot 3 (Java 17) + Spring Data JPA + MySQL + HTML/CSS/JavaScript frontend.
The frontend is served by Spring Boot itself, so there is only **one app to run**.

## Features
- **Products**: list, search, filter by category, add/update/delete (admin), stock tracking
- **Categories**
- **Auth**: register / login, passwords hashed with BCrypt, roles USER / ADMIN
- **Cart**: add, remove, view per user (with stock check)
- **Orders**: checkout is one `@Transactional` operation (validates stock, reduces stock, saves price at purchase time, clears cart), order history, admin can change order status
- **Global exception handling**: clean JSON errors like `{"error": "Cart is empty"}`
- Demo data is added on first start (3 categories, 10 products, 1 admin user)

## Architecture
```
Controller  ->  Service  ->  Repository  ->  Database
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
  application.properties       (MySQL settings)
  application-h2.properties    (in-memory DB profile)
  static/                      index.html, style.css, app.js
```

## Requirements
- JDK 17 or newer (`java -version`)
- Either **MySQL 8** or nothing (use the H2 option below)
- VS Code with "Extension Pack for Java" and "Spring Boot Extension Pack" (optional, you can also use the terminal)

## Run it (Option A: MySQL)
1. Open `src/main/resources/application.properties` and set `spring.datasource.password` to your MySQL root password.
   The database `ecommerce_db` is created automatically.
2. In a terminal inside this folder:
   - Windows: `.\mvnw.cmd spring-boot:run`
   - Mac/Linux: `./mvnw spring-boot:run`
   (the first run downloads Maven and the libraries, so it needs internet and takes a few minutes)
3. Open http://localhost:8080

## Run it (Option B: H2, no MySQL needed)
Data is kept in memory and is lost when you stop the app. Good for a quick demo.
- Windows PowerShell: `.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=h2"`
- Mac/Linux: `./mvnw spring-boot:run -Dspring-boot.run.profiles=h2`

## Run from VS Code
Open the folder, wait for the Java extension to finish importing the project, then press **F5**
and choose "Ecommerce (MySQL)" or "Ecommerce (H2 - no MySQL needed)".

## Demo login
- Admin: `admin@store.com` / `admin123` (shows an **Admin** tab: add/delete products, update order status)
- Or click **Register** to create a normal customer account.

## REST API
| Method | URL | Purpose |
|---|---|---|
| GET | `/api/products?q=&page=0&size=12` | List / search products |
| GET | `/api/products/{id}` | One product |
| POST / PUT / DELETE | `/api/products[/{id}]` | Create / update / delete product |
| GET / POST | `/api/categories` | List / create categories |
| POST | `/api/auth/register` | Register `{name,email,password}` |
| POST | `/api/auth/login` | Login `{email,password}` |
| GET | `/api/cart/{userId}` | View cart |
| POST | `/api/cart/{userId}/add?productId=1&qty=1` | Add to cart |
| DELETE | `/api/cart/item/{itemId}` | Remove cart item |
| POST | `/api/orders/{userId}/checkout` | Place order from cart |
| GET | `/api/orders/{userId}` | Order history |
| GET | `/api/orders/all` | All orders (admin view) |
| PUT | `/api/orders/{orderId}/status?status=SHIPPED` | Update order status |

## Known limitations / next steps
This version identifies the logged-in user by id stored in the browser, so the API is **not** secured by tokens.
Planned improvements:
1. Spring Security + JWT, with role-based access (ADMIN-only product and order management)
2. Payment integration (Razorpay test mode)
3. DTOs for requests/responses
4. Docker + deployment on AWS EC2/RDS
5. React frontend

## Troubleshooting
- `Access denied for user 'root'`: wrong MySQL password in `application.properties`.
- `Communications link failure`: MySQL service is not running (Windows: Services -> MySQL80 -> Start), or use Option B.
- `Port 8080 already in use`: change `server.port` in `application.properties`.
- Page shows "User not found" after you reset the database: click **Logout** and log in again.
