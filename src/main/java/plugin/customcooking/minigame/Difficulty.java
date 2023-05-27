package plugin.customcooking.minigame;

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
