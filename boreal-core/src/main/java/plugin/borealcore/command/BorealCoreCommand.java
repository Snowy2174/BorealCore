package plugin.borealcore.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import plugin.borealcore.BorealCore;
import plugin.borealcore.manager.ConfigManager;
import plugin.borealcore.manager.MessageManager;
import plugin.borealcore.utility.DebugLevel;
import plugin.borealcore.utility.AdventureUtil;
import plugin.borealcore.utility.CommandUtil;
import plugin.borealcore.utility.ItemUtil;

public class BorealCoreCommand {

    public LiteralCommandNode<CommandSourceStack> buildCommandNode() {
        return Commands.literal("borealcore")
                .requires(src -> src.getSender().hasPermission("borealcore.admin"))
                // /borealcore give <player> <item> [<amount>]
                .then(Commands.literal("give")
                        .then(Commands.argument("player", StringArgumentType.word())
                                .suggests(CommandUtil::suggestOnlinePlayers)
                                .then(Commands.argument("item", StringArgumentType.word())
                                        .executes(ctx -> handleGiveItemCommand(ctx, false))
                                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                                .executes(ctx -> handleGiveItemCommand(ctx, true))
                                        )
                                )
                        )
                )
                // /borealcore clear <player> <item> [<amount>]
                .then(Commands.literal("clear")
                        .then(Commands.argument("player", StringArgumentType.word())
                                .suggests(CommandUtil::suggestOnlinePlayers)
                                .then(Commands.argument("item", StringArgumentType.word())
                                        .executes(ctx -> handleClearCommand(ctx, false))
                                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                                .executes(ctx -> handleClearCommand(ctx, true))
                                        )
                                )
                        )
                )
                // /borealcore purge <player>
                .then(Commands.literal("purge")
                        .then(Commands.argument("player", StringArgumentType.word())
                                .suggests(CommandUtil::suggestOnlinePlayers)
                                .executes(this::handlePurgeCommand)
                        )
                )
                // /borealcore reload
                .then(Commands.literal("reload")
                        .executes(this::handleReloadCommand)
                )

                // /borealcore debugLevel <level>
                .then(Commands.literal("debugLevel")
                        .then(Commands.argument("level", StringArgumentType.word())
                                .suggests((ctx, builder) -> {
                                    for (DebugLevel level : DebugLevel.values()) {
                                        builder.suggest(level.name());
                                    }
                                    return builder.buildFuture();
                                })
                                .executes(this::handleDebugLevelCommand)
                        )
                )

                .executes(ctx -> {
                    AdventureUtil.sendMessage(ctx.getSource().getSender(), MessageManager.infoNegative + "Usage: /borealcore <subcommand>");
                    return Command.SINGLE_SUCCESS;
                })
                .build();
    }

    private int handleGiveItemCommand(CommandContext<CommandSourceStack> ctx, boolean hasAmount) {
        CommandSender sender = ctx.getSource().getSender();
        String targetPlayer = StringArgumentType.getString(ctx, "player");
        Player player = Bukkit.getPlayer(targetPlayer);

        if (player == null) {
            AdventureUtil.sendMessage(sender, MessageManager.infoNegative + MessageManager.playerNotExist);
            return Command.SINGLE_SUCCESS;
        }

        String itemName = StringArgumentType.getString(ctx, "item");
        int amount = hasAmount ? IntegerArgumentType.getInteger(ctx, "amount") : 1;

        ItemUtil.giveItem(player, itemName, amount, true);
        AdventureUtil.sendMessage(sender, "Gave " + amount + " " + itemName + " to " + player.getName());

        return Command.SINGLE_SUCCESS;
    }

    private int handleClearCommand(CommandContext<CommandSourceStack> ctx, boolean hasAmount) {
        CommandSender sender = ctx.getSource().getSender();
        String targetPlayer = StringArgumentType.getString(ctx, "player");
        Player player = Bukkit.getPlayer(targetPlayer);

        if (player == null) {
            AdventureUtil.sendMessage(sender, MessageManager.infoNegative + MessageManager.playerNotExist);
            return Command.SINGLE_SUCCESS;
        }

        String itemName = StringArgumentType.getString(ctx, "item");
        int amount = hasAmount ? IntegerArgumentType.getInteger(ctx, "amount") : 1;

        ItemUtil.removeItem(player.getInventory(), itemName, amount);
        AdventureUtil.sendMessage(sender, "Cleared " + amount + " of " + itemName + " from " + player.getName());

        return Command.SINGLE_SUCCESS;
    }

    private int handlePurgeCommand(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();
        String targetPlayer = StringArgumentType.getString(ctx, "player");
        Player player = Bukkit.getPlayer(targetPlayer);

        if (player == null) {
            AdventureUtil.sendMessage(sender, MessageManager.infoNegative + MessageManager.playerNotExist);
            return Command.SINGLE_SUCCESS;
        }

        // @TODO Yeah bud, gotta move this to jade module
        AdventureUtil.sendMessage(sender, "Purged all jade data for player: " + player.getName());
        AdventureUtil.sendMessage(sender, "Or well, would have if this was connected to the jade module. This is just a placeholder for now.");
        return Command.SINGLE_SUCCESS;
    }

    private int handleReloadCommand(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();
        long startTime = System.currentTimeMillis();

        BorealCore.reload();

        AdventureUtil.sendMessage(sender, MessageManager.prefix + MessageManager.reload.replace("{time}", String.valueOf(System.currentTimeMillis() - startTime)));
        if (ConfigManager.debugLevel == DebugLevel.DEBUG) {
            AdventureUtil.sendMessage(sender, MessageManager.prefix + "Debug Level: <green>" + ConfigManager.debugLevel);
        }
        return Command.SINGLE_SUCCESS;
    }

    private int handleDebugLevelCommand(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();
        String levelArg = StringArgumentType.getString(ctx, "level");

        try {
            DebugLevel debugLevel = DebugLevel.valueOf(levelArg.toUpperCase());
            if (ConfigManager.debugLevel != debugLevel) {
                ConfigManager.setDebugLevel(debugLevel);
                AdventureUtil.sendMessage(sender, MessageManager.prefix + "Debug level set to <green>" + debugLevel);
            } else {
                AdventureUtil.sendMessage(sender, MessageManager.prefix + "Debug level is already set to <green>" + debugLevel);
            }
        } catch (IllegalArgumentException e) {
            AdventureUtil.sendMessage(sender, MessageManager.infoNegative + "Invalid debug level. Use: INFO, DEBUG");
        }

        return Command.SINGLE_SUCCESS;
    }
}