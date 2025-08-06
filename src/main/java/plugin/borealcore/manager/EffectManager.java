package plugin.borealcore.manager;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import plugin.borealcore.action.*;
import plugin.borealcore.functions.cooking.configs.RecipeManager;
import plugin.borealcore.functions.cooking.object.Recipe;
import plugin.borealcore.manager.configs.ConfigManager;
import plugin.borealcore.manager.configs.DebugLevel;
import plugin.borealcore.object.Function;
import plugin.borealcore.utility.AdventureUtil;
import plugin.borealcore.utility.ConfigUtil;
import plugin.borealcore.utility.GUIUtil;

import java.util.*;

import static plugin.borealcore.utility.AdventureUtil.getComponentFromMiniMessage;

public class EffectManager extends Function {
    public static Map<String, List<PotionEffect>> EFFECTS;

    @Override
    public void load() {
        EFFECTS = new HashMap<>();
        loadEffects();
        AdventureUtil.consoleMessage("Loaded <green>" + EFFECTS.size() + " <gray>buff categories");
    }

    @Override
    public void unload() {
        if (EFFECTS != null) EFFECTS.clear();
    }

    private void loadEffects() {
        YamlConfiguration config = ConfigUtil.getConfig("recipes/buffs.yml");
        for (String sectionName : config.getKeys(false)) {
            ConfigurationSection section = config.getConfigurationSection(sectionName);
            List<PotionEffect> effectsList = new ArrayList<>();
            List<PotionEffect> perfectEffectsList = new ArrayList<>();

            for (String levelKey : section.getKeys(false)) {
                ConfigurationSection levelSection = section.getConfigurationSection(levelKey);

                String typeString = levelSection.getString("type");
                PotionEffectType type = PotionEffectType.getByName(typeString.toUpperCase());
                if (type == null) {
                    AdventureUtil.consoleMessage(DebugLevel.ERROR, "Potion effect " + typeString + " doesn't exist!");
                    continue;
                }

                int duration = levelSection.getInt("duration") * 1200;
                int amplifier = levelSection.getInt("amplifier");

                effectsList.add(new PotionEffect(type, duration, amplifier));
                perfectEffectsList.add(new PotionEffect(type, duration / 2 * 3, amplifier + 1));
            }
            EFFECTS.put(sectionName, effectsList);
            EFFECTS.put(sectionName + ConfigManager.perfectItemSuffix, perfectEffectsList);
        }
    }

