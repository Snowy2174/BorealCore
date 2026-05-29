package plugin.borealcore.manager;

import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import plugin.borealcore.object.Function;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlaceholderManager extends Function {

    private final Pattern placeholderPattern = Pattern.compile("%([^%]*)%");
    private boolean hasPlaceholderAPI = false;
    private final List<PlaceholderExpansion> registeredExpansions = new ArrayList<>();

    public PlaceholderManager() {
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            hasPlaceholderAPI = true;
        }
    }

    /**
     * Allows modules to register their own placeholder expansions.
     */
    public void registerExpansion(PlaceholderExpansion expansion) {
        if (hasPlaceholderAPI && expansion != null) {
            if (expansion.register()) {
                registeredExpansions.add(expansion);
            }
        }
    }

    /**
     * Allows modules to unregister their expansions (e.g., on module disable).
     */
    public void unregisterExpansion(PlaceholderExpansion expansion) {
        if (hasPlaceholderAPI && expansion != null) {
            expansion.unregister();
            registeredExpansions.remove(expansion);
        }
    }

    public static String setPlaceholders(Player player, String text) {
        return PlaceholderAPI.setPlaceholders(player, text);
    }

    public static String setPlaceholders(OfflinePlayer player, String text) {
        return PlaceholderAPI.setPlaceholders(player, text);
    }

    public String parse(Player player, String text) {
        if (hasPlaceholderAPI) {
            return setPlaceholders(player, text);
        }
        return text;
    }

    @Override
    public void load() {
        // Modules handle placeholder loading internally
    }

    @Override
    public void unload() {
        if (hasPlaceholderAPI) {
            for (PlaceholderExpansion expansion : new ArrayList<>(registeredExpansions)) {
                expansion.unregister();
            }
            registeredExpansions.clear();
        }
    }

    public List<String> detectPlaceholders(String text) {
        if (text == null || !text.contains("%")) return Collections.emptyList();
        List<String> placeholders = new ArrayList<>();
        Matcher matcher = placeholderPattern.matcher(text);
        while (matcher.find()) placeholders.add(matcher.group());
        return placeholders;
    }
}