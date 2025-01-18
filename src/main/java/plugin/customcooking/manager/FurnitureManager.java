package plugin.customcooking.manager;

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
import plugin.customcooking.CustomCooking;
import plugin.customcooking.listener.FurnitureListener;
import plugin.customcooking.manager.configs.ConfigManager;
import plugin.customcooking.manager.configs.MessageManager;
import plugin.customcooking.object.Function;
import plugin.customcooking.utility.AdventureUtil;

import java.util.*;

import static net.kyori.adventure.key.Key.key;
import static plugin.customcooking.manager.configs.ConfigManager.splashTime;
import static plugin.customcooking.utility.AdventureUtil.playerSound;
import static plugin.customcooking.utility.InventoryUtil.build;

public class FurnitureManager extends Function {

    private final FurnitureListener furnitureListener;
    private final Map<Player, Long> cooldowns;
    private static final Map<Location, Hologram> holograms = new HashMap<>();
    private Map<Location, BukkitTask> activeFXTasks = new HashMap<>();

    public FurnitureManager() {
        this.furnitureListener = new FurnitureListener(this);
        this.cooldowns = new HashMap<>();
        load();
    }

    @Override
    public void load() {
        Bukkit.getPluginManager().registerEvents(this.furnitureListener, CustomCooking.plugin);
    }

    @Override
    public void unload() {
        HandlerList.unregisterAll(this.furnitureListener);
    }

