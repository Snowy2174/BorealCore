package plugin.borealcore.utility;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.EmbedBuilder;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import org.bukkit.Bukkit;

import java.awt.*;

public class DiscordUtil {

    public static boolean isDiscordSRVAvailable() {
        return Bukkit.getPluginManager().isPluginEnabled("DiscordSRV");
    }

    public static void sendMessage(String gameChannelName, String message) {
        if (!isDiscordSRVAvailable()) return;
        TextChannel channel = DiscordSRV.getPlugin().getDestinationTextChannelForGameChannelName(gameChannelName);
        if (channel != null) {
            channel.sendMessage(message).queue();
        } else {
            AdventureUtil.consoleMessage(DebugLevel.WARNING, "Could not find Discord channel mapped to: " + gameChannelName);
        }
    }

    public static void sendErrorMessage(String message) {
        if (!isDiscordSRVAvailable()) return;
        String developerPing = "<@701490040273895445>";
        TextChannel channel = DiscordSRV.getPlugin().getDestinationTextChannelForGameChannelName("developer-logs");
        if (channel != null) {
            channel.sendMessage(developerPing + " " + message).queue();
        }
    }

    /**
     * Sends a fully customized embed
     * @param gameChannelName The DiscordSRV channel name (e.g., "towny", "moderation")
     * @param embedBuilder    The constructed EmbedBuilder
     */
    public static void sendEmbed(String gameChannelName, EmbedBuilder embedBuilder) {
        if (!isDiscordSRVAvailable()) return;

        TextChannel channel = DiscordSRV.getPlugin().getDestinationTextChannelForGameChannelName(gameChannelName);
        if (channel != null) {
            channel.sendMessageEmbeds(embedBuilder.build()).queue();
        } else {
            AdventureUtil.consoleMessage(DebugLevel.WARNING, "Could not find Discord channel mapped to: " + gameChannelName);
        }
    }

    /**
     * Sends an Author-styled embed
     *
     * @param gameChannelName The DiscordSRV channel name
     * @param authorName      The name to display in the Author field
     * @param authorImageUrl  The URL of the avatar/icon to display
     * @param description     Main text body (can be null)
     * @param hexColor        Hex color code (e.g., "#EF3E36")
     */
    public static void sendAuthorEmbed(String gameChannelName, String authorName, String authorImageUrl, String description, String hexColor) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setAuthor(authorName, null, authorImageUrl);

        if (description != null && !description.isEmpty()) {
            embed.setDescription(description);
        }
        if (hexColor != null && !hexColor.isEmpty()) {
            embed.setColor(Color.decode(hexColor));
        }

        sendEmbed(gameChannelName, embed);
    }

    /**
     * Sends a Title-styled embed
     *
     * @param gameChannelName The DiscordSRV channel name
     * @param title           The bold title text
     * @param titleUrl        The URL the title links to (can be null)
     * @param hexColor        Hex color code (e.g., "#EC3131")
     */
    public static void sendTitleEmbed(String gameChannelName, String title, String titleUrl, String hexColor) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle(title, titleUrl);

        if (hexColor != null && !hexColor.isEmpty()) {
            embed.setColor(Color.decode(hexColor));
        }

        sendEmbed(gameChannelName, embed);
    }
}