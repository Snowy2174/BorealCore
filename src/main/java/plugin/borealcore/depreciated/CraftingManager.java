package plugin.borealcore.depreciated;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import plugin.borealcore.manager.configs.ConfigManager;
import plugin.borealcore.object.Function;
import plugin.borealcore.utility.AdventureUtil;
import plugin.borealcore.utility.ConfigUtil;
import plugin.borealcore.utility.InventoryUtil;

import java.util.HashMap;

public class CraftingManager extends Function {

    static HashMap<String, Recipe> RECIPES = new HashMap<>();

    @Override
    public void load() {
        registerRecipes();
        AdventureUtil.consoleMessage("Loaded " + RECIPES.size() + " recipes");
        for (String key : RECIPES.keySet()) {
            Bukkit.getServer().addRecipe(RECIPES.get(key));
        }
    }

    @Override
    public void unload() {
        RECIPES.clear();
    }

    public void registerRecipes() {
        YamlConfiguration recipes = ConfigUtil.getConfig("crafting.yml");
        recipes.getKeys(false).forEach(key -> {
            Recipe recipe = null;
            if (recipes.isConfigurationSection(key)) {
                String type = recipes.getString(key + ".type", "shaped").toLowerCase();
                if (type.equals("shaped")) {
                    ShapedRecipe shapedRecipe = new ShapedRecipe(ConfigManager.getNamespacedKey(key), InventoryUtil.build(key));
                    shapedRecipe.shape(recipes.getStringList(key + ".shape").toArray(new String[0]));
                    recipes.getConfigurationSection(key + ".ingredients").getKeys(false).forEach(ingredient -> {
                        shapedRecipe.setIngredient(ingredient.charAt(0), InventoryUtil.build(recipes.getString(key + ".ingredients." + ingredient)));
                    });
                    recipe = shapedRecipe;
                } else if (type.equals("shapeless")) {
                    ShapelessRecipe shapelessRecipe = new ShapelessRecipe(ConfigManager.getNamespacedKey(key), InventoryUtil.build(key));
                    recipes.getStringList(key + ".ingredients").forEach(ingredient -> {
                        shapelessRecipe.addIngredient(InventoryUtil.build(ingredient));
                    });
                    recipe = shapelessRecipe;
                }
            }
            RECIPES.put(key, recipe);
        });
    }
}
