package plugin.borealcore;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import plugin.borealcore.api.module.BorealGUI;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class ConfigEditorGUI extends BorealGUI {

    private final int rows;
    private Object[] items;
    private Consumer<File> fileClickHandler;
    private ConfigSectionClickHandler configSectionClickHandler;
    private String sectionPath;
    private ConfigurationSection section;
    private Runnable backAction;
    private Runnable saveAction;
    private Map<String, ConfigEditorModule.Pair<Object, Object>> changeLog;

    // Pagination tracking
    private int page = 0;

    // Helper class to bind an item to its click action during generation
    private static class ActionItem {
        final ItemStack item;
        final Consumer<InventoryClickEvent> action;

        ActionItem(ItemStack item, Consumer<InventoryClickEvent> action) {
            this.item = item;
            this.action = action;
        }
    }

    public ConfigEditorGUI(
            String title,
            int rows,
            File[] files,
            Consumer<File> fileClickHandler,
            Consumer<File> fileRightClickHandler,
            Runnable backAction
    ) {
        super(rows, Component.text(title));
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
            Map<String, ConfigEditorModule.Pair<Object, Object>> changeLog
    ) {
        super(rows, Component.text(title));
        this.rows = rows;
        this.section = section;
        this.sectionPath = sectionPath;
        this.configSectionClickHandler = configSectionClickHandler;
        this.backAction = backAction;
        this.saveAction = saveAction;
        this.changeLog = changeLog;
    }

    /**
     * Refreshes the current inventory view for the player.
     */
    public void refresh(Player player) {
        init(player);
    }

    @Override
    public void init(Player player) {
        // Clear all previous items to ensure clean page rendering
        getInventory().clear();

        // 1. Setup Borders
        ItemStack borderItem = createItem(Material.BLACK_STAINED_GLASS_PANE, Component.empty());
        for (int i = 0; i < 9; i++) {
            setItem(0, i, borderItem, null);
            setItem(rows - 1, i, borderItem, null);
        }

        // 2. Setup Persistent Actions (Back / Save)
        if (backAction != null) {
            setItem(rows - 1, 0,
                    createItem(Material.ARROW, Component.text("Back").color(NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false)),
                    e -> backAction.run());
        }

        if (saveAction != null) {
            setItem(rows - 1, 4,
                    createItem(Material.LIME_STAINED_GLASS_PANE, Component.text("Save").color(NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false)),
                    e -> saveAction.run());
        }

        // 3. Generate all clickable items for the central view
        List<ActionItem> clickableItems = new ArrayList<>();

        if (items != null && items instanceof File[]) {
            Arrays.sort((File[]) items, (f1, f2) -> {
                if (f1.isDirectory() && !f2.isDirectory()) return -1;
                if (!f1.isDirectory() && f2.isDirectory()) return 1;
                return f1.getName().compareTo(f2.getName());
            });

            for (File file : (File[]) items) {
                if (file != null) {
                    ItemStack item = createFileItem(file);
                    clickableItems.add(new ActionItem(item, e -> {
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

                clickableItems.add(new ActionItem(parentItem, e -> {
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
                clickableItems.add(new ActionItem(item, e -> {
                    if (configSectionClickHandler != null) {
                        configSectionClickHandler.onConfigSectionClick(key, value, isSection, e.isRightClick());
                    }
                }));
            }
        }

        // 4. Handle Pagination & Rendering Grid Math
        if (!clickableItems.isEmpty()) {
            int itemsPerPage = (rows - 2) * 9; // Available slots inside borders
            int totalPages = (int) Math.ceil((double) clickableItems.size() / itemsPerPage);

            // Ensure current page is valid
            if (page >= totalPages) page = Math.max(0, totalPages - 1);

            int startIndex = page * itemsPerPage;
            int endIndex = Math.min(startIndex + itemsPerPage, clickableItems.size());

            // Place items in grid
            int currentRow = 1;
            int currentCol = 0;

            for (int i = startIndex; i < endIndex; i++) {
                ActionItem guiItem = clickableItems.get(i);
                setItem(currentRow, currentCol, guiItem.item, guiItem.action);

                currentCol++;
                if (currentCol > 8) {
                    currentCol = 0;
                    currentRow++;
                }
            }

            // 5. Draw Pagination Controls
            if (totalPages > 1) {
                if (page > 0) {
                    setItem(rows - 1, 3,
                            createItem(Material.ARROW, Component.text("Previous Page").color(NamedTextColor.YELLOW)),
                            e -> {
                                this.page--;
                                init(player);
                            });
                }

                if (page < totalPages - 1) {
                    setItem(rows - 1, 5,
                            createItem(Material.ARROW, Component.text("Next Page").color(NamedTextColor.YELLOW)),
                            e -> {
                                this.page++;
                                init(player);
                            });
                }

                setItem(rows - 1, 2,
                        createItem(Material.PAPER,
                                Component.text("Page " + (page + 1) + "/" + totalPages).color(NamedTextColor.GRAY)),
                        null);
            }
        }
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

        if (meta != null) {
            meta.displayName(name);
            if (lore != null && !lore.isEmpty()) {
                meta.lore(lore);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    public interface ConfigSectionClickHandler {
        void onConfigSectionClick(String key, Object value, boolean isSection, boolean isRightClick);
    }
}