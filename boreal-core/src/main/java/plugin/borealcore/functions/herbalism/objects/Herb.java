package plugin.borealcore.functions.herbalism.objects;

import org.bukkit.potion.PotionEffect;
import plugin.borealcore.action.Action;
import plugin.borealcore.functions.cooking.Difficulty;
import plugin.borealcore.functions.cooking.object.Layout;

import java.util.List;

public class Herb {
    protected Difficulty[] difficulty;
    protected Layout[] layout;
    protected HerbalismType type;
    protected String key;
    protected String nick;
    protected int time;
    protected Action[] actions;
    protected List<PotionEffect> effects;
    protected Modifier[] modifiers;
    protected int colour;

    public Herb(String key, String nick, HerbalismType type, Difficulty[] difficulties, int time) {
        this.key = key;
        this.nick = nick;
        this.type = type;
        this.difficulty = difficulties;
        this.time = time;
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

    public void setEffects(List<PotionEffect> effects) {
        this.effects = effects;
    }

    public void setModifiers(Modifier[] modifiers) {
        this.modifiers = modifiers;
    }

    public List<PotionEffect> getEffects() {
        return effects;
    }

    public Modifier[] getModifiers() {
        return modifiers;
    }

    public void setActions(Action[] actions) {
        this.actions = actions;
    }

    public Action[] getActions() {
        return actions;
    }

    public int getColour() {
        return colour;
    }

    public void setColour(int colour) {
        this.colour = colour;
    }
}
