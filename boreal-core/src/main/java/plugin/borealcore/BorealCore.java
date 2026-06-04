package plugin.borealcore;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.plugin.java.JavaPlugin;
import plugin.borealcore.api.module.ModuleContext;
import plugin.borealcore.api.module.ModuleLoadException;
import plugin.borealcore.database.DatabaseManager;
import plugin.borealcore.functions.cooking.CookCommand;
import plugin.borealcore.functions.cooking.CookTabCompletion;
import plugin.borealcore.functions.cooking.CookingCompetitionManager;
import plugin.borealcore.functions.cooking.CookingManager;
import plugin.borealcore.functions.cooking.MasteryManager;
import plugin.borealcore.functions.cooking.recipebook.RecipeBookCommand;
import plugin.borealcore.functions.cooking.recipebook.RecipeBookTabCompletion;
import plugin.borealcore.functions.cooking.configs.LayoutManager;
import plugin.borealcore.functions.cooking.configs.RecipeManager;
import plugin.borealcore.functions.herbalism.HerbalismCommand;
import plugin.borealcore.functions.herbalism.HerbalismManager;
import plugin.borealcore.functions.herbalism.configs.HerbManager;
import plugin.borealcore.functions.traps.TrapsCommand;
import plugin.borealcore.functions.traps.TrapsManager;
import plugin.borealcore.manager.EffectManager;
import plugin.borealcore.manager.GuiManager;
import plugin.borealcore.manager.PlaceholderManager;
import plugin.borealcore.manager.ConfigManager;
import plugin.borealcore.manager.MessageManager;
import plugin.borealcore.module.loader.ModuleLoader;
import plugin.borealcore.object.GUIListener;
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
    private static MasteryManager masteryManager;
    private static DatabaseManager databaseManager;
    private static TrapsManager trapsManager;
    private static ModuleLoader moduleLoader;
    private static ModuleContext globalModuleContext;

    @Override
    public void onLoad() {
        plugin = this;
    }

    @Override
    public void onEnable() {
        adventure = BukkitAudiences.create(this);
        protocolManager = ProtocolLibrary.getProtocolManager();

        ConfigManager.load();
        MessageManager.load();

        databaseManager = new DatabaseManager(this);
        effectManager = new EffectManager();
        guiManager = new GuiManager();
        placeholderManager = new PlaceholderManager();

        globalModuleContext = new ModuleContext(this, databaseManager, placeholderManager);
        moduleLoader = new ModuleLoader(globalModuleContext);

        try {
            moduleLoader.loadAllModules();
        } catch (Exception e) {
            getLogger().severe("Failed to load modules on startup: " + e.getMessage());
        }

        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            if (this.globalModuleContext != null) {
                for (var node : this.globalModuleContext.getRegisteredCommands()) {
                    event.registrar().register(node);
                }
            }
        });

        // Future modules
        cookingManager = new CookingManager();
        herbalismManager = new HerbalismManager();
        competitionManager = new CookingCompetitionManager();
        layoutManager = new LayoutManager();
        masteryManager = new MasteryManager();
        recipeManager = new RecipeManager();
        herbManager = new HerbManager();
        trapsManager = new TrapsManager();

        reloadConfig();

        getCommand("cooking").setExecutor(new CookCommand());
        getCommand("cooking").setTabCompleter(new CookTabCompletion());
        getCommand("recipes").setExecutor(new RecipeBookCommand());
        getCommand("recipes").setTabCompleter(new RecipeBookTabCompletion());
        getCommand("herbalism").setExecutor(new HerbalismCommand());
        //getCommand("herbalism").setTabCompleter(new HerbalismTabCompletion());
        getCommand("traps").setExecutor(new TrapsCommand());
        getServer().getPluginManager().registerEvents(new GUIListener(), this);

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
        databaseManager.unload(); // Important
        recipeManager.unload();
        herbManager.unload();
        layoutManager.unload();
        effectManager.unload();
        guiManager.unload();
        masteryManager.unload();
        trapsManager.unload();

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

    public static RecipeManager getRecipeManager() {
        return recipeManager;
    }

    public static EffectManager getEffectManager() {
        return effectManager;
    }

    public static DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public static HerbManager getHerbManager() {
        return herbManager;
    }

    public static HerbalismManager getHerbalismManager() {
        return herbalismManager;
    }

    public static ModuleLoader getModuleLoader() {
        return moduleLoader;
    }

    public static ModuleContext getGlobalModuleContext() {
        return globalModuleContext;
    }

    public static void disablePlugin(String errorMessage, Exception e) {
        plugin.getLogger().log(Level.SEVERE, errorMessage, e);
        plugin.getServer().getPluginManager().disablePlugin(plugin);
    }


    public static void reload() {
        ConfigManager.load();
        MessageManager.load();

        moduleLoader.unloadAllModules();
        globalModuleContext.clearCommands();

        try {
            moduleLoader.loadAllModules();
        } catch (ModuleLoadException e) {
            plugin.getLogger().severe("Exception encountered during reload: " + e.getMessage());
        }
        plugin.getServer().reloadCommandAliases();

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
        getDatabaseManager().unload();
        getDatabaseManager().load();
    }
}