package plugin.borealcore.api.module;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;
import plugin.borealcore.BorealCore;
import plugin.borealcore.database.DatabaseManager;
import plugin.borealcore.manager.PlaceholderManager;
import plugin.borealcore.manager.configs.ConfigManager;

import java.util.Map;
import java.util.logging.Logger;

/**
 * Context object provided to modules during initialization.
 * Gives modules access to core plugin resources.
 */
public class ModuleContext {

    private final BorealCore plugin;
    private final DatabaseManager database;
    private final Logger logger;
    private final PluginManager pluginManager;
    private final PlaceholderManager placeholderManager;

    public ModuleContext(BorealCore plugin, DatabaseManager database, PlaceholderManager placeholderManager) {
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
    public DatabaseManager getDatabaseManager() {
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

    /**
     * Gets a ConfigurationSection for the module.
     * @param identifier If ending with ".yml", gets that file. Otherwise, gets a subsection from config.yml.
     */
    public ConfigurationSection getModuleConfig(String identifier) {
        if (identifier.endsWith(".yml")) {
            return ConfigManager.getConfig(identifier);
        } else {
            YamlConfiguration mainConfig = ConfigManager.getConfig("config.yml");
            ConfigurationSection section = mainConfig.getConfigurationSection(identifier);
            if (section == null) {
                section = mainConfig.createSection(identifier);
                ConfigManager.saveConfig(mainConfig, "config.yml");
            }
            return section;
        }
    }

    /**
     * Saves a module's ConfigurationSection back to disk.
     */
    public void saveModuleConfig(String identifier, ConfigurationSection section) {
        if (identifier.endsWith(".yml")) {
            if (section instanceof YamlConfiguration) {
                ConfigManager.saveConfig((YamlConfiguration) section, identifier);
            } else {
                logger.warning("Attempted to save a non-YamlConfiguration to file: " + identifier);
            }
        } else {
            YamlConfiguration mainConfig = ConfigManager.getConfig("config.yml");
            mainConfig.set(identifier, section);
            ConfigManager.saveConfig(mainConfig, "config.yml");
        }
    }

    /**
     * Applies default values to a module's config.
     * If the keys don't exist, they are written and saved to disk.
     * @param identifier The file (".yml") or main config section path
     * @param defaultValues A map of config paths to default values
     * @return The populated ConfigurationSection ready for reading
     */
    public ConfigurationSection setupModuleDefaults(String identifier, Map<String, Object> defaultValues) {
        ConfigurationSection config = getModuleConfig(identifier);
        boolean changed = false;

        for (Map.Entry<String, Object> entry : defaultValues.entrySet()) {
            if (!config.contains(entry.getKey())) {
                config.set(entry.getKey(), entry.getValue());
                changed = true;
            }
        }

        if (changed) {
            saveModuleConfig(identifier, config);
        }
        return config;
    }
}

