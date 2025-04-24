import java.util.Vector;

public class SharedAccount extends Account {
    private Vector<Integer> sharedUserIds;

    // Constructor for new shared account
    public SharedAccount(int accountId, Vector<Integer> sharedUserIds, String accountName, String username,
                         String password, String url, String notes) {
        super(accountId, -1, accountName, username, password, url, notes); // -1 or ignore single userId
        this.sharedUserIds = new Vector<>(sharedUserIds);
    }

    // Overloaded constructor for accounts without predefined ID
    public SharedAccount(Vector<Integer> sharedUserIds, String accountName, String username,
                         String password, String url, String notes) {
        this(-1, sharedUserIds, accountName, username, password, url, notes);
    }

    public Vector<Integer> getSharedUserIds() {
        return sharedUserIds;
    }

    public void addUserId(int userId) {
        if (!sharedUserIds.contains(userId)) {
            sharedUserIds.add(userId);
        }
    }

    public void removeUserId(int userId) {
        sharedUserIds.remove(Integer.valueOf(userId));
    }

    public boolean isAccessibleBy(int userId) {
        return sharedUserIds.contains(userId);
    }

    @Override
    public String toString() {
        return "[Shared] " + getAccountName() + " (" + getUsername() + ") | Shared with: " + sharedUserIds.toString();
    }
}
