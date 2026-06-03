package plugin.borealcore.api.action.action;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.Nullable;
import plugin.borealcore.api.action.Action;

public record PotionEffectImpl(PotionEffect[] potionEffects) implements Action {

    @Override
    public void doOn(Player player, @Nullable Player anotherPlayer) {
        for (PotionEffect potionEffect : potionEffects) {
            player.addPotionEffect(potionEffect);
        }
    }
}
