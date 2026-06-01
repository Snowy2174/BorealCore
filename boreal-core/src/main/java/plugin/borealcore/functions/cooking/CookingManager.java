package plugin.borealcore.functions.cooking;


import dev.lone.itemsadder.api.CustomFurniture;
import dev.lone.itemsadder.api.Events.FurnitureBreakEvent;
import dev.lone.itemsadder.api.Events.FurnitureInteractEvent;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Rotation;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;
import plugin.borealcore.BorealCore;
import plugin.borealcore.action.Action;
import plugin.borealcore.api.event.CookResultEvent;
import plugin.borealcore.api.module.BorealModule;
import plugin.borealcore.api.module.ModuleContext;
import plugin.borealcore.functions.cooking.competition.Competition;
import plugin.borealcore.functions.cooking.competition.placeholder.CompetitionPapi;
import plugin.borealcore.functions.cooking.configs.LayoutManager;
import plugin.borealcore.functions.cooking.configs.RecipeManager;
import plugin.borealcore.functions.cooking.object.DroppedItem;
import plugin.borealcore.functions.cooking.object.Ingredient;
import plugin.borealcore.functions.cooking.object.Layout;
import plugin.borealcore.functions.cooking.object.Recipe;
import plugin.borealcore.functions.jade.JadeManager;
import plugin.borealcore.functions.traps.TrapsManager;
import plugin.borealcore.manager.GuiManager;
import plugin.borealcore.manager.configs.ConfigManager;
import plugin.borealcore.manager.configs.DebugLevel;
import plugin.borealcore.manager.configs.MessageManager;
import plugin.borealcore.object.Function;
import plugin.borealcore.object.SimpleListener;
import plugin.borealcore.utility.AdventureUtil;
import plugin.borealcore.utility.GuiUtil;
import plugin.borealcore.utility.InventoryUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static net.kyori.adventure.key.Key.key;
import static plugin.borealcore.BorealCore.getPlaceholderManager;
import static plugin.borealcore.functions.cooking.CookingConfig.perfectChance;
import static plugin.borealcore.manager.GuiManager.INGREDIENTS;
import static plugin.borealcore.utility.AdventureUtil.playerSound;

public class CookingManager extends Function implements BorealModule {
    private final Random random;
    private final HashMap<Player, Recipe> cookedRecipe;
    private final HashMap<Player, Location> cookingPotLocations;
    public final ConcurrentHashMap<Player, CookingPlayer> cookingPlayerCache;
    private final Map<UUID, BukkitRunnable> playerSoundTasks = new HashMap<>();
    private final SimpleListener simpleListener;
    private CookingPapi cookingPlaceholders;
    private CompetitionPapi competitionPlaceholders;
    private static final Map<Location, Hologram> holograms = new HashMap<>();
    private final Map<Player, Long> clickCooldowns;
    static Map<Location, BukkitTask> activeFXTasks = new HashMap<>();

    public CookingManager() {
        this.random = new Random();
        this.cookedRecipe = new HashMap<>();
        this.cookingPotLocations = new HashMap<>();
        this.cookingPlayerCache = new ConcurrentHashMap<>();
        this.simpleListener = new SimpleListener(this);
        this.clickCooldowns = new ConcurrentHashMap<>();
    }

    @Override
    public void load() {
        this.cookingPlaceholders = new CookingPapi();
        this.competitionPlaceholders = new CompetitionPapi();
        getPlaceholderManager().registerExpansion(cookingPlaceholders);
        getPlaceholderManager().registerExpansion(competitionPlaceholders);
        Bukkit.getPluginManager().registerEvents(this.simpleListener, BorealCore.plugin);
        GuiManager guiManager = BorealCore.getGuiManager();
        guiManager.registerGui("cookingRecipeBook", () -> new CookingRecipeBookGUI(null));
    }

    @Override
    public void unload() {
        if (this.simpleListener != null) HandlerList.unregisterAll(this.simpleListener);
        if (this.cookingPlaceholders != null) getPlaceholderManager().unregisterExpansion(cookingPlaceholders); //Use context.getPlaceholderManager for modular implementation
        if (this.competitionPlaceholders != null) getPlaceholderManager().unregisterExpansion(competitionPlaceholders);
        activeFXTasks.clear();
        holograms.clear();
        clickCooldowns.clear();
    }

