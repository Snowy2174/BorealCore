package plugin.borealcore.manager.configs;

import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import plugin.borealcore.BorealCore;
import plugin.borealcore.functions.cooking.CookingConfig;
import plugin.borealcore.functions.jade.JadeConfig;

import java.io.File;

public class ConfigManager {
    public static String lang;
    public static DebugLevel debugLevel;
    public static boolean processAnalyticsEnabled;
    public static String customNamespace;

    public static void load() {
        YamlConfiguration config = getConfig("config.yml");

        lang = config.getString("lang", "english");
        customNamespace = config.getString("mechanics.namespace", "borealcore");
        debugLevel = DebugLevel.valueOf(config.getString("debug-level", "INFO").toUpperCase());
        processAnalyticsEnabled = config.getBoolean("analytics.process.enabled", true);

        CookingConfig.perfectChance = config.getDouble("mechanics.perfect-chance", 0.35);
        CookingConfig.ingredientRefundChance = config.getDouble("mechanics.ingredients-refund-chance", 0.1);

        CookingConfig.starterRecipes = config.getStringList("mechanics.starter-recipes");
        CookingConfig.unknownItemSuffix = config.getString("mechanics.unknown-item-suffix", "_unknown");
        CookingConfig.perfectItemSuffix = config.getString("mechanics.perfect-item-suffix", "_perfect");
        CookingConfig.particleItemSuffix = config.getString("mechanics.particle-item-suffix", "_particle");

        CookingConfig.effectLore = config.getString("mechanics.effect-lore", " <!italic><gold>\uD83E\uDDEA <white>{effect} <gold>{amplifier} {duration}");
        CookingConfig.hungerLore = config.getString("mechanics.hunger-lore", " <!italic><gold>\uD83C\uDF56 Restores {hunger} hunger");
        CookingConfig.saturationLore = config.getString("mechanics.saturation-lore", " <!italic><gold>\uD83C\uDF56 Restores {saturation} saturation");
        CookingConfig.failureItem = config.getString("mechanics.failure-item", "failureitem");
        CookingConfig.unlitCookingPot = config.getString("mechanics.unlit-cooking-pot", "cooking_pot_unlit");
        CookingConfig.litCookingPot = config.getString("mechanics.lit-cooking-pot", "cooking_pot_lit");

        CookingConfig.splashEffect = config.getString("mechanics.pot-effect", "pot_effect");
        CookingConfig.splashTime = config.getInt("mechanics.splash-time", 100);
        CookingConfig.masteryJadeReward = config.getInt("mechanics.mastery-reward", 5);

        CookingConfig.recipeBookTextureNamespace = config.getString("gui.config.recipe-book", "borealcore:recipe_book");
        CookingConfig.masteryLine = config.getString("gui.config.mastery-line", "<!italic><#ff9900>Mastery [{mastery}]");
        CookingConfig.masteryBar = config.getString("gui.config.mastery-bar", "<!italic><#ffcc33>[{bar}<#ffcc33>]");
        CookingConfig.masteryInfoTrue = config.getString("gui.config.mastery-true", "<!italic><#ffcc99>This item has been mastered /<!italic><#ffcc99>and will be cooked automatically.");
        CookingConfig.masteryInfoFalse = config.getString("gui.config.mastery-false", "<!italic><#ffcc99>This dish has not been mastered /<!italic><#ffcc99>and will have to be manually cooked.");
        CookingConfig.ingredientsLine = config.getString("gui.config.ingredients-line", "<!italic><#ffcc33>Ingredients:");
        CookingConfig.cookLine = config.getString("gui.config.info-cook", "<!italic><#ffcc33>[Click] <#ffcc99>to Cook");
        CookingConfig.cookLineRight = config.getString("gui.config.info-right-cook", "<!italic><#ffcc33>[Right Click] <#ffcc99>to Cook");
        CookingConfig.cookLineLeft = config.getString("gui.config.info-left-cook", "<!italic><#ffcc33>[Left Click] <#ffcc99>to Autocook");
        CookingConfig.cookLineShift = config.getString("gui.config.info-shift-cook", "<!italic><#ffcc33>[Middle Click] <#ffcc99>to Autocook x16");
        CookingConfig.unknownItem = config.getString("gui.items.unknown-item", "unknownrecipe");
        CookingConfig.grinderItem = config.getString("gui.items.grinder-item", "grinder");

        CookingConfig.successTitle = config.getStringList("titles.success.title").toArray(new String[0]);
        CookingConfig.successSubTitle = config.getStringList("titles.success.subtitle").toArray(new String[0]);
        CookingConfig.successFadeIn = config.getInt("titles.success.fade.in", 10) * 50;
        CookingConfig.successFadeStay = config.getInt("titles.success.fade.stay", 30) * 50;
        CookingConfig.successFadeOut = config.getInt("titles.success.fade.out", 10) * 50;

        CookingConfig.failureTitle = config.getStringList("titles.failure.title").toArray(new String[0]);
        CookingConfig.failureSubTitle = config.getStringList("titles.failure.subtitle").toArray(new String[0]);
        CookingConfig.failureFadeIn = config.getInt("titles.failure.fade.in", 10) * 50;
        CookingConfig.failureFadeStay = config.getInt("titles.failure.fade.stay", 30) * 50;
        CookingConfig.failureFadeOut = config.getInt("titles.failure.fade.out", 10) * 50;

        if (CookingConfig.successTitle.length == 0) CookingConfig.successTitle = new String[]{""};
        if (CookingConfig.successSubTitle.length == 0) CookingConfig.successSubTitle = new String[]{""};
        if (CookingConfig.failureTitle.length == 0) CookingConfig.failureTitle = new String[]{""};
        if (CookingConfig.failureSubTitle.length == 0) CookingConfig.failureSubTitle = new String[]{""};

        JadeConfig.brewingRequiredQuality = config.getInt("mechanics.brewing-required-quality", 8);
        JadeConfig.refarmableCrops = config.getStringList("mechanics.refarmable-crops");


    }

    public static @NotNull NamespacedKey getNamespacedKey(String key) {
        return new NamespacedKey(BorealCore.plugin, key);
    }

    public static void setDebugLevel(DebugLevel debugLevel) {
        YamlConfiguration config = getConfig("config.yml");
        config.set("debug-level", debugLevel.toString());
        saveConfig(config, "config.yml");
    }

    public static YamlConfiguration getConfig(String configName) {
        File file = new File(BorealCore.plugin.getDataFolder(), configName);
        if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
        if (!file.exists()) {
            try {
                BorealCore.plugin.saveResource(configName.substring(configName.lastIndexOf("/") + 1), false);
            } catch (IllegalArgumentException e) {
                try { file.createNewFile(); } catch (Exception ignored) {}
            }
        }
        return YamlConfiguration.loadConfiguration(file);
    }

    /**
     * Saves a YamlConfiguration to disk
     */
    public static void saveConfig(YamlConfiguration config, String configName) {
        File file = new File(BorealCore.plugin.getDataFolder(), configName);
        try {
            config.save(file);
        } catch (Exception e) {
            BorealCore.plugin.getLogger().severe("Failed to save config: " + configName);
        }
    }
}
