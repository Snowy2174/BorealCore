package plugin.customcooking.functions.jade;

import java.time.LocalDateTime;

public class JadeSource {
    private final String name;
    private final long cooldown;
    private final int limit;
    private final double rate;

    public JadeSource(String name, long cooldown, int limit, double rate) {
        this.name = name;
        this.cooldown = cooldown;
        this.limit = limit;
        this.rate = rate;
    }

    public String getName() {
        return name;
    }
    public long getCooldown() {
        return cooldown;
    }
    public int getLimit() {
        return limit;
    }
    public double getRate() {
        return rate;
    }
}
