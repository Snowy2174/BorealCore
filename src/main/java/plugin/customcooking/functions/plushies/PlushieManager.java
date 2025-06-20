package plugin.customcooking.functions.plushies;

import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTItem;
import dev.lone.itemsadder.api.CustomStack;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import plugin.customcooking.manager.configs.ConfigManager;
import plugin.customcooking.manager.configs.MessageManager;
import plugin.customcooking.object.Function;
import plugin.customcooking.utility.AdventureUtil;
import plugin.customcooking.utility.ConfigUtil;
import plugin.customcooking.utility.InventoryUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class PlushieManager extends Function {

    private final Map<String, List<String>> plushieCategories = new HashMap<>();
    private final Random random = new Random();

    @Override
    public void load() {
        YamlConfiguration config = ConfigUtil.getConfig("config.yml");
        plushieCategories.clear();

        if (config.isConfigurationSection("plushies")) {
            for (String rarity : config.getConfigurationSection("plushies").getKeys(false)) {
                List<String> plushies = config.getStringList("plushies." + rarity);
                plushieCategories.put(rarity, plushies);
            }
        }
    }

    @Override
    public void unload() {
        plushieCategories.clear();
    }

    public List<String> getPlushiesByRarity(String rarity) {
        return plushieCategories.getOrDefault(rarity, List.of());
    }

    public void processGamble(Player player, Player target, int amount) {
        if (amount < 1) {
            AdventureUtil.playerMessage(player, "<red>Invalid amount.");
            return;
        }

        for (int i = 0; i < amount; i++) {
            double chance = random.nextDouble() * 100;

            if (chance <= 3) {
                givePlushie(target, "Mythic", "<light_purple><bold>", true);
            } else if (chance <= 11) {
                givePlushie(target, "Rare", "<aqua><bold>", false);
            } else if (chance <= 21) {
                givePlushie(target, "Uncommon", "<green><bold>", false);
            } else if (chance <= 56) {
                givePlushie(target, "Common", "<white><bold>>", false);
            } else {
                sendNoReward(target);
            }
        }
    }

    private void givePlushie(Player player, String rarity, String color, boolean broadcast) {
        List<String> plushieList = getPlushiesByRarity(rarity);
        if (plushieList.isEmpty()) {
            AdventureUtil.playerMessage(player, "<red>No plushies defined for rarity: " + rarity);
            return;
        }

        String selectedPlushie = plushieList.get(random.nextInt(plushieList.size()));
        ItemStack plushieItem = InventoryUtil.build(selectedPlushie);

        if (plushieItem == null) {
            AdventureUtil.playerMessage(player, "<red>Failed to create plushie item: " + selectedPlushie);
            return;
        }

        plushieItem = modifyPlushieItem(plushieItem, selectedPlushie, player);

        player.getInventory().addItem(plushieItem);

        AdventureUtil.playerTitle(player, "<green>You won a " + color + rarity + "<green> Plushie!", "<green>Congratulations!", ConfigManager.successFadeIn,
                ConfigManager.successFadeStay,
                ConfigManager.successFadeOut);
        AdventureUtil.playerSound(player, Sound.Source.PLAYER, Key.key(getRewardSound1()), 1.0f, 1.0f);
        AdventureUtil.playerSound(player, Sound.Source.PLAYER, Key.key(getRewardSound2()), 1.0f, 1.0f);

        if (broadcast) {
            String message = MessageManager.infoPositive + "<green>" + player.getName() + " has won a " + color + rarity + "<green> Plushie!";
            Bukkit.broadcastMessage(message);
            AdventureUtil.consoleMessage(message);
        } else {
            AdventureUtil.playerMessage(player, MessageManager.infoPositive + "You received a " + color + rarity + "<green> Plushie!");
        }
    }

    private ItemStack modifyPlushieItem(ItemStack plushieItem, String id, Player player) {
        NBTItem nbtItem = new NBTItem(plushieItem);
        NBTCompound nbtCompound = nbtItem.addCompound("BorealCore");
        nbtCompound.setString("originalOwner", player.getName());
        nbtCompound.setString("plushieId", id);

        ItemMeta itemMeta = nbtItem.getItem().getItemMeta();
        if (itemMeta != null) {
            List<Component> lore = plushieItem.lore();
            lore.add(Component.empty());
            lore.add(AdventureUtil.getComponentFromMiniMessage("<italic><gray> Original Owner: </italic>" + player.getName()));
            itemMeta.lore(lore);
        }
        plushieItem.setItemMeta(itemMeta);
        return plushieItem;
    }

    private void updatePlayerPlushies(Player player) {
        for (ItemStack plushieItem : player.getInventory().getContents()) {
            if (plushieItem != null) {
                CustomStack stack = CustomStack.byItemStack(plushieItem);
                if (stack != null && stack.getNamespace().equals("plushies")) {
                    player.getInventory().removeItem(plushieItem);
                    String id = stack.getId();
                    plushieItem = modifyPlushieItem(plushieItem, id, player);
                    player.getInventory().addItem(plushieItem);
                }
            }
        }
    }

    private void sendNoReward(Player player) {
        AdventureUtil.playerTitle(player, "<red>Oh no! You didn't get anything this time!",
                "<red>Better luck next time!",
                ConfigManager.successFadeIn,
                ConfigManager.successFadeStay,
                ConfigManager.successFadeOut);
        AdventureUtil.playerSound(player, Sound.Source.PLAYER, Key.key(getNoRewardSound()), 1.0f, 1.0f);
    }

    public int getTitleDuration() {
        return 2;
    }

    public String getRewardSound1() {
        return "block.note_block.pling";
    }

    public String getRewardSound2() {
        return "entity.experience_orb.pickup";
    }

    public String getNoRewardSound() {
        return "entity.witch.death";
    }
}
