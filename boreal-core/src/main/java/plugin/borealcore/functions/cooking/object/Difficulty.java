package plugin.borealcore.functions.cooking.object;

public record Difficulty(int timer, int speed) {
    @Override
    public int timer() {
        return timer;
    }

    @Override
    public int speed() {
        return speed;
    }
}
