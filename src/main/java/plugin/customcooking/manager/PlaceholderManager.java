package plugin.customcooking.manager;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import plugin.customcooking.cooking.CookingPapi;
import plugin.customcooking.cooking.competition.placeholder.CompetitionPapi;
import plugin.customcooking.object.Function;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlaceholderManager extends Function {

    private final Pattern placeholderPattern = Pattern.compile("%([^%]*)%");
    private CompetitionPapi competitionPapi;
    private CookingPapi cookingPapi;
    private JadePapi jadePapi;
    private boolean hasPlaceholderAPI = false;

    public PlaceholderManager() {
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            hasPlaceholderAPI = true;
            this.competitionPapi = new CompetitionPapi();
            this.cookingPapi = new CookingPapi();
            this.jadePapi = new JadePapi();
        }
        load();
    }

    public String parse(Player player, String text) {
        if (hasPlaceholderAPI) {
            return setPlaceholders(player, text);
        }
        return text;
    }

    @Override
    public void load() {
        if (competitionPapi != null) competitionPapi.register();
        if (jadePapi != null) jadePapi.register();
        if (cookingPapi != null) cookingPapi.register();
    }

    @Override
    public void unload() {
        if (this.competitionPapi != null) competitionPapi.unregister();
        if (this.jadePapi != null) jadePapi.unregister();
        if (this.cookingPapi != null) cookingPapi.unregister();
    }

    public List<String> detectPlaceholders(String text){
        if (text == null || !text.contains("%")) return Collections.emptyList();
        List<String> placeholders = new ArrayList<>();
        Matcher matcher = placeholderPattern.matcher(text);
        while (matcher.find()) placeholders.add(matcher.group());
        return placeholders;
    }

    public static String setPlaceholders(Player player, String text) {
        return PlaceholderAPI.setPlaceholders(player, text);
    }

    public static String setPlaceholders(OfflinePlayer player, String text) {
        return PlaceholderAPI.setPlaceholders(player, text);
    }
}
