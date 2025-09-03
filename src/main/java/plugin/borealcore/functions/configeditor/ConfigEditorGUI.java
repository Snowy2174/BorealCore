package plugin.borealcore.functions.configeditor;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import fr.minuskube.inv.content.Pagination;
import fr.minuskube.inv.content.SlotIterator;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import plugin.borealcore.BorealCore;
import plugin.borealcore.functions.configeditor.ConfigEditorManager.Pair;

import java.io.File;
import java.util.*;
import java.util.function.Consumer;

public class ConfigEditorGUI implements InventoryProvider {

    private final String title;
    private final int rows;
    private Object[] items;
    private Consumer<File> fileClickHandler;
    private ConfigSectionClickHandler configSectionClickHandler;
    private String sectionPath;
    private ConfigurationSection section;
    private Runnable backAction;
    private Runnable saveAction;
    private Map<String, Pair<Object, Object>> changeLog;
    private SmartInventory inventory;

    public ConfigEditorGUI(
            String title,
            int rows,
            File[] files,
            Consumer<File> fileClickHandler,
            Consumer<File> fileRightClickHandler,
            Runnable backAction
    ) {
        this.title = title;
        this.rows = rows;
        this.items = files;
        this.fileClickHandler = fileClickHandler;
        this.backAction = backAction;
    }

    public ConfigEditorGUI(
            String title,
            int rows,
            ConfigurationSection section,
            String sectionPath,
            ConfigSectionClickHandler configSectionClickHandler,
            Runnable backAction,
            Runnable saveAction,
            Map<String, Pair<Object, Object>> changeLog
    ) {
        this.title = title;
        this.rows = rows;
        this.section = section;
        this.sectionPath = sectionPath;
        this.configSectionClickHandler = configSectionClickHandler;
        this.backAction = backAction;
        this.saveAction = saveAction;
        this.changeLog = changeLog;
    }

    public void open(Player player) {
        this.inventory = SmartInventory.builder()
                .provider(this)
                .size(rows, 9)
                .title(title)
                .manager(BorealCore.getInventoryManager())
                .build();

        inventory.open(player);
    }

    public void refresh(Player player) {
        if (inventory != null && player.getOpenInventory() != null) {
            inventory.open(player);
        }
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        Pagination pagination = contents.pagination();

        ItemStack borderItem = createItem(Material.BLACK_STAINED_GLASS_PANE, Component.empty());
        for (int i = 0; i < 9; i++) {
            contents.set(0, i, ClickableItem.empty(borderItem));
            contents.set(rows - 1, i, ClickableItem.empty(borderItem));
        }

        if (backAction != null) {
            contents.set(rows - 1, 0, ClickableItem.of(
                    createItem(Material.ARROW, Component.text("Back").color(NamedTextColor.WHITE)
                            .decoration(TextDecoration.ITALIC, false)),
                    e -> backAction.run()));
        }

        if (saveAction != null) {
            contents.set(rows - 1, 4, ClickableItem.of(
                    createItem(Material.LIME_STAINED_GLASS_PANE, Component.text("Save").color(NamedTextColor.GREEN)
                            .decoration(TextDecoration.ITALIC, false)),
                    e -> saveAction.run()));
        }

        List<ClickableItem> clickableItems = new ArrayList<>();

        if (items != null && items instanceof File[]) {
            Arrays.sort((File[]) items, (f1, f2) -> {
                if (f1.isDirectory() && !f2.isDirectory()) return -1;
                if (!f1.isDirectory() && f2.isDirectory()) return 1;
                return f1.getName().compareTo(f2.getName());
            });

            for (File file : (File[]) items) {
                if (file != null) {
                    ItemStack item = createFileItem(file);
                    clickableItems.add(ClickableItem.of(item, e -> {
                        if (fileClickHandler != null) {
                            fileClickHandler.accept(file);
                        }
                    }));
                }
            }
        } else if (section != null) {
            if (!sectionPath.equals("@root")) {
                ItemStack parentItem = createItem(Material.FEATHER,
                        Component.text("..").color(NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false),
                        Collections.singletonList(Component.text("Go back to parent section").color(NamedTextColor.YELLOW)
                                .decoration(TextDecoration.ITALIC, false)));

                clickableItems.add(ClickableItem.of(parentItem, e -> {
                    if (backAction != null) {
                        backAction.run();
                    }
                }));
            }

            Set<String> keys = section.getKeys(false);
            List<String> sortedKeys = new ArrayList<>(keys);
            sortedKeys.sort((k1, k2) -> {
                boolean isSection1 = section.isConfigurationSection(k1);
                boolean isSection2 = section.isConfigurationSection(k2);
                if (isSection1 && !isSection2) return -1;
                if (!isSection1 && isSection2) return 1;
                return k1.compareTo(k2);
            });

            for (String key : sortedKeys) {
                boolean isSection = section.isConfigurationSection(key);
                Object value = section.get(key);

                String fullPath = sectionPath.equals("@root") ? key : sectionPath + "." + key;
                boolean isModified = changeLog != null && changeLog.containsKey(fullPath);

                ItemStack item = createConfigItem(key, value, isSection, isModified);
                clickableItems.add(ClickableItem.of(item, e -> {
                    if (configSectionClickHandler != null) {
                        configSectionClickHandler.onConfigSectionClick(key, value, isSection, e.isRightClick());
                    }
                }));
            }
        }

        if (!clickableItems.isEmpty()) {
            pagination.setItems(clickableItems.toArray(new ClickableItem[0]));

            int itemsPerPage = (rows - 2) * 9;
            pagination.setItemsPerPage(itemsPerPage);

            pagination.addToIterator(contents.newIterator(SlotIterator.Type.HORIZONTAL, 1, 0)
                    .blacklist(0, 0).blacklist(0, 8)
                    .blacklist(rows - 1, 0).blacklist(rows - 1, 8));

            // Only show pagination controls if needed
            int totalPages = (int) Math.ceil((double) clickableItems.size() / itemsPerPage);

            if (totalPages > 1) {
                if (!pagination.isFirst()) {
                    contents.set(rows - 1, 3, ClickableItem.of(
                            createItem(Material.ARROW, Component.text("Previous Page").color(NamedTextColor.YELLOW)),
                            e -> open(player, pagination.getPage() - 1, contents)));
                }

                if (!pagination.isLast()) {
                    contents.set(rows - 1, 5, ClickableItem.of(
                            createItem(Material.ARROW, Component.text("Next Page").color(NamedTextColor.YELLOW)),
                            e -> open(player, pagination.getPage() + 1, contents)));
                }

                contents.set(rows - 1, 2, ClickableItem.empty(
                        createItem(Material.PAPER,
                                Component.text("Page " + (pagination.getPage() + 1) + "/" + totalPages)
                                        .color(NamedTextColor.GRAY))));
            }
        }
    }

