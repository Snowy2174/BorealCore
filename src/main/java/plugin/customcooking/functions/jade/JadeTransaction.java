package plugin.customcooking.functions.jade;

import java.time.LocalDateTime;

public class JadeTransaction {
    private final String player;
    private final double amount;
    private final String source;
    private final LocalDateTime timestamp;

    public JadeTransaction(String player, double amount, String source, LocalDateTime timestamp) {
        this.player = player;
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
}
