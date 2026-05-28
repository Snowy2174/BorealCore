package plugin.borealcore.module.loader;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

/**
 * Custom classloader for isolating module classes.
 * Allows multiple modules to have their own dependencies.
 */
public class ModuleClassLoader extends URLClassLoader {

    private final String moduleId;
    private final List<ModuleClassLoader> dependencies;

    public ModuleClassLoader(String moduleId, URL[] urls, ClassLoader parent) {
        super(urls, parent);
        this.moduleId = moduleId;
        this.dependencies = new ArrayList<>();
    }

    public String getModuleId() {
        return moduleId;
    }

    public void addDependency(ModuleClassLoader loader) {
        dependencies.add(loader);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        try {
            return super.findClass(name);
        } catch (ClassNotFoundException e) {
            for (ModuleClassLoader dependency : dependencies) {
                try {
                    return dependency.findClass(name);
                } catch (ClassNotFoundException ignored) {
                }
            }
            throw e;
        }
    }
}

