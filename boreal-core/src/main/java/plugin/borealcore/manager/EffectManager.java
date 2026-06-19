package plugin.borealcore.manager;

import net.kyori.adventure.text.Component;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import plugin.borealcore.api.action.Action;
import plugin.borealcore.action.CommandActionImpl;
import plugin.borealcore.action.HungerEffectImpl;
import plugin.borealcore.action.MessageActionImpl;
import plugin.borealcore.action.PotionEffectImpl;
import plugin.borealcore.action.SaturationEffectImpl;
import plugin.borealcore.action.SoundActionImpl;
import plugin.borealcore.action.VanillaXPImpl;
import plugin.borealcore.utility.DebugLevel;
import plugin.borealcore.api.Function;
import plugin.borealcore.utility.AdventureUtil;
import plugin.borealcore.utility.GuiUtil;
import plugin.borealcore.utility.ItemUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static plugin.borealcore.utility.AdventureUtil.consoleMessage;
import static plugin.borealcore.utility.AdventureUtil.getComponentFromMiniMessage;

public class EffectManager extends Function {

    public static Map<String, List<PotionEffect>> EFFECTS;

    @FunctionalInterface
    public interface ActionFactory {
        Action create(ConfigurationSection section, String actionKey, String nick, boolean perfect);
    }

    @FunctionalInterface
    public interface ActionLoreProvider<T extends Action> {
        List<Component> generateLore(T action);
    }

    private static final Map<String, ActionFactory> ACTION_FACTORIES = new HashMap<>();
    private static final Map<Class<? extends Action>, ActionLoreProvider<?>> LORE_PROVIDERS = new HashMap<>();

    /**
     * Allows external modules to register custom actions and how they render lore.
     *
     * @param key          The string key used in config files (e.g., "teleport")
     * @param actionClass  The class of the Action implementation
     * @param factory      The factory that parses the config and builds the Action
     * @param loreProvider (Optional) The provider that generates lore for this action
     */
    public static <T extends Action> void registerAction(String key, Class<T> actionClass, ActionFactory factory, ActionLoreProvider<T> loreProvider) {
        ACTION_FACTORIES.put(key, factory);
        if (loreProvider != null) {
            LORE_PROVIDERS.put(actionClass, loreProvider);
        }
        consoleMessage(DebugLevel.DEBUG, "Registered action: " + key + " with class " + actionClass.getSimpleName());
    }

    @Override
    public void load() {
        EFFECTS = new HashMap<>();
        registerDefaultActions();
        AdventureUtil.consoleMessage("Loaded <green>" + EFFECTS.size() + " <gray>buff categories");
    }

    @Override
    public void unload() {
        if (EFFECTS != null) EFFECTS.clear();
        ACTION_FACTORIES.clear();
        LORE_PROVIDERS.clear();
    }

