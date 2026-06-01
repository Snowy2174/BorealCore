package plugin.borealcore;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import fr.minuskube.inv.InventoryManager;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.plugin.java.JavaPlugin;
import plugin.borealcore.api.module.ModuleLoadException;
import plugin.borealcore.database.DatabaseManager;
import plugin.borealcore.depreciated.AnalyticsManager;
import plugin.borealcore.depreciated.CraftingManager;
import plugin.borealcore.functions.bending.BendingManager;
import plugin.borealcore.functions.brewery.BreweryManager;
import plugin.borealcore.functions.cooking.CookCommand;
import plugin.borealcore.functions.cooking.CookTabCompletion;
import plugin.borealcore.functions.cooking.CookingCompetitionManager;
import plugin.borealcore.functions.cooking.CookingManager;
import plugin.borealcore.functions.cooking.MasteryManager;
import plugin.borealcore.functions.cooking.RecipeBookCommand;
import plugin.borealcore.functions.cooking.RecipeBookTabCompletion;
import plugin.borealcore.functions.cooking.configs.LayoutManager;
import plugin.borealcore.functions.cooking.configs.RecipeManager;
import plugin.borealcore.functions.duels.DuelsManager;
import plugin.borealcore.functions.herbalism.HerbalismCommand;
import plugin.borealcore.functions.herbalism.HerbalismManager;
import plugin.borealcore.functions.herbalism.configs.HerbManager;
import plugin.borealcore.functions.jade.JadeCommand;
import plugin.borealcore.functions.jade.JadeManager;
import plugin.borealcore.functions.jade.JadeTabCompletion;
import plugin.borealcore.functions.karmicnode.NodeCommand;
import plugin.borealcore.functions.karmicnode.NodeManager;
import plugin.borealcore.functions.market.MarketManager;
import plugin.borealcore.functions.sit.SitCommand;
import plugin.borealcore.functions.GambleCommand;
import plugin.borealcore.functions.PlushieManager;
import plugin.borealcore.functions.titles.TitlesModule;
import plugin.borealcore.functions.traps.TrapsCommand;
import plugin.borealcore.functions.traps.TrapsManager;
import plugin.borealcore.functions.wiki.WikiCommand;
import plugin.borealcore.functions.wiki.WikiManager;
import plugin.borealcore.functions.wiki.WikiTabCompletion;
import plugin.borealcore.manager.EffectManager;
import plugin.borealcore.manager.FurnitureManager;
import plugin.borealcore.manager.GuiManager;
import plugin.borealcore.manager.PlaceholderManager;
import plugin.borealcore.manager.configs.ConfigManager;
import plugin.borealcore.manager.configs.MessageManager;
import plugin.borealcore.module.loader.ModuleLoader;
import plugin.borealcore.utility.AdventureUtil;

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
    private static DatabaseManager databaseManager;
    private static WikiManager wikiManager;
    private static CraftingManager craftingManager;
    private static AnalyticsManager analyticsManager;
    private static PlushieManager plushieManager;
    private static DuelsManager duelsManager;
    private static BendingManager bendingManager;
    private static BreweryManager breweryManager;
    private static TrapsManager trapsManager;
    private static TitlesModule titleManager;
    private static MarketManager marketManager;
    private static ModuleLoader moduleLoader;

    @Override
    public void onLoad() {
        plugin = this;
    }

    @Override
    public void onEnable() {
        adventure = BukkitAudiences.create(this);
        protocolManager = ProtocolLibrary.getProtocolManager();
        inventoryManager = new InventoryManager(this);

        ConfigManager.load();
        MessageManager.load();

        databaseManager = new DatabaseManager(this);
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
        jadeManager = new JadeManager();
        craftingManager = new CraftingManager();
        analyticsManager = new AnalyticsManager();
        plushieManager = new PlushieManager();
        bendingManager = new BendingManager();
        breweryManager = new BreweryManager();
        trapsManager = new TrapsManager();
        titleManager = new TitlesModule();
        marketManager = new MarketManager();

        moduleLoader = new ModuleLoader(this, databaseManager, placeholderManager);
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
        //getCommand("herbalism").setTabCompleter(new HerbalismTabCompletion());
        getCommand("traps").setExecutor(new TrapsCommand());
        getCommand("sit").setExecutor(new SitCommand());
        getCommand("market").setExecutor(marketManager);
        getCommand("market").setTabCompleter(marketManager);

        AdventureUtil.consoleMessage("Plugin Enabled!");
    }

    @Override
    public void onDisable() {

        if (moduleLoader != null) {
            moduleLoader.unloadAllModules();
        }

        cookingManager.unload();
        herbalismManager.unload();
        competitionManager.unload();
        placeholderManager.unload();
        databaseManager.unload();
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
       // duelsManager.unload();
        bendingManager.unload();
        breweryManager.unload();
        trapsManager.unload();
        titleManager.unload();
        marketManager.unload();

        AdventureUtil.consoleMessage("[BorealCore] Plugin Disabled!");

        if (adventure != null) {
            adventure.close();
            adventure = null;
        }
    }

    @Override
    public void reloadConfig() {
        reload();
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

    public static DatabaseManager getDatabaseManager() {
        return databaseManager;
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

    public static TrapsManager getTrapsManager() {
        return trapsManager;
    }

    public static TitlesModule getTitleManager() {
        return titleManager;
    }

    public static MarketManager getMarketManager() {
        return marketManager;
    }

    public static ModuleLoader getModuleLoader() {
        return moduleLoader;
    }

    public static void disablePlugin(String errorMessage, Exception e) {
        plugin.getLogger().log(Level.SEVERE, errorMessage, e);
        plugin.getServer().getPluginManager().disablePlugin(plugin);
    }


    public static void reload() {
        ConfigManager.load();
        MessageManager.load();

        getModuleLoader().unloadAllModules();
        try {
            getModuleLoader().loadAllModules();
        } catch (ModuleLoadException e) {
            throw new RuntimeException(e);
        }

        getLayoutManager().unload();
        getLayoutManager().load();
        getEffectManager().unload();
        getEffectManager().load();
        getRecipeManager().unload();
        getRecipeManager().load();
        getHerbManager().unload();
        getHerbManager().load();
        getCookingManager().unload();
        getCookingManager().load();
        getHerbalismManager().unload();
        getHerbalismManager().load();
        getGuiManager().unload();
        getGuiManager().load();
        getCompetitionManager().unload();
        getCompetitionManager().load();
        getFurnitureManager().unload();
        getFurnitureManager().load();
        getJadeManager().unload();
        getJadeManager().load();
        getNodeManager().unload();
        getNodeManager().load();
        getWikiManager().unload();
        getWikiManager().load();
        getInventoryManager().init();
        getDatabaseManager().unload();
        getDatabaseManager().load();
        getAnalyticsManager().unload();
        getAnalyticsManager().load();
        getPlushieManager().unload();
        getPlushieManager().load();
        //BorealCore.getDuelsManager().unload(); @TODO REVERT!!!
        //BorealCore.getDuelsManager().load();
        getBendingManager().unload();
        getBendingManager().load();
        getBreweryManager().unload();
        getBreweryManager().load();
        getTrapsManager().unload();
        getTrapsManager().load();
        getTitleManager().unload();
        getTitleManager().load();
        getMarketManager().unload();
        getMarketManager().load();
    }
}