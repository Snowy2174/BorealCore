package plugin.customcooking.manager;

import net.kyori.adventure.sound.Sound;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;
import plugin.customcooking.configs.ConfigManager;
import plugin.customcooking.configs.LayoutManager;
import plugin.customcooking.configs.MasteryManager;
import plugin.customcooking.CustomCooking;
import plugin.customcooking.configs.RecipeManager;
import plugin.customcooking.listener.InteractListener;
import plugin.customcooking.minigame.*;
import plugin.customcooking.util.AdventureUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import static net.kyori.adventure.key.Key.key;
import static plugin.customcooking.configs.RecipeManager.*;
import static plugin.customcooking.util.AdventureUtil.playerSound;
import static plugin.customcooking.util.InventoryUtil.*;

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

    public void handleCooking(String recipe, Player player, boolean auto) {
            if (isPlayerCooking(player)) {
                AdventureUtil.playerMessage(player, "<grey>[<bold><red>!</bold><grey>] <red>You're already cooking something.");
            } else {
                // get the bar config
                Product bar = RecipeManager.RECIPES.get(recipe);
                // checks if player has required ingredients
                List<String> ingredients = RecipeManager.itemIngredients.get(recipe);
                if (playerHasIngredients(player.getInventory(), ingredients)) {
                    removeIngredients(player.getInventory(), ingredients);
                    playerSound(player, Sound.Source.AMBIENT, key("customcooking", "cooking.ingredient"), 1f, 1f);
                    if (auto) {
                        giveItem(player, String.valueOf(successItems.get(recipe)));
                        playerSound(player, Sound.Source.AMBIENT, key("customcooking", "done"), 1f, 1f);
                        AdventureUtil.playerMessage(player, "<gray>[<green><bold>!</bold><gray>] <green>You have auto-cooked one " + RECIPES.get(recipe).getNick());
                    } else {
                        onCookedItem(player, bar);
                    }
                } else {
                    AdventureUtil.playerMessage(player, "<grey>[<bold><red>!</bold><grey>] <red>You do not have the required ingredients to cook this item.</red>");
                }
            }
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
            playerSound(player, Sound.Source.AMBIENT, key("customcooking", "cooking.fail"), 1f, 1f);
            fail(player);
            return;
        }
        if (!(loot instanceof DroppedItem droppedItem)) {
            return;
        }
        if (Math.random() < 0.3) {
            if (!player.hasPermission("customcooking.mastery." + droppedItem.getKey())) {
                MasteryManager.handleMastery(player, droppedItem.getKey());
            }
            playerSound(player, Sound.Source.AMBIENT, key("customcooking", "cooking.done"), 1f, 1f);
            String drop = perfectItems.get(droppedItem.getKey());
            giveItem(player, drop);
            sendSuccessTitle(player, ("Perfect " + droppedItem.getNick()));
        } else {
            playerSound(player, Sound.Source.AMBIENT, key("customcooking", "cooking.done"), 1f, 1f);
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
        playerSound(player, Sound.Source.AMBIENT, key("customcooking", "fail"), 1f, 1f);
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

    private boolean isPlayerCooking(Player player) {
        CookingPlayer cookingPlayer = getCookingPlayer(player);
        if (cookingPlayer!= null) {
            return true;
        }
        return false;
    }


    public void playSoundLoop(Player player) {
        soundTask = new BukkitRunnable() {
            @Override
            public void run() {
                playerSound(player, Sound.Source.AMBIENT, key("customcooking", "cooking"), 1f, 1f);
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