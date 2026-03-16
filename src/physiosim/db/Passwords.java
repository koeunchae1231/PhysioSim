// src/physiosim/db/Passwords.java
package physiosim.db;

import java.security.MessageDigest;
import java.util.Base64;

public final class Passwords {

    private Passwords() {}

    // 비밀번호 → SHA-256 해시(Base64)
    public static String hash(String plain) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(plain.getBytes());
            return Base64.getEncoder().encodeToString(digest);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // 해시 검증
    public static boolean verify(String plain, String storedHash) {
        return hash(plain).equals(storedHash);
    }
}
