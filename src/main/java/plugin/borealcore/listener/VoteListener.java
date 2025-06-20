package plugin.borealcore.listener;

import com.bencodez.votingplugin.events.PlayerVoteEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import plugin.borealcore.functions.jade.JadeManager;

public class VoteListener implements Listener {
    private final JadeManager jadeManager;

    public VoteListener(JadeManager jadeManager) {
        this.jadeManager = jadeManager;
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
