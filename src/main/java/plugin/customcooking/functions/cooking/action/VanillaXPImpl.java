package plugin.customcooking.functions.cooking.action;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.entity.Player;
import plugin.customcooking.utility.AdventureUtil;

public record VanillaXPImpl(int amount, boolean mending) implements Action {

    @Override
    public void doOn(Player player, Player another) {
        player.giveExp(amount, mending);
        AdventureUtil.playerSound(player, Sound.Source.PLAYER, Key.key("minecraft:entity.experience_orb.pickup"), 1, 1);
    }
}
