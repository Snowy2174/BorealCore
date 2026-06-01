package plugin.borealcore.database;

import plugin.borealcore.BorealCore;
import plugin.borealcore.object.Function;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import static plugin.borealcore.utility.AdventureUtil.consoleMessage;

public class DatabaseManager extends Function {

    public final BorealCore plugin;

    // Stores active connections by their database name (e.g., "traps" -> Connection)
    private final Map<String, Connection> activeConnections = new ConcurrentHashMap<>();

    public DatabaseManager(BorealCore instance) {
        this.plugin = instance;
    }

    /**
     * Retrieves an existing connection or creates a new one for the specified database name.
     * Modules will call this method passing their desired database name (e.g., "jade_transactions").
     */
    public Connection getConnection(String dbName) {
        // Return existing connection if it is active and open
        if (activeConnections.containsKey(dbName)) {
            Connection conn = activeConnections.get(dbName);
            try {
                if (conn != null && !conn.isClosed()) {
                    return conn;
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.WARNING, "Connection to " + dbName + " was closed unexpectedly. Reconnecting...");
            }
        }

        try {
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }

            File dataFolder = new File(plugin.getDataFolder(), dbName + ".db");
            if (!dataFolder.exists()) {
                dataFolder.createNewFile();
            }

            Class.forName("org.sqlite.JDBC");
            Connection connection = DriverManager.getConnection("jdbc:sqlite:" + dataFolder);
            activeConnections.put(dbName, connection);

            consoleMessage("Loaded SQLite database: " + dbName + ".db");
            return connection;

        } catch (Exception ex) {
            BorealCore.disablePlugin("Unable to retrieve connection for database: " + dbName, ex);
            return null;
        }
    }

    /**
     * Safely closes query resources.
     * Note: Modules should NOT close the Connection object directly, as it is cached and shared.
     */
    public void close(PreparedStatement ps, ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
            if (ps != null) {
                ps.close();
            }
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, "Failed to close SQL resources", ex);
        }
    }

    public void unload() {
        for (Map.Entry<String, Connection> entry : activeConnections.entrySet()) {
            try {
                Connection conn = entry.getValue();
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                    consoleMessage("Closed SQLite database: " + entry.getKey() + ".db");
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to close database: " + entry.getKey(), e);
            }
        }
        activeConnections.clear();
    }
}