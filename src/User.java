import java.util.Vector;

public class User {
    private int userId;
    private String username;
    private String masterPassword;
    private Vector<Account> accounts;

    public User(int userId, String username, String masterPassword) {
        this.userId = userId;
        this.username = username;
        this.masterPassword = masterPassword;
    }

    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getMasterPassword() {
        return masterPassword;
    }

    public void setMasterPassword(String masterPassword) {
        this.masterPassword = masterPassword;
    }
}