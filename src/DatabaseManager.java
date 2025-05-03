// DatabaseManager.java
import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static final String DB_NAME = "NotesPasswordDB";
    private static final String DB_URL  = "jdbc:derby:" + DB_NAME + ";";
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }


    /** Create DB and tables if they don’t exist */
    public static void initDatabase() {
        boolean exists = new File(DB_NAME).exists();
        try (Connection conn = DriverManager.getConnection(DB_URL + (exists ? "" : "create=true"))) {
            // Users table
            try (Statement s = conn.createStatement()) {
                s.executeUpdate("""
                    CREATE TABLE Users (
                      id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
                      username VARCHAR(255) UNIQUE,
                      password VARCHAR(255)
                    )
                """);
            } catch (SQLException e){ if (!"X0Y32".equals(e.getSQLState())) e.printStackTrace(); }
            // Notes table
            try (Statement s = conn.createStatement()) {
                s.executeUpdate("""
                    CREATE TABLE Notes (
                      id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
                      owner VARCHAR(255),
                      title VARCHAR(255),
                      content VARCHAR(1000)
                    )
                """);
            } catch (SQLException e){ if (!"X0Y32".equals(e.getSQLState())) e.printStackTrace(); }
            // Passwords table
            try (Statement s = conn.createStatement()) {
                s.executeUpdate("""
                    CREATE TABLE Passwords (
                      id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
                      owner VARCHAR(255),
                      title VARCHAR(255),
                      username VARCHAR(255),
                      password VARCHAR(255)
                    )
                """);
            } catch (SQLException e){ if (!"X0Y32".equals(e.getSQLState())) e.printStackTrace(); }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean hasUsers() {
        String sql = "SELECT COUNT(*) FROM Users";
        try (Connection c = DriverManager.getConnection(DB_URL);
             Statement  s = c.createStatement();
             ResultSet  rs = s.executeQuery(sql)) {
            rs.next();
            return rs.getInt(1) > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    public static void createUser(String user, String pass) {
        String sql = "INSERT INTO Users(username,password) VALUES(?,?)";
        try (Connection c = DriverManager.getConnection(DB_URL);
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, user);
            p.setString(2, pass);
            p.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean checkLogin(String user, String pass) {
        String sql = "SELECT * FROM Users WHERE username=? AND password=?";
        try (Connection c = DriverManager.getConnection(DB_URL);
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, user);
            p.setString(2, pass);
            try (ResultSet rs = p.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }

    public static void saveNote(String owner, String title, String content) {
        String sql = "INSERT INTO Notes(owner,title,content) VALUES(?,?,?)";
        try (Connection c = DriverManager.getConnection(DB_URL);
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, owner);
            p.setString(2, title);
            p.setString(3, content);
            p.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<Object[]> getAllNotes(String owner) {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT id,title FROM Notes WHERE owner=?";
        try (Connection c = DriverManager.getConnection(DB_URL);
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, owner);
            try (ResultSet rs = p.executeQuery()) {
                while (rs.next()) {
                    list.add(new Object[]{rs.getInt("id"), rs.getString("title")});
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static String getNoteContent(int id) {
        String sql = "SELECT content FROM Notes WHERE id=?";
        try (Connection c = DriverManager.getConnection(DB_URL);
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, id);
            try (ResultSet rs = p.executeQuery()) {
                if (rs.next()) return rs.getString("content");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static void deleteNote(int id) {
        String sql = "DELETE FROM Notes WHERE id=?";
        try (Connection c = DriverManager.getConnection(DB_URL);
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, id);
            p.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void savePassword(String owner, String title, String username, String password) {
        String sql = "INSERT INTO Passwords(owner,title,username,password) VALUES(?,?,?,?)";
        try (Connection c = DriverManager.getConnection(DB_URL);
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, owner);
            p.setString(2, title);
            p.setString(3, username);
            p.setString(4, password);
            p.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<Object[]> getAllPasswords(String owner) {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT id,title,username,password FROM Passwords WHERE owner=?";
        try (Connection c = DriverManager.getConnection(DB_URL);
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, owner);
            try (ResultSet rs = p.executeQuery()) {
                while (rs.next()) {
                    list.add(new Object[]{
                            rs.getInt("id"),
                            rs.getString("title"),
                            rs.getString("username"),
                            rs.getString("password")
                    });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static void deletePassword(int id) {
        String sql = "DELETE FROM Passwords WHERE id=?";
        try (Connection c = DriverManager.getConnection(DB_URL);
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, id);
            p.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static void resetLogin(String password) throws SQLException {
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "UPDATE Users SET password = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, password);
                stmt.executeUpdate();
            }
        }
    }



    public static List<Object[]> searchNotesByTitle(String owner, String query) {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT id, title FROM Notes WHERE owner=? AND LOWER(title) LIKE ?";
        try (Connection c = DriverManager.getConnection(DB_URL);
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, owner);
            p.setString(2, "%" + query.toLowerCase() + "%");
            try (ResultSet rs = p.executeQuery()) {
                while (rs.next()) {
                    list.add(new Object[]{rs.getInt("id"), rs.getString("title")});
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static List<Object[]> searchPasswordsByTitleOrUsername(String owner, String query) {
        List<Object[]> list = new ArrayList<>();
        String sql = """
        SELECT id, title, username, password
        FROM Passwords
        WHERE owner=? AND (LOWER(title) LIKE ? OR LOWER(username) LIKE ?)
    """;
        try (Connection c = DriverManager.getConnection(DB_URL);
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, owner);
            String q = "%" + query.toLowerCase() + "%";
            p.setString(2, q);
            p.setString(3, q);
            try (ResultSet rs = p.executeQuery()) {
                while (rs.next()) {
                    list.add(new Object[]{
                            rs.getInt("id"),
                            rs.getString("title"),
                            rs.getString("username"),
                            rs.getString("password")
                    });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
