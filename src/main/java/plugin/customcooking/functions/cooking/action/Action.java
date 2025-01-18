package plugin.customcooking.functions.cooking.action;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public interface Action {

    void doOn(Player player, @Nullable Player anotherPlayer);

}
