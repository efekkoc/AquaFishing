package me.furkankoc.aquafishing.database;

import me.furkankoc.aquafishing.codex.PlayerCodex;
import me.furkankoc.aquafishing.codex.PlayerFishProgress;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.sql.*;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

/**
 * SQLite implementation of CodexRepository.
 * Uses Paper's built-in SQLite driver and parameterized prepared statements for high performance and safety.
 */
public class SqliteCodexRepository implements CodexRepository {

    private final Plugin plugin;
    private final File dbFile;
    private Connection connection;

    public SqliteCodexRepository(Plugin plugin) {
        this.plugin = plugin;
        this.dbFile = new File(plugin.getDataFolder(), "aquafishing.db");
    }

    @Override
    public synchronized void init() {
        try {
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }
            Class.forName("org.sqlite.JDBC");
            this.connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());

            try (Statement stmt = connection.createStatement()) {
                stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS aquafishing_codex (
                        player_uuid VARCHAR(36) NOT NULL,
                        fish_id VARCHAR(64) NOT NULL,
                        catch_count INTEGER NOT NULL DEFAULT 1,
                        max_weight REAL NOT NULL DEFAULT 0.0,
                        first_discovered INTEGER NOT NULL,
                        PRIMARY KEY (player_uuid, fish_id)
                    );
                """);
                // Index on player_uuid for fast loading
                stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_codex_uuid ON aquafishing_codex(player_uuid);");
            }
            plugin.getLogger().info("SQLite veritabanı başarıyla bağlandı ve şema doğrulandı.");
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "SQLite veritabanı başlatılamadı!", e);
        }
    }

    private synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
        }
        return connection;
    }

    @Override
    public synchronized PlayerCodex load(UUID uuid) {
        PlayerCodex codex = new PlayerCodex(uuid);
        String sql = "SELECT fish_id, catch_count, max_weight, first_discovered FROM aquafishing_codex WHERE player_uuid = ?";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String fishId = rs.getString("fish_id");
                    int count = rs.getInt("catch_count");
                    double maxWeight = rs.getDouble("max_weight");
                    long firstTime = rs.getLong("first_discovered");

                    codex.addLoadedProgress(fishId, new PlayerFishProgress(count, maxWeight, firstTime));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "SQLite'tan oyuncu verisi çekilemedi: " + uuid, e);
        }

        return codex;
    }

    @Override
    public synchronized void save(PlayerCodex codex) {
        String upsertSql = """
            INSERT INTO aquafishing_codex (player_uuid, fish_id, catch_count, max_weight, first_discovered)
            VALUES (?, ?, ?, ?, ?)
            ON CONFLICT(player_uuid, fish_id) DO UPDATE SET
                catch_count = excluded.catch_count,
                max_weight = excluded.max_weight;
        """;

        try {
            Connection conn = getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(upsertSql)) {
                String uuidStr = codex.getPlayerUuid().toString();
                for (Map.Entry<String, PlayerFishProgress> entry : codex.getDiscoveries().entrySet()) {
                    ps.setString(1, uuidStr);
                    ps.setString(2, entry.getKey());
                    ps.setInt(3, entry.getValue().getCatchCount());
                    ps.setDouble(4, entry.getValue().getMaxWeight());
                    ps.setLong(5, entry.getValue().getFirstDiscoveredMillis());
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            conn.commit();
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "SQLite'a veri kaydedilemedi: " + codex.getPlayerUuid(), e);
        }
    }

    @Override
    public synchronized void saveAll(Collection<PlayerCodex> codices) {
        for (PlayerCodex codex : codices) {
            save(codex);
        }
    }

    @Override
    public synchronized void close() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.WARNING, "SQLite bağlantısı kapatılırken hata oluştu", e);
            }
        }
    }
}
