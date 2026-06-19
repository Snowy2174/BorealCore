package plugin.borealcore.jade.object;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.EmbedBuilder;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import plugin.borealcore.jade.config.JadeMessage;

public class JadeEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList handlerList = new HandlerList();
    private final double amount;
    private final String source;
    private boolean cancelled;

    public JadeEvent(@NotNull Player who, double amount, @Nullable String source) {
        super(who);
        this.cancelled = false;
        this.amount = amount;
        this.source = source;
        sendDiscordMessage();
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return getHandlerList();
    }

    public double getAmount() {
        return amount;
    }

    @Nullable
    public String getSource() {
        return source;
    }

    public void sendDiscordMessage() {
        if (!cancelled) {
            String playerName = getPlayer().getName();
            String source = getSource() != null && !getSource().isEmpty() ? getSource() : "playing";
            String authorName = JadeMessage.jadeBroadcast
                    .replace("{player}", playerName)
                    .replace("{amount}", String.valueOf((int) getAmount()))
                    .replace("{source}", source);
            String avatarUrl = "https://cravatar.eu/avatar/" + playerName + "/64.png";

            EmbedBuilder embed = new EmbedBuilder()
                    .setAuthor(authorName, null, avatarUrl)
                    .setColor(0x00FF00);

            TextChannel textChannel = DiscordSRV.getPlugin().getDestinationTextChannelForGameChannelName("general");
            if (textChannel != null) {
                textChannel.sendMessageEmbeds(embed.build()).queue();
            }
        }
    }
}
