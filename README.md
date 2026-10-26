# Train Ticket Reservation System

A console-based Train Ticket Reservation System built using **Java, MySQL, and JDBC**. This project demonstrates train searching, ticket booking, PNR generation, ticket cancellation, seat availability management, and database operations.

## Features

- View all available trains, routes, fares, and seats.
- Search trains by source and destination.
- Book tickets for one or more seats.
- Generate a unique PNR number for every booking.
- Check booking status using the PNR number.
- Cancel tickets and restore seat availability.
- View booking history.
- Store train and booking information in MySQL.
- Use prepared statements to handle SQL inputs.
- Use database transactions to maintain booking consistency.
- Automatically create database tables and insert sample trains.

## Technologies Used

- Java 17 or later
- MySQL 8.x
- JDBC (Java Database Connectivity)
- MySQL Connector/J
- IntelliJ IDEA

## Project Structure

```text
Train-Ticket-Reservation-System/
├── TrainTicketReservation.java
└── README.md
```

## Database Setup

### Step 1: Create the database

Open MySQL Workbench and execute:

```sql
CREATE DATABASE train_reservation;
```

### Step 2: Configure the database connection

Open `TrainTicketReservation.java` and update the following constants with your MySQL credentials:

```java
private static final String DB_URL =
        "jdbc:mysql://localhost:3306/train_reservation";

private static final String DB_USER = "root";

private static final String DB_PASSWORD = "YOUR_MYSQL_PASSWORD";
```

Replace `YOUR_MYSQL_PASSWORD` with your local MySQL password. Never upload real passwords or other secrets to GitHub.

### Step 3: Install MySQL Connector/J

Download the JDBC driver from the official website:

https://dev.mysql.com/downloads/connector/j/

Add the downloaded JAR file to your Java project's dependencies in IntelliJ IDEA.

The program automatically creates the required tables and inserts sample train records when the train table is empty.

## How to Run

1. Install JDK 17 or later.
2. Install and start MySQL Server.
3. Create the `train_reservation` database.
4. Add MySQL Connector/J to your project's dependencies.
5. Configure your database username and password.
6. Open `TrainTicketReservation.java` in IntelliJ IDEA.
7. Run the `main()` method.

## Application Menu

```text
=== TRAIN TICKET RESERVATION SYSTEM ===

1. View trains
2. Search trains
3. Book ticket
4. Check PNR
5. Cancel ticket
6. View all bookings (admin/demo)
0. Exit
```

## How the System Works

### 1. View Trains

Displays train IDs, names, source cities, destination cities, fares, and available seats.

### 2. Search Trains

Allows users to search for direct trains by entering their source and destination cities.

### 3. Book Tickets

The user selects a train and enters the passenger's name, age, and required number of seats. The system checks availability, calculates the total fare, generates a PNR, saves the booking, and updates seat availability.

### 4. Check PNR Status

Users enter their PNR number to view passenger details, train information, fare, booking time, and current status.

### 5. Cancel Tickets

The system marks the booking as cancelled and returns the reserved seats to the train's available seat count.

### 6. View Bookings

Displays stored bookings, including PNR, passenger name, train, number of seats, fare, and status.

## Database Tables

### Trains Table

Stores train information:

- Train ID
- Train name
- Source city
- Destination city
- Fare
- Total seats
- Available seats

### Bookings Table

Stores reservation information:

- Booking ID
- PNR number
- Train ID
- Passenger name
- Passenger age
- Number of seats
- Total fare
- Booking status
- Booking timestamp

## Sample Train Data

The application inserts fictional sample trains when the train table is empty, including:

| Train Name | Source | Destination |
|---|---|---|
| Godavari Express | Hyderabad | Visakhapatnam |
| Charminar Express | Hyderabad | Chennai |
| East Coast Express | Visakhapatnam | Chennai |
| Konark Express | Bhubaneswar | Hyderabad |

These records are for demonstration purposes only.

## Concepts Practiced

- Java classes, methods, loops, and conditional statements
- Exception handling
- JDBC database connectivity
- SQL queries and prepared statements
- Primary keys and foreign keys
- SQL joins
- CRUD operations
- Database transactions and row locking
- Seat availability and fare calculations

## Limitations

- This is an educational project, not a real railway reservation service.
- Train schedules, routes, and fares are sample data.
- The application stores one lead passenger per booking, even when multiple seats are reserved.
- The booking-history option has no administrator authentication.
- Travel dates, coach selection, berth allocation, payments, and refunds are not implemented.
- Cancellation releases seats but does not process a refund.

## Future Enhancements

- Add individual passenger records for group bookings.
- Add travel dates, departure times, classes, and berth allocation.
- Implement user registration and secure authentication.
- Add an administrator dashboard to manage trains.
- Add payment integration and refund workflows.
- Build a graphical interface or web application.
- Add automated tests and logging.

## Disclaimer

This project is intended for learning Java, JDBC, MySQL, and database-backed application development. It is not affiliated with Indian Railways and must not be used for actual ticket reservations.

## Author

**Avinash Kollasani**

GitHub: https://github.com/kolasaniavinash
