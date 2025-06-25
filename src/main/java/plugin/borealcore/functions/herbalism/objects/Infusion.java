package plugin.borealcore.functions.herbalism.objects;

import plugin.borealcore.action.Action;

public class Infusion {

    protected Action[] effects;
    protected Herb[] ingredients;

    public Infusion() {
        this.ingredients = ingredients;
        this.effects = effects;
    }

    public Herb[] getIngredients() {
        return ingredients;
    }

    public Action[] getEffects() {
        return effects;
    }
}
