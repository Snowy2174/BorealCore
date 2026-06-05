package plugin.borealcore.cooking;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import plugin.borealcore.database.DatabaseErrors;
import plugin.borealcore.database.DatabaseManager;
import plugin.borealcore.utility.SerialisationUtil;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class CookingDatabase {

    private final DatabaseManager coreDbManager;
    private final Plugin plugin;

    public CookingDatabase(DatabaseManager coreDbManager) {
        this.coreDbManager = coreDbManager;
        this.plugin = coreDbManager.plugin;
    }

    // playerRecipeDataExists;
    // updatePlayerRecipeData;
    // updateRecipeStatus;

    public List<ItemStack> getIngredientBagItems(Player player) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<ItemStack> items = new ArrayList<>();
        try {
            conn = coreDbManager.getConnection("plugin/borealcore/cooking");
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
            plugin.getLogger().log(Level.SEVERE, DatabaseErrors.sqlConnectionExecute(), e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            coreDbManager.close(ps, rs, conn);
        }
        return items;
    }

    public void saveIngredientBagItems(Player player, List<ItemStack> items) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = coreDbManager.getConnection("plugin/borealcore/cooking");
            String query = "REPLACE INTO ingredient_bag (uuid, items) VALUES (?, ?);";
            ps = conn.prepareStatement(query);
            ps.setString(1, player.getUniqueId().toString());
            ps.setString(2, SerialisationUtil.serializeItems(items));
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, DatabaseErrors.sqlConnectionExecute(), e);
        } finally {
            coreDbManager.close(ps, null, conn);
        }
    }


}
