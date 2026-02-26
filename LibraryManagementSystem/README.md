# Library Management System

A full-stack library management system built with **Java**, **JavaFX**, and **MongoDB**. Features a client-server architecture with a rich GUI for browsing, checking out, and managing books.

## Features

- **Account Management** — Create accounts with email verification (via SendGrid), password hashing with SHA-256 and per-user salting, and password reset functionality
- **Real-Time Search** — Search the catalog by title with live filtering on each keystroke
- **Book Checkout & Hold System** — Check out available books, place holds, and receive automatic email notifications when held books become available
- **Role-Based Access Control** — Dedicated Librarian account with privileges to add and remove books from the catalog
- **Concurrent Access** — Synchronized database operations with collection-level locking to prevent race conditions (e.g., two users checking out the same book simultaneously)
- **Persistent Storage** — MongoDB backend with separate collections for user accounts and library items

## Architecture

```
┌─────────────────┐          Socket (Port 4242)          ┌─────────────────┐
│     Client       │ ◄──────────────────────────────────► │     Server       │
│                  │                                      │                  │
│  JavaFX GUI      │         ObjectOutputStream /         │  Reader Thread   │
│  Writer Thread   │─────►   ObjectInputStream    ◄───────│  Writer Thread   │
│  Reader Thread   │                                      │  MongoDB x2      │
│                  │                                      │  SendMail        │
└─────────────────┘                                      └─────────────────┘
```

The system uses a multi-threaded socket-based architecture. Each client connection spawns a dedicated Reader thread on the server that acts as the controller, routing requests to the appropriate MongoDB collection or mail service. Two separate MongoDB instances are used per connection — one for the login collection and one for the library collection — enabling concurrent access across collections without blocking.

## Project Structure

```
LibraryManagementSystem/
├── client/
│   └── src/
│       ├── ClientPack/
│       │   ├── Client.java              # Entry point, sets up networking and GUI
│       │   ├── Reader.java              # Listens for server messages, acts as model
│       │   ├── Writer.java              # Sends messages to server
│       │   └── GUIClasses/
│       │       ├── GUI.fxml / GUIController.java                     # Login screen
│       │       ├── SignUp.fxml / SignUpController.java                # Account creation
│       │       ├── ForgotPassword.fxml / ForgotPasswordController.java
│       │       ├── LoggedIn.fxml / LoggedInController.java           # User library view
│       │       ├── LibrarianLoggedIn.fxml / LibrarianLoggedInController.java
│       │       ├── LibraryItem.fxml / LibraryItemController.java     # Book card component
│       │       ├── LibraryItemDetails.fxml / LibraryItemDetailsController.java
│       │       └── AddBook.fxml / AddBookController.java             # Librarian add book
│       └── Common/
│           └── LoginMessage.java         # Shared message class for authentication
├── server/
│   └── src/
│       ├── ServerPack/
│       │   ├── Server.java              # Entry point, accepts client connections
│       │   ├── Reader.java              # Per-client handler, routes requests
│       │   ├── Writer.java              # Sends messages to client
│       │   ├── MongoDB.java             # Database access layer
│       │   └── SendMail.java            # SendGrid email integration
│       └── Common/
│           └── LoginMessage.java
├── jars/
│   ├── ClientSide.jar                   # Pre-built client executable
│   └── ServerSide.jar                   # Pre-built server executable
└── README.md
```

## Prerequisites

- **Java 11+** with JavaFX SDK
- **MongoDB** running locally (default connection)
- **SendGrid API Key** configured in `SendMail.java` for email verification

## Getting Started

### Option 1: Run from JARs

```bash
# Start the server first
java -jar jars/ServerSide.jar

# Then start one or more clients
java -jar jars/ClientSide.jar
```

### Option 2: Run from Source

1. Start your local MongoDB instance
2. Configure your SendGrid API key in `server/src/ServerPack/SendMail.java`
3. Compile and run the server:
   ```bash
   cd server/src
   javac ServerPack/Server.java
   java ServerPack.Server
   ```
4. Compile and run the client:
   ```bash
   cd client/src
   javac --module-path /path/to/javafx-sdk/lib --add-modules javafx.controls,javafx.fxml ClientPack/Client.java
   java --module-path /path/to/javafx-sdk/lib --add-modules javafx.controls,javafx.fxml ClientPack.Client
   ```

## User Guide

### Creating an Account
1. Click **Sign Up** from the login screen
2. Fill in all required fields with a valid email address
3. Enter the 6-digit verification code sent to your email
4. Log in with your new credentials (password is case-sensitive)

### Browsing the Library
- Browse available books in the scrollable catalog
- Use the **search bar** to filter by title in real time
- Click a book title for more details
- **Check Out** a book to add it to your personal library
- **Hold** a book that's currently checked out by someone else
- View your holds and checked-out books via **Hold List** and **Personal Library**

### Librarian Features
- Log in with a librarian-designated account
- **Add books** to the catalog (separate multiple authors/genres with commas)
- **Delete books** that are not currently checked out
- All standard browsing and search features are available

## Technical Details

### Synchronization
- The `SendMail` function is synchronized to prevent concurrent email sends
- Two collection-level locks on the server ensure thread-safe MongoDB access
- Operations on different collections (logins vs. library items) can proceed in parallel
- Operations on the same collection are serialized to prevent data corruption

### Security
- Passwords are hashed with SHA-256 and a unique per-user salt before storage
- Email verification is required for account creation and password reset
- Server-side validation for duplicate emails and usernames

## Tech Stack

| Component       | Technology                |
|----------------|---------------------------|
| Language        | Java                     |
| GUI Framework   | JavaFX (FXML + Controllers) |
| Database        | MongoDB                  |
| Email Service   | SendGrid                 |
| Communication   | Java Sockets + ObjectStreams |

## License

This project was developed for EE422C at The University of Texas at Austin (Spring 2023).
