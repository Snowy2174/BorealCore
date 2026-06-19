package plugin.borealcore.traps;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import plugin.borealcore.database.DatabaseErrors;
import plugin.borealcore.database.DatabaseManager;
import plugin.borealcore.utility.SerialisationUtil;
import plugin.borealcore.traps.object.Trap;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import static plugin.borealcore.utility.AdventureUtil.consoleMessage;

public class TrapsDatabase {

    private final DatabaseManager coreDbManager;
    private final Plugin plugin;

    public TrapsDatabase(DatabaseManager coreDbManager) {
        this.coreDbManager = coreDbManager;
        this.plugin = coreDbManager.plugin;
    }

    public List<Trap> getActiveFishingTraps() {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            List<Trap> fishingTraps = new ArrayList<>();
            conn = coreDbManager.getConnection("jade_transactions");
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
            plugin.getLogger().log(Level.SEVERE, DatabaseErrors.sqlConnectionExecute(), ex);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to deserialize items", e);
            e.printStackTrace();
        } finally {
            coreDbManager.close(ps, rs, conn);
        }
        return null;
    }

    public Trap getFishingTrapById(String uuid) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = coreDbManager.getConnection("jade_transactions");
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
            plugin.getLogger().log(Level.SEVERE, DatabaseErrors.sqlConnectionExecute(), ex);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to deserialize items", e);
            e.printStackTrace();
        } finally {
            coreDbManager.close(ps, rs, conn);
        }
        return null;
    }

    public void saveFishingTrap(Trap trap) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = coreDbManager.getConnection("jade_transactions");
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
            plugin.getLogger().log(Level.SEVERE, DatabaseErrors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, DatabaseErrors.sqlConnectionClose(), ex);
            }
        }
    }

    public void deleteFishingTrapById(String uuid) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = coreDbManager.getConnection("jade_transactions");
            ps = conn.prepareStatement("DELETE FROM fishing_traps WHERE uuid = ?");
            ps.setString(1, uuid);
            ps.executeUpdate();
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, DatabaseErrors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
                consoleMessage("Deleted fishing trap with UUID: " + uuid);
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, DatabaseErrors.sqlConnectionClose(), ex);
            }
        }
    }
}
