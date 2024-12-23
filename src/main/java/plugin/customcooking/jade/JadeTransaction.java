package plugin.customcooking.jade;

import java.time.LocalDateTime;

public class JadeTransaction {
    private String player;
    private double amount;
    private String source;
    private LocalDateTime timestamp;

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
