package plugin.customcooking.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import plugin.customcooking.cooking.Recipe;

    public class CookResultEvent extends PlayerEvent implements Cancellable {

        private boolean cancelled;
        private boolean isPerfect;

        private final ItemStack itemStack;
        private final String id;
        private static final HandlerList handlerList = new HandlerList();

        public CookResultEvent(@NotNull Player who, boolean isPerfect, @Nullable ItemStack itemStack, @Nullable String id) {
            super(who);
            this.cancelled = false;
            this.isPerfect = isPerfect;
            this.itemStack = itemStack;
            this.id = id;
        }

        @Override
        public boolean isCancelled() {
            return cancelled;
        }

        @Override
        public void setCancelled(boolean cancel) {
            cancelled = cancel;
        }

        public static HandlerList getHandlerList() {
            return handlerList;
        }

        @NotNull
        @Override
        public HandlerList getHandlers() {
            return getHandlerList();
        }

        public boolean isPerfect() {
            return isPerfect;
        }

        /**
         * Would be null if failed or caught a mob
         * @return loot id
         */
        @Nullable
        public ItemStack getItemStack() {
            return itemStack;
        }

        /**
         * Would be null if failed
         */
        @Nullable
        public String getDishID() {
            return id;
        }
    }
