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
        Connection conn = DriverManager.getConnection(URL);
        try (Statement st = conn.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON");
        }
        return conn;
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

            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS vitals (
                    id           INTEGER PRIMARY KEY AUTOINCREMENT,
                    character_id INTEGER NOT NULL,
                    hr           INTEGER,
                    sbp          REAL,
                    dbp          REAL,
                    map          REAL,
                    rr           INTEGER,
                    spo2         REAL,
                    glucose      REAL,
                    temp         REAL,
                    recorded_at  TEXT NOT NULL DEFAULT (datetime('now')),
                    FOREIGN KEY (character_id) REFERENCES characters(id)
                        ON DELETE CASCADE
                );
            """);

            st.executeUpdate("""
                CREATE INDEX IF NOT EXISTS idx_vitals_character_recorded
                ON vitals(character_id, recorded_at);
            """);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
