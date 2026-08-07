package me.qKing12.RoyaleEconomy.API;

import me.qKing12.RoyaleEconomy.MultiCurrency.internal.CurrencyBalanceTop;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

//Currency proxy class
public class Currency extends me.qKing12.RoyaleEconomy.MultiCurrency.Currency {
    private me.qKing12.RoyaleEconomy.MultiCurrency.internal.Currency currency;

    public Currency(me.qKing12.RoyaleEconomy.MultiCurrency.internal.Currency currency){
        super(currency);
        this.currency = currency;
    }

    public CurrencyBalanceTop getCurrencyBalanceTop() {
        return currency.getCurrencyBalanceTop();
    }

    public boolean hasDecimals() {
        return currency.hasDecimals();
    }

    public String getCurrencyId() {
        return currency.getCurrencyId();
    }

    public String getReceiveCurrencyMessage() {
        return currency.getReceiveCurrencyMessage();
    }

    public String getCurrencyName() {
        return currency.getCurrencyName();
    }

    public String getColor() {
        return currency.getColor();
    }

    public double getCoinsReference() {
        return currency.getCoinsReference();
    }

    public double getExchangePercent() {
        return currency.getExchangePercent();
    }

    public String getBalanceMessage() {
        return currency.getBalanceMessage();
    }

    public String getBypassExchangePermission() {
        return currency.getBypassExchangePermission();
    }

    public ArrayList<String> getCommandHelp() {
        return currency.getCommandHelp();
    }

    public ArrayList<String> getCommands() {
        return currency.getCommands();
    }

    public String getExchangePermission() {
        return currency.getExchangePermission();
    }

    public String getOwnPermission() {
        return currency.getOwnPermission();
    }

    public String getSendCurrencyMessage() {
        return currency.getSendCurrencyMessage();
    }

    public String getSendPermission() {
        return currency.getSendPermission();
    }

    public ItemStack getIcon() {
        return currency.getIcon();
    }

    public String formatMoney(double amount){
        return currency.formatMoney(amount);
    }

    public void addAmount(String uuid, double amount){
        currency.addAmount(uuid, amount);
    }

    public void removeAmountCache(String uuid, boolean sending){
        currency.removeAmountCache(uuid, sending);
    }

    public boolean removeAmount(String uuid, double amount){
        return currency.removeAmount(uuid, amount);
    }

    public void setAmount(String uuid, double amount){
        currency.setAmount(uuid, amount);
    }

    public ConcurrentHashMap<String, Double> getCacheAmounts() {
        return currency.getCacheAmounts();
    }

    public double getAmount(String uuid){
        return currency.getAmount(uuid);
    }

    public double calculateBuyFee(double coins){
        return currency.calculateBuyFee(coins);
    }
}
