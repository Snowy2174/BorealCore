package plugin.borealcore.functions.herbalism.objects;

import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import plugin.borealcore.action.Action;

import java.util.List;

import static plugin.borealcore.functions.herbalism.HerbalismManager.infusionItemStack;

public class Infusion {

    protected Action[] actions;
    protected List<PotionEffect> effects;
    protected Herb[] ingredients;
    protected int ingredientIndex = 0;
    protected Double quality;
    protected ItemStack itemStack;

    public Infusion(Herb[] herbs) {
        this.ingredients = ingredients;
        this.quality = 0.0;
    }

    public Herb[] getIngredients() {
        return ingredients;
    }

    public List<PotionEffect> getEffects() {
        return effects;
    }

    public void setPotionEffects(List<PotionEffect> effects) {
        this.effects = effects;
    }

    public int getIngredientIndex() {
        return ingredientIndex;
    }

    public void incrementIngredientIndex() {
        this.ingredientIndex++;
    }

    public void recalculateQuality(double stageModifier) {
        this.quality += stageModifier * 10 / this.ingredients.length;
    }

    public Double getQuality() {
        return quality;
    }

    public void setQuality(Double quality) {
        this.quality = quality;
    }

    public void buildStack() {
        this.itemStack = infusionItemStack(this);
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }
}
