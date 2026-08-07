package me.qKing12.RoyaleEconomy.MultiCurrency.internal.abstracts;

import me.qKing12.RoyaleEconomy.MultiCurrency.internal.CurrencyBalanceTop;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

public interface Currency {

    CurrencyBalanceTop getCurrencyBalanceTop();

    boolean hasDecimals();

    String getCurrencyId();

    String getReceiveCurrencyMessage();

    String getCurrencyName();

    String getColor();

    double getCoinsReference();

    int getExchangePercent();

    String getBalanceMessage();

    String getBypassExchangePermission();

    List<String> getCommandHelp();

    List<String> getCommands();

    String getExchangePermission();

    String getOwnPermission();

    String getSendCurrencyMessage();

    String getSendPermission();

    ItemStack getIcon();

    String formatMoney(double amount);

    void addAmount(String uuid, double amount);

    void removeAmountCache(String uuid, boolean sending);

    boolean removeAmount(String uuid, double amount);

    void setAmount(String uuid, double amount);

    Map<String, Double> getCacheAmounts();

    double getAmount(String uuid);

    double calculateBuyFee(double coins);

}
