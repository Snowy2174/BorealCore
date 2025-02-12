package plugin.customcooking.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CookResultEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList handlerList = new HandlerList();
    private final ItemStack itemStack;
    private final String id;
    private boolean cancelled;
    private final boolean isPerfect;

    public CookResultEvent(@NotNull Player who, boolean isPerfect, @Nullable ItemStack itemStack, @Nullable String id) {
        super(who);
        this.cancelled = false;
        this.isPerfect = isPerfect;
        this.itemStack = itemStack;
        this.id = id;
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

    public boolean isPerfect() {
        return isPerfect;
    }

    @Nullable
    public ItemStack getItemStack() {
        return itemStack;
    }

    @Nullable
    public String getDishID() {
        return id;
    }
}
