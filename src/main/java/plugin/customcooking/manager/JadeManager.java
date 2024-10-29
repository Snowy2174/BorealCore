package plugin.customcooking.manager;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import plugin.customcooking.manager.configs.MessageManager;
import plugin.customcooking.object.Function;
import plugin.customcooking.util.AdventureUtil;
import plugin.customcooking.util.InventoryUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.bukkit.Bukkit.getServer;
import static plugin.customcooking.manager.configs.ConfigManager.*;

public class JadeManager extends Function {
    public static Map<Player, Map<String, Integer>> LIMITS;

    @Override
    public void load() {
        LIMITS = new HashMap<>();
        AdventureUtil.consoleMessage("[CustomCooking] Initialised Jade limit system");
    }

    @Override
    public void unload() {
        if (LIMITS != null) LIMITS.clear();
    }

    public static void handleGiveJadeCommand(CommandSender sender, String[] args) {
        if (args.length < 3) {
            AdventureUtil.sendMessage(sender, MessageManager.infoNegative + "/jade give <player> <source> <amount>");
            return;
        }

        Player player = Bukkit.getPlayer(args[0]);
        if (player == null) {
            AdventureUtil.sendMessage(sender, MessageManager.infoNegative + MessageManager.playerNotExist);
            return;
        }

        String source = args[1];
        Integer amount = Integer.parseInt(args[2]);

        if (!LIMITS.containsKey(player)) {
            HashMap<String, Integer> sourceMap = new HashMap<>();
            sourceMap.put(source, amount);
            LIMITS.put(player, sourceMap);
            return;
        }

        Map<String, Integer> jadeMap = LIMITS.get(player);

        if (LIMITS.get(player).containsKey(source)) {
            Integer newJade = jadeMap.get(source) + amount;
            if (newJade > getLimitForSource(source)) {
                AdventureUtil.sendMessage(player, MessageManager.infoNegative + "You've reached your limit for Jade from " + source + " today, try again later.");
                return;
            }
            jadeMap.replace(source, newJade);
            giveJade(player, amount);

        } else {
            LIMITS.get(player).put(source, amount);
            AdventureUtil.sendMessage(player, MessageManager.infoPositive + "This is the first time you've gotten Jade from " + source + " today, you have " + getLimitForSource(source) + " remaining.");
            giveJade(player, amount);
        }

        String bcast = MessageManager.infoPositive + "Whilst " + source + ", " + player.getName() + " has found " + amount.toString() + "₪";
        getServer().broadcast(AdventureUtil.getComponentFromMiniMessage(bcast));
        AdventureUtil.sendMessage(sender, "Gave " + amount + " from " + source + " to " + player.getName());
    }

    private static void giveJade(Player player, Integer amount) {
        String command = "av User " + player.getName() + " AddPoints " + amount.toString();
        Bukkit.dispatchCommand(getServer().getConsoleSender(), command);
        AdventureUtil.sendMessage(player, MessageManager.infoPositive + "");
    }

    private static int getLimitForSource(String source) {
        switch (source) {
            case "fishing":
                return fishingLimit;
            case "cooking":
                return cookingLimit;
            case "crops":
                return cropsLimit;
            default:
                throw new IllegalArgumentException("Unknown source: " + source);
        }
    }


}
