package plugin.borealcore.api.module;

/**
 * Base interface for all BorealCore modules.
 * External developers should implement this interface
 * to create modules that can be loaded dynamically by the module loader.
 */
public interface BorealModule {

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

    /**
     * Called when the module is being loaded into the system.
     * This is called BEFORE {@link #onModuleEnable()}
     * 
     * @param context The module context providing access to plugin resources
     * @throws Exception if initialization fails
     */
    void onModuleInitialize(ModuleContext context) throws Exception;

}


