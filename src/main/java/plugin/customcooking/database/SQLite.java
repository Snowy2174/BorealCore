package plugin.customcooking.database;

import plugin.customcooking.CustomCooking;
import plugin.customcooking.utility.AdventureUtil;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;

public class SQLite extends Database {
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
    String dbname;

    public SQLite(CustomCooking instance) {
        super(instance);
        dbname = "jade_transactions";
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
            plugin.getLogger().log(Level.SEVERE, "SQLite exception on initialize", ex);
        } catch (ClassNotFoundException ex) {
            plugin.getLogger().log(Level.SEVERE, "You need the SQLite JBDC library.");
        }
        return result;
    }

    @Override
    public void load() {
        AdventureUtil.consoleMessage("Loading SQLite database...");
        connection = getSQLConnection();
        try {
            Statement s = connection.createStatement();
            s.executeUpdate(SQLiteCreateTokensTable);
            s.executeUpdate(SQLiteCreateUsersTable);
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