package plugin.borealcore.market;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import plugin.borealcore.BorealCore;
import plugin.borealcore.api.module.BorealModule;
import plugin.borealcore.api.module.ModuleContext;
import plugin.borealcore.manager.configs.DebugLevel;
import plugin.borealcore.manager.configs.MessageManager;
import plugin.borealcore.utility.AdventureUtil;
import plugin.borealcore.utility.ItemUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class MarketModule implements BorealModule, CommandExecutor, TabCompleter {

    public static HashMap<String, HashMap<String, ItemContext>> MARKET_ITEMS;

    @Override
    public void onModuleEnable() {
        MARKET_ITEMS = new HashMap<>();
        loadMarketItems();
        int totalItems = MARKET_ITEMS.values().stream().mapToInt(Map::size).sum();
        AdventureUtil.consoleMessage("Loaded <green>" + totalItems + " <gray>market items across <green>" + MARKET_ITEMS.size() + " <gray>categories");
    }

    @Override
    public void onModuleDisable() {
        if (MARKET_ITEMS != null) MARKET_ITEMS.clear();
    }

    @Override
    public void onModuleInitialize(ModuleContext context) {
        context.getPlugin().getCommand("market").setExecutor(this);
        context.getPlugin().getCommand("market").setTabCompleter(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            AdventureUtil.consoleMessage(MessageManager.noConsole);
            return true;
        }

        if (!player.hasPermission("market.access")) {
            AdventureUtil.playerMessage(player, MessageManager.infoNegative + "You don't have permission to access the market.");
            return true;
        }

        if (args.length == 0) {
            openMarketMainMenu(player);
            return true;
        }

        if (args[0].equalsIgnoreCase("category")) {
            if (args.length < 2) {
                AdventureUtil.playerMessage(player, MessageManager.infoNegative + "Usage: /market category <name>");
                return true;
            }

            String categoryName = args[1];
            if (!MARKET_ITEMS.containsKey(categoryName)) {
                AdventureUtil.playerMessage(player, MessageManager.infoNegative + "Category not found: " + categoryName);
                return true;
            }

            openMarketCategory(player, categoryName);
            return true;
        }

        sendUsage(player);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player player) || !player.hasPermission("market.access")) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            completions.add("category");
            return completions.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 2 && args[0].equalsIgnoreCase("category")) {
            return MARKET_ITEMS.keySet().stream()
                    .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    private void sendUsage(Player player) {
        AdventureUtil.playerMessage(player, "<gold>Market Commands:");
        AdventureUtil.playerMessage(player, "<yellow>/market <gray>- Open the main market menu");
        AdventureUtil.playerMessage(player, "<yellow>/market category <name> <gray>- Open a specific market category");
    }

    private void openMarketMainMenu(Player player) {
        AdventureUtil.consoleMessage(DebugLevel.DEBUG, "Opening main market menu for " + player.getName());
        new MarketGUI("Shop", 6).open(player);
    }

    private void openMarketCategory(Player player, String categoryName) {
        AdventureUtil.consoleMessage(DebugLevel.DEBUG, "Opening market category: " + categoryName + " for " + player.getName());
        List<ItemContext> items = getItems(categoryName);
        new CategoryGUI(categoryName, 6, items).open(player);
    }

    public static List<String> getCategories() {
        return new ArrayList<>(MARKET_ITEMS.keySet());
    }

    public static List<ItemContext> getItems(String category) {
        HashMap<String, ItemContext> categoryItems = MARKET_ITEMS.get(category);
        if (categoryItems == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(categoryItems.values());
    }

    private void loadMarketItems() {
        File marketDir = new File(BorealCore.plugin.getDataFolder() + File.separator + "market");
        if (!marketDir.exists()) {
            if (!marketDir.mkdir()) {
                AdventureUtil.consoleMessage(DebugLevel.DEBUG, "Failed to create market directory");
                return;
            }
            BorealCore.plugin.saveResource("market" + File.separator + "crops.yml", false);
        }

        File[] files = marketDir.listFiles();
        if (files == null) {
            AdventureUtil.consoleMessage(DebugLevel.DEBUG, "No market category files found");
            return;
        }

        for (File categoryFile : files) {
            if (!categoryFile.getName().endsWith(".yml")) continue;

            String categoryName = categoryFile.getName().replace(".yml", "");
            loadCategoryItems(categoryName, categoryFile);
        }
    }


    private void loadCategoryItems(String categoryName, File categoryFile) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(categoryFile);

        if (config == null || config.getKeys(false).isEmpty()) {
            AdventureUtil.consoleMessage(DebugLevel.DEBUG, "Configuration file is empty or failed to load: " + categoryFile.getName());
            return;
        }

        HashMap<String, ItemContext> categoryItems = new HashMap<>();
        Set<String> itemIds = config.getKeys(false);

        for (String itemId : itemIds) {
            try {
                if (!config.isConfigurationSection(itemId)) {
                    AdventureUtil.consoleMessage(DebugLevel.DEBUG, "Item " + itemId + " is not a configuration section");
                    continue;
                }

                ConfigurationSection itemSection = config.getConfigurationSection(itemId);
                ItemContext context = loadItemContext(itemId, itemSection);

                if (context != null) {
                    categoryItems.put(itemId, context);
                    //AdventureUtil.consoleMessage(DebugLevel.DEBUG, "Loaded item " + itemId + " in category " + categoryName);
                }
            } catch (Exception e) {
                AdventureUtil.consoleMessage(DebugLevel.DEBUG, "Failed to load item " + itemId + " in category " + categoryName + ": " + e.getMessage());
            }
        }

        if (!categoryItems.isEmpty()) {
            MARKET_ITEMS.put(categoryName, categoryItems);
            AdventureUtil.consoleMessage(DebugLevel.DEBUG, "Loaded <green>" + categoryItems.size() + " <gray>items in category <green>" + categoryName);
        } else {
            AdventureUtil.consoleMessage(DebugLevel.DEBUG, "No items loaded for category: " + categoryName);
        }
    }

    private ItemContext loadItemContext(String id, ConfigurationSection section) {
        try {
            if (section == null) {
                AdventureUtil.consoleMessage(DebugLevel.DEBUG, "Configuration section is null for item: " + id);
                return null;
            }

            ItemStack display = ItemUtil.build(id);

            if (display == null) {
                AdventureUtil.consoleMessage(DebugLevel.DEBUG, "Failed to build item display for: " + id);
                return null;
            }

            String displayName = section.getString("name", id);
            double buyPrice = section.getDouble("buy", 0.0);
            double sellPrice = section.getDouble("sell", 0.0);

            formatMarketItemDisplay(display, displayName, buyPrice, sellPrice);

            return new ItemContext(id, display, buyPrice, sellPrice);
        } catch (Exception e) {
            AdventureUtil.consoleMessage(DebugLevel.DEBUG, "Failed to load item context for " + id + ": " + e.getMessage());
            return null;
        }
    }


    private void formatMarketItemDisplay(ItemStack item, String displayName, double buyPrice, double sellPrice) {
        item.editMeta(meta -> {
            meta.setDisplayName(displayName);

            List<String> lore = new ArrayList<>();
            lore.add(" ");
            if (buyPrice > 0) {
                lore.add("<green>Buy: <gold>$" + String.format("%.2f", buyPrice));
            }
            if (sellPrice > 0) {
                lore.add("<red>Sell: <gold>$" + String.format("%.2f", sellPrice));
            }
            lore.add(" ");
            meta.setLore(lore);
        });
    }
}
