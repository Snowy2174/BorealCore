package plugin.borealcore.functions.herbalism;

import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;
import plugin.borealcore.BorealCore;
import plugin.borealcore.functions.cooking.Difficulty;
import plugin.borealcore.functions.cooking.configs.LayoutManager;
import plugin.borealcore.functions.cooking.object.Layout;
import plugin.borealcore.functions.herbalism.objects.*;
import plugin.borealcore.manager.configs.ConfigManager;
import plugin.borealcore.manager.configs.MessageManager;
import plugin.borealcore.object.Function;
import plugin.borealcore.utility.AdventureUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static net.kyori.adventure.key.Key.key;
import static plugin.borealcore.functions.herbalism.configs.HerbManager.HERBS;
import static plugin.borealcore.functions.herbalism.objects.ModifierType.*;

public class HerbalismManager extends Function {

    private final Random random;
    private final HashMap<Player, Infusion> currentInfusions;
    private final HashMap<Player, Location> teaPotLocations;
    public final ConcurrentHashMap<Player, InfusingPlayer> infusingPlayerCache;
    private final Map<UUID, BukkitRunnable> playerSoundTasks = new HashMap<>();

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

    public void handleInfuse(Herb[] herbs, Player player) {
        // @TODO generate an infusion object
        Infusion infusion = new Infusion(herbs);
        // @TODO get the ingredients from the player inventory or clicked furniture
        ArrayList<ItemStack> ingredients = new ArrayList<>(); //@TODO replace with actual ingredient retrieval logic
        for (ItemStack item : ingredients) {
            if (item == null || item.getType() == Material.AIR) continue;
            // @TODO check if the item is a valid herb and add it to the infusion
            Herb herb = HERBS.get(item.getType().name().toLowerCase());
            if (herb != null) {
                infusion.getIngredients().add(herb);
            } else {
                AdventureUtil.playerMessage(player, MessageManager.pluginError + ": <gray>Invalid herb: " + item.getType().name());
                return;
            }
        }
        onStartInfusion(player, infusion);
        // @TODO modify the infusion object with the final model data
    }

    public void autoInfuse(Player player, Double quality, String[] args) {
        if (isPlayerInfusing(player)) {
            AdventureUtil.playerMessage(player, MessageManager.pluginError + ": <gray>You are already infusing something.");
            return;
        }

        Herb[] herbs = new Herb[args.length];
        for (int i = 0; i < args.length; i++) {
            herbs[i] = HERBS.get(key(args[i].toLowerCase()));
            if (herbs[i] == null) {
                AdventureUtil.playerMessage(player, MessageManager.pluginError + ": <gray>Unknown herb: " + args[i]);
                return;
            }
        }

        buildInfusion(herbs, player, quality);
    }

    public void buildInfusion(Herb[] herbs, Player player, Double quality) {
        if (herbs == null || herbs.length == 0) {
            AdventureUtil.playerMessage(player, MessageManager.pluginError + ": <gray>You need to select at least one herb to infuse.");
            return;
        }
        Infusion infusion = new Infusion(herbs);
        infusion.setQuality(quality);
        infusion = formulateEffects(infusion);
        infusion.buildStack();
        if (infusion.getItemStack() == null) {
            AdventureUtil.playerMessage(player, MessageManager.pluginError + ": <gray>Failed to create infusion item stack.");
            return;
        }
        player.getInventory().addItem(infusion.getItemStack());
    }

