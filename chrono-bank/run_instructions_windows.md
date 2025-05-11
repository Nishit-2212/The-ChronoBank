# ChronoBank System - Run Instructions for Windows 11

This document provides instructions on how to set up and run the ChronoBank Java application on a Windows 11 system with PostgreSQL.

## Prerequisites

1.  **Java Development Kit (JDK):** Version 8 or higher. We recommend OpenJDK 11 or 17 for better compatibility with modern tools. You can download it from [Adoptium Temurin](https://adoptium.net/) or Oracle Java SE Development Kit.
    *   Ensure `JAVA_HOME` environment variable is set and the JDK `bin` directory is added to your system `PATH`.
2.  **Apache Maven:** Version 3.6.x or higher. Download from [Apache Maven Project](https://maven.apache.org/download.cgi).
    *   Ensure `MAVEN_HOME` or `M2_HOME` environment variable is set and the Maven `bin` directory is added to your system `PATH`.
3.  **PostgreSQL Database Server:** Version 12 or higher. Download from [PostgreSQL Official Website](https://www.postgresql.org/download/).
    *   During installation, remember the password you set for the `postgres` superuser (or create a new user for the application).
4.  **pgAdmin 4 (Recommended):** A GUI tool for managing PostgreSQL databases. It usually comes with the PostgreSQL installer or can be downloaded separately from [pgAdmin Website](https://www.pgadmin.org/download/).
5.  **Git (Optional but Recommended):** For cloning the project if you receive it as a Git repository. Download from [Git SCM](https://git-scm.com/download/win).

## Project Files

You should have a zip file (`chrono-bank-project.zip` or similar) containing the following structure:

```
chrono-bank/
├── pom.xml
├── src/
│   └── main/
│       └── java/
│           └── com/
│               └── chronobank/
│                   ├── Main.java
│                   ├── dao/
│                   ├── db/
│                   ├── model/
│                   ├── pattern/
│                   ├── service/
│                   └── util/
├── target/
│   └── chrono-bank-1.0-SNAPSHOT.jar  (This is the executable JAR)
├── chrono_bank_schema.sql
├── chrono_bank_architecture.md
└── run_instructions_windows.md (this file)
```

## Setup Steps

### 1. Set up PostgreSQL Database

1.  **Start PostgreSQL Server:** Ensure your PostgreSQL service is running. You can check this in Windows Services (`services.msc`).
2.  **Create the Database:**
    *   Open pgAdmin 4.
    *   Connect to your PostgreSQL server.
    *   In the browser tree, right-click on `Databases` -> `Create` -> `Database...`.
    *   Enter `chronobank` as the database name.
    *   Click `Save`.
3.  **Run the Schema Script:**
    *   Select the newly created `chronobank` database in pgAdmin.
    *   Open the Query Tool (Tools -> Query Tool or click the SQL button).
    *   Open the `chrono_bank_schema.sql` file (provided with the project) in a text editor, copy its content.
    *   Paste the SQL content into the Query Tool.
    *   Execute the script (click the "Execute/Refresh" button, usually a lightning bolt icon, or press F5).
    *   This will create the necessary tables (`users`, `accounts`, `transactions`, etc.).
4.  **Verify Database Credentials:**
    *   The application is configured by default to connect to PostgreSQL using:
        *   Host: `localhost`
        *   Port: `5432`
        *   Database: `chronobank`
        *   Username: `postgres`
        *   Password: `password`
    *   If your PostgreSQL `postgres` user has a different password, or if you created a different user for this database, you need to update the credentials in the Java code.
    *   Open the file `chrono-bank/src/main/java/com/chronobank/db/DatabaseConnector.java`.
    *   Modify the `DB_URL`, `USER`, and `PASS` static final strings accordingly:
        ```java
        private static final String DB_URL = "jdbc:postgresql://localhost:5432/chronobank";
        private static final String USER = "your_postgres_username"; // e.g., postgres
        private static final String PASS = "your_postgres_password"; // The password you set
        ```
    *   If you modify `DatabaseConnector.java`, you will need to recompile the project (see Step 2 below).

### 2. Build the Java Project (if source code was modified or to ensure latest build)

If you modified any `.java` files (like `DatabaseConnector.java`) or if you want to build from source:

1.  Open a Command Prompt (cmd) or PowerShell.
2.  Navigate to the root directory of the project (the `chrono-bank` folder where `pom.xml` is located).
    ```bash
    cd path\to\chrono-bank
    ```
3.  Run the Maven build command:
    ```bash
    mvn clean package
    ```
4.  This command will clean previous builds, compile the source code, run any tests (if configured in `pom.xml`), and package the application into an executable JAR file. The JAR will be located at `chrono-bank/target/chrono-bank-1.0-SNAPSHOT.jar`.

### 3. Run the Application

1.  Open a Command Prompt (cmd) or PowerShell.
2.  Navigate to the root directory of the project (`chrono-bank`).
3.  Run the application using the following command:
    ```bash
    java -jar target/chrono-bank-1.0-SNAPSHOT.jar
    ```
4.  The application will start, and the `Main.java` class will execute its test scenarios. You will see output in the console indicating the progress of these scenarios, including user registrations, account creations, transactions, etc.
5.  **Important:** The application requires the PostgreSQL database to be running and accessible with the correct credentials as configured in `DatabaseConnector.java`.

## Expected Output

The console output will show:
*   Initialization messages.
*   Confirmation of database connection (or error messages if connection fails).
*   Step-by-step execution of test scenarios defined in `Main.java`:
    *   User registration and login attempts.
    *   Account creation for different users and types.
    *   Deposits, withdrawals, and transfers.
    *   Loan operations (disbursement, repayment).
    *   Investor account operations (making investments).
    *   Demonstrations of Observer and State patterns (e.g., notifications, account status changes).
    *   Transaction history display.
*   Final shutdown messages.

## Troubleshooting

*   **`mvn: command not found` or `java: command not found`:** Ensure JDK and Maven `bin` directories are in your system `PATH` environment variable and `JAVA_HOME`/`M2_HOME` are set correctly. Restart your Command Prompt/PowerShell after making changes to environment variables.
*   **Database Connection Errors (e.g., `Connection refused`, `password authentication failed`):**
    *   Verify PostgreSQL service is running.
    *   Double-check the host, port, database name, username, and password in `DatabaseConnector.java` and ensure they match your PostgreSQL setup.
    *   Ensure your PostgreSQL server is configured to accept TCP/IP connections on `localhost:5432` (check `postgresql.conf` and `pg_hba.conf`).
    *   Make sure the `chronobank` database exists and the schema has been applied.
*   **Compilation Errors during `mvn clean package`:**
    *   Check the console output for specific error messages. These usually point to issues in the Java code.
    *   Ensure you have the correct JDK version (8 or higher).
*   **`ClassNotFoundException` or `NoClassDefFoundError` when running the JAR:**
    *   This usually means the JAR was not built correctly or is missing dependencies. The `maven-shade-plugin` in `pom.xml` is configured to create an executable JAR with all dependencies included. Ensure the `mvn clean package` command completed successfully.

## Further Development

*   The project uses Maven for dependency management and building.
*   The main application logic and test scenarios are in `com.chronobank.Main.java`.
*   Domain models are in `com.chronobank.model`.
*   Data Access Objects (DAOs) are in `com.chronobank.dao`.
*   Service layer classes (including `BankingFacade` and `AccountFactory`) are in `com.chronobank.service`.

---
