# BorealCore Module Development Kit (ADK)

## Overview

The BorealCore Module Development Kit allows external developers to create modular plugins that integrate seamlessly
with the BorealCore plugin system. Modules are loaded dynamically at runtime and have full access to the core BorealCore
database manager, configuration APIs, centralized command registration, item enrichment APIs, and event system.

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
            <groupId>io.papermc.paper</groupId>
            <artifactId>paper-api</artifactId>
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
plugin-depends:
  - PlaceholderAPI
  - Towny
module-depends:
  - jade
  - market

```

**Field Descriptions:**

* `id`: Unique identifier for your module (use lowercase, no spaces)
* `name`: Display name shown in console logs
* `version`: Semantic version (major.minor.patch)
* `author`: Your name or organization
* `main`: Fully qualified class name implementing `BorealModule`
* `minimum-borealcore-version`: Minimum BC version your module requires
* `plugin-depends`: A list of Bukkit/Spigot plugins that must be enabled for this module to load.
* `module-depends`: A list of other BorealCore module ids that must be loaded before this module.

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
        
        context.getLogger().info("My Custom Module has been enabled!");
    }

    @Override
    public void onModuleDisable() throws Exception {
        // Cleanup resources (cancel specific tasks, clear caches, etc.)
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
    
    // Get the Item Enrichment Manager (for global item manipulation)
    ItemEnrichmentManager itemManager = context.getItemEnrichmentManager();
}

```

### 6. Configuration Management

BorealCore provides a standardized configuration API through the `ModuleContext`. You can choose to save your module's
settings either as a section within the main `config.yml` or as a standalone file.

#### Setting up Configuration Defaults

Always use `setupModuleDefaults()` to ensure your configuration keys exist without overwriting user changes. Define all
defaults in a Map for clean, centralized management:

```java
Map<String, Object> defaults = new HashMap<>();
defaults.put("setting-one", 100);
defaults.put("setting-two", "value");
defaults.put("section.nested-setting", "nested-value");

// Option A: Save inside the main config.yml (recommended for core settings)
// The identifier "config.yml" integrates into the main file
ConfigurationSection config = context.setupModuleDefaults("config.yml", defaults);

// Option B: Save as a standalone file (recommended for module-specific settings)
// Any identifier ending with ".yml" creates a new file in the data folder
ConfigurationSection config = context.setupModuleDefaults("my-module.yml", defaults);

// Option C: Save within a section of config.yml
// Use dot notation for nested organization
ConfigurationSection config = context.setupModuleDefaults("modules.my-module", defaults);

```

#### Setting up Message Defaults

Similarly, use `setupModuleMessages()` for your module's messages. This loads defaults into the messages file and
ensures consistent message handling:

```java
Map<String, String> messageDefaults = new HashMap<>();
messageDefaults.put("greeting", "Hello {player}!");
messageDefaults.put("farewell", "Goodbye {player}!");
messageDefaults.put("error-occurred", "<red>An error occurred: {error}");

// Setup defaults (messages are always stored in messages_<lang>.yml)
// Note: Do NOT include "messages." prefix - it's added automatically
ConfigurationSection messages = context.setupModuleMessages(messageDefaults);

// Access messages (they're returned as a ConfigurationSection with "messages." prefix)
String greeting = messages.getString("messages.greeting", "Hello {player}!");

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

#### Using Loader Pattern for Best Practices

For cleaner code organization, create dedicated loader classes for your module's config and messages:

**MyModuleConfigLoader.java:**

```java
public class MyModuleConfigLoader {
    public static void load(ModuleContext context) {
        Map<String, Object> defaults = new HashMap<>();
        defaults.put("setting-one", 100);
        defaults.put("setting-two", "value");
        
        ConfigurationSection config = context.setupModuleDefaults("config.yml", defaults);
        
        // Load into your static config class
        MyModuleConfig.settingOne = config.getInt("setting-one", 100);
        MyModuleConfig.settingTwo = config.getString("setting-two", "value");
    }
}

