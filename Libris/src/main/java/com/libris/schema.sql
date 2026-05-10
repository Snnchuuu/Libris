-- ============================================================
-- Libris Library Management System - Database Schema
-- ============================================================

-- Database Initialization
CREATE DATABASE IF NOT EXISTS libris_db;
USE libris_db;

-- -------------------------------------------------------
-- TABLE: users
-- Stores both Admin and Member accounts.
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    user_id       INT AUTO_INCREMENT PRIMARY KEY,
    username       VARCHAR(100)   NOT NULL,
    name           VARCHAR(150)   NOT NULL,
    email          VARCHAR(150)   NOT NULL UNIQUE,
    password       VARCHAR(255)   NOT NULL,          -- Store hashed passwords in production!
    role           ENUM('ADMIN', 'MEMBER') NOT NULL,
    balance        DOUBLE          NOT NULL DEFAULT 0.0,  -- Penalty balance for members
    total_delays   INT             NOT NULL DEFAULT 0     -- Total late return count
);

-- -------------------------------------------------------
-- TABLE: library_items
-- Stores all material types: Book, EBook, AudioBook, Periodical.
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS library_items (
    item_id            INT AUTO_INCREMENT PRIMARY KEY,
    title              VARCHAR(200)  NOT NULL,
    author             VARCHAR(150)  NOT NULL,
    publication_year   INT           NOT NULL,
    copy_count         INT           NOT NULL DEFAULT 1,
    available_copies   INT           NOT NULL DEFAULT 1,  -- Tracks current available stock
    status             VARCHAR(50)   NOT NULL DEFAULT 'Available',
    item_type          ENUM('BOOK', 'EBOOK', 'AUDIOBOOK', 'PERIODICAL') NOT NULL,

    -- Book-specific fields
    isbn                VARCHAR(20),
    page_count          INT,
    genre               VARCHAR(100),

    -- EBook-specific fields
    file_format         VARCHAR(20),
    file_size           DOUBLE,

    -- AudioBook-specific fields
    duration            INT,           -- Duration in minutes
    narrator            VARCHAR(150),

    -- Periodical-specific fields
    issue_number        INT,
    period              VARCHAR(50)   -- e.g. 'Weekly', 'Monthly'
);

-- -------------------------------------------------------
-- TABLE: borrow_records
-- Tracks every borrowing transaction.
-- Updated with ON DELETE CASCADE to allow item deletion.
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS borrow_records (
    record_id     INT AUTO_INCREMENT PRIMARY KEY,
    user_id        INT            NOT NULL,
    item_id        INT            NOT NULL,
    borrow_date    DATETIME       NOT NULL,
    due_date       DATETIME       NOT NULL,           -- Expected return date (borrow + loan period)
    return_date    DATETIME,                          -- Actual return date (NULL if not returned yet)
    status         ENUM('BORROWED', 'RETURNED') NOT NULL DEFAULT 'BORROWED',
    fine_amount    DOUBLE         NOT NULL DEFAULT 0.0,  -- Penalty calculated on return
    reminder_sent  TINYINT(1)     NOT NULL DEFAULT 0,    -- Overdue reminder sent flag (BorrowScheduler)

    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (item_id) REFERENCES library_items(item_id) ON DELETE CASCADE
);

-- -------------------------------------------------------
-- TABLE: reservations
-- Handles item reservations.
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS reservations (
    reservation_id  INT AUTO_INCREMENT PRIMARY KEY,
    user_id          INT           NOT NULL,
    item_id          INT           NOT NULL,
    request_date     DATE          NOT NULL,
    status           ENUM('PENDING', 'FULFILLED', 'CANCELLED') NOT NULL DEFAULT 'PENDING',

    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (item_id) REFERENCES library_items(item_id) ON DELETE CASCADE
);

-- -------------------------------------------------------
-- TABLE: reviews
-- Stores user reviews and ratings.
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS reviews (
    review_id    INT AUTO_INCREMENT PRIMARY KEY,
    user_id      INT           NOT NULL,
    item_id      INT           NOT NULL,
    rating       INT           NOT NULL,              -- 1-5
    comment      TEXT,
    review_date  DATE          NOT NULL,

    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (item_id) REFERENCES library_items(item_id) ON DELETE CASCADE,

    UNIQUE KEY unique_review (user_id, item_id),
    CONSTRAINT chk_rating CHECK (rating BETWEEN 1 AND 5)
);

-- -------------------------------------------------------
-- TABLE: wish_list
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS wish_list (
    wish_id     INT AUTO_INCREMENT PRIMARY KEY,
    user_id     INT  NOT NULL,
    item_id     INT  NOT NULL,
    added_date  DATE NOT NULL,

    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (item_id) REFERENCES library_items(item_id) ON DELETE CASCADE,

    UNIQUE KEY unique_wish (user_id, item_id)
);

-- -------------------------------------------------------
-- Sample Data
-- -------------------------------------------------------

INSERT INTO users (username, name, email, password, role) VALUES
('ahmetadmin', 'Ahmet Yilmaz', 'ahmet@libris.com', 'admin123', 'ADMIN');

INSERT INTO users (username, name, email, password, role, balance, total_delays) VALUES
('cantek', 'Can Tekin',  'can@mail.com',  'pass123', 'MEMBER', 0.0, 0),
('elifdemir', 'Elif Demir', 'elif@mail.com', 'pass456', 'MEMBER', 0.0, 0);

INSERT INTO library_items (title, author, publication_year, copy_count, available_copies, status, item_type, isbn, page_count, genre) VALUES
('Java Programming', 'Deitel',                    2024, 5, 5, 'Available', 'BOOK', '123-456', 800, 'Education'),
('Araba Sevdasi',    'Recaizade Mahmut Ekrem',    1875, 3, 3, 'Available', 'BOOK', '456-789', 276, 'Novel');

INSERT INTO library_items (title, author, publication_year, copy_count, available_copies, status, item_type, file_format, file_size) VALUES
('Digital Trends', 'AI Expert', 2025, 1, 1, 'Available', 'EBOOK', 'PDF', 15.5);

INSERT INTO library_items (title, author, publication_year, copy_count, available_copies, status, item_type, duration, narrator) VALUES
('Sapiens', 'Harari', 2014, 2, 2, 'Available', 'AUDIOBOOK', 900, 'John Smith');

INSERT INTO library_items (title, author, publication_year, copy_count, available_copies, status, item_type, issue_number, period) VALUES
('Science Weekly', 'Global Science', 2026, 10, 10, 'Available', 'PERIODICAL', 45, 'Weekly');
