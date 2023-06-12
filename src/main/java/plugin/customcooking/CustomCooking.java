package plugin.customcooking;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import fr.minuskube.inv.InventoryManager;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import plugin.customcooking.commands.MainCommand;
import plugin.customcooking.commands.TabCompletion;
import plugin.customcooking.configs.LayoutManager;
import plugin.customcooking.configs.MasteryManager;
import plugin.customcooking.configs.RecipeManager;
import plugin.customcooking.manager.CompetitionManager;
import plugin.customcooking.manager.CookingManager;
import plugin.customcooking.manager.FurnitureManager;
import plugin.customcooking.manager.PlaceholderManager;
import plugin.customcooking.util.AdventureUtil;
import plugin.customcooking.util.ConfigUtil;
import plugin.customcooking.util.PlaceholderUtil;

public class CustomCooking extends JavaPlugin {

    public static CustomCooking plugin;
    public static BukkitAudiences adventure;
    public static ProtocolManager protocolManager;
    private static CookingManager cookingManager;
    private static CompetitionManager competitionManager;
    private static RecipeManager recipeManager;
    private static PlaceholderManager placeholderManager;
    private static LayoutManager layoutManager;
    private static FurnitureManager furnitureManager;
    private static MasteryManager masteryManager;
    private static InventoryManager inventoryManager;

    @Override
    public void onLoad() {
        plugin = this;
    }

    @Override
    public void onEnable() {
        adventure = BukkitAudiences.create(this);
        protocolManager = ProtocolLibrary.getProtocolManager();

        cookingManager = new CookingManager();
        competitionManager = new CompetitionManager();
        placeholderManager = new PlaceholderManager();
        layoutManager = new LayoutManager();
        furnitureManager = new FurnitureManager();
        masteryManager = new MasteryManager();
        recipeManager = new RecipeManager();
        inventoryManager = new InventoryManager(this);

        inventoryManager.init();

        reloadConfig();
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PlaceholderUtil(this).register();
        }
        getCommand("cooking").setExecutor(new MainCommand());
        getCommand("cooking").setTabCompleter(new TabCompletion());

        AdventureUtil.consoleMessage("[CustomCooking] Plugin Enabled!");
    }

    @Override
    public void onDisable() {

        cookingManager.unload();
        competitionManager.unload();
        placeholderManager.unload();
        recipeManager.unload();
        layoutManager.unload();

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
    public CompetitionManager getCompetitionManager() {
        return competitionManager;
    }
    public PlaceholderManager getPlaceholderManager() {
        return placeholderManager;
    }
    public static LayoutManager getLayoutManager() {
        return layoutManager;
    }

    public static FurnitureManager getFurnitureManager() {
        return furnitureManager;
    }

    public static RecipeManager getRecipeManager() {
        return recipeManager;
    }

    public static MasteryManager getMasteryManager() {
        return masteryManager;
    }
    public static InventoryManager getInventoryManager() {
        return inventoryManager;
    }
}