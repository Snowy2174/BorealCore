package plugin.customcooking.functions.cooking.object;


import net.kyori.adventure.text.Component;
import plugin.customcooking.action.Action;
import plugin.customcooking.functions.cooking.Difficulty;

import java.util.Collections;
import java.util.List;

public class Recipe {

    public static Recipe EMPTY = new Recipe("null", "null", Collections.singletonList(Collections.singletonList(Component.empty())), new Difficulty[]{new Difficulty(1, 1)}, Collections.singletonList("null"), "null", 5000, 0, 0, 0);
    protected final int masteryreq;
    protected final int slot;
    protected final double score;
    public Action[] perfectConsumeActions;
    protected String key;
    protected String nick;
    protected List<String> ingredients;
    protected String cookedItems;
    protected List<List<Component>> dishEffects;
    protected Difficulty[] difficulty;
    protected Layout[] layout;
    protected int time;
    protected Action[] successActions;
    protected Action[] failureActions;
    protected Action[] consumeActions;

    public Recipe(String key, String nick, List<List<Component>> dishEffects, Difficulty[] difficulty, List<String> ingredients, String cookedItems, int time, int masteryreq, int slot, double score) {
        this.key = key;
        this.nick = nick;
        this.dishEffects = dishEffects;
        this.difficulty = difficulty;
        this.ingredients = ingredients;
        this.cookedItems = cookedItems;
        this.time = time;
        this.masteryreq = masteryreq;
        this.slot = slot;
        this.score = score;
    }

    public String getKey() {
        return key;
    }

    public String getNick() {
        return nick;
    }

    public Difficulty[] getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty[] difficulty) {
        this.difficulty = difficulty;
    }

    public List<String> getIngredients() {
        return ingredients;
    }

    public String getCookedItems() {
        return cookedItems;
    }

    public List<List<Component>> getDishEffectsLore() {
        return dishEffects;
    }

    public Layout[] getLayout() {
        return layout;
    }

    public void setLayout(Layout[] layout) {
        this.layout = layout;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public int getSlot() {
        return slot;
    }

    public double getScore() {
        return score;
    }

    public int getMasteryreq() {
        return masteryreq;
    }

    public Action[] getSuccessActions() {
        return successActions;
    }

    public void setSuccessActions(Action[] successActions) {
        this.successActions = successActions;
    }

    public Action[] getFailureActions() {
        return failureActions;
    }

    public void setFailureActions(Action[] failureActions) {
        this.failureActions = failureActions;
    }

    public Action[] getConsumeActions() {
        return consumeActions;
    }

    public void setConsumeActions(Action[] consumeActions) {
        this.consumeActions = consumeActions;
    }

    public Action[] getPerfectConsumeActions() {
        return perfectConsumeActions;
    }

    public void setPerfectConsumeActions(Action[] perfectConsumeActions) {
        this.perfectConsumeActions = perfectConsumeActions;
    }

}
