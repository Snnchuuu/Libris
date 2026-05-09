-- ============================================================
-- Libris Library Management System - Database Schema
-- ============================================================
-- Run this file once to set up all required tables in MySQL.
-- Make sure your database (e.g. 'libris_db') is created first.
-- ============================================================

-- -------------------------------------------------------
-- TABLE: users
-- Stores both Admin and Member accounts.
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    user_id        INT AUTO_INCREMENT PRIMARY KEY,
    username       VARCHAR(100)   NOT NULL,
    email          VARCHAR(150)   NOT NULL UNIQUE,
    password       VARCHAR(255)   NOT NULL,          -- Store hashed passwords in production!
    role           ENUM('ADMIN', 'MEMBER') NOT NULL,
    balance        DOUBLE         NOT NULL DEFAULT 0.0,  -- Penalty balance for members
    total_delays   INT            NOT NULL DEFAULT 0     -- Total late return count
);

-- -------------------------------------------------------
-- TABLE: library_items
-- Stores all material types: Book, EBook, AudioBook, Periodical.
-- The 'item_type' column differentiates between them.
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
    isbn               VARCHAR(20),
    page_count         INT,
    genre              VARCHAR(100),

    -- EBook-specific fields
    file_format        VARCHAR(20),
    file_size          DOUBLE,

    -- AudioBook-specific fields
    duration           INT,          -- Duration in minutes
    narrator           VARCHAR(150),

    -- Periodical-specific fields
    issue_number       INT,
    period             VARCHAR(50)   -- e.g. 'Weekly', 'Monthly'
);

-- -------------------------------------------------------
-- TABLE: borrow_records
-- Tracks every borrowing transaction.
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS borrow_records (
    record_id     INT AUTO_INCREMENT PRIMARY KEY,
    user_id       INT            NOT NULL,
    item_id       INT            NOT NULL,
    borrow_date   DATE           NOT NULL,
    due_date      DATE           NOT NULL,            -- Expected return date (borrow + 15 days)
    return_date   DATE,                               -- Actual return date (NULL if not returned yet)
    status        ENUM('BORROWED', 'RETURNED') NOT NULL DEFAULT 'BORROWED',
    fine_amount   DOUBLE         NOT NULL DEFAULT 0.0, -- Penalty calculated on return

    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (item_id) REFERENCES library_items(item_id)
);

-- -------------------------------------------------------
-- TABLE: reservations
-- Handles item reservations when all copies are borrowed.
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS reservations (
    reservation_id  INT AUTO_INCREMENT PRIMARY KEY,
    user_id         INT           NOT NULL,
    item_id         INT           NOT NULL,
    request_date    DATE          NOT NULL,
    status          ENUM('PENDING', 'FULFILLED', 'CANCELLED') NOT NULL DEFAULT 'PENDING',

    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (item_id) REFERENCES library_items(item_id)
);

-- -------------------------------------------------------
-- TABLE: reviews
-- Stores user reviews and ratings (1-5) for any item.
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS reviews (
    review_id    INT AUTO_INCREMENT PRIMARY KEY,
    user_id      INT           NOT NULL,
    item_id      INT           NOT NULL,
    rating       INT           NOT NULL,              -- Must be between 1 and 5
    comment      TEXT,
    review_date  DATE          NOT NULL,

    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (item_id) REFERENCES library_items(item_id),

    -- A user can only review the same item once
    UNIQUE KEY unique_review (user_id, item_id),

    -- Enforce rating range at the database level
    CONSTRAINT chk_rating CHECK (rating BETWEEN 1 AND 5)
);

-- -------------------------------------------------------
-- Sample Data (optional - for testing)
-- -------------------------------------------------------

-- Admin user
INSERT INTO users (username, email, password, role) VALUES
('Ahmet Yılmaz', 'ahmet@libris.com', 'admin123', 'ADMIN');

-- Member users
INSERT INTO users (username, email, password, role, balance, total_delays) VALUES
('Can Tekin',  'can@mail.com',  'pass123', 'MEMBER', 0.0, 0),
('Elif Demir', 'elif@mail.com', 'pass456', 'MEMBER', 0.0, 0);

-- Sample books
INSERT INTO library_items (title, author, publication_year, copy_count, available_copies, status, item_type, isbn, page_count, genre) VALUES
('Java Programming', 'Deitel',                    2024, 5, 5, 'Available', 'BOOK', '123-456', 800, 'Education'),
('Araba Sevdası',    'Recaizade Mahmut Ekrem',    1875, 3, 3, 'Available', 'BOOK', '456-789', 276, 'Novel');

-- Sample EBook
INSERT INTO library_items (title, author, publication_year, copy_count, available_copies, status, item_type, file_format, file_size) VALUES
('Digital Trends', 'AI Expert', 2025, 1, 1, 'Available', 'EBOOK', 'PDF', 15.5);

