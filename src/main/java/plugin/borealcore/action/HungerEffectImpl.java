package plugin.borealcore.action;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public record HungerEffectImpl(int hunger) implements Action {

    @Override
    public void doOn(Player player, @Nullable Player anotherPlayer) {
        int foodLevel = player.getFoodLevel();
        int newFoodLevel = Math.min(20, foodLevel + hunger / 4);
        player.setFoodLevel(newFoodLevel);
    }
}
