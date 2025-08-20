package plugin.borealcore.functions.cooking.object;

import plugin.borealcore.functions.cooking.Difficulty;

import java.util.List;

public class DroppedItem extends Recipe {
    public DroppedItem(String key, String nick, Difficulty[] difficulty, List<String> ingredients, String cookedItems, int time, int mastery, int slot, double score) {
        super(key, nick, difficulty, ingredients, cookedItems, time, mastery, slot, score);
    }
}

