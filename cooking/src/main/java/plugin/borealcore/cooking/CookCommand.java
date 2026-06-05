package plugin.borealcore.cooking;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import plugin.borealcore.cooking.competition.Competition;
import plugin.borealcore.cooking.competition.CompetitionSchedule;
import plugin.borealcore.cooking.configs.CookingMessage;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import plugin.borealcore.manager.GuiManager;
import plugin.borealcore.manager.MessageManager;
import plugin.borealcore.utility.AdventureUtil;
import plugin.borealcore.utility.CommandUtil;

import java.util.List;
import java.util.Set;

import static plugin.borealcore.cooking.configs.RecipeManager.COOKING_RECIPES;

public class CookCommand {

    private final CookingModule cookingModule;

    public CookCommand(CookingModule cookingModule) {
        this.cookingModule = cookingModule;
    }

    public LiteralCommandNode<CommandSourceStack> buildCommandNode() {
        return Commands.literal("plugin/borealcore/cooking")
                .executes(ctx -> showStatsCommand(ctx.getSource().getSender()))
                .requires(src -> src.getSender().hasPermission("borealcore.admin"))

                .then(Commands.literal("cook")
                        .then(Commands.argument("recipe", StringArgumentType.word())
                                .suggests((ctx, builder) -> {
                                    Set<String> recipes = COOKING_RECIPES.keySet();
                                    recipes.forEach(builder::suggest);
                                    return builder.buildFuture();
                                })
                                .then(Commands.argument("player", StringArgumentType.word())
                                        .suggests(CommandUtil::suggestOnlinePlayers)
                                        .executes(ctx -> handleCookCommand(ctx.getSource().getSender(), StringArgumentType.getString(ctx, "recipe"), StringArgumentType.getString(ctx, "player"), 1, false))
                                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                                .executes(ctx -> handleCookCommand(ctx.getSource().getSender(), StringArgumentType.getString(ctx, "recipe"), StringArgumentType.getString(ctx, "player"), IntegerArgumentType.getInteger(ctx, "amount"), false))
                                                .then(Commands.literal("auto")
                                                        .executes(ctx -> handleCookCommand(ctx.getSource().getSender(), StringArgumentType.getString(ctx, "recipe"), StringArgumentType.getString(ctx, "player"), IntegerArgumentType.getInteger(ctx, "amount"), true))
                                                )
                                        )
                                )
                        )
                )

                // /cooking migrateperms
                .then(Commands.literal("migrateperms")
                        .executes(ctx -> handleMigratePermsCommand(ctx.getSource().getSender())))

                // /cooking unlock <player> [<recipe>|all|player]
                .then(Commands.literal("unlock")
                        .then(Commands.argument("player", StringArgumentType.word())
                                .suggests(CommandUtil::suggestOnlinePlayers)
                                .executes(ctx -> handleUnlockCommand(ctx.getSource().getSender(), StringArgumentType.getString(ctx, "player"), null))
                                .then(Commands.argument("recipe", StringArgumentType.word())
                                        .executes(ctx -> handleUnlockCommand(ctx.getSource().getSender(), StringArgumentType.getString(ctx, "player"), StringArgumentType.getString(ctx, "recipe")))
                                )
                        )
                )

                // /cooking lock <player> <recipe>
                .then(Commands.literal("lock")
                        .then(Commands.argument("player", StringArgumentType.word())
                                .suggests(CommandUtil::suggestOnlinePlayers)
                                .then(Commands.argument("recipe", StringArgumentType.word())
                                        .executes(ctx -> handleLockCommand(ctx.getSource().getSender(), StringArgumentType.getString(ctx, "player"), StringArgumentType.getString(ctx, "recipe")))
                                )
                        )
                )

