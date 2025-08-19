package plugin.borealcore.functions.duels;

import com.meteordevelopments.duels.api.Duels;
import com.meteordevelopments.duels.api.event.match.MatchEndEvent;
import com.meteordevelopments.duels.api.event.match.MatchStartEvent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerGameModeChangeEvent;

import static plugin.borealcore.functions.duels.DuelsManager.endMatch;
import static plugin.borealcore.functions.duels.DuelsManager.startMatch;

public class DuelsListener implements Listener {


    public DuelsListener() {
        Duels api = (Duels) Bukkit.getServer().getPluginManager().getPlugin("Duels");
    }

    @EventHandler
    public void onMatchStart(MatchStartEvent event) {
        startMatch(event);
    }

    @EventHandler
    public void onMatchEnd(MatchEndEvent event) {
        endMatch(event);
    }

    @EventHandler
    public void onGamemodeChange(PlayerGameModeChangeEvent event) {
    }
}
