package plugin.borealcore.functions.herbalism;

import org.bukkit.plugin.Plugin;
import plugin.borealcore.database.DatabaseManager;

public class HerbalismDatabase {

    private final DatabaseManager coreDbManager;
    private final Plugin plugin;

    public HerbalismDatabase(DatabaseManager coreDbManager) {
        this.coreDbManager = coreDbManager;
        this.plugin = coreDbManager.plugin;
    }
}
