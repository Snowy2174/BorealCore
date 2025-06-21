package plugin.borealcore.functions.jade;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public class JadeTransaction {
    private final String player;
    private final double amount;
    private final String source;
    private final LocalDateTime timestamp;
    private UUID uuid;

    public JadeTransaction(String player, @NotNull UUID uuid, double amount, String source, LocalDateTime timestamp) {
        this.player = player;
        this.uuid = uuid;
        this.amount = amount;
        this.source = source;
        this.timestamp = timestamp;
    }

    public String getPlayer() {
        return player;
    }

    public double getAmount() {
        return amount;
    }

    public String getSource() {
        return source;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getUuid() {
        return String.valueOf(uuid);
    }
}
