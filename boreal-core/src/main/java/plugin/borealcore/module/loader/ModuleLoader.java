package plugin.borealcore.module.loader;

import plugin.borealcore.BorealCore;
import plugin.borealcore.api.module.*;
import plugin.borealcore.database.Database;
import plugin.borealcore.utility.AdventureUtil;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;

/**
 * Loads and manages BorealCore modules from JAR files.
 * Scans the plugins/BorealCore/modules/ directory for module JARs.
 */
public class ModuleLoader {

    private final BorealCore plugin;
    private final Database database;
    private final File modulesDirectory;
    private final Map<String, BorealModule> loadedModules;
    private final Map<String, ModuleMetadata> moduleMetadata;
    private final Map<String, ModuleClassLoader> moduleClassLoaders;

    public ModuleLoader(BorealCore plugin, Database database) {
        this.plugin = plugin;
        this.database = database;
        this.modulesDirectory = new File(plugin.getDataFolder(), "modules");
        this.loadedModules = new HashMap<>();
        this.moduleMetadata = new HashMap<>();
        this.moduleClassLoaders = new HashMap<>();

        if (!modulesDirectory.exists()) {
            modulesDirectory.mkdirs();
        }
    }

    /**
     * Loads all modules from the modules directory.
     * 
     * @throws ModuleLoadException if loading fails
     */
    public void loadAllModules() throws ModuleLoadException {
        File[] moduleJars = modulesDirectory.listFiles((dir, name) -> name.endsWith(".jar"));

        if (moduleJars == null || moduleJars.length == 0) {
            AdventureUtil.consoleMessage("No modules found in " + modulesDirectory.getPath());
            return;
        }

        AdventureUtil.consoleMessage("Loading " + moduleJars.length + " module(s)...");

        for (File jarFile : moduleJars) {
            try {
                loadModule(jarFile);
            } catch (ModuleLoadException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to load module: " + jarFile.getName(), e);
            }
        }

        // Enable all loaded modules
        enableAllModules();
    }

    /**
     * Loads a single module from a JAR file.
     * 
     * @param jarFile The JAR file containing the module
     * @throws ModuleLoadException if loading fails
     */
    private void loadModule(File jarFile) throws ModuleLoadException {
        try (JarFile jar = new JarFile(jarFile)) {
            // Read module.yml manifest
            JarEntry manifestEntry = jar.getJarEntry("module.yml");
            if (manifestEntry == null) {
                throw new ModuleLoadException("module.yml not found in " + jarFile.getName());
            }

            ModuleMetadata metadata = ModuleManifestParser.parseManifest(jar.getInputStream(manifestEntry));
            
            // Check BorealCore version compatibility
            if (!isVersionCompatible(metadata.getMinimumBorealCoreVersion())) {
                throw new ModuleLoadException("Module '" + metadata.getModuleName() + 
                    "' requires BorealCore " + metadata.getMinimumBorealCoreVersion() + 
                    " but only " + plugin.getDescription().getVersion() + " is installed");
            }

            // Load the module class
            ModuleClassLoader classLoader = new ModuleClassLoader(
                metadata.getModuleId(),
                new URL[]{jarFile.toURI().toURL()},
                plugin.getClass().getClassLoader()
            );

            Class<?> moduleClass = classLoader.loadClass(metadata.getMainClass());
            
            if (!BorealModule.class.isAssignableFrom(moduleClass)) {
                throw new ModuleLoadException("Main class " + metadata.getMainClass() + 
                    " does not implement BorealModule");
            }

            BorealModule module = (BorealModule) moduleClass.getDeclaredConstructor().newInstance();
            
            // Store metadata and module
            moduleMetadata.put(metadata.getModuleId(), metadata);
            loadedModules.put(metadata.getModuleId(), module);
            moduleClassLoaders.put(metadata.getModuleId(), classLoader);

            // Register in the global module registry
            ModuleRegistry.getInstance().registerModule(metadata.getModuleId(), module, metadata);

            AdventureUtil.consoleMessage("Loaded module: " + metadata);

        } catch (Exception e) {
            throw new ModuleLoadException("Failed to load module from " + jarFile.getName(), e);
        }
    }

    /**
     * Initializes and enables all loaded modules.
     */
    private void enableAllModules() {
        ModuleContext context = new ModuleContext(plugin, database);

        for (String moduleId : loadedModules.keySet()) {
            try {
                BorealModule module = loadedModules.get(moduleId);
                module.onModuleInitialize(context);
                module.onModuleEnable();
                AdventureUtil.consoleMessage("Enabled module: " + moduleId);
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to enable module: " + moduleId, e);
            }
        }
    }

    /**
     * Disables all loaded modules.
     */
    public void disableAllModules() {
        for (String moduleId : new ArrayList<>(loadedModules.keySet())) {
            try {
                BorealModule module = loadedModules.get(moduleId);
                module.onModuleDisable();
                AdventureUtil.consoleMessage("Disabled module: " + moduleId);
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to disable module: " + moduleId, e);
            }
        }

        loadedModules.clear();
        moduleMetadata.clear();
        
        // Clear the global registry
        ModuleRegistry.getInstance().clear();
        
        // Clean up classloaders
        for (ModuleClassLoader classLoader : moduleClassLoaders.values()) {
            try {
                classLoader.close();
            } catch (IOException e) {
                plugin.getLogger().log(Level.WARNING, "Failed to close module classloader", e);
            }
        }
        moduleClassLoaders.clear();
    }

    /**
     * Gets a loaded module by ID.
     * 
     * @param moduleId The module ID
     * @return The module instance, or null if not loaded
     */
    public BorealModule getModule(String moduleId) {
        return loadedModules.get(moduleId);
    }

    /**
     * Gets metadata for an module.
     * 
     * @param moduleId The module ID
     * @return The module metadata, or null if not found
     */
    public ModuleMetadata getModuleMetadata(String moduleId) {
        return moduleMetadata.get(moduleId);
    }

    /**
     * Gets all loaded module IDs.
     * 
     * @return A collection of module IDs
     */
    public Collection<String> getLoadedModuleIds() {
        return Collections.unmodifiableCollection(loadedModules.keySet());
    }

    /**
     * Checks if two versions are compatible (semantic versioning).
     * 
     * @param required The required version
     * @return true if current plugin version meets requirement
     */
    private boolean isVersionCompatible(String required) {
        String current = plugin.getDescription().getVersion();
        return compareVersions(current, required) >= 0;
    }

    /**
     * Compares two semantic versions.
     * Returns: positive if v1 > v2, zero if equal, negative if v1 < v2
     */
    private int compareVersions(String v1, String v2) {
        String[] parts1 = v1.split("\\.");
        String[] parts2 = v2.split("\\.");

        for (int i = 0; i < Math.max(parts1.length, parts2.length); i++) {
            int num1 = i < parts1.length ? Integer.parseInt(parts1[i].replaceAll("[^0-9]", "")) : 0;
            int num2 = i < parts2.length ? Integer.parseInt(parts2[i].replaceAll("[^0-9]", "")) : 0;

            if (num1 != num2) {
                return Integer.compare(num1, num2);
            }
        }
        return 0;
    }
}




