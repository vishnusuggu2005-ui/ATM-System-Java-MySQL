# ATM Management System

A Java-based **ATM Management System** using JDBC and MySQL that simulates basic ATM operations through a console interface.

## Features

- User login with account number and PIN
- Check account balance
- Deposit money
- Withdraw money
- View transaction history
- Change ATM PIN
- Store account and transaction details in MySQL
- Database transactions for deposits and withdrawals

## Technologies Used

- **Programming Language:** Java
- **Database:** MySQL
- **Database Connectivity:** JDBC
- **IDE:** Visual Studio Code (VS Code)

## Project Structure

```text
ATMManagementSystem/
├── src/
│   └── ATMManagementSystem.java
├── database/
│   └── atm_db.sql
└── README.md
```

## Prerequisites

- JDK 17 or later
- MySQL Server
- MySQL Workbench (optional)
- Visual Studio Code
- Extension Pack for Java
- MySQL Connector/J JDBC driver

## Installation and Execution

### 1. Clone the Repository

```bash
git clone YOUR_GITHUB_REPOSITORY_URL
cd ATMManagementSystem
```

Replace `YOUR_GITHUB_REPOSITORY_URL` with your GitHub repository URL. Alternatively, download and extract the project ZIP file.

### 2. Set Up the Database

1. Open MySQL Workbench.
2. Open `database/atm_db.sql`.
3. Execute the SQL script to create the database, tables, and demo account.

### 3. Configure Database Credentials

Open `src/ATMManagementSystem.java` and update the MySQL password:

```java
private static final String DB_URL =
    "jdbc:mysql://localhost:3306/atm_db";

private static final String DB_USER = "root";
private static final String DB_PASSWORD = "YOUR_MYSQL_PASSWORD";
```

Replace the password with your local MySQL password.

### 4. Configure JDBC

Download MySQL Connector/J from:

https://dev.mysql.com/downloads/connector/j/

Add the downloaded JAR file to the project's referenced libraries in VS Code.

### 5. Run the Application

Open `src/ATMManagementSystem.java` in VS Code and click **Run** above the `main()` method.

Ensure that MySQL Server is running before starting the application.

## Demo Login Credentials

| Field | Value |
|---|---|
| Account Number | `1001001` |
| PIN | `1234` |
| Initial Balance | ₹5,000.00 |

These credentials are for local testing only.

## Learning Outcomes

- Java classes, methods, and object-oriented programming
- JDBC database connectivity
- SQL queries and prepared statements
- Database transaction management
- Input validation and exception handling
- Basic banking application logic

## Future Enhancements

- Java Swing graphical user interface
- Admin dashboard and account creation
- PIN encryption and login attempt limits
- ATM receipt generation
- Improved security and audit logging

## Security Disclaimer

This project is intended for educational purposes only. It stores demo PINs in plain text and is not suitable for real banking transactions. Never use real banking credentials or customer information.

## Author

**Your Name**

GitHub: https://github.com/vishnusuggu2005-ui
