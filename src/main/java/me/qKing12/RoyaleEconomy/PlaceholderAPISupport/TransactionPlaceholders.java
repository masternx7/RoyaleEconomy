package me.qKing12.RoyaleEconomy.PlaceholderAPISupport;

import me.qKing12.RoyaleEconomy.DataManager.BankTransaction;
import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Shared transaction placeholder cache used by both placeholder expansions.
 *
 * Index 1 is the newest transaction, matching the order used by the bank menu.
 * Placeholders:
 *   bank_transaction_count / transaction_count
 *   bank_transaction_<n> / transaction_<n>
 *   sharedbank_transaction_count
 *   sharedbank_transaction_<n>
 */
public final class TransactionPlaceholders {

    private static final ConcurrentHashMap<String, List<BankTransaction>> BANK = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, List<BankTransaction>> SHARED = new ConcurrentHashMap<>();

    private TransactionPlaceholders() {
    }

    public static String resolve(Player player, String identifier) {
        boolean shared;
        String suffix;

        if (identifier.startsWith("bank_transaction_")) {
            shared = false;
            suffix = identifier.substring("bank_transaction_".length());
        } else if (identifier.startsWith("sharedbank_transaction_")) {
            shared = true;
            suffix = identifier.substring("sharedbank_transaction_".length());
        } else if (identifier.startsWith("transaction_")) {
            shared = false;
            suffix = identifier.substring("transaction_".length());
        } else {
            return null;
        }

        List<BankTransaction> entries = get(player, shared);
        if (suffix.equalsIgnoreCase("count"))
            return String.valueOf(entries.size());

        int index;
        try {
            index = Integer.parseInt(suffix);
        } catch (NumberFormatException exception) {
            return null;
        }

        if (index < 1 || index > entries.size())
            return "";

        return entries.get(index - 1).format(System.currentTimeMillis());
    }

    public static void refresh(Player player) {
        String uuid = player.getUniqueId().toString();
        BANK.put(uuid, loadBankTransactions(uuid));
        SHARED.put(uuid, loadSharedTransactions(uuid));
    }

    public static void remove(Player player) {
        String uuid = player.getUniqueId().toString();
        BANK.remove(uuid);
        SHARED.remove(uuid);
    }

    private static List<BankTransaction> get(Player player, boolean shared) {
        String uuid = player.getUniqueId().toString();
        ConcurrentHashMap<String, List<BankTransaction>> cache = shared ? SHARED : BANK;
        List<BankTransaction> entries = cache.get(uuid);
        if (entries == null) {
            entries = shared ? loadSharedTransactions(uuid) : loadBankTransactions(uuid);
            cache.put(uuid, entries);
        }
        return entries;
    }

    private static List<BankTransaction> loadBankTransactions(String uuid) {
        try {
            return RoyaleEconomy.dataManager.getTransactionLogEntries(uuid);
        } catch (Exception exception) {
            return Collections.emptyList();
        }
    }

    private static List<BankTransaction> loadSharedTransactions(String uuid) {
        try {
            String bankID = RoyaleEconomy.dataManager.getSharedBankManager().getSharedBankId(uuid);
            if (bankID == null || bankID.isEmpty())
                return Collections.emptyList();
            return RoyaleEconomy.dataManager.getSharedBankManager().getSharedTransactionLogEntries(bankID);
        } catch (Exception exception) {
            return Collections.emptyList();
        }
    }
}
