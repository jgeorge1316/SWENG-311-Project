import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.awt.datatransfer.StringSelection;

public class NotesManagerFrame extends BaseFrame implements ActionListener {
    private final String username;
    private final DefaultTableModel passwordsModel = new DefaultTableModel(new String[]{"ID","Title","Username","Password"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return column != 0;
        }
    };

    private final DefaultTableModel notesModel = new DefaultTableModel(new String[]{"ID","Title"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return column != 0;
        }
    };
    private final JTable notesTable = new JTable(notesModel);
    private final JTable passwordsTable = new JTable(passwordsModel);
    private final JTextArea noteContentArea = new JTextArea();
    private JTextField noteSearchField;
    private JTextField pwdSearchField;

    public NotesManagerFrame(String username) {
        super("Notes & Password Manager — " + username, 800, 600);
        this.username = username;
        initUI();
        loadAll();
    }

    private void initUI() {
        JTabbedPane tabs = new JTabbedPane();
        JLabel statusLabel = new JLabel(" ");

        //Notes section
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

        //search notes
        noteSearchField = new JTextField();
        JButton noteSearchButton = new JButton("Search");
        noteSearchField.setActionCommand("searchNotes");
        noteSearchField.addActionListener(this);
        noteSearchButton.setActionCommand("searchNotes");
        noteSearchButton.addActionListener(this);
        JPanel noteSearchPanel = new JPanel(new BorderLayout(5, 5));
        noteSearchPanel.add(noteSearchField, BorderLayout.CENTER);
        noteSearchPanel.add(noteSearchButton, BorderLayout.EAST);
        notesTab.add(noteSearchPanel, BorderLayout.NORTH);

        JPanel noteBtns = new JPanel();
        noteBtns.add(makeButton("Add Note", "addNote"));
        noteBtns.add(makeButton("Delete Note", "deleteNote"));
        noteBtns.add(makeButton("Save Changes", "saveNoteEdits"));
        notesTab.add(noteBtns, BorderLayout.SOUTH);

        notesTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && notesTable.getSelectedRow() >= 0) {
                int id = (int) notesTable.getValueAt(notesTable.getSelectedRow(), 0);
                noteContentArea.setText(DatabaseManager.getNoteContent(id));
            }
        });

        //password section
        passwordsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        passwordsTable.getColumnModel().getColumn(0).setMaxWidth(50);
        JScrollPane pwdListPane = new JScrollPane(passwordsTable);

        JPanel pwdTab = new JPanel(new BorderLayout(10, 10));
        pwdTab.setBorder(new EmptyBorder(10, 10, 10, 10));
        pwdTab.add(pwdListPane, BorderLayout.CENTER);

        //search pass
        pwdSearchField = new JTextField();
        JButton pwdSearchButton = new JButton("Search");
        pwdSearchField.setActionCommand("searchPasswords");
        pwdSearchField.addActionListener(this);
        pwdSearchButton.setActionCommand("searchPasswords");
        pwdSearchButton.addActionListener(this);
        JPanel pwdSearchPanel = new JPanel(new BorderLayout(5, 5));
        pwdSearchPanel.add(pwdSearchField, BorderLayout.CENTER);
        pwdSearchPanel.add(pwdSearchButton, BorderLayout.EAST);
        pwdTab.add(pwdSearchPanel, BorderLayout.NORTH);

        JPanel pwdBtns = new JPanel();
        pwdBtns.add(makeButton("Add Password", "addPassword"));
        pwdBtns.add(makeButton("Delete Password", "deletePassword"));
        pwdBtns.add(makeButton("Save Changes", "savePasswordEdits"));

        JPanel pwdBottomPanel = new JPanel(new BorderLayout());
        pwdBottomPanel.add(pwdBtns, BorderLayout.NORTH);
        pwdBottomPanel.add(statusLabel, BorderLayout.SOUTH);
        pwdTab.add(pwdBottomPanel, BorderLayout.SOUTH);

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
                        Timer timer = new Timer(10000, evt -> statusLabel.setText(" "));
                        timer.setRepeats(false);
                        timer.start();
                    }
                }
            }
        });

        tabs.addTab("Notes", notesTab);
        tabs.addTab("Passwords", pwdTab);
        add(tabs);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        switch (cmd) {
            case "addNote" -> addNote();
            case "deleteNote" -> deleteNote();
            case "saveNoteEdits" -> saveNoteEdits();
            case "addPassword" -> addPassword();
            case "deletePassword" -> deletePassword();
            case "savePasswordEdits" -> savePasswordEdits();
            case "searchNotes" -> searchNotes(noteSearchField.getText());
            case "searchPasswords" -> searchPasswords(pwdSearchField.getText());
        }
    }

    private JButton makeButton(String text, String actionCommand) {
        JButton button = new JButton(text);
        button.setActionCommand(actionCommand);
        button.addActionListener(this);
        return button;
    }

    private void saveNoteEdits() {
        int row = notesTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a note to save.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            int id = (int) notesModel.getValueAt(row, 0);
            String title = (String) notesModel.getValueAt(row, 1);
            String content = noteContentArea.getText();
            DatabaseManager.updateNote(id, title, content);
            JOptionPane.showMessageDialog(this, "Note saved successfully.", "Saved", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to save note: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void savePasswordEdits() {
        for (int i = 0; i < passwordsModel.getRowCount(); i++) {
            try {
                int id = ((Number) passwordsModel.getValueAt(i, 0)).intValue();
                String title = passwordsModel.getValueAt(i, 1).toString();
                String user = passwordsModel.getValueAt(i, 2).toString();
                String pass = passwordsModel.getValueAt(i, 3).toString();
                DatabaseManager.updatePassword(id, title, user, pass);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error saving row " + (i+1) + ": " + ex.getMessage(), "Save Error", JOptionPane.ERROR_MESSAGE);
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
        if (title != null && !title.trim().isEmpty()) {
            String content = JOptionPane.showInputDialog(this, "Note Content:");
            DatabaseManager.saveNote(username, title, content);
            loadAll();
        }
    }

    private void deleteNote() {
        int r = notesTable.getSelectedRow();
        if (r >= 0) {
            int id = (int) notesTable.getValueAt(r, 0);
            if (JOptionPane.showConfirmDialog(this, "Delete this note?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                DatabaseManager.deleteNote(id);
                noteContentArea.setText("");
                loadAll();
            }
        }
    }

    private void addPassword() {
        JPanel p = new JPanel(new GridLayout(3, 2, 5, 5));
        JTextField t = new JTextField();
        JTextField u = new JTextField();
        JTextField pw = new JPasswordField();
        p.add(new JLabel("Title:")); p.add(t);
        p.add(new JLabel("Username:")); p.add(u);
        p.add(new JLabel("Password:")); p.add(pw);
        if (JOptionPane.showConfirmDialog(this, p, "Add Password", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            DatabaseManager.savePassword(username, t.getText(), u.getText(), pw.getText());
            loadAll();
        }
    }

    private void deletePassword() {
        int selectedRow = passwordsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a password to delete", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int id = ((Number) passwordsTable.getValueAt(selectedRow, 0)).intValue();
        if (JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this password?", "Delete Password", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            DatabaseManager.deletePassword(id);
            loadAll();
        }
    }
}