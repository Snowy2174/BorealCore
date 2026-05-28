package plugin.borealcore.functions.cooking.competition.bossbar;

import net.kyori.adventure.bossbar.BossBar;
import org.jetbrains.annotations.NotNull;

public class BossBarConfig {

    private final String[] text;
    private final int interval;
    private final BossBar.Overlay overlay;
    private final BossBar.Color color;
    private final int rate;

    public BossBarConfig(String[] text, BossBar.Overlay overlay, BossBar.Color color, int rate, int interval) {
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

    public @NotNull BossBar.Overlay getOverlay() {
        return overlay;
    }

    public @NotNull BossBar.Color getColor() {
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
