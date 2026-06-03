package plugin.borealcore.config;

import org.bukkit.configuration.ConfigurationSection;
import plugin.borealcore.manager.ConfigManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Loads config-editor-specific messages from the messages file.
 *
 */
public class ConfigEditorMessageLoader {

    public static void load() {
        Map<String, String> messageDefaults = new HashMap<>();
        
        messageDefaults.put("config-editor-opening", "Opening Config Editor for {plugin}...");
        messageDefaults.put("config-editor-plugin-not-found", "Plugin '{plugin}' not found.");
        messageDefaults.put("config-editor-data-folder-not-found", "Data folder for {plugin} does not exist!");
        messageDefaults.put("config-editor-config-not-found", "Config file for {plugin} does not exist!");
        messageDefaults.put("config-editor-section-not-found", "Section {section} does not exist!");
        messageDefaults.put("config-editor-value-updated", "Value updated successfully.");
        messageDefaults.put("config-editor-invalid-value", "Invalid value: {error}");
        messageDefaults.put("config-editor-changes-saved", "Changes saved:");
        messageDefaults.put("config-editor-no-changes", "No changes to save.");
        messageDefaults.put("config-editor-saving-error", "Error saving config: {error}");
        messageDefaults.put("config-editor-reload-success", "Configuration for {plugin} reloaded successfully!");
        messageDefaults.put("config-editor-no-files", "No config files found in this directory.");
        messageDefaults.put("config-editor-reading-error", "Error reading folder: {error}");
        messageDefaults.put("config-editor-no-permission", "You don't have permission to use this command.");
        messageDefaults.put("config-editor-cancelled", "Cancelled");
        messageDefaults.put("config-editor-timed-out", "Timed out.");

        ConfigurationSection messages = ConfigManager.setupModuleMessages(messageDefaults);

        ConfigEditorMessage.configEditorOpening = messages.getString("messages.config-editor-opening", "Opening Config Editor for {plugin}...");
        ConfigEditorMessage.configEditorPluginNotFound = messages.getString("messages.config-editor-plugin-not-found", "Plugin '{plugin}' not found.");
        ConfigEditorMessage.configEditorDataFolderNotFound = messages.getString("messages.config-editor-data-folder-not-found", "Data folder for {plugin} does not exist!");
        ConfigEditorMessage.configEditorConfigNotFound = messages.getString("messages.config-editor-config-not-found", "Config file for {plugin} does not exist!");
        ConfigEditorMessage.configEditorSectionNotFound = messages.getString("messages.config-editor-section-not-found", "Section {section} does not exist!");
        ConfigEditorMessage.configEditorValueUpdated = messages.getString("messages.config-editor-value-updated", "Value updated successfully.");
        ConfigEditorMessage.configEditorInvalidValue = messages.getString("messages.config-editor-invalid-value", "Invalid value: {error}");
        ConfigEditorMessage.configEditorChangesSaved = messages.getString("messages.config-editor-changes-saved", "Changes saved:");
        ConfigEditorMessage.configEditorNoChanges = messages.getString("messages.config-editor-no-changes", "No changes to save.");
        ConfigEditorMessage.configEditorSavingError = messages.getString("messages.config-editor-saving-error", "Error saving config: {error}");
        ConfigEditorMessage.configEditorReloadSuccess = messages.getString("messages.config-editor-reload-success", "Configuration for {plugin} reloaded successfully!");
        ConfigEditorMessage.configEditorNoFiles = messages.getString("messages.config-editor-no-files", "No config files found in this directory.");
        ConfigEditorMessage.configEditorReadingError = messages.getString("messages.config-editor-reading-error", "Error reading folder: {error}");
        ConfigEditorMessage.configEditorNoPermission = messages.getString("messages.config-editor-no-permission", "You don't have permission to use this command.");
        ConfigEditorMessage.configEditorCancelled = messages.getString("messages.config-editor-cancelled", "Cancelled");
        ConfigEditorMessage.configEditorTimedOut = messages.getString("messages.config-editor-timed-out", "Timed out.");
    }
}



