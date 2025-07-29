package plugin.borealcore.functions.herbalism.objects;

public class Modifier {
    protected String name;
    protected int value;
    protected ModifierType type;

    public Modifier(ModifierType type, int value) {
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

    public ModifierType getType() {
        return type;
    }

    public void setType(ModifierType type) {
        this.type = type;
    }
}
