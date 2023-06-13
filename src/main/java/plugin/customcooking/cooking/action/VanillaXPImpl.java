package plugin.customcooking.cooking.action;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.entity.Player;
import plugin.customcooking.util.AdventureUtil;

public record VanillaXPImpl(int amount, boolean mending) implements ActionInterface {

    @Override
    public void doOn(Player player, Player another) {
        player.giveExp(amount, mending);
        AdventureUtil.playerSound(player, Sound.Source.PLAYER, Key.key("minecraft:entity.experience_orb.pickup"), 1, 1);
    }
}
