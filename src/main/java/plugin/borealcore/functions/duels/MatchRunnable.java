package plugin.borealcore.functions.duels;


import com.meteordevelopments.duels.api.Duels;
import com.meteordevelopments.duels.api.arena.Arena;
import com.meteordevelopments.duels.api.arena.ArenaManager;
import com.meteordevelopments.duels.api.spectate.SpectateManager;
import com.meteordevelopments.duels.api.spectate.Spectator;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import plugin.borealcore.utility.AdventureUtil;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class MatchRunnable extends BukkitRunnable {
    private final Set<Player> players;
    private Player player1;
    private Player player2;
    private final SpectateManager spectateManager;
    private final ArenaManager arenaManager;
    private Arena arena;


    public MatchRunnable(Set<Player> players) {
        Duels api = (Duels) Bukkit.getServer().getPluginManager().getPlugin("Duels");
        setPlayers(players);

        this.players = players;

        this.spectateManager = api.getSpectateManager();
        this.arenaManager = api.getArenaManager();

        if (player1 != null) {
            this.arena = arenaManager.get(player1);
        }
    }


    @Override
    public void run() {
        //plugin.getLogger().info("Match: " + arena.toString() + " Players: " + this.players.toString());
        List<Spectator> spectators = getSpectators(spectateManager);
        for (Spectator spectator : spectators) {
            Player player = spectator.getPlayer();
            AdventureUtil.playerActionbar(player, getFormat());
        }
    }

    public Set<Player> getPlayers() {
        return players;
    }

    public void setPlayers(Set<Player> players) {
        Iterator<Player> iterator = players.iterator();
        if (iterator.hasNext()) {
            player1 = iterator.next();
        }
        if (iterator.hasNext()) {
            player2 = iterator.next();
        }
    }

    public List<Spectator> getSpectators(SpectateManager spectateManager) {
        return spectateManager.getSpectators(arena);
    }

    public String getFormat() {
        return String.format("<gray>%s: <red>%d / %.0f <grey>| %s: <red>%d / %.0f",
                player1.getName(), (int) player1.getHealth(), player1.getMaxHealth(),
                player2.getName(), (int) player2.getHealth(), player2.getMaxHealth());
    }

    public Player getPlayer1() {
        return player1;
    }

    public Player getPlayer2() {
        return player2;
    }

    public double getHealth1() {
        return player1.getHealth();
    }

    public double getHealth2() {
        return player2.getHealth();
    }
}