    @Override
    public void update(Player player, InventoryContents contents) {
        // Not needed as we use refresh instead
    }

    private void open(Player player, int page, InventoryContents contents) {
        contents.pagination().page(page);
        player.getOpenInventory().getTopInventory().clear();
        contents.inventory().open(player, page);
    }

    private ItemStack createFileItem(File file) {
        Material material;
        if (file.isDirectory()) {
            material = Material.CHEST;
        } else if (file.getName().endsWith(".yml")) {
            material = Material.PAPER;
        } else {
            material = Material.BARRIER;
        }

        return createItem(material,
                Component.text(file.getName()).color(NamedTextColor.WHITE)
                        .decoration(TextDecoration.ITALIC, false),
                Collections.singletonList(Component.text("Click to open").color(NamedTextColor.YELLOW)
                        .decoration(TextDecoration.ITALIC, false)));
    }

    private ItemStack createConfigItem(String key, Object value, boolean isSection, boolean isModified) {
        Material material = isSection ? Material.CHEST : Material.PAPER;

        Component name = Component.text(key).color(isModified ? NamedTextColor.YELLOW : NamedTextColor.WHITE)
                .decoration(TextDecoration.ITALIC, false);
        if (isModified) {
            name = name.append(Component.text("*").color(NamedTextColor.YELLOW));
        }

        List<Component> lore = new ArrayList<>();

        if (!isSection) {
            lore.add(Component.text("Current value: ").color(NamedTextColor.GREEN)
                    .decoration(TextDecoration.ITALIC, false)
                    .append(Component.text(String.valueOf(value)).color(NamedTextColor.WHITE)
                            .decoration(TextDecoration.ITALIC, false)));

            if (value instanceof Boolean) {
                lore.add(Component.text("Click to toggle").color(NamedTextColor.YELLOW)
                        .decoration(TextDecoration.ITALIC, false));
            } else {
                lore.add(Component.text("Left click to edit").color(NamedTextColor.YELLOW)
                        .decoration(TextDecoration.ITALIC, false));
                lore.add(Component.text("Right click to restore original value").color(NamedTextColor.YELLOW)
                        .decoration(TextDecoration.ITALIC, false));
            }
        } else {
            lore.add(Component.text("Click to browse").color(NamedTextColor.YELLOW)
                    .decoration(TextDecoration.ITALIC, false));
        }

        return createItem(material, name, lore);
    }

    private ItemStack createItem(Material material, Component name) {
        return createItem(material, name, null);
    }

    private ItemStack createItem(Material material, Component name, List<Component> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(name);

        if (lore != null && !lore.isEmpty()) {
            meta.lore(lore);
        }

        item.setItemMeta(meta);
        return item;
    }

    public interface ConfigSectionClickHandler {
        void onConfigSectionClick(String key, Object value, boolean isSection, boolean isRightClick);
    }
}