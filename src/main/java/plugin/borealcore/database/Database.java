package plugin.borealcore.database;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Consumer;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;
import plugin.borealcore.BorealCore;
import plugin.borealcore.functions.jade.LeaderboardType;
import plugin.borealcore.functions.jade.object.JadeTransaction;
import plugin.borealcore.functions.jade.object.Leaderboard;
import plugin.borealcore.functions.jade.object.LeaderboardEntry;
import plugin.borealcore.functions.traps.Trap;
import plugin.borealcore.manager.configs.DebugLevel;
import plugin.borealcore.object.Function;
import plugin.borealcore.utility.AdventureUtil;
import plugin.borealcore.utility.SerialisationUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static java.lang.Double.parseDouble;
import static plugin.borealcore.functions.jade.JadeManager.jadeSources;
import static plugin.borealcore.utility.AdventureUtil.consoleMessage;

public abstract class Database extends Function {
    public final BorealCore plugin;
    public Connection connection;
    public String table;
    private final ConcurrentLinkedQueue<JadeTransaction> pendingTransactions = new ConcurrentLinkedQueue<>();

    public Database(BorealCore instance) {
        plugin = instance;
    }

    public abstract Connection getSQLConnection();

    public abstract void load();

    public abstract void unload();

    public void initialize() {
        this.connection = this.getSQLConnection();
        try {
            PreparedStatement ps = this.connection.prepareStatement("SELECT * FROM " + this.table + " WHERE uuid = ?");
            ResultSet rs = ps.executeQuery();
            this.close(ps, rs);
            consoleMessage("Loaded SQLiteJade database");
        } catch (SQLException ex) {
            BorealCore.disablePlugin("Unable to retrieve connection during database initialization", ex);
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
            ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now().minusHours(24)));
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
            ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now().minusHours(24)));
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
                ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now().minusDays(30)));
                rs = ps.executeQuery();

                while (rs.next()) {
                    String source = rs.getString("source");
                    double total = rs.getDouble("total");
                    sourceJadeMap.put(source, total);
                }

                if (sourceJadeMap.isEmpty()) {
                    sourceJadeMap.put("not_in_database", 0.0);
                } else {
                    sourceJadeMap.put("not_in_last_24_hours", 0.0);
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

                if (actualTotal < 0) {
                    int offsetAmount = Math.abs(actualTotal);
                    String insertTransactionQuery = "INSERT INTO jade_transactions (player, uuid, amount, source, timestamp) VALUES (?, ?, ?, ?, ?);";
                    PreparedStatement psOffsetTransaction = conn.prepareStatement(insertTransactionQuery);
                    psOffsetTransaction.setString(1, player);
                    psOffsetTransaction.setString(2, uuid);
                    psOffsetTransaction.setInt(3, offsetAmount);
                    psOffsetTransaction.setString(4, "migration");
                    psOffsetTransaction.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
                    psOffsetTransaction.executeUpdate();
                    psOffsetTransaction.close();

                    plugin.getLogger().info("Added correction transaction for uuid " + uuid + " (player: " + player + "): Offset amount = " + offsetAmount);
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

    public void purgeUser(String uuid) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = this.getSQLConnection();
            String query = "DELETE FROM jade_totals WHERE uuid = ?;";
            ps = conn.prepareStatement(query);
            ps.setString(1, uuid);
            ps.executeUpdate();

            query = "DELETE FROM jade_transactions WHERE uuid = ?;";
            ps = conn.prepareStatement(query);
            ps.setString(1, uuid);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), e);
        } finally {
            closeResources(conn, ps, null);
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
            String orderBy = " ORDER BY jade DESC LIMIT 50;";

            // Add conditions based on type
            switch (type) {
                case CURRENT -> condition = " GROUP BY uuid, player";
                case ALLTIME -> condition = " WHERE amount > 0 GROUP BY uuid, player";
                case SPENT -> {
                    condition = " WHERE amount < 0 GROUP BY uuid, player";
                    orderBy = " ORDER BY jade ASC LIMIT 50;";
                }
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
                    AdventureUtil.consoleMessage(DebugLevel.ERROR,"Unknown leaderboard type: " + type);
                    return null;
                }
            }
            String query = baseQuery + condition + orderBy;
            ps = conn.prepareStatement(query);
            if (requiresTimestamp) {
                ps.setTimestamp(1, Timestamp.valueOf(
                        LocalDateTime.now().minusDays(type.name().contains("WEEKLY") ? 7 : 30)
                ));
            }
            rs = ps.executeQuery();
            while (rs.next()) {
                int position = rs.getInt("position");
                UUID uuid = UUID.fromString(rs.getString("uuid"));
                String playerName = rs.getString("player");
                int jadeAmount = Math.abs(rs.getInt("jade"));
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
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.getName().equalsIgnoreCase(playerName)) {
                return onlinePlayer.getUniqueId().toString();
            }
        }
        return null;
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

    public Map<String, Double> getMostUsedSources(Duration duration) {
        String query = "SELECT source, SUM(amount) AS total FROM jade_transactions WHERE source != '' AND amount > 0 AND timestamp >= ? GROUP BY source";
        Map<String, Double> sourceUsage = new HashMap<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement(query);
            ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now().minus(duration)));
            rs = ps.executeQuery();

            while (rs.next()) {
                sourceUsage.put(rs.getString("source"), rs.getDouble("total"));
            }

            double total = sourceUsage.values().stream().mapToDouble(Double::doubleValue).sum();
            return sourceUsage.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, entry -> (entry.getValue() / total) * 100));
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), e);
            return Collections.emptyMap();
        } finally {
            closeResources(conn, ps, rs);
        }
    }

    public Map<String, Map<String, Double>> getSourceDistributionChanges() {
        String query = "SELECT strftime('%Y-%W', timestamp) AS week, source, SUM(amount) AS total FROM jade_transactions WHERE source != '' AND timestamp >= ? GROUP BY week, source";
        Map<String, Map<String, Double>> weeklyDistribution = new HashMap<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement(query);
            ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now().minusMonths(3)));
            rs = ps.executeQuery();

            while (rs.next()) {
                String week = rs.getString("week");
                String source = rs.getString("source");
                double total = rs.getDouble("total");

                weeklyDistribution.computeIfAbsent(week, k -> new HashMap<>()).put(source, total);
            }
            return weeklyDistribution;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), e);
            return Collections.emptyMap();
        } finally {
            closeResources(conn, ps, rs);
        }
    }

    public Map<String, Double> getAveragePlayerGainPerWeek() {
        String query = "SELECT strftime('%Y-%W', timestamp) AS week, uuid, SUM(amount) AS total FROM jade_transactions WHERE amount > 0 GROUP BY week, uuid";
        Map<String, Map<String, Double>> weeklyPlayerGains = new HashMap<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement(query);
            rs = ps.executeQuery();

            while (rs.next()) {
                String week = rs.getString("week");
                String playerId = rs.getString("uuid");
                double total = rs.getDouble("total");

                weeklyPlayerGains.computeIfAbsent(week, k -> new HashMap<>()).put(playerId, total);
            }

            Map<String, Double> averageGains = new HashMap<>();
            for (Map.Entry<String, Map<String, Double>> entry : weeklyPlayerGains.entrySet()) {
                String week = entry.getKey();
                Map<String, Double> playerGains = entry.getValue();
                double average = playerGains.values().stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
                averageGains.put(week, average);
            }
            return averageGains;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), e);
            return Collections.emptyMap();
        } finally {
            closeResources(conn, ps, rs);
        }
    }

    public Map<String, Integer> getSourceDependencySpread() {
        String query = "SELECT source, COUNT(DISTINCT uuid) AS users FROM jade_transactions WHERE source != '' AND amount > 0 GROUP BY source";
        Map<String, Integer> sourceSpread = new HashMap<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement(query);
            rs = ps.executeQuery();

            while (rs.next()) {
                sourceSpread.put(rs.getString("source"), rs.getInt("users"));
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), e);
        } finally {
            closeResources(conn, ps, rs);
        }
        return sourceSpread;
    }

    public double getTop10PercentPlayersShare() {
        String query = "SELECT uuid, SUM(amount) AS total FROM jade_transactions WHERE amount > 0 GROUP BY uuid ORDER BY total DESC";
        List<Double> totals = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement(query);
            rs = ps.executeQuery();

            while (rs.next()) {
                totals.add(rs.getDouble("total"));
            }

            int top10PercentCount = (int) Math.ceil(totals.size() * 0.1);
            double top10PercentTotal = totals.stream().limit(top10PercentCount).mapToDouble(Double::doubleValue).sum();
            double overallTotal = totals.stream().mapToDouble(Double::doubleValue).sum();

            return (overallTotal == 0) ? 0 : (top10PercentTotal / overallTotal) * 100;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), e);
            return 0;
        } finally {
            closeResources(conn, ps, rs);
        }
    }

    public Map<String, Double> getSourceEfficiencyAnalysis() {
        String query = """
                    SELECT source, SUM(amount) AS total, COUNT(DISTINCT uuid) AS users
                    FROM jade_transactions
                    WHERE source != '' AND amount > 0
                    GROUP BY source
                """;
        Map<String, Double> sourceEfficiency = new HashMap<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement(query);
            rs = ps.executeQuery();

            while (rs.next()) {
                String source = rs.getString("source");
                double total = rs.getDouble("total");
                int users = rs.getInt("users");
                sourceEfficiency.put(source, (users == 0) ? 0 : total / users);
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), e);
        } finally {
            closeResources(conn, ps, rs);
        }
        return sourceEfficiency;
    }

    // playerRecipeDataExists;
    // updatePlayerRecipeData;
    // updateRecipeStatus;


    public List<Trap> getActiveFishingTraps() {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            List<Trap> fishingTraps = new ArrayList<>();
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT * FROM fishing_traps WHERE active = 1");
            rs = ps.executeQuery();

            while (rs.next()) {
                UUID uuid = UUID.fromString(rs.getString("uuid"));
                UUID owner = UUID.fromString(rs.getString("owner"));
                String key = rs.getString("key");
                Location location = SerialisationUtil.deserializeLocation(rs.getString("location"));
                boolean active = rs.getInt("active") == 1;
                List<ItemStack> items = SerialisationUtil.deserializeItems(rs.getString("items"));
                int maxItems = rs.getInt("maxItems");
                ItemStack bait = SerialisationUtil.deserializeItems(rs.getString("bait")).get(0);

                fishingTraps.add(new Trap(key, uuid, owner, location, active, items, maxItems, bait));
            }
            return fishingTraps;
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to deserialize items", e);
            e.printStackTrace();
        } finally {
            closeResources(conn, ps, rs);
        }
        return null;
    }

    public Trap getFishingTrapById(String uuid) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT * FROM fishing_traps WHERE uuid = ?");
            ps.setString(1, uuid);
            rs = ps.executeQuery();

            if (rs.next()) {
                UUID owner = UUID.fromString(rs.getString("owner"));
                String key = rs.getString("key");
                Location location = SerialisationUtil.deserializeLocation(rs.getString("location"));
                boolean active = rs.getInt("active") == 1;
                List<ItemStack> items = SerialisationUtil.deserializeItems(rs.getString("items"));
                int maxItems = rs.getInt("maxItems");
                ItemStack bait = SerialisationUtil.deserializeItems(rs.getString("bait")).get(0);

                return new Trap(key, UUID.fromString(uuid), owner, location, active, items, maxItems, bait);
            }
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to deserialize items", e);
            e.printStackTrace();
        } finally {
            closeResources(conn, ps, rs);
        }
        return null;
    }

    public void saveFishingTrap(Trap trap) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("REPLACE INTO fishing_traps(uuid, owner, key, location, active, items, maxItems, bait) VALUES(?, ?, ?, ?, ?, ?, ?, ?)");

            ps.setString(1, trap.getUuid().toString());
            ps.setString(2, trap.getOwner().toString());
            ps.setString(3, trap.getKey());
            ps.setString(4, SerialisationUtil.serializeLocation(trap.getLocation()));
            ps.setInt(5, trap.isActive() ? 1 : 0);
            ps.setString(6, SerialisationUtil.serializeItems(trap.getItems()));
            ps.setInt(7, trap.getMaxItems());
            ps.setString(8, SerialisationUtil.serializeItems(Collections.singletonList(trap.getBait())));

            ps.executeUpdate();
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
    }

    public void deleteFishingTrapById(String uuid) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("DELETE FROM fishing_traps WHERE uuid = ?");
            ps.setString(1, uuid);
            ps.executeUpdate();
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
                consoleMessage("Deleted fishing trap with UUID: " + uuid);
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
    }

    public List<ItemStack> getIngredientBagItems(Player player) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<ItemStack> items = new ArrayList<>();
        try {
            conn = this.getSQLConnection();
            String query = "SELECT items FROM ingredient_bag WHERE uuid = ?;";
            ps = conn.prepareStatement(query);
            ps.setString(1, player.getUniqueId().toString());
            rs = ps.executeQuery();

            if (rs.next()) {
                String serializedItems = rs.getString("items");
                if (serializedItems != null && !serializedItems.isEmpty()) {
                    items = SerialisationUtil.deserializeItems(serializedItems);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            closeResources(conn, ps, rs);
        }
        return items;
    }

    public void saveIngredientBagItems(Player player, List<ItemStack> items) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = this.getSQLConnection();
            String query = "REPLACE INTO ingredient_bag (uuid, items) VALUES (?, ?);";
            ps = conn.prepareStatement(query);
            ps.setString(1, player.getUniqueId().toString());
            ps.setString(2, SerialisationUtil.serializeItems(items));
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), e);
        } finally {
            closeResources(conn, ps, null);
        }
    }


}