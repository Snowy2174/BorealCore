package plugin.customcooking.configs;

import org.bukkit.configuration.file.YamlConfiguration;
import plugin.customcooking.util.ConfigUtil;

import java.util.List;

public class ConfigManager {
    public static String lang;
    public static Integer perfectChance;
    public static int splashTime;
    public static List<String> starterRecipes;
    public static String customNamespace;
    public static String failureItem;
    public static String unlitCookingPot;
    public static String litCookingPot;
    public static String splashEffect;
    public static String recipeBookTextureNamespace;
    public static String masteryLine;
    public static String masteryBar;
    public static String masteryInfoTrue;
    public static String masteryInfoFalse;
    public static String ingredientsLine;
    public static String cookLine;
    public static String cookLineRight;
    public static String cookLineLeft;
    public static String unknownItem;
    public static String grinderItem;
    public static String[] successTitle;
    public static String[] successSubTitle;
    public static int successFadeIn;
    public static int successFadeStay;
    public static int successFadeOut;
    public static String[] failureTitle;
    public static String[] failureSubTitle;
    public static int failureFadeIn;
    public static int failureFadeStay;
    public static int failureFadeOut;

    public static void load() {
        YamlConfiguration config = ConfigUtil.getConfig("config.yml");

        lang = config.getString("lang", "english");

        perfectChance = config.getInt("mechanics.perfect-chance", 30) / 100;
        splashTime = config.getInt("mechanics.splash-time", 100);

        starterRecipes = config.getStringList("mechanics.starter-recipes");
        customNamespace = config.getString("mechanics.namespace", "customcooking");
        failureItem = config.getString("mechanics.failure-item", "failureitem");
        unlitCookingPot = config.getString("mechanics.unlit-cooking-pot", "cooking_pot_unlit");
        litCookingPot = config.getString("mechanics.lit-cooking-pot", "cooking_pot_lit");
        splashEffect = config.getString("mechanics.pot-effect", "pot_effect");

        recipeBookTextureNamespace = config.getString("gui.config.recipe-book", "customcooking:recipe_book");
        masteryLine = config.getString("gui.config.mastery-line", "<!italic><#ff9900>Mastery [{mastery}]");
        masteryBar = config.getString("gui.config.mastery-bar", "<!italic><#ffcc33>[{bar}<#ffcc33>]");
        masteryInfoTrue = config.getString("gui.config.mastery-true", "<!italic><#ffcc99>This item has been mastered /<!italic><#ffcc99>and will be cooked automatically.");
        masteryInfoFalse = config.getString("gui.config.mastery-false", "<!italic><#ffcc99>This dish has not been mastered /<!italic><#ffcc99>and will have to be manually cooked.");
        ingredientsLine = config.getString("gui.config.ingredients-line", "<!italic><#ffcc33>Ingredients:");
        cookLine = config.getString("gui.config.info-cook", "<!italic><#ffcc33>[Click] <#ffcc99>to Cook");
        cookLineRight = config.getString("gui.config.info-right-cook", "<!italic><#ffcc33>[Right Click] <#ffcc99>to Cook");
        cookLineLeft = config.getString("gui.config.info-left-cook", "<!italic><#ffcc33>[Left Click] <#ffcc99>to Autocook");
        unknownItem = config.getString("gui.items.unknown-item", "unknownrecipe");
        grinderItem = config.getString("gui.items.grinder-item", "grinder");


        successTitle = config.getStringList("titles.success.title").toArray(new String[0]);
        successSubTitle = config.getStringList("titles.success.subtitle").toArray(new String[0]);
        successFadeIn = config.getInt("titles.success.fade.in", 10) * 50;
        successFadeStay = config.getInt("titles.success.fade.stay", 30) * 50;
        successFadeOut = config.getInt("titles.success.fade.out", 10) * 50;

        failureTitle = config.getStringList("titles.failure.title").toArray(new String[0]);
        failureSubTitle = config.getStringList("titles.failure.subtitle").toArray(new String[0]);
        failureFadeIn = config.getInt("titles.failure.fade.in", 10) * 50;
        failureFadeStay = config.getInt("titles.failure.fade.stay", 30) * 50;
        failureFadeOut = config.getInt("titles.failure.fade.out", 10) * 50;

        if (successTitle.length == 0) successTitle = new String[]{""};
        if (successSubTitle.length == 0) successSubTitle = new String[]{""};
        if (failureTitle.length == 0) failureTitle = new String[]{""};
        if (failureSubTitle.length == 0) failureSubTitle = new String[]{""};


    }
}
