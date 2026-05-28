package plugin.borealcore.database;

import plugin.borealcore.BorealCore;
import plugin.borealcore.manager.configs.DebugLevel;
import plugin.borealcore.utility.AdventureUtil;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;

public class SQLiteData extends Database {
    public String SQLiteCreateTrapsTable = "CREATE TABLE IF NOT EXISTS fishing_traps (" +
            "`uuid` VARCHAR(36) NOT NULL," +
            "`owner` TEXT NOT NULL," +
            "`key` TEXT NOT NULL," +
            "`location` TEXT NOT NULL," +
            "`active` INTEGER NOT NULL," +
            "`items` TEXT," +
            "`maxItems` INTEGER NOT NULL," +
            "`bait` TEXT," +
            "PRIMARY KEY (`uuid`)" +
            ");";
    public String SQLiteCreateCookingTable = "CREATE TABLE IF NOT EXISTS ingredient_bag (" +
            "`uuid` VARCHAR(36) NOT NULL," +
            "`items` TEXT," +
            "PRIMARY KEY (`uuid`)" +
            ");";
    String dbname;

    public SQLiteData(BorealCore instance) {
        super(instance);
        dbname = "traps";
        table = "fishing_traps";
    }

    public Connection getSQLConnection() {
        Connection result = null;
        File dataFolder = new File(plugin.getDataFolder(), dbname + ".db");
        if (!dataFolder.exists()) {
            try {
                dataFolder.createNewFile();
            } catch (IOException e) {
                AdventureUtil.consoleMessage(DebugLevel.ERROR, "File write error: " + dbname + ".db");
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
            AdventureUtil.consoleMessage(DebugLevel.ERROR, "You need the SQLiteJade JBDC library.");
        }
        return result;
    }

    @Override
    public void load() {
        AdventureUtil.consoleMessage("Loading SQLiteData database...");
        connection = getSQLConnection();
        try {
            Statement s = connection.createStatement();
            s.executeUpdate(SQLiteCreateTrapsTable);
            s.executeUpdate(SQLiteCreateCookingTable);
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
                    connection.close();
                    plugin.getLogger().info("Database connection closed successfully.");
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Error closing database connection", e);
            }
        }
    }
}