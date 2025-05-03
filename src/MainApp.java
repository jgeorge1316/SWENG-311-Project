// MainApp.java
import javax.swing.*;

public class MainApp {
    public static void main(String[] args) {
        // ensure DB and tables exist
        DatabaseManager.initDatabase();

        SwingUtilities.invokeLater(() -> {
            LoginFrame login = new LoginFrame();
            login.setVisible(true);
        });
    }
}
