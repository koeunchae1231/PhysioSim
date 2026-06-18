package physiosim.db;

import org.mindrot.jbcrypt.BCrypt;

public final class Passwords {

    private static final int BCRYPT_COST = 12;

    private Passwords() {}

    // Password -> bcrypt hash
    public static String hash(String plain) {
        return BCrypt.hashpw(plain, BCrypt.gensalt(BCRYPT_COST));
    }

    // bcrypt verification
    public static boolean verify(String plain, String storedHash) {
        if (plain == null || storedHash == null || storedHash.isBlank()) {
            return false;
        }

        try {
            return BCrypt.checkpw(plain, storedHash);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