    private void registerDefaultActions() {
        registerAction("hunger", HungerEffectImpl.class,
                (sec, key, nick, perfect) -> new HungerEffectImpl(sec.getInt(key)),
                action -> List.of(getComponentFromMiniMessage(ConfigManager.hungerLore.replace("{hunger}", String.valueOf(action.hunger()))))
        );

        registerAction("saturation", SaturationEffectImpl.class,
                (sec, key, nick, perfect) -> new SaturationEffectImpl(sec.getInt(key)),
                action -> List.of(getComponentFromMiniMessage(ConfigManager.saturationLore.replace("{saturation}", String.valueOf(action.saturation()))))
        );

        registerAction("message", MessageActionImpl.class,
                (sec, key, nick, perfect) -> new MessageActionImpl(sec.getStringList(key).toArray(new String[0]), nick),
                null
        );

        registerAction("command", CommandActionImpl.class,
                (sec, key, nick, perfect) -> new CommandActionImpl(sec.getStringList(key).toArray(new String[0]), nick),
                null
        );

        registerAction("exp", VanillaXPImpl.class,
                (sec, key, nick, perfect) -> new VanillaXPImpl(sec.getInt(key), false), null);
        registerAction("mending", VanillaXPImpl.class,
                (sec, key, nick, perfect) -> new VanillaXPImpl(sec.getInt(key), true), null);

        registerAction("sound", SoundActionImpl.class,
                (sec, key, nick, perfect) -> new SoundActionImpl(
                        sec.getString(key + ".source"),
                        sec.getString(key + ".key"),
                        (float) sec.getDouble(key + ".volume"),
                        (float) sec.getDouble(key + ".pitch")
                ), null);

        registerAction("potion-effect", PotionEffectImpl.class,
                (sec, actionKey, nick, perfect) -> {
                    List<PotionEffect> potionEffectList = new ArrayList<>();
                    for (String key : sec.getConfigurationSection(actionKey).getKeys(false)) {
                        String typeStr = sec.getString(actionKey + "." + key + ".type", "BLINDNESS").toUpperCase();
                        PotionEffectType type = PotionEffectType.getByName(typeStr);
                        if (type == null) {
                            AdventureUtil.consoleMessage("<red>Potion effect " + typeStr + " doesn't exist");
                            type = PotionEffectType.LUCK;
                        }
                        potionEffectList.add(new PotionEffect(
                                type,
                                sec.getInt(actionKey + "." + key + ".duration"),
                                sec.getInt(actionKey + "." + key + ".amplifier")
                        ));
                    }
                    return new PotionEffectImpl(potionEffectList.toArray(new PotionEffect[0]));
                },
                action -> {
                    List<Component> actionLore = new ArrayList<>();
                    for (PotionEffect potionEffect : action.potionEffects()) {
                        actionLore.add(getComponentFromMiniMessage(ConfigManager.effectLore
                                .replace("{effect}", GuiUtil.formatString(potionEffect.getType().getName()))
                                .replace("{amplifier}", ItemUtil.amplifierToRoman(potionEffect.getAmplifier() + 1))
                                .replace("{duration}", ItemUtil.getDuration(potionEffect.getDuration() / 20))));
                    }
                    actionLore.add(Component.text(" "));
                    return actionLore;
                }
        );
    }

    public static Action[] getActions(ConfigurationSection section, String nick, boolean perfect) {
        if (section == null) return null;
        List<Action> actions = new ArrayList<>();

        for (String actionKey : section.getKeys(false)) {
            ActionFactory factory = ACTION_FACTORIES.get(actionKey);
            if (factory != null) {
                Action action = factory.create(section, actionKey, nick, perfect);
                if (action != null) {
                    actions.add(action);
                }
            } else {
                AdventureUtil.consoleMessage(DebugLevel.WARNING, "Unregistered action type found in config: " + actionKey);
            }
        }
        return actions.toArray(new Action[0]);
    }

    @SuppressWarnings("unchecked")
    public static List<List<Component>> buildActionsLore(List<Action[]> actions) {
        if (actions == null || actions.isEmpty()) {
            AdventureUtil.consoleMessage(DebugLevel.WARNING, " No actions provided for lore generation.");
            return List.of(List.of(Component.text("No actions available.")));
        }

        List<List<Component>> lore = new ArrayList<>();
        for (Action[] actionArray : actions) {
            if (actionArray == null || actionArray.length == 0) continue;

            List<Component> actionLore = new ArrayList<>();
            for (Action action : actionArray) {
                ActionLoreProvider<Action> provider = (ActionLoreProvider<Action>) LORE_PROVIDERS.get(action.getClass());
                if (provider != null) {
                    actionLore.addAll(provider.generateLore(action));
                }
            }
            if (!actionLore.isEmpty()) {
                lore.add(actionLore);
            }
        }
        return lore;
    }

    public static void loadEffects(String configPath) {
        YamlConfiguration config = ConfigManager.getConfig(configPath + ".yml");
        for (String sectionName : config.getKeys(false)) {
            ConfigurationSection section = config.getConfigurationSection(sectionName);
            List<PotionEffect> effectsList = new ArrayList<>();
            List<PotionEffect> perfectEffectsList = new ArrayList<>();

            for (String levelKey : section.getKeys(false)) {
                ConfigurationSection levelSection = section.getConfigurationSection(levelKey);
                if (levelSection == null) continue;

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
            EFFECTS.put(sectionName + "_perfect", perfectEffectsList);
        }
    }

}