-- Sample AudioBook
INSERT INTO library_items (title, author, publication_year, copy_count, available_copies, status, item_type, duration, narrator) VALUES
('Sapiens', 'Harari', 2014, 2, 2, 'Available', 'AUDIOBOOK', 900, 'John Smith');

-- Sample Periodical
INSERT INTO library_items (title, author, publication_year, copy_count, available_copies, status, item_type, issue_number, period) VALUES
('Science Weekly', 'Global Science', 2026, 10, 10, 'Available', 'PERIODICAL', 45, 'Weekly');
-- ============================================================
-- Libris Library Management System - Sample Data
-- Run this file to populate the database with test data.
-- WARNING: This will delete all existing data first!
-- ============================================================

USE libris_db;

-- -------------------------------------------------------
-- Clear existing data (order matters due to foreign keys)
-- -------------------------------------------------------
DELETE FROM borrow_records;
DELETE FROM reservations;
DELETE FROM reviews;
DELETE FROM wish_list;
DELETE FROM library_items;
DELETE FROM users;

-- Reset auto increment counters
ALTER TABLE users AUTO_INCREMENT = 1;
ALTER TABLE library_items AUTO_INCREMENT = 1;
ALTER TABLE borrow_records AUTO_INCREMENT = 1;
ALTER TABLE reservations AUTO_INCREMENT = 1;
ALTER TABLE reviews AUTO_INCREMENT = 1;
ALTER TABLE wish_list AUTO_INCREMENT = 1;

-- -------------------------------------------------------
-- USERS (1 Admin + 6 Members)
-- -------------------------------------------------------
INSERT INTO users (username, email, password, role, balance, total_delays) VALUES
('Ahmet Yılmaz',  'ahmet@libris.com',  'admin123', 'ADMIN',  0.0, 0),
('Can Tekin',     'can@mail.com',      'pass123',  'MEMBER', 0.0, 0),
('Elif Demir',    'elif@mail.com',     'pass456',  'MEMBER', 0.0, 0),
('Murat Şahin',   'murat@mail.com',    'pass789',  'MEMBER', 0.0, 0),
('Zeynep Kaya',   'zeynep@mail.com',   'pass321',  'MEMBER', 0.0, 0),
('Burak Arslan',  'burak@mail.com',    'pass654',  'MEMBER', 0.0, 0),
('Selin Çelik',   'selin@mail.com',    'pass987',  'MEMBER', 0.0, 0);

-- -------------------------------------------------------
-- LIBRARY ITEMS
-- -------------------------------------------------------

-- Books
INSERT INTO library_items (title, author, publication_year, copy_count, available_copies, status, item_type, isbn, page_count, genre) VALUES
('Java Programming',            'Deitel',                       2024, 5, 5, 'Available', 'BOOK', '123-456', 800,  'Education'),
('Araba Sevdası',               'Recaizade Mahmut Ekrem',       1875, 3, 3, 'Available', 'BOOK', '456-789', 276,  'Novel'),
('Suç ve Ceza',                 'Fyodor Dostoyevski',           1866, 4, 4, 'Available', 'BOOK', '111-222', 551,  'Classic'),
('Simyacı',                     'Paulo Coelho',                 1988, 6, 6, 'Available', 'BOOK', '333-444', 208,  'Fiction'),
('Clean Code',                  'Robert C. Martin',             2008, 3, 3, 'Available', 'BOOK', '555-666', 431,  'Education'),
('Dune',                        'Frank Herbert',                1965, 2, 2, 'Available', 'BOOK', '777-888', 896,  'Science Fiction'),
('İnce Memed',                  'Yaşar Kemal',                  1955, 4, 4, 'Available', 'BOOK', '999-000', 472,  'Novel'),
('Design Patterns',             'Gang of Four',                 1994, 2, 2, 'Available', 'BOOK', '112-233', 395,  'Education');

-- EBooks
INSERT INTO library_items (title, author, publication_year, copy_count, available_copies, status, item_type, file_format, file_size) VALUES
('Digital Trends',              'AI Expert',                    2025, 1, 1, 'Available', 'EBOOK', 'PDF',  15.5),
('Python Crash Course',         'Eric Matthes',                 2023, 1, 1, 'Available', 'EBOOK', 'EPUB', 8.2),
('The Pragmatic Programmer',    'David Thomas',                 2019, 1, 1, 'Available', 'EBOOK', 'PDF',  12.7),
('Artificial Intelligence',     'Stuart Russell',               2022, 1, 1, 'Available', 'EBOOK', 'PDF',  22.3);

