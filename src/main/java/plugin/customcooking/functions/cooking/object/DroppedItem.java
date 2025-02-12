package plugin.customcooking.functions.cooking.object;

import net.kyori.adventure.text.Component;
import plugin.customcooking.functions.cooking.Difficulty;

import java.util.List;

public class DroppedItem extends Recipe {
    public DroppedItem(String key, String nick, List<List<Component>> dishEffects, Difficulty[] difficulty, List<String> ingredients, String cookedItems, int time, int mastery, int slot, double score) {
        super(key, nick, dishEffects, difficulty, ingredients, cookedItems, time, mastery, slot, score);
    }
}