    public static Action[] getActions(ConfigurationSection section, String nick, boolean perfect) {
        if (section == null) return null;
        List<Action> actions = new ArrayList<>();
        for (String action : section.getKeys(false)) {
            switch (action) {
                case "hunger" -> actions.add(new HungerEffectImpl(section.getInt(action)));
                case "saturation" -> actions.add(new SaturationEffectImpl(section.getInt(action)));
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
                            AdventureUtil.consoleMessage("<red>Potion effect " + section.getString(action + "." + key + ".type", "BLINDNESS") + " doesn't exist");
                        potionEffectList.add(new PotionEffect(
                                type == null ? PotionEffectType.LUCK : type,
                                section.getInt(action + "." + key + ".duration"),
                                section.getInt(action + "." + key + ".amplifier")
                        ));
                    }
                    actions.add(new PotionEffectImpl(potionEffectList.toArray(new PotionEffect[0])));
                }
                case "dish-buff" -> {
                        String actionKey = section.getString(action);
                        if (perfect) {
                            actionKey += ConfigManager.perfectItemSuffix;
                        }
                        actions.add(new PotionEffectImpl(EFFECTS.get(actionKey).toArray(new PotionEffect[0])));
                }
            }
        }
        return actions.toArray(new Action[0]);
    }

    public static List<List<Component>> buildActionsLore(List<Action[]> actions) {
        if (actions == null || actions.isEmpty()) {
            AdventureUtil.consoleMessage(DebugLevel.WARNING, " No actions provided for lore generation.");
            return List.of(List.of(Component.text("No actions available.")));
        }
        List<List<Component>> lore = new ArrayList<>();
        for (Action[] actionArray : actions) {
            List<Component> actionLore = new ArrayList<>();
            if (actionArray == null || actionArray.length == 0) {
                AdventureUtil.consoleMessage(DebugLevel.WARNING, " Empty action array found.");
                continue;
            }
            if (actions != null) {
                for (Action action : actionArray) {
                    if (action instanceof PotionEffectImpl potionEffectAction) {
                        for (PotionEffect potionEffect : potionEffectAction.potionEffects()) {
                            actionLore.add(getComponentFromMiniMessage(ConfigManager.effectLore
                                    .replace("{effect}", GUIUtil.formatString(potionEffect.getType().getName()))
                                    .replace("{amplifier}", amplifierToRoman(potionEffect.getAmplifier() + 1))
                                    .replace("{duration}", getDuration(potionEffect.getDuration() / 20))));
                        }
                        actionLore.add(Component.text(" "));
                    } else if (action instanceof HungerEffectImpl hungerEffectAction) {
                        actionLore.add(getComponentFromMiniMessage(ConfigManager.hungerLore
                                .replace("{hunger}", String.valueOf(hungerEffectAction.hunger()))));
                    } else if (action instanceof SaturationEffectImpl saturationEffectAction) {
                        actionLore.add(getComponentFromMiniMessage(ConfigManager.saturationLore
                                .replace("{saturation}", String.valueOf(saturationEffectAction.saturation()))));
                    }
                }
                lore.add(actionLore);
            } else {
                AdventureUtil.consoleMessage(DebugLevel.WARNING, " No actions found for the provided key.");
            }
        }
        return lore;
    }

    private static String getDuration(int durationInSeconds) {
        if (durationInSeconds <= 0) {
            return " ";
        }
        int minutes = durationInSeconds / 60;
        int seconds = durationInSeconds % 60;
        StringBuilder durationString = new StringBuilder().append("<gold>for ");
        if (minutes > 0) {
            durationString.append(minutes).append(minutes > 1 ? " mins " : " min ");
        }
        if (seconds > 0) {
            durationString.append(seconds).append("s");
        }
        return durationString.toString();
    }

    private static String amplifierToRoman(int amplifier) {
        int[] values = {10, 9, 5, 4, 1};
        String[] romanLetters = {"X", "IX", "V", "IV", "I"};

        StringBuilder roman = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            while (amplifier >= values[i]) {
                amplifier -= values[i];
                roman.append(romanLetters[i]);
            }
        }
        return roman.toString();
    }

    public static void addPotionEffectLore(ItemStack itemStack, String key, Boolean perfect) {
        Recipe recipe = RecipeManager.COOKING_RECIPES.get(key.replaceAll("[\\[\\]]", "").replace(ConfigManager.perfectItemSuffix, ""));

        if (recipe != null && recipe.getDishEffectsLore() != null) {
            ItemMeta itemMeta = itemStack.getItemMeta();
            if (itemMeta == null) {
                itemMeta = Bukkit.getItemFactory().getItemMeta(itemStack.getType());
                itemStack.setItemMeta(itemMeta);
            }

            List<Component> lore = itemMeta.lore();
            if (lore == null) {
                lore = new ArrayList<>();
            }

            int insertIndex = Math.min(2, lore.size()); // Insert after the second line or at the end if there are fewer than two lines
            lore.add(insertIndex, Component.text(" "));
            lore.addAll(insertIndex + 1, (perfect ? recipe.getDishEffectsLore().get(0) : recipe.getDishEffectsLore().get(1)));

            itemMeta.lore(lore);
            itemStack.setItemMeta(itemMeta);
        } else {
            AdventureUtil.consoleMessage(DebugLevel.WARNING, "No valid recipe found for key: " + key);
        }
    }
}
