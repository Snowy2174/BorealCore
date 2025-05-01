package plugin.customcooking.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
}
