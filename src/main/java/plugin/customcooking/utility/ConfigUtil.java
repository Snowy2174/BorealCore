package plugin.customcooking.utility;

import org.bukkit.configuration.file.YamlConfiguration;
import plugin.customcooking.BorealCore;
import plugin.customcooking.manager.configs.ConfigManager;
import plugin.customcooking.manager.configs.MessageManager;

import java.io.File;

public class ConfigUtil {

    public static YamlConfiguration getConfig(String configName) {
        File file = new File(BorealCore.plugin.getDataFolder(), configName);
        if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
        if (!file.exists()) BorealCore.plugin.saveResource(configName, false);
        return YamlConfiguration.loadConfiguration(file);
    }

    public static void reload() {
        ConfigManager.load();
        MessageManager.load();
        BorealCore.getLayoutManager().unload();
        BorealCore.getLayoutManager().load();
        BorealCore.getEffectManager().unload();
        BorealCore.getEffectManager().load();
        BorealCore.getRecipeManager().unload();
        BorealCore.getRecipeManager().load();
        BorealCore.getCookingManager().unload();
        BorealCore.getCookingManager().load();
        BorealCore.getGuiManager().unload();
        BorealCore.getGuiManager().load();
        BorealCore.getCompetitionManager().unload();
        BorealCore.getCompetitionManager().load();
        BorealCore.getFurnitureManager().unload();
        BorealCore.getFurnitureManager().load();
        BorealCore.getJadeManager().unload();
        BorealCore.getJadeManager().load();
        BorealCore.getNodeManager().unload();
        BorealCore.getNodeManager().load();
        BorealCore.getWikiManager().unload();
        BorealCore.getWikiManager().load();
        BorealCore.getInventoryManager().init();
        BorealCore.getDatabase().unload();
        BorealCore.getDatabase().load();
        BorealCore.getAnalyticsManager().unload();
        BorealCore.getAnalyticsManager().load();
        BorealCore.getPlushieManager().unload();
        BorealCore.getPlushieManager().load();
        BorealCore.getDuelsManager().unload();
        BorealCore.getDuelsManager().load();
    }
}