    public void handleCooking(String recipe, Player player, CustomFurniture clickedFurniture) {
        if (isPlayerCooking(player)) {
            AdventureUtil.playerMessage(player, MessageManager.infoNegative + MessageManager.alreadyCooking);
        } else {
            Recipe bar = RecipeManager.COOKING_RECIPES.get(recipe);
            List<String> ingredients = bar.getIngredients();
            if (InventoryUtil.handleIngredientCheck(player.getInventory(), ingredients, 1)) {
                InventoryUtil.removeIngredients(player.getInventory(), ingredients, 1);
                if (clickedFurniture != null) {
                    Location loc = clickedFurniture.getArmorstand().getLocation();
                    CookingPotUtil.ingredientsSFX(player, ingredients, loc);
                }
                onCookedItem(player, bar, clickedFurniture);
            } else {
                AdventureUtil.playerMessage(player, MessageManager.infoNegative + MessageManager.noIngredients);
            }
        }
    }

    public void handleMaterialAutocooking(String recipeId, Player player, Integer amount) {
        Ingredient recipe = INGREDIENTS.get(recipeId);
        List<String> ingredients = recipe.getIngredients();
        if (InventoryUtil.handleIngredientCheck(player.getInventory(), ingredients, amount)) {
            InventoryUtil.removeIngredients(player.getInventory(), ingredients, amount);
            InventoryUtil.giveItem(player, recipe.getKey(), amount, true);
            playerSound(player, Sound.Source.AMBIENT, key(ConfigManager.customNamespace, "done"), 1f, 1f);
            AdventureUtil.playerMessage(player, MessageManager.infoPositive + MessageManager.cookingAutocooked.replace("{recipe}", recipe.getNick()) + " x" + amount);
        } else {
            AdventureUtil.playerMessage(player, MessageManager.infoNegative + MessageManager.noIngredients);
        }
    }

    public void handleAutocooking(String recipeId, Player player, Integer amount) {
        if (isPlayerCooking(player)) {
            AdventureUtil.playerMessage(player, MessageManager.infoNegative + MessageManager.alreadyCooking);
        } else {
            Recipe recipe = RecipeManager.COOKING_RECIPES.get(recipeId);
            List<String> ingredients = recipe.getIngredients();
            if (InventoryUtil.handleIngredientCheck(player.getInventory(), ingredients, amount)) {
                // Delay removal of items if furniture is not null
                InventoryUtil.removeIngredients(player.getInventory(), ingredients, amount);
                InventoryUtil.giveItem(player, String.valueOf(recipe.getCookedItems()), amount, true);
                playerSound(player, Sound.Source.AMBIENT, key(ConfigManager.customNamespace, "done"), 1f, 1f);
                AdventureUtil.playerMessage(player, MessageManager.infoPositive + MessageManager.cookingAutocooked.replace("{recipe}", recipe.getNick()) + " x" + amount);
            } else {
                AdventureUtil.playerMessage(player, MessageManager.infoNegative + MessageManager.noIngredients);
            }
        }

    }

    private void onCookedItem(Player player, Recipe recipe, CustomFurniture clickedFurniture) {
        player.closeInventory();
        cookedRecipe.put(player, recipe);
        if (clickedFurniture != null) {
            cookingPotLocations.put(player, clickedFurniture.getArmorstand().getLocation());
        }

        CookingPlayer cookingPlayer = cookingPlayerCache.remove(player);
        if (cookingPlayer == null && (recipe != Recipe.EMPTY)) {
            if (recipe == null) {
                AdventureUtil.playerMessage(player, MessageManager.pluginError + ": <gray>There ain't no custom recipe");
            } else {
                showPlayerBar(player, recipe);
            }
        }
    }

    @Override
    public void onInteract(PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        CookingPlayer cookingPlayer = cookingPlayerCache.remove(player);
        if (cookingPlayer != null) {
            proceedBarInteract(player, cookingPlayer);
        }
    }

