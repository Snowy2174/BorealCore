package plugin.borealcore.module.loader;

import plugin.borealcore.api.module.BorealModule;
import plugin.borealcore.api.module.ModuleMetadata;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Central registry for accessing loaded modules and their metadata.
 * Provides a query interface for module information.
 */
public class ModuleRegistry {

    private static ModuleRegistry instance;
    private final Map<String, BorealModule> modules;
    private final Map<String, ModuleMetadata> metadata;

    private ModuleRegistry() {
        this.modules = new HashMap<>();
        this.metadata = new HashMap<>();
    }

    /**
     * Gets the singleton instance of the module registry.
     *
     * @return The module registry instance
     */
    public static synchronized ModuleRegistry getInstance() {
        if (instance == null) {
            instance = new ModuleRegistry();
        }
        return instance;
    }

    /**
     * Registers a loaded module.
     *
     * @param moduleId       The module ID
     * @param module         The module instance
     * @param moduleMetadata The module metadata
     */
    protected void registerModule(String moduleId, BorealModule module, ModuleMetadata moduleMetadata) {
        modules.put(moduleId, module);
        metadata.put(moduleId, moduleMetadata);
    }

    /**
     * Unregisters an module.
     *
     * @param moduleId The module ID
     */
    protected void unregisterModule(String moduleId) {
        modules.remove(moduleId);
        metadata.remove(moduleId);
    }

    /**
     * Gets a loaded module by ID.
     *
     * @param moduleId The module ID
     * @return The module instance, or null if not loaded
     */
    public BorealModule getModule(String moduleId) {
        return modules.get(moduleId);
    }

    /**
     * Gets metadata for an module.
     *
     * @param moduleId The module ID
     * @return The module metadata, or null if not found
     */
    public ModuleMetadata getModuleMetadata(String moduleId) {
        return metadata.get(moduleId);
    }

    /**
     * Checks if an module is loaded.
     *
     * @param moduleId The module ID
     * @return true if the module is loaded
     */
    public boolean isModuleLoaded(String moduleId) {
        return modules.containsKey(moduleId);
    }

    /**
     * Gets all loaded module IDs.
     *
     * @return An unmodifiable collection of module IDs
     */
    public Collection<String> getLoadedModuleIds() {
        return Collections.unmodifiableCollection(modules.keySet());
    }

    /**
     * Gets the count of loaded modules.
     *
     * @return The number of loaded modules
     */
    public int getLoadedModuleCount() {
        return modules.size();
    }

    /**
     * Clears the registry (used when disabling modules).
     */
    protected void clear() {
        modules.clear();
        metadata.clear();
    }
}

