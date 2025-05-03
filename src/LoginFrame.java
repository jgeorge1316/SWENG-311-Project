// LoginFrame.java
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;

public class LoginFrame extends BaseFrame {
    private JTextField  userField  = new JTextField(15);
    private JPasswordField passField = new JPasswordField(15);

    public LoginFrame() {
        super("Login", 350, 200);

        // If no master user exists yet, prompt to create one
        if (!DatabaseManager.hasUsers()) {
            showCreateUserDialog();
        }

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);
        gbc.fill  = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        panel.add(userField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        panel.add(passField, gbc);

        JButton loginBtn = new JButton("Login");

        loginBtn.addActionListener(e -> authenticate());
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1;
        panel.add(loginBtn, gbc);

        JButton resetButton = new JButton("Reset Login");

        resetButton.addActionListener(e -> showResetDialog());
        gbc.gridx = 1; gbc.gridy = 2; gbc.gridwidth = 1;
        panel.add(resetButton, gbc);
        add(panel);
    }

    private void showCreateUserDialog() {
        JTextField  u = new JTextField(15);
        JPasswordField p1 = new JPasswordField(15);
        JPasswordField p2 = new JPasswordField(15);

        JPanel dp = new JPanel(new GridLayout(3,2,5,5));
        dp.add(new JLabel("Create Username:"));
        dp.add(u);
        dp.add(new JLabel("Create Password:"));
        dp.add(p1);
        dp.add(new JLabel("Confirm Password:"));
        dp.add(p2);

        int res = JOptionPane.showConfirmDialog(
                this, dp, "Set Master Credentials", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
        );
        if (res == JOptionPane.OK_OPTION) {
            String us = u.getText().trim();
            String pw1 = new String(p1.getPassword());
            String pw2 = new String(p2.getPassword());
            if (us.isEmpty() || pw1.isEmpty() || !pw1.equals(pw2)) {
                JOptionPane.showMessageDialog(this,
                        "Invalid or mismatched credentials.",
                        "Error", JOptionPane.ERROR_MESSAGE
                );
                showCreateUserDialog();
            } else {
                DatabaseManager.createUser(us, pw1);
                JOptionPane.showMessageDialog(this,
                        "Master user created. Please log in.",
                        "Success", JOptionPane.INFORMATION_MESSAGE
                );
            }
        } else {
            System.exit(0);
        }
    }

    private void authenticate() {
        String user = userField.getText().trim();
        String pass = new String(passField.getPassword());
        if (DatabaseManager.checkLogin(user, pass)) {
            dispose();
            NotesManagerFrame mgr = new NotesManagerFrame(user);
            mgr.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Login failed. Please check your credentials.",
                    "Error", JOptionPane.ERROR_MESSAGE
            );
        }
    }
    private void showResetDialog() {
        JPasswordField newPasswordField = new JPasswordField();

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("New Password:"));
        panel.add(newPasswordField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Reset Login",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String newPassword = new String(newPasswordField.getPassword());

            if (!newPassword.isEmpty()) {
                try {
                    DatabaseManager.resetLogin(newPassword);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                JOptionPane.showMessageDialog(this, "Login updated successfully!");
            } else {
                JOptionPane.showMessageDialog(this, "Fields cannot be empty.",
                        "Input Error", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

}