    public void proceedBarInteract(Player player, CookingPlayer cookingPlayer) {
        cookingPlayer.cancel();
        Recipe recipe = cookedRecipe.remove(player);
        Location cookingPot = cookingPotLocations.remove(player);

        stopSoundLoop(player);

        player.removePotionEffect(PotionEffectType.SLOWNESS);

        if (!cookingPlayer.isSuccess()) {
            if (cookingPot != null) {
                CookingPotUtil.playCookingResultSFX(cookingPot, InventoryUtil.build(CookingConfig.failureItem), false);
            }
            handleFailureResult(player);
            return;
        }

        if (!(recipe instanceof DroppedItem droppedItem)) {
            return;
        }

        double masteryPerfectionMultiplier = RecipeDataUtil.hasMastery(player, droppedItem.getKey()) ? 1.5 : 1;
        boolean perfect = cookingPlayer.isPerfect() && (Math.random() < perfectChance * masteryPerfectionMultiplier);
        String drop = recipe.getCookedItems();

        CookResultEvent cookResultEvent = new CookResultEvent(player, perfect, InventoryUtil.build(drop), drop);
        Bukkit.getPluginManager().callEvent(cookResultEvent);
        if (cookResultEvent.isCancelled()) {
            return;
        }

        if (perfect) {
            AdventureUtil.playerMessage(player, MessageManager.infoPositive + MessageManager.cookingPerfect.replace("{recipe}", droppedItem.getNick()));
            drop = drop + CookingConfig.perfectItemSuffix;
            if (!RecipeDataUtil.hasMastery(player, droppedItem.getKey())) {
                MasteryManager.handleMastery(player, droppedItem.getKey());
            }
            JadeManager.cookingJade(player);
        }

        if (cookingPot != null) {
            CookingPotUtil.playCookingResultSFX(cookingPot, InventoryUtil.build(drop), true);
        }

        if (droppedItem.getSuccessActions() != null) {
            for (Action action : droppedItem.getSuccessActions()) {
                action.doOn(player, null);
            }
        }

        if (Competition.currentCompetition != null) {
            Competition.currentCompetition.tryAddBossBarToPlayer(player);
            float score = ((float) droppedItem.getScore());
            Competition.currentCompetition.refreshData(player, score, perfect);
        }

        if (Math.random() < CookingConfig.ingredientRefundChance) {
            refundIngredients(player, recipe);
        }

        playerSound(player, Sound.Source.AMBIENT, key(ConfigManager.customNamespace, "cooking.done"), 1f, 1f);
        InventoryUtil.giveItem(player, drop, 1, true);
        sendSuccessTitle(player, droppedItem.getNick());

        MasteryManager.incrementRecipeCount(player);
    }

    private void refundIngredients(Player player, Recipe loot) {
        List<String> ingredients = loot.getIngredients();
        String ingredient = ingredients.get(random.nextInt(ingredients.size()));
        String[] parts = ingredient.split(":");
        AdventureUtil.playerMessage(player, MessageManager.infoPositive + "You have used one less: " + GuiUtil.formatString(parts[0]));
        InventoryUtil.giveItem(player, parts[0], 1, false);
    }

    private void handleFailureResult(Player player) {
        playerSound(player, Sound.Source.AMBIENT, key(ConfigManager.customNamespace, "fail"), 1f, 1f);
        AdventureUtil.playerTitle(
                player,
                CookingConfig.failureTitle[random.nextInt(CookingConfig.failureTitle.length)],
                CookingConfig.failureSubTitle[random.nextInt(CookingConfig.failureSubTitle.length)],
                CookingConfig.failureFadeIn,
                CookingConfig.failureFadeStay,
                CookingConfig.failureFadeOut
        );
        InventoryUtil.giveItem(player, CookingConfig.failureItem, 1, false);
    }


    private void sendSuccessTitle(Player player, String recipe) {
        AdventureUtil.playerTitle(
                player,
                CookingConfig.successTitle[random.nextInt(CookingConfig.successTitle.length)]
                        .replace("{recipe}", recipe)
                        .replace("{player}", player.getName()),
                CookingConfig.successSubTitle[random.nextInt(CookingConfig.successSubTitle.length)]
                        .replace("{recipe}", recipe)
                        .replace("{player}", player.getName()),
                CookingConfig.successFadeIn,
                CookingConfig.successFadeStay,
                CookingConfig.successFadeOut
        );
    }

