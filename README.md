# Brokage Firm API 

A SpringBoot based backend service for a brokage platform that allows users to manage assets and orders, with admin capabilities to match orders.

---

## Requirements / Tech Stack

- Java 17
- Maven 3.6+
- Spring Boot 3.x
- H2 db

---

## About initial user(s) creation
At least 1 admin user is required for the API (to call admin-only and other customer services). A user definition parameter is required to create the users in startup. The format is ``username:password:isAdmin``. application-dev.properties includes 2 user definitions to be created; one admin and one regular customer:

```
users=admin:adminpass:true,user:userpass:false
```
So, you can use admin:adminpass credentials to test the admin-only and customer endpoints, and user:userpass to test customer endpoints.
Similar to JWT Secret key or datasource credentials, ``users`` variable  should NOT be included in the properties in production mode. 
Please see **To run the project/Production Mode** section below for instructions on how to define it in production.  


## To build the project

**Maven wrapper included in the application, so you can build, run tests or the application by using mvnw executable.**

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


### Production Mode
Before starting the application with prod profile, set the following environment variables:

| Variable       | Description               |
| -------------- | ------------------------- |
| DS_USERNAME    | Database username         |
| DS_PASSWORD    | Database password         |
| JWT_SECRET_KEY | Secret key for JWT tokens |
| USERS          | Predefined users to create| |

```
DS_USERNAME=db_user DS_PASSWORD=db_pass JWT_SECRET_KEY=mysecret USERS=admin:pass:true,user:pass:false ./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```
The app will start on http://localhost:8080 (Update application.properties to change server port: ```server.port=8080```)

## API Endpoints

### Login
```
POST /api/login
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
- Include returned token in all requests using the Authorization header. Otherwise you will get 401 UNAUTHORIZED error.
```
Authorization: Bearer <your_token>
```
- Default token expiry is 180 minutes. You can update it in application/properties (```jwt.expiryMinutes=180```).


### Create Customer (Optional service)
```
POST /api/customers
{
  "username": "someuser",
  "password": "somepassword",
  "isAdmin": false
}

RETURNS:

201 CREATED
{
    "id": 1,
	"username": "someuser",
	"isAdmin": false
}

or

400 BAD REQUEST (invalid request parameter)
{
    "message": "Non-blank username required", // or "Non-blank password required"
    "timestamp": ...
}

400 BAD REQUEST (Existing user)
{
    "message": "Customer: someuser already exists",
    "timestamp": ...
}
```

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

201 CREATED
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

400 BAD REQUEST (invalid request parameter)
{
    "message": "Non-blank assetName required", // or "Positive size required" ...
    "timestamp": ...
}

404 NOT FOUND (Missing required asset)
{
    "message": "Asset: USD not found for customer: 1",
    "timestamp": ...
}

400 BAD REQUEST (Insufficient Funds)
{
    "message": "Customer: 1 does not have: 100.0000 USD",
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

200 OK
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

401 UNAUTHORIZED (Order owner is someone else)
{
    "message": "Order is not owned by the current user",
    "timestamp": ...
}
```

### Match Order
```
PUT /api/orders/<orderId>

RETURNS:

200 OK
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
- A default admin account (username: admin, password: adminpass) created in dev profile on app startup. Please see **About initial user(s) creation** section for details.

### List Orders
```
GET /api/orders[?startDate=2025-04-01T08:00:00&endDate=2025-04-10T18:00:00]

RETURNS:
200 OK
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


### Create Assets (Optional service)
```
POST /api/assets
{
    "customerId": 1,
    "assetName": "USD",
    "size": 100,
    "usableSize": 100
}

RETURNS:

201 CREATED
{
    "id": 1,
    "customerId": 1,
    "assetName": "USD",
    "size": 100,
    "usableSize": 100
}

or

401 UNAUTHORIZED (Admin privileges required)
{
    "message": "Admin privileges required",
    "timestamp": ...
}

404 NOT FOUND (Customer not found)
{
    "message": "Customer 1 not found",
    "timestamp": ...
}

400 BAD REQUEST (Asset already exists for customer)
{
    "message": "Asset: USD already exists for customer: 1",
    "timestamp": ...
}
```


### List Assets of Current User
```
GET /api/assets 

RETURNS:

200 OK
[
    {
        "id": 1,
        "customerId": 1,
        "assetName": "USD",
        "size": 100,
        "usableSize": 100
    }
    ...
]

```

### List Assets (of any user)
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
    "message": "Only asset owner or admins can list customer's assets",
    "timestamp": ...
}
```