package plugin.customcooking.manager;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import plugin.customcooking.cooking.action.*;
import plugin.customcooking.object.Function;
import plugin.customcooking.util.AdventureUtil;
import plugin.customcooking.util.ConfigUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class EffectManager extends Function {
    public static Map<String, List<PotionEffect>> EFFECTS;

    @Override
    public void load() {
        EFFECTS = new HashMap<>();
        YamlConfiguration config = ConfigUtil.getConfig("buffs.yml");
        for (String sectionName : config.getKeys(false)) {
            ConfigurationSection section = config.getConfigurationSection(sectionName);
            List<PotionEffect> effectsList = new ArrayList<>();

            // Iterate over the keys in each section
            for (String levelKey : section.getKeys(false)) {
                ConfigurationSection levelSection = section.getConfigurationSection(levelKey);

                String typeString = levelSection.getString("type");
                PotionEffectType type = PotionEffectType.getByName(typeString.toUpperCase());
                if (type == null) {
                    // Handle invalid potion effect type
                    AdventureUtil.consoleMessage("<red>[CustomCooking] Potion effect " + typeString + " doesn't exist!");
                    continue;
                }

                int duration = levelSection.getInt("duration");
                int amplifier = levelSection.getInt("amplifier");

                PotionEffect potionEffect = new PotionEffect(type, duration*1200, amplifier);
                effectsList.add(potionEffect);
            }
            EFFECTS.put(sectionName, effectsList);
        }
        AdventureUtil.consoleMessage("[CustomCooking] Loaded <green>" + EFFECTS.size() + " <gray>buff categories");
    }

    @Override
    public void unload() {
        if (EFFECTS != null) EFFECTS.clear();
    }

    public static Action[] getActions(ConfigurationSection section, String nick) {
        if (section != null) {
            List<Action> actions = new ArrayList<>();
            for (String action : section.getKeys(false)) {
                switch (action) {
                    case "message" -> actions.add(new MessageActionImpl(section.getStringList(action).toArray(new String[0]), nick));
                    case "command" -> actions.add(new CommandActionImpl(section.getStringList(action).toArray(new String[0]), nick));
                    case "exp" -> actions.add(new VanillaXPImpl(section.getInt(action), false));
                    case "mending" -> actions.add(new VanillaXPImpl(section.getInt(action), true));
                    case "sound" -> actions.add(new SoundActionImpl(
                            section.getString(action + ".source"),
                            section.getString(action + ".key"),
                            (float) section.getDouble(action + ".volume"),
                            (float) section.getDouble(action + ".pitch")
                    ));
                    case "potion-effect" -> {
                        List<PotionEffect> potionEffectList = new ArrayList<>();
                        for (String key : section.getConfigurationSection(action).getKeys(false)) {
                            PotionEffectType type = PotionEffectType.getByName(section.getString(action + "." + key + ".type", "BLINDNESS").toUpperCase());
                            if (type == null) AdventureUtil.consoleMessage("<red>[CustomCooking] Potion effect " + section.getString(action + "." + key + ".type", "BLINDNESS") + " doesn't exists");
                            potionEffectList.add(new PotionEffect(
                                    type == null ? PotionEffectType.LUCK : type,
                                    section.getInt(action + "." + key + ".duration"),
                                    section.getInt(action + "." + key + ".amplifier")
                            ));
                        }
                        actions.add(new PotionEffectImpl(potionEffectList.toArray(new PotionEffect[0])));
                    }
                    case "dish-buff" -> actions.add(new PotionEffectImpl(EFFECTS.get(section.getString(action)).toArray(new PotionEffect[0])));
                }
            }
            return actions.toArray(new Action[0]);
        }
        return null;
    }
}
