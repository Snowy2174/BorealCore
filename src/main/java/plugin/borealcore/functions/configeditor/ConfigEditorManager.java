package plugin.borealcore.functions.configeditor;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.EmbedBuilder;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;
import plugin.borealcore.BorealCore;
import plugin.borealcore.manager.configs.MessageManager;
import plugin.borealcore.object.Function;
import plugin.borealcore.object.SimpleListener;
import plugin.borealcore.utility.AdventureUtil;
import plugin.borealcore.utility.ChatInputUtil;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class ConfigEditorManager extends Function implements CommandExecutor, TabCompleter {

    private final SimpleListener simpleListener;
    private final Permission configEditorPermission;
    private final Map<String, YamlConfiguration> configCache;
    private final Map<String, File> configFileCache;
    private final Map<Player, Map<String, Pair<Object, Object>>> changeLogMap;
    private final Map<Player, SmartInventoryContext> activeInventories;

    public ConfigEditorManager() {
        this.simpleListener = new SimpleListener(this);
        this.configEditorPermission = new Permission("borealcore.configeditor", PermissionDefault.OP);
        this.configCache = new HashMap<>();
        this.configFileCache = new HashMap<>();
        this.changeLogMap = new HashMap<>();
        this.activeInventories = new HashMap<>();
    }

    @Override
    public void load() {
        Bukkit.getPluginManager().registerEvents(this.simpleListener, BorealCore.getInstance());
        registerCommand();
        AdventureUtil.consoleMessage("Config Editor Module Enabled!");
    }

    @Override
    public void unload() {
        if (this.simpleListener != null) HandlerList.unregisterAll(this.simpleListener);
        AdventureUtil.consoleMessage("Config Editor Module Disabled!");
    }

    private void registerCommand() {
        BorealCore.getInstance().getCommand("configeditor").setExecutor(this);
        BorealCore.getInstance().getCommand("configeditor").setTabCompleter(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            AdventureUtil.consoleMessage(MessageManager.noConsole);
            return true;
        }

        if (!player.hasPermission(configEditorPermission)) {
            AdventureUtil.playerMessage(player, MessageManager.infoNegative + MessageManager.configEditorNoPermission);
            return true;
        }

        if (args.length == 0) {
            sendUsage(player);
            return true;
        }

        if (args[0].equalsIgnoreCase("browse")) {
            if (args.length < 2) {
                AdventureUtil.playerMessage(player, MessageManager.infoNegative + MessageManager.lackArgs);
                return true;
            }

            String pluginName = args[1];
            Plugin targetPlugin = Bukkit.getPluginManager().getPlugin(pluginName);

            if (targetPlugin == null) {
                AdventureUtil.playerMessage(player, MessageManager.infoNegative +
                        MessageManager.configEditorPluginNotFound.replace("{plugin}", pluginName));
                return true;
            }

            if (!hasEditPermission(player, targetPlugin)) {
                AdventureUtil.playerMessage(player, MessageManager.infoNegative + MessageManager.configEditorNoPermission);
                return true;
            }

            AdventureUtil.playerMessage(player, MessageManager.infoPositive +
                    MessageManager.configEditorOpening.replace("{plugin}", targetPlugin.getName()));
            openDataFolderScreen(player, targetPlugin, null);

            return true;
        }

        String pluginName = args[0];
        Plugin targetPlugin = Bukkit.getPluginManager().getPlugin(pluginName);

        if (targetPlugin == null) {
            AdventureUtil.playerMessage(player, MessageManager.infoNegative +
                    MessageManager.configEditorPluginNotFound.replace("{plugin}", pluginName));
            return true;
        }

        if (!hasEditPermission(player, targetPlugin)) {
            AdventureUtil.playerMessage(player, MessageManager.infoNegative + MessageManager.configEditorNoPermission);
            return true;
        }

        AdventureUtil.playerMessage(player, MessageManager.infoPositive +
                MessageManager.configEditorOpening.replace("{plugin}", targetPlugin.getName()));
        openPluginConfigScreen(player, targetPlugin, "@root", null, null, getChangeLog(player), false);

        return true;
    }

    private boolean hasEditPermission(Player player, Plugin plugin) {
        return player.hasPermission("borealcore.configeditor." + plugin.getName().toLowerCase()) ||
                player.hasPermission("borealcore.configeditor.*");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player player) || !player.hasPermission(configEditorPermission)) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            completions.add("browse");

            Arrays.stream(Bukkit.getPluginManager().getPlugins())
                    .filter(plugin -> hasEditPermission(player, plugin))
                    .map(Plugin::getName)
                    .forEach(completions::add);

            return completions.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 2 && args[0].equalsIgnoreCase("browse")) {
            return Arrays.stream(Bukkit.getPluginManager().getPlugins())
                    .filter(plugin -> hasEditPermission(player, plugin))
                    .map(Plugin::getName)
                    .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    private void sendUsage(Player player) {
        AdventureUtil.playerMessage(player, "Config Editor Usage:");
        AdventureUtil.playerMessage(player, "/configeditor <plugin> - Edit config.yml of the specified plugin");
        AdventureUtil.playerMessage(player, "/configeditor browse <plugin> - Browse the data folder of the specified plugin");
    }

    public void openDataFolderScreen(Player player, Plugin plugin, File folder) {
        Bukkit.getScheduler().runTaskAsynchronously(BorealCore.getInstance(), () -> {
            File dataFolder = folder != null ? folder : plugin.getDataFolder();
            boolean isRoot = dataFolder.getAbsolutePath().equals(plugin.getDataFolder().getAbsolutePath());

            if (!dataFolder.exists()) {
                AdventureUtil.playerMessage(player, MessageManager.infoNegative +
                        MessageManager.configEditorDataFolderNotFound.replace("{plugin}", plugin.getName()));
                return;
            }

            try {
                File[] filesArray = dataFolder.listFiles(file ->
                        file.isDirectory() || file.getName().endsWith(".yml"));

                if (filesArray == null || filesArray.length == 0) {
                    AdventureUtil.playerMessage(player, MessageManager.infoNegative + MessageManager.configEditorNoFiles);
                    return;
                }

                ConfigEditorGUI gui = new ConfigEditorGUI(
                        "Data Folder - " + plugin.getName(),
                        6,
                        filesArray,
                        (clickedFile) -> {
                            if (clickedFile.isDirectory()) {
                                openDataFolderScreen(player, plugin, clickedFile);
                            } else {
                                openPluginConfigScreen(player, plugin, "@root", null, clickedFile, getChangeLog(player), true);
                            }
                        },
                        null,
                        () -> {
                            if (!isRoot && folder != null && folder.getParentFile() != null) {
                                openDataFolderScreen(player, plugin, folder.getParentFile());
                            }
                        }
                );

                Bukkit.getScheduler().runTask(BorealCore.getInstance(), () -> {
                    gui.open(player);
                });

            } catch (Exception e) {
                AdventureUtil.playerMessage(player, MessageManager.infoNegative +
                        MessageManager.configEditorReadingError.replace("{error}", e.getMessage()));
                e.printStackTrace();
            }
        });
    }

    public void openPluginConfigScreen(
            Player player,
            Plugin plugin,
            String sectionPath,
            YamlConfiguration root,
            File file,
            Map<String, Pair<Object, Object>> changeLog,
            boolean fromDataFolderScreen
    ) {
        openPluginConfigScreen(player, plugin, sectionPath, root, file, changeLog, fromDataFolderScreen, null);
    }

    public void openPluginConfigScreen(
            Player player,
            Plugin plugin,
            String sectionPath,
            YamlConfiguration root,
            File file,
            Map<String, Pair<Object, Object>> changeLog,
            boolean fromDataFolderScreen,
            Runnable afterSaveAction
    ) {
        Bukkit.getScheduler().runTaskAsynchronously(BorealCore.getInstance(), () -> {
            try {
                File configFile = file != null ? file : new File(plugin.getDataFolder(), "config.yml");
                if (!configFile.exists()) {
                    AdventureUtil.playerMessage(player, MessageManager.infoNegative +
                            MessageManager.configEditorConfigNotFound.replace("{plugin}", plugin.getName()));
                    return;
                }

                String cacheKey = plugin.getName() + ":" + configFile.getAbsolutePath();
                YamlConfiguration config = root != null ? root : configCache.computeIfAbsent(cacheKey, k -> YamlConfiguration.loadConfiguration(configFile));
                configFileCache.putIfAbsent(cacheKey, configFile);

                ConfigurationSection section = config;
                if (!sectionPath.equals("@root")) {
                    section = config.getConfigurationSection(sectionPath);
                    if (section == null) {
                        AdventureUtil.playerMessage(player, MessageManager.infoNegative +
                                MessageManager.configEditorSectionNotFound.replace("{section}", sectionPath));
                        openPluginConfigScreen(player, plugin, "@root", config, configFile, changeLog, fromDataFolderScreen);
                        return;
                    }
                }

                ConfigEditorGUI gui = new ConfigEditorGUI(
                        "Config Editor - " + plugin.getName(),
                        6,
                        section,
                        sectionPath,
                        (key, value, isSection, isRightClick) -> {
                            if (isRightClick && !isSection) {
                                restoreValue(player, plugin, sectionPath, key, value, config, configFile, changeLog, fromDataFolderScreen);
                            } else if (isSection) {
                                String newPath = sectionPath.equals("@root") ? key : sectionPath + "." + key;
                                openPluginConfigScreen(player, plugin, newPath, config, configFile, changeLog, fromDataFolderScreen);
                            } else {
                                editValue(player, plugin, sectionPath, key, value, config, configFile, changeLog, fromDataFolderScreen);
                            }
                        },
                        () -> {
                            if (sectionPath.equals("@root")) {
                                if (fromDataFolderScreen) {
                                    openDataFolderScreen(player, plugin, configFile.getParentFile());
                                }
                            } else {
                                String parentPath = sectionPath.contains(".") ?
                                        sectionPath.substring(0, sectionPath.lastIndexOf(".")) : "@root";
                                openPluginConfigScreen(player, plugin, parentPath, config, configFile, changeLog, fromDataFolderScreen);
                            }
                        },
                        () -> {
                            saveConfig(player, plugin, config, configFile, changeLog, afterSaveAction);
                        },
                        changeLog
                );

                Bukkit.getScheduler().runTask(BorealCore.getInstance(), () -> {
                    activeInventories.put(player, new SmartInventoryContext(
                            gui, plugin, sectionPath, config, configFile, fromDataFolderScreen
                    ));
                    gui.open(player);
                });

            } catch (Exception e) {
                AdventureUtil.playerMessage(player, MessageManager.infoNegative +
                        MessageManager.configEditorReadingError.replace("{error}", e.getMessage()));
                e.printStackTrace();
            }
        });
    }

    private void editValue(
            Player player,
            Plugin plugin,
            String sectionPath,
            String key,
            Object value,
            YamlConfiguration config,
            File configFile,
            Map<String, Pair<Object, Object>> changeLog,
            boolean fromDataFolderScreen
    ) {
        String fullPath = sectionPath.equals("@root") ? key : sectionPath + "." + key;
        Object originalValue = changeLog.containsKey(fullPath) ? changeLog.get(fullPath).getFirst() : value;

        if (value instanceof Boolean) {
            boolean newValue = !((Boolean) value);

            if (newValue == (originalValue instanceof Boolean ? (Boolean) originalValue : false)) {
                changeLog.remove(fullPath);
            } else {
                changeLog.put(fullPath, new Pair<>(originalValue, newValue));
            }

            if (sectionPath.equals("@root")) {
                config.set(key, newValue);
            } else {
                config.set(sectionPath + "." + key, newValue);
            }

            // Refresh the GUI instead of reopening it
            refreshInventory(player);
            return;
        }

        // For non-boolean values, we need to get chat input
        player.closeInventory();

        String messageText = "<green>Enter new value for " + key + ":\n" +
                "<yellow>Current value: <white>" + value.toString() + "\n" +
                "<green>[Click to copy current value]";

        ChatInputUtil.getChatInput(player, messageText, value.toString(), (input) -> {
            if (input == null) {
                // Reopen the GUI if canceled
                reopenLastInventory(player);
                return;
            }

            try {
                Object newValue = ValueConverter.convertValue(input, value);

                if (newValue.equals(originalValue)) {
                    changeLog.remove(fullPath);
                } else {
                    changeLog.put(fullPath, new Pair<>(originalValue, newValue));
                }

                if (sectionPath.equals("@root")) {
                    config.set(key, newValue);
                } else {
                    config.set(sectionPath + "." + key, newValue);
                }

                AdventureUtil.playerMessage(player, MessageManager.infoPositive + MessageManager.configEditorValueUpdated);
            } catch (Exception e) {
                AdventureUtil.playerMessage(player, MessageManager.infoNegative +
                        MessageManager.configEditorInvalidValue.replace("{error}", e.getMessage()));
            }

            // Reopen the GUI after editing
            reopenLastInventory(player);
        });
    }

    private void restoreValue(
            Player player,
            Plugin plugin,
            String sectionPath,
            String key,
            Object value,
            YamlConfiguration config,
            File configFile,
            Map<String, Pair<Object, Object>> changeLog,
            boolean fromDataFolderScreen
    ) {
        String fullPath = sectionPath.equals("@root") ? key : sectionPath + "." + key;

        if (changeLog.containsKey(fullPath)) {
            Object originalValue = changeLog.get(fullPath).getFirst();

            if (sectionPath.equals("@root")) {
                config.set(key, originalValue);
            } else {
                config.set(sectionPath + "." + key, originalValue);
            }

            changeLog.remove(fullPath);
            AdventureUtil.playerMessage(player, MessageManager.infoPositive + "Value restored to original.");

            // Refresh the GUI instead of reopening it
            refreshInventory(player);
        }
    }

    private void refreshInventory(Player player) {
        SmartInventoryContext context = activeInventories.get(player);
        if (context != null && player.getOpenInventory() != null) {
            Bukkit.getScheduler().runTask(BorealCore.getInstance(), () -> {
                context.gui.refresh(player);
            });
        }
    }

    private void reopenLastInventory(Player player) {
        SmartInventoryContext context = activeInventories.get(player);
        if (context != null) {
            openPluginConfigScreen(
                    player,
                    context.plugin,
                    context.sectionPath,
                    context.config,
                    context.configFile,
                    getChangeLog(player),
                    context.fromDataFolderScreen
            );
        }
    }

    private void saveConfig(
            Player player,
            Plugin plugin,
            YamlConfiguration config,
            File configFile,
            Map<String, Pair<Object, Object>> changeLog,
            Runnable afterSaveAction
    ) {
        try {
            config.save(configFile);

            if (!changeLog.isEmpty()) {
                AdventureUtil.playerMessage(player, MessageManager.infoPositive + MessageManager.configEditorChangesSaved);

                List<ConfigChange> changes = new ArrayList<>();
                for (Map.Entry<String, Pair<Object, Object>> entry : changeLog.entrySet()) {
                    AdventureUtil.playerMessage(player, MessageManager.infoPositive + entry.getKey() + ": " +
                            entry.getValue().getFirst() + " → " + entry.getValue().getSecond());

                    changes.add(new ConfigChange(
                            entry.getKey(),
                            entry.getValue().getFirst(),
                            entry.getValue().getSecond()
                    ));
                }

                // Only send Discord notification for ProjectKorra changes
                if (!changes.isEmpty() && plugin.getName().equalsIgnoreCase("ProjectKorra")) {
                    sendChangesToDiscord(player.getName(), configFile.getName(), changes);

                    // Run the "pk reload" command for ProjectKorra
                    Bukkit.getScheduler().runTask(BorealCore.getInstance(), () -> {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "pk reload");
                    });
                }

                changeLog.clear();

                // Refresh the GUI to show the cleared changelog
                refreshInventory(player);
            } else {
                AdventureUtil.playerMessage(player, MessageManager.infoPositive + MessageManager.configEditorNoChanges);
            }

            String cacheKey = plugin.getName() + ":" + configFile.getAbsolutePath();
            configCache.remove(cacheKey);

            if (afterSaveAction != null) {
                afterSaveAction.run();
            }

        } catch (Exception e) {
            AdventureUtil.playerMessage(player, MessageManager.infoNegative +
                    MessageManager.configEditorSavingError.replace("{error}", e.getMessage()));
            e.printStackTrace();
        }
    }

    private void sendChangesToDiscord(String editor, String fileName, List<ConfigChange> changes) {
        try {
            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("ProjectKorra Config Changes")
                    .setColor(0x00AA00)
                    .setFooter("Changes made by: " + editor, null)
                    .setTimestamp(new Date().toInstant());

            StringBuilder changesText = new StringBuilder();
            for (ConfigChange change : changes) {
                changesText.append("`").append(change.path).append("`: ")
                        .append(change.oldValue).append(" → ")
                        .append(change.newValue).append("\n");
            }

            if (changesText.length() > 0) {
                embed.addField(fileName, changesText.toString(), false);

                TextChannel textChannel = DiscordSRV.getPlugin().getDestinationTextChannelForGameChannelName("game-balancing");
                if (textChannel != null) {
                    textChannel.sendMessageEmbeds(embed.build()).queue();
                }
            }

        } catch (Exception e) {
            AdventureUtil.consoleMessage("Failed to send ProjectKorra changes to Discord: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Map<String, Pair<Object, Object>> getChangeLog(Player player) {
        return changeLogMap.computeIfAbsent(player, k -> new HashMap<>());
    }

    @Override
    public void onQuit(Player player) {
        changeLogMap.remove(player);
        activeInventories.remove(player);
    }

    public static class Pair<K, V> {
        private final K first;
        private final V second;

        public Pair(K first, V second) {
            this.first = first;
            this.second = second;
        }

        public K getFirst() {
            return first;
        }

        public V getSecond() {
            return second;
        }
    }

    private static class ConfigChange {
        private final String path;
        private final Object oldValue;
        private final Object newValue;

        public ConfigChange(String path, Object oldValue, Object newValue) {
            this.path = path;
            this.oldValue = oldValue;
            this.newValue = newValue;
        }
    }

    private static class SmartInventoryContext {
        private final ConfigEditorGUI gui;
        private final Plugin plugin;
        private final String sectionPath;
        private final YamlConfiguration config;
        private final File configFile;
        private final boolean fromDataFolderScreen;

        public SmartInventoryContext(
                ConfigEditorGUI gui,
                Plugin plugin,
                String sectionPath,
                YamlConfiguration config,
                File configFile,
                boolean fromDataFolderScreen
        ) {
            this.gui = gui;
            this.plugin = plugin;
            this.sectionPath = sectionPath;
            this.config = config;
            this.configFile = configFile;
            this.fromDataFolderScreen = fromDataFolderScreen;
        }
    }
}