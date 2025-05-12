package plugin.customcooking.database;

import com.bencodez.votingplugin.VotingPluginHooks;
import com.bencodez.votingplugin.user.VotingPluginUser;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Consumer;
import plugin.customcooking.CustomCooking;
import plugin.customcooking.functions.jade.JadeTransaction;
import plugin.customcooking.functions.jade.Leaderboard;
import plugin.customcooking.functions.jade.LeaderboardEntry;
import plugin.customcooking.functions.jade.LeaderboardType;
import plugin.customcooking.object.Function;
import plugin.customcooking.utility.AdventureUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;

import static plugin.customcooking.functions.jade.JadeManager.jadeSources;

public abstract class Database extends Function {
    public final CustomCooking plugin;
    public Connection connection;
    public String table = "jade_transactions";
    private final ConcurrentLinkedQueue<JadeTransaction> pendingTransactions = new ConcurrentLinkedQueue<>();

    public Database(CustomCooking instance) {
        plugin = instance;
    }

    public abstract Connection getSQLConnection();
    public abstract void load();
    public abstract void unload();

    public void initialize() {
        this.connection = this.getSQLConnection();
        try {
            PreparedStatement ps = this.connection.prepareStatement("SELECT * FROM " + this.table + " WHERE player = ?");
            ResultSet rs = ps.executeQuery();
            this.close(ps, rs);
            AdventureUtil.consoleMessage("[CustomCooking] Loaded SQLite database");
        } catch (SQLException ex) {
            CustomCooking.disablePlugin("Unable to retrieve connection during database initialization", ex);
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

    private void closeResources(Connection conn, PreparedStatement ps, ResultSet rs) {
        try {
            if (rs != null) rs.close();
            if (ps != null) ps.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), e);
        }
    }



