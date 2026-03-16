// src/physiosim/db/Database.java
package physiosim.db;

import java.sql.*;

public final class Database {

    private static final String URL = "jdbc:sqlite:data/physiosim.db";

    static {
        init();
    }

    private Database() {}

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    private static void init() {
        try (Connection conn = getConnection();
             Statement st = conn.createStatement()) {

            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS users (
                    user_id       TEXT PRIMARY KEY,
                    email         TEXT NOT NULL UNIQUE,
                    password_hash TEXT NOT NULL,
                    role          TEXT NOT NULL CHECK (role IN ('CLINICIAN','RESEARCHER')),
                    created_at    INTEGER NOT NULL
                );
            """);

            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS characters (
                    id         INTEGER PRIMARY KEY AUTOINCREMENT,
                    owner_id   TEXT    NOT NULL,      -- users.user_id 참조
                    name       TEXT    NOT NULL,
                    sex        TEXT,
                    birth      TEXT CHECK (length(birth) = 8 OR birth IS NULL),
                    height_cm  REAL,
                    weight_kg  REAL,
                    created_at INTEGER NOT NULL,
                    FOREIGN KEY (owner_id) REFERENCES users(user_id)
                );
            """);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
