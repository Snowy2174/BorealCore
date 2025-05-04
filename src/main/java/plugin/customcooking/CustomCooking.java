package plugin.customcooking;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import fr.minuskube.inv.InventoryManager;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.plugin.java.JavaPlugin;
import plugin.customcooking.commands.*;
import plugin.customcooking.database.SQLite;
import plugin.customcooking.functions.brewery.BreweryManager;
import plugin.customcooking.functions.cooking.CompetitionManager;
import plugin.customcooking.functions.cooking.CookingManager;
import plugin.customcooking.database.Database;
import plugin.customcooking.functions.jade.JadeManager;
import plugin.customcooking.functions.karmicnode.NodeManager;
import plugin.customcooking.functions.wiki.WikiManager;
import plugin.customcooking.manager.*;
import plugin.customcooking.manager.configs.EffectManager;
import plugin.customcooking.manager.configs.LayoutManager;
import plugin.customcooking.manager.configs.RecipeManager;
import plugin.customcooking.utility.AdventureUtil;
import plugin.customcooking.utility.ConfigUtil;

public class CustomCooking extends JavaPlugin {

    public static CustomCooking plugin;
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
    private static BreweryManager breweryManager;

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
        breweryManager = new BreweryManager();

        db.dbload();
        inventoryManager.init();

        reloadConfig();
        getCommand("cooking").setExecutor(new CookCommand());
        getCommand("cooking").setTabCompleter(new CookTabCompletion());
        getCommand("jade").setExecutor(new JadeCommand());
        getCommand("jade").setTabCompleter(new JadeTabCompletion());
        getCommand("kn").setExecutor(new NodeCommand());
        getCommand("wiki").setExecutor(new WikiCommand());
        getCommand("wiki").setTabCompleter(new WikiTabCompletion());

        AdventureUtil.consoleMessage("[CustomCooking] Plugin Enabled!");
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

        AdventureUtil.consoleMessage("[CustomCooking] Plugin Disabled!");

        if (adventure != null) {
            adventure.close();
            adventure = null;
        }
    }

    @Override
    public void reloadConfig() {
        ConfigUtil.reload();
    }

    public static CustomCooking getInstance() {
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
    public static BreweryManager getBreweryManager() {
        return breweryManager;
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
}