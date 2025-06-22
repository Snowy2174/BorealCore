package plugin.borealcore.listener;

import com.bencodez.votingplugin.events.PlayerVoteEvent;
import com.dre.brewery.api.events.brew.BrewModifyEvent;
import net.momirealms.customcrops.api.core.block.BreakReason;
import net.momirealms.customcrops.api.event.CropBreakEvent;
import net.momirealms.customfishing.api.event.FishingResultEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import plugin.borealcore.functions.jade.JadeManager;

import static com.dre.brewery.api.events.brew.BrewModifyEvent.Type.SEAL;

public class JadeSourceListener implements Listener {
    private final JadeManager jadeManager;
    public JadeSourceListener(JadeManager jadeManager) {
        this.jadeManager = jadeManager;
    }

    @EventHandler
    public void onPlayerFishEvent(FishingResultEvent event) {
        if (event.isCancelled()) {
            return;
        }
        jadeManager.fishingJade(event);
    }

    @EventHandler
    public void onCropBreakEvent(CropBreakEvent event) {
        if (event.isCancelled() || event.reason() != BreakReason.ACTION ) {
            return;
        }
        System.out.println("Processing farmingJade for player: " + event.entityBreaker());
        jadeManager.farmingJade(event);
    }

    @EventHandler
    public void onBrewEvent(BrewModifyEvent event) {
        if (event.getType() != SEAL || event.isCancelled() || event.getPlayer() == null) {
            return;
        }
        jadeManager.breweryJade(event);
    }

    @EventHandler
    public void onVote(PlayerVoteEvent event) {
        if (event.isWasOnline()) {
            jadeManager.give(Bukkit.getPlayer(event.getPlayer()), 1, "voting");
        } else {
            jadeManager.giveOffline(Bukkit.getOfflinePlayer(event.getPlayer()), 1, "voting");
        }
    }
}
