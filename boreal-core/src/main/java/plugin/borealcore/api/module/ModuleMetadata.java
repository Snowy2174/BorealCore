package plugin.borealcore.api.module;

import java.util.List;

/**
 * Metadata about a module loaded from the module manifest.
 * Used for tracking module information without instantiation.
 */
public class ModuleMetadata {

    private final String moduleId;
    private final String moduleName;
    private final String version;
    private final String author;
    private final String mainClass;
    private final String minimumCoreVersion;
    private final List<String> pluginDependencies;

    public ModuleMetadata(String moduleId, String moduleName, String version, String author,
                          String mainClass, String minimumCoreVersion, List<String> pluginDependencies) {
        this.moduleId = moduleId;
        this.moduleName = moduleName;
        this.version = version;
        this.author = author;
        this.mainClass = mainClass;
        this.minimumCoreVersion = minimumCoreVersion;
        this.pluginDependencies = pluginDependencies;
    }

    public String getModuleId() {
        return moduleId;
    }

    public String getModuleName() {
        return moduleName;
    }

    public String getVersion() {
        return version;
    }

    public String getAuthor() {
        return author;
    }

    public String getMainClass() {
        return mainClass;
    }

    public String getMinimumCoreVersion() {
        return minimumCoreVersion;
    }

    public List<String> getPluginDependencies() {
        return pluginDependencies;
    }

    @Override
    public String toString() {
        return String.format("%s v%s by %s (requires BC %s)",
                moduleName, version, author, minimumCoreVersion);
    }
}