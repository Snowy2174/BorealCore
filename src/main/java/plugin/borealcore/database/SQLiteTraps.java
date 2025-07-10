package plugin.borealcore.database;

import plugin.borealcore.BorealCore;
import plugin.borealcore.utility.AdventureUtil;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;

public class SQLiteTraps extends Database {
    public String SQLiteCreateTrapsTable = "CREATE TABLE IF NOT EXISTS fishing_traps (" +
            "`id` TEXT NOT NULL," +
            "`owner` TEXT NOT NULL," +
            "`key` TEXT NOT NULL," +
            "`location` TEXT NOT NULL," +
            "`active` INTEGER NOT NULL," +
            "`items` TEXT," +
            "`maxItems` INTEGER NOT NULL," +
            "`bait` TEXT," +
            "PRIMARY KEY (`id`)" +
            ");";
    String dbname;

    public SQLiteTraps(BorealCore instance) {
        super(instance);
        dbname = "traps";
    }

    public Connection getSQLConnection() {
        Connection result = null;
        File dataFolder = new File(plugin.getDataFolder(), dbname + ".db");
        if (!dataFolder.exists()) {
            try {
                dataFolder.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "File write error: " + dbname + ".db");
            }
        }
        try {
            if (connection != null && !connection.isClosed()) {
                result = connection;
            } else {
                Class.forName("org.sqlite.JDBC");
                connection = DriverManager.getConnection("jdbc:sqlite:" + dataFolder);
                result = connection;
            }
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, "SQLiteJade exception on initialize", ex);
        } catch (ClassNotFoundException ex) {
            plugin.getLogger().log(Level.SEVERE, "You need the SQLiteJade JBDC library.");
        }
        return result;
    }

    @Override
    public void load() {
        AdventureUtil.consoleMessage("Loading SQLiteJade database...");
        connection = getSQLConnection();
        try {
            Statement s = connection.createStatement();
            s.executeUpdate(SQLiteCreateTrapsTable);
            s.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        initialize();
    }

    @Override
    public void unload() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.commit();
                    connection.close();
                    plugin.getLogger().info("Database connection closed successfully.");
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Error closing database connection", e);
            }
        }
    }
}