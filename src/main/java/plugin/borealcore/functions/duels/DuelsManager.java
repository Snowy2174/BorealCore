package plugin.borealcore.functions.duels;

import com.meteordevelopments.duels.api.Duels;
import com.meteordevelopments.duels.api.arena.Arena;
import com.meteordevelopments.duels.api.arena.ArenaManager;
import com.meteordevelopments.duels.api.event.match.MatchEndEvent;
import com.meteordevelopments.duels.api.event.match.MatchStartEvent;
import org.bukkit.entity.Player;
import plugin.borealcore.BorealCore;
import plugin.borealcore.manager.configs.DebugLevel;
import plugin.borealcore.object.Function;
import plugin.borealcore.utility.AdventureUtil;

import java.util.HashMap;
import java.util.Set;

import static org.bukkit.Bukkit.getServer;

public class DuelsManager extends Function {

    private static BorealCore plugin;
    private static ArenaManager arenaManager;
    private static final HashMap<Arena, MatchRunnable> ongoingRunnables = new HashMap<>();

    public DuelsManager() {
        plugin = BorealCore.plugin;
        arenaManager = ((Duels) getServer().getPluginManager().getPlugin("Duels")).getArenaManager();
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
        MatchRunnable task = new MatchRunnable(players);
        Player player1 = task.getPlayer1();
        Arena arena = arenaManager.get(player1);

        if (ongoingRunnables.containsKey(arena)) {
            AdventureUtil.consoleMessage(DebugLevel.DEBUG, "Match already ongoing for arena: " + arena.getName());
            ongoingRunnables.remove(arena);
        }

        task.runTaskTimer(plugin, 0L, 20L);
        ongoingRunnables.put(arena, task);
        AdventureUtil.consoleMessage(DebugLevel.DEBUG,"Match started: " + ongoingRunnables.get(arena));
    }

    public static void endMatch(MatchEndEvent event) {
        Set<Player> players = event.getMatch().getPlayers();
        Arena arena = event.getMatch().getArena();

        if (!ongoingRunnables.containsKey(arena)) {
            AdventureUtil.consoleMessage(DebugLevel.DEBUG, "No ongoing match found for arena: " + arena.getName());
            return;
        }

        MatchRunnable task = ongoingRunnables.remove(arena);
        task.cancel();
        plugin.getLogger().info("Match ended: " + players);
    }

}