    private void showPlayerBar(Player player, @Nullable Recipe recipe) {
        Layout layout;
        if (recipe != null && recipe.getLayout() != null) {
            layout = recipe.getLayout()[random.nextInt(recipe.getLayout().length)];
        } else {
            layout = (Layout) LayoutManager.LAYOUTS.values().toArray()[random.nextInt(LayoutManager.LAYOUTS.size())];
        }
        int speed;
        int timer;
        int time;
        if (recipe != null) {
            Difficulty difficulty = recipe.getDifficulty()[random.nextInt(recipe.getDifficulty().length)];
            speed = difficulty.speed();
            timer = difficulty.timer();
            time = recipe.getTime();
        } else {
            speed = random.nextInt(5);
            time = 10000;
            timer = 1;
        }
        if (speed < 1) {
            speed = 1;
        }
        Difficulty difficult = new Difficulty(timer, speed);

        CookingPlayer cookingPlayer = new CookingPlayer(System.currentTimeMillis() + time, player, layout, difficult, this);
        cookingPlayer.runTaskTimer(BorealCore.plugin, 0, 1);
        cookingPlayerCache.put(player, cookingPlayer);
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, time / 50, 3));
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
        return cookingPlayer != null;
    }

    public void playSoundLoop(Player player) {
        BukkitRunnable soundTask = new BukkitRunnable() {
            @Override
            public void run() {
                playerSound(player, Sound.Source.AMBIENT, key(ConfigManager.customNamespace, "cooking"), 1f, 1f);
            }
        };
        soundTask.runTaskTimerAsynchronously(BorealCore.plugin, 0L, 60L);
        playerSoundTasks.put(player.getUniqueId(), soundTask);
        new BukkitRunnable() {
            @Override
            public void run() {
                stopSoundLoop(player);
            }
        }.runTaskLater(BorealCore.plugin, 600L);
    }

    public void stopSoundLoop(Player player) {
        UUID playerId = player.getUniqueId();
        BukkitRunnable soundTask = playerSoundTasks.get(playerId);
        if (soundTask != null) {
            soundTask.cancel();
            playerSoundTasks.remove(playerId);
        }
    }

    @Override
    public void onConsumeItem(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        ItemStack itemStack = event.getItem();
        String bcId = itemStack.getItemMeta().getPersistentDataContainer()
                .get(new NamespacedKey(BorealCore.getInstance(), "id"), PersistentDataType.STRING);
        if (bcId == null) {
            return;
        }

        bcId = bcId.replaceAll("[\\[\\]]", "");

        boolean perfect = bcId.contains(CookingConfig.perfectItemSuffix);
        String recipeKey = bcId.replace(CookingConfig.perfectItemSuffix, "");
        Recipe recipe = RecipeManager.COOKING_RECIPES.get(recipeKey);
        if (!(recipe instanceof DroppedItem droppedItem)) {
            AdventureUtil.consoleMessage(DebugLevel.ERROR, "Recipe not found or not a DroppedItem: " + recipeKey);
            AdventureUtil.playerMessage(player, MessageManager.pluginError + ": <gray>Recipe not found or not a DroppedItem: " + recipeKey);
            return;
        }

        Action[] actions = perfect ? droppedItem.getConsumeActions().get(1) : droppedItem.getConsumeActions().get(0);

        if (actions != null) {
            for (Action action : actions) {
                action.doOn(player, null);
                AdventureUtil.consoleMessage(DebugLevel.DEBUG, "Action performed: " + action.getClass().getSimpleName() + " for player: " + player.getName() + " for dish: " + recipeKey);
            }
        }
    }

    @Override
    public void onFurnitureInteract(FurnitureInteractEvent event) {
        Player player = event.getPlayer();
        CustomFurniture clickedFurniture = event.getFurniture();

        if (clickedFurniture.getId().equals(CookingConfig.unlitCookingPot)) {
            if (!clickCooldowns.containsKey(player) || (System.currentTimeMillis() - clickCooldowns.get(player) >= 2000)) {
                clickCooldowns.put(player, System.currentTimeMillis());
                if (player.getInventory().getItemInMainHand().getType() == Material.FLINT_AND_STEEL) {
                    ItemFrame unlitpot = (ItemFrame) Objects.requireNonNull(clickedFurniture).getArmorstand();
                    Rotation rot = unlitpot.getRotation();
                    ItemFrame litpot = (ItemFrame) CustomFurniture.spawnPreciseNonSolid(CookingConfig.litCookingPot, unlitpot.getLocation()).getArmorstand();
                    litpot.setRotation(rot);
                    clickedFurniture.remove(false);
                    unlitpot.getLocation().getBlock().setType(Material.BARRIER);
                    AdventureUtil.playerMessage(player, MessageManager.infoPositive + MessageManager.potLight);
                    CookingPotUtil.playCookingPotFX(clickedFurniture.getEntity().getLocation());
                } else {
                    AdventureUtil.playerMessage(player, MessageManager.infoNegative + MessageManager.potCold);
                }
            } else {
                String cooldown = String.valueOf((2000 - (System.currentTimeMillis() - clickCooldowns.get(player)) / 1000));
                AdventureUtil.playerMessage(player, MessageManager.infoNegative + MessageManager.potCooldown.replace("{time}", cooldown));
            }
        } else if (clickedFurniture.getId().equals(CookingConfig.litCookingPot)) {
            CookingPotUtil.playCookingPotFX(clickedFurniture.getEntity().getLocation());
            new CookingRecipeBookGUI(clickedFurniture).open(player);
        }
    }

    @Override
    public void onFurnitureBreak(FurnitureBreakEvent event) {
        CustomFurniture clickedFurniture = event.getFurniture();

        if (clickedFurniture.getNamespacedID().equals("fishing_trap")) {
            TrapsManager.getTrapsDatabase().deleteFishingTrapById(clickedFurniture.getEntity().getUniqueId().toString());
        }
        if (clickedFurniture.getId().equals(CookingConfig.litCookingPot)) {
            CookingPotUtil.cancelCookingPotFX(clickedFurniture.getArmorstand().getLocation());
        }
    }

    @Override
    public void onModuleEnable() throws Exception {

    }

    @Override
    public void onModuleDisable() throws Exception {

    }

    @Override
    public void onModuleInitialize(ModuleContext context) throws Exception {

    }
}