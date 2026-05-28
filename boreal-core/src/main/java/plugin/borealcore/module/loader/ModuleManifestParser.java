package plugin.borealcore.module.loader;

import org.bukkit.configuration.file.YamlConfiguration;
import plugin.borealcore.api.module.ModuleLoadException;
import plugin.borealcore.api.module.ModuleMetadata;

import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Parses module.yml manifest files for module metadata.
 */
public class ModuleManifestParser {

    /**
     * Parses an module.yml manifest file.
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
            String minimumBorealCoreVersion = config.getString("minimum-borealcore-version", "1.0.0");

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

            return new ModuleMetadata(moduleId, name, version, author, mainClass, minimumBorealCoreVersion);

        } catch (ModuleLoadException e) {
            throw e;
        } catch (Exception e) {
            throw new ModuleLoadException("Failed to parse module.yml", e);
        }
    }
}

