package plugin.borealcore.duels;

import com.meteordevelopments.duels.api.Duels;
import com.meteordevelopments.duels.api.arena.Arena;
import com.meteordevelopments.duels.api.arena.ArenaManager;
import com.meteordevelopments.duels.api.event.match.MatchEndEvent;
import com.meteordevelopments.duels.api.event.match.MatchStartEvent;
import org.bukkit.entity.Player;
import plugin.borealcore.BorealCore;
import plugin.borealcore.api.module.BorealModule;
import plugin.borealcore.api.module.ModuleContext;
import plugin.borealcore.utility.DebugLevel;
import plugin.borealcore.utility.AdventureUtil;

import java.util.HashMap;
import java.util.Set;

import static org.bukkit.Bukkit.getServer;

public class DuelsModule implements BorealModule {

    private static BorealCore plugin;
    private static ArenaManager arenaManager;
    private static final HashMap<Arena, MatchRunnable> ongoingRunnables = new HashMap<>();

    public DuelsModule() {
        plugin = BorealCore.plugin;
        arenaManager = ((Duels) getServer().getPluginManager().getPlugin("Duels")).getArenaManager();
    }

    @Override
    public void onModuleEnable() {
        getServer().getPluginManager().registerEvents(new DuelsListener(), plugin);
    }

    @Override
    public void onModuleDisable() {
        ongoingRunnables.clear();
    }

    @Override
    public void onModuleInitialize(ModuleContext context) {
    }

    public static void startMatch(MatchStartEvent event) {
        Player[] players = event.getPlayers();
        Arena arena = event.getMatch().getArena();

        if (players.length != 2) {
            AdventureUtil.consoleMessage(DebugLevel.DEBUG,
                    "Skipping healthbar runnable because this match has " + players.length + " players.");
            return;
        }

        MatchRunnable task = new MatchRunnable(players[0], players[1], arena);

        if (ongoingRunnables.containsKey(arena)) {
            AdventureUtil.consoleMessage(DebugLevel.DEBUG, "Match already ongoing for arena: " + arena.getName());
            ongoingRunnables.remove(arena).cancel();
        }

        task.runTaskTimer(plugin, 0L, 20L);
        ongoingRunnables.put(arena, task);
        AdventureUtil.consoleMessage(DebugLevel.DEBUG, "Match started: " + ongoingRunnables.get(arena));
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
