package plugin.borealcore.functions.herbalism.configs;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.potion.PotionEffect;
import plugin.borealcore.BorealCore;
import plugin.borealcore.functions.cooking.Difficulty;
import plugin.borealcore.functions.cooking.configs.LayoutManager;
import plugin.borealcore.functions.cooking.object.Layout;
import plugin.borealcore.functions.herbalism.objects.Herb;
import plugin.borealcore.functions.herbalism.objects.HerbalismType;
import plugin.borealcore.functions.herbalism.objects.Modifier;
import plugin.borealcore.functions.herbalism.objects.ModifierType;
import plugin.borealcore.manager.EffectManager;
import plugin.borealcore.manager.configs.DebugLevel;
import plugin.borealcore.object.Function;
import plugin.borealcore.utility.AdventureUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class HerbManager extends Function {

    public static HashMap<String, Herb> HERBS;

    @Override
    public void load() {
        HERBS = new HashMap<>();
        loadItems();
        AdventureUtil.consoleMessage("Loaded <green>" + (HERBS.size()) + " <gray>herbalism items");
    }

    @Override
    public void unload() {
        if (HERBS != null) HERBS.clear();
    }

    public void loadItems() {
        File recipe_file = new File(BorealCore.plugin.getDataFolder() + File.separator + "herbalism");
        if (!recipe_file.exists()) {
            if (!recipe_file.mkdir()) return;
            BorealCore.plugin.saveResource(BorealCore.plugin.getDataFolder() + File.separator + "herbalism.yml", false);
        }
        File[] files = recipe_file.listFiles();
        if (files == null) return;
        for (File file : files) {
            if (!file.getName().equals("herbalism.yml")) continue;
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            Set<String> items = config.getKeys(false);

            for (String key : items) {

                ConfigurationSection itemSection = config.getConfigurationSection(key);

                // Bar mechanic
                List<Difficulty> difficulties = new ArrayList<>();
                List<String> difficultyList = itemSection.getStringList("difficulty");
                if (difficultyList.isEmpty()) {
                    String[] diff = StringUtils.split(itemSection.getString("difficulty", "1-1"), "-");
                    Difficulty difficulty = new Difficulty(Integer.parseInt(diff[0]), Integer.parseInt(diff[1]));
                    difficulties.add(difficulty);
                } else {
                    for (String difficultyStr : difficultyList) {
                        String[] diff = StringUtils.split(difficultyStr, "-");
                        Difficulty difficulty = new Difficulty(Integer.parseInt(diff[0]), Integer.parseInt(diff[1]));
                        difficulties.add(difficulty);
                    }
                }
                HerbalismType type = HerbalismType.valueOf(itemSection.getString("type"));
                if (type == null) {
                    type = HerbalismType.MODIFIER;
                }
                Herb herb = new Herb(
                        key,
                        itemSection.getString("nick", key),
                        type,
                        difficulties.toArray(new Difficulty[0]),
                        itemSection.getInt("time", 10000)
                );

                // Set layout
                if (itemSection.contains("layout")) {
                    List<Layout> layoutList = new ArrayList<>();
                    for (String layoutName : itemSection.getStringList("layout")) {
                        Layout layout = LayoutManager.LAYOUTS.get(layoutName);
                        if (layout == null) {
                            AdventureUtil.consoleMessage("<red>Bar " + layoutName + " doesn't exist");
                            continue;
                        }
                        layoutList.add(layout);
                    }
                    herb.setLayout(layoutList.toArray(new Layout[0]));
                }

                // Set colour
                if (itemSection.contains("colour")) {
                    String colorStr = itemSection.getString("colour");
                    int colour;
                    if (colorStr.startsWith("#")) {
                        colour = Integer.parseInt(colorStr.substring(1), 16);
                    } else if (colorStr.contains(",")) {
                        String[] rgb = colorStr.split(",");
                        colour = (Integer.parseInt(rgb[0]) << 16) | (Integer.parseInt(rgb[1]) << 8) | Integer.parseInt(rgb[2]);
                    } else {
                        colour = 0xFFFFFF;
                    }
                    herb.setColour(colour);
                }

                herb.setActions(EffectManager.getActions(itemSection.getConfigurationSection("action.consume"), herb.getNick(), false));
                herb.setEffects(getEffects(itemSection.getConfigurationSection("effects")));

                if (type == HerbalismType.MODIFIER) {
                    herb.setModifiers(getModifiers(itemSection.getConfigurationSection("action.modify")));
                }

                HERBS.put(key, herb);
            }
        }
    }


    private Modifier[] getModifiers(ConfigurationSection section) {
        if (section != null) {
            List<Modifier> modifier = new ArrayList<>();
            for (String type : section.getKeys(false)) {
                if (ModifierType.valueOf(type) != null) {
                    modifier.add(new Modifier(ModifierType.valueOf(type.toUpperCase()), section.getInt(type)));
                } else {
                    AdventureUtil.consoleMessage(DebugLevel.WARNING, "Modifier type, " + type + " is unrecognised.");
                }
            }
            return modifier.toArray(new Modifier[0]);
        }
        return null;
    }

    private List<PotionEffect> getEffects(ConfigurationSection section) {
        return null;
    }
}
