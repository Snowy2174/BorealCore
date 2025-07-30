package plugin.borealcore.utility;

import org.bukkit.configuration.file.YamlConfiguration;
import plugin.borealcore.BorealCore;
import plugin.borealcore.manager.configs.ConfigManager;
import plugin.borealcore.manager.configs.MessageManager;
import plugin.borealcore.object.Function;

import java.io.File;

public class ConfigUtil {

    public static YamlConfiguration getConfig(String configName) {
        File file = new File(BorealCore.plugin.getDataFolder(), configName);
        if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
        if (!file.exists()) BorealCore.plugin.saveResource(configName, false);
        return YamlConfiguration.loadConfiguration(file);
    }

    public static void reload() {
        for (Function function : BorealCore.functions) {
            function.unload();
            function.load();
        }
        BorealCore.getInventoryManager().init();
        BorealCore.getDatabase().unload();
        BorealCore.getDatabase().load();
        BorealCore.getTrapsDatabase().unload();
        BorealCore.getTrapsDatabase().load();
    }
}


