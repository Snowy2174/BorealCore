package plugin.borealcore.brewery;

import plugin.borealcore.api.module.BorealModule;
import plugin.borealcore.api.module.ModuleContext;
import plugin.borealcore.functions.cooking.object.Recipe;

import java.util.HashMap;

public class BreweryModule implements BorealModule {

    public BreweryModule() {
    }

    public static HashMap<String, Recipe> RECIPES;

    @Override
    public void onModuleEnable() {

        RECIPES = new HashMap<>();
        // @TODO: loadItems();
        //AdventureUtil.consoleMessage("Loaded <green>" + (RECIPES.size()) + " <gray> brewing recipes");
    }

    @Override
    public void onModuleDisable() throws Exception {

    }

    @Override
    public void onModuleInitialize(ModuleContext context) throws Exception {

    }
}
