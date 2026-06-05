package plugin.borealcore.cooking;

import plugin.borealcore.cooking.configs.CookingConfig;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import plugin.borealcore.BorealCore;
import plugin.borealcore.manager.ConfigManager;
import plugin.borealcore.utility.AdventureUtil;
import plugin.borealcore.utility.ItemUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CookingPotUtil {

    public static void ingredientsSFX(Player player, List<String> ingredients, Location loc) {
        spawnNextIngredient(loc, player, ingredients, 0);
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
                    spawnNextIngredient(loc, player, ingredients, currentIndex + 1);
                });
                spawnSplashItem(loc);
                AdventureUtil.playerSound(player, net.kyori.adventure.sound.Sound.Source.AMBIENT, key(ConfigManager.customNamespace, "ingredient" + i), 1f, 1f);
            }
        }.runTaskLater(BorealCore.plugin, 20);
    }

    private static void spawnFakeIngredientItem(Location loc, String ingredient, Runnable onComplete) {
        Location spawnLocation = loc.clone().add(0, 2, 0);

        Item itemEntity = loc.getWorld().dropItem(spawnLocation, ItemUtil.build(ingredient));
        itemEntity.setCanPlayerPickup(false);
        itemEntity.setVelocity(itemEntity.getVelocity().zero());

        new BukkitRunnable() {
            @Override
            public void run() {
                itemEntity.remove();
                onComplete.run();
            }
        }.runTaskLater(BorealCore.plugin, 10);
    }

    private static void spawnSplashItem(Location loc) {
        Location spawnLocation = loc.clone().subtract(0, 0.1, 0);

        ArmorStand armorStand = (ArmorStand) loc.getWorld().spawnEntity(spawnLocation, EntityType.ARMOR_STAND);
        armorStand.setVisible(false);
        armorStand.setGravity(false);
        armorStand.setCollidable(false);
        armorStand.setDisabledSlots(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET);
        ItemStack splashItem = ItemUtil.build(CookingConfig.splashEffect);
        armorStand.setItem(EquipmentSlot.HEAD, splashItem);

        new BukkitRunnable() {
            @Override
            public void run() {
                armorStand.remove();
            }
        }.runTaskLater(BorealCore.plugin, CookingConfig.splashTime);
    }

    public static void playCookingResultSFX(Location loc, ItemStack item, Boolean success) {
        Location location = loc.add(0, 1.25, 0);

        if (success) {
            // Particles: composter
            loc.getWorld().spawnParticle(Particle.COMPOSTER, location, 15, 0.5, 0.5, 0.5);
        } else {
            // Particles: squid_ink
            loc.getWorld().spawnParticle(Particle.ANGRY_VILLAGER, location, 15, 0.5, 0.5, 0.5);
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

        new BukkitRunnable() {
            @Override
            public void run() {
                hologram.delete();
            }
        }.runTaskLater(BorealCore.plugin, 60);
    }

    public static void playCookingPotFX(Location location) {
        if (CookingModule.activeFXTasks.containsKey(location)) {
            return;
        }
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                playAmbientEffects(location);
            }
        }.runTaskTimerAsynchronously(BorealCore.plugin, 0L, 80L);
        CookingModule.activeFXTasks.put(location, task);
    }

    public static void playAmbientEffects(Location loc) {
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

    public static void cancelCookingPotFX(Location location) {
        BukkitTask task = CookingModule.activeFXTasks.remove(location);
        if (task != null) {
            task.cancel();
        }
    }
}
