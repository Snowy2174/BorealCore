package plugin.borealcore;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import fr.minuskube.inv.InventoryManager;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import plugin.borealcore.commands.*;
import plugin.borealcore.database.SQLite;
import plugin.borealcore.functions.cooking.CompetitionManager;
import plugin.borealcore.functions.cooking.CookingManager;
import plugin.borealcore.database.Database;
import plugin.borealcore.functions.duels.DuelsManager;
import plugin.borealcore.functions.jade.JadeManager;
import plugin.borealcore.functions.karmicnode.NodeManager;
import plugin.borealcore.functions.plushies.PlushieManager;
import plugin.borealcore.functions.wiki.WikiManager;
import plugin.borealcore.listener.BendingListener;
import plugin.borealcore.manager.*;
import plugin.borealcore.manager.configs.EffectManager;
import plugin.borealcore.manager.configs.LayoutManager;
import plugin.borealcore.manager.configs.RecipeManager;
import plugin.borealcore.utility.AdventureUtil;
import plugin.borealcore.utility.ConfigUtil;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class BorealCore extends JavaPlugin {

    public static BorealCore plugin;
    public static BukkitAudiences adventure;
    public static ProtocolManager protocolManager;
    private static CookingManager cookingManager;
    private static CompetitionManager competitionManager;
    private static GuiManager guiManager;
    private static RecipeManager recipeManager;
    private static PlaceholderManager placeholderManager;
    private static LayoutManager layoutManager;
    private static EffectManager effectManager;
    private static FurnitureManager furnitureManager;
    private static MasteryManager masteryManager;
    private static InventoryManager inventoryManager;
    private static NodeManager nodeManager;
    private static JadeManager jadeManager;
    private static Database db;
    private static WikiManager wikiManager;
    private static CraftingManager craftingManager;
    private static AnalyticsManager analyticsManager;
    private static PlushieManager plushieManager;
    private static DuelsManager duelsManager;

    @Override
    public void onLoad() {
        plugin = this;
    }

    @Override
    public void onEnable() {
        adventure = BukkitAudiences.create(this);
        protocolManager = ProtocolLibrary.getProtocolManager();
        inventoryManager = new InventoryManager(this);

        cookingManager = new CookingManager();
        competitionManager = new CompetitionManager();
        layoutManager = new LayoutManager();
        effectManager = new EffectManager();
        furnitureManager = new FurnitureManager();
        masteryManager = new MasteryManager();
        recipeManager = new RecipeManager();
        guiManager = new GuiManager();
        placeholderManager = new PlaceholderManager();
        nodeManager = new NodeManager();
        wikiManager = new WikiManager();
        db = new SQLite(this);
        jadeManager = new JadeManager(db);
        craftingManager = new CraftingManager();
        analyticsManager = new AnalyticsManager(db);
        plushieManager = new PlushieManager();
        duelsManager = new DuelsManager();

        reloadConfig();
        getCommand("cooking").setExecutor(new CookCommand());
        getCommand("cooking").setTabCompleter(new CookTabCompletion());
        getCommand("jade").setExecutor(new JadeCommand());
        getCommand("jade").setTabCompleter(new JadeTabCompletion());
        getCommand("kn").setExecutor(new NodeCommand());
        getCommand("wiki").setExecutor(new WikiCommand());
        getCommand("wiki").setTabCompleter(new WikiTabCompletion());
        getCommand("plushies").setExecutor(new GambleCommand());

        Bukkit.getPluginManager().registerEvents(new BendingListener(), this);

        // @TODO
        // Debug what this does and if it is still needed
        Bukkit.getScheduler().runTaskLater(this, new Runnable() {
            @Override
            public void run() {
                Bukkit.dispatchCommand((CommandSender)Bukkit.getConsoleSender(), "mythicmobs reload");
            }
        }, 100L);

        AdventureUtil.consoleMessage("[BorealCore] Plugin Enabled!");
    }

    @Override
    public void onDisable() {

        cookingManager.unload();
        competitionManager.unload();
        placeholderManager.unload();
        recipeManager.unload();
        layoutManager.unload();
        effectManager.unload();
        guiManager.unload();
        jadeManager.unload();
        nodeManager.unload();
        wikiManager.unload();
        craftingManager.unload();
        furnitureManager.unload();
        masteryManager.unload();
        analyticsManager.unload();
        plushieManager.unload();
        duelsManager.unload();
        db.unload();


        AdventureUtil.consoleMessage("[BorealCore] Plugin Disabled!");

        if (adventure != null) {
            adventure.close();
            adventure = null;
        }
    }

    @Override
    public void reloadConfig() {
        ConfigUtil.reload();
    }

    public static BorealCore getInstance() {
        return plugin;
    }
    public static CookingManager getCookingManager() {
        return cookingManager;
    }
    public static CompetitionManager getCompetitionManager() {
        return competitionManager;
    }
    public static PlaceholderManager getPlaceholderManager() {
        return placeholderManager;
    }
    public static LayoutManager getLayoutManager() {
        return layoutManager;
    }
    public static GuiManager getGuiManager() {
        return guiManager;
    }
    public static FurnitureManager getFurnitureManager() {
        return furnitureManager;
    }

    public static RecipeManager getRecipeManager() {
        return recipeManager;
    }
    public static EffectManager getEffectManager() {
        return effectManager;
    }

    public static MasteryManager getMasteryManager() {
        return masteryManager;
    }
    public static JadeManager getJadeManager() {
        return jadeManager;
    }
    public static InventoryManager getInventoryManager() {
        return inventoryManager;
    }
    public static Database getDatabase() {
        return db;
    }
    public static NodeManager getNodeManager() {
        return nodeManager;
    }
    public static WikiManager getWikiManager() {
        return wikiManager;
    }
    public static CraftingManager getCraftingManager() {
        return craftingManager;
    }
    public static AnalyticsManager getAnalyticsManager() {
        return analyticsManager;
    }
    public static PlushieManager getPlushieManager() {
        return plushieManager;
    }
    public static DuelsManager getDuelsManager() {
        return duelsManager;
    }

    public static void disablePlugin(String errorMessage, Exception e) {
        plugin.getLogger().log(Level.SEVERE, errorMessage, e);
        plugin.getServer().getPluginManager().disablePlugin(plugin);
    }
}