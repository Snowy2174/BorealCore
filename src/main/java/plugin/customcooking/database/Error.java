package plugin.customcooking.database;


import plugin.customcooking.CustomCooking;

import java.util.logging.Level;

public class Error {
    public static void execute(CustomCooking plugin, Exception ex) {
        plugin.getLogger().log(Level.SEVERE, "Couldn't execute MySQL statement: ", ex);
    }

    public static void close(CustomCooking plugin, Exception ex) {
        plugin.getLogger().log(Level.SEVERE, "Failed to close MySQL connection: ", ex);
    }
}