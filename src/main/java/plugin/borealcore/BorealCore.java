package plugin.borealcore;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import fr.minuskube.inv.InventoryManager;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.plugin.java.JavaPlugin;
import plugin.borealcore.commands.*;
import plugin.borealcore.database.Database;
import plugin.borealcore.database.SQLiteData;
import plugin.borealcore.database.SQLiteJade;
import plugin.borealcore.depreciated.AnalyticsManager;
import plugin.borealcore.depreciated.CraftingManager;
import plugin.borealcore.functions.bending.BendingManager;
import plugin.borealcore.functions.brewery.BreweryManager;
import plugin.borealcore.functions.cooking.CookingCompetitionManager;
import plugin.borealcore.functions.cooking.CookingManager;
import plugin.borealcore.functions.cooking.MasteryManager;
import plugin.borealcore.functions.cooking.configs.LayoutManager;
import plugin.borealcore.functions.cooking.configs.RecipeManager;
import plugin.borealcore.functions.duels.DuelsManager;
import plugin.borealcore.functions.herbalism.HerbalismManager;
import plugin.borealcore.functions.herbalism.configs.HerbManager;
import plugin.borealcore.functions.jade.JadeManager;
import plugin.borealcore.functions.karmicnode.NodeManager;
import plugin.borealcore.functions.plushies.PlushieManager;
import plugin.borealcore.functions.traps.TrapsManager;
import plugin.borealcore.functions.wiki.WikiManager;
import plugin.borealcore.manager.EffectManager;
import plugin.borealcore.manager.FurnitureManager;
import plugin.borealcore.manager.GuiManager;
import plugin.borealcore.manager.PlaceholderManager;
import plugin.borealcore.utility.AdventureUtil;
import plugin.borealcore.utility.ConfigUtil;

import java.util.logging.Level;

public class BorealCore extends JavaPlugin {

    public static BorealCore plugin;
    public static BukkitAudiences adventure;
    public static ProtocolManager protocolManager;
    private static CookingManager cookingManager;
    private static HerbalismManager herbalismManager;
    private static CookingCompetitionManager competitionManager;
    private static GuiManager guiManager;
    private static RecipeManager recipeManager;
    private static HerbManager herbManager;
    private static PlaceholderManager placeholderManager;
    private static LayoutManager layoutManager;
    private static EffectManager effectManager;
    private static FurnitureManager furnitureManager;
    private static MasteryManager masteryManager;
    private static InventoryManager inventoryManager;
    private static NodeManager nodeManager;
    private static JadeManager jadeManager;
    private static Database db;
    private static Database traps;
    private static WikiManager wikiManager;
    private static CraftingManager craftingManager;
    private static AnalyticsManager analyticsManager;
    private static PlushieManager plushieManager;
    private static DuelsManager duelsManager;
    private static BendingManager bendingManager;
    private static BreweryManager breweryManager;
    private static TrapsManager trapsManager;

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
        herbalismManager = new HerbalismManager();
        competitionManager = new CookingCompetitionManager();
        layoutManager = new LayoutManager();
        effectManager = new EffectManager();
        furnitureManager = new FurnitureManager();
        masteryManager = new MasteryManager();
        recipeManager = new RecipeManager();
        herbManager = new HerbManager();
        guiManager = new GuiManager();
        placeholderManager = new PlaceholderManager();
        nodeManager = new NodeManager();
        wikiManager = new WikiManager();
        db = new SQLiteJade(this);
        traps = new SQLiteData(this);
        jadeManager = new JadeManager(db);
        craftingManager = new CraftingManager();
        analyticsManager = new AnalyticsManager(db);
        plushieManager = new PlushieManager();
        duelsManager = new DuelsManager();
        bendingManager = new BendingManager();
        breweryManager = new BreweryManager();
        trapsManager = new TrapsManager();

        reloadConfig();
        getCommand("cooking").setExecutor(new CookCommand());
        getCommand("cooking").setTabCompleter(new CookTabCompletion());
        getCommand("jade").setExecutor(new JadeCommand());
        getCommand("jade").setTabCompleter(new JadeTabCompletion());
        getCommand("kn").setExecutor(new NodeCommand());
        getCommand("wiki").setExecutor(new WikiCommand());
        getCommand("wiki").setTabCompleter(new WikiTabCompletion());
        getCommand("plushies").setExecutor(new GambleCommand());
        getCommand("recipes").setExecutor(new RecipeBookCommand());
        getCommand("recipes").setTabCompleter(new RecipeBookTabCompletion());
        getCommand("herbalism").setExecutor(new HerbalismCommand());
        //getCommand("herbalism").setTabCompleter(new HerbalismTabCompletion());#
        getCommand("traps").setExecutor(new TrapsCommand());
        getCommand("sit").setExecutor(new SitCommand(cookingManager.getSitListener()));

        AdventureUtil.consoleMessage("Plugin Enabled!");
    }

    @Override
    public void onDisable() {

        cookingManager.unload();
        herbalismManager.unload();
        competitionManager.unload();
        placeholderManager.unload();
        recipeManager.unload();
        herbManager.unload();
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
        bendingManager.unload();
        breweryManager.unload();
        db.unload();
        traps.unload();

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

    public static CookingCompetitionManager getCompetitionManager() {
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

    public static BendingManager getBendingManager() {
        return bendingManager;
    }

    public static BreweryManager getBreweryManager() {
        return breweryManager;
    }

    public static HerbManager getHerbManager() {
        return herbManager;
    }

    public static HerbalismManager getHerbalismManager() {
        return herbalismManager;
    }

    public static Database getTrapsDatabase() {
        return traps;
    }

    public static TrapsManager getTrapsManager() {
        return trapsManager;
    }

    public static void disablePlugin(String errorMessage, Exception e) {
        plugin.getLogger().log(Level.SEVERE, errorMessage, e);
        plugin.getServer().getPluginManager().disablePlugin(plugin);
    }
}