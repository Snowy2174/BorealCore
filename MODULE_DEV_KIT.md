# BorealCore Module Development Kit (ADK)

## Overview

The BorealCore Module Development Kit allows external developers to create modular plugins that integrate seamlessly with the BorealCore plugin system. Modules are loaded dynamically at runtime and have full access to the core BorealCore database and event system.

## Getting Started

### 1. Project Setup

Create a new Maven project with the following structure:

```
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
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
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
            <url>https://hub.spigotmc.org/nexus/content/repositories/snapshots/</url>
        </repository>
    </repositories>

    <dependencies>
        <!-- BorealCore API -->
        <dependency>
            <groupId>plugin.borealcore</groupId>
            <artifactId>BorealCore</artifactId>
            <version>1.1.9</version>
            <scope>provided</scope>
        </dependency>

        <!-- Spigot API -->
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
```

**Field Descriptions:**
- `id`: Unique identifier for your module (use lowercase, no spaces)
- `name`: Display name shown in console logs
- `version`: Semantic version (major.minor.patch)
- `author`: Your name or organization
- `main`: Fully qualified class name implementing `BorealModule`
- `minimum-borealcore-version`: Minimum BC version your module requires

### 4. Implement BorealModule Interface

Create your main module class:

```java
package com.example.module;

import plugin.borealcore.api.module.ModuleContext;
import plugin.borealcore.api.module.BorealModule;
import plugin.borealcore.database.Database;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

public class MyModule implements BorealModule, Listener {

    private ModuleContext context;
    private Database database;

    @Override
    public String getModuleId() {
        return "my-module";
    }

    @Override
    public String getModuleName() {
        return "My Custom Module";
    }

    @Override
    public String getModuleVersion() {
        return "1.0.0";
    }

    @Override
    public String getModuleAuthor() {
        return "Your Name";
    }

    @Override
    public String getMinimumBorealCoreVersion() {
        return "1.1.9";
    }

    @Override
    public void onModuleInitialize(ModuleContext context) throws Exception {
        this.context = context;
        this.database = context.getDatabase();
        
        // Initialize your module here (load configs, setup data, etc.)
        context.getLogger().info("Initializing " + getModuleName());
    }

    @Override
    public void onModuleEnable() throws Exception {
        // Register event listeners
        context.getPluginManager().registerEvents(this, context.getPlugin());
        
        context.getLogger().info(getModuleName() + " has been enabled!");
    }

    @Override
    public void onModuleDisable() throws Exception {
        // Cleanup resources
        context.getLogger().info(getModuleName() + " has been disabled!");
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
    
    // Get the database
    Database database = context.getDatabase();
    
    // Get the logger
    Logger logger = context.getLogger();
    
    // Get the plugin manager (for registering listeners)
    PluginManager pluginManager = context.getPluginManager();
}
```

### 6. Database Access

Access the shared database to query jade data or store custom data:

```java
@Override
public void onModuleInitialize(ModuleContext context) throws Exception {
    Database db = context.getDatabase();
    
    // Execute queries
    String query = "SELECT * FROM jade_totals WHERE uuid = ?";
    // Use db methods to query
}
```

## Deployment

### Building Your Module

Build the JAR file:

```bash
mvn clean package
```

This creates `my-module-1.0.0.jar` in the `target/` directory.

### Installing Your Module

1. Create the modules directory if it doesn't exist:
   ```
   plugins/BorealCore/modules/
   ```

2. Copy your module JAR into the directory:
   ```
   cp target/my-module-1.0.0.jar plugins/BorealCore/modules/
   ```

3. Restart the server

4. Check the console for module load messages:
   ```
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

```
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
4. **Don't access static fields from other modules** - Use database for communication
5. **Version your module properly** - Use semantic versioning (major.minor.patch)
6. **Test with multiple BC versions** - Ensure compatibility with your minimum version

## Support

For issues or questions:
- Check the example modules included with BorealCore
- Review the BorealCore source code
- Check the module logs for error messages

