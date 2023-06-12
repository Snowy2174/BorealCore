package plugin.customcooking.competition.bossbar;

import org.bukkit.boss.BarColor;

public class BossBarConfig {

    private final String[] text;
    private final int interval;
    private final BossBarOverlay overlay;
    private final BarColor color;
    private final int rate;

    public BossBarConfig(String[] text, BossBarOverlay overlay, BarColor color, int rate, int interval) {
        this.text = text;
        this.overlay = overlay;
        this.color = color;
        this.rate = rate;
        this.interval = interval;
    }

    public String[] getText() {
        return text;
    }

    public int getInterval() {
        return interval;
    }

    public BossBarOverlay getOverlay() {
        return overlay;
    }

    public BarColor getColor() {
        return color;
    }

    public int getRate() {
        return rate;
    }
    public enum BossBarOverlay {
        NOTCHED_6,
        NOTCHED_10,
        NOTCHED_12,
        NOTCHED_20,
        PROGRESS
    }
}
