package plugin.borealcore.crops;

import org.bukkit.event.HandlerList;
import plugin.borealcore.api.module.BorealModule;
import plugin.borealcore.api.module.ModuleContext;

public class CropsModule implements BorealModule {

    ModuleContext context;
    CropsListener listener;

    @Override
    public void onModuleEnable() throws Exception {
        if (context.getPluginManager().getPlugin("CustomCrops") == null) {
            throw new Exception("CustomCrops plugin not found. CropsModule requires CustomCrops to function.");
        }
        this.listener = new CropsListener();
        context.getPluginManager().registerEvents(this.listener, context.getPlugin());
    }

    @Override
    public void onModuleDisable() throws Exception {
        HandlerList.unregisterAll(this.listener);
    }

    @Override
    public void onModuleInitialize(ModuleContext context) throws Exception {
        this.context = context;
    }
}
