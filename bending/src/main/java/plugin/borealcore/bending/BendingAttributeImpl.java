package plugin.borealcore.bending;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import plugin.borealcore.api.action.Action;

public record BendingAttributeImpl() implements Action {

    @Override
    public void doOn(Player player, @Nullable Player anotherPlayer) {

    }
}
