package plugin.customcooking.Util;

import org.bukkit.configuration.file.YamlConfiguration;
import plugin.customcooking.Configs.ConfigManager;
import plugin.customcooking.CustomCooking;

import java.io.File;

public class ConfigUtil {

    public static YamlConfiguration getConfig(String configName) {
        File file = new File(CustomCooking.plugin.getDataFolder(), configName);
        if (!file.exists()) CustomCooking.plugin.saveResource(configName, false);
        return YamlConfiguration.loadConfiguration(file);
    }

    public static void reload() {
        ConfigManager.load();
        CustomCooking.getLayoutManager().unload();
        CustomCooking.getLayoutManager().load();
        CustomCooking.getRecipeManager().unload();
        CustomCooking.getRecipeManager().load();
        CustomCooking.getCookingManager().unload();
        CustomCooking.getCookingManager().load();
        CustomCooking.getFurnitureManager().unload();
        CustomCooking.getFurnitureManager().load();
    }
}


