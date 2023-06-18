package plugin.customcooking.gui;


import java.util.List;

public class Ingredient {

    protected String key;
    protected String nick;
    protected Integer slot;
    protected List<String> ingredients;


    public Ingredient(String key, String nick, int slot, List<String> ingredients) {
        this.key = key;
        this.nick = nick;
        this.slot = slot;
        this.ingredients = ingredients;
    }

    public String getKey() {
        return key;
    }
    public String getNick() {
        return nick;
    }

    public Integer getSlot() {
        return slot;
    }
    public List<String> getIngredients() {
        return ingredients;
    }
}
