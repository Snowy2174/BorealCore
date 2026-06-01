package plugin.borealcore.depreciated;

import org.bukkit.plugin.Plugin;
import plugin.borealcore.database.DatabaseManager;
import plugin.borealcore.database.Errors;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class AnalyticsDatabase {

    private final DatabaseManager coreDbManager;
    private final Plugin plugin;

    public AnalyticsDatabase(DatabaseManager coreDbManager) {
        this.coreDbManager = coreDbManager;
        this.plugin = coreDbManager.plugin;
    }


    public Map<String, Double> getMostUsedSources(Duration duration) {
        String query = "SELECT source, SUM(amount) AS total FROM jade_transactions WHERE source != '' AND amount > 0 AND timestamp >= ? GROUP BY source";
        Map<String, Double> sourceUsage = new HashMap<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = coreDbManager.getConnection("jade_transactions");
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
            coreDbManager.close(ps, rs);
        }
    }

    public Map<String, Map<String, Double>> getSourceDistributionChanges() {
        String query = "SELECT strftime('%Y-%W', timestamp) AS week, source, SUM(amount) AS total FROM jade_transactions WHERE source != '' AND timestamp >= ? GROUP BY week, source";
        Map<String, Map<String, Double>> weeklyDistribution = new HashMap<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = coreDbManager.getConnection("jade_transactions");
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
            coreDbManager.close(ps, rs);
        }
    }

    public Map<String, Double> getAveragePlayerGainPerWeek() {
        String query = "SELECT strftime('%Y-%W', timestamp) AS week, uuid, SUM(amount) AS total FROM jade_transactions WHERE amount > 0 GROUP BY week, uuid";
        Map<String, Map<String, Double>> weeklyPlayerGains = new HashMap<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = coreDbManager.getConnection("jade_transactions");
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
            coreDbManager.close(ps, rs);
        }
    }

    public Map<String, Integer> getSourceDependencySpread() {
        String query = "SELECT source, COUNT(DISTINCT uuid) AS users FROM jade_transactions WHERE source != '' AND amount > 0 GROUP BY source";
        Map<String, Integer> sourceSpread = new HashMap<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = coreDbManager.getConnection("jade_transactions");
            ps = conn.prepareStatement(query);
            rs = ps.executeQuery();

            while (rs.next()) {
                sourceSpread.put(rs.getString("source"), rs.getInt("users"));
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), e);
        } finally {
            coreDbManager.close(ps, rs);
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
            conn = coreDbManager.getConnection("jade_transactions");
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
            coreDbManager.close(ps, rs);
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
            conn = coreDbManager.getConnection("jade_transactions");
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
            coreDbManager.close(ps, rs);
        }
        return sourceEfficiency;
    }
}
