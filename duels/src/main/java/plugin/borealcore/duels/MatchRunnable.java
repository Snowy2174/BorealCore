package plugin.borealcore.duels;


import com.meteordevelopments.duels.api.Duels;
import com.meteordevelopments.duels.api.arena.Arena;
import com.meteordevelopments.duels.api.spectate.SpectateManager;
import com.meteordevelopments.duels.api.spectate.Spectator;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import plugin.borealcore.utility.AdventureUtil;
import plugin.borealcore.utility.DebugLevel;

import java.util.List;

import static plugin.borealcore.manager.MessageManager.actionBarHealth;

public class MatchRunnable extends BukkitRunnable {
    private final Player player1;
    private final Player player2;
    private final SpectateManager spectateManager;
    private final Arena arena;


    public MatchRunnable(Player player1, Player player2, Arena arena) {
        Duels api = (Duels) Bukkit.getServer().getPluginManager().getPlugin("Duels");

        this.player1 = player1;
        this.player2 = player2;
        this.arena = arena;

        this.spectateManager = api.getSpectateManager();
    }

    @Override
    public void run() {
        List<Spectator> spectators = getSpectators(spectateManager);
        for (Spectator spectator : spectators) {
            Player player = spectator.getPlayer();
            if (player.getGameMode() != GameMode.SPECTATOR) {
                player.setGameMode(GameMode.SPECTATOR);
                AdventureUtil.consoleMessage(DebugLevel.DEBUG, "Player " + player.getName() + "'s game mode changed to spectator whilst spectating " + arena.getName());
            }
            AdventureUtil.playerActionbar(player, String.format(actionBarHealth,
                    player1.getName(), player1.getHealth(), player1.getMaxHealth(),
                    player2.getName(), player2.getHealth(), player2.getMaxHealth()));
        }
    }


    public List<Spectator> getSpectators(SpectateManager spectateManager) {
        return spectateManager.getSpectators(arena);
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