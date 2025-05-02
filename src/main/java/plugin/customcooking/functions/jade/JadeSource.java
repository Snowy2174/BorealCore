package plugin.customcooking.functions.jade;

import java.time.LocalDateTime;

public class JadeSource {
    private final String name;
    private final long cooldown;
    private final int limit;

    public JadeSource(String name, long cooldown, int limit) {
        this.name = name;
        this.cooldown = cooldown;
        this.limit = limit;
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
}