```

**In your module's onModuleEnable():**

```java
@Override
public void onModuleEnable() throws Exception {
    MyModuleConfigLoader.load(context);
    MyModuleMessageLoader.load(context);
    
    // Rest of initialization...
}

```

### 7. Database Access

BorealCore provides a dynamic `DatabaseManager` that grants modules their own isolated SQLite database files. Modules
are entirely responsible for defining their own tables and executing their own queries.

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

```

**Important Database Rules for Modules:**

1. **Own Your Schema:** The BorealCore plugin will not create tables for you. Always run your
   `CREATE TABLE IF NOT EXISTS` queries when your module initializes.
2. **Do NOT Close the `Connection`:** The `DatabaseManager` caches and shares the `Connection` object. If you call
   `conn.close()`, you will lock your module out of the database.
3. **Always Close Statements & ResultSets:** Use Java's `try-with-resources` block to ensure objects are closed
   automatically to prevent memory leaks.

## 8. Inter-Module Dependencies and Load Order

If your module acts as an add-on or requires API access to another BorealCore module, you must define it in the
`module-depends` list in your `module.yml`.

BorealCore features an intelligent Dependency Graph Resolver. When loading, it scans all modules and guarantees the
following lifecycle safety:

* **Strict Load Order:** Modules you depend on will be fully initialized and enabled before your module's `onModuleInitialize()` or `onModuleEnable()` methods are called.
* **Strict Unload Order:** When the server stops or modules are reloaded, BorealCore disables modules in reverse order. Your module will be safely disabled before the modules you depend on are wiped.
* **Circular Dependency Protection:** If Module A depends on B, and B depends on A, BorealCore will safely abort loading to prevent a stack overflow crash and print an error to the console.
* **Missing Dependency Protection:** If you depend on a module that is not installed or failed to load, your module will not attempt to start.

**Accessing Another Module:**
Once your dependencies are guaranteed by `module-depends`, you can safely interact with them. For example, if you depend on a market module:

```java
@Override
public void onModuleEnable() throws Exception {
    // Because we defined 'market' in module-depends, we can guarantee it exists
    // and is fully enabled at this point.
    MarketModule market = (MarketModule) ModuleRegistry.getInstance().getModule("market");
    
    if (market != null) {
        market.registerCustomCategory("MyNewCategory");
    }
}

```

## 9. Command Registration (Paper Brigadier)

BorealCore manages command registration centrally to prevent memory leaks and ghost commands during runtime module
reloads. **Do not** register `LifecycleEvents.COMMANDS` listeners directly inside your modules.

Instead, construct your commands using Paper's Brigadier API and pass the compiled `LiteralCommandNode` to the
`ModuleContext` during the `onModuleInitialize` phase. BorealCore will automatically gather these nodes and register them safely. You can also supply optional descriptions and aliases directly through the wrapper.

```java
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import java.util.List;

// ... inside your module class ...

@Override
public void onModuleInitialize(ModuleContext context) throws Exception {
    this.context = context;

    // 1. Build your Brigadier command node
    LiteralCommandNode<CommandSourceStack> myCommandNode = Commands.literal("mycommand")
        .requires(source -> source.getSender().hasPermission("mymodule.use"))
        .executes(cmd -> {
            cmd.getSource().getSender().sendPlainMessage("Hello from My Custom Module!");
            return 1;
        })
        .build();

    // 2. Queue it with the ModuleContext
    // Option A: Just the node
    context.registerCommand(myCommandNode);
    // Option B: Node + Description
    context.registerCommand(myCommandNode, "A helpful description of what this does");
    // Option C: Node + Description + Aliases
    context.registerCommand(myCommandNode, "A helpful description", List.of("mycmd", "mc"));
    
    context.getLogger().info("Queued /mycommand for registration");
}

```

## 10. Item Enrichment API

BorealCore features a decoupled Registry/Provider pattern for generating items via `ItemUtil`. If your module needs to apply persistent NBT data, Custom Model Data, or add special lore components to specific items whenever they are generated across the server, you should implement an `ItemEnricher`.

