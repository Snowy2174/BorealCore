package plugin.customcooking.manager;

import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import plugin.customcooking.cooking.Recipe;
import plugin.customcooking.cooking.action.*;
import plugin.customcooking.manager.configs.ConfigManager;
import plugin.customcooking.object.Function;
import plugin.customcooking.util.AdventureUtil;
import plugin.customcooking.util.ConfigUtil;
import plugin.customcooking.util.GUIUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static plugin.customcooking.manager.configs.RecipeManager.RECIPES;
import static plugin.customcooking.util.AdventureUtil.getComponentFromMiniMessage;

public class EffectManager extends Function {
    public static Map<String, List<PotionEffect>> EFFECTS;
    @Override
    public void load() {
        EFFECTS = new HashMap<>();
        loadEffects();
        AdventureUtil.consoleMessage("[CustomCooking] Loaded <green>" + EFFECTS.size() + " <gray>buff categories");
    }

    @Override
    public void unload() {
        if (EFFECTS != null) EFFECTS.clear();
    }

    private void loadEffects() {
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
                            if (type == null)
                                AdventureUtil.consoleMessage("<red>[CustomCooking] Potion effect " + section.getString(action + "." + key + ".type", "BLINDNESS") + " doesn't exists");
                            potionEffectList.add(new PotionEffect(
                                    type == null ? PotionEffectType.LUCK : type,
                                    section.getInt(action + "." + key + ".duration"),
                                    section.getInt(action + "." + key + ".amplifier")
                            ));
                        }
                        actions.add(new PotionEffectImpl(potionEffectList.toArray(new PotionEffect[0])));
                    }
                    case "dish-buff" -> {
                        actions.add(new PotionEffectImpl(EFFECTS.get(section.getString(action)).toArray(new PotionEffect[0])));
                    }
                }
            }
            return actions.toArray(new Action[0]);
        }
        return null;
    }

    public static List<Component> buildEffectLore(List<PotionEffect> effectsList) {
        List<Component> lore = new ArrayList<>();
        for (PotionEffect potionEffect : effectsList) {
            lore.add(getComponentFromMiniMessage(ConfigManager.effectLore
                    .replace("{effect}", GUIUtil.formatString(potionEffect.getType().getName()))
                    .replace("{amplifier}", amplifierToRoman(potionEffect.getAmplifier())
                    .replace("{duration}", String.valueOf(potionEffect.getDuration()/20))))
        );}
        return lore;
    }

    private static String amplifierToRoman(int amplifier) {
        int[] values = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
        String[] romanLetters = {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};
        StringBuilder roman = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            while (amplifier >= values[i]) {
                amplifier = amplifier - values[i];
                roman.append(romanLetters[i]);
            }
        }
        return roman.toString();
    }

    public static void addPotionEffectLore(ItemStack itemStack, String key) {
        Recipe recipe = RECIPES.get(key.replaceAll("[\\[\\]]", ""));
        if (recipe != null && recipe.getDishEffectsLore() != null) {
            ItemMeta itemMeta = itemStack.getItemMeta();
            List<Component> lore = itemMeta.lore();

            if (lore == null) {
                lore = new ArrayList<>();
            }

            // Find the index to insert the new lore lines
            int insertIndex = Math.min(2, lore.size()); // Insert after the second line, or at the end if there are fewer than two lines

            // Insert the new lore lines
            lore.add(insertIndex, Component.newline());
            lore.addAll(insertIndex + 1, new ArrayList<>(recipe.getDishEffectsLore()));

            itemMeta.lore(lore);
            itemStack.setItemMeta(itemMeta);
        }
    }
}
