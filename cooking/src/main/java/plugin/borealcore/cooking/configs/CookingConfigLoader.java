package plugin.borealcore.cooking.configs;

import org.bukkit.configuration.ConfigurationSection;
import plugin.borealcore.manager.ConfigManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Loads cooking-specific configuration from config.yml.
 * Called by CookingModule during module initialization.
 * <p>
 * Uses ModuleContext pattern: All defaults are applied automatically,
 * and configuration is read from the main config file or dedicated section.
 */
public class CookingConfigLoader {

    public static void load() {
        Map<String, Object> defaults = new HashMap<>();

        // Mechanics configurations
        defaults.put("mechanics.perfect-chance", 0.35);
        defaults.put("mechanics.ingredients-refund-chance", 0.1);
        defaults.put("mechanics.splash-time", 100);
        defaults.put("mechanics.mastery-reward", 5);
        defaults.put("mechanics.unknown-item-suffix", "_unknown");
        defaults.put("mechanics.perfect-item-suffix", "_perfect");
        defaults.put("mechanics.particle-item-suffix", "_particle");
        defaults.put("mechanics.failure-item", "failureitem");
        defaults.put("mechanics.unlit-cooking-pot", "cooking_pot_unlit");
        defaults.put("mechanics.lit-cooking-pot", "cooking_pot_lit");
        defaults.put("mechanics.pot-effect", "pot_effect");

        // GUI configurations
        defaults.put("gui.config.recipe-book", "borealcore:recipe_book");
        defaults.put("gui.config.mastery-line", "<!italic><#ff9900>Mastery [{mastery}]");
        defaults.put("gui.config.mastery-bar", "<!italic><#ffcc33>[{bar}<#ffcc33>]");
        defaults.put("gui.config.mastery-true", "<!italic><#ffcc99>This item has been mastered /<!italic><#ffcc99>and will be cooked automatically.");
        defaults.put("gui.config.mastery-false", "<!italic><#ffcc99>This dish has not been mastered /<!italic><#ffcc99>and will have to be manually cooked.");
        defaults.put("gui.config.ingredients-line", "<!italic><#ffcc33>Ingredients:");
        defaults.put("gui.config.info-cook", "<!italic><#ffcc33>[Click] <#ffcc99>to Cook");
        defaults.put("gui.config.info-right-cook", "<!italic><#ffcc33>[Right Click] <#ffcc99>to Cook");
        defaults.put("gui.config.info-left-cook", "<!italic><#ffcc33>[Left Click] <#ffcc99>to Autocook");
        defaults.put("gui.config.info-shift-cook", "<!italic><#ffcc33>[Middle Click] <#ffcc99>to Autocook x16");
        defaults.put("gui.items.unknown-item", "unknownrecipe");
        defaults.put("gui.items.grinder-item", "grinder");

        // Starter recipes
        defaults.put("mechanics.starter-recipes.0", "custardpie");
        defaults.put("mechanics.starter-recipes.1", "tomatosoup");
        defaults.put("mechanics.starter-recipes.2", "carrotsoup");
        defaults.put("mechanics.starter-recipes.3", "seedsoup");
        defaults.put("mechanics.starter-recipes.4", "pumpkinsoup");
        defaults.put("mechanics.starter-recipes.5", "cactussoup");
        defaults.put("mechanics.starter-recipes.6", "potatosoup");

        // Setup defaults using ConfigManager utility (config file is main config.yml)
        ConfigurationSection config = ConfigManager.setupModuleDefaults("config.yml", defaults);

        CookingConfig.perfectChance = config.getDouble("mechanics.perfect-chance", 0.35);
        CookingConfig.ingredientRefundChance = config.getDouble("mechanics.ingredients-refund-chance", 0.1);
        CookingConfig.splashTime = config.getInt("mechanics.splash-time", 100);
        CookingConfig.masteryJadeReward = config.getInt("mechanics.mastery-reward", 5);

        CookingConfig.starterRecipes = config.getStringList("mechanics.starter-recipes");
        CookingConfig.unknownItemSuffix = config.getString("mechanics.unknown-item-suffix", "_unknown");
        CookingConfig.perfectItemSuffix = config.getString("mechanics.perfect-item-suffix", "_perfect");
        CookingConfig.particleItemSuffix = config.getString("mechanics.particle-item-suffix", "_particle");

        ConfigManager.effectLore = config.getString("mechanics.effect-lore", " <!italic><gold>\uD83E\uDDEA <white>{effect} <gold>{amplifier} {duration}");
        ConfigManager.hungerLore = config.getString("mechanics.hunger-lore", " <!italic><gold>\uD83C\uDF56 Restores {hunger} hunger");
        ConfigManager.saturationLore = config.getString("mechanics.saturation-lore", " <!italic><gold>\uD83C\uDF56 Restores {saturation} saturation");
        CookingConfig.failureItem = config.getString("mechanics.failure-item", "failureitem");
        CookingConfig.unlitCookingPot = config.getString("mechanics.unlit-cooking-pot", "cooking_pot_unlit");
        CookingConfig.litCookingPot = config.getString("mechanics.lit-cooking-pot", "cooking_pot_lit");

        CookingConfig.splashEffect = config.getString("mechanics.pot-effect", "pot_effect");

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

        // Title/Subtitle configurations
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
    }
}


