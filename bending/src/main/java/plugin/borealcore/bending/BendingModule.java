package plugin.borealcore.bending;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.HandlerList;
import plugin.borealcore.BorealCore;
import plugin.borealcore.api.module.BorealModule;
import plugin.borealcore.api.module.ModuleContext;

import java.util.HashMap;
import java.util.Map;

public class BendingModule implements BorealModule {

    private final BendingListener bendingListener;
    private ConfigurationSection config;

    public BendingModule() {
        this.bendingListener = new BendingListener();
    }

    @Override
    public void onModuleEnable() throws Exception {
        Bukkit.getPluginManager().registerEvents(this.bendingListener, BorealCore.plugin);
    }

    @Override
    public void onModuleDisable() throws Exception {
        if (this.bendingListener != null) {
            HandlerList.unregisterAll(this.bendingListener);
        }
    }

    @Override
    public void onModuleInitialize(ModuleContext context) throws Exception {
        Map<String, Object> defaults = new HashMap<>();

        defaults.put("lava-contact-damage", 1);
        defaults.put("fire-contact-damage", 1);
        this.config = context.setupModuleDefaults("bending", defaults);

        BendingConfig.fireContactDamage = config.getInt("fire-contact-damage", 1);
        BendingConfig.lavaContactDamage = config.getInt("lava-contact-damage", 1);
    }
}
