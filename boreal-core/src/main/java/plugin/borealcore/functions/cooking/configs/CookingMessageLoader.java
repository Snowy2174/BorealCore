package plugin.borealcore.functions.cooking.configs;

import org.bukkit.configuration.ConfigurationSection;
import plugin.borealcore.manager.ConfigManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Loads cooking-specific messages from the messages file.
 * Called by CookingManager during module initialization.
 * 
 * Uses ModuleContext pattern: All message defaults are applied automatically,
 * and messages are read from the messages_<lang>.yml file.
 */
public class CookingMessageLoader {

    public static void load() {
        // Define all cooking message defaults in a single map (without "messages." prefix)
        Map<String, String> messageDefaults = new HashMap<>();
        
        messageDefaults.put("mastery-message", "<green>You have achieved mastery for the dish: {recipe}");
        messageDefaults.put("mastery-reward", "<green>You have been given 5 ₪ for gaining {recipe} mastery");
        messageDefaults.put("recipe-locked", "<red>You have lost the recipe: {recipe}");
        messageDefaults.put("recipe-unlocked", "<green>You have unlocked the recipe: {recipe}");
        messageDefaults.put("recipe-unknown", "<red>You haven't unlocked this recipe yet..");
        messageDefaults.put("recipe-no-pot", "<red>You need to use a cooking pot to cook this recipe!");
        messageDefaults.put("already-cooking", "<red>You're already cooking something!");
        messageDefaults.put("no-ingredients", "<red>You do not have the required ingredients to cook this item.");
        messageDefaults.put("too-slow", "You've failed to produce the dish in the required time");
        messageDefaults.put("cooking-autocooked", "<green>You have autocooked one {recipe}");
        messageDefaults.put("cooking-perfect", "<green>You have cooked the dish {recipe} perfectly!");
        messageDefaults.put("pot-light-up", "<green>You lit up the cooking pot!");
        messageDefaults.put("pot-cold", "<red>You can't cook int a cold pot.. try heating it up");
        messageDefaults.put("pot-cooldown", "You need to wait {time} seconds before interacting with this again!");
        messageDefaults.put("no-rank", "No Rank");
        messageDefaults.put("competition-ongoing", "There is currently a cooking tournament in progress! Start cooking to join the contest for a prize!");
        messageDefaults.put("force-competition-success", "Forced to start a cooking competition.");
        messageDefaults.put("force-competition-failure", "The competition does not exist.");
        messageDefaults.put("force-competition-end", "Forced to end the current competition.");
        messageDefaults.put("force-competition-cancel", "Forced to cancel the competition");
        messageDefaults.put("no-player", "No player");
        messageDefaults.put("no-score", "No score");

        // Setup defaults using ConfigManager utility (messages file)
        ConfigurationSection messages = ConfigManager.setupModuleMessages(messageDefaults);

        // Load values into static fields using the "messages." prefix
        CookingMessage.masteryMessage = messages.getString("messages.mastery-message", "<green>You have achieved mastery for the dish: {recipe}");
        CookingMessage.masteryReward = messages.getString("messages.mastery-reward", "<green>You have been given 5 ₪ for gaining {recipe} mastery");

        CookingMessage.recipeLocked = messages.getString("messages.recipe-locked", "<red>You have lost the recipe: {recipe}");
        CookingMessage.recipeUnlocked = messages.getString("messages.recipe-unlocked", "<green>You have unlocked the recipe: {recipe}");
        CookingMessage.recipeUnknown = messages.getString("messages.recipe-unknown", "<red>You haven't unlocked this recipe yet..");
        CookingMessage.recipeNoPot = messages.getString("messages.recipe-no-pot", "<red>You need to use a cooking pot to cook this recipe!");

        CookingMessage.alreadyCooking = messages.getString("messages.already-cooking", "<red>You're already cooking something!");
        CookingMessage.noIngredients = messages.getString("messages.no-ingredients", "<red>You do not have the required ingredients to cook this item.");
        CookingMessage.tooSlow = messages.getString("messages.too-slow", "You've failed to produce the dish in the required time");
        CookingMessage.cookingAutocooked = messages.getString("messages.cooking-autocooked", "<green>You have autocooked one {recipe}");
        CookingMessage.cookingPerfect = messages.getString("messages.cooking-perfect", "<green>You have cooked the dish {recipe} perfectly!");

        CookingMessage.potLight = messages.getString("messages.pot-light-up", "<green>You lit up the cooking pot!");
        CookingMessage.potCold = messages.getString("messages.pot-cold", "<red>You can't cook int a cold pot.. try heating it up");
        CookingMessage.potCooldown = messages.getString("messages.pot-cooldown", "You need to wait {time} seconds before interacting with this again!");

        CookingMessage.notOnline = messages.getString("messages.not-online", "That player is not online.");
        CookingMessage.notEnoughPlayers = messages.getString("messages.players-not-enough", "The number of players who can cook is not enough for the cooking competition to be started as scheduled.");
        CookingMessage.noRank = messages.getString("messages.no-rank", "No Rank");
        CookingMessage.competitionOn = messages.getString("messages.competition-ongoing", "There is currently a cooking tournament in progress! Start cooking to join the contest for a prize!");
        CookingMessage.forceSuccess = messages.getString("messages.force-competition-success", "Forced to start a cooking competition.");
        CookingMessage.forceFailure = messages.getString("messages.force-competition-failure", "The competition does not exist.");
        CookingMessage.forceEnd = messages.getString("messages.force-competition-end", "Forced to end the current competition.");
        CookingMessage.forceCancel = messages.getString("messages.force-competition-cancel", "Forced to cancel the competition");
        CookingMessage.noPlayer = messages.getString("messages.no-player", "No player");
        CookingMessage.noScore = messages.getString("messages.no-score", "No score");
    }
}


