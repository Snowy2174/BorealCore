package plugin.borealcore.jade;

import com.bencodez.votingplugin.events.PlayerVoteEvent;
import com.dre.brewery.api.events.brew.BrewModifyEvent;
import net.momirealms.customcrops.api.core.block.BreakReason;
import net.momirealms.customcrops.api.event.CropBreakEvent;
import net.momirealms.customfishing.api.event.FishingResultEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import plugin.borealcore.cooking.CookResultEvent;
import plugin.borealcore.utility.AdventureUtil;
import plugin.borealcore.utility.DebugLevel;

import static com.dre.brewery.api.events.brew.BrewModifyEvent.Type.SEAL;

public class JadeSourceListener implements Listener {
    private final JadeModule jadeModule;

    public JadeSourceListener(JadeModule jadeModule) {
        this.jadeModule = jadeModule;
    }

    @EventHandler
    public void onPlayerFishEvent(FishingResultEvent event) {
        if (event.isCancelled()) {
            return;
        }
        JadeModule.fishingJade(event);
    }

    @EventHandler
    public void onCropBreakEvent(CropBreakEvent event) {
        if (event.isCancelled() || event.reason() != BreakReason.ACTION) {
            return;
        }
        AdventureUtil.consoleMessage(DebugLevel.DEBUG, "Processing farmingJade for player: " + event.entityBreaker());
        jadeModule.farmingJade(event);
    }

    @EventHandler
    public void onBrewEvent(BrewModifyEvent event) {
        if (event.getType() != SEAL || event.isCancelled() || event.getPlayer() == null) {
            return;
        }
        jadeModule.breweryJade(event);
    }

    @EventHandler
    public void onVote(PlayerVoteEvent event) {
        if (event.isWasOnline()) {
            JadeModule.give(Bukkit.getPlayer(event.getPlayer()), 1, "voting");
        } else {
            JadeModule.giveOffline(Bukkit.getOfflinePlayer(event.getPlayer()), 1, "voting");
        }
    }

    // @TODO add the listeners for the vote tier rewards

    @EventHandler
    public void onCook(CookResultEvent event) {
        if (event.isPerfect()) {
            jadeModule.cookingJade(event.getPlayer());
        }
    }
}
