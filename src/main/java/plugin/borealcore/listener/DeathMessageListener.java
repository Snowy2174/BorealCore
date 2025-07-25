package plugin.borealcore.listener;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.eclipse.sisu.Priority;

public class DeathMessageListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDeath(org.bukkit.event.entity.PlayerDeathEvent event) {
        event.setDeathMessage( ChatColor.GRAY + "[" + ChatColor.RED + "☠" + ChatColor.GRAY + "] " + event.getDeathMessage());
    }
}
