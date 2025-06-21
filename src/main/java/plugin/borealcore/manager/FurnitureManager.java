package plugin.borealcore.manager;

import dev.lone.itemsadder.api.CustomFurniture;
import dev.lone.itemsadder.api.Events.FurnitureBreakEvent;
import dev.lone.itemsadder.api.Events.FurnitureInteractEvent;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import plugin.borealcore.BorealCore;
import plugin.borealcore.listener.FurnitureListener;
import plugin.borealcore.manager.configs.ConfigManager;
import plugin.borealcore.manager.configs.MessageManager;
import plugin.borealcore.object.Function;
import plugin.borealcore.utility.AdventureUtil;
import plugin.borealcore.utility.InventoryUtil;

import java.util.*;

import static net.kyori.adventure.key.Key.key;

public class FurnitureManager extends Function {

    private static final Map<Location, Hologram> holograms = new HashMap<>();
    private final FurnitureListener furnitureListener;
    private final Map<Player, Long> cooldowns;
    private final Map<Location, BukkitTask> activeFXTasks = new HashMap<>();

    public FurnitureManager() {
        this.furnitureListener = new FurnitureListener(this);
        this.cooldowns = new HashMap<>();
        load();
    }

    @Override
    public void load() {
        Bukkit.getPluginManager().registerEvents(this.furnitureListener, BorealCore.plugin);
    }

    @Override
    public void unload() {
        HandlerList.unregisterAll(this.furnitureListener);
    }

    public static void ingredientsSFX(Player player, List<String> ingredients, Location loc) {
        spawnNextIngredient(loc, player, ingredients, 0); // Start spawning ingredients from index 0
    }