    public static ItemStack infusionItemStack(Infusion infusion) {
        if (infusion == null || infusion.getIngredients() == null || infusion.getIngredients().size() == 0) {
            return null;
        }
        ItemStack itemStack = new ItemStack(Material.POTION, 1);
        // @TODO generate itemstack from infusion object
        // @TODO set lore, name, colour, etc.
        itemStack.editMeta(meta -> {
            meta.displayName(AdventureUtil.getComponentFromMiniMessage("Herbalism Infusion"));
            List<String> lore = new ArrayList<>();
            lore.add("Infusion Quality: " + infusion.getQuality());
            for (Herb herb : infusion.getIngredients()) {
                lore.add("Ingredient: " + herb.getNick());
            }

            lore.add("Effects:");
            // @TODO use logic from EffectManager for cooking? (maybe with modifications from modifications)
            for (PotionEffect effect : infusion.getEffects()) {
                lore.add(effect.getType().getName() + " (" + effect.getDuration() / 20 + "s, Level " + (effect.getAmplifier() + 1) + ")");
            }

            meta.setLore(lore);
            meta.setCustomModelData(1234);
        });

        // Set potion type and color based on the infusion ingredients
        PotionMeta potionMeta = (PotionMeta) itemStack.getItemMeta();
        potionMeta.setColor(Color.fromRGB(averageColor(infusion.getIngredients())));
        itemStack.setItemMeta(potionMeta);

        // Write effects to NBT
        NBTItem nbtItem = new NBTItem(itemStack);
        NBTCompound nbt = nbtItem.addCompound("BorealCore");
        StringBuilder sb = new StringBuilder();
        if (infusion.getEffects() != null) {
            for (PotionEffect effect : infusion.getEffects()) {
                sb.append(effect.getType().getName()).append(",")
                        .append(effect.getDuration()).append(",")
                        .append(effect.getAmplifier()).append(";");
            }
        }
        nbt.setString("infusion_effects", sb.toString());
        itemStack.setItemMeta(nbtItem.getItem().getItemMeta());

        return itemStack;
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
            AdventureUtil.playerMessage(player, MessageManager.pluginError + ": <gray>Infusion says no");
        } else {
            if (recipe == null) {
            } else {
                //@TODO init first step of infusion, by sending bar of first ingredient
                showPlayerBar(player, recipe.getIngredients().get(0));
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
        infusingPlayer.cancel();
        Infusion infusion = currentInfusions.get(player);

        //stopSoundLoop(player);
        player.removePotionEffect(PotionEffectType.SLOWNESS);

        if (infusion == null)
            return;

        int idx = infusion.getIngredientIndex();
        Herb currentIngredient = infusion.getIngredients().get(idx);

        double stageModifier = 0;
        if (infusingPlayer.isPerfect()) {
            //@TODO handle perfect result
            stageModifier = 2;
        } else if (infusingPlayer.isSuccess()) {
            stageModifier = 1;
        } else {
            //@TODO handle failure result
        }
        infusion.recalculateQuality(stageModifier);

        infusion.incrementIngredientIndex();

        if (infusion.getIngredientIndex() < infusion.getIngredients().size()) {
            // Start next minigame for the next ingredient
            showPlayerBar(player, infusion.getIngredients().get(infusion.getIngredientIndex()));
        } else {
            // All ingredients done, finish infusion
            // @TODO: handle infusion completion
            infusion = formulateEffects(infusion);
            infusion.buildStack();
            if (infusion.getItemStack() != null) {
                player.getInventory().addItem(infusion.getItemStack());
                sendIngredientSuccessTitle(player, infusion.getIngredients().get(0).getNick());
                AdventureUtil.playerMessage(player, MessageManager.prefix + ": <gray>Infusion completed successfully!");
                currentInfusions.remove(player);
            } else {
                AdventureUtil.playerMessage(player, MessageManager.pluginError + ": <gray>Failed to create infusion item stack.");
            }
        }
    }

    private void handleFailureResult(Player player) {
    }

    private Infusion formulateEffects(Infusion infusion) {
        Double quality = infusion.getQuality();
        List<Herb> ingredients = infusion.getIngredients();
        List<Modifier> modifiers = new ArrayList<>();

        // Apply initial effects from the unprocessed herbs
        for (Herb herb : ingredients) {
            if (herb.getEffects() != null) {
                infusion.getEffects().addAll(herb.getEffects());
            }
        }

        // Combine effects by type (max duration/amplifier)
        Map<PotionEffectType, PotionEffect> combinedEffects = new HashMap<>();
        for (PotionEffect effect : infusion.getEffects()) {
            PotionEffectType type = effect.getType();
            if (combinedEffects.containsKey(type)) {
                PotionEffect existing = combinedEffects.get(type);
                int newDuration = Math.max(existing.getDuration(), effect.getDuration());
                int newAmplifier = Math.max(existing.getAmplifier(), effect.getAmplifier());
                combinedEffects.put(type, new PotionEffect(type, newDuration, newAmplifier));
            } else {
                combinedEffects.put(type, effect);
            }
        }
        infusion.getEffects().clear();
        infusion.getEffects().addAll(combinedEffects.values());

        // Apply quality modifier to all effects
        List<PotionEffect> qualityEffects = new ArrayList<>();
        for (PotionEffect effect : infusion.getEffects()) {
            PotionEffectType type = effect.getType();
            int newDuration = (int) (effect.getDuration() * (1 + quality / 100));
            int newAmplifier = effect.getAmplifier() + (int) (quality / 10);
            qualityEffects.add(new PotionEffect(type, newDuration, newAmplifier));
        }
        infusion.getEffects().clear();
        infusion.getEffects().addAll(qualityEffects);

        // Collect modifiers from the ingredients
        for (Herb herb : ingredients) {
            if (herb.getModifiers() != null) {
                modifiers.addAll(Arrays.asList(herb.getModifiers()));
            }
        }

        // Apply modifiers from the infusion
        if (!modifiers.isEmpty()) {
            for (Modifier modifier : modifiers) {
                List<PotionEffect> toAdd = new ArrayList<>();
                ModifierType type = modifier.getType();
                if (type == AMPLIFY) {
                    for (PotionEffect effect : infusion.getEffects()) {
                        PotionEffectType effType = effect.getType();
                        int newAmplifier = effect.getAmplifier() + modifier.getValue();
                        toAdd.add(new PotionEffect(effType, effect.getDuration(), newAmplifier));
                    }
                } else if (type == LENGTHEN) {
                    for (PotionEffect effect : infusion.getEffects()) {
                        PotionEffectType effType = effect.getType();
                        int newDuration = (int) (effect.getDuration() * (1 + modifier.getValue() / 100.0));
                        toAdd.add(new PotionEffect(effType, newDuration, effect.getAmplifier()));
                    }
                } else if (type == DILUTE) {
                    for (PotionEffect effect : infusion.getEffects()) {
                        PotionEffectType effType = effect.getType();
                        int newDuration = (int) (effect.getDuration() * (1 + quality / 100));
                        int newAmplifier = effect.getAmplifier() + (int) (quality / 10);
                        toAdd.add(new PotionEffect(effType, newDuration, newAmplifier));
                    }
                } else if (type == CONCENTRATE) {
                    for (PotionEffect effect : infusion.getEffects()) {
                        PotionEffectType effType = effect.getType();
                        int newDuration = (int) (effect.getDuration() * (1 - modifier.getValue() / 100.0));
                        int newAmplifier = effect.getAmplifier() + modifier.getValue();
                        toAdd.add(new PotionEffect(effType, newDuration, newAmplifier));
                    }
                } else if (type == INVERT) {
                    for (PotionEffect effect : infusion.getEffects()) {
                        PotionEffectType invertedType = InvertedEffect.getInverted(effect.getType());
                        toAdd.add(new PotionEffect(invertedType, effect.getDuration(), effect.getAmplifier()));
                    }
                }
                infusion.getEffects().addAll(toAdd);
            }
        }

        return infusion;
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
            layout = (Layout) LayoutManager.LAYOUTS.values().toArray()[random.nextInt(LayoutManager.LAYOUTS.size())];
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

        InfusingPlayer infusingPlayer = new InfusingPlayer(System.currentTimeMillis() + time, player, layout, difficult, this);
        infusingPlayer.runTaskTimer(BorealCore.plugin, 0, 1);
        //@TODO sort cache
        infusingPlayerCache.put(player, infusingPlayer);
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, time / 50, 3));
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

    public static int averageColor(List<Herb> herbs) {
        int r = 0, g = 0, b = 0;
        for (Herb herb : herbs) {
            int color = herb.getColour();
            r += (color >> 16) & 0xFF;
            g += (color >> 8) & 0xFF;
            b += color & 0xFF;
        }
        int n = herbs.size();
        return ((r / n) << 16) | ((g / n) << 8) | (b / n);
    }

    public static List<PotionEffect> readInfusionEffectsFromNBT(ItemStack itemStack) {
        NBTItem nbtItem = new NBTItem(itemStack);
        NBTCompound nbt = nbtItem.getCompound("BorealCore");
        List<PotionEffect> effects = new ArrayList<>();
        if (nbt != null && nbt.hasKey("infusion_effects")) {
            String data = nbt.getString("infusion_effects");
            String[] parts = data.split(";");
            for (String part : parts) {
                if (part.isEmpty()) continue;
                String[] vals = part.split(",");
                PotionEffectType type = PotionEffectType.getByName(vals[0]);
                int duration = Integer.parseInt(vals[1]);
                int amplifier = Integer.parseInt(vals[2]);
                if (type != null) {
                    effects.add(new PotionEffect(type, duration, amplifier));
                }
            }
        }
        return effects;
    }
}
