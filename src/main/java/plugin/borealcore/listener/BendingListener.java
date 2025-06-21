package plugin.borealcore.listener;

import com.projectkorra.projectkorra.event.AbilityVelocityAffectEntityEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class BendingListener implements Listener {

    @EventHandler
    public void onAbilityVelocityAffectEntityEvent(AbilityVelocityAffectEntityEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (event.getAffected().hasMetadata("NPC")) {
            event.setCancelled(true);
        }
    }
}
