package plugin.borealcore.functions.herbalism.objects;

public class Modifier {
    protected String name;
    protected int value;
    protected HerbalismType type;

    public Modifier(HerbalismType type, int value) {
        this.type = type;
        this.value = value;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public int getValue() {
        return value;
    }
    public void setValue(int value) {
        this.value = value;
    }
    public HerbalismType getType() {
        return type;
    }
    public void setType(HerbalismType type) {
        this.type = type;
    }
}
