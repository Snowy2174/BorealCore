package plugin.borealcore.api.module;

import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;
import plugin.borealcore.BorealCore;
import plugin.borealcore.database.DatabaseManager;
import plugin.borealcore.manager.ConfigManager;
import plugin.borealcore.manager.ItemEnrichmentManager;
import plugin.borealcore.manager.MessageManager;
import plugin.borealcore.manager.PlaceholderManager;
import plugin.borealcore.utility.AdventureUtil;
import plugin.borealcore.utility.DebugLevel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
    private final ItemEnrichmentManager itemEnrichmentManager;
    private final List<QueuedCommand> registeredCommands = new ArrayList<>();

    public ModuleContext(BorealCore plugin, DatabaseManager database, PlaceholderManager placeholderManager, ItemEnrichmentManager itemEnrichmentManager) {
        this.plugin = plugin;
        this.database = database;
        this.logger = plugin.getLogger();
        this.pluginManager = plugin.getServer().getPluginManager();
        this.placeholderManager = placeholderManager;
        this.itemEnrichmentManager = itemEnrichmentManager;
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
     * @return The item enrichment manager for processing items
     */
    public ItemEnrichmentManager getItemEnrichmentManager() {
        return this.itemEnrichmentManager;
    }

    /**
     * Gets a ConfigurationSection for the module.
     *
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
                    AdventureUtil.consoleMessage(DebugLevel.WARNING, MessageManager.nonYamlConfigSave.replace("{file}", identifier));
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
     * Supports both standalone .yml files and sections within config.yml.
     *
     * @param identifier    File name (.yml) or section path in config.yml
     * @param defaultValues A map of config paths to default values
     * @return The populated ConfigurationSection ready for reading
     */
    public ConfigurationSection setupModuleDefaults(String identifier, Map<String, Object> defaultValues) {
        return ConfigManager.setupModuleDefaults(identifier, defaultValues);
    }

    /**
     * Applies default values to a module's messages.
     * If the keys don't exist, they are written to the messages file.
     *
     * @param messageDefaults A map of message keys to default values (without "messages." prefix)
     * @return The populated ConfigurationSection of messages
     */
    public ConfigurationSection setupModuleMessages(Map<String, String> messageDefaults) {
        return ConfigManager.setupModuleMessages(messageDefaults);
    }

    /**
     * Queues a Brigadier command node for registration with no description or aliases.
     *
     * @param commandNode The root literal command node to register.
     */
    public void registerCommand(LiteralCommandNode<CommandSourceStack> commandNode) {
        registerCommand(commandNode, null, List.of());
    }

    /**
     * Queues a Brigadier command node for registration with a description, but no aliases.
     *
     * @param commandNode The root literal command node to register.
     * @param description A brief description of the command.
     */
    public void registerCommand(LiteralCommandNode<CommandSourceStack> commandNode, String description) {
        registerCommand(commandNode, description, List.of());
    }

    /**
     * Queues a Brigadier command node for registration with a description and aliases.
     *
     * @param commandNode The root literal command node to register.
     * @param description A brief description of the command (can be null).
     * @param aliases     A collection of string aliases for the command.
     */
    public void registerCommand(LiteralCommandNode<CommandSourceStack> commandNode, String description, Collection<String> aliases) {
        this.registeredCommands.add(new QueuedCommand(commandNode, description, aliases != null ? aliases : List.of()));
    }

    /**
     * Retrieves an unmodifiable list of all command nodes currently queued by loaded modules.
     *
     * @return A list of registered Brigadier command nodes.
     */
    public List<QueuedCommand> getRegisteredCommands() {
        return this.registeredCommands;
    }

    /**
     * Purges all tracked commands from the context.
     * This must be called prior to reloading modules to ensure ghost commands
     * are not carried over into the new initialization phase.
     */
    public void clearCommands() {
        this.registeredCommands.clear();
    }


    public record QueuedCommand(
            LiteralCommandNode<CommandSourceStack> node,
            String description,
            Collection<String> aliases
    ) {
    }
}


