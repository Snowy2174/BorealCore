package plugin.borealcore.functions.herbalism;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;
import plugin.borealcore.BorealCore;
import plugin.borealcore.functions.cooking.CookingPlayer;
import plugin.borealcore.functions.cooking.Difficulty;
import plugin.borealcore.functions.cooking.configs.LayoutManager;
import plugin.borealcore.functions.cooking.object.Layout;
import plugin.borealcore.functions.herbalism.objects.Herb;
import plugin.borealcore.functions.herbalism.objects.Infusion;
import plugin.borealcore.manager.configs.ConfigManager;
import plugin.borealcore.manager.configs.MessageManager;
import plugin.borealcore.object.Function;
import plugin.borealcore.utility.AdventureUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static net.kyori.adventure.key.Key.key;

public class HerbalismManager extends Function {

    private final Random random;
    private final HashMap<Player, Infusion> currentInfusions;
    private final HashMap<Player, Location> teaPotLocations;
    public final ConcurrentHashMap<Player, InfusingPlayer> infusingPlayerCache;
    private Map<UUID, BukkitRunnable> playerSoundTasks = new HashMap<>();

    public HerbalismManager() {
        this.random = new Random();
        this.currentInfusions = new HashMap<>();
        this.teaPotLocations = new HashMap<>();
        this.infusingPlayerCache = new ConcurrentHashMap<>();
        load();
    }

    @Override
    public void load() {
    }

    @Override
    public void unload() {
    }

    public void handleInfuse() {
        // @TODO generate an infusion object
        // @TODO modify the infusion object with the final model data
    }

    public void modifyInfusion() {
        // @TODO Compound all herb effects individually into one array
        // @TODO for each modifier apply effects
        // @TODO generate lore from both final effect list and ingredient list
        // @TODO generate and set itemstack, colour (from mean of herb colours) and ....
        // @TODO apply quality modifier (perfect/normal/fail) for each bar event and update array
        // @TODO FIGURE OUT HOW TO DYNAMICALLY RECALL EFFECTS FROM NBT, action serialisation or regeneration of infusion from uuid?
    }

    private void onStartInfusion(Player player, Infusion recipe) {

        player.closeInventory();
        currentInfusions.put(player, recipe);

        //@TODO furntiture implementation for teapot
        //if (clickedFurniture != null) {
        //    cookingPotLocations.put(player, clickedFurniture.getArmorstand().getLocation());
        //}

        InfusingPlayer infusingPlayer = infusingPlayerCache.remove(player);
        if (infusingPlayer == null) {
            AdventureUtil.playerMessage(player, MessageManager.pluginError + ": <gray>There ain't no custom recipe");
        } else {
            if (recipe == null) {
            } else {
                //@TODO init first step of infusion, by sending bar of first ingredient
                showPlayerBar(player, recipe.getIngredients()[0]);
            }
        }
    }

    public void onBarInteract(PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        InfusingPlayer infusingPlayer = infusingPlayerCache.remove(player);
        if (infusingPlayer != null) {
            proceedBarInteract(player, infusingPlayer);
        }
    }

    public void proceedBarInteract(Player player, InfusingPlayer infusingPlayer) {
        //@TODO get number of ingredient failed/sucseeded, find a way to mark the infusion progress in currentInfusions, do SFX if furnture present
        //@TODO get next ingredient, send bar for next ingredient
        //@TODO on last ingredient, get infusion and build item ?
    }

    private void handleFailureResult(Player player) {
    }

    private Infusion recalculateQuality(Infusion infusion, int stage) {
        //@TODO move to Infusion class?
        //@TODO I'm thinking, prev + 0-2 * 10 / ingredient count = new qual ()
        //@TODO if quality less than 2 fail drink, 2-8 normal, 9-10 configured length amplifier and +1 to effect levels
        return null;
    }

    private Infusion applyQualityMultiplier(Infusion infusion) {
        //@TODO for each potion effect impl, 1.1* time + 1 to level, if effect in defined list (ie not for saturation etc)
        return null;
    }


    private void sendIngredientSuccessTitle(Player player, String recipe) {
        AdventureUtil.playerTitle(
                player,
                ConfigManager.successTitle[random.nextInt(ConfigManager.successTitle.length)]
                        .replace("{recipe}", recipe)
                        .replace("{player}", player.getName()),
                ConfigManager.successSubTitle[random.nextInt(ConfigManager.successSubTitle.length)]
                        .replace("{recipe}", recipe)
                        .replace("{player}", player.getName()),
                ConfigManager.successFadeIn,
                ConfigManager.successFadeStay,
                ConfigManager.successFadeOut
        );
    }

    private void showPlayerBar(Player player, @Nullable Herb ingredient) {
        Layout layout;
        if (ingredient != null && ingredient.getLayout() != null) {
            layout = ingredient.getLayout()[random.nextInt(ingredient.getLayout().length)];
        } else {
            layout = (Layout) LayoutManager.LAYOUTS.values().toArray()[random.nextInt(LayoutManager.LAYOUTS.values().size())];
        }
        int speed;
        int timer;
        int time;
        if (ingredient != null) {
            Difficulty difficulty = ingredient.getDifficulty()[random.nextInt(ingredient.getDifficulty().length)];
            speed = difficulty.speed();
            timer = difficulty.timer();
            time = ingredient.getTime();
        } else {
            speed = random.nextInt(5);
            time = 10000;
            timer = 1;
        }
        if (speed < 1) {
            speed = 1;
        }
        Difficulty difficult = new Difficulty(timer, speed);

        //@TODO make an overall Infusing player method, and dynamically add deadlines based on each ingredient
        InfusingPlayer infusingPlayer = new InfusingPlayer(System.currentTimeMillis() + time, player, layout, difficult, this);
        infusingPlayer.runTaskTimer(BorealCore.plugin, 0, 1);
        //@TODO sort cache
        infusingPlayerCache.put(player, infusingPlayer);
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, time / 50, 3));
    }

    @Override
    public void onQuit(Player player) {
        currentInfusions.remove(player);
    }

    @Nullable
    public InfusingPlayer getInfusingPlayer(Player player) {
        return infusingPlayerCache.get(player);
    }

    public void removeInfusingPlayer(Player player) {
        infusingPlayerCache.remove(player);
    }

    private boolean isPlayerInfusing(Player player) {
        InfusingPlayer infusingPlayer = getInfusingPlayer(player);
        return infusingPlayer != null;
    }

}