                // /cooking mastery <player> <recipe> [<count>]
                .then(Commands.literal("mastery")
                        .then(Commands.argument("player", StringArgumentType.word())
                                .suggests(CommandUtil::suggestOnlinePlayers)
                                .then(Commands.argument("recipe", StringArgumentType.word())
                                        .executes(ctx -> handleMasteryCommand(ctx.getSource().getSender(), StringArgumentType.getString(ctx, "player"), StringArgumentType.getString(ctx, "recipe"), -1))
                                        .then(Commands.argument("count", IntegerArgumentType.integer(0))
                                                .executes(ctx -> handleMasteryCommand(ctx.getSource().getSender(), StringArgumentType.getString(ctx, "player"), StringArgumentType.getString(ctx, "recipe"), IntegerArgumentType.getInteger(ctx, "count")))
                                        )
                                )
                        )
                )

                // /cooking recipebook [<player>]
                .then(Commands.literal("recipebook")
                        .executes(ctx -> handleRecipeBookCommand(ctx.getSource().getSender(), null))
                        .then(Commands.argument("player", StringArgumentType.word())
                                .suggests(CommandUtil::suggestOnlinePlayers)
                                .executes(ctx -> handleRecipeBookCommand(ctx.getSource().getSender(), StringArgumentType.getString(ctx, "player")))
                        )
                )

                // /cooking competition <start/end/cancel/join>
                .then(Commands.literal("competition")
                        .then(Commands.literal("start")
                                .then(Commands.argument("name", StringArgumentType.word())
                                        .executes(ctx -> handleCompetitionCommand(ctx.getSource().getSender(), "start", StringArgumentType.getString(ctx, "name")))
                                )
                        )
                        .then(Commands.literal("end")
                                .executes(ctx -> handleCompetitionCommand(ctx.getSource().getSender(), "end", null))
                        )
                        .then(Commands.literal("cancel")
                                .executes(ctx -> handleCompetitionCommand(ctx.getSource().getSender(), "cancel", null))
                        )
                        .then(Commands.literal("join")
                                .executes(ctx -> handleCompetitionCommand(ctx.getSource().getSender(), "join", null))
                        )
                )

