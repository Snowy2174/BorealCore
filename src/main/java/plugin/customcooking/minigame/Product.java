package plugin.customcooking.minigame;


public class Product {

    public static Product EMPTY = new Product("empty", new Difficulty[]{new Difficulty(1, 1)}, 5000);

    protected String key;
    protected String nick;
    protected Difficulty[] difficulty;
    protected Layout[] layout;
    protected int time;

    public Product(String key, Difficulty[] difficulty, int time) {
        this.key = key;
        this.difficulty = difficulty;
        this.time = time;
    }

    public String getKey() {
        return key;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
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

}
