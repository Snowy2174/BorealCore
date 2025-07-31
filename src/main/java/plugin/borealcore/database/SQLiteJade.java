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

public class SQLiteJade extends Database {
    public String SQLiteCreateTokensTable = "CREATE TABLE IF NOT EXISTS jade_transactions (" + // make sure to put your table name in here too.
            "`player` varchar(32) NOT NULL," + // This creates the different columns you will save data to. varchar(32) Is a string, int = integer
            "`amount` int(11) NOT NULL," +
            "`uuid` VARCHAR(36)," +
            "`source` varchar(32) NOT NULL," +
            "`timestamp` datetime NOT NULL," +
            "PRIMARY KEY (`player`, `timestamp`)" +  // This is creating 4 columns: player, amount, source, timestamp. Primary key is a combination of player and timestamp.
            ");"; // we can search by player and timestamp to get the amount and source.
    public String SQLiteCreateUsersTable = "CREATE TABLE IF NOT EXISTS jade_totals (" +
            "    `player` varchar(32) NOT NULL PRIMARY KEY," +
            "    `uuid` VARCHAR(36)," +
            "    `jade` int(11) NOT NULL" +
            ");"; // we can search by player and timestamp to get the amount and source.
    public String SQLiteCreateRecipesTable = "CREATE TABLE IF NOT EXISTS recipe_data (" +
            "uuid VARCHAR(36) NOT NULL," +
            "recipe_type VARCHAR(32) NOT NULL," +
            "recipe_name VARCHAR(64) NOT NULL," +
            "mastery_count INT DEFAULT 0," +
            "PRIMARY KEY (uuid, recipe_type, recipe_name)" +
            ");";
    String dbname;

    public SQLiteJade(BorealCore instance) {
        super(instance);
        dbname = "jade_transactions";
        table = "jade_transactions";
    }

    public Connection getSQLConnection() {
        Connection result = null;
        File dataFolder = new File(plugin.getDataFolder(), dbname + ".db");
        if (!dataFolder.exists()) {
            try {
                dataFolder.createNewFile();
            } catch (IOException e) {
                AdventureUtil.consoleMessage(DebugLevel.ERROR,  "File write error: " + dbname + ".db");
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
            AdventureUtil.consoleMessage(DebugLevel.ERROR,  "You need the SQLiteJade JBDC library.");
        }
        return result;
    }

    @Override
    public void load() {
        AdventureUtil.consoleMessage("Loading SQLiteJade database...");
        connection = getSQLConnection();
        try {
            Statement s = connection.createStatement();
            s.executeUpdate(SQLiteCreateTokensTable);
            s.executeUpdate(SQLiteCreateUsersTable);
            // s.executeUpdate(SQLiteCreateRecipesTable);
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