package plugin.borealcore.api.module;

/**
 * Interface for module lifecycle management.
 * Provides hooks for module enable/disable events.
 */
public interface ModuleInitializer {

    /**
     * Called when the module is enabled.
     * This is where listeners should be registered and services initialized.
     *
     * @throws Exception if enabling fails
     */
    void onModuleEnable() throws Exception;

    /**
     * Called when the module is disabled.
     * This is where listeners should be unregistered and resources cleaned up.
     *
     * @throws Exception if disabling fails
     */
    void onModuleDisable() throws Exception;
}

