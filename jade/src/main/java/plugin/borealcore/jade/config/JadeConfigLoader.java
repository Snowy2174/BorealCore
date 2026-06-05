package plugin.borealcore.jade.config;

import org.bukkit.configuration.ConfigurationSection;
import plugin.borealcore.manager.ConfigManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Loads jade-specific configuration from config.yml.
 * Called by JadeModule during module initialization.
 * <p>
 * Uses ModuleContext pattern: All defaults are applied automatically,
 * and configuration is read from the main config file.
 */
public class JadeConfigLoader {

    public static void load() {
        // Define all jade config defaults in a single map
        Map<String, Object> defaults = new HashMap<>();

        defaults.put("mechanics.brewing-required-quality", 8);

        // Setup defaults using ConfigManager utility (config file is main config.yml)
        ConfigurationSection config = ConfigManager.setupModuleDefaults("config.yml", defaults);

        // Load values into static fields
        JadeConfig.brewingRequiredQuality = config.getInt("mechanics.brewing-required-quality", 8);
        JadeConfig.refarmableCrops = config.getStringList("mechanics.refarmable-crops");
    }
}


