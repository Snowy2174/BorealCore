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
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerJoinEvent;
import plugin.borealcore.BorealCore;
import plugin.borealcore.functions.titles.TitlesGUI.TitleNode;
import plugin.borealcore.functions.titles.TitlesGUI.SuffixStatus;
import plugin.borealcore.manager.configs.MessageManager;
import plugin.borealcore.object.Function;
import plugin.borealcore.object.SimpleListener;
import plugin.borealcore.utility.AdventureUtil;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class TitlesModule extends Function {

    private final SimpleListener simpleListener;
    private LuckPerms luckPerms;

    // Keep track of active GUIs
    private final Map<UUID, TitlesGUI> activeGuis = new HashMap<>();

    // LuckPerms event listeners
    private EventSubscription<UserDataRecalculateEvent> userDataRecalculateListener;
    private EventSubscription<NodeAddEvent> nodeAddListener;
    private EventSubscription<NodeRemoveEvent> nodeRemoveListener;

    public TitlesModule() {
        this.simpleListener = new SimpleListener(this);
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
                PermissionHolder target = event.getTarget();
                if (target instanceof User) {
                    UUID uuid = ((User) target).getUniqueId();
                    Player player = Bukkit.getPlayer(uuid);
                    if (player != null && player.isOnline()) {
                        Bukkit.getScheduler().runTaskLater(BorealCore.getInstance(), () -> {
                            validatePlayerByUUID(uuid);
                        }, 5L);
                    }
                }
            }
        });

        // Listen for node remove events (mainly to catch inheritance nodes)
        nodeRemoveListener = luckPerms.getEventBus().subscribe(NodeRemoveEvent.class, event -> {
            if (event.isUser() && event.getNode() instanceof InheritanceNode) {
                PermissionHolder target = event.getTarget();
                if (target instanceof User) {
                    UUID uuid = ((User) target).getUniqueId();
                    Player player = Bukkit.getPlayer(uuid);
                    if (player != null && player.isOnline()) {
                        Bukkit.getScheduler().runTaskLater(BorealCore.getInstance(), () -> {
                            validatePlayerByUUID(uuid);
                        }, 5L);
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
        Set<String> availablePrefixes = new HashSet<>();

        user.resolveInheritedNodes(QueryOptions.nonContextual()).stream()
                .filter(NodeType.PREFIX::matches)
                .map(NodeType.PREFIX::cast)
                .forEach(prefixNode -> {
                    availablePrefixes.add(prefixNode.getMetaValue());
                });

        boolean changes = false;
        List<Node> nodesToRemove = new ArrayList<>();

        for (Node node : user.getNodes()) {
            if (NodeType.PREFIX.matches(node)) {
                String prefixValue = NodeType.PREFIX.cast(node).getMetaValue();
                if (!availablePrefixes.contains(prefixValue) && !isSpecialOverride(prefixValue)) {
                    nodesToRemove.add(node);
                    changes = true;
                }
            }
        }

        if (changes) {
            Bukkit.getScheduler().runTask(BorealCore.getInstance(), () -> {
                for (Node node : nodesToRemove) {
                    user.data().remove(node);
                }

                luckPerms.getUserManager().saveUser(user).thenRun(() -> {
                    AdventureUtil.playerMessage(player, MessageManager.infoPositive +
                            "Some of your prefix selections were removed because you no longer have access to them.");

                    TitlesGUI gui = activeGuis.get(player.getUniqueId());
                    if (gui != null) {
                        gui.refresh();
                    }
                });
            });
        }
    }

    private boolean isSpecialOverride(String value) {
        return false;
    }

    private void registerCommand() {
        TitlesCommand titlesCommand = new TitlesCommand(this);
        BorealCore.getInstance().getCommand("titles").setExecutor(titlesCommand);
        BorealCore.getInstance().getCommand("titles").setTabCompleter(titlesCommand);
    }

    public void openTitleScreen(Player player, TitleType titleType) {
        if (luckPerms == null) {
            AdventureUtil.playerMessage(player, MessageManager.infoNegative + "LuckPerms API is not available.");
            return;
        }

        CompletableFuture<User> userFuture = luckPerms.getUserManager().loadUser(player.getUniqueId());

        userFuture.thenAcceptAsync(user -> {
            Bukkit.getScheduler().runTask(BorealCore.getInstance(), () -> {
                TitlesGUI gui = new TitlesGUI(
                        titleType == TitleType.PREFIX ? "Prefix Manager" : "Suffix Manager",
                        6,
                        player,
                        user,
                        titleType,
                        node -> handleTitleNodeClick(player, user, node, titleType),
                        null
                );

                activeGuis.put(player.getUniqueId(), gui);
                gui.open(player);
            });
        }).exceptionally(ex -> {
            AdventureUtil.playerMessage(player, MessageManager.infoNegative + "Failed to load LuckPerms user data.");
            ex.printStackTrace();
            return null;
        });
    }

    private void handleTitleNodeClick(Player player, User user, TitleNode selectedNode, TitleType titleType) {
        TitlesGUI gui = activeGuis.get(player.getUniqueId());
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

    private void handlePrefixClick(Player player, User user, TitleNode selectedNode, TitlesGUI gui) {
        TitleNode currentActive = gui.getActiveNode();
        boolean deactivating = selectedNode.equals(currentActive);

        removeAllUserPrefixNodes(user);

        if (!deactivating) {
            addPrefixNode(user, selectedNode);

            luckPerms.getUserManager().saveUser(user).thenRun(() -> {
                Bukkit.getScheduler().runTask(BorealCore.getInstance(), () -> {
                    AdventureUtil.playerMessage(player, MessageManager.infoPositive +
                            "Prefix set to " + selectedNode.getValue());
                    openTitleScreen(player, TitleType.PREFIX);
                });
            });
        } else {
            luckPerms.getUserManager().saveUser(user).thenRun(() -> {
                Bukkit.getScheduler().runTask(BorealCore.getInstance(), () -> {
                    AdventureUtil.playerMessage(player, MessageManager.infoPositive + "Prefix deactivated");
                    openTitleScreen(player, TitleType.PREFIX);
                });
            });
        }
    }

    private void handleSuffixClick(Player player, User user, TitleNode selectedNode, TitlesGUI gui) {
        SuffixStatus currentStatus = gui.getSuffixStatus(selectedNode);
        List<SuffixNode> allSuffixNodes = gui.getAllSuffixNodes();

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

        Map<SuffixNode, Integer> priorityChanges = new HashMap<>();
        SuffixNode selectedSuffixNode = null;
        for (SuffixNode node : allSuffixNodes) {
            if (node.getMetaValue().equals(selectedNode.getValue())) {
                selectedSuffixNode = node;
                break;
            }
        }

        if (selectedSuffixNode == null) {
            AdventureUtil.playerMessage(player, MessageManager.infoNegative + "Error: Could not find the selected suffix.");
            return;
        }

        SuffixNode currentPrimary = null;
        SuffixNode currentSecondary = null;

        List<SuffixNode> sortedNodes = allSuffixNodes.stream()
                .sorted(Comparator.comparing(SuffixNode::getPriority).reversed())
                .collect(Collectors.toList());

        if (!sortedNodes.isEmpty()) {
            currentPrimary = sortedNodes.get(0);
            if (sortedNodes.size() > 1) {
                currentSecondary = sortedNodes.get(1);
            }
        }

        switch (newStatus) {
            case PRIMARY:
                priorityChanges.put(selectedSuffixNode, TitlesGUI.getMaxPriority());
                if (currentPrimary != null && !currentPrimary.equals(selectedSuffixNode)) {
                    priorityChanges.put(currentPrimary, TitlesGUI.getSecondaryPriority());
                    if (currentSecondary != null && !currentSecondary.equals(selectedSuffixNode)) {
                        priorityChanges.put(currentSecondary, TitlesGUI.getInactivePriority());
                    }
                }
                break;
            case SECONDARY:
                priorityChanges.put(selectedSuffixNode, TitlesGUI.getSecondaryPriority());
                if (currentStatus == SuffixStatus.PRIMARY && currentSecondary != null) {
                    priorityChanges.put(currentSecondary, TitlesGUI.getMaxPriority());
                }
                break;
            case INACTIVE:
                priorityChanges.put(selectedSuffixNode, TitlesGUI.getInactivePriority());
                if (currentStatus == SuffixStatus.PRIMARY && currentSecondary != null) {
                    priorityChanges.put(currentSecondary, TitlesGUI.getMaxPriority());
                }
                break;
        }

        boolean changes = false;
        for (Map.Entry<SuffixNode, Integer> entry : priorityChanges.entrySet()) {
            SuffixNode node = entry.getKey();
            int newPriority = entry.getValue();

            if (node.getPriority() != newPriority) {
                user.data().remove(node);

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
            luckPerms.getUserManager().saveUser(user).thenRun(() -> {
                Bukkit.getScheduler().runTask(BorealCore.getInstance(), () -> {
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
        user.getNodes().stream()
                .filter(NodeType.PREFIX::matches)
                .forEach(node -> user.data().remove(node));
    }

    private void addPrefixNode(User user, TitleNode node) {
        PrefixNode.Builder builder = PrefixNode.builder()
                .priority(TitlesGUI.getMaxPriority())
                .prefix(node.getValue());

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
        Bukkit.getScheduler().runTaskLater(BorealCore.getInstance(), () -> {
            validatePlayerByUUID(event.getPlayer().getUniqueId());
        }, 20L);
    }

    @Override
    public void onQuit(Player player) {
        activeGuis.remove(player.getUniqueId());
    }

    public enum TitleType {
        PREFIX,
        SUFFIX
    }
}