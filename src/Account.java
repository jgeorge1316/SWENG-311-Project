public class Account {
    private int accountId;
    private int userId;
    private String accountName;
    private String username;
    private String password;
    private String url;
    private String notes;

    public Account(int accountId, int userId, String accountName, String username,
                   String password, String url, String notes) {
        this.accountId = accountId;
        this.userId = userId;
        this.accountName = accountName;
        this.username = username;
        this.password = password;
        this.url = url;
        this.notes = notes;
    }

    public Account(int userId, String accountName, String username,
                   String password, String url, String notes) {
        this(-1, userId, accountName, username, password, url, notes);
    }
    public int getAccountId() {

        return accountId;
    }

    public int getUserId() {

        return userId;
    }

    public String getAccountName() {

        return accountName;
    }

    public void setAccountName(String accountName) {

        this.accountName = accountName;
    }

    public String getUsername() {

        return username;
    }

    public void setUsername(String username) {

        this.username = username;
    }

    public String getPassword() {

        return password;
    }

    public void setPassword(String password) {

        this.password = password;
    }

    public String getUrl() {

        return url;
    }

    public void setUrl(String url) {

        this.url = url;
    }

    public String getNotes() {

        return notes;
    }

    public void setNotes(String notes) {

        this.notes = notes;
    }

    @Override
    public String toString() {

        return accountName + " (" + username + ")";
    }
}