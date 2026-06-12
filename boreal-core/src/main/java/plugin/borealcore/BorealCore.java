package plugin.borealcore;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.plugin.java.JavaPlugin;
import plugin.borealcore.api.module.ModuleContext;
import plugin.borealcore.api.module.ModuleLoadException;
import plugin.borealcore.command.BorealCoreCommand;
import plugin.borealcore.database.DatabaseManager;
import plugin.borealcore.listener.GUIListener;
import plugin.borealcore.manager.ConfigManager;
import plugin.borealcore.manager.EffectManager;
import plugin.borealcore.manager.GuiManager;
import plugin.borealcore.manager.ItemEnrichmentManager;
import plugin.borealcore.manager.MessageManager;
import plugin.borealcore.manager.PlaceholderManager;
import plugin.borealcore.module.loader.ModuleLoader;

import java.util.List;
import java.util.logging.Level;

import static plugin.borealcore.utility.AdventureUtil.consoleMessage;

public class BorealCore extends JavaPlugin {

    public static BorealCore plugin;
    public static BukkitAudiences adventure;
    public static ProtocolManager protocolManager;
    private static GuiManager guiManager;
    private static PlaceholderManager placeholderManager;
    private static EffectManager effectManager;
    private static DatabaseManager databaseManager;
    private static ItemEnrichmentManager itemEnrichmentManager;
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
        itemEnrichmentManager = new ItemEnrichmentManager();

        globalModuleContext = new ModuleContext(this, databaseManager, placeholderManager, itemEnrichmentManager);
        moduleLoader = new ModuleLoader(globalModuleContext);

        try {
            moduleLoader.loadAllModules();
        } catch (Exception e) {
            getLogger().severe("Failed to load modules on startup: " + e.getMessage());
        }

        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            if (this.globalModuleContext != null) {
                for (ModuleContext.QueuedCommand queued : this.globalModuleContext.getRegisteredCommands()) {
                    commands.registrar().register(queued.node(), queued.description(), queued.aliases());
                    consoleMessage("Registered command from module: " + queued.node().getLiteral());
                }
            }
            commands.registrar().register(new BorealCoreCommand().buildCommandNode(), "The main command to manage BorealCore functions and modules", List.of("bc"));
        });

        getServer().getPluginManager().registerEvents(new GUIListener(), this);
        reloadConfig();

        consoleMessage("Plugin Enabled!");
    }

    @Override
    public void onDisable() {

        if (moduleLoader != null) {
            moduleLoader.unloadAllModules();
        }

        placeholderManager.unload();
        databaseManager.unload(); // Important
        effectManager.unload();
        guiManager.unload();

        consoleMessage("[BorealCore] Plugin Disabled!");

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

    public static PlaceholderManager getPlaceholderManager() {
        return placeholderManager;
    }

    public static GuiManager getGuiManager() {
        return guiManager;
    }

    public static EffectManager getEffectManager() {
        return effectManager;
    }

    public static DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public static ItemEnrichmentManager getItemEnrichmentManager() {
        return itemEnrichmentManager;
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


    public static void reload() { // @TODO it's very silly to reload everything just to load the config on Enable
        ConfigManager.load();
        MessageManager.load();

        getEffectManager().unload();
        getEffectManager().load();
        getPlaceholderManager().unload();
        getPlaceholderManager().load();
        getGuiManager().unload();
        getGuiManager().load();
        getDatabaseManager().unload();
        getDatabaseManager().load();

        moduleLoader.unloadAllModules();
        globalModuleContext.clearCommands();

        try {
            moduleLoader.loadAllModules();
        } catch (ModuleLoadException e) {
            plugin.getLogger().severe("Exception encountered during reload: " + e.getMessage());
        }
    }
}