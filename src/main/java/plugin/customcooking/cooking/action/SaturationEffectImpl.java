package plugin.customcooking.cooking.action;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public record SaturationEffectImpl(int saturation) implements Action {

    @Override
    public void doOn(Player player, @Nullable Player anotherPlayer) {
        float saturationLevel = player.getSaturation();
        float newSaturationLevel = Math.min(20, saturationLevel + saturation);
        player.setSaturation(newSaturationLevel);


    }
}
