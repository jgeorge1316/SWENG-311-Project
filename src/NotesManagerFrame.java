// NotesManagerFrame.java
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.awt.datatransfer.StringSelection;

public class NotesManagerFrame extends BaseFrame {
    private final String username;
    private final DefaultTableModel passwordsModel = new DefaultTableModel(new String[]{"ID","Title","Username","Password"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return column != 0; // Make ID column (index 0) non-editable
        }
    };

    private final DefaultTableModel notesModel = new DefaultTableModel(new String[]{"ID","Title"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return column != 0;
        }
    };
    private final JTable notesTable     = new JTable(notesModel);
    private final JTable passwordsTable = new JTable(passwordsModel);
    private final JTextArea noteContentArea = new JTextArea();

    public NotesManagerFrame(String username) {
        super("Notes & Password Manager — " + username, 800, 600);
        this.username = username;
        initUI();
        loadAll();
    }

    private void initUI() {
        JTabbedPane tabs = new JTabbedPane();
        JLabel statusLabel = new JLabel(" ");

        // --- Notes Tab ---
        notesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        notesTable.getColumnModel().getColumn(0).setMaxWidth(50);
        JScrollPane notesListPane = new JScrollPane(notesTable);
        notesListPane.setPreferredSize(new Dimension(250, 0));

        noteContentArea.setEditable(true);
        noteContentArea.setLineWrap(true);
        noteContentArea.setWrapStyleWord(true);
        JScrollPane contentPane = new JScrollPane(noteContentArea);

        JSplitPane noteSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, notesListPane, contentPane);
        noteSplit.setResizeWeight(0.3);
        JPanel notesTab = new JPanel(new BorderLayout(10, 10));
        notesTab.setBorder(new EmptyBorder(10, 10, 10, 10));
        notesTab.add(noteSplit, BorderLayout.CENTER);

        // [NEW] Search bar for notes
        JPanel noteSearchPanel = new JPanel(new BorderLayout(5, 5));
        JTextField noteSearchField = new JTextField();
        JButton noteSearchButton = new JButton("Search");
        noteSearchPanel.add(noteSearchField, BorderLayout.CENTER);
        noteSearchPanel.add(noteSearchButton, BorderLayout.EAST);

        noteSearchField.addActionListener(e -> searchNotes(noteSearchField.getText()));
        noteSearchButton.addActionListener(e -> searchNotes(noteSearchField.getText()));
        notesTab.add(noteSearchPanel, BorderLayout.NORTH);

        JPanel noteBtns = new JPanel();
        noteBtns.add(makeButton("Add Note", e -> addNote()));
        noteBtns.add(makeButton("Delete Note", e -> deleteNote()));
        notesTab.add(noteBtns, BorderLayout.SOUTH);

        notesTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && notesTable.getSelectedRow() >= 0) {
                int id = (int) notesTable.getValueAt(notesTable.getSelectedRow(), 0);
                noteContentArea.setText(DatabaseManager.getNoteContent(id));
            }
        });
        noteBtns.add(makeButton("Save Changes", e -> saveNoteEdits()));

        // --- Passwords Tab ---
        passwordsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        passwordsTable.getColumnModel().getColumn(0).setMaxWidth(50);
        JScrollPane pwdListPane = new JScrollPane(passwordsTable);

        JPanel pwdTab = new JPanel(new BorderLayout(10, 10));
        pwdTab.setBorder(new EmptyBorder(10, 10, 10, 10));
        pwdTab.add(pwdListPane, BorderLayout.CENTER);

        // [NEW] Search bar for passwords
        JPanel pwdSearchPanel = new JPanel(new BorderLayout(5, 5));
        JTextField pwdSearchField = new JTextField();
        JButton pwdSearchButton = new JButton("Search");
        pwdSearchPanel.add(pwdSearchField, BorderLayout.CENTER);
        pwdSearchPanel.add(pwdSearchButton, BorderLayout.EAST);

        pwdSearchField.addActionListener(e -> searchPasswords(pwdSearchField.getText()));
        pwdSearchButton.addActionListener(e -> searchPasswords(pwdSearchField.getText()));
        pwdTab.add(pwdSearchPanel, BorderLayout.NORTH);

        JPanel pwdBtns = new JPanel();
        pwdBtns.add(makeButton("Add Password", e -> addPassword()));
        pwdBtns.add(makeButton("Delete Password", e -> deletePassword()));
        pwdTab.add(pwdBtns, BorderLayout.SOUTH);

        JPanel pwdBottomPanel = new JPanel(new BorderLayout());
        pwdBottomPanel.add(pwdBtns, BorderLayout.NORTH);
        pwdBottomPanel.add(statusLabel, BorderLayout.SOUTH);
        pwdTab.add(pwdBottomPanel, BorderLayout.SOUTH);
        // Copy cell content to clipboard on click
        passwordsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = passwordsTable.rowAtPoint(e.getPoint());
                int col = passwordsTable.columnAtPoint(e.getPoint());
                if (row >= 0 && col >= 0) {
                    Object value = passwordsTable.getValueAt(row, col);
                    if (value != null) {
                        StringSelection selection = new StringSelection(value.toString());
                        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
                        statusLabel.setText("Copied to clipboard: " + value.toString());

                        // Clear the message after 3 seconds
                        Timer timer = new Timer(10000, evt -> statusLabel.setText(" "));
                        timer.setRepeats(false);
                        timer.start();
                    }
                }
            }
        });
        pwdBtns.add(makeButton("Save Changes", e -> savePasswordEdits()));

        // add tabs
        tabs.addTab("Notes", notesTab);
        tabs.addTab("Passwords", pwdTab);

        add(tabs);
    }

    private void saveNoteEdits() {
        int selectedRow = notesTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a note to save.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int id = (int) notesModel.getValueAt(selectedRow, 0);   // ID
            String title = (String) notesModel.getValueAt(selectedRow, 1); // Title
            String content = noteContentArea.getText();             // Content from the editor

            DatabaseManager.updateNote(id, title, content);
            JOptionPane.showMessageDialog(this, "Note saved successfully.", "Saved", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to save note: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void savePasswordEdits() {
        int rowCount = passwordsModel.getRowCount();
        for (int i = 0; i < rowCount; i++) {
            try {
                int id = ((Number) passwordsModel.getValueAt(i, 0)).intValue();
                String title = passwordsModel.getValueAt(i, 1).toString();
                String user = passwordsModel.getValueAt(i, 2).toString();
                String pass = passwordsModel.getValueAt(i, 3).toString();

                DatabaseManager.updatePassword(id, title, user, pass);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Error saving row " + (i+1) + ": " + ex.getMessage(),
                        "Save Error",
                        JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }

        JOptionPane.showMessageDialog(this, "All changes saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        loadAll();
    }

    private void searchNotes(String query) {
        notesModel.setRowCount(0);
        for (Object[] row : DatabaseManager.searchNotesByTitle(username, query)) {
            notesModel.addRow(row);
        }
    }

    private void searchPasswords(String query) {
        passwordsModel.setRowCount(0);
        for (Object[] row : DatabaseManager.searchPasswordsByTitleOrUsername(username, query)) {
            passwordsModel.addRow(row);
        }
    }

    private JButton makeButton(String text, ActionListener al) {
        JButton b = new JButton(text);
        b.addActionListener(al);
        return b;
    }

    private void loadAll() {
        notesModel.setRowCount(0);
        for (Object[] row : DatabaseManager.getAllNotes(username)) {
            notesModel.addRow(row);
        }
        passwordsModel.setRowCount(0);
        for (Object[] row : DatabaseManager.getAllPasswords(username)) {
            passwordsModel.addRow(row);
        }
    }

    private void addNote() {
        String title = JOptionPane.showInputDialog(this, "Note Title:");
        if (title!=null && !title.trim().isEmpty()) {
            String content = JOptionPane.showInputDialog(this, "Note Content:");
            DatabaseManager.saveNote(username,title,content);
            loadAll();
        }
    }

    private void deleteNote() {
        int r = notesTable.getSelectedRow();
        if (r>=0) {
            int id = (int)notesTable.getValueAt(r,0);
            if (JOptionPane.showConfirmDialog(this,"Delete this note?","Confirm",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION) {
                DatabaseManager.deleteNote(id);
                noteContentArea.setText("");
                loadAll();
            }
        }
    }

    private void addPassword() {
        JPanel p = new JPanel(new GridLayout(3,2,5,5));
        JTextField t = new JTextField();
        JTextField u = new JTextField();
        JTextField pw= new JPasswordField();
        p.add(new JLabel("Title:"));     p.add(t);
        p.add(new JLabel("Username:"));  p.add(u);
        p.add(new JLabel("Password:"));  p.add(pw);
        if (JOptionPane.showConfirmDialog(this,p,"Add Password",JOptionPane.OK_CANCEL_OPTION)==JOptionPane.OK_OPTION) {
            DatabaseManager.savePassword(username, t.getText(), u.getText(), pw.getText());
            loadAll();
        }
    }

    private void deletePassword() {
        int selectedRow = passwordsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please select a password to delete",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // Safely cast the table value to an int
        Number idNum = (Number) passwordsTable.getValueAt(selectedRow, 0);
        int id = idNum.intValue();

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete this password?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                DatabaseManager.deletePassword(id);
                JOptionPane.showMessageDialog(
                        this,
                        "Password deleted successfully.",
                        "Deleted",
                        JOptionPane.INFORMATION_MESSAGE
                );
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(
                        this,
                        "Error deleting password:\n" + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
                ex.printStackTrace();
            }
            loadAll();  // repopulate both tables
        }
    }

}