                .executes(ctx -> {
                    showCommandHelp(ctx.getSource().getSender());
                    return Command.SINGLE_SUCCESS;
                })
                .build();
    }

    private int showStatsCommand(CommandSender sender) {
        if (sender instanceof Player player) {
            List<String> unlockedRecipes = RecipeDataUtil.getUnlockedRecipes(player);

            AdventureUtil.sendMessage(sender, "<gold><bold>BorealCore</bold><grey> version 1.1.9.1");
            AdventureUtil.sendMessage(sender, "<grey>Created by <gold>SnowyOwl217");
            AdventureUtil.sendMessage(sender, "<gold> Total Recipes Cooked: " + MasteryManager.getRecipeCount(player.getName()));
            AdventureUtil.sendMessage(sender, "<gold> Total Recipes Unlocked: " + unlockedRecipes.size());
            AdventureUtil.sendMessage(sender, "<gold> Total Recipes Mastered: " + RecipeDataUtil.getMasteredRecipes(player, unlockedRecipes).size());
            AdventureUtil.sendMessage(sender, "<gold> Total Recipes Unknown: " + RecipeDataUtil.getLockedRecipes(unlockedRecipes).size());
        }
        return Command.SINGLE_SUCCESS;
    }

    private void showCommandHelp(CommandSender sender) {
        AdventureUtil.sendMessage(sender, "<gold><bold>BorealCore</bold><grey> version 1.0.0");
        AdventureUtil.sendMessage(sender, "<grey>Created by <gold>SnowyOwl217");
        AdventureUtil.sendMessage(sender, "<gold>/cooking cook <recipe> <player> [<amount>] [auto]");
        AdventureUtil.sendMessage(sender, "<gold>/cooking unlock <player> [<recipe>]");
        AdventureUtil.sendMessage(sender, "<gold>/cooking lock <player> <recipe>");
        AdventureUtil.sendMessage(sender, "<gold>/cooking competition <start/end/cancel>");
    }

    private int handleCookCommand(CommandSender sender, String recipe, String username, int amount, boolean auto) {
        Player player = Bukkit.getPlayer(username);
        if (player != null) {
            if (auto) {
                cookingModule.handleAutocooking(recipe, player, amount);
            } else {
                cookingModule.handleCooking(recipe, player, null);
            }
        } else {
            AdventureUtil.sendMessage(sender, MessageManager.infoNegative + MessageManager.playerNotExist);
        }
        return Command.SINGLE_SUCCESS;
    }

    private int handleUnlockCommand(CommandSender sender, String targetPlayer, String recipe) {
        Player player = Bukkit.getPlayer(targetPlayer);
        if (player == null) {
            AdventureUtil.consoleMessage(MessageManager.infoNegative + MessageManager.playerNotExist);
            return Command.SINGLE_SUCCESS;
        }

        if (recipe == null) {
            RecipeDataUtil.checkAndAddRandomRecipe(player);
        } else if (recipe.equalsIgnoreCase("all")) {
            RecipeDataUtil.unlockAllRecipes(player);
        } else if (recipe.equalsIgnoreCase("player")) {
            RecipeDataUtil.unlockStarterRecipes(player);
        } else {
            RecipeDataUtil.setRecipeStatus(player, recipe, true);
        }
        return Command.SINGLE_SUCCESS;
    }

    private int handleLockCommand(CommandSender sender, String targetPlayer, String recipe) {
        Player player = Bukkit.getPlayer(targetPlayer);
        if (player == null) {
            AdventureUtil.consoleMessage("<red>[!] Player " + targetPlayer + " not found.");
            return Command.SINGLE_SUCCESS;
        }
        RecipeDataUtil.setRecipeStatus(player, recipe, false);
        return Command.SINGLE_SUCCESS;
    }

    private int handleMasteryCommand(CommandSender sender, String targetPlayer, String recipe, int count) {
        Player player = Bukkit.getPlayer(targetPlayer);
        if (player == null) {
            AdventureUtil.consoleMessage("<red>[!] Player " + targetPlayer + " not found.");
            return Command.SINGLE_SUCCESS;
        }

        if (count == -1) {
            RecipeDataUtil.setRecipeData(player, recipe, RecipeDataUtil.getDefaultRequiredMastery(recipe));
        } else {
            RecipeDataUtil.setRecipeData(player, recipe, count);
        }
        return Command.SINGLE_SUCCESS;
    }

    private int handleCompetitionCommand(CommandSender sender, String action, String name) {
        if (action.equals("start")) {
            if (CompetitionSchedule.startCompetition(name)) {
                AdventureUtil.sendMessage(sender, MessageManager.prefix + CookingMessage.forceSuccess);
            } else {
                AdventureUtil.sendMessage(sender, MessageManager.prefix + CookingMessage.forceFailure);
            }
        } else if (action.equals("end")) {
            CompetitionSchedule.endCompetition();
            AdventureUtil.sendMessage(sender, MessageManager.prefix + CookingMessage.forceEnd);
        } else if (action.equals("cancel")) {
            CompetitionSchedule.cancelCompetition();
            AdventureUtil.sendMessage(sender, MessageManager.prefix + CookingMessage.forceCancel);
        } else if (action.equals("join")) {
            if (sender instanceof Player player) {
                Competition.currentCompetition.tryAddBossBarToPlayer(player);
            }
        }
        return Command.SINGLE_SUCCESS;
    }

    private int handleRecipeBookCommand(CommandSender sender, String targetPlayer) {
        if (targetPlayer == null) {
            if (sender instanceof Player player) {
                GuiManager.openGui(player, "cookingRecipeBook");
            }
            return Command.SINGLE_SUCCESS;
        }

        Player player = Bukkit.getPlayer(targetPlayer);
        if (player == null) {
            AdventureUtil.sendMessage(sender, MessageManager.infoNegative + MessageManager.playerNotExist);
            return Command.SINGLE_SUCCESS;
        }

        GuiManager.openGui(player, "cookingRecipeBook");
        return Command.SINGLE_SUCCESS;
    }

    @Deprecated
    private int handleMigratePermsCommand(CommandSender sender) {
        long startTime = System.currentTimeMillis();
        int migratedCount = MasteryManager.migratePermissions();
        AdventureUtil.sendMessage(sender, MessageManager.prefix + " Migrated and Updated the perms for <green>" + migratedCount + " Recipes and Masteries <gray>in <green>" + (System.currentTimeMillis() - startTime) + "ms");
        return Command.SINGLE_SUCCESS;
    }
}