    private static void spawnNextIngredient(Location loc, Player player, List<String> ingredients, int currentIndex) {
        if (currentIndex >= ingredients.size()) {
            return;
        }
        String ingredient = ingredients.get(currentIndex);
        String[] parts = ingredient.split(":");
        Random random = new Random();
        int i = random.nextInt(3);

        if (parts[0].endsWith("*")) {
            parts[0] = parts[0].replaceAll("\\*", "");
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                spawnFakeIngredientItem(loc, parts[0], () -> {
                    // After the fake ingredient is removed, spawn the next ingredient
                    spawnNextIngredient(loc, player, ingredients, currentIndex + 1);
                });
                spawnSplashItem(loc);
                AdventureUtil.playerSound(player, net.kyori.adventure.sound.Sound.Source.AMBIENT, key(ConfigManager.customNamespace, "ingredient" + i), 1f, 1f);
            }
        }.runTaskLater(BorealCore.plugin, 20);
    }

    private static void spawnFakeIngredientItem(Location loc, String ingredient, Runnable onComplete) {
        Location spawnLocation = loc.clone().add(0, 2, 0);

        // Create a dropped item entity at the specified location
        Item itemEntity = loc.getWorld().dropItem(spawnLocation, InventoryUtil.build(ingredient));
        itemEntity.setCanPlayerPickup(false);
        itemEntity.setVelocity(itemEntity.getVelocity().zero());

        new BukkitRunnable() {
            @Override
            public void run() {
                itemEntity.remove(); // Remove the item entity from the world
                onComplete.run(); // Invoke the callback when the removal is complete
            }
        }.runTaskLater(BorealCore.plugin, 10);
    }

    private static void spawnSplashItem(Location loc) {
        Location spawnLocation = loc.clone().subtract(0, 0.1, 0);

        ArmorStand armorStand = (ArmorStand) loc.getWorld().spawnEntity(spawnLocation, EntityType.ARMOR_STAND);
        armorStand.setVisible(false);
        armorStand.setGravity(false);
        ItemStack splashItem = InventoryUtil.build(ConfigManager.splashEffect);
        armorStand.setItem(EquipmentSlot.HEAD, splashItem);

        new BukkitRunnable() {
            @Override
            public void run() {
                armorStand.remove(); // Remove the item entity from the world
            }
        }.runTaskLater(BorealCore.plugin, ConfigManager.splashTime);
    }

    public static void playCookingResultSFX(Location loc, ItemStack item, Boolean success) {
        Location location = loc.add(0, 1.25, 0);

        if (success) {
            // Particles: composter
            loc.getWorld().spawnParticle(Particle.COMPOSTER, location, 15, 0.5, 0.5, 0.5);
        } else {
            // Particles: squid_ink
            loc.getWorld().spawnParticle(Particle.VILLAGER_ANGRY, location, 15, 0.5, 0.5, 0.5);
        }
        playCookingPreview(location, item, success);
    }

    private static void playCookingPreview(Location loc, ItemStack item, Boolean success) {
        loc.getWorld().spawnParticle(Particle.CRIT, loc, 15, 0.25, 0.25, 0.25, 0.2);
        createHologram(item, loc, success);
    }

    public static void createHologram(ItemStack recipe, Location location, Boolean success) {

        String name = recipe.displayName().examinableName() + "_" + success.toString() + "_" + location.getBlockX() + "_" + location.getBlockY();
        if (DHAPI.getHologram(name) != null)
            return;

        List<String> contents = new ArrayList<>();

        if (success) {
            contents.add("&aSuccess!");
        } else {
            contents.add("&aFailure!");
        }

        contents.add(recipe.getItemMeta().getDisplayName());
        Hologram hologram = DHAPI.createHologram(name, location.clone().add(0, 1.5, 0), contents);
        DHAPI.addHologramLine(hologram, recipe);

        // Schedule a task to remove the hologram after a set time
        new BukkitRunnable() {
            @Override
            public void run() {
                hologram.delete(); // Remove the hologram
            }
        }.runTaskLater(BorealCore.plugin, 60);
    }

    public void onFurnitureInteract(FurnitureInteractEvent event) {
        Player player = event.getPlayer();
        CustomFurniture clickedFurniture = event.getFurniture();

        // Check if the clicked block is an unlit cookingpot
        if (clickedFurniture.getId().equals(ConfigManager.unlitCookingPot)) {
            if (!cooldowns.containsKey(player) || (System.currentTimeMillis() - cooldowns.get(player) >= 2000)) {
                cooldowns.put(player, System.currentTimeMillis());
                if (player.getInventory().getItemInMainHand().getType() == Material.FLINT_AND_STEEL) {
                    ItemFrame unlitpot = (ItemFrame) Objects.requireNonNull(clickedFurniture).getArmorstand();
                    Rotation rot = unlitpot.getRotation();
                    ItemFrame litpot = (ItemFrame) CustomFurniture.spawnPreciseNonSolid(ConfigManager.litCookingPot, unlitpot.getLocation()).getArmorstand();
                    litpot.setRotation(rot);
                    clickedFurniture.remove(false);
                    unlitpot.getLocation().getBlock().setType(Material.BARRIER);
                    AdventureUtil.playerMessage(player, MessageManager.infoPositive + MessageManager.potLight);
                    playCookingPotFX(clickedFurniture.getEntity().getLocation());
                } else {
                    AdventureUtil.playerMessage(player, MessageManager.infoNegative + MessageManager.potCold);
                }
            } else {
                String cooldown = String.valueOf((2000 - (System.currentTimeMillis() - cooldowns.get(player)) / 1000));
                AdventureUtil.playerMessage(player, MessageManager.infoNegative + MessageManager.potCooldown.replace("{time}", cooldown));
            }
        } else if (clickedFurniture.getId().equals(ConfigManager.litCookingPot)) {
            playCookingPotFX(clickedFurniture.getEntity().getLocation());
            GuiManager.getRecipeBook(clickedFurniture).open(player);
        }
    }

    public void onFurnitureBreak(FurnitureBreakEvent event) {
        CustomFurniture clickedFurniture = event.getFurniture();

        if (clickedFurniture.getId().equals(ConfigManager.litCookingPot)) {
            cancelCookingPotFX(clickedFurniture.getArmorstand().getLocation());
        }
    }

    public void playCookingPotFX(Location location) {
        if (activeFXTasks.containsKey(location)) {
            return; // FX is already running at this location
        }

        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                playAmbientEffects(location);
            }
        }.runTaskTimerAsynchronously(BorealCore.plugin, 0L, 80L);

        activeFXTasks.put(location, task);
    }

    public void playAmbientEffects(Location loc) {
        if (!loc.getChunk().isLoaded()) {
            cancelCookingPotFX(loc);
            return;
        }
        Bukkit.getScheduler().runTask(BorealCore.plugin, () -> {
            loc.getWorld().spawnParticle(Particle.FLAME, loc, 3, 0.25, 0.25, 0.25, 0.01);
            loc.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, loc.clone().add(0, 1, 0), 0, 0, 1, 0, 0.03, null, true);
            loc.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, loc.clone().add(0, 1, 0), 0, 0, 1, 0, 0.03, null, true);
            loc.getWorld().playSound(loc, Sound.BLOCK_FIRE_AMBIENT, 1f, 1f);
            loc.getWorld().playSound(loc, Sound.BLOCK_LAVA_AMBIENT, 1f, 1f);
        });
        new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.getScheduler().runTask(BorealCore.plugin, () -> {
                    loc.getWorld().spawnParticle(Particle.FLAME, loc, 3, 0.25, 0.25, 0.25, 0.01);
                    loc.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, loc.clone().add(0, 1, 0), 0, 0, 1, 0, 0.03, null, true);
                    loc.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, loc.clone().add(0, 1, 0), 0, 0, 1, 0, 0.03, null, true);
                });
            }
        }.runTaskLaterAsynchronously(BorealCore.plugin, 40L);
    }

    public void cancelCookingPotFX(Location location) {
        BukkitTask task = activeFXTasks.remove(location);
        if (task != null) {
            task.cancel();
        }
    }
}

