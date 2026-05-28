package plugin.borealcore.api.module;

import org.bukkit.plugin.PluginManager;
import plugin.borealcore.BorealCore;
import plugin.borealcore.database.Database;
import plugin.borealcore.manager.PlaceholderManager;

import java.util.logging.Logger;

/**
 * Context object provided to modules during initialization.
 * Gives modules access to core plugin resources.
 */
public class ModuleContext {

    private final BorealCore plugin;
    private final Database database;
    private final Logger logger;
    private final PluginManager pluginManager;
    private final PlaceholderManager placeholderManager;

    public ModuleContext(BorealCore plugin, Database database, PlaceholderManager placeholderManager) {
        this.plugin = plugin;
        this.database = database;
        this.logger = plugin.getLogger();
        this.pluginManager = plugin.getServer().getPluginManager();
        this.placeholderManager = placeholderManager;
    }

    /**
     * @return The main BorealCore plugin instance
     */
    public BorealCore getPlugin() {
        return plugin;
    }

    /**
     * @return The primary database instance
     */
    public Database getDatabase() {
        return database;
    }

    /**
     * @return The plugin's logger instance
     */
    public Logger getLogger() {
        return logger;
    }

    /**
     * @return The server's plugin manager (for registering listeners)
     */
    public PluginManager getPluginManager() {
        return pluginManager;
    }

    /**
     * @return The placeholder manager for registering PAPI expansions
     */
    public PlaceholderManager getPlaceholderManager() {
        return this.placeholderManager;
    }
}

