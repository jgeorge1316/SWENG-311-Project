import javax.swing.*;

public class MainApp {
    public static void main(String[] args) {
        DatabaseManager.initDatabase();
        SwingUtilities.invokeLater(() -> {
            LoginFrame login = new LoginFrame();
            login.setVisible(true);
        });
    }
}
