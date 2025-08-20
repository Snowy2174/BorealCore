package plugin.borealcore.functions.bending;

import com.projectkorra.projectkorra.event.AbilityVelocityAffectEntityEvent;
import com.projectkorra.projectkorra.util.TempBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import plugin.borealcore.manager.configs.DebugLevel;

import static plugin.borealcore.manager.configs.ConfigManager.lavaDamage;
import static plugin.borealcore.utility.AdventureUtil.consoleMessage;

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
        if (event.getCause() == EntityDamageEvent.DamageCause.LAVA && event.getEntity() instanceof Player player) {
            Boolean temp = false;
            Boolean arena = false;
            if (TempBlock.isTempBlock(event.getEntity().getLocation().getBlock())) {
                temp = true;
            }
            if (event.getEntity().getLocation().getWorld().getName().equals("arenaworld")) {
                arena = true;
            }
            if (temp || arena) {
                consoleMessage(DebugLevel.DEBUG, "Lava damage event triggered: " + event.getEntity().getName() + " | TempBlock: " + temp + " | Arena: " + arena + " | Damage: " + event.getFinalDamage());
                player.setHealth(Math.min(20, player.getHealth() + event.getDamage() - lavaDamage)); // Hacky way to do this, because it doesn't actually let me cancel the event
                // Need to cast to Damagable then use the heal method, but intelij is refusing me
                // https://jd.papermc.io/paper/1.21.1/org/bukkit/entity/Damageable.html#heal
            }
        }
    }
}
