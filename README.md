# Brokage Firm API 

A SpringBoot based backend service for a brokage platform that allows users to manage assets and orders, with admin capabilities to match orders.

---

## Requirements / Tech Stack

- Java 17
- Maven 3.6+
- Spring Boot 3.x
- H2 db

---

## To build the project
```
./mvnw clean install
```

## To run the tests
```
./mvnw test
```

## To run the project

### Development Mode (default)

To run the app locally in development (dev) mode:

```
./mvnw spring-boot:run
```
Or explicitly specify the dev profile:
```
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

You may update application.properties line: ```spring.profiles.active=dev```

**Note**: H2 in-memory database is used, so application.properties has: ```spring.jpa.hibernate.ddl-auto=update``` and required tables (Order, Asset, and Customer) are auto created. Remove or comment out this line to change this behaviour.

### Production Mode
Before starting the application with prod profile, set the following environment variables:

| Variable       | Description               |
| -------------- | ------------------------- |
| DS_USERNAME    | Database username         |
| DS_PASSWORD    | Database password         |
| JWT_SECRET_KEY | Secret key for JWT tokens |

```
DS_USERNAME=myuser DS_PASSWORD=mypass JWT_SECRET_KEY=mysecret ./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```
The app will start on http://localhost:8080 (Update application.properties to change ```server.port=8080```)

## API Endpoints

### Login
```
POST /api/auth/login
{
  "username": "john",
  "password": "password123"
}

RETURNS:

Http 200 OK
{
    "token": ....
}

or

Http 401 UNAUTHORIZED
{
    "message": "Invalid credentials",
    "timestamp": ...
}

```
- Include returned token in all requests using the Authorization header. Otherwise you will get 401 UNAUTHORIZED.
```
Authorization: Bearer <your_token>
```
- Default token expiry is 180 minutes. You can update it in application/properties.

### Create Order
```
POST /api/orders
{
  "assetName": "USD",
  "side": "SELL" //or "BUY",
  "size": 100,
  "price": 32.0
}

RETURNS:

200 OK
{
    "id": 1,
    "customerId": 1,
    "assetName": "USD",
    "side": "SELL",
    "status": "PENDING",
    "size": 100,
    "price": 32.0,
    "createDate": ...
}

or

404 NOT FOUND (Missing required asset)
{
    "message": "Asset USD not found for customer 1",
    "timestamp": ...
}

400 BAD REQUEST (Insufficient Funds)
{
    "message": "Customer 1 does not have 100.0000 USD",
    "timestamp": ...
}
```
Notes: 
- TRY asset is mandatory for both BUY and SELL orders. 
- If asset is missing for a BUY order, it will be created. However, you cannot SELL a missing asset (results in 404 error). 
- You may use decimals for size and price.

### Delete Order
```
DELETE /api/orders/<orderId>

RETURNS:

{
    "id": 1,
    "customerId": 1,
    "assetName": "USD",
    "side": "SELL",
    "status": "CANCELED",
    "size": 100,
    "price": 32.0,
    "createDate": ...
}

or

404 NOT FOUND (Order with <orderId> & PENDING status not found)
{
    "message": "Pending Order 1 not found.",
    "timestamp": ...
}

401 UNAUTHORIZED (Order is not owned)
{
    "message": "Order is not owned by the current user",
    "timestamp": ...
}
```

### Match Order
```
PUT /api/orders/<orderId>

RETURNS:

{
    "id": 1,
    "customerId": 1,
    "assetName": "USD",
    "side": "SELL",
    "status": "MATCHED",
    "size": 100,
    "price": 32.0,
    "createDate": ...
}

or

401 UNAUTHORIZED (Admin privileges required)
{
    "message": "Admin privileges required",
    "timestamp": ...
}
```
Notes:
- This is an admin-only end-point. Admin users have isAdmin = true flag in customer table. 
- a default admin account (username: admin, password: admin) created in dev profile on app startup. You can also use H2 console (localhost:8080/h2-console) to create/update users.

### List Orders
```
GET /api/orders[?startDate=2025-04-01T08:00:00&endDate=2025-04-10T18:00:00]

RETURNS:
[
    {
        "id": 1,
        "customerId": 1,
        "assetName": "USD",
        "side": "SELL",
        "status": "MATCHED",
        "size": 100,
        "price": 32.0,
        "createDate": ...
    }
    ...
]

```
Note:
- startDate and endDate params are optional. By default, orders in last 365 days are returned.


### List Assets of Current User
```
GET /api/assets 

RETURNS:
[
    {
        "id": 1,
        "customerId": 1,
        "assetName": "USD",
        "side": "SELL",
        "size": 100,
        "usableSize": 100
    }
    ...
]

```

### List Assets 
```
GET /api/assets/<customerId>

RETURNS:
[
    {
        "id": 1,
        "customerId": 1,
        "assetName": "USD",
        "side": "SELL",
        "size": 100,
        "usableSize": 100
    }
    ...
]

or

401 UNAUTHORIZED (Admin privileges required)
{
    "message": "Only customer or admins can list customer's assets",
    "timestamp": ...
}
```
Note:
- This is an admin endpoint. Only admin user can list assets of anyone. 
