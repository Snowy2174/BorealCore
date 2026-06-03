package plugin.borealcore.jade;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.node.Node;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import plugin.borealcore.jade.config.JadeDatabase;
import plugin.borealcore.jade.config.JadeMessage;
import plugin.borealcore.jade.object.Leaderboard;
import plugin.borealcore.jade.object.LeaderboardEntry;
import plugin.borealcore.jade.object.LeaderboardType;
import plugin.borealcore.object.DebugLevel;
import plugin.borealcore.manager.MessageManager;
import plugin.borealcore.utility.AdventureUtil;

import java.util.concurrent.CompletableFuture;

import static plugin.borealcore.jade.JadeModule.reconcileJadeData;

public class JadeCommand {

    private final JadeModule jadeModule;
    private final JadeDatabase database;

    public JadeCommand(JadeModule jadeModule) {
        this.jadeModule = jadeModule;
        this.database = JadeModule.getDatabase();
    }

    /**
     * Builds the Brigadier command tree with subcommands, aliases, and redirects.
     */
    public LiteralCommandNode<CommandSourceStack> buildCommandNode() {

        // Node: /jade leaderboard
        LiteralCommandNode<CommandSourceStack> leaderboardNode = Commands.literal("leaderboard")
                .executes(ctx -> handleLeaderboardCommand(ctx, "CURRENT", 1))
                .then(Commands.argument("type", StringArgumentType.word())
                        .suggests(this::suggestLeaderboardTypes)
                        .executes(ctx -> handleLeaderboardCommand(ctx, StringArgumentType.getString(ctx, "type"), 1))
                        .then(Commands.argument("page", IntegerArgumentType.integer(1))
                                .executes(ctx -> handleLeaderboardCommand(ctx,
                                        StringArgumentType.getString(ctx, "type"),
                                        IntegerArgumentType.getInteger(ctx, "page")))))
                .build();

        // Node: /jade give (Root level admin command)
        LiteralCommandNode<CommandSourceStack> giveNode = Commands.literal("give")
                .requires(src -> src.getSender().hasPermission("borealcore.admin"))
                .then(Commands.argument("player", StringArgumentType.word())
                        .suggests(this::suggestOnlinePlayers)
                        .then(Commands.argument("amount", IntegerArgumentType.integer())
                                .executes(ctx -> handleGiveJade(ctx, StringArgumentType.getString(ctx, "player"), "", IntegerArgumentType.getInteger(ctx, "amount")))
                                .then(Commands.argument("source", StringArgumentType.word())
                                        .suggests(this::suggestJadeSources)
                                        .executes(ctx -> handleGiveJade(ctx, StringArgumentType.getString(ctx, "player"), StringArgumentType.getString(ctx, "source"), IntegerArgumentType.getInteger(ctx, "amount"))))))
                .build();

        LiteralCommandNode<CommandSourceStack> removeNode = Commands.literal("remove")
                .then(Commands.argument("player", StringArgumentType.word())
                        .suggests(this::suggestOnlinePlayers)
                        .then(Commands.argument("amount", IntegerArgumentType.integer())
                                .executes(ctx -> handleRemoveJade(ctx, StringArgumentType.getString(ctx, "player"), IntegerArgumentType.getInteger(ctx, "amount"), ""))
                                .then(Commands.argument("source", StringArgumentType.word())
                                        .suggests(this::suggestJadeSources)
                                        .executes(ctx -> handleRemoveJade(ctx, StringArgumentType.getString(ctx, "player"), IntegerArgumentType.getInteger(ctx, "amount"), StringArgumentType.getString(ctx, "source"))))))
                .build();

        LiteralCommandNode<CommandSourceStack> setNode = Commands.literal("set")
                .then(Commands.argument("player", StringArgumentType.word())
                        .suggests(this::suggestOnlinePlayers)
                        .then(Commands.argument("amount", IntegerArgumentType.integer())
                                .executes(ctx -> handleSetJade(ctx, StringArgumentType.getString(ctx, "player"), IntegerArgumentType.getInteger(ctx, "amount"), ""))
                                .then(Commands.argument("source", StringArgumentType.word())
                                        .suggests(this::suggestJadeSources)
                                        .executes(ctx -> handleSetJade(ctx, StringArgumentType.getString(ctx, "player"), IntegerArgumentType.getInteger(ctx, "amount"), StringArgumentType.getString(ctx, "source"))))))
                .build();

        LiteralCommandNode<CommandSourceStack> adminNode = Commands.literal("admin")
                .requires(src -> src.getSender().hasPermission("borealcore.admin"))

                .then(removeNode)
                .then(setNode)

                // /jade admin give -> redirects to /jade give
                .then(Commands.literal("give").redirect(giveNode))
                // /jade admin take -> redirects to /jade admin remove
                .then(Commands.literal("take").redirect(removeNode))

                .then(Commands.literal("reset")
                        .then(Commands.argument("player", StringArgumentType.word())
                                .suggests(this::suggestOnlinePlayers)
                                .executes(ctx -> handleSetJade(ctx, StringArgumentType.getString(ctx, "player"), 0, "reset"))))

                .then(Commands.literal("totalJadeForPlayer")
                        .then(Commands.argument("player", StringArgumentType.word())
                                .suggests(this::suggestOnlinePlayers)
                                .executes(ctx -> handleTotalJadeForPlayerCommand(ctx, StringArgumentType.getString(ctx, "player")))))

                .then(Commands.literal("totalJadeForSource")
                        .then(Commands.argument("source", StringArgumentType.word())
                                .suggests(this::suggestJadeSources)
                                .executes(ctx -> handleTotalJadeForSourceCommand(ctx, StringArgumentType.getString(ctx, "source")))))

                .then(Commands.literal("getMostRecent")
                        .then(Commands.argument("player", StringArgumentType.word())
                                .suggests(this::suggestOnlinePlayers)
                                .executes(ctx -> handleGetMostRecent(ctx, StringArgumentType.getString(ctx, "player"), ""))
                                .then(Commands.argument("source", StringArgumentType.word())
                                        .suggests(this::suggestJadeSources)
                                        .executes(ctx -> handleGetMostRecent(ctx, StringArgumentType.getString(ctx, "player"), StringArgumentType.getString(ctx, "source"))))))

                .then(Commands.literal("getPlayerData")
                        .then(Commands.argument("player", StringArgumentType.word())
                                .suggests(this::suggestOnlinePlayers)
                                .executes(ctx -> handleGetPlayerData(ctx, StringArgumentType.getString(ctx, "player")))))

                .then(Commands.literal("verifyAndFixTotals").executes(this::handleVerifyAndFixTotals))
                .then(Commands.literal("reconsile").executes(this::handleReconcileCommand))
                .executes(ctx -> {
                    AdventureUtil.sendMessage(ctx.getSource().getSender(), MessageManager.infoNegative + "Usage: /jade admin <subcommand>");
                    return Command.SINGLE_SUCCESS;
                })
                .build();

        return Commands.literal("jade")
                // Player Commands
                .then(Commands.literal("limits").executes(this::handleLimitsCommand))
                .then(Commands.literal("balance").executes(this::handleBalanceCommand))

                .then(Commands.literal("toggle")
                        .then(Commands.literal("announcements").executes(ctx -> handleAnnoucementPreferenceCommand(ctx, "announcements")))
                        .then(Commands.literal("notifications").executes(ctx -> handleAnnoucementPreferenceCommand(ctx, "notifications"))))
                .then(leaderboardNode)
                .then(Commands.literal("top").redirect(leaderboardNode))

                // Admin Paths
                .then(giveNode) // Root level /jade give
                .then(adminNode) // /jade admin ...
                .executes(ctx -> {
                    AdventureUtil.sendMessage(ctx.getSource().getSender(), MessageManager.infoNegative + "Usage: /jade <subcommand>");
                    return Command.SINGLE_SUCCESS;
                })
                .build();
    }

