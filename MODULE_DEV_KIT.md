# BorealCore Module Development Kit (ADK)

## Overview

The BorealCore Module Development Kit allows external developers to create modular plugins that integrate seamlessly with the BorealCore plugin system. Modules are loaded dynamically at runtime and have full access to the core BorealCore database manager, configuration APIs, and event system.

## Getting Started

### 1. Project Setup

Create a new Maven project with the following structure:

```text
my-module/
├── pom.xml
└── src/
    └── main/
        ├── java/
        │   └── com/
        │       └── example/
        │           └── module/
        │               └── MyModule.java
        └── resources/
            └── module.yml

```

### 2. POM Configuration

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="[http://maven.apache.org/POM/4.0.0](http://maven.apache.org/POM/4.0.0)"
         xmlns:xsi="[http://www.w3.org/2001/XMLSchema-instance](http://www.w3.org/2001/XMLSchema-instance)"
         xsi:schemaLocation="[http://maven.apache.org/POM/4.0.0](http://maven.apache.org/POM/4.0.0) 
         [http://maven.apache.org/xsd/maven-4.0.0.xsd](http://maven.apache.org/xsd/maven-4.0.0.xsd)">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.example</groupId>
    <artifactId>my-module</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>

    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <maven.compiler.source>21</maven.compiler.source>
        <maven.compiler.target>21</maven.compiler.target>
    </properties>

    <repositories>
        <repository>
            <id>spigot-repo</id>
            <url>[https://hub.spigotmc.org/nexus/content/repositories/snapshots/](https://hub.spigotmc.org/nexus/content/repositories/snapshots/)</url>
        </repository>
    </repositories>

    <dependencies>
        <dependency>
            <groupId>plugin.borealcore</groupId>
            <artifactId>BorealCore</artifactId>
            <version>1.1.9</version>
            <scope>provided</scope>
        </dependency>

        <dependency>
            <groupId>org.spigotmc</groupId>
            <artifactId>spigot-api</artifactId>
            <version>1.21.1-R0.1-SNAPSHOT</version>
            <scope>provided</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.8.1</version>
                <configuration>
                    <source>21</source>
                    <target>21</target>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-shade-plugin</artifactId>
                <version>3.4.1</version>
                <executions>
                    <execution>
                        <phase>package</phase>
                        <goals>
                            <goal>shade</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
        <resources>
            <resource>
                <directory>src/main/resources</directory>
            </resource>
        </resources>
    </build>
</project>

```

### 3. Create module.yml Manifest

In `src/main/resources/module.yml`:

```yaml
id: my-module
name: My Custom Module
version: 1.0.0
author: Your Name
main: com.example.module.MyModule
minimum-borealcore-version: 1.1.9
plugin-dependencies:
  - PlaceholderAPI
  - Towny
```

**Field Descriptions:**

* `id`: Unique identifier for your module (use lowercase, no spaces)
* `name`: Display name shown in console logs
* `version`: Semantic version (major.minor.patch)
* `author`: Your name or organization
* `main`: Fully qualified class name implementing `BorealModule`
* `minimum-borealcore-version`: Minimum BC version your module requires

### 4. Implement BorealModule Interface

Create your main module class:

```java
package com.example.module;

import plugin.borealcore.api.module.ModuleContext;
import plugin.borealcore.api.module.BorealModule;
import plugin.borealcore.database.DatabaseManager;
import org.bukkit.event.Listener;
import org.bukkit.configuration.ConfigurationSection;
import java.util.HashMap;
import java.util.Map;

public class MyModule implements BorealModule, Listener {

    private ModuleContext context;
    private DatabaseManager databaseManager;
    private ConfigurationSection config;

    @Override
    public void onModuleInitialize(ModuleContext context) throws Exception {
        this.context = context;
        this.databaseManager = context.getDatabaseManager();
        
        // Set up configuration defaults
        Map<String, Object> defaults = new HashMap<>();
        defaults.put("enabled", true);
        defaults.put("messages.prefix", "&7[&bMyModule&7] ");
        
        // This registers defaults and loads the config section
        this.config = context.setupModuleDefaults("modules.my-module", defaults);
        
        context.getLogger().info("Initializing My Custom Module");
    }

    @Override
    public void onModuleEnable() throws Exception {
        if (!config.getBoolean("enabled")) {
            context.getLogger().info("My Custom Module is disabled in config.");
            return;
        }

        // Register event listeners
        context.getPluginManager().registerEvents(this, context.getPlugin());

        // Register a custom action with the EffectManager
        EffectManager.registerAction(
                "ignite", // The config key (e.g., ignite: 5)
                IgniteActionImpl.class,

                // 1. How to parse it from the config
                (section, key, nick, perfect) -> new IgniteActionImpl(section.getInt(key)),

                // 2. How to render it in lore (Optional, can be null)
                action -> List.of(AdventureUtil.getComponentFromMiniMessage("<red>Ignites target for " + action.getSeconds() + "s"))
        );
        
        context.getLogger().info("My Custom Module has been enabled!");
    }

    @Override
    public void onModuleDisable() throws Exception {
        // Cleanup resources
        context.getLogger().info("My Custom Module has been disabled!");
    }
}

```

### 5. Access BorealCore Resources

The `ModuleContext` provides access to core resources:

```java
@Override
public void onModuleInitialize(ModuleContext context) throws Exception {
    // Get the main plugin instance
    BorealCore plugin = context.getPlugin();
    
    // Get the dynamic Database Manager
    DatabaseManager databaseManager = context.getDatabaseManager();
    
    // Get the logger
    Logger logger = context.getLogger();
    
    // Get the plugin manager (for registering listeners)
    PluginManager pluginManager = context.getPluginManager();
    
    // Get the Placeholder manager (for PAPI expansions)
    PlaceholderManager placeholderManager = context.getPlaceholderManager();
}

```

### 6. Configuration Management

BorealCore provides a standardized configuration API through the `ModuleContext`. You can choose to save your module's settings either as a section within the main `config.yml` or as a standalone file.

#### Setting up Defaults

Always use `setupModuleDefaults` to ensure your configuration keys exist without overwriting user changes:

```java
Map<String, Object> defaults = new HashMap<>();
defaults.put("setting-one", 100);
defaults.put("setting-two", "value");

// Option A: Save inside the main config.yml under a sub-section
// The identifier "modules.my-module" will create a section in config.yml
ConfigurationSection sectionConfig = context.setupModuleDefaults("modules.my-module", defaults);

// Option B: Save as a standalone file
// Providing an identifier ending with ".yml" creates a new file in the data folder
ConfigurationSection fileConfig = context.setupModuleDefaults("my-module.yml", defaults);

```

#### Reading and Saving

If you need to retrieve or save configuration data manually during runtime:

```java
// Fetch the configuration manually
ConfigurationSection myConfig = context.getModuleConfig("my-module.yml");

// Update a value and save it back to disk
myConfig.set("setting-one", 200);
context.saveModuleConfig("my-module.yml", myConfig);

```

### 7. Database Access

BorealCore provides a dynamic `DatabaseManager` that grants modules their own isolated SQLite database files. Modules are entirely responsible for defining their own tables and executing their own queries.

```java
// ... Inside your module or DAO class ...

@Override
public void onModuleInitialize(ModuleContext context) throws Exception {
    DatabaseManager dbManager = context.getDatabaseManager();
    
    // 1. Request a connection to your module's specific database file (creates my_module_data.db)
    Connection conn = dbManager.getConnection("my_module_data");
    
    if (conn == null) {
        context.getLogger().severe("Failed to connect to module database!");
        return;
    }

    // 2. Initialize your module's schema (Only runs if the table doesn't exist)
    String createTable = "CREATE TABLE IF NOT EXISTS my_custom_table ("
            + "`uuid` VARCHAR(36) NOT NULL,"
            + "`score` INTEGER NOT NULL,"
            + "PRIMARY KEY (`uuid`)"
            + ");";

    // Use try-with-resources to automatically close the Statement
    try (Statement statement = conn.createStatement()) {
        statement.execute(createTable);
    } catch (SQLException e) {
        e.printStackTrace();
    }
}

public int getPlayerScore(String uuid) {
    // Retrieve the cached connection
    Connection conn = context.getDatabaseManager().getConnection("my_module_data");
    if (conn == null) return 0;

    String query = "SELECT score FROM my_custom_table WHERE uuid = ?;";
    
    // 3. Execute queries using try-with-resources to automatically close statements and result sets
    try (PreparedStatement ps = conn.prepareStatement(query)) {
        ps.setString(1, uuid);
        
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("score");
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    
    return 0;
}

```

**Important Database Rules for Modules:**

1. **Own Your Schema:** The BorealCore plugin will not create tables for you. Always run your `CREATE TABLE IF NOT EXISTS` queries when your module initializes.
2. **Do NOT Close the `Connection`:** The `DatabaseManager` caches and shares the `Connection` object. If you call `conn.close()`, you will lock your module out of the database. BorealCore will close the connection safely when the server shuts down.
3. **Always Close Statements & ResultSets:** Use Java's `try-with-resources` block (as shown above) to ensure your `PreparedStatement` and `ResultSet` objects are closed automatically to prevent memory leaks.

## Deployment

### Building Your Module

Build the JAR file:

```bash
mvn clean package

```

This creates `my-module-1.0.0.jar` in the `target/` directory.

### Installing Your Module

1. Create the modules directory if it doesn't exist:

```text
plugins/BorealCore/modules/

```

2. Copy your module JAR into the directory:

```text
cp target/my-module-1.0.0.jar plugins/BorealCore/modules/

```

3. Restart the server
4. Check the console for module load messages:

```text
[BorealCore] Loaded module: My Custom Module v1.0.0 by Your Name (requires BC 1.1.9)
[BorealCore] Enabled module: my-module

```

## Event Listeners

Your module can implement Bukkit's `Listener` interface to handle events:

```java
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class MyModule implements BorealModule, Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        context.getLogger().info(event.getPlayer().getName() + " joined!");
    }

    @Override
    public void onModuleEnable() throws Exception {
        context.getPluginManager().registerEvents(this, context.getPlugin());
    }
}

```

## Error Handling

If your module fails to initialize, it will be skipped and logged:

```text
[SEVERE] Failed to enable module: my-module
java.lang.Exception: ...

```

Always wrap code that might throw exceptions:

```java
@Override
public void onModuleEnable() throws Exception {
    try {
        // Your code here
    } catch (Exception e) {
        context.getLogger().log(java.util.logging.Level.SEVERE, "Failed to enable", e);
        throw e;
    }
}

```

## Best Practices

1. **Use unique module IDs** - Follow Java package naming conventions (reverse domain notation)
2. **Handle startup failures gracefully** - Catch exceptions and log issues
3. **Clean up resources** - Always implement `onModuleDisable()` properly
4. **Utilize Context Configuration APIs** - Use `setupModuleDefaults` instead of manual File writing to ensure proper integration with BorealCore's config caches and systems.
5. **Don't access static fields from other modules** - Use database for communication
6. **Version your module properly** - Use semantic versioning (major.minor.patch)
7. **Test with multiple BC versions** - Ensure compatibility with your minimum version

## Support

For issues or questions:

* Check the example modules included with BorealCore (like the ConfigEditorModule)
* Review the BorealCore source code
* Check the module logs for error messages

```

```