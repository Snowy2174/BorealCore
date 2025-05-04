package plugin.customcooking.functions.jade;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import plugin.customcooking.CustomCooking;

public class AnnouncementScheduler extends BukkitRunnable {

    private final Player player;

    AnnouncementScheduler(final Player player) {
        this.player = player;
    }

    @Override
    public void run() {
        if (player.isOnline()) {
            int jadeAmount = JadeManager.getTotalJadeForPlayer(player);
            StringBuilder limitsMessage = new StringBuilder("Jade Limits:\n");
            JadeManager.jadeSources.forEach((source, jadeSource) -> {
                String limitInfo = JadeManager.checkJadeLimit(player, source);
                limitsMessage.append("- ").append(source).append(": ").append(limitInfo).append("\n");
            });

            player.sendMessage("You currently have " + jadeAmount + " Jade.");
            player.sendMessage(limitsMessage.toString());
        } else {
            this.cancel();
        }
    }

    public static void start(Player player) {
        new AnnouncementScheduler(player).runTaskTimer(
                CustomCooking.getInstance(),
                0L,
                12000L // 10 minutes in ticks
        );
    }

}