    private int getSingleIntResult(String query, Object... params) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = this.getSQLConnection();
            ps = conn.prepareStatement(query);

            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }

            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), e);
        } finally {
            closeResources(conn, ps, rs);
        }

        return 0;
    }

    public void getJadeForPlayerAsync(Player player, Consumer<Integer> callback) {
        new BukkitRunnable() {
            @Override
            public void run() {
                int jade = getJadeForPlayer(player);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        callback.accept(jade);
                    }
                }.runTask(plugin);
            }
        }.runTaskAsynchronously(plugin);
    }

    public int getJadeForPlayer(Player player) {
        String query = "SELECT jade FROM jade_totals WHERE uuid = ?;";
        return getSingleIntResult(query, player.getUniqueId().toString());
    }

    public int getTotalJadeFromSource(String source) {
        String query = "SELECT SUM(amount) AS total FROM jade_transactions WHERE source = ?;";
        return getSingleIntResult(query, source);
    }

    public LocalDateTime getLastTransactionTimestamp(Player player, String source) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = this.getSQLConnection();
            String query = "SELECT timestamp FROM jade_transactions WHERE uuid = ? AND source = ? ORDER BY timestamp DESC LIMIT 1;";
            ps = conn.prepareStatement(query);
            ps.setString(1, player.getUniqueId().toString());
            ps.setString(2, source);
            rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getTimestamp("timestamp").toLocalDateTime();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), e);
        } finally {
            closeResources(conn, ps, rs);
        }
        return null;
    }

    public List<LocalDateTime> getRecentPositiveTransactionTimestamps(Player player, String source) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<LocalDateTime> timestamps = new ArrayList<>();

        try {
            conn = this.getSQLConnection();
            String query = "SELECT timestamp FROM jade_transactions WHERE uuid = ? AND source = ? AND amount > 0 AND timestamp >= ? ORDER BY timestamp DESC;";
            ps = conn.prepareStatement(query);
            ps.setString(1, player.getUniqueId().toString());
            ps.setString(2, source);
            ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now().minus(24, ChronoUnit.HOURS)));
            rs = ps.executeQuery();

            while (rs.next()) {
                timestamps.add(rs.getTimestamp("timestamp").toLocalDateTime());
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), e);
        } finally {
            closeResources(conn, ps, rs);
        }
        return timestamps;
    }

    public boolean isOnCooldown(Player player, String source) {
        LocalDateTime lastTransactionTime = getLastTransactionTimestamp(player, source);
        if (lastTransactionTime == null) return false;

        LocalDateTime cooldownEndTime = lastTransactionTime.plusSeconds(jadeSources.get(source).getCooldown());
        return LocalDateTime.now().isBefore(cooldownEndTime);
    }

    public long getCooldownTimeLeft(Player player, String source) {
        LocalDateTime lastTransactionTime = getLastTransactionTimestamp(player, source);
        if (lastTransactionTime == null) return 0;

        LocalDateTime cooldownEndTime = lastTransactionTime.plusSeconds(jadeSources.get(source).getCooldown());
        return ChronoUnit.SECONDS.between(LocalDateTime.now(), cooldownEndTime);
    }

    public HashMap<String, Double> getJadeFromSources(Player player) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        HashMap<String, Double> sourceJadeMap = new HashMap<>();

        try {
            conn = this.getSQLConnection();
            String query = "SELECT source, SUM(amount) AS total FROM jade_transactions WHERE uuid = ? AND amount > 0 AND timestamp >= ? GROUP BY source";

            // Check for transactions in the last 24 hours
            ps = conn.prepareStatement(query);
            ps.setString(1, player.getUniqueId().toString());
            ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now().minus(24, ChronoUnit.HOURS)));
            rs = ps.executeQuery();

            while (rs.next()) {
                String source = rs.getString("source");
                double total = rs.getDouble("total");
                sourceJadeMap.put(source, total);
            }

            if (sourceJadeMap.isEmpty()) {
                rs.close();
                ps.close();

                ps = conn.prepareStatement(query);
                ps.setString(1, player.getUniqueId().toString());
                ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now().minus(30, ChronoUnit.DAYS)));
                rs = ps.executeQuery();

                while (rs.next()) {
                    String source = rs.getString("source");
                    double total = rs.getDouble("total");
                    sourceJadeMap.put(source, total);
                }

                if (sourceJadeMap.isEmpty()) {
                    sourceJadeMap.put("not_in_database", 0.0);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), e);
        } finally {
            closeResources(conn, ps, rs);
        }

        return sourceJadeMap;
    }

    public void addTransaction(JadeTransaction transaction) {
        Connection conn = null;
        PreparedStatement psTransaction = null;
        PreparedStatement psTotals = null;

        try {
            conn = this.getSQLConnection();
            conn.setAutoCommit(false);

            String transactionQuery = "INSERT INTO jade_transactions (player, uuid, amount, source, timestamp) VALUES (?, ?, ?, ?, ?);";
            psTransaction = conn.prepareStatement(transactionQuery);
            psTransaction.setString(1, transaction.getPlayer());
            psTransaction.setString(2, transaction.getUuid());
            psTransaction.setDouble(3, transaction.getAmount());
            psTransaction.setString(4, transaction.getSource());
            psTransaction.setTimestamp(5, Timestamp.valueOf(transaction.getTimestamp()));
            psTransaction.executeUpdate();

            // Update or insert the player's total in jade_totals
            String totalsQuery = """
                        INSERT INTO jade_totals (player, uuid, jade)
                        VALUES (?, ?, ?)
                        ON CONFLICT(uuid) DO UPDATE SET jade = jade + ?;
                    """;
            psTotals = conn.prepareStatement(totalsQuery);
            psTotals.setString(1, transaction.getPlayer());
            psTotals.setString(2, transaction.getUuid());
            psTotals.setDouble(3, transaction.getAmount());
            psTotals.setDouble(4, transaction.getAmount());
            psTotals.executeUpdate();

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback on failure
                } catch (SQLException rollbackEx) {
                    plugin.getLogger().log(Level.SEVERE, "Transaction rollback failed", rollbackEx);
                }
            }
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), e);
            pendingTransactions.add(transaction);
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

    public void startRetryTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                while (!pendingTransactions.isEmpty()) {
                    JadeTransaction transaction = pendingTransactions.poll();
                    if (transaction != null) {
                        addTransaction(
                            transaction
                        );
                    }
                }
            }
        }.runTaskTimerAsynchronously(plugin, 0L, 20L * 60);
    }

    public void verifyAndFixTotals() {
        Connection conn = null;
        PreparedStatement psTransactions = null;
        PreparedStatement psTotals = null;
        ResultSet rsPlayers = null;

        try {
            conn = this.getSQLConnection();

            String queryPlayers = "SELECT uuid, player, jade FROM jade_totals;";
            psTotals = conn.prepareStatement(queryPlayers);
            rsPlayers = psTotals.executeQuery();

            while (rsPlayers.next()) {
                String uuid = rsPlayers.getString("uuid");
                String player = rsPlayers.getString("player");
                int recordedTotal = rsPlayers.getInt("jade");

                String queryTransactions = "SELECT SUM(amount) AS total FROM jade_transactions WHERE uuid = ?;";
                psTransactions = conn.prepareStatement(queryTransactions);
                psTransactions.setString(1, uuid);
                ResultSet rsTransactions = psTransactions.executeQuery();

                int actualTotal = 0;
                if (rsTransactions.next()) {
                    actualTotal = rsTransactions.getInt("total");
                }
                rsTransactions.close();
                psTransactions.close();

                if (recordedTotal != actualTotal) {
                    plugin.getLogger().warning("Discrepancy found for uuid " + uuid + " (player: " + player + "): Recorded total = " + recordedTotal + ", Actual total = " + actualTotal);

                    String fixQuery = "UPDATE jade_totals SET jade = ?, player = ? WHERE uuid = ?;";
                    PreparedStatement psFix = conn.prepareStatement(fixQuery);
                    psFix.setInt(1, actualTotal);
                    psFix.setString(2, player);
                    psFix.setString(3, uuid);
                    psFix.executeUpdate();
                    psFix.close();

                    plugin.getLogger().info("Fixed total for uuid " + uuid + " (player: " + player + "): Updated total = " + actualTotal);
                }
            }

            String queryMissingPlayers = "SELECT uuid, player, SUM(amount) AS total FROM jade_transactions WHERE uuid NOT IN (SELECT uuid FROM jade_totals) GROUP BY uuid, player;";
            psTransactions = conn.prepareStatement(queryMissingPlayers);
            ResultSet rsMissingPlayers = psTransactions.executeQuery();

            while (rsMissingPlayers.next()) {
                String uuid = rsMissingPlayers.getString("uuid");
                String player = rsMissingPlayers.getString("player");
                int total = rsMissingPlayers.getInt("total");

                String insertQuery = "INSERT INTO jade_totals (uuid, player, jade) VALUES (?, ?, ?);";
                PreparedStatement psInsert = conn.prepareStatement(insertQuery);
                psInsert.setString(1, uuid);
                psInsert.setString(2, player);
                psInsert.setInt(3, total);
                psInsert.executeUpdate();
                psInsert.close();

                plugin.getLogger().info("Inserted new uuid " + uuid + " (player: " + player + ") with total jade = " + total);
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

    public Leaderboard queryLeaderboard(LeaderboardType type) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<LeaderboardEntry> leaderboard = new ArrayList<>();
        try {
            conn = this.getSQLConnection();

            // Base query
            String baseQuery = """
            SELECT ROW_NUMBER() OVER (ORDER BY SUM(amount) DESC) AS position, uuid, player, SUM(amount) AS jade
            FROM jade_transactions
        """;
            String condition = "";
            boolean requiresTimestamp = false;

            // Add conditions based on type
            switch (type) {
                case CURRENT -> condition = " GROUP BY uuid, player";
                case ALLTIME -> condition = " WHERE amount > 0 GROUP BY uuid, player";
                case FARMING -> condition = " WHERE source = 'farming' GROUP BY uuid, player";
                case FARMINGMONTHLY -> {
                    condition = " WHERE source = 'farming' AND timestamp >= ? GROUP BY uuid, player";
                    requiresTimestamp = true;
                }
                case FARMINGWEEKLY -> {
                    condition = " WHERE source = 'farming' AND timestamp >= ? GROUP BY uuid, player";
                    requiresTimestamp = true;
                }
                case COOKING -> condition = " WHERE source = 'cooking' GROUP BY uuid, player";
                case COOKINGMONTHLY -> {
                    condition = " WHERE source = 'cooking' AND timestamp >= ? GROUP BY uuid, player";
                    requiresTimestamp = true;
                }
                case COOKINGWEEKLY -> {
                    condition = " WHERE source = 'cooking' AND timestamp >= ? GROUP BY uuid, player";
                    requiresTimestamp = true;
                }
                case BREWING -> condition = " WHERE source = 'brewing' GROUP BY uuid, player";
                case BREWINGMONTHLY -> {
                    condition = " WHERE source = 'brewing' AND timestamp >= ? GROUP BY uuid, player";
                    requiresTimestamp = true;
                }
                case BREWINGWEEKLY -> {
                    condition = " WHERE source = 'brewing' AND timestamp >= ? GROUP BY uuid, player";
                    requiresTimestamp = true;
                }
                case FISHING -> condition = " WHERE source = 'fishing' GROUP BY uuid, player";
                case FISHINGMONTHLY -> {
                    condition = " WHERE source = 'fishing' AND timestamp >= ? GROUP BY uuid, player";
                    requiresTimestamp = true;
                }
                case FISHINGWEEKLY -> {
                    condition = " WHERE source = 'fishing' AND timestamp >= ? GROUP BY uuid, player";
                    requiresTimestamp = true;
                }
                default -> {
                    plugin.getLogger().warning("Unknown leaderboard type: " + type);
                    return null;
                }
            }

            // Final query
            String query = baseQuery + condition + " ORDER BY jade DESC LIMIT 50;";
            ps = conn.prepareStatement(query);

            // Set timestamp if required
            if (requiresTimestamp) {
                ps.setTimestamp(1, Timestamp.valueOf(
                        LocalDateTime.now().minus(type.name().contains("WEEKLY") ? 7 : 30, ChronoUnit.DAYS)
                ));
            }

            rs = ps.executeQuery();

            while (rs.next()) {
                int position = rs.getInt("position");
                UUID uuid = UUID.fromString(rs.getString("uuid"));
                String playerName = rs.getString("player");
                int jadeAmount = rs.getInt("jade");
                leaderboard.add(new LeaderboardEntry(uuid, playerName, jadeAmount, position));
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), e);
        } finally {
            closeResources(conn, ps, rs);
        }
        return new Leaderboard(type, leaderboard);
    }

    private String resolveUUID(String playerName) {
        // Attempt to resolve the UUID using the Bukkit API for online players only
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.getName().equalsIgnoreCase(playerName)) {
                return onlinePlayer.getUniqueId().toString();
            }
        }
        return null; // Return null if no match is found
    }

    public List<String> getAllTotals() {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<String> uuids = new ArrayList<>();

        try {
            conn = this.getSQLConnection();
            String query = "SELECT uuid FROM jade_totals;";
            ps = conn.prepareStatement(query);
            rs = ps.executeQuery();

            while (rs.next()) {
                uuids.add(rs.getString("uuid"));
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), e);
        } finally {
            closeResources(conn, ps, rs); // Ensure resources are closed properly
        }
        return uuids;
    }

    public List<String> getAllSources() {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<String> sources = new ArrayList<>();

        try {
            conn = this.getSQLConnection();
            String query = "SELECT DISTINCT source FROM jade_transactions;";
            ps = conn.prepareStatement(query);
            rs = ps.executeQuery();

            while (rs.next()) {
                sources.add(rs.getString("source"));
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
        return sources;
    }

    public double getTotalJadeByUUID(String uuid) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = this.getSQLConnection();
            String query = "SELECT jade FROM jade_totals WHERE uuid = ?;";
            ps = conn.prepareStatement(query);
            ps.setString(1, uuid);
            rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getDouble("jade");
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), e);
        } finally {
            closeResources(conn, ps, rs);
        }
        return 0;
    }
}