By registering an enricher, your module natively hooks into `ItemUtil.build()` without needing to modify core utility classes.

```java
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import plugin.borealcore.api.item.ItemEnricher;
import net.kyori.adventure.text.Component;
import java.util.ArrayList;
import java.util.List;

// ... inside your module class ...

@Override
public void onModuleEnable() throws Exception {
    context.getItemEnrichmentManager().register(new ItemEnricher() {
        
        @Override
        public boolean handles(String itemKey) {
            // Check if your module is responsible for enriching this specific item string
            return itemKey.startsWith("custom_module_item_");
        }

        @Override
        public void enrich(ItemStack itemStack, String itemKey) {
            ItemMeta meta = itemStack.getItemMeta();
            if (meta == null) return;

            List<Component> lore = meta.lore();
            if (lore == null) lore = new ArrayList<>();

            // Inject custom lore or PersistentDataContainer tags dynamically
            lore.add(Component.text("<gray>Generated by My Module"));
            
            meta.lore(lore);
            itemStack.setItemMeta(meta);
        }
    });
}

```

## 11. Event Listeners

Your module can implement Bukkit's `Listener` interface to handle standard events:

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
    
    @Override
    public void onModuleDisable() throws Exception {
        // Essential: Unregister listeners and cancel tasks on disable!
        org.bukkit.event.HandlerList.unregisterAll(this);
    }
}

```

## 12. Deployment

### Building Your Module

Build the JAR file:

```bash
mvn clean package

```

This creates `my-module-1.0.0.jar` in the `target/` directory.

### Installing Your Module

1. Create the modules directory if it doesn't exist: `plugins/BorealCore/modules/`
2. Copy your module JAR into the directory
3. Restart the server or run the BorealCore reload command
4. Check the console for module load messages:

```text
[BorealCore] Loaded module: My Custom Module v1.0.0 by Your Name (requires BC 1.1.9)
[BorealCore] Enabled module: my-module

```

## 13. Error Handling

If your module fails to initialize, it will be skipped and logged safely by the core:

```text
[SEVERE] Failed to enable module: my-module
java.lang.Exception: ...

```

Always wrap dangerous startup code that might throw exceptions:

```java
@Override
public void onModuleEnable() throws Exception {
    try {
        // Your setup code here
    } catch (Exception e) {
        context.getLogger().log(java.util.logging.Level.SEVERE, "Failed to enable module components", e);
        throw e; // Throwing guarantees the core knows this module failed
    }
}

```

## 14. Best Practices

1. **Use unique module IDs** - Follow Java package naming conventions or unique plugin IDs to prevent conflicts.
2. **Centralize commands** - Pass Brigadier nodes to `context.registerCommand()`. Do not hook into server lifecycles manually.
3. **Use the ItemEnricher API** - Do not hardcode specific module lore checks inside generic utilities; register an enricher instead.
4. **Clean up resources** - Always cancel your module-specific `BukkitTasks` and unregister `Listener`s in `onModuleDisable()`.
5. **Organize configurations** - Use dedicated `ConfigLoader` and `MessageLoader` classes for clarity.
6. **Never hardcode config values** - Always use your populated config classes.
7. **Respect dependencies** - Don't access static fields from other modules unless you've declared them in `module-depends`.
8. **Use SetupModule* utilities** - Prefer `context.setupModuleDefaults()` over manual file writing to preserve user edits safely.
9. **Handle exceptions gracefully** - Always catch and log exceptions during startup to prevent entire server crashes.
10. **Use placeholder getRequiredPlugin()** - Implement the `getRequiredPlugin()` method to specify the required plugin dependency as BorealCore for placeholder addons!

## 15. Support

For issues or questions:

* Check the example modules included with BorealCore (like the `ConfigEditorModule`).
* Review the BorealCore source code interfaces.
* Just ask Snowy at the end of the day innit
* Check the server logs during start-up for detailed dependency and injection error messages.