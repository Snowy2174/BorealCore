package plugin.borealcore.functions.bending;

import com.projectkorra.projectkorra.event.AbilityVelocityAffectEntityEvent;
import com.projectkorra.projectkorra.util.TempBlock;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

import static plugin.borealcore.manager.configs.ConfigManager.fireContactDamage;
import static plugin.borealcore.manager.configs.ConfigManager.lavaContactDamage;

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
        if (!TempBlock.isTempBlock(event.getEntity().getLocation().getBlock())) {
            return;
        }
        if (!(event.getEntity() instanceof Player player)) return;
        switch (event.getCause()) {
            case CUSTOM -> {
                if (player.getLocation().getBlock().getType() == Material.LAVA) {
                    event.setDamage(lavaContactDamage);
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
