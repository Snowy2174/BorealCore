package plugin.customcooking.Manager;

import net.kyori.adventure.sound.Sound;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import plugin.customcooking.Configs.ConfigManager;
import plugin.customcooking.Configs.LayoutManager;
import plugin.customcooking.Configs.MasteryManager;
import plugin.customcooking.CustomCooking;
import plugin.customcooking.Listener.InteractListener;
import plugin.customcooking.Minigame.*;
import plugin.customcooking.Util.AdventureUtil;

import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import static net.kyori.adventure.key.Key.key;
import static plugin.customcooking.Configs.RecipeManager.perfectItems;
import static plugin.customcooking.Configs.RecipeManager.successItems;
import static plugin.customcooking.Util.AdventureUtil.playerSound;
import static plugin.customcooking.Util.InventoryUtil.giveItem;

public class CookingManager extends Function {

    private final InteractListener interactListener;
    private final HashMap<Player, Product> cookedRecipe;
    public final ConcurrentHashMap<Player, CookingPlayer> cookingPlayerCache;
    private BukkitRunnable soundTask;

    public CookingManager() {
        this.interactListener = new InteractListener(this);
        this.cookedRecipe = new HashMap<>();
        this.cookingPlayerCache = new ConcurrentHashMap<>();
        load();
    }

    @Override
    public void load() {
        Bukkit.getPluginManager().registerEvents(this.interactListener, CustomCooking.plugin);
    }

    @Override
    public void unload() {
        HandlerList.unregisterAll(this.interactListener);
    }

    public void onCookedItem(Player player, Product recipe) {

        player.closeInventory();
        cookedRecipe.put(player, recipe);
        CookingPlayer cookingPlayer = cookingPlayerCache.remove(player);
        if (cookingPlayer == null) {
            if (recipe == Product.EMPTY) {
            }
            else {
                // No custom recipe
                if (recipe == null) {
                    AdventureUtil.playerMessage(player, "There ain't no custom recipe");
                } else {
                    showPlayerBar(player, recipe);
                }
            }
        }
    }

    public void onBarInteract(PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        CookingPlayer cookingPlayer = cookingPlayerCache.remove(player);
        if (cookingPlayer != null) {
            proceedBarInteract(player, cookingPlayer);
        }
    }

    public void proceedBarInteract(Player player, CookingPlayer cookingPlayer) {
        cookingPlayer.cancel();
        Product loot = cookedRecipe.remove(player);
        stopSoundLoop();
        player.removePotionEffect(PotionEffectType.SLOW);
        if (!cookingPlayer.isSuccess()) {
            playerSound(player, Sound.Source.AMBIENT, key("customfoods", "cooking.fail"), 1f, 1f);
            fail(player);
            return;
        }
        if (!(loot instanceof DroppedItem droppedItem)) {
            return;
        }
        if (Math.random() < 0.5) {
            if (player.hasPermission("customcooking." + droppedItem.getKey() + ".mastery")) {
                MasteryManager.handleMastery(player, droppedItem.getKey());
            }
            playerSound(player, Sound.Source.AMBIENT, key("customfoods", "cooking.done"), 1f, 1f);
            String drop = perfectItems.get(droppedItem.getKey());
            giveItem(player, drop);
            sendSuccessTitle(player, ("Perfect " + droppedItem.getNick()));
        } else {
            playerSound(player, Sound.Source.AMBIENT, key("customfoods", "cooking.done"), 1f, 1f);
            String drop = successItems.get(droppedItem.getKey());
            giveItem(player, drop);
            sendSuccessTitle(player, droppedItem.getNick());
        }
    }

    private void sendSuccessTitle(Player player, String recipe) {
        AdventureUtil.playerTitle(
                player,
                ConfigManager.successTitle[new Random().nextInt(ConfigManager.successTitle.length)]
                        .replace("{recipe}", recipe)
                        .replace("{player}", player.getName()),
                ConfigManager.successSubTitle[new Random().nextInt(ConfigManager.successSubTitle.length)]
                        .replace("{recipe}", recipe)
                        .replace("{player}", player.getName()),
                ConfigManager.successFadeIn,
                ConfigManager.successFadeStay,
                ConfigManager.successFadeOut
        );
    }

    private void fail(Player player) {
        playerSound(player, Sound.Source.AMBIENT, key("customfoods", "fail"), 1f, 1f);
        AdventureUtil.playerTitle(
                player,
                ConfigManager.failureTitle[new Random().nextInt(ConfigManager.failureTitle.length)],
                ConfigManager.failureSubTitle[new Random().nextInt(ConfigManager.failureSubTitle.length)],
                ConfigManager.failureFadeIn,
                ConfigManager.failureFadeStay,
                ConfigManager.failureFadeOut
        );
        giveItem(player, "failureitem");
    }

    private void showPlayerBar(Player player, @Nullable Product recipe) {
        Layout layout;
        if (recipe != null && recipe.getLayout() != null) {
            layout = recipe.getLayout()[new Random().nextInt(recipe.getLayout().length)];
        } else {
            layout = (Layout) LayoutManager.LAYOUTS.values().toArray()[new Random().nextInt(LayoutManager.LAYOUTS.values().size())];
        }
        int speed;
        int timer;
        int time;
        if (recipe != null) {
            Difficulty difficulty = recipe.getDifficulty()[new Random().nextInt(recipe.getDifficulty().length)];
            speed = difficulty.speed();
            timer = difficulty.timer();
            time = recipe.getTime();
        } else {
            speed = new Random().nextInt(5);
            time = 10000;
            timer = 1;
        }
        if (speed < 1) {
            speed = 1;
        }
        Difficulty difficult = new Difficulty(timer, speed);

        CookingPlayer cookingPlayer = new CookingPlayer(System.currentTimeMillis() + time, player, layout, difficult, this);
        cookingPlayer.runTaskTimerAsynchronously(CustomCooking.plugin, 0, 1);
        cookingPlayerCache.put(player, cookingPlayer);
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, time / 50, 3));
        playSoundLoop(player);
    }

    @Override
    public void onQuit(Player player) {
        cookedRecipe.remove(player);
    }

    @Nullable
    public CookingPlayer getCookingPlayer(Player player) {
        return cookingPlayerCache.get(player);
    }

    public void removeCookingPlayer(Player player) {
        cookingPlayerCache.remove(player);
    }

    public void playSoundLoop(Player player) {
        soundTask = new BukkitRunnable() {
            @Override
            public void run() {
                playerSound(player, Sound.Source.AMBIENT, key("customfoods", "cooking"), 1f, 1f);
            }
        };
        soundTask.runTaskTimer(CustomCooking.plugin, 0L, 60L); // run every 20 ticks (1 second)
    }

    public void stopSoundLoop() {
        if (soundTask != null) {
            soundTask.cancel();
        }
    }
}