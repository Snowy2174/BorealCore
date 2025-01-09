package plugin.customcooking;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import fr.minuskube.inv.InventoryManager;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.plugin.java.JavaPlugin;
import plugin.customcooking.commands.*;
import plugin.customcooking.gui.GuiManager;
import plugin.customcooking.jade.JadeDatabase;
import plugin.customcooking.karmicnode.NodeManager;
import plugin.customcooking.manager.JadeManager;
import plugin.customcooking.jade.SQLite;
import plugin.customcooking.manager.*;
import plugin.customcooking.manager.configs.LayoutManager;
import plugin.customcooking.manager.DataManager;
import plugin.customcooking.manager.configs.RecipeManager;
import plugin.customcooking.util.AdventureUtil;
import plugin.customcooking.util.ConfigUtil;
import plugin.customcooking.wiki.WikiManager;

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
    private static DataManager dataManager;
    private static InventoryManager inventoryManager;
    private static NodeManager nodeManager;
    private static JadeManager jadeManager;
    private static JadeDatabase db;
    private static WikiManager wikiManager;
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
        dataManager = new DataManager();
        recipeManager = new RecipeManager();
        guiManager = new GuiManager();
        placeholderManager = new PlaceholderManager();
        nodeManager = new NodeManager();
        jadeManager = new JadeManager();
        wikiManager = new WikiManager();
        db = new SQLite(this);

        db.dbload();
        inventoryManager.init();

        reloadConfig();
        getCommand("cooking").setExecutor(new CookCommand());
        getCommand("cooking").setTabCompleter(new CookTabCompletion());
        getCommand("jade").setExecutor(new JadeCommand());
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

    public static RecipeManager getRecipeManager() {
        return recipeManager;
    }
    public static EffectManager getEffectManager() {
        return effectManager;
    }

    public static DataManager getMasteryManager() {
        return dataManager;
    }
    public static JadeManager getJadeManager() {
        return jadeManager;
    }
    public static InventoryManager getInventoryManager() {
        return inventoryManager;
    }
    public static JadeDatabase getDatabase() {
        return db;
    }
    public static NodeManager getNodeManager() {
        return nodeManager;
    }
    public static WikiManager getWikiManager() {
        return wikiManager;
    }
}