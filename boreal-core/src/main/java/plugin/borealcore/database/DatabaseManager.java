package plugin.borealcore.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import plugin.borealcore.BorealCore;
import plugin.borealcore.api.Function;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import static plugin.borealcore.utility.AdventureUtil.consoleMessage;

public class DatabaseManager extends Function {

    public final BorealCore plugin;

    private final Map<String, HikariDataSource> dataSources = new ConcurrentHashMap<>();

    public DatabaseManager(BorealCore instance) {
        this.plugin = instance;
    }

    /**
     * Retrieves a connection from the pool for the specified database name.
     * Modules will call this method passing their desired database name (e.g., "jade_transactions").
     */
    public Connection getConnection(String dbName) {
        if (dataSources.containsKey(dbName)) {
            try {
                return dataSources.get(dbName).getConnection();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to get connection from pool: " + dbName, e);
                return null;
            }
        }

        try {
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }

            File dbFile = new File(plugin.getDataFolder(), dbName + ".db");

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:sqlite:" + dbFile.getAbsolutePath());
            config.setMaximumPoolSize(5);
            config.setMinimumIdle(1);
            config.setConnectionTimeout(30000);
            config.setIdleTimeout(600000);
            config.setMaxLifetime(1800000);
            config.setAutoCommit(true);

            HikariDataSource dataSource = new HikariDataSource(config);
            dataSources.put(dbName, dataSource);

            consoleMessage("Loaded SQLite database with HikariCP: " + dbName + ".db");
            return dataSource.getConnection();

        } catch (Exception ex) {
            BorealCore.disablePlugin("Unable to retrieve connection for database: " + dbName, ex);
            return null;
        }
    }

    /**
     * Safely closes query resources.
     * Note: Modules should NOT close the Connection object directly, as it is pooled by HikariCP.
     */
    public void close(PreparedStatement ps, ResultSet rs, Connection conn) {
        try {
            if (rs != null) {
                rs.close();
            }
            if (ps != null) {
                ps.close();
            }
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, "Failed to close SQL resources", ex);
        }
    }

    public void unload() {
        for (Map.Entry<String, HikariDataSource> entry : dataSources.entrySet()) {
            try {
                HikariDataSource dataSource = entry.getValue();
                if (dataSource != null && !dataSource.isClosed()) {
                    dataSource.close();
                    consoleMessage("Closed SQLite database: " + entry.getKey() + ".db");
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to close database: " + entry.getKey(), e);
            }
        }
        dataSources.clear();
    }
}