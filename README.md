📚 Libris
Libris is a web-based library management system developed for the CENG106 Object-Oriented Programming course. The project focuses on managing book inventories, member records, and the lending/return processes using robust OOP principles.

🛠️ Tech Stack
Language: Java

Framework: Vaadin (Building web UIs directly with Java classes)

Database: MySQL

Database Connectivity: JDBC

Build Tool: Maven

IDE: Eclipse

🧬 OOP Implementation
The project is designed to demonstrate core Object-Oriented Programming concepts:

Inheritance: Specialized classes like Book, EBook, AudioBook, and Periodical inherit from a common LibraryItem base class.

Polymorphism: Each material type overrides methods for specific loan periods and penalty calculations.

Abstraction: Core functionalities are defined through interfaces such as Searchable, Borrowable, and Reviewable.

Design Patterns:

Observer Pattern: Used for the notification system.

Strategy Pattern: Implemented for dynamic penalty calculations and recommendation algorithms.

🌟 Key Features
Diverse Material Support: Management for physical books, e-books (file formats), audiobooks (durations), and periodicals (issue numbers).

Smart Penalty System: Automated penalty calculation based on delay duration and user history.

Reservation System: A queue-based system for borrowed items with automated notifications.

Personalized Recommendations: Suggests books based on user history and ratings.

Role-Based Access: * Admin: Manage inventory, track members, and view statistics.

Member: Search the catalog, borrow/return items, and manage personal reading lists.
