package plugin.customcooking.functions.karmicnode;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import plugin.customcooking.CustomCooking;
import plugin.customcooking.manager.configs.MessageManager;
import plugin.customcooking.object.Function;
import plugin.customcooking.utility.ConfigUtil;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static plugin.customcooking.utility.AdventureUtil.consoleMessage;
import static plugin.customcooking.utility.ConfigUtil.getConfig;

public class NodeManager extends Function {


    @Override
    public void load() {
        consoleMessage(MessageManager.prefix + "Loaded mastery values");
    }

    public static void handleUpdateMaxWave(String player, int wave) {
        YamlConfiguration config = ConfigUtil.getConfig("data/nodedata.yml");
        File file = new File(CustomCooking.plugin.getDataFolder(), "data/nodedata.yml");

        if (Bukkit.getPlayer(player) == null) {
            consoleMessage("Player " + player + " is not online, so no score added");
            return;
        }

        if (Bukkit.getPlayer(player).getGameMode() != GameMode.SURVIVAL) {
            consoleMessage("Player " + player + " is not in survival mode, so no score added");
            return;
        }

        String playerPath = "players." + player;

        if (!config.contains(playerPath)) {
            config.set(playerPath, 0);
        }

        if (wave > config.getInt(playerPath)) {
            config.set(playerPath, wave);
        }

        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }

    public static int getMaxWave(String playerName) {
        FileConfiguration config = getConfig("data/nodedata.yml");
        return config.getInt("players." + playerName, 0);
    }

    private static void savePlayerStats(FileConfiguration config) {
        File file = new File(CustomCooking.plugin.getDataFolder(), "data/nodedata.yml");
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getLeaderboardEntry(int index) {
        YamlConfiguration config = ConfigUtil.getConfig("data/nodedata.yml");
        File file = new File(CustomCooking.plugin.getDataFolder(), "data/nodedata.yml");

        Map<String, Integer> playerWaves = new HashMap<>();
        for (String key : config.getConfigurationSection("players").getKeys(false)) {
            playerWaves.put(key, config.getInt("players." + key));
        }

        List<Map.Entry<String, Integer>> sortedPlayers = playerWaves.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .collect(Collectors.toList());

        if (index < 0 || index >= sortedPlayers.size()) {
            return "N/A";
        }

        Map.Entry<String, Integer> entry = sortedPlayers.get(index);
        return entry.getKey() + " &7- &e" + entry.getValue();
    }
}
