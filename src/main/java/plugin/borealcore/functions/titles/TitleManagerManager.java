package plugin.borealcore.functions.titles;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.event.EventSubscription;
import net.luckperms.api.event.node.NodeAddEvent;
import net.luckperms.api.event.node.NodeRemoveEvent;
import net.luckperms.api.event.user.UserDataRecalculateEvent;
import net.luckperms.api.model.PermissionHolder;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.InheritanceNode;
import net.luckperms.api.node.types.PrefixNode;
import net.luckperms.api.node.types.SuffixNode;
import net.luckperms.api.query.QueryOptions;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import plugin.borealcore.BorealCore;
import plugin.borealcore.functions.titles.TitleManagerGUI.TitleNode;
import plugin.borealcore.functions.titles.TitleManagerGUI.SuffixStatus;
import plugin.borealcore.manager.configs.MessageManager;
import plugin.borealcore.object.Function;
import plugin.borealcore.object.SimpleListener;
import plugin.borealcore.utility.AdventureUtil;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class TitleManagerManager extends Function implements CommandExecutor, TabCompleter {

    private final SimpleListener simpleListener;
    private final Permission titleManagerPermission;
    private LuckPerms luckPerms;
    // Keep track of active GUIs
    private final Map<UUID, TitleManagerGUI> activeGuis = new HashMap<>();
    // LuckPerms event listeners
    private EventSubscription<UserDataRecalculateEvent> userDataRecalculateListener;
    private EventSubscription<NodeAddEvent> nodeAddListener;
    private EventSubscription<NodeRemoveEvent> nodeRemoveListener;

    public TitleManagerManager() {
        this.simpleListener = new SimpleListener(this);
        this.titleManagerPermission = new Permission("borealcore.titlemanager", PermissionDefault.TRUE);
    }

    @Override
    public void load() {
        Bukkit.getPluginManager().registerEvents(this.simpleListener, BorealCore.getInstance());
        registerCommand();

        try {
            this.luckPerms = LuckPermsProvider.get();
            setupLuckPermsListeners();
            AdventureUtil.consoleMessage("Title Manager Module Enabled!");
        } catch (Exception e) {
            AdventureUtil.consoleMessage("Failed to initialize LuckPerms API for Title Manager: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void unload() {
        if (this.simpleListener != null) HandlerList.unregisterAll(this.simpleListener);

        // Clean up LuckPerms listeners
        if (userDataRecalculateListener != null) {
            userDataRecalculateListener.close();
        }
        if (nodeAddListener != null) {
            nodeAddListener.close();
        }
        if (nodeRemoveListener != null) {
            nodeRemoveListener.close();
        }

        AdventureUtil.consoleMessage("Title Manager Module Disabled!");
    }

    private void setupLuckPermsListeners() {
        if (luckPerms == null) return;

        // Listen for permission recalculation events (including group changes)
        userDataRecalculateListener = luckPerms.getEventBus().subscribe(UserDataRecalculateEvent.class, event -> {
            UUID uuid = event.getUser().getUniqueId();
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                validatePlayerOverrides(player, event.getUser());
            }
        });

        // Listen for node add events (mainly to catch inheritance nodes)
        nodeAddListener = luckPerms.getEventBus().subscribe(NodeAddEvent.class, event -> {
            if (event.isUser() && event.getNode() instanceof InheritanceNode) {
                // Safely get the UUID from the target
                PermissionHolder target = event.getTarget();
                if (target instanceof User) {
                    UUID uuid = ((User) target).getUniqueId();
                    Player player = Bukkit.getPlayer(uuid);
                    if (player != null && player.isOnline()) {
                        // Schedule a check after the group change is fully processed
                        Bukkit.getScheduler().runTaskLater(BorealCore.getInstance(), () -> {
                            validatePlayerByUUID(uuid);
                        }, 5L); // Short delay to ensure LuckPerms has processed everything
                    }
                }
            }
        });

        // Listen for node remove events (mainly to catch inheritance nodes)
        nodeRemoveListener = luckPerms.getEventBus().subscribe(NodeRemoveEvent.class, event -> {
            if (event.isUser() && event.getNode() instanceof InheritanceNode) {
                // Safely get the UUID from the target
                PermissionHolder target = event.getTarget();
                if (target instanceof User) {
                    UUID uuid = ((User) target).getUniqueId();
                    Player player = Bukkit.getPlayer(uuid);
                    if (player != null && player.isOnline()) {
                        // Schedule a check after the group change is fully processed
                        Bukkit.getScheduler().runTaskLater(BorealCore.getInstance(), () -> {
                            validatePlayerByUUID(uuid);
                        }, 5L); // Short delay to ensure LuckPerms has processed everything
                    }
                }
            }
        });
    }

    private void validatePlayerByUUID(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player != null && player.isOnline()) {
            CompletableFuture<User> userFuture = luckPerms.getUserManager().loadUser(uuid);
            userFuture.thenAcceptAsync(user -> {
                validatePlayerOverrides(player, user);
            });
        }
    }

    private void validatePlayerOverrides(Player player, User user) {
        // Get a list of all available prefixes from groups
        // Note: We're only tracking prefixes now, not suffixes
        Set<String> availablePrefixes = new HashSet<>();

        // Collect all inherited prefixes only (not suffixes)
        user.resolveInheritedNodes(QueryOptions.nonContextual()).stream()
                .filter(NodeType.PREFIX::matches)
                .map(NodeType.PREFIX::cast)
                .forEach(prefixNode -> {
                    availablePrefixes.add(prefixNode.getMetaValue());
                });

        // Check directly assigned nodes to see if any prefixes should be removed
        // Note: We're only checking prefixes, leaving all suffixes alone
        boolean changes = false;
        List<Node> nodesToRemove = new ArrayList<>();

        for (Node node : user.getNodes()) {
            if (NodeType.PREFIX.matches(node)) {
                String prefixValue = NodeType.PREFIX.cast(node).getMetaValue();
                // If this prefix isn't available in any group and it's not a special override
                if (!availablePrefixes.contains(prefixValue) && !isSpecialOverride(prefixValue)) {
                    nodesToRemove.add(node);
                    changes = true;
                }
            }
            // We're no longer checking or removing suffixes based on inheritance
        }

        // Apply changes if needed
        if (changes) {
            Bukkit.getScheduler().runTask(BorealCore.getInstance(), () -> {
                for (Node node : nodesToRemove) {
                    user.data().remove(node);
                }

                luckPerms.getUserManager().saveUser(user).thenRun(() -> {
                    // Notify player if their prefix was removed due to group changes
                    AdventureUtil.playerMessage(player, MessageManager.infoPositive +
                            "Some of your prefix selections were removed because you no longer have access to them.");

                    // Refresh GUI if open
                    TitleManagerGUI gui = activeGuis.get(player.getUniqueId());
                    if (gui != null) {
                        gui.refresh();
                    }
                });
            });
        }
    }

    private boolean isSpecialOverride(String value) {
        // Optional: Add logic here if you have special prefixes/suffixes that should never be removed
        // For example: donor titles, special event titles, etc.
        return false;
    }

    private void registerCommand() {
        BorealCore.getInstance().getCommand("titles").setExecutor(this);
        BorealCore.getInstance().getCommand("titles").setTabCompleter(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            AdventureUtil.consoleMessage(MessageManager.noConsole);
            return true;
        }

        if (!player.hasPermission(titleManagerPermission)) {
            AdventureUtil.playerMessage(player, MessageManager.infoNegative + "You don't have permission to manage titles.");
            return true;
        }

        if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("prefix")) {
                openTitleScreen(player, TitleType.PREFIX);
                return true;
            } else if (args[0].equalsIgnoreCase("suffix")) {
                openTitleScreen(player, TitleType.SUFFIX);
                return true;
            }
        }

        // Default to prefix view
        openTitleScreen(player, TitleType.PREFIX);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player player) || !player.hasPermission(titleManagerPermission)) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            List<String> completions = Arrays.asList("prefix", "suffix");
            return completions.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    public void openTitleScreen(Player player, TitleType titleType) {
        if (luckPerms == null) {
            AdventureUtil.playerMessage(player, MessageManager.infoNegative + "LuckPerms API is not available.");
            return;
        }

        CompletableFuture<User> userFuture = luckPerms.getUserManager().loadUser(player.getUniqueId());

        userFuture.thenAcceptAsync(user -> {
            Bukkit.getScheduler().runTask(BorealCore.getInstance(), () -> {
                // Create the GUI without the direct reference to itself
                TitleManagerGUI gui = new TitleManagerGUI(
                        titleType == TitleType.PREFIX ? "Prefix Manager" : "Suffix Manager",
                        6,
                        player,
                        user,
                        titleType,
                        node -> handleTitleNodeClick(player, user, node, titleType),
                        null
                );

                // Store the GUI for later reference
                activeGuis.put(player.getUniqueId(), gui);

                // Open the GUI
                gui.open(player);
            });
        }).exceptionally(ex -> {
            AdventureUtil.playerMessage(player, MessageManager.infoNegative + "Failed to load LuckPerms user data.");
            ex.printStackTrace();
            return null;
        });
    }

    private void handleTitleNodeClick(Player player, User user, TitleNode selectedNode, TitleType titleType) {
        // Get the GUI from our map
        TitleManagerGUI gui = activeGuis.get(player.getUniqueId());
        if (gui == null) {
            AdventureUtil.playerMessage(player, MessageManager.infoNegative + "Error accessing title manager. Please try again.");
            return;
        }

        if (titleType == TitleType.PREFIX) {
            handlePrefixClick(player, user, selectedNode, gui);
        } else {
            handleSuffixClick(player, user, selectedNode, gui);
        }
    }

    private void handlePrefixClick(Player player, User user, TitleNode selectedNode, TitleManagerGUI gui) {
        TitleNode currentActive = gui.getActiveNode();
        boolean deactivating = selectedNode.equals(currentActive);

        // Remove all existing user-assigned prefix nodes
        removeAllUserPrefixNodes(user);

        // If not deactivating, add the new prefix
        if (!deactivating) {
            addPrefixNode(user, selectedNode);

            // Save and show success message
            luckPerms.getUserManager().saveUser(user).thenRun(() -> {
                Bukkit.getScheduler().runTask(BorealCore.getInstance(), () -> {
                    AdventureUtil.playerMessage(player, MessageManager.infoPositive +
                            "Prefix set to " + selectedNode.getValue());
                    openTitleScreen(player, TitleType.PREFIX);
                });
            });
        } else {
            // Just saving the removal
            luckPerms.getUserManager().saveUser(user).thenRun(() -> {
                Bukkit.getScheduler().runTask(BorealCore.getInstance(), () -> {
                    AdventureUtil.playerMessage(player, MessageManager.infoPositive + "Prefix deactivated");
                    openTitleScreen(player, TitleType.PREFIX);
                });
            });
        }
    }

    private void handleSuffixClick(Player player, User user, TitleNode selectedNode, TitleManagerGUI gui) {
        // COMPLETELY REVISED APPROACH: Focus on priority management instead of node manipulation

        // Get current status of selected node
        SuffixStatus currentStatus = gui.getSuffixStatus(selectedNode);

        // Get all suffix nodes
        List<SuffixNode> allSuffixNodes = gui.getAllSuffixNodes();

        // Determine next state for the selected node
        SuffixStatus newStatus;
        switch (currentStatus) {
            case INACTIVE:
                newStatus = SuffixStatus.PRIMARY;
                break;
            case PRIMARY:
                newStatus = SuffixStatus.SECONDARY;
                break;
            case SECONDARY:
                newStatus = SuffixStatus.INACTIVE;
                break;
            default:
                newStatus = SuffixStatus.INACTIVE;
                break;
        }

        // Map of nodes to their new priorities
        Map<SuffixNode, Integer> priorityChanges = new HashMap<>();

        // Find the selected node in the user's nodes
        SuffixNode selectedSuffixNode = null;
        for (SuffixNode node : allSuffixNodes) {
            if (node.getMetaValue().equals(selectedNode.getValue())) {
                selectedSuffixNode = node;
                break;
            }
        }

        if (selectedSuffixNode == null) {
            // This shouldn't happen since we're displaying existing nodes
            AdventureUtil.playerMessage(player, MessageManager.infoNegative + "Error: Could not find the selected suffix.");
            return;
        }

        // Current primary and secondary nodes (if any)
        SuffixNode currentPrimary = null;
        SuffixNode currentSecondary = null;

        // Find current primary and secondary nodes
        List<SuffixNode> sortedNodes = allSuffixNodes.stream()
                .sorted(Comparator.comparing(SuffixNode::getPriority).reversed())
                .collect(Collectors.toList());

        if (!sortedNodes.isEmpty()) {
            currentPrimary = sortedNodes.get(0);
            if (sortedNodes.size() > 1) {
                currentSecondary = sortedNodes.get(1);
            }
        }

        // Handle priority changes based on the new status
        switch (newStatus) {
            case PRIMARY:
                // Make selected node primary
                priorityChanges.put(selectedSuffixNode, TitleManagerGUI.getMaxPriority());

                // If there was a primary, make it secondary
                if (currentPrimary != null && !currentPrimary.equals(selectedSuffixNode)) {
                    priorityChanges.put(currentPrimary, TitleManagerGUI.getSecondaryPriority());

                    // If there was a secondary, make it inactive
                    if (currentSecondary != null && !currentSecondary.equals(selectedSuffixNode)) {
                        priorityChanges.put(currentSecondary, TitleManagerGUI.getInactivePriority());
                    }
                }
                break;

            case SECONDARY:
                // Make selected node secondary
                priorityChanges.put(selectedSuffixNode, TitleManagerGUI.getSecondaryPriority());

                // If selected was primary, promote secondary to primary if it exists
                if (currentStatus == SuffixStatus.PRIMARY && currentSecondary != null) {
                    priorityChanges.put(currentSecondary, TitleManagerGUI.getMaxPriority());
                }
                break;

            case INACTIVE:
                // Make selected node inactive
                priorityChanges.put(selectedSuffixNode, TitleManagerGUI.getInactivePriority());

                // If selected was primary, promote secondary to primary if it exists
                if (currentStatus == SuffixStatus.PRIMARY && currentSecondary != null) {
                    priorityChanges.put(currentSecondary, TitleManagerGUI.getMaxPriority());
                }
                // If selected was secondary, nothing else to adjust
                break;
        }

        // Apply all priority changes by recreating nodes with new priorities
        boolean changes = false;
        for (Map.Entry<SuffixNode, Integer> entry : priorityChanges.entrySet()) {
            SuffixNode node = entry.getKey();
            int newPriority = entry.getValue();

            // Only update if priority is actually changing
            if (node.getPriority() != newPriority) {
                // Remove old node
                user.data().remove(node);

                // Create new node with updated priority
                SuffixNode.Builder builder = SuffixNode.builder()
                        .priority(newPriority)
                        .suffix(node.getMetaValue());

                if (node.getExpiry() != null) {
                    builder.expiry(node.getExpiry());
                }

                SuffixNode newNode = builder.build();
                user.data().add(newNode);
                changes = true;
            }
        }

        if (changes) {
            // Save changes
            luckPerms.getUserManager().saveUser(user).thenRun(() -> {
                Bukkit.getScheduler().runTask(BorealCore.getInstance(), () -> {
                    // Determine message based on new status
                    String message;
                    switch (newStatus) {
                        case PRIMARY:
                            message = "Primary suffix set to " + selectedNode.getValue();
                            break;
                        case SECONDARY:
                            message = "Secondary suffix set to " + selectedNode.getValue();
                            break;
                        case INACTIVE:
                            message = "Suffix " + selectedNode.getValue() + " set to inactive";
                            break;
                        default:
                            message = "Suffix status updated";
                            break;
                    }

                    AdventureUtil.playerMessage(player, MessageManager.infoPositive + message);
                    openTitleScreen(player, TitleType.SUFFIX);
                });
            });
        } else {
            // No changes were needed
            AdventureUtil.playerMessage(player, MessageManager.infoNegative + "No changes were made to your suffix status.");
            openTitleScreen(player, TitleType.SUFFIX);
        }
    }

    private List<SuffixNode> getUserSuffixNodes(User user) {
        List<SuffixNode> suffixNodes = new ArrayList<>();

        user.getNodes().stream()
                .filter(NodeType.SUFFIX::matches)
                .map(NodeType.SUFFIX::cast)
                .forEach(suffixNodes::add);

        return suffixNodes;
    }

    private void removeAllUserPrefixNodes(User user) {
        // Find and remove all directly assigned prefix nodes
        user.getNodes().stream()
                .filter(NodeType.PREFIX::matches)
                .forEach(node -> user.data().remove(node));
    }

    private void addPrefixNode(User user, TitleNode node) {
        // Create a new prefix node with maximum priority
        PrefixNode.Builder builder = PrefixNode.builder()
                .priority(TitleManagerGUI.getMaxPriority())
                .prefix(node.getValue());

        // Add a default expiry of 1 year if not specified
        if (node.getExpiry() != null) {
            builder.expiry(node.getExpiry());
        } else {
            builder.expiry(Duration.of(365, ChronoUnit.DAYS));
        }

        PrefixNode newNode = builder.build();
        user.data().add(newNode);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Validate player overrides when they join
        Bukkit.getScheduler().runTaskLater(BorealCore.getInstance(), () -> {
            validatePlayerByUUID(event.getPlayer().getUniqueId());
        }, 20L); // Slight delay to ensure all permissions are loaded
    }

    @Override
    public void onQuit(Player player) {
        // Clean up the GUI references when player quits
        activeGuis.remove(player.getUniqueId());
    }

    public enum TitleType {
        PREFIX,
        SUFFIX
    }
}