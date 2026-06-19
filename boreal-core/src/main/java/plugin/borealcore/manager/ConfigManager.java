package plugin.borealcore.manager;

import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import plugin.borealcore.BorealCore;
import plugin.borealcore.utility.DebugLevel;

import java.io.File;
import java.util.Map;

public class ConfigManager {
    public static String lang;
    public static DebugLevel debugLevel;
    public static boolean processAnalyticsEnabled;
    public static String customNamespace;
    public static String effectLore;
    public static String hungerLore;
    public static String saturationLore;

    public static void load() {
        YamlConfiguration config = getConfig("config.yml");

        lang = config.getString("lang", "english");
        customNamespace = config.getString("mechanics.namespace", "borealcore");
        debugLevel = DebugLevel.valueOf(config.getString("debug-level", "INFO").toUpperCase());
        processAnalyticsEnabled = config.getBoolean("analytics.process.enabled", true);
        effectLore = config.getString("mechanics.effect-lore", " <!italic><gold>\uD83E\uDDEA <white>{effect} <gold>{amplifier} {duration}");
        hungerLore = config.getString("mechanics.hunger-lore", " <!italic><gold>\uD83C\uDF56 Restores {hunger} hunger");
        saturationLore = config.getString("mechanics.saturation-lore", " <!italic><gold>\uD83C\uDF56 Restores {saturation} saturation");

    }

    public static @NotNull NamespacedKey getNamespacedKey(String key) {
        return new NamespacedKey(BorealCore.plugin, key);
    }

    public static void setDebugLevel(DebugLevel debugLevel) {
        YamlConfiguration config = getConfig("config.yml");
        config.set("debug-level", debugLevel.toString());
        saveConfig(config, "config.yml");
    }

    /**
     * Utility method: Setup default config values for a module.
     * Provides backward-compatible config default initialization.
     *
     * @param identifier    File name (".yml") or section path in config.yml
     * @param defaultValues Map of config paths to default values
     * @return ConfigurationSection with defaults applied
     */
    public static ConfigurationSection setupModuleDefaults(String identifier, Map<String, Object> defaultValues) {
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

    /**
     * Utility method: Setup default message values for a module.
     * Reads from messages file and applies defaults.
     *
     * @param messageDefaults Map of message keys to default values (without "messages." prefix)
     * @return ConfigurationSection with defaults applied
     */
    public static ConfigurationSection setupModuleMessages(Map<String, String> messageDefaults) {
        YamlConfiguration config = getConfig("messages_" + lang + ".yml");
        boolean changed = false;

        for (Map.Entry<String, String> entry : messageDefaults.entrySet()) {
            String path = "messages." + entry.getKey();
            if (!config.contains(path)) {
                config.set(path, entry.getValue());
                changed = true;
            }
        }

        if (changed) {
            saveConfig(config, "messages_" + lang + ".yml");
        }
        return config;
    }

    /**
     * Gets a module's configuration section.
     * Supports both standalone .yml files and sections within config.yml
     */
    public static ConfigurationSection getModuleConfig(String identifier) {
        if (identifier.endsWith(".yml")) {
            return getConfig(identifier);
        } else {
            YamlConfiguration mainConfig = getConfig("config.yml");
            ConfigurationSection section = mainConfig.getConfigurationSection(identifier);
            if (section == null) {
                section = mainConfig.createSection(identifier);
                saveConfig(mainConfig, "config.yml");
            }
            return section;
        }
    }

    /**
     * Saves a module's ConfigurationSection back to disk.
     */
    public static void saveModuleConfig(String identifier, ConfigurationSection section) {
        if (identifier.endsWith(".yml")) {
            if (section instanceof YamlConfiguration) {
                saveConfig((YamlConfiguration) section, identifier);
            } else {
                BorealCore.plugin.getLogger().warning("Attempted to save a non-YamlConfiguration to file: " + identifier);
            }
        } else {
            YamlConfiguration mainConfig = getConfig("config.yml");
            mainConfig.set(identifier, section);
            saveConfig(mainConfig, "config.yml");
        }
    }

    public static YamlConfiguration getConfig(String configName) {
        File file = new File(BorealCore.plugin.getDataFolder(), configName);
        if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
        if (!file.exists()) {
            try {
                BorealCore.plugin.saveResource(configName.substring(configName.lastIndexOf("/") + 1), false);
            } catch (IllegalArgumentException e) {
                try {
                    file.createNewFile();
                } catch (Exception ignored) {
                }
            }
        }
        return YamlConfiguration.loadConfiguration(file);
    }

    /**
     * Saves a YamlConfiguration to disk
     */
    public static void saveConfig(YamlConfiguration config, String configName) {
        File file = new File(BorealCore.plugin.getDataFolder(), configName);
        try {
            config.save(file);
        } catch (Exception e) {
            BorealCore.plugin.getLogger().severe("Failed to save config: " + configName);
        }
    }
}