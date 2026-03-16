package physiosim.db;

import java.sql.*;

public class UserRepository {

    private final Connection conn;

    public UserRepository(Connection conn) {
        this.conn = conn;
    }

    public boolean register(String userId, String email, String plainPassword, String role) {
        if (userId == null || email == null || plainPassword == null) return false;
        if (userId.isBlank() || email.isBlank() || plainPassword.isBlank()) return false;

        String sql = """
            INSERT INTO users (user_id, email, password_hash, role, created_at)
            VALUES (?, ?, ?, ?, ?);
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId.trim());
            ps.setString(2, email.trim());
            ps.setString(3, Passwords.hash(plainPassword));
            ps.setString(4, role == null ? "CLINICIAN" : role.trim());
            ps.setLong(5, System.currentTimeMillis());
            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("[register] " + e.getMessage());
            return false;
        }
    }

    public boolean login(String userId, String plainPassword) {
        if (userId == null || plainPassword == null) return false;
        if (userId.isBlank() || plainPassword.isBlank()) return false;

        String sql = """
            SELECT password_hash FROM users
            WHERE user_id = ?;
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return false;

                String storedHash = rs.getString("password_hash");
                return Passwords.verify(plainPassword, storedHash);
            }
        } catch (SQLException e) {
            System.err.println("[login] " + e.getMessage());
            return false;
        }
    }
}
