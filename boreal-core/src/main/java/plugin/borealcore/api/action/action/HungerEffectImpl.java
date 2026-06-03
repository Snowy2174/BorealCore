package plugin.borealcore.api.action.action;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import plugin.borealcore.api.action.Action;
import plugin.borealcore.object.DebugLevel;

import static plugin.borealcore.utility.AdventureUtil.consoleMessage;

public record HungerEffectImpl(int hunger) implements Action {

    @Override
    public void doOn(Player player, @Nullable Player anotherPlayer) {
        consoleMessage(DebugLevel.DEBUG, "Applying hunger effect: current=" + player.getFoodLevel() + ", hunger=" + hunger);
        int foodLevel = player.getFoodLevel();
        int newFoodLevel = Math.min(20, foodLevel + hunger);
        player.setFoodLevel(newFoodLevel);
    }
}
