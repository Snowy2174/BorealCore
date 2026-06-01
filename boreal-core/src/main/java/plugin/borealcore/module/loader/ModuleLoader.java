package plugin.borealcore.module.loader;

import org.bukkit.configuration.file.YamlConfiguration;
import plugin.borealcore.BorealCore;
import plugin.borealcore.api.module.BorealModule;
import plugin.borealcore.api.module.ModuleContext;
import plugin.borealcore.api.module.ModuleLoadException;
import plugin.borealcore.api.module.ModuleMetadata;
import plugin.borealcore.database.DatabaseManager;
import plugin.borealcore.manager.PlaceholderManager;
import plugin.borealcore.utility.AdventureUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;

/**
 * Loads and manages BorealCore modules from JAR files.
 * Scans the plugins/BorealCore/modules/ directory for module JARs.
 */
public class ModuleLoader {

    private final BorealCore plugin;
    private final DatabaseManager database;
    private final PlaceholderManager placeholderManager;
    private final File modulesDirectory;
    private final Map<String, BorealModule> loadedModules;
    private final Map<String, ModuleMetadata> moduleMetadata;
    private final Map<String, ModuleClassLoader> moduleClassLoaders;

    public ModuleLoader(BorealCore plugin, DatabaseManager database, PlaceholderManager  placeholderManager) {
        this.plugin = plugin;
        this.database = database;
        this.placeholderManager = placeholderManager;
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

            ModuleMetadata metadata = parseManifest(jar.getInputStream(manifestEntry));
            
            // Check BorealCore version compatibility
            if (!isVersionCompatible(metadata.getMinimumCoreVersion())) {
                throw new ModuleLoadException("Module '" + metadata.getModuleName() + 
                    "' requires BorealCore " + metadata.getMinimumCoreVersion() + 
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
        ModuleContext context = new ModuleContext(plugin, database, placeholderManager);

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
    public void unloadAllModules() {
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
    /**
     * Compares two semantic versions.
     * Returns: positive if v1 > v2, zero if equal, negative if v1 < v2
     */
    private int compareVersions(String v1, String v2) {
        String[] parts1 = v1.split("\\.");
        String[] parts2 = v2.split("\\.");

        for (int i = 0; i < Math.max(parts1.length, parts2.length); i++) {
            String raw1 = i < parts1.length ? parts1[i] : "0";
            String raw2 = i < parts2.length ? parts2[i] : "0";

            String clean1 = raw1.replaceAll("[^0-9]", "");
            String clean2 = raw2.replaceAll("[^0-9]", "");

            int num1 = clean1.isEmpty() ? 0 : Integer.parseInt(clean1);
            int num2 = clean2.isEmpty() ? 0 : Integer.parseInt(clean2);

            if (num1 != num2) {
                return Integer.compare(num1, num2);
            }
        }
        return 0;
    }

    /**
     * Parses a module.yml manifest file.
     *
     * @param inputStream The input stream to the module.yml file
     * @return The parsed module metadata
     * @throws ModuleLoadException if parsing fails
     */
    public static ModuleMetadata parseManifest(InputStream inputStream) throws ModuleLoadException {
        try (InputStreamReader reader = new InputStreamReader(inputStream)) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(reader);

            String moduleId = config.getString("id");
            String name = config.getString("name");
            String version = config.getString("version");
            String author = config.getString("author");
            String mainClass = config.getString("main");
            String minimumCoreVersion = config.getString("minimum-borealcore-version", "1.0.0");

            if (moduleId == null || moduleId.isEmpty()) {
                throw new ModuleLoadException("module.yml is missing required field 'id'");
            }
            if (name == null || name.isEmpty()) {
                throw new ModuleLoadException("module.yml is missing required field 'name'");
            }
            if (version == null || version.isEmpty()) {
                throw new ModuleLoadException("module.yml is missing required field 'version'");
            }
            if (author == null || author.isEmpty()) {
                throw new ModuleLoadException("module.yml is missing required field 'author'");
            }
            if (mainClass == null || mainClass.isEmpty()) {
                throw new ModuleLoadException("module.yml is missing required field 'main'");
            }

            return new ModuleMetadata(moduleId, name, version, author, mainClass, minimumCoreVersion);

        } catch (ModuleLoadException e) {
            throw e;
        } catch (Exception e) {
            throw new ModuleLoadException("Failed to parse module.yml", e);
        }
    }
}




