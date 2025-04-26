import java.sql.*;
import java.util.Scanner;

public class demo {

    private static final String DB_URL = "jdbc:derby:NotesPasswordDB;create=true";

    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            createTables(conn);
            runMenu(conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void createTables(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE TABLE Notes (" +
                    "id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)," +
                    "title VARCHAR(255)," +
                    "content VARCHAR(1000))");
        } catch (SQLException e) {
            if (!tableAlreadyExists(e)) {
                e.printStackTrace();
            }
        }

        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE TABLE Passwords (" +
                    "id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)," +
                    "title VARCHAR(255)," +
                    "username VARCHAR(255)," +
                    "password VARCHAR(255))");
        } catch (SQLException e) {
            if (!tableAlreadyExists(e)) {
                e.printStackTrace();
            }
        }
    }

    private static boolean tableAlreadyExists(SQLException e) {
        return e.getSQLState().equals("X0Y32");
    }


    private static void runMenu(Connection conn) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("\n===== Notes and Password Manager =====");
            System.out.println("1. Add Note");
            System.out.println("2. View Notes");
            System.out.println("3. Delete Note");
            System.out.println("4. Add Password");
            System.out.println("5. View Passwords");
            System.out.println("6. Delete Password");
            System.out.println("7. Exit");
            System.out.print("Choose an option: ");

            int choice = Integer.parseInt(scanner.nextLine());

            switch (choice) {
                case 1 -> addNote(conn, scanner);
                case 2 -> viewNotes(conn);
                case 3 -> deleteNote(conn, scanner);
                case 4 -> addPassword(conn, scanner);
                case 5 -> viewPasswords(conn);
                case 6 -> deletePassword(conn, scanner);
                case 7 -> {
                    System.out.println("Goodbye!");
                    return;
                }
                default -> System.out.println("Invalid option.");
            }
        }
    }

    private static void deleteNote(Connection conn, Scanner scanner) {
        try {
            System.out.print("Enter the ID of the note to delete: ");
            int id = Integer.parseInt(scanner.nextLine());

            PreparedStatement ps = conn.prepareStatement("DELETE FROM Notes WHERE id = ?");
            ps.setInt(1, id);
            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Note deleted.");
            } else {
                System.out.println("Note not found.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void deletePassword(Connection conn, Scanner scanner) {
        try {
            System.out.print("Enter the ID of the password to delete: ");
            int id = Integer.parseInt(scanner.nextLine());

            PreparedStatement ps = conn.prepareStatement("DELETE FROM Passwords WHERE id = ?");
            ps.setInt(1, id);
            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Password deleted.");
            } else {
                System.out.println("Password not found.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private static void addNote(Connection conn, Scanner scanner) {
        try {
            System.out.print("Enter title: ");
            String title = scanner.nextLine();
            System.out.print("Enter content: ");
            String content = scanner.nextLine();

            PreparedStatement ps = conn.prepareStatement("INSERT INTO Notes (title, content) VALUES (?, ?)");
            ps.setString(1, title);
            ps.setString(2, content);
            ps.executeUpdate();

            System.out.println("Note saved.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void viewNotes(Connection conn) {
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM Notes");

            System.out.println("\n--- Notes ---");
            while (rs.next()) {
                System.out.println("ID: " + rs.getInt("id"));
                System.out.println("Title: " + rs.getString("title"));
                System.out.println("Content: " + rs.getString("content"));
                System.out.println("--------------------");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void addPassword(Connection conn, Scanner scanner) {
        try {
            System.out.print("Enter title (e.g., website name): ");
            String title = scanner.nextLine();
            System.out.print("Enter username: ");
            String username = scanner.nextLine();
            System.out.print("Enter password: ");
            String password = scanner.nextLine();

            PreparedStatement ps = conn.prepareStatement("INSERT INTO Passwords (title, username, password) VALUES (?, ?, ?)");
            ps.setString(1, title);
            ps.setString(2, username);
            ps.setString(3, password);
            ps.executeUpdate();

            System.out.println("Password saved.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void viewPasswords(Connection conn) {
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM Passwords");

            System.out.println("\n--- Passwords ---");
            while (rs.next()) {
                System.out.println("ID: " + rs.getInt("id"));
                System.out.println("Title: " + rs.getString("title"));
                System.out.println("Username: " + rs.getString("username"));
                System.out.println("Password: " + rs.getString("password"));
                System.out.println("--------------------");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