-- AudioBooks
INSERT INTO library_items (title, author, publication_year, copy_count, available_copies, status, item_type, duration, narrator) VALUES
('Sapiens',                     'Yuval Noah Harari',            2014, 2, 2, 'Available', 'AUDIOBOOK', 900,  'John Smith'),
('Atomic Habits',               'James Clear',                  2018, 2, 2, 'Available', 'AUDIOBOOK', 690,  'James Clear'),
('The Great Gatsby',            'F. Scott Fitzgerald',          1925, 1, 1, 'Available', 'AUDIOBOOK', 370,  'Jake Gyllenhaal'),
('Dune',                        'Frank Herbert',                1965, 1, 1, 'Available', 'AUDIOBOOK', 1380, 'Simon Vance');

-- Periodicals
INSERT INTO library_items (title, author, publication_year, copy_count, available_copies, status, item_type, issue_number, period) VALUES
('Science Weekly',              'Global Science Publishing',    2026, 10, 10, 'Available', 'PERIODICAL', 45,  'Weekly'),
('National Geographic',         'National Geographic Society',  2026, 5,  5,  'Available', 'PERIODICAL', 312, 'Monthly'),
('MIT Technology Review',       'MIT',                          2026, 3,  3,  'Available', 'PERIODICAL', 89,  'Monthly'),
('Popular Science',             'Popular Science Publishing',   2026, 4,  4,  'Available', 'PERIODICAL', 156, 'Monthly');

-- -------------------------------------------------------
-- BORROW RECORDS
-- user_id 2 = Can Tekin, 3 = Elif Demir, 4 = Murat
-- item_id 1 = Java Programming, 2 = Araba Sevdası etc.
-- -------------------------------------------------------
INSERT INTO borrow_records (user_id, item_id, borrow_date, due_date, return_date, status, fine_amount) VALUES
(2, 1, '2026-04-01', '2026-04-16', '2026-04-16', 'RETURNED', 0.0),   -- Can returned Java Programming on time
(2, 3, '2026-04-20', '2026-05-05', '2026-05-08', 'RETURNED', 18.0),  -- Can returned Suç ve Ceza 3 days late
(3, 2, '2026-04-15', '2026-04-30', '2026-04-30', 'RETURNED', 0.0),   -- Elif returned Araba Sevdası on time
(4, 4, '2026-05-01', '2026-05-16', NULL,          'BORROWED', 0.0),  -- Murat currently has Simyacı
(5, 5, '2026-05-03', '2026-05-18', NULL,          'BORROWED', 0.0),  -- Zeynep currently has Clean Code
(6, 17,'2026-04-10', '2026-04-25', '2026-05-01', 'RETURNED', 30.0);  -- Burak returned Science Weekly 6 days late

-- Update balances for members who paid fines
UPDATE users SET balance = 18.0, total_delays = 1 WHERE user_id = 2; -- Can Tekin
UPDATE users SET balance = 30.0, total_delays = 1 WHERE user_id = 6; -- Burak Arslan

-- Update available copies for currently borrowed items
UPDATE library_items SET available_copies = available_copies - 1 WHERE item_id = 4; -- Simyacı
UPDATE library_items SET available_copies = available_copies - 1 WHERE item_id = 5; -- Clean Code

-- -------------------------------------------------------
-- RESERVATIONS
-- -------------------------------------------------------
INSERT INTO reservations (user_id, item_id, request_date, status) VALUES
(3, 4, '2026-05-02', 'PENDING'),   -- Elif waiting for Simyacı
(7, 5, '2026-05-04', 'PENDING');   -- Selin waiting for Clean Code

-- -------------------------------------------------------
-- REVIEWS
-- -------------------------------------------------------
INSERT INTO reviews (user_id, item_id, rating, comment, review_date) VALUES
(2, 1, 5, 'Best Java book I have ever read. Highly recommended for beginners!',        '2026-04-17'),
(2, 3, 4, 'A masterpiece. Dostoyevski really makes you think about guilt and justice.', '2026-05-09'),
(3, 2, 5, 'Bihruz Bey is both hilarious and tragic. A classic Turkish novel.',          '2026-05-01'),
(4, 4, 5, 'Life-changing book. Simple yet profound message.',                           '2026-05-02'),
(5, 5, 4, 'Every developer should read this. Great tips on writing clean code.',        '2026-05-04'),
(6, 17,3, 'Decent magazine but some articles felt too short.',                          '2026-05-02');

-- -------------------------------------------------------
-- WISH LIST
-- -------------------------------------------------------
INSERT INTO wish_list (user_id, item_id, added_date) VALUES
(2, 6,  '2026-05-01'),  -- Can wants Dune (book)
(2, 13, '2026-05-03'),  -- Can wants Sapiens (audiobook)
(3, 1,  '2026-05-02'),  -- Elif wants Java Programming
(3, 9,  '2026-05-02'),  -- Elif wants Digital Trends (ebook)
(4, 7,  '2026-05-01'),  -- Murat wants İnce Memed
(5, 8,  '2026-05-04'),  -- Zeynep wants Design Patterns
(7, 14, '2026-05-05');  -- Selin wants Atomic Habits (audiobook)
