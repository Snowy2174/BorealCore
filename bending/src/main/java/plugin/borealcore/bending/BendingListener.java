package plugin.borealcore.bending;

import com.projectkorra.projectkorra.event.AbilityVelocityAffectEntityEvent;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

import static plugin.borealcore.bending.BendingConfig.fireContactDamage;
import static plugin.borealcore.bending.BendingConfig.lavaContactDamage;

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

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamageEvent(EntityDamageEvent event) {
        if (!event.getEntity().getWorld().getName().toLowerCase().equals("arenaworld")) {
            return;
        }
        if (!(event.getEntity() instanceof Player player)) return;
        switch (event.getCause()) {
            case CUSTOM -> {
                if (player.getLocation().getBlock().getType() == Material.LAVA) {
                    event.setDamage(lavaContactDamage);
                }
                if (player.getLocation().getBlock().getType() == Material.FIRE) {
                    event.setDamage(fireContactDamage);
                }
            }
            case LAVA -> {
                event.setCancelled(true);
            }
            case FIRE -> {
                event.setDamage(fireContactDamage);
            }
            default -> {
            }
        }
    }
}
