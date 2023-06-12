package plugin.customcooking.manager;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import plugin.customcooking.competition.placeholder.CompetitionPapi;
import plugin.customcooking.minigame.Function;
import plugin.customcooking.util.PlaceholderUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlaceholderManager extends Function {

    private final Pattern placeholderPattern = Pattern.compile("%([^%]*)%");
    private CompetitionPapi competitionPapi;
    private boolean hasPlaceholderAPI = false;

    public PlaceholderManager() {
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            hasPlaceholderAPI = true;
            this.competitionPapi = new CompetitionPapi();
        }
        load();
    }

    public String parse(Player player, String text) {
        if (hasPlaceholderAPI) {
            return PlaceholderUtil.setPlaceholders(player, text);
        }
        return text;
    }

    @Override
    public void load() {
        if (competitionPapi != null) competitionPapi.register();
    }

    @Override
    public void unload() {
        if (this.competitionPapi != null) competitionPapi.unregister();
    }

    public List<String> detectPlaceholders(String text){
        if (text == null || !text.contains("%")) return Collections.emptyList();
        List<String> placeholders = new ArrayList<>();
        Matcher matcher = placeholderPattern.matcher(text);
        while (matcher.find()) placeholders.add(matcher.group());
        return placeholders;
    }
}
