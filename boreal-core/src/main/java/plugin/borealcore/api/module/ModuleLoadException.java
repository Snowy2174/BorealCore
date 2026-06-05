package plugin.borealcore.api.module;

/**
 * Exception thrown when a module fails to load.
 */
public class ModuleLoadException extends Exception {

    public ModuleLoadException(String message) {
        super(message);
    }

    public ModuleLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}

