//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package plugin.customcooking.functions.jade;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.bukkit.entity.Player;
import plugin.customcooking.CustomCooking;

public abstract class JadeDatabase {
    static CustomCooking plugin;
    Connection connection;
    public String table = "jade_transactions";
    public int tokens = 0;

    public JadeDatabase(CustomCooking instance) {
        plugin = CustomCooking.getInstance();
    }

    public Connection getSQLConnection() {
        return null;
    }

    public abstract void dbload();

    public void initialize() {
        this.connection = this.getSQLConnection();

        try {
            PreparedStatement ps = this.connection.prepareStatement("SELECT * FROM " + this.table + " WHERE player = ?");
            ResultSet rs = ps.executeQuery();
            this.close(ps, rs);
        } catch (SQLException var3) {
            plugin.getLogger().log(Level.SEVERE, "Unable to retreive connection", var3);
        }

    }

    public Integer getPlayerData(String playerName, String column) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = this.getSQLConnection();
            ps = conn.prepareStatement("SELECT jade FROM jade_totals WHERE player = ?;");
            ps.setString(1, playerName.toLowerCase());
            rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt("jade");
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), e);
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), e);
            }
        }

        return 0;
    }

    public void addTransaction(Player player, double amount, String source, LocalDateTime timestamp) {
        Connection conn = null;
        PreparedStatement psTransaction = null;
        PreparedStatement psTotals = null;

        try {
            conn = this.getSQLConnection();

            // Add the transaction to jade_transactions
            String transactionQuery = "INSERT INTO jade_transactions (player, amount, source, timestamp) VALUES (?, ?, ?, ?);";
            psTransaction = conn.prepareStatement(transactionQuery);
            psTransaction.setString(1, player.getName().toLowerCase());
            psTransaction.setDouble(2, amount);
            psTransaction.setString(3, source);
            psTransaction.setTimestamp(4, Timestamp.valueOf(timestamp));
            psTransaction.executeUpdate();

            // Update or insert the player's total in jade_totals
            String totalsQuery = """
            INSERT INTO jade_totals (player, jade)
            VALUES (?, ?)
            ON CONFLICT(player) DO UPDATE SET jade = jade + ?;
        """;
            psTotals = conn.prepareStatement(totalsQuery);
            psTotals.setString(1, player.getName().toLowerCase());
            psTotals.setDouble(2, amount);
            psTotals.setDouble(3, amount);
            psTotals.executeUpdate();

        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), e);
        } finally {
            try {
                if (psTransaction != null) psTransaction.close();
                if (psTotals != null) psTotals.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), e);
            }
        }
    }


    public int getJadeForPlayer(Player player) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = this.getSQLConnection();
            String query = "SELECT jade FROM jade_totals WHERE player = ?;";
            ps = conn.prepareStatement(query);
            ps.setString(1, player.getName().toLowerCase());
            rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt("jade");
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), e);
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), e);
            }
        }

        return 0; // Return 0 if no record is found.
    }

    public int getTotalJadeFromSource(String source) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        int var6;
        try {
            conn = this.getSQLConnection();
            String query = "SELECT SUM(amount) AS total FROM jade_transactions WHERE source = ?;";
            ps = conn.prepareStatement(query);
            ps.setString(1, source);
            rs = ps.executeQuery();
            if (!rs.next()) {
                return 0;
            }

            var6 = rs.getInt("total");
        } catch (SQLException var17) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), var17);
            return 0;
        } finally {
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
            } catch (SQLException var16) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), var16);
            }

        }

        return var6;
    }

    public List<LocalDateTime> getRecentPositiveTransactionTimestamps(Player player, String source) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<LocalDateTime> timestamps = new ArrayList<>();

        try {
            conn = this.getSQLConnection();
            String query = "SELECT timestamp FROM jade_transactions WHERE player = ? AND source = ? AND amount > 0 AND timestamp >= ? ORDER BY timestamp DESC;";
            ps = conn.prepareStatement(query);
            ps.setString(1, player.getName().toLowerCase());
            ps.setString(2, source);
            ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now().minus(24, ChronoUnit.HOURS)));
            rs = ps.executeQuery();
            while (rs.next()) {
                timestamps.add(rs.getTimestamp("timestamp").toLocalDateTime());
            }
        } catch (SQLException var17) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), var17);
        } finally {
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
            } catch (SQLException var16) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), var16);
            }
        }

        return timestamps;
    }

    public void verifyAndFixTotals() {
        Connection conn = null;
        PreparedStatement psTransactions = null;
        PreparedStatement psTotals = null;
        ResultSet rsPlayers = null;

        try {
            conn = this.getSQLConnection();

            String queryPlayers = "SELECT player, jade FROM jade_totals;";
            psTotals = conn.prepareStatement(queryPlayers);
            rsPlayers = psTotals.executeQuery();

            while (rsPlayers.next()) {
                String player = rsPlayers.getString("player");
                int recordedTotal = rsPlayers.getInt("jade");

                String queryTransactions = "SELECT SUM(amount) AS total FROM jade_transactions WHERE player = ?;";
                psTransactions = conn.prepareStatement(queryTransactions);
                psTransactions.setString(1, player);
                ResultSet rsTransactions = psTransactions.executeQuery();

                int actualTotal = 0;
                if (rsTransactions.next()) {
                    actualTotal = rsTransactions.getInt("total");
                }
                rsTransactions.close();
                psTransactions.close();

                if (recordedTotal != actualTotal) {
                    plugin.getLogger().warning("Discrepancy found for player " + player + ": Recorded total = " + recordedTotal + ", Actual total = " + actualTotal);

                    String fixQuery = "UPDATE jade_totals SET jade = ? WHERE player = ?;";
                    PreparedStatement psFix = conn.prepareStatement(fixQuery);
                    psFix.setInt(1, actualTotal);
                    psFix.setString(2, player);
                    psFix.executeUpdate();
                    psFix.close();

                    plugin.getLogger().info("Fixed total for player " + player + ": Updated total = " + actualTotal);
                }
            }

            String queryMissingPlayers = "SELECT player, SUM(amount) AS total FROM jade_transactions WHERE player NOT IN (SELECT player FROM jade_totals) GROUP BY player;";
            psTransactions = conn.prepareStatement(queryMissingPlayers);
            ResultSet rsMissingPlayers = psTransactions.executeQuery();

            while (rsMissingPlayers.next()) {
                String player = rsMissingPlayers.getString("player");
                int total = rsMissingPlayers.getInt("total");

                String insertQuery = "INSERT INTO jade_totals (player, jade) VALUES (?, ?);";
                PreparedStatement psInsert = conn.prepareStatement(insertQuery);
                psInsert.setString(1, player);
                psInsert.setInt(2, total);
                psInsert.executeUpdate();
                psInsert.close();

                plugin.getLogger().info("Inserted new player " + player + " with total jade = " + total);
            }
            rsMissingPlayers.close();
            psTransactions.close();

        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), e);
        } finally {
            try {
                if (rsPlayers != null) rsPlayers.close();
                if (psTransactions != null) psTransactions.close();
                if (psTotals != null) psTotals.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), e);
            }
        }
    }


    public void close(PreparedStatement ps, ResultSet rs) {
        try {
            if (ps != null) {
                ps.close();
            }

            if (rs != null) {
                rs.close();
            }
        } catch (SQLException var4) {
            Error.close(plugin, var4);
        }

    }
}
