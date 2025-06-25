package plugin.borealcore.listener;



import com.meteordevelopments.duels.api.Duels;
import com.meteordevelopments.duels.api.event.match.MatchEndEvent;
import com.meteordevelopments.duels.api.event.match.MatchStartEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

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
}
