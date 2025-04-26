import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.util.Base64;

public class HighSecurityAccount extends Account {
    // Encryption constants
    private static final String ALGORITHM = "AES";
    private String masterPassword;

    /* Constructor with accountId */
    public HighSecurityAccount(int accountId, int userId, String accountName, String username,
                               String password, String url, String notes, String masterPassword) {
        super(accountId, userId, accountName, username, encrypt(password, masterPassword), url, notes);
        this.masterPassword = masterPassword;
    }

    /* Constructor without accountID */
    public HighSecurityAccount(int userId, String accountName, String username,
                               String password, String url, String notes, String masterPassword) {
        super(userId, accountName, username, encrypt(password, masterPassword), url, notes);
        this.masterPassword = masterPassword;
    }

    /**
     * Override setUsername method
     */
    @Override
    public void setUsername(String username) {
        // Implement additional validation or logging for high security
        super.setUsername(username);
    }

    /**
     * Override getPassword method to decrypt before returning
     */
    @Override
    public String getPassword() {
        // Decrypt the password before returning it
        return decrypt(super.getPassword(), masterPassword);
    }

    /* Override setPassword method to encrypt before storing */
    @Override
    public void setPassword(String password) {
        // Encrypt the password before storing it
        super.setPassword(encrypt(password, masterPassword));
    }

    /**
     * Update the master password and re-encrypt stored credentials
     */
    public void updateMasterPassword(String newMasterPassword) {
        // Get the current password in plain text
        String currentPassword = getPassword();
        // Store the new master password
        this.masterPassword = newMasterPassword;
        // Re-encrypt the password with the new master password
        super.setPassword(encrypt(currentPassword, newMasterPassword));
    }

    /**
     * Encrypt data using strong encryption
     */
    private static String encrypt(String data, String masterPassword) {
        try {
            if (data == null) return null;

            SecretKey key = generateKey(masterPassword);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] encryptedData = cipher.doFinal(data.getBytes());
            return Base64.getEncoder().encodeToString(encryptedData);
        } catch (Exception e) {
            throw new RuntimeException("Error encrypting data", e);
        }
    }

    /**
     * Decrypt data using strong decryption
     */
    private static String decrypt(String encryptedData, String masterPassword) {
        try {
            if (encryptedData == null) return null;

            SecretKey key = generateKey(masterPassword);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] decodedData = Base64.getDecoder().decode(encryptedData);
            byte[] decryptedData = cipher.doFinal(decodedData);
            return new String(decryptedData);
        } catch (Exception e) {
            throw new RuntimeException("Error decrypting data", e);
        }
    }

    /**
     * Generate encryption key from master password
     */
    private static SecretKey generateKey(String masterPassword) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] key = digest.digest(masterPassword.getBytes());
        return new SecretKeySpec(key, ALGORITHM);
    }
}