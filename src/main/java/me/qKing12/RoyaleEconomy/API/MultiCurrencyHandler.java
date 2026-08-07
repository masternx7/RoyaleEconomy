package me.qKing12.RoyaleEconomy.API;

import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.stream.Collectors;

public class MultiCurrencyHandler {

    public static Currency findCurrencyById(String id){
        for(me.qKing12.RoyaleEconomy.MultiCurrency.internal.Currency currency : me.qKing12.RoyaleEconomy.MultiCurrency.internal.MultiCurrencyHandler.getCurrencies())
            if(currency.getCurrencyId().equalsIgnoreCase(id))
                return new Currency(currency);
        return null;
    }

    public static String getNoPermissionMessage() {
        return me.qKing12.RoyaleEconomy.MultiCurrency.internal.MultiCurrencyHandler.getNoPermissionMessage();
    }

    public static ItemStack getDefaultCoinsIcon() {
        return me.qKing12.RoyaleEconomy.MultiCurrency.internal.MultiCurrencyHandler.getDefaultCoinsIcon();
    }

    public static String getDefaultCoinsColor() {
        return me.qKing12.RoyaleEconomy.MultiCurrency.internal.MultiCurrencyHandler.getDefaultCoinsColor();
    }

    public static String getDefaultCoinsName() {
        return me.qKing12.RoyaleEconomy.MultiCurrency.internal.MultiCurrencyHandler.getDefaultCoinsName();
    }

    public static List<Currency> getCurrencies() {
        if(me.qKing12.RoyaleEconomy.MultiCurrency.internal.MultiCurrencyHandler.getCurrencies()==null)
            return null;
        return me.qKing12.RoyaleEconomy.MultiCurrency.internal.MultiCurrencyHandler.getCurrencies().stream().map(c -> new Currency(c)).collect(Collectors.toList());
    }
}
