package plugin.customcooking;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import fr.minuskube.inv.InventoryManager;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.plugin.java.JavaPlugin;
import plugin.customcooking.commands.MainCommand;
import plugin.customcooking.commands.TabCompletion;
import plugin.customcooking.gui.GuiManager;
import plugin.customcooking.manager.*;
import plugin.customcooking.manager.configs.LayoutManager;
import plugin.customcooking.manager.configs.MasteryManager;
import plugin.customcooking.util.AdventureUtil;
import plugin.customcooking.util.ConfigUtil;

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

        inventoryManager.init();

        reloadConfig();
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
        effectManager.unload();
        guiManager.unload();

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

    public static MasteryManager getMasteryManager() {
        return masteryManager;
    }
    public static InventoryManager getInventoryManager() {
        return inventoryManager;
    }
}