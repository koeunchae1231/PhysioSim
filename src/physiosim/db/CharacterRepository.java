package physiosim.db;

import java.sql.*;
import java.util.*;

public class CharacterRepository {

    private final Connection conn;

    public CharacterRepository(Connection conn) {
        this.conn = Objects.requireNonNull(conn, "conn is null");
    }

    // 캐릭터 등록
    public int insert(String ownerUserId,
                      String name,
                      String sex,
                      String birth,
                      Double heightCm,
                      Double weightKg
    ) throws SQLException {

        if (ownerUserId == null || ownerUserId.isBlank())
            throw new IllegalArgumentException("ownerUserId is blank");
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("name is blank");

        final String sql = """
            INSERT INTO characters(owner_id, name, sex, birth, height_cm, weight_kg, created_at)
            VALUES (?,?,?,?,?,?,?)
        """;

        try (PreparedStatement ps =
                     conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // 1. owner_id
            ps.setString(1, ownerUserId.trim());

            // 2. name
            ps.setString(2, name.trim());

            // 3. sex
            if (sex == null || sex.isBlank()) ps.setNull(3, Types.VARCHAR);
            else ps.setString(3, sex.trim());

            // 4. birth (YYYYMMDD)
            if (birth == null || birth.isBlank()) ps.setNull(4, Types.VARCHAR);
            else ps.setString(4, birth.trim());

            // 5. height_cm
            if (heightCm == null) ps.setNull(5, Types.REAL);
            else ps.setDouble(5, heightCm);

            // 6. weight_kg
            if (weightKg == null) ps.setNull(6, Types.REAL);
            else ps.setDouble(6, weightKg);

            // 7. created_at (epoch millis)
            ps.setLong(7, System.currentTimeMillis());

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                return rs.next() ? rs.getInt(1) : -1;
            }
        }
    }

    // id로 한 개 조회
    public CharacterRow findById(int id) throws SQLException {
        final String sql = """
            SELECT id, owner_id, name, sex, birth, height_cm, weight_kg, created_at
              FROM characters
             WHERE id = ?
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    // 특정 유저의 전체 캐릭터 목록
    public java.util.List<CharacterRow> findByOwner(String ownerUserId) throws SQLException {
        final String sql = """
            SELECT id, owner_id, name, sex, birth, height_cm, weight_kg, created_at
              FROM characters
             WHERE owner_id = ?
             ORDER BY created_at DESC, id DESC
        """;
        java.util.List<CharacterRow> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ownerUserId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    public boolean delete(int id) throws SQLException {
        final String sql = "DELETE FROM characters WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private static CharacterRow mapRow(ResultSet rs) throws SQLException {
        return new CharacterRow(
                rs.getInt("id"),
                rs.getString("owner_id"),
                rs.getString("name"),
                rs.getString("sex"),
                rs.getString("birth"),
                rs.getObject("height_cm", Double.class),
                rs.getObject("weight_kg", Double.class),
                rs.getLong("created_at")
        );
    }

    // DTO
    public static record CharacterRow(
            int id,
            String ownerId,
            String name,
            String sex,
            String birth,
            Double heightCm,
            Double weightKg,
            long createdAt
    ) {}
}
