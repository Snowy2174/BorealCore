package plugin.borealcore.functions.duels;

import me.realized.duels.api.Duels;
import me.realized.duels.api.arena.Arena;
import me.realized.duels.api.arena.ArenaManager;
import me.realized.duels.api.event.match.MatchEndEvent;
import me.realized.duels.api.event.match.MatchStartEvent;
import org.bukkit.entity.Player;
import plugin.borealcore.BorealCore;
import plugin.borealcore.listener.DuelsListener;
import plugin.borealcore.object.Function;

import java.util.HashMap;
import java.util.Set;

import static org.bukkit.Bukkit.getServer;

public class DuelsManager extends Function {

    private static HashMap<Arena, MatchRunable> ongoingRunnables = new HashMap<>();
    private static BorealCore plugin;
    private static ArenaManager arenaManager;

    public DuelsManager() {
        this.plugin = BorealCore.plugin;
        this.arenaManager = ((Duels) getServer().getPluginManager().getPlugin("Duels")).getArenaManager();
    }

    @Override
    public void load() {
        getServer().getPluginManager().registerEvents(new DuelsListener(), plugin);
    }

    @Override
    public void unload() {
        ongoingRunnables.clear();
    }

    public static void startMatch(MatchStartEvent event) {
        Set<Player> players = event.getMatch().getPlayers();
        MatchRunable task = new MatchRunable(players);
        Player player1 = task.getPlayer1();
        Arena arena = arenaManager.get(player1);

        task.runTaskTimer(plugin, 0L, 20L);
        ongoingRunnables.put(arena, task);
        plugin.getLogger().info("Match started: " + ongoingRunnables.get(arena));
    }

    public static void endMatch(MatchEndEvent event) {
        Set<Player> players = event.getMatch().getPlayers();
        Player player1 = players.iterator().next();
        Arena arena = arenaManager.get(player1);

        MatchRunable task = ongoingRunnables.remove(arena);
        if (task != null) {
            task.cancel();
        }
        plugin.getLogger().info("Match ended: " + players);
    }

}
