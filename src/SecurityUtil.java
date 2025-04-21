import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class SecurityUtil {
    private static final String ALGORITHM = "AES";
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String CHAR_LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String CHAR_UPPER = CHAR_LOWER.toUpperCase();
    private static final String NUMBER = "0123456789";
    private static final String SPECIAL = "!@#$%^&*()_+-=[]{}|;:,.<>?";
    private static final String DATA_FOR_RANDOM_STRING = CHAR_LOWER + CHAR_UPPER + NUMBER + SPECIAL;

    /* Generate key from master password*/
    private static SecretKey generateKey(String masterPassword) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] key = digest.digest(masterPassword.getBytes());
        return new SecretKeySpec(key, ALGORITHM);
    }

    /* Generate a secure random password*/
    public static String generateRandomPassword(int length, boolean useLower, boolean useUpper,
                                                boolean useDigits, boolean useSpecial) {
        if (length <= 0) {
            throw new IllegalArgumentException("Password length must be positive");
        }

        StringBuilder password = new StringBuilder(length);
        String dataForPassword = "";

        if (useLower) dataForPassword += CHAR_LOWER;
        if (useUpper) dataForPassword += CHAR_UPPER;
        if (useDigits) dataForPassword += NUMBER;
        if (useSpecial) dataForPassword += SPECIAL;

        if (dataForPassword.isEmpty()) {
            dataForPassword = DATA_FOR_RANDOM_STRING;
        }

        for (int i = 0; i < length; i++) {
            int randomIndex = RANDOM.nextInt(dataForPassword.length());
            password.append(dataForPassword.charAt(randomIndex));
        }

        return password.toString();
    }
}