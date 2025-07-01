package plugin.borealcore.functions.bending;

import org.bukkit.Bukkit;
import plugin.borealcore.BorealCore;
import plugin.borealcore.listener.BendingListener;
import plugin.borealcore.object.Function;

public class BendingManager extends Function {

    private final BendingListener bendingListener;

    public BendingManager() {
        this.bendingListener = new BendingListener();
    }

    @Override
    public void load() {
        Bukkit.getPluginManager().registerEvents(this.bendingListener, BorealCore.plugin);
    }

    @Override
    public void unload() {
    }


}
