package plugin.borealcore.functions.jade;

import plugin.borealcore.api.module.ModuleContext;
import plugin.borealcore.api.module.BorealModule;
import plugin.borealcore.database.Database;
import plugin.borealcore.utility.AdventureUtil;

/**
 * Jade module - provides the jade currency system for BorealCore.
 * This module is a template showing how to structure a BorealCore module.
 *
 */
public class JadeModule implements BorealModule {

    private Database database;
    private ModuleContext context;

    @Override
    public String getModuleId() {
        return "jade";
    }

    @Override
    public String getModuleName() {
        return "Jade";
    }

    @Override
    public String getModuleVersion() {
        return "1.0.0";
    }

    @Override
    public String getModuleAuthor() {
        return "BMC";
    }

    @Override
    public String getMinimumBorealCoreVersion() {
        return "1.1.9.1";
    }

    @Override
    public void onModuleInitialize(ModuleContext context) throws Exception {
        this.context = context;
        this.database = context.getDatabase();
        
        context.getLogger().info("Initializing Jade module...");
    }

    @Override
    public void onModuleEnable() {
        AdventureUtil.consoleMessage("Jade module enabled!");
    }

    @Override
    public void onModuleDisable() {
        AdventureUtil.consoleMessage("Jade module disabled!");
    }
}



