package plugin.borealcore.database;

public class Errors {
    public static String sqlConnectionExecute() {
        return "Couldn't execute MySQL statement: ";
    }

    public static String sqlConnectionClose() {
        return "Failed to close MySQL connection: ";
    }

    public static String noSQLConnection() {
        return "Unable to retreive MYSQL connection: ";
    }

    public static String noTableFound() {
        return "JadeManager Error: No Table Found";
    }
}
