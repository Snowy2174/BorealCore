package plugin.customcooking.cooking;


import plugin.customcooking.cooking.action.Action;

public class Product {

    public static Product EMPTY = new Product("null", "null", new Difficulty[]{new Difficulty(1, 1)}, 5000, 0, 0, 0);

    protected String key;
    protected String nick;
    protected Difficulty[] difficulty;
    protected Layout[] layout;
    protected int time;
    protected final int masteryreq;
    protected final int slot;
    protected Action[] successActions;
    protected Action[] failureActions;
    protected Action[] consumeActions;
    protected final double score;

    public Product(String key, String nick, Difficulty[] difficulty, int time, int masteryreq, int slot, double score) {
        this.key = key;
        this.nick = nick;
        this.difficulty = difficulty;
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

    public int getSlot() {
        return slot;
    }
    public double getScore() {
        return score;
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

    public Action[] getConsumeActions() {
        return consumeActions;
    }

    public void setConsumeActions(Action[] consumeActions) {
        this.consumeActions = consumeActions;
    }

    public void setFailureActions(Action[] failureActions) {
        this.failureActions = failureActions;
    }

}
