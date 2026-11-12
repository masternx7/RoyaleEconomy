package me.qKing12.RoyaleEconomy.DataManager;

import me.qKing12.RoyaleEconomy.RoyaleEconomy;

/**
 * A single entry from a bank transaction log.
 *
 * The line is stored exactly as it is shown in the bank menu. The
 * %transaction-countdown% placeholder is replaced when the line is displayed.
 */
public class BankTransaction {

    private final long timestamp;
    private final String line;

    public BankTransaction(long timestamp, String line) {
        this.timestamp = timestamp;
        this.line = line;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getLine() {
        return line;
    }

    public String format(long now) {
        return line.replace("%transaction-countdown%",
                RoyaleEconomy.messageHelper.formatTimeNotDetailed(now - timestamp));
    }
}
