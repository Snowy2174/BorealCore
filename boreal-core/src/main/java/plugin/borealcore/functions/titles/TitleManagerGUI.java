package plugin.borealcore.functions.titles;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import fr.minuskube.inv.content.Pagination;
import fr.minuskube.inv.content.SlotIterator;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.SuffixNode;
import net.luckperms.api.query.QueryOptions;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.checkerframework.checker.nullness.qual.Nullable;
import plugin.borealcore.BorealCore;
import plugin.borealcore.functions.titles.TitleManagerManager.TitleType;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class TitleManagerGUI implements InventoryProvider {

    private final String title;
    private final int rows;
    private final Player player;
    private final User lpUser;
    private final TitleType titleType;
    private final Consumer<TitleNode> nodeClickHandler;
    private final Runnable backAction;
    private SmartInventory inventory;
    private List<TitleNode> titleNodes;
    private TitleNode activeNode;
    private List<TitleNode> activeSuffixNodes; // List to track active suffixes (up to 2)

    // Constants for priorities
    private static final int BASE_OVERRIDE_PRIORITY = 100; // Set overrides much higher than inherited
    private static final int MAX_PRIORITY = 999; // Maximum priority for primary suffix/prefix
    private static final int SECONDARY_SUFFIX_PRIORITY = 998; // Priority for secondary suffix
    private static final int INACTIVE_PRIORITY = 50; // Priority for inactive suffixes

    public TitleManagerGUI(
            String title,
            int rows,
            Player player,
            User lpUser,
            TitleType titleType,
            Consumer<TitleNode> nodeClickHandler,
            Runnable backAction
    ) {
        this.title = title;
        this.rows = rows;
        this.player = player;
        this.lpUser = lpUser;
        this.titleType = titleType;
        this.nodeClickHandler = nodeClickHandler;
        this.backAction = backAction;

        loadAndProcessTitleNodes();
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

    public void refresh() {
        loadAndProcessTitleNodes();
        if (inventory != null && player.getOpenInventory() != null) {
            inventory.open(player);
        }
    }

    private void loadAndProcessTitleNodes() {
        List<TitleNode> allNodes = new ArrayList<>();
        Map<String, TitleNode> valueToNodeMap = new HashMap<>();

        // First, collect nodes based on type
        if (titleType == TitleType.PREFIX) {
            // For prefixes: collect both inherited and direct nodes
            lpUser.resolveInheritedNodes(QueryOptions.nonContextual()).stream()
                    .filter(NodeType.PREFIX::matches)
                    .map(NodeType.PREFIX::cast)
                    .forEach(prefixNode -> {
                        TitleNode node = new TitleNode(
                                prefixNode.getKey(),
                                prefixNode.getMetaValue(),
                                prefixNode.getPriority(),
                                prefixNode.getExpiry(),
                                titleType,
                                prefixNode,
                                isNodeDirectlyAssigned(lpUser, prefixNode)
                        );
                        allNodes.add(node);

                        // Use meta value as key for deduplication
                        valueToNodeMap.put(prefixNode.getMetaValue(), node);
                    });
        } else {
            // For suffixes: only collect directly assigned nodes
            // This change ensures suffixes are treated independently from inheritance
            lpUser.getNodes().stream()
                    .filter(NodeType.SUFFIX::matches)
                    .map(NodeType.SUFFIX::cast)
                    .forEach(suffixNode -> {
                        TitleNode node = new TitleNode(
                                suffixNode.getKey(),
                                suffixNode.getMetaValue(),
                                suffixNode.getPriority(),
                                suffixNode.getExpiry(),
                                titleType,
                                suffixNode,
                                true // Always directly assigned for suffixes
                        );
                        allNodes.add(node);

                        // Use meta value as key for deduplication
                        valueToNodeMap.put(suffixNode.getMetaValue(), node);
                    });
        }

        // Find active node(s) based on priority
        if (titleType == TitleType.PREFIX) {
            this.activeNode = allNodes.stream()
                    .max(Comparator.comparing(TitleNode::getPriority))
                    .orElse(null);

            this.activeSuffixNodes = null; // Not applicable for prefixes
        } else {
            // For suffixes, find the two highest priority nodes
            this.activeSuffixNodes = allNodes.stream()
                    .sorted(Comparator.comparing(TitleNode::getPriority).reversed())
                    .limit(2)
                    .collect(Collectors.toList());

            this.activeNode = activeSuffixNodes.isEmpty() ? null : activeSuffixNodes.get(0);
        }

        // Filter and deduplicate
        if (titleType == TitleType.PREFIX) {
            // For prefixes: show direct assignments or unique values
            this.titleNodes = valueToNodeMap.values().stream()
                    .filter(node -> node.isDirectlyAssigned() || !isDuplicate(node, allNodes))
                    .sorted((n1, n2) -> {
                        // Sort by active status first, then direct assignment, then priority
                        int activeComparison = Boolean.compare(n2.equals(activeNode), n1.equals(activeNode));
                        if (activeComparison != 0) return activeComparison;

                        int directComparison = Boolean.compare(n2.isDirectlyAssigned(), n1.isDirectlyAssigned());
                        if (directComparison != 0) return directComparison;

                        return Integer.compare(n2.getPriority(), n1.getPriority());
                    })
                    .collect(Collectors.toList());
        } else {
            // For suffixes: show all directly assigned suffixes (no filtering needed)
            this.titleNodes = valueToNodeMap.values().stream()
                    .sorted((n1, n2) -> {
                        // Sort by active status first, then priority
                        int activeComparison = Integer.compare(getSuffixStatusValue(n2), getSuffixStatusValue(n1));
                        if (activeComparison != 0) return activeComparison;

                        return Integer.compare(n2.getPriority(), n1.getPriority());
                    })
                    .collect(Collectors.toList());
        }
    }

    private int getSuffixStatusValue(TitleNode node) {
        SuffixStatus status = getSuffixStatus(node);
        switch (status) {
            case PRIMARY: return 2;
            case SECONDARY: return 1;
            default: return 0;
        }
    }

    private boolean isSuffixActive(TitleNode node) {
        if (titleType != TitleType.SUFFIX || activeSuffixNodes == null) {
            return false;
        }
        return activeSuffixNodes.contains(node);
    }

    private boolean isDuplicate(TitleNode node, List<TitleNode> allNodes) {
        // Check if there's a directly assigned node with the same value
        return allNodes.stream()
                .anyMatch(other -> other.isDirectlyAssigned() &&
                        other.getValue().equals(node.getValue()) &&
                        !other.equals(node));
    }

    private boolean isNodeDirectlyAssigned(User user, Node node) {
        return user.getNodes().stream()
                .anyMatch(n -> n.getKey().equals(node.getKey()));
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        Pagination pagination = contents.pagination();

        // Create border
        ItemStack borderItem = createItem(Material.BLACK_STAINED_GLASS_PANE, Component.empty());
        for (int i = 0; i < 9; i++) {
            contents.set(0, i, ClickableItem.empty(borderItem));
            contents.set(rows - 1, i, ClickableItem.empty(borderItem));
        }

        // Add navigation buttons
        if (backAction != null) {
            contents.set(rows - 1, 0, ClickableItem.of(
                    createItem(Material.ARROW, Component.text("Back").color(NamedTextColor.WHITE)
                            .decoration(TextDecoration.ITALIC, false)),
                    e -> backAction.run()));
        }

        // Add toggle button to switch between prefix and suffix view
        contents.set(rows - 1, 4, ClickableItem.of(
                createItem(
                        titleType == TitleType.PREFIX ? Material.NAME_TAG : Material.PAPER,
                        Component.text("Switch to " + (titleType == TitleType.PREFIX ? "Suffix" : "Prefix") + " View")
                                .color(NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false)
                ),
                e -> {
                    TitleManagerManager manager = BorealCore.getTitleManager();
                    manager.openTitleScreen(player, titleType == TitleType.PREFIX ? TitleType.SUFFIX : TitleType.PREFIX);
                }));

        // Create clickable items for title nodes
        List<ClickableItem> clickableItems = new ArrayList<>();

        if (titleNodes.isEmpty()) {
            // Display a message if no titles are available
            ItemStack noTitlesItem = createItem(
                    Material.BARRIER,
                    Component.text("No " + (titleType == TitleType.PREFIX ? "prefixes" : "suffixes") + " available")
                            .color(NamedTextColor.RED).decoration(TextDecoration.ITALIC, false),
                    Collections.singletonList(Component.text("You don't have any " +
                                    (titleType == TitleType.PREFIX ? "prefixes" : "suffixes") + " assigned.")
                            .color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false))
            );
            clickableItems.add(ClickableItem.empty(noTitlesItem));
        } else {
            for (TitleNode node : titleNodes) {
                ItemStack item;
                if (titleType == TitleType.PREFIX) {
                    boolean isActive = node.equals(activeNode);
                    item = createPrefixItem(node, isActive);
                } else {
                    SuffixStatus status = getSuffixStatus(node);
                    item = createSuffixItem(node, status);
                }

                clickableItems.add(ClickableItem.of(item, e -> {
                    if (nodeClickHandler != null) {
                        nodeClickHandler.accept(node);
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

    public SuffixStatus getSuffixStatus(TitleNode node) {
        if (activeSuffixNodes == null || activeSuffixNodes.isEmpty()) {
            return SuffixStatus.INACTIVE;
        }

        if (activeSuffixNodes.size() >= 1 && node.equals(activeSuffixNodes.get(0))) {
            return SuffixStatus.PRIMARY;
        }

        if (activeSuffixNodes.size() >= 2 && node.equals(activeSuffixNodes.get(1))) {
            return SuffixStatus.SECONDARY;
        }

        return SuffixStatus.INACTIVE;
    }

    private void open(Player player, int page, InventoryContents contents) {
        contents.pagination().page(page);
        player.getOpenInventory().getTopInventory().clear();
        contents.inventory().open(player, page);
    }

    private ItemStack createPrefixItem(TitleNode node, boolean isActive) {
        Material material;
        if (isActive) {
            material = Material.LIME_DYE;
        } else if (node.isDirectlyAssigned()) {
            material = Material.LIGHT_BLUE_DYE;
        } else {
            material = Material.GRAY_DYE;
        }

        // Convert Minecraft color codes to Adventure components
        Component name = parseLegacyText(node.getValue());

        // Apply additional formatting based on status
        if (isActive) {
            name = Component.text("▶ ").color(NamedTextColor.GREEN)
                    .decoration(TextDecoration.ITALIC, false)
                    .append(name);
        } else if (node.isDirectlyAssigned()) {
            name = Component.text("● ").color(NamedTextColor.AQUA)
                    .decoration(TextDecoration.ITALIC, false)
                    .append(name);
        }

        List<Component> lore = new ArrayList<>();

        if (isActive) {
            lore.add(Component.text("Status: ").color(NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false)
                    .append(Component.text("Active").color(NamedTextColor.GREEN)));
        } else {
            lore.add(Component.text("Status: ").color(NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false)
                    .append(Component.text("Inactive").color(NamedTextColor.RED)));
        }

        if (node.isDirectlyAssigned()) {
            lore.add(Component.text("Source: ").color(NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false)
                    .append(Component.text("Your Selection").color(NamedTextColor.AQUA)));
        } else {
            lore.add(Component.text("Source: ").color(NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false)
                    .append(Component.text("Group/Rank").color(NamedTextColor.YELLOW)));
        }

        addExpiryToLore(lore, node);

        lore.add(Component.empty());
        lore.add(Component.text("Click to " + (isActive ? "Deactivate" : "Activate")).color(NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, false));

        // Display raw value in lore to help players understand the color codes
        lore.add(Component.empty());
        lore.add(Component.text("Raw value: ").color(NamedTextColor.DARK_GRAY)
                .decoration(TextDecoration.ITALIC, false)
                .append(Component.text(node.getValue()).color(NamedTextColor.GRAY)));

        return createItem(material, name, lore);
    }

    private ItemStack createSuffixItem(TitleNode node, SuffixStatus status) {
        Material material;
        NamedTextColor statusColor;

        switch (status) {
            case PRIMARY:
                material = Material.LIME_DYE;
                statusColor = NamedTextColor.GREEN;
                break;
            case SECONDARY:
                material = Material.YELLOW_DYE;
                statusColor = NamedTextColor.YELLOW;
                break;
            default:
                material = Material.GRAY_DYE;
                statusColor = NamedTextColor.RED;
                break;
        }

        // Convert Minecraft color codes to Adventure components
        Component name = parseLegacyText(node.getValue());

        // Apply additional formatting based on status
        if (status == SuffixStatus.PRIMARY) {
            name = Component.text("1▶ ").color(NamedTextColor.GREEN)
                    .decoration(TextDecoration.ITALIC, false)
                    .append(name);
        } else if (status == SuffixStatus.SECONDARY) {
            name = Component.text("2▶ ").color(NamedTextColor.YELLOW)
                    .decoration(TextDecoration.ITALIC, false)
                    .append(name);
        }

        List<Component> lore = new ArrayList<>();

        // Status line
        String statusText;
        switch (status) {
            case PRIMARY:
                statusText = "Active (Primary)";
                break;
            case SECONDARY:
                statusText = "Active (Secondary)";
                break;
            default:
                statusText = "Inactive";
                break;
        }

        lore.add(Component.text("Status: ").color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false)
                .append(Component.text(statusText).color(statusColor)));

        lore.add(Component.text("Priority: ").color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false)
                .append(Component.text(String.valueOf(node.getPriority())).color(NamedTextColor.GOLD)));

        addExpiryToLore(lore, node);

        lore.add(Component.empty());

        // Click actions text - make this clearer for rotation
        String clickActionText;
        switch (status) {
            case INACTIVE:
                clickActionText = "Click to Make Primary";
                break;
            case PRIMARY:
                clickActionText = "Click to Make Secondary";
                break;
            case SECONDARY:
                clickActionText = "Click to Make Inactive";
                break;
            default:
                clickActionText = "Click to Cycle Status";
                break;
        }

        lore.add(Component.text(clickActionText).color(NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, false));

        // Display raw value in lore to help players understand the color codes
        lore.add(Component.empty());
        lore.add(Component.text("Raw value: ").color(NamedTextColor.DARK_GRAY)
                .decoration(TextDecoration.ITALIC, false)
                .append(Component.text(node.getValue()).color(NamedTextColor.GRAY)));

        return createItem(material, name, lore);
    }

    private void addExpiryToLore(List<Component> lore, TitleNode node) {
        if (node.getExpiry() != null) {
            long expiryMs = node.getExpiry().toEpochMilli() - System.currentTimeMillis();

            if (expiryMs > 0) {
                long days = TimeUnit.MILLISECONDS.toDays(expiryMs);
                long hours = TimeUnit.MILLISECONDS.toHours(expiryMs) % 24;

                lore.add(Component.text("Expires in: ").color(NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false)
                        .append(Component.text(days + "d " + hours + "h").color(NamedTextColor.YELLOW)));
            } else {
                lore.add(Component.text("Expired").color(NamedTextColor.RED)
                        .decoration(TextDecoration.ITALIC, false));
            }
        } else {
            lore.add(Component.text("Permanent").color(NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
        }
    }

    /**
     * Converts legacy color codes (like &a or §a) to Adventure components
     */
    private Component parseLegacyText(String text) {
        // Use Adventure's LegacyComponentSerializer to handle the color codes
        return LegacyComponentSerializer.legacyAmpersand().deserialize(text);
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

    @Override
    public void update(Player player, InventoryContents contents) {
        // Not needed as we use refresh instead
    }

    public static class TitleNode {
        private final String key;
        private final String value;
        private final int priority;
        private final @Nullable Instant expiry; // Can be null for permanent nodes
        private final TitleType type;
        private final Node node;
        private final boolean directlyAssigned;

        public TitleNode(String key, String value, int priority, @Nullable Instant expiry,
                         TitleType type, Node node, boolean directlyAssigned) {
            this.key = key;
            this.value = value;
            this.priority = priority;
            this.expiry = expiry;
            this.type = type;
            this.node = node;
            this.directlyAssigned = directlyAssigned;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }

        public int getPriority() {
            return priority;
        }

        public @Nullable Instant getExpiry() {
            return expiry;
        }

        public TitleType getType() {
            return type;
        }

        public Node getNode() {
            return node;
        }

        public boolean isDirectlyAssigned() {
            return directlyAssigned;
        }

        public boolean isPrefix() {
            return type == TitleType.PREFIX;
        }

        public boolean isSuffix() {
            return type == TitleType.SUFFIX;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            TitleNode other = (TitleNode) obj;
            return value.equals(other.value) && type == other.type;
        }

        @Override
        public int hashCode() {
            return Objects.hash(value, type);
        }
    }

    // Getter for active node (for prefixes)
    public TitleNode getActiveNode() {
        return activeNode;
    }

    // Getter for active suffix nodes (for suffixes)
    public List<TitleNode> getActiveSuffixNodes() {
        return activeSuffixNodes != null ? activeSuffixNodes : Collections.emptyList();
    }

    // Get all directly assigned suffix nodes
    public List<SuffixNode> getAllSuffixNodes() {
        return lpUser.getNodes().stream()
                .filter(NodeType.SUFFIX::matches)
                .map(NodeType.SUFFIX::cast)
                .collect(Collectors.toList());
    }

    // Constants for external use
    public static int getBaseOverridePriority() {
        return BASE_OVERRIDE_PRIORITY;
    }

    public static int getMaxPriority() {
        return MAX_PRIORITY;
    }

    public static int getSecondaryPriority() {
        return SECONDARY_SUFFIX_PRIORITY;
    }

    public static int getInactivePriority() {
        return INACTIVE_PRIORITY;
    }

    // Enum for suffix status
    public enum SuffixStatus {
        INACTIVE,
        PRIMARY,
        SECONDARY
    }
}