    public void onFurnitureInteract(FurnitureInteractEvent event) {
        Player player = event.getPlayer();
        CustomFurniture clickedFurniture = event.getFurniture();

        // Check if the clicked block is an unlit cookingpot
        if (clickedFurniture.getId().equals(ConfigManager.unlitCookingPot)) {
            if (!cooldowns.containsKey(player) || (System.currentTimeMillis() - cooldowns.get(player) >= 2000)) {
                // Set a cooldown of 2 seconds
                cooldowns.put(player, System.currentTimeMillis());
                // Check if the player right-clicked with flint and steel
                if (player.getInventory().getItemInMainHand().getType() == Material.FLINT_AND_STEEL) {

                    // Replace the furniture block with the lit furniture
                    //clickedFurniture.replaceFurniture(ConfigManager.litCookingPot);
                    ItemFrame unlitpot = (ItemFrame) Objects.requireNonNull(clickedFurniture).getArmorstand();
                    Rotation rot = unlitpot.getRotation();
                    ItemFrame litpot = (ItemFrame) CustomFurniture.spawnPreciseNonSolid(ConfigManager.litCookingPot, unlitpot.getLocation()).getArmorstand();
                    litpot.setRotation(rot);

                    // Replace the furniture block with the lit furniture
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

    public static void ingredientsSFX(Player player, List<String> ingredients, Location loc) {
        spawnNextIngredient(loc, player, ingredients, 0); // Start spawning ingredients from index 0
    }

    private static void spawnNextIngredient(Location loc, Player player, List<String> ingredients, int currentIndex) {
        if (currentIndex >= ingredients.size()) {
            // All ingredients have been spawned
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
                playerSound(player, net.kyori.adventure.sound.Sound.Source.AMBIENT, key(ConfigManager.customNamespace, "ingredient"+ i), 1f, 1f);
            }
        }.runTaskLater(CustomCooking.plugin, 20);
    }

    private static void spawnFakeIngredientItem(Location loc, String ingredient, Runnable onComplete) {
        Location spawnLocation = loc.clone().add(0,2,0);

        // Create a dropped item entity at the specified location
        Item itemEntity = loc.getWorld().dropItem(spawnLocation, build(ingredient));
        itemEntity.setCanPlayerPickup(false);
        itemEntity.setVelocity(itemEntity.getVelocity().zero());

        new BukkitRunnable() {
            @Override
            public void run() {
                itemEntity.remove(); // Remove the item entity from the world
                onComplete.run(); // Invoke the callback when the removal is complete
            }
        }.runTaskLater(CustomCooking.plugin, 10);
    }

    private static void spawnSplashItem(Location loc) {
        Location spawnLocation = loc.clone().subtract(0,0.1,0);

        // Create an ArmorStand entity at the specified location
        ArmorStand armorStand = (ArmorStand) loc.getWorld().spawnEntity(spawnLocation, EntityType.ARMOR_STAND);
        armorStand.setVisible(false);
        armorStand.setGravity(false);

        // Create a fake ingredient ItemStack
        ItemStack splashItem = build(ConfigManager.splashEffect);

        // Set the fake ingredient ItemStack as the ArmorStand's helmet item
        armorStand.setItem(EquipmentSlot.HEAD, splashItem);

        new BukkitRunnable() {
            @Override
            public void run() {
                armorStand.remove(); // Remove the item entity from the world
            }
        }.runTaskLater(CustomCooking.plugin, splashTime);
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
        // Particles: crit
        loc.getWorld().spawnParticle(Particle.CRIT, loc, 15, 0.25, 0.25, 0.25, 0.2);
        // Spawn recipe item preview
        createHologram(item, loc, success);
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
        }.runTaskTimerAsynchronously(CustomCooking.plugin, 0L, 80L);

        activeFXTasks.put(location, task);
    }

    public void playAmbientEffects(Location loc) {
        if (!loc.getChunk().isLoaded()) {
            cancelCookingPotFX(loc);
            return;
        }

        // Schedule all world interactions on the main thread
        Bukkit.getScheduler().runTask(CustomCooking.plugin, () -> {
            // Particles: flame
            loc.getWorld().spawnParticle(Particle.FLAME, loc, 3, 0.25, 0.25, 0.25, 0.01);

            // Particles: campfire_cosy_smoke
            loc.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, loc.clone().add(0, 1, 0), 0, 0, 1, 0, 0.03, null, true);
            loc.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, loc.clone().add(0, 1, 0), 0, 0, 1, 0, 0.03, null, true);

            // Sound: block.fire.ambient
            loc.getWorld().playSound(loc, Sound.BLOCK_FIRE_AMBIENT, 1f, 1f);

            // Sound: block.lava.ambient
            loc.getWorld().playSound(loc, Sound.BLOCK_LAVA_AMBIENT, 1f, 1f);
        });

        // Schedule the repeated particle effects asynchronously
        new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.getScheduler().runTask(CustomCooking.plugin, () -> {
                    // Repeated particles
                    loc.getWorld().spawnParticle(Particle.FLAME, loc, 3, 0.25, 0.25, 0.25, 0.01);
                    loc.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, loc.clone().add(0, 1, 0), 0, 0, 1, 0, 0.03, null, true);
                    loc.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, loc.clone().add(0, 1, 0), 0, 0, 1, 0, 0.03, null, true);
                });
            }
        }.runTaskLaterAsynchronously(CustomCooking.plugin, 40L);
    }

    public void cancelCookingPotFX(Location location) {
        BukkitTask task = activeFXTasks.remove(location);
        if (task != null) {
            task.cancel();
        }
    }

    public static void createHologram(ItemStack recipe, Location location, Boolean success) {

        String name = recipe.displayName().examinableName() + "_" +  success.toString() + "_" + location.getBlockX() + "_" + location.getBlockY();
        if(DHAPI.getHologram(name) != null)
            return;

        List<String> contents = new ArrayList<>();

            if (success) {
                contents.add("&aSuccess!");
            } else {
                contents.add("&aFailure!");
            }

            contents.add(recipe.getItemMeta().getDisplayName());
            Hologram hologram = DHAPI.createHologram(name, location.clone().add(0,1.5,0), contents);
            DHAPI.addHologramLine(hologram, recipe);

            // Schedule a task to remove the hologram after a set time
            new BukkitRunnable() {
                @Override
                public void run() {
                    hologram.delete(); // Remove the hologram
                }
            }.runTaskLater(CustomCooking.plugin, 60);
        }
    }

