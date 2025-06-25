package plugin.borealcore.functions.brewery;

import plugin.borealcore.functions.cooking.object.Recipe;
import plugin.borealcore.object.Function;
import plugin.borealcore.utility.AdventureUtil;

import java.util.HashMap;

public class BreweryManager extends Function {

    public BreweryManager() {}

    public static HashMap<String, Recipe> RECIPES;

    @Override
    public void load() {
        RECIPES = new HashMap<>();
        // @TODO: loadItems();
        AdventureUtil.consoleMessage("Loaded <green>" + (RECIPES.size()) + " <gray> brewing recipes");
    }

    @Override
    public void unload() {
        // clear map
    }

}
