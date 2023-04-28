package plugin.customcooking.Configs;

import org.bukkit.configuration.file.YamlConfiguration;
import plugin.customcooking.Util.ConfigUtil;

public class ConfigManager {
    public static String lang;
    public static boolean otherProductBar;
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
    public static boolean vaultHook;
    public static boolean instantBar;

    public static void load() {
        YamlConfiguration config = ConfigUtil.getConfig("config.yml");

        lang = config.getString("lang", "english");

        instantBar = config.getBoolean("mechanics.instant-bar", false);
        otherProductBar = config.getBoolean("mechanics.other-recipes.cooking-bar", true);

        vaultHook = config.getBoolean("integration.Vault", true);

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
