package plugin.customcooking.manager;

import plugin.customcooking.database.Database;
import plugin.customcooking.manager.configs.ConfigManager;
import plugin.customcooking.object.Function;

import static org.apache.logging.log4j.LogManager.getLogger;

public class AnalyticsManager extends Function {

    private Database database;
    private String AllSourcesQuery = "SELECT * FROM jade_data";
    private String AllPlayersQuery = "SELECT * FROM jade_data";
    private String AllTimeQuery = "SELECT COUNT(*) FROM jade_data";

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
           // Initialize or load any necessary data for analytics
           queryJadeDatabase();
           sendAnalyticsData();
       }
    }

    @Override
    public void unload() {
        // Cleanup or save any data if necessary
        getLogger().info("Analytics Manager has been disabled.");
    }

    private void queryJadeDatabase() {
        // @TODO Implement the logic to query the database
    }

    private void sendAnalyticsData() {
        // @TODO Implement the logic to send the analytics data to dev channel
    }

}
