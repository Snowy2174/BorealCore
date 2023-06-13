package plugin.customcooking.cooking.action;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.entity.Player;
import plugin.customcooking.util.AdventureUtil;

public record SoundActionImpl(String source, String sound, float volume, float pitch) implements Action {

    @Override
    public void doOn(Player player, Player another) {
        AdventureUtil.playerSound(player, Sound.Source.valueOf(source.toUpperCase()), Key.key(sound), volume, pitch);
    }
}
