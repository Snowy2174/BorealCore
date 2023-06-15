package plugin.customcooking.util;

import org.bukkit.configuration.file.YamlConfiguration;
import plugin.customcooking.CustomCooking;
import plugin.customcooking.configs.ConfigManager;
import plugin.customcooking.configs.MessageManager;

import java.io.File;

public class ConfigUtil {

    public static YamlConfiguration getConfig(String configName) {
        File file = new File(CustomCooking.plugin.getDataFolder(), configName);
        if (!file.exists()) CustomCooking.plugin.saveResource(configName, false);
        return YamlConfiguration.loadConfiguration(file);
    }

    public static void reload() {
        ConfigManager.load();
        MessageManager.load();
        CustomCooking.getLayoutManager().unload();
        CustomCooking.getLayoutManager().load();
        CustomCooking.getEffectManager().unload();
        CustomCooking.getEffectManager().load();
        CustomCooking.getRecipeManager().unload();
        CustomCooking.getRecipeManager().load();
        CustomCooking.getCookingManager().unload();
        CustomCooking.getCookingManager().load();
        CustomCooking.getCompetitionManager().unload();
        CustomCooking.getCompetitionManager().load();
        CustomCooking.getFurnitureManager().unload();
        CustomCooking.getFurnitureManager().load();
    }
}


