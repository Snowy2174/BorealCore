package plugin.borealcore.sit;

import org.bukkit.event.HandlerList;
import plugin.borealcore.api.module.BorealModule;
import plugin.borealcore.api.module.ModuleContext;

public class SitModule implements BorealModule {

    ModuleContext context;
    private SitListener sitListener;

    @Override
    public void onModuleEnable() throws Exception {
        context.getPluginManager().registerEvents(sitListener, context.getPlugin());
    }

    @Override
    public void onModuleDisable() throws Exception {
        if (this.sitListener != null) HandlerList.unregisterAll(this.sitListener);
    }

    @Override
    public void onModuleInitialize(ModuleContext context) throws Exception {
        context.getPlugin().getCommand("sit").setExecutor(new SitCommand());
        this.sitListener = new SitListener(context.getPlugin());
    }
}
