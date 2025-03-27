Book Management System
This is a simple Java-based application to manage a collection of books in a library. The application provides a console interface for adding, viewing, updating, deleting, and searching for books. It uses MySQL for storing book data.

Features
Add a Book: Add a new book to the database with a title, author, and publication year.
View All Books: Display a list of all books in the database.
Update a Book: Update the details of an existing book by its ID.
Delete a Book: Remove a book from the database by its ID.
Search for a Book: Search for books by title or author (case-insensitive).

Create a table named books with the following structure:

CREATE TABLE books (
    id INT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(255) NOT NULL,
    year INT NOT NULL
);
