package plugin.borealcore.functions.herbalism.objects;

public class Modifier {
    protected String name;
    protected int value;
    protected HerbalismType type;

    public Modifier(HerbalismType type, int value) {
        this.type = type;
        this.value = value;
    }
}
