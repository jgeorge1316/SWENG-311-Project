import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.sql.*;

public class Demo2 extends JFrame {
    private static final String DB_NAME = "NotesPasswordDB";
    private static final String DB_URL = "jdbc:derby:" + DB_NAME + ";";
    private Connection connection;
    private JTabbedPane tabbedPane;
    private JTable notesTable;
    private JTable passwordsTable;
    private DefaultTableModel notesModel;
    private DefaultTableModel passwordsModel;
    private JTextArea noteContentArea;
    private JButton addNoteBtn, deleteNoteBtn;
    private JButton addPasswordBtn, deletePasswordBtn;
    private JButton resetCredentialsBtn;

    public Demo2() {
        setTitle("Notes & Password Manager");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
        initDatabase();
    }

    private void initComponents() {
        // Create tabbed pane
        tabbedPane = new JTabbedPane();

        // Notes panel
        JPanel notesPanel = new JPanel(new BorderLayout(10, 10));
        notesPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Notes table
        String[] notesColumns = {"ID", "Title"};
        notesModel = new DefaultTableModel(notesColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        notesTable = new JTable(notesModel);
        notesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        notesTable.getColumnModel().getColumn(0).setMaxWidth(50);
        JScrollPane notesScrollPane = new JScrollPane(notesTable);
        notesScrollPane.setPreferredSize(new Dimension(250, 0));

        // Note content area
        noteContentArea = new JTextArea();
        noteContentArea.setEditable(false);
        noteContentArea.setLineWrap(true);
        noteContentArea.setWrapStyleWord(true);
        JScrollPane contentScrollPane = new JScrollPane(noteContentArea);

        // Note selection listener
        notesTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && notesTable.getSelectedRow() != -1) {
                int noteId = (int) notesTable.getValueAt(notesTable.getSelectedRow(), 0);
                displayNoteContent(noteId);
            }
        });

        // Notes buttons
        JPanel notesButtonPanel = new JPanel();
        addNoteBtn = new JButton("Add Note");
        deleteNoteBtn = new JButton("Delete Note");
        notesButtonPanel.add(addNoteBtn);
        notesButtonPanel.add(deleteNoteBtn);

        // Add components to notes panel
        JSplitPane notesSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, notesScrollPane, contentScrollPane);
        notesSplitPane.setResizeWeight(0.3);
        notesPanel.add(notesSplitPane, BorderLayout.CENTER);
        notesPanel.add(notesButtonPanel, BorderLayout.SOUTH);

        // Passwords panel
        JPanel passwordsPanel = new JPanel(new BorderLayout(10, 10));
        passwordsPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Passwords table
        String[] passwordsColumns = {"ID", "Title", "Username", "Password"};
        passwordsModel = new DefaultTableModel(passwordsColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        passwordsTable = new JTable(passwordsModel);
        passwordsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        passwordsTable.getColumnModel().getColumn(0).setMaxWidth(50);
        JScrollPane passwordsScrollPane = new JScrollPane(passwordsTable);

        // Passwords buttons
        JPanel passwordsButtonPanel = new JPanel();
        addPasswordBtn = new JButton("Add Password");
        deletePasswordBtn = new JButton("Delete Password");
        resetCredentialsBtn = new JButton("Reset Master Credentials");
        passwordsButtonPanel.add(addPasswordBtn);
        passwordsButtonPanel.add(deletePasswordBtn);
        passwordsButtonPanel.add(resetCredentialsBtn);

        // Add components to passwords panel
        passwordsPanel.add(passwordsScrollPane, BorderLayout.CENTER);
        passwordsPanel.add(passwordsButtonPanel, BorderLayout.SOUTH);

        // Add tabs
        tabbedPane.addTab("Notes", new ImageIcon(), notesPanel, "Manage your notes");
        tabbedPane.addTab("Passwords", new ImageIcon(), passwordsPanel, "Manage your passwords");

        // Add to frame
        add(tabbedPane);

        // Button listeners
        addNoteBtn.addActionListener(e -> showAddNoteDialog());
        deleteNoteBtn.addActionListener(e -> deleteSelectedNote());
        addPasswordBtn.addActionListener(e -> showAddPasswordDialog());
        deletePasswordBtn.addActionListener(e -> deleteSelectedPassword());
        resetCredentialsBtn.addActionListener(e -> showResetCredentialsDialog());

        // Window close event to properly shut down DB
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                closeConnection();
            }
        });
    }

    private void initDatabase() {
        boolean dbExists = new File(DB_NAME).exists();

        if (!dbExists) {
            showLoginDialog(true);
        } else {
            showLoginDialog(false);
        }
    }

    private void showLoginDialog(boolean isNewDB) {
        JDialog loginDialog = new JDialog(this, isNewDB ? "Create Database" : "Login", true);
        loginDialog.setLayout(new BorderLayout());

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        JLabel usernameLabel = new JLabel(isNewDB ? "Create Username:" : "Username:");
        JTextField usernameField = new JTextField(15);
        JLabel passwordLabel = new JLabel(isNewDB ? "Create Password:" : "Password:");
        JPasswordField passwordField = new JPasswordField(15);
        JButton loginButton = new JButton(isNewDB ? "Create Database" : "Login");

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(usernameLabel, gbc);

        gbc.gridx = 1;
        panel.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(passwordLabel, gbc);

        gbc.gridx = 1;
        panel.add(passwordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(loginButton, gbc);

        loginDialog.add(panel, BorderLayout.CENTER);

        loginButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(loginDialog,
                        "Username and password cannot be empty",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (isNewDB) {
                setupDatabase(username, password);
                JOptionPane.showMessageDialog(loginDialog,
                        "Database created successfully. Please log in.",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                loginDialog.dispose();
                showLoginDialog(false);
            } else {
                if (connectToDatabase(username, password)) {
                    loginDialog.dispose();
                    refreshTables();
                } else {
                    JOptionPane.showMessageDialog(loginDialog,
                            "Login failed. Please check your credentials.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        loginDialog.pack();
        loginDialog.setLocationRelativeTo(this);
        loginDialog.setResizable(false);
        loginDialog.setVisible(true);
    }

    private void setupDatabase(String username, String password) {
        try {
            Connection conn = DriverManager.getConnection(DB_URL + "create=true");
            conn.setAutoCommit(false);
            createTables(conn);
            setupAuthentication(conn, username, password);
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error creating database: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean connectToDatabase(String username, String password) {
        try {
            connection = DriverManager.getConnection(
                    "jdbc:derby:" + DB_NAME + ";user=" + username + ";password=" + password);
            // Ensure we use the APP schema where tables were created
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("SET SCHEMA APP");
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void createTables(Connection conn) {
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
        return "X0Y32".equals(e.getSQLState());
    }

    private void setupAuthentication(Connection conn, String username, String password) {
        try (Statement stmt = conn.createStatement()) {
            // Enable built-in authentication
            stmt.executeUpdate("CALL SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY(" +
                    "'derby.connection.requireAuthentication', 'true')");

            // Set the authentication provider to 'BUILTIN'
            stmt.executeUpdate("CALL SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY(" +
                    "'derby.authentication.provider', 'BUILTIN')");

            // Create a user - make sure to use the correct syntax
            stmt.executeUpdate("CALL SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY(" +
                    "'derby.user." + username + "', '" + password + "')");

            // Set default user access permissions
            stmt.executeUpdate("CALL SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY(" +
                    "'derby.database.fullAccessUsers', '" + username + "')");

            // Make sure changes are committed
            conn.commit();

            // Shutdown DB properly to apply new authentication settings
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            // Proper shutdown to ensure settings are applied
            DriverManager.getConnection("jdbc:derby:" + DB_NAME + ";shutdown=true");
        } catch (SQLException e) {
            // Ignore expected shutdown exception (XJ015)
            if (!"XJ015".equals(e.getSQLState())) {
                e.printStackTrace();
            }
        }
    }

    private void refreshTables() {
        refreshNotesTable();
        refreshPasswordsTable();
    }

    private void refreshNotesTable() {
        notesModel.setRowCount(0);
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT id, title FROM Notes");
            while (rs.next()) {
                Object[] row = {rs.getInt("id"), rs.getString("title")};
                notesModel.addRow(row);
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error loading notes: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshPasswordsTable() {
        passwordsModel.setRowCount(0);
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM Passwords");
            while (rs.next()) {
                Object[] row = {
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("username"),
                        rs.getString("password")
                };
                passwordsModel.addRow(row);
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error loading passwords: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void displayNoteContent(int noteId) {
        try {
            PreparedStatement ps = connection.prepareStatement("SELECT content FROM Notes WHERE id = ?");
            ps.setInt(1, noteId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                noteContentArea.setText(rs.getString("content"));
            } else {
                noteContentArea.setText("");
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error loading note content: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showAddNoteDialog() {
        JDialog addNoteDialog = new JDialog(this, "Add New Note", true);
        addNoteDialog.setLayout(new BorderLayout());

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel titlePanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("Title:");
        JTextField titleField = new JTextField();
        titlePanel.add(titleLabel, BorderLayout.WEST);
        titlePanel.add(titleField, BorderLayout.CENTER);

        JLabel contentLabel = new JLabel("Content:");
        JTextArea contentArea = new JTextArea(10, 30);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        JScrollPane contentScrollPane = new JScrollPane(contentArea);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        panel.add(titlePanel, BorderLayout.NORTH);
        panel.add(contentLabel, BorderLayout.WEST);
        panel.add(contentScrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        addNoteDialog.add(panel);

        saveButton.addActionListener(e -> {
            String title = titleField.getText().trim();
            String content = contentArea.getText().trim();

            if (title.isEmpty()) {
                JOptionPane.showMessageDialog(addNoteDialog,
                        "Title cannot be empty",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (addNote(title, content)) {
                addNoteDialog.dispose();
                refreshNotesTable();
            }
        });

        cancelButton.addActionListener(e -> addNoteDialog.dispose());

        addNoteDialog.pack();
        addNoteDialog.setLocationRelativeTo(this);
        addNoteDialog.setVisible(true);
    }

    private boolean addNote(String title, String content) {
        try {
            PreparedStatement ps = connection.prepareStatement("INSERT INTO Notes (title, content) VALUES (?, ?)");
            ps.setString(1, title);
            ps.setString(2, content);
            ps.executeUpdate();
            ps.close();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error adding note: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private void deleteSelectedNote() {
        int selectedRow = notesTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a note to delete",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int noteId = (int) notesTable.getValueAt(selectedRow, 0);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this note?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            if (deleteNote(noteId)) {
                refreshNotesTable();
                noteContentArea.setText("");
            }
        }
    }

    private boolean deleteNote(int noteId) {
        try {
            PreparedStatement ps = connection.prepareStatement("DELETE FROM Notes WHERE id = ?");
            ps.setInt(1, noteId);
            ps.executeUpdate();
            ps.close();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error deleting note: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private void showAddPasswordDialog() {
        JDialog addPasswordDialog = new JDialog(this, "Add New Password", true);
        addPasswordDialog.setLayout(new BorderLayout());

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        JLabel titleLabel = new JLabel("Title (e.g. website):");
        JTextField titleField = new JTextField(20);
        JLabel usernameLabel = new JLabel("Username:");
        JTextField usernameField = new JTextField(20);
        JLabel passwordLabel = new JLabel("Password:");
        JPasswordField passwordField = new JPasswordField(20);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(titleLabel, gbc);

        gbc.gridx = 1;
        panel.add(titleField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(usernameLabel, gbc);

        gbc.gridx = 1;
        panel.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(passwordLabel, gbc);

        gbc.gridx = 1;
        panel.add(passwordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        panel.add(buttonPanel, gbc);

        addPasswordDialog.add(panel);

        saveButton.addActionListener(e -> {
            String title = titleField.getText().trim();
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());

            if (title.isEmpty()) {
                JOptionPane.showMessageDialog(addPasswordDialog,
                        "Title cannot be empty",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (addPassword(title, username, password)) {
                addPasswordDialog.dispose();
                refreshPasswordsTable();
            }
        });

        cancelButton.addActionListener(e -> addPasswordDialog.dispose());

        addPasswordDialog.pack();
        addPasswordDialog.setLocationRelativeTo(this);
        addPasswordDialog.setVisible(true);
    }

    private boolean addPassword(String title, String username, String password) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO Passwords (title, username, password) VALUES (?, ?, ?)");
            ps.setString(1, title);
            ps.setString(2, username);
            ps.setString(3, password);
            ps.executeUpdate();
            ps.close();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error adding password: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private void deleteSelectedPassword() {
        int selectedRow = passwordsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a password to delete",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int passwordId = (int) passwordsTable.getValueAt(selectedRow, 0);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this password?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            if (deletePassword(passwordId)) {
                refreshPasswordsTable();
            }
        }
    }

    private boolean deletePassword(int passwordId) {
        try {
            PreparedStatement ps = connection.prepareStatement("DELETE FROM Passwords WHERE id = ?");
            ps.setInt(1, passwordId);
            ps.executeUpdate();
            ps.close();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error deleting password: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private void showResetCredentialsDialog() {
        JDialog dlg = new JDialog(this, "Reset Master Credentials", true);
        dlg.setLayout(new GridBagLayout());
        dlg.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField userField = new JTextField(15);
        JPasswordField passField = new JPasswordField(15);
        JPasswordField passConfirm = new JPasswordField(15);
        JButton save = new JButton("Save");
        JButton cancel = new JButton("Cancel");

        gbc.gridx = 0; gbc.gridy = 0;
        dlg.add(new JLabel("New Username:"), gbc);
        gbc.gridx = 1;
        dlg.add(userField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        dlg.add(new JLabel("New Password:"), gbc);
        gbc.gridx = 1;
        dlg.add(passField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        dlg.add(new JLabel("Confirm Password:"), gbc);
        gbc.gridx = 1;
        dlg.add(passConfirm, gbc);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btns.add(save);
        btns.add(cancel);
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        dlg.add(btns, gbc);

        save.addActionListener(ev -> {
            String u = userField.getText().trim();
            String p = new String(passField.getPassword());
            String p2 = new String(passConfirm.getPassword());
            if (u.isEmpty() || p.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Username/password cannot be blank","Error",JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!p.equals(p2)) {
                JOptionPane.showMessageDialog(dlg, "Passwords do not match","Error",JOptionPane.ERROR_MESSAGE);
                return;
            }
            dlg.dispose();
            resetAuthentication(u, p);
        });

        cancel.addActionListener(ev -> dlg.dispose());

        dlg.pack();
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    private void resetAuthentication(String newUser, String newPass) {
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(
                    "CALL SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY(" +
                            "'derby.user." + newUser + "','" + newPass + "')");
            stmt.executeUpdate(
                    "CALL SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY(" +
                            "'derby.database.fullAccessUsers','" + newUser + "')");
            JOptionPane.showMessageDialog(
                    this,
                    "Master credentials reset.\nPlease restart the application to apply changes.",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE
            );
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(
                    this,
                    "Error resetting credentials:\n" + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                try {
                    DriverManager.getConnection("jdbc:derby:;shutdown=true");
                } catch (SQLException se) {
                    if (!"XJ015".equals(se.getSQLState())) {
                        se.printStackTrace();
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            Demo2 app = new Demo2();
            app.setVisible(true);
        });
    }
}