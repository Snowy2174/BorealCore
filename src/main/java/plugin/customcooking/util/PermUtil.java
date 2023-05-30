package plugin.customcooking.util;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PermUtil {

    public static void addPermission(UUID userUuid, String permission) {
        LuckPerms luckPerms = LuckPermsProvider.get();
        // Load, modify, then save
        luckPerms.getUserManager().modifyUser(userUuid, user -> {
            // Add the permission
            user.data().add(Node.builder(permission).build());
        });
    }

    public static void removePermission(UUID uniqueId, String permission) {
        LuckPerms luckPerms = LuckPermsProvider.get();
        // Load, modify, then save
        luckPerms.getUserManager().modifyUser(uniqueId, user -> {
            // Remove the permission
            user.data().remove(Node.builder(permission).build());
        });
    }

    public static boolean hasPermission(Player player, String permission) {
        LuckPerms luckPerms = LuckPermsProvider.get();
        User user = luckPerms.getPlayerAdapter(Player.class).getUser(player);
        return user.getCachedData().getPermissionData().checkPermission(permission).asBoolean();
    }
}
