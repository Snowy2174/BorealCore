package plugin.customcooking.gui;

import dev.lone.itemsadder.api.CustomFurniture;
import dev.lone.itemsadder.api.FontImages.FontImageWrapper;
import fr.minuskube.inv.SmartInventory;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import plugin.customcooking.CustomCooking;
import plugin.customcooking.manager.configs.ConfigManager;
import plugin.customcooking.object.Function;
import plugin.customcooking.util.AdventureUtil;

import java.io.File;
import java.util.HashMap;
import java.util.Set;

import static plugin.customcooking.util.ConfigUtil.getConfig;

public class GuiManager extends Function {

    public static HashMap<String, Ingredient> INGREDIENTS;
    public static SmartInventory INGREDIENTS_MENU;

    @Override
    public void load() {
        INGREDIENTS = new HashMap<>();
        loadItems();
        INGREDIENTS_MENU = getIngredientsBook();
        AdventureUtil.consoleMessage("[CustomCooking] Loaded <green>" + (INGREDIENTS.size()) + " <gray>ingredients");
    }

    @Override
    public void unload() {
        if (INGREDIENTS != null) INGREDIENTS.clear();
    }

    public static SmartInventory getRecipeBook(CustomFurniture clickedFurniture) {
        return  SmartInventory.builder()
                .manager(CustomCooking.getInventoryManager())
                .id("recipeBook")
                .provider(new RecipeBookProvider(clickedFurniture))
                .size(6, 9)
                .title(ChatColor.WHITE + new FontImageWrapper(ConfigManager.recipeBookTextureNamespace).applyPixelsOffset(-16) + ChatColor.RESET + FontImageWrapper.applyPixelsOffsetToString( ChatColor.RESET + "Recipe Book", -190))
                .build();
    }

    public static SmartInventory getIngredientsBook() {
        return SmartInventory.builder()
                .manager(CustomCooking.getInventoryManager())
                .id("ingredientsMenu")
                .provider(new IngredientBookProvider())
                .size(6, 9)
                .title(ChatColor.WHITE + "Ingredients Menu")
                .build();
    }


    private void loadItems() {
            YamlConfiguration config = getConfig("ingredients.yml");
            Set<String> ingredients = config.getKeys(false);

            for (String key : ingredients) {

                ConfigurationSection ingredientSection = config.getConfigurationSection(key);

                Ingredient ingredient = new Ingredient(
                        key,
                        ingredientSection.getString("nick", key),
                        ingredientSection.getInt("slot", 1),
                        ingredientSection.getStringList("ingredients")
                );

                INGREDIENTS.put(key, ingredient);
            }
        }
    }
