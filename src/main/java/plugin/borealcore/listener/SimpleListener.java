package plugin.borealcore.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import plugin.borealcore.object.Function;

public record SimpleListener(Function function) implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        function.onJoin(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        function.onQuit(event.getPlayer());
    }
}