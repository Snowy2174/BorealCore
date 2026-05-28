package plugin.borealcore.utility;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitTask;
import plugin.borealcore.BorealCore;
import plugin.borealcore.manager.configs.MessageManager;
import plugin.borealcore.utility.AdventureUtil;

import java.util.function.Consumer;

public class ChatInputUtil {

    public static void getChatInput(Player player, String message, String copyText, Consumer<String> callback) {
        AdventureUtil.playerMessage(player, message);
        AdventureUtil.playerMessage(player, "<yellow>Type '+cancel' to cancel.");

        InputListener listener = new InputListener(player, callback);
        Bukkit.getPluginManager().registerEvents(listener, BorealCore.getInstance());

        BukkitTask timeoutTask = Bukkit.getScheduler().runTaskLater(BorealCore.getInstance(), () -> {
            if (listener.isActive) {
                HandlerList.unregisterAll(listener);
                listener.isActive = false;
                AdventureUtil.playerMessage(player, MessageManager.infoNegative + MessageManager.configEditorTimedOut);
                Bukkit.getScheduler().runTask(BorealCore.getInstance(), () -> callback.accept(null));
            }
        }, 20 * 30); // 30 seconds timeout

        listener.timeoutTask = timeoutTask;
    }

    private static class InputListener implements Listener {
        private final Player player;
        private final Consumer<String> callback;
        private BukkitTask timeoutTask;
        private boolean isActive = true;

        public InputListener(Player player, Consumer<String> callback) {
            this.player = player;
            this.callback = callback;
        }

        @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
        public void onChat(AsyncChatEvent event) {
            if (!isActive || event.getPlayer() != player) {
                return;
            }

            event.setCancelled(true);
            String content = ((TextComponent) event.message()).content();

            if (content.equals("+cancel")) {
                AdventureUtil.playerMessage(player, MessageManager.infoNegative + MessageManager.configEditorCancelled);
                Bukkit.getScheduler().runTask(BorealCore.getInstance(), () -> callback.accept(null));
            } else {
                Bukkit.getScheduler().runTask(BorealCore.getInstance(), () -> callback.accept(content));
            }

            HandlerList.unregisterAll(this);
            if (timeoutTask != null) {
                timeoutTask.cancel();
            }
            isActive = false;
        }
    }
}