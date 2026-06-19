package plugin.borealcore.brewery;

import plugin.borealcore.api.module.BorealModule;
import plugin.borealcore.api.module.ModuleContext;
import plugin.borealcore.manager.EffectManager;

public class BreweryModule implements BorealModule {

    public BreweryModule() {
    }


    @Override
    public void onModuleEnable() {

        EffectManager.registerAction("reduce-drunkenness", DrunknessEffectImpl.class,
                (sec, key, nick, perfect) -> new DrunknessEffectImpl(sec.getInt(key)), null);

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
