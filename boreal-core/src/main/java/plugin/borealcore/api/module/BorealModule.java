package plugin.borealcore.api.module;

/**
 * Base interface for all BorealCore modules.
 * External developers should extend Function and implement this interface
 * to create modules that can be loaded dynamically by the module loader.
 */
public interface BorealModule extends ModuleInitializer {

    /**
     * @return The unique identifier for this module (e.g., "jade", "cooking")
     */
    String getModuleId();

    /**
     * @return The display name of this module
     */
    String getModuleName();

    /**
     * @return The version of this module
     */
    String getModuleVersion();

    /**
     * @return The author(s) of this module
     */
    String getModuleAuthor();

    /**
     * @return The minimum version of BorealCore required for this module
     */
    String getMinimumBorealCoreVersion();

    /**
     * Called when the module is being loaded into the system.
     * This is called BEFORE {@link #onModuleEnable()}
     * 
     * @param context The module context providing access to plugin resources
     * @throws Exception if initialization fails
     */
    void onModuleInitialize(ModuleContext context) throws Exception;

}


