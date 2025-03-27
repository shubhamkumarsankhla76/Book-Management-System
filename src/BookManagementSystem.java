import java.sql.*;
import java.util.*;

public class BookManagementSystem {

    // Database connection parameters
    private static final String DB_URL = "jdbc:mysql://localhost:3306/library";
    private static final String DB_USER = System.getenv("DB_USER");
    private static final String DB_PASSWORD = System.getenv("DB_PASSWORD"); // Get the password from environment variables
    private static Connection conn;
    private static PreparedStatement preparedStatement;
    private static ResultSet rs;
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        try {
            // Establish connection
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            System.out.println("*Connected to database*");

            while (true) {
                System.out.println("\n----- Menu -----\n");
                System.out.println("1. Add a Book");
                System.out.println("2. View All Books");
                System.out.println("3. Update a Book");
                System.out.println("4. Delete a Book");
                System.out.println("5. Search for a Book");
                System.out.println("6. Exit\n");
                System.out.print("Enter your choice: ");

                // Handle non-integer inputs
                while (!scanner.hasNextInt()) {
                    System.out.println("\nInvalid choice. Please enter a number.\n");
                    scanner.nextLine();
                    System.out.print("Enter your choice: ");
                }

                int choice = scanner.nextInt();
                scanner.nextLine();

                // Menu options
                switch (choice) {
                    case 1 -> addBook();
                    case 2 -> viewAllBooks();
                    case 3 -> updateBook();
                    case 4 -> deleteBook();
                    case 5 -> searchBooks();
                    case 6 -> {
                        System.out.println("\nExiting...");
                        conn.close();
                        return;
                    }
                    default -> System.out.println("\nInvalid choice. Please enter again.\n");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //Adds a new book to the database.
    private static void addBook() throws SQLException {
        System.out.println("\n----- Add a Book -----\n");
        System.out.print("Enter book title: ");
        String title = scanner.nextLine();
        System.out.print("Enter book author: ");
        String author = scanner.nextLine();
        int year = getYearInput();

        int bookId = getAvailableBookId();

        String sql = "INSERT INTO books (id, title, author, year) VALUES (?, ?, ?, ?)";
        preparedStatement = conn.prepareStatement(sql);
        preparedStatement.setInt(1, bookId);
        preparedStatement.setString(2, title);
        preparedStatement.setString(3, author);
        preparedStatement.setInt(4, year);

        int rowsInserted = preparedStatement.executeUpdate();
        if (rowsInserted > 0) {
            System.out.println("\nA new book was added successfully!");
            System.out.println("Book ID: " + bookId);
        }
    }

    //Gets a valid year input from the user
    private static int getYearInput() {
        int year = 0;
        boolean validInput = false;
        while (!validInput) {
            System.out.print("Enter publication year: ");
            try {
                year = scanner.nextInt();
                scanner.nextLine();
                if (String.valueOf(year).length() > 4) {
                    System.out.println("\nInvalid year. Year can't have more than 4 digits.");
                } else {
                    validInput = true;
                }
            } catch (InputMismatchException e) {
                System.out.println("\nInvalid year. Year can't contain letters.");
                scanner.nextLine();// Consume invalid input
            }
        }
        return year;
    }

    //Gets the next available book ID.
    private static int getAvailableBookId() throws SQLException {
        String sql = "SELECT id FROM books ORDER BY id";
        preparedStatement = conn.prepareStatement(sql);
        rs = preparedStatement.executeQuery();

        int expectedId = 1;
        while (rs.next()) {
            int currentId = rs.getInt("id");
            if (currentId != expectedId) {
                return expectedId;
            }
            expectedId++;
        }
        return expectedId;
    }

    //Views all books in the database
    private static void viewAllBooks() throws SQLException {
        System.out.println("\n----- View All Books -----\n");
        String sql = "SELECT * FROM books";
        preparedStatement = conn.prepareStatement(sql);
        rs = preparedStatement.executeQuery();

        boolean found = false;
        while (rs.next()) {
            found = true;
            System.out.println("ID: " + rs.getInt("id"));
            System.out.println("Title: " + rs.getString("title"));
            System.out.println("Author: " + rs.getString("author"));
            System.out.println("Year: " + rs.getInt("year"));
            System.out.println();
        }

        if (!found) {
            System.out.println("No books found in the library!");
        }
    }

    // Updates an existing book in the database
    private static void updateBook() throws SQLException {
        System.out.println("\n----- Update a Book -----\n");

        int id = -1;
        while (id == -1) {
            System.out.print("Enter book ID to update: ");
            try {
                id = scanner.nextInt();
            } catch (InputMismatchException e) {
                System.out.println("\nInvalid input. Please enter a valid numerical ID.\n");
                scanner.next();
            }
        }

        String checkSql = "SELECT COUNT(*) FROM books WHERE id=?";
        preparedStatement = conn.prepareStatement(checkSql);
        preparedStatement.setInt(1, id);
        java.sql.ResultSet rs = preparedStatement.executeQuery();
        rs.next();
        int count = rs.getInt(1);

        if (count == 0) {
            System.out.println("\nNo book found with ID " + id);
            return;
        }

        scanner.nextLine(); // Consume newline left-over
        System.out.print("Enter new title: ");
        String title = scanner.nextLine();
        System.out.print("Enter new author: ");
        String author = scanner.nextLine();
        int year = getYearInput();

        String sql = "UPDATE books SET title=?, author=?, year=? WHERE id=?";
        preparedStatement = conn.prepareStatement(sql);
        preparedStatement.setString(1, title);
        preparedStatement.setString(2, author);
        preparedStatement.setInt(3, year);
        preparedStatement.setInt(4, id);

        preparedStatement.executeUpdate();
        System.out.println("\nBook updated successfully!");
    }

    // Deletes a book from the database
    private static void deleteBook() throws SQLException {
        System.out.println("\n----- Delete a Book -----\n");
        int id;

        while (true) {
            System.out.print("Enter book ID to delete: ");
            try {
                id = scanner.nextInt();
                scanner.nextLine();
                break;
            } catch (InputMismatchException e) {
                System.out.println("\nInvalid input. Please enter a valid integer for the book ID.\n");
                scanner.nextLine();// Consume invalid input
            }
        }

        String sql = "DELETE FROM books WHERE id=?";
        preparedStatement = conn.prepareStatement(sql);
        preparedStatement.setInt(1, id);

        int rowsDeleted = preparedStatement.executeUpdate();
        if (rowsDeleted > 0) {
            System.out.println("\nBook deleted successfully!");
        } else {
            System.out.println("\nNo book found with ID " + id);
        }
    }

    //Searches for books by title or author.
    private static void searchBooks() throws SQLException {
        System.out.println("\n----- Search for a Book -----\n");
        System.out.print("Enter title or author name: ");
        String searchTerm = scanner.nextLine().trim();


        String sql = "SELECT * FROM books WHERE LOWER(title) LIKE LOWER(?) OR LOWER(author) LIKE LOWER(?)";


        preparedStatement = conn.prepareStatement(sql);
        preparedStatement.setString(1, "%" + searchTerm + "%");
        preparedStatement.setString(2, "%" + searchTerm + "%");


        rs = preparedStatement.executeQuery();


        boolean found = printResults(rs);


        if (!found) {
            System.out.println("\nNo books found with \"" + searchTerm + "\" in title or author.");
        }
    }

    //Prints the results of a query.
    private static boolean printResults(ResultSet rs) throws SQLException {
        boolean found = false;
        while (rs.next()) {
            found = true;
            int id = rs.getInt("id");
            String title = rs.getString("title");
            String author = rs.getString("author");
            int year = rs.getInt("year");


            System.out.println("\nID: " + id);
            System.out.println("Title: " + title);
            System.out.println("Author: " + author);
            System.out.println("Year: " + year);
            System.out.println();
        }
        return found;
    }

}