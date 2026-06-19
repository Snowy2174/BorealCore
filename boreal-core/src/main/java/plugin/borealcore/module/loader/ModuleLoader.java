package plugin.borealcore.module.loader;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import plugin.borealcore.api.module.BorealModule;
import plugin.borealcore.api.module.ModuleContext;
import plugin.borealcore.api.module.ModuleLoadException;
import plugin.borealcore.api.module.ModuleMetadata;
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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;

/**
 * Loads and manages BorealCore modules from JAR files.
 * Scans the plugins/BorealCore/modules/ directory for module JARs.
 */
public class ModuleLoader {

    private final ModuleContext globalContext;
    private final File modulesDirectory;

    private final Map<String, BorealModule> loadedModules;
    private final Map<String, ModuleMetadata> moduleMetadata;
    private final Map<String, ModuleClassLoader> moduleClassLoaders;

    public ModuleLoader(ModuleContext context) {
        this.globalContext = context;
        this.modulesDirectory = new File(globalContext.getPlugin().getDataFolder(), "modules");

        this.loadedModules = new LinkedHashMap<>();
        this.moduleMetadata = new LinkedHashMap<>();
        this.moduleClassLoaders = new LinkedHashMap<>();

        if (!modulesDirectory.exists()) {
            modulesDirectory.mkdirs();
        }
    }

    /**
     * Loads all modules from the modules directory in dependency order.
     * * @throws ModuleLoadException if loading fails
     */
    public void loadAllModules() throws ModuleLoadException {
        File[] moduleJars = modulesDirectory.listFiles((dir, name) -> name.endsWith(".jar"));

        if (moduleJars == null || moduleJars.length == 0) {
            AdventureUtil.consoleMessage("No modules found in " + modulesDirectory.getPath());
            return;
        }

        AdventureUtil.consoleMessage("Discovered " + moduleJars.length + " module(s). Resolving dependencies...");

        Map<String, File> jarFileMap = new HashMap<>();
        Map<String, ModuleMetadata> preScanMetadata = new HashMap<>();

        for (File jarFile : moduleJars) {
            try (JarFile jar = new JarFile(jarFile)) {
                JarEntry manifestEntry = jar.getJarEntry("module.yml");
                if (manifestEntry == null) continue;

                ModuleMetadata metadata = parseManifest(jar.getInputStream(manifestEntry));
                jarFileMap.put(metadata.getModuleId(), jarFile);
                preScanMetadata.put(metadata.getModuleId(), metadata);
            } catch (Exception e) {
                globalContext.getLogger().log(Level.SEVERE, "Failed to read manifest for: " + jarFile.getName(), e);
            }
        }

        List<String> sortedModuleIds;
        try {
            sortedModuleIds = sortModules(preScanMetadata);
        } catch (ModuleLoadException e) {
            globalContext.getLogger().severe("Failed to resolve module dependency graph: " + e.getMessage());
            return;
        }

        for (String moduleId : sortedModuleIds) {
            try {
                loadModule(jarFileMap.get(moduleId), preScanMetadata.get(moduleId));
            } catch (ModuleLoadException e) {
                globalContext.getLogger().log(Level.SEVERE, "Failed to load module: " + moduleId, e);
            }
        }

        enableAllModules();
    }

    /**
     * Performs a topological sort on the modules using a depth-first search.
     */
    private List<String> sortModules(Map<String, ModuleMetadata> modulesToLoad) throws ModuleLoadException {
        List<String> sorted = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        Set<String> visiting = new HashSet<>();

        for (String moduleId : modulesToLoad.keySet()) {
            visitModule(moduleId, modulesToLoad, visited, visiting, sorted);
        }
        return sorted;
    }

    private void visitModule(String moduleId, Map<String, ModuleMetadata> modulesToLoad, Set<String> visited, Set<String> visiting, List<String> sorted) throws ModuleLoadException {
        if (visiting.contains(moduleId)) {
            throw new ModuleLoadException("Circular dependency detected involving module: " + moduleId);
        }
        if (visited.contains(moduleId)) {
            return;
        }

        visiting.add(moduleId);

        ModuleMetadata metadata = modulesToLoad.get(moduleId);
        List<String> depends = metadata.getModuleDependencies();

        if (depends != null) {
            for (String depId : depends) {
                if (!modulesToLoad.containsKey(depId) && !loadedModules.containsKey(depId)) {
                    throw new ModuleLoadException("Module '" + moduleId + "' requires missing module '" + depId + "'");
                }
                if (modulesToLoad.containsKey(depId)) {
                    visitModule(depId, modulesToLoad, visited, visiting, sorted);
                }
            }
        }

        visiting.remove(moduleId);
        visited.add(moduleId);
        sorted.add(moduleId);
    }

    /**
     * Loads a single module from a JAR file (bypassing manifest re-parse).
     */
    private void loadModule(File jarFile, ModuleMetadata metadata) throws ModuleLoadException {
        try {
            if (!isVersionCompatible(metadata.getMinimumCoreVersion())) {
                throw new ModuleLoadException("Module '" + metadata.getModuleName() +
                        "' requires BorealCore " + metadata.getMinimumCoreVersion() +
                        " but only " + globalContext.getPlugin().getDescription().getVersion() + " is installed");
            }

            if (!checkPluginDependencies(metadata)) {
                throw new ModuleLoadException("Missing required external plugins for module: " + metadata.getModuleName());
            }

            ModuleClassLoader classLoader = new ModuleClassLoader(
                    metadata.getModuleId(),
                    new URL[]{jarFile.toURI().toURL()},
                    globalContext.getPlugin().getClass().getClassLoader()
            );

            Class<?> moduleClass = classLoader.loadClass(metadata.getMainClass());

            if (moduleClass == null) {
                throw new ModuleLoadException("Main class " + metadata.getMainClass() + " not found in module " + metadata.getModuleName());
            }

            if (!BorealModule.class.isAssignableFrom(moduleClass)) {
                throw new ModuleLoadException("Main class " + metadata.getMainClass() + " does not implement BorealModule");
            }

            BorealModule module = (BorealModule) moduleClass.getDeclaredConstructor().newInstance();

            moduleMetadata.put(metadata.getModuleId(), metadata);
            loadedModules.put(metadata.getModuleId(), module);
            moduleClassLoaders.put(metadata.getModuleId(), classLoader);

            ModuleRegistry.getInstance().registerModule(metadata.getModuleId(), module, metadata);
            AdventureUtil.consoleMessage("Loaded module: " + metadata.getModuleName());

        } catch (Exception e) {
            throw new ModuleLoadException("Failed to load module from " + jarFile.getName(), e);
        }
    }

    /**
     * Initializes and enables all loaded modules.
     * Iterates over LinkedHashMap to ensure dependencies are initialized first.
     */
    private void enableAllModules() {
        List<String> failedModules = new ArrayList<>();

        for (Map.Entry<String, BorealModule> entry : loadedModules.entrySet()) {
            String moduleId = entry.getKey();
            BorealModule module = entry.getValue();

            try {
                module.onModuleInitialize(globalContext);
                module.onModuleEnable();
                AdventureUtil.consoleMessage("Enabled module: " + moduleId);
            } catch (Exception e) {
                globalContext.getLogger().log(Level.SEVERE, "Failed to enable module: " + moduleId + ". It will be disabled.", e);
                failedModules.add(moduleId);
            }
        }

        for (String failedId : failedModules) {
            cleanupFailedModule(failedId);
        }
    }

    private void cleanupFailedModule(String moduleId) {
        loadedModules.remove(moduleId);
        moduleMetadata.remove(moduleId);
        ModuleRegistry.getInstance().unregisterModule(moduleId);

        ModuleClassLoader classLoader = moduleClassLoaders.remove(moduleId);
        if (classLoader != null) {
            try {
                classLoader.close();
            } catch (IOException e) {
                globalContext.getLogger().log(Level.WARNING, "Failed to close classloader for failed module: " + moduleId, e);
            }
        }
    }

    /**
     * Disables all loaded modules in reverse dependency order.
     */
    public void unloadAllModules() {
        if (loadedModules.isEmpty()) {
            return;
        }

        List<String> reverseOrderIds = new ArrayList<>(loadedModules.keySet()).reversed();

        for (String moduleId : reverseOrderIds) {
            try {
                BorealModule module = loadedModules.get(moduleId);
                if (module != null) {
                    module.onModuleDisable();
                    AdventureUtil.consoleMessage("Disabled module: " + moduleId);
                }
            } catch (Exception e) {
                globalContext.getLogger().log(Level.SEVERE, "Exception while disabling module: " + moduleId, e);
            }
        }

        loadedModules.clear();
        moduleMetadata.clear();
        ModuleRegistry.getInstance().clear();

        for (ModuleClassLoader classLoader : moduleClassLoaders.values()) {
            try {
                classLoader.close();
            } catch (IOException e) {
                globalContext.getLogger().log(Level.WARNING, "Failed to close module classloader", e);
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
     * Gets metadata for a module.
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
        String current = globalContext.getPlugin().getDescription().getVersion();
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
     * Checks if the required plugins for a module are present and enabled.
     * Returns: true if all dependencies are met, false otherwise
     */
    public boolean checkPluginDependencies(ModuleMetadata metadata) {
        List<String> requiredPlugins = metadata.getPluginDependencies();

        if (requiredPlugins == null || requiredPlugins.isEmpty()) {
            return true;
        }

        for (String pluginName : requiredPlugins) {
            Plugin plugin = Bukkit.getPluginManager().getPlugin(pluginName);

            if (plugin == null || !plugin.isEnabled()) {
                this.globalContext.getLogger().warning("Cannot load module '" + metadata.getModuleName() + "'. Required plugin '" + pluginName + "' is missing or disabled!");
                return false;
            }
        }

        return true;
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
            List<String> pluginDependencies = config.getStringList("plugin-depends");
            List<String> moduleDependencies = config.getStringList("module-depends");

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

            return new ModuleMetadata(moduleId, name, version, author, mainClass, minimumCoreVersion, pluginDependencies, moduleDependencies);

        } catch (ModuleLoadException e) {
            throw e;
        } catch (Exception e) {
            throw new ModuleLoadException("Failed to parse module.yml", e);
        }
    }
}




