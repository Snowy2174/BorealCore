//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package plugin.customcooking.jade;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Map;
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

        Integer var6;
        try {
            conn = this.getSQLConnection();
            ps = conn.prepareStatement("SELECT " + column + " FROM " + this.table + " WHERE player = ?;");
            ps.setString(1, playerName.toLowerCase());
            rs = ps.executeQuery();
            if (!rs.next()) {
                return 0;
            }

            var6 = rs.getInt(column);
        } catch (SQLException var17) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), var17);
            return 0;
        } finally {
            try {
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

    public void setPlayerData(Player player, Map<String, Integer> data) {
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            StringBuilder columns = new StringBuilder();
            StringBuilder placeholders = new StringBuilder();
            Iterator var7 = data.keySet().iterator();

            while(var7.hasNext()) {
                String key = (String)var7.next();
                if (columns.length() > 0) {
                    columns.append(",");
                    placeholders.append(",");
                }

                columns.append(key);
                placeholders.append("?");
            }

            String query = "REPLACE INTO " + this.table + " (player," + columns + ") VALUES(?, " + placeholders + ");";
            ps = ((Connection)conn).prepareStatement(query);
            ps.setString(1, player.getName().toLowerCase());
            int index = 2;
            Iterator var9 = data.values().iterator();

            while(var9.hasNext()) {
                int value = (Integer)var9.next();
                ps.setInt(index++, value);
            }

            ps.executeUpdate();
        } catch (SQLException var19) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), var19);
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }

                if (conn != null) {
                    ((Connection)conn).close();
                }
            } catch (SQLException var18) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), var18);
            }

        }

    }

    public void addTransaction(Player player, int amount, String source, LocalDateTime timestamp) {
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = this.getSQLConnection();
            String query = "INSERT INTO jade_transactions (player, amount, source, timestamp) VALUES (?, ?, ?, ?);";
            ps = conn.prepareStatement(query);
            ps.setString(1, player.getName().toLowerCase());
            ps.setInt(2, amount);
            ps.setString(3, source);
            ps.setTimestamp(4, Timestamp.valueOf(timestamp));
            ps.executeUpdate();
        } catch (SQLException var16) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), var16);
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }

                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException var15) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), var15);
            }

        }

    }

    public int getTotalJadeForPlayer(Player player) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        int var6;
        try {
            conn = this.getSQLConnection();
            String query = "SELECT SUM(amount) AS total FROM jade_transactions WHERE player = ?;";
            ps = conn.prepareStatement(query);
            ps.setString(1, player.getName().toLowerCase());
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

    public int getTotalJade() {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        int var5;
        try {
            conn = this.getSQLConnection();
            String query = "SELECT SUM(amount) AS total FROM jade_transactions;";
            ps = conn.prepareStatement(query);
            rs = ps.executeQuery();
            if (!rs.next()) {
                return 0;
            }

            var5 = rs.getInt("total");
        } catch (SQLException var16) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), var16);
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
            } catch (SQLException var15) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), var15);
            }

        }

        return var5;
    }

    public int getTotalTransactionsForPlayer(Player player) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = this.getSQLConnection();
            String query = "SELECT COUNT(*) AS total FROM jade_transactions WHERE player = ?;";
            ps = conn.prepareStatement(query);
            ps.setString(1, player.getName().toLowerCase());
            rs = ps.executeQuery();
            if (rs.next()) {
                int var6 = rs.getInt("total");
                return var6;
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

        return 0;
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
