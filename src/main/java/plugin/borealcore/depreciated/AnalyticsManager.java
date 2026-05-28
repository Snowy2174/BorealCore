package plugin.borealcore.depreciated;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.EmbedBuilder;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import plugin.borealcore.database.Database;
import plugin.borealcore.manager.configs.ConfigManager;
import plugin.borealcore.object.Function;

import java.time.Duration;
import java.util.Map;
import java.util.stream.Collectors;

import static org.apache.logging.log4j.LogManager.getLogger;

public class AnalyticsManager extends Function {

    private final Database database;

    public AnalyticsManager(Database database) {
        this.database = database;
    }

    @Override
    public void load() {
        if (database == null) {
            getLogger().error("Database is not initialized. Analytics Manager cannot be loaded.");
            return;
        }
        if (ConfigManager.processAnalyticsEnabled) {
            getLogger().info("Analytics Manager has been enabled.");
            queryJadeDatabase();
        }
    }

    @Override
    public void unload() {
        // Cleanup or save any data if necessary
        getLogger().info("Analytics Manager has been disabled.");
    }

    private void queryJadeDatabase() {
        EmbedBuilder embed = generateDiscordEmbed();
        TextChannel textChannel = DiscordSRV.getPlugin().getDestinationTextChannelForGameChannelName("developer-logs");
        if (textChannel != null) {
            textChannel.sendMessageEmbeds(embed.build()).queue();
        }
    }

    public EmbedBuilder generateDiscordEmbed() {
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Jade Economy Analytics")
                .setColor(0x00FF00);

        embed.addField("Most Used Sources (Last Week)", formatSourceUsage(database.getMostUsedSources(Duration.ofDays(7))), false);
        embed.addField("Most Used Sources (Last Month)", formatSourceUsage(database.getMostUsedSources(Duration.ofDays(30))), false);
        embed.addField("Most Used Sources (Last 3 Months)", formatSourceUsage(database.getMostUsedSources(Duration.ofDays(90))), false);
        embed.addField("Most Used Sources (Last Year)", formatSourceUsage(database.getMostUsedSources(Duration.ofDays(365))), false);
        embed.addField("Average Player Gain Per Week (Last 3 Months)", formatAverageGains(database.getAveragePlayerGainPerWeek()), false);
        embed.addField("Source Dependency Spread", formatSourceDependencySpread(database.getSourceDependencySpread()), false);
        embed.addField("Top 10% Players' Share of Jade", String.format("%.2f%%", database.getTop10PercentPlayersShare()), false);
        embed.addField("Source Efficiency Analysis", formatSourceEfficiency(database.getSourceEfficiencyAnalysis()), false);

        return embed;
    }

    private String formatSourceUsage(Map<String, Double> sourceUsage) {
        return sourceUsage.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + String.format("%.2f", entry.getValue()) + "%")
                .collect(Collectors.joining("\n"));
    }

    private String formatAverageGains(Map<String, Double> averageGains) {
        return averageGains.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + String.format("%.2f", entry.getValue()))
                .collect(Collectors.joining("\n"));
    }

    private String formatSourceDependencySpread(Map<String, Integer> sourceSpread) {
        return sourceSpread.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue() + " players")
                .collect(Collectors.joining("\n"));
    }

    private String formatSourceEfficiency(Map<String, Double> sourceEfficiency) {
        return sourceEfficiency.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + String.format("%.2f", entry.getValue()) + " jade/player")
                .collect(Collectors.joining("\n"));
    }
}
