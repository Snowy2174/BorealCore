package plugin.borealcore.action;

import com.dre.brewery.api.BreweryApi;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import plugin.borealcore.manager.configs.DebugLevel;

import static plugin.borealcore.utility.AdventureUtil.consoleMessage;

public record DrunknessEffectImpl(int drunkness) implements Action {

    @Override
    public void doOn(Player player, @Nullable Player anotherPlayer) {
        if (BreweryApi.getBPlayer(player) == null) {
            consoleMessage(DebugLevel.DEBUG, "Player is not drunk: " + player.getName());
            return;
        }
        consoleMessage(DebugLevel.DEBUG, "Applying drunkness effect: current=" + BreweryApi.getBPlayer(player).getDrunkeness() + ", drunkness=" + drunkness);
        int drunkLevel = BreweryApi.getBPlayer(player).getDrunkeness();
        int newDrunkLevel = Math.max(0, drunkLevel - drunkness);
        BreweryApi.setPlayerDrunk(player, newDrunkLevel, 0);
    }
}