    // Suggestion Providers

    private CompletableFuture<Suggestions> suggestOnlinePlayers(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        String remaining = builder.getRemaining().toLowerCase();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getName().toLowerCase().startsWith(remaining)) {
                builder.suggest(player.getName());
            }
        }
        return builder.buildFuture();
    }

    private CompletableFuture<Suggestions> suggestJadeSources(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        String remaining = builder.getRemaining().toLowerCase();
        for (String source : JadeModule.jadeSources.keySet()) {
            if (source.toLowerCase().startsWith(remaining)) {
                builder.suggest(source);
            }
        }
        return builder.buildFuture();
    }

    private CompletableFuture<Suggestions> suggestLeaderboardTypes(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        String remaining = builder.getRemaining().toLowerCase();
        for (LeaderboardType type : LeaderboardType.values()) {
            if (type.name().toLowerCase().startsWith(remaining)) {
                builder.suggest(type.name());
            }
        }
        return builder.buildFuture();
    }

    // Execution Handlers

    private int handleAnnoucementPreferenceCommand(CommandContext<CommandSourceStack> ctx, String type) {
        CommandSender sender = ctx.getSource().getSender();
        if (!(sender instanceof Player player)) {
            AdventureUtil.sendMessage(sender, MessageManager.infoNegative + MessageManager.playerNotExist);
            return Command.SINGLE_SUCCESS;
        }
        LuckPerms api = LuckPermsProvider.get();
            api.getUserManager().modifyUser(player.getUniqueId(), user -> {
                Node node = Node.builder("jade." + type).build();
                boolean currentStatus = user.getCachedData().getPermissionData().checkPermission("jade." + type).asBoolean();
                AdventureUtil.consoleMessage(DebugLevel.DEBUG, MessageManager.infoPositive + "Current announcement status for " + player.getName() + ": " + currentStatus);
                if (currentStatus) {
                    user.data().remove(node);
                    AdventureUtil.sendMessage(player, MessageManager.infoPositive + "Jade " + type + " disabled.");
                } else {
                    user.data().add(node);
                    AdventureUtil.sendMessage(player, MessageManager.infoPositive + "Jade " + type + " enabled.");
                }
            });
        return Command.SINGLE_SUCCESS;
    }

    private int handleLimitsCommand(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();
        if (!(sender instanceof Player player)) {
            AdventureUtil.sendMessage(sender, MessageManager.infoNegative + MessageManager.playerNotExist);
            return Command.SINGLE_SUCCESS;
        }
        JadeModule.sendJadeLimitMessage(player);
        return Command.SINGLE_SUCCESS;
    }

    private int handleBalanceCommand(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();
        if (!(sender instanceof Player player)) {
            AdventureUtil.sendMessage(sender, MessageManager.infoNegative + MessageManager.playerNotExist);
            return Command.SINGLE_SUCCESS;
        }
        database.getJadeForPlayerAsync(player, jade -> {
            AdventureUtil.sendMessage(sender, MessageManager.infoPositive + "Total jade: " + jade);
        });
        return Command.SINGLE_SUCCESS;
    }

    private int handleLeaderboardCommand(CommandContext<CommandSourceStack> ctx, String typeStr, int page) {
        CommandSender sender = ctx.getSource().getSender();
        LeaderboardType type;

        try {
            type = LeaderboardType.valueOf(typeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            AdventureUtil.sendMessage(sender, MessageManager.infoNegative + "Invalid leaderboard type");
            return Command.SINGLE_SUCCESS;
        }

        Leaderboard leaderboard = database.getLeaderboard(type);
        int entriesPerPage = 5;
        int totalEntries = leaderboard.getEntries().size();
        int totalPages = Math.max(1, (int) Math.ceil((double) totalEntries / entriesPerPage));

        if (page < 1 || page > totalPages) {
            AdventureUtil.sendMessage(sender, MessageManager.infoNegative + "Page out of range. Total pages: " + totalPages);
            return Command.SINGLE_SUCCESS;
        }

        AdventureUtil.sendMessage(sender, JadeMessage.leaderboardHeader
                .replace("{type}", type.toString())
                .replace("{page}", String.valueOf(page))
                .replace("{totalPages}", String.valueOf(totalPages)));

        leaderboard.getEntries().stream()
                .skip((long) (page - 1) * entriesPerPage)
                .limit(entriesPerPage)
                .forEach(entry -> AdventureUtil.sendMessage(sender, JadeMessage.leaderboardEntry
                        .replace("{player}", entry.getPlayerName())
                        .replace("{position}", String.valueOf(entry.getPosition()))
                        .replace("{score}", String.valueOf(entry.getTotalAmount()))));

        AdventureUtil.sendMessage(sender, JadeMessage.leaderboardFooter);
        AdventureUtil.sendMessage(sender, MessageManager.infoPositive + "Your position: " + leaderboard.getEntries()
                .stream()
                .filter(entry -> entry.getPlayerName().equalsIgnoreCase(sender.getName()))
                .findFirst()
                .map(LeaderboardEntry::getPosition)
                .orElse(0));

        return Command.SINGLE_SUCCESS;
    }

    // Admin Handlers

    private int handleGiveJade(CommandContext<CommandSourceStack> ctx, String playerName, String source, int amount) {
        CommandSender sender = ctx.getSource().getSender();
        Player player = Bukkit.getPlayer(playerName);

        if (player == null) {
            AdventureUtil.sendMessage(sender, MessageManager.infoNegative + MessageManager.playerNotExist);
            return Command.SINGLE_SUCCESS;
        }

        JadeModule.giveJadeCommand(player, source, amount);
        AdventureUtil.sendMessage(sender, "Gave " + amount + " from " + (source.isEmpty() ? "unknown" : source) + " to " + player.getName());
        return Command.SINGLE_SUCCESS;
    }

    private int handleRemoveJade(CommandContext<CommandSourceStack> ctx, String playerName, int amount, String source) {
        CommandSender sender = ctx.getSource().getSender();
        Player player = Bukkit.getPlayer(playerName);

        if (player == null) {
            AdventureUtil.sendMessage(sender, MessageManager.infoNegative + MessageManager.playerNotExist);
            return Command.SINGLE_SUCCESS;
        }

        JadeModule.remove(player, amount, source);
        AdventureUtil.sendMessage(sender, MessageManager.infoPositive + "Removed " + amount + " from " + (source.isEmpty() ? "unknown" : source) + " from " + player.getName());
        return Command.SINGLE_SUCCESS;
    }

    private int handleSetJade(CommandContext<CommandSourceStack> ctx, String playerName, int amount, String source) {
        CommandSender sender = ctx.getSource().getSender();
        Player player = Bukkit.getPlayer(playerName);

        if (player == null) {
            AdventureUtil.sendMessage(sender, MessageManager.infoNegative + MessageManager.playerNotExist);
            return Command.SINGLE_SUCCESS;
        }

        JadeModule.setBalance(player, amount, source);

        if ("reset".equalsIgnoreCase(source)) {
            AdventureUtil.sendMessage(sender, MessageManager.infoPositive + "Reset balance for " + player.getName() + " to 0");
        } else {
            AdventureUtil.sendMessage(sender, MessageManager.infoPositive + "Set balance of " + player.getName() + " to " + amount + (source.isEmpty() ? "" : " (" + source + ")"));
        }
        return Command.SINGLE_SUCCESS;
    }

    private int handleTotalJadeForPlayerCommand(CommandContext<CommandSourceStack> ctx, String playerName) {
        CommandSender sender = ctx.getSource().getSender();
        Player player = Bukkit.getPlayer(playerName);

        if (player == null) {
            AdventureUtil.sendMessage(sender, MessageManager.infoNegative + MessageManager.playerNotExist);
            return Command.SINGLE_SUCCESS;
        }

        database.getJadeForPlayerAsync(player, jade -> {
            AdventureUtil.sendMessage(sender, "Total jade for " + player.getName() + ": " + jade);
        });
        return Command.SINGLE_SUCCESS;
    }

    private int handleTotalJadeForSourceCommand(CommandContext<CommandSourceStack> ctx, String source) {
        CommandSender sender = ctx.getSource().getSender();
        int totalJade = database.getTotalJadeFromSource(source);
        AdventureUtil.sendMessage(sender, "Total jade for source " + source + ": " + totalJade);
        return Command.SINGLE_SUCCESS;
    }

    private int handleGetMostRecent(CommandContext<CommandSourceStack> ctx, String playerName, String source) {
        CommandSender sender = ctx.getSource().getSender();
        Player player = Bukkit.getPlayer(playerName);

        if (player == null) {
            AdventureUtil.sendMessage(sender, MessageManager.infoNegative + MessageManager.playerNotExist);
            return Command.SINGLE_SUCCESS;
        }

        AdventureUtil.sendMessage(sender, "Most recent transaction for " + player.getName() + ": " + database.getRecentPositiveTransactionTimestamps(player, source));
        return Command.SINGLE_SUCCESS;
    }

    private int handleGetPlayerData(CommandContext<CommandSourceStack> ctx, String playerName) {
        CommandSender sender = ctx.getSource().getSender();
        Player player = Bukkit.getPlayer(playerName);

        if (player == null) {
            AdventureUtil.sendMessage(sender, MessageManager.infoNegative + MessageManager.playerNotExist);
            return Command.SINGLE_SUCCESS;
        }

        database.getJadeForPlayerAsync(player, jade -> {
            AdventureUtil.sendMessage(sender, "Total jade for " + player.getName() + ": " + jade);
        });
        return Command.SINGLE_SUCCESS;
    }

    private int handleVerifyAndFixTotals(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();
        database.verifyAndFixTotals();
        AdventureUtil.sendMessage(sender, "Jade totals verified and fixed");
        return Command.SINGLE_SUCCESS;
    }

    private int handleReconcileCommand(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();
        for (Player p : Bukkit.getOnlinePlayers()) {
            reconcileJadeData(p);
            AdventureUtil.sendMessage(sender, MessageManager.infoPositive + "Reconciled jade data for " + p.getName());
            return Command.SINGLE_SUCCESS;
        }
        return Command.SINGLE_SUCCESS;
    }
}