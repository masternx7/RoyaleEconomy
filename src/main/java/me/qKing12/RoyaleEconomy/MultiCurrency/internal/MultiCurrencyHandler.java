package me.qKing12.RoyaleEconomy.MultiCurrency.internal;

import me.qKing12.RoyaleEconomy.Commands.dynamic.DynamicCommandsSetup;
import me.qKing12.RoyaleEconomy.MultiCurrencyShops.ShopPlayerCache;
import me.qKing12.RoyaleEconomy.MultiCurrencyShops.ShopsLoad;
import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import me.qKing12.RoyaleEconomy.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class MultiCurrencyHandler {
    private static ArrayList<Currency> currencies;

    public static Currency findCurrencyById(String id){
        for(Currency currency : currencies)
            if(currency.getCurrencyId().equalsIgnoreCase(id))
                return currency;
        return null;
    }

    protected static String noPlayerMessage;
    protected static String notEnoughMessage;
    protected static String sellMenuMessage;
    protected static String buyMenuMessage;
    protected static String invalidAmountMessage;

    protected static String noPermissionMessage;

    public static String getNoPermissionMessage() {
        return noPermissionMessage;
    }

    protected static String noOwnOtherPlayer;

    protected static ItemStack defaultCoinsIcon;
    protected static String defaultCoinsColor;
    protected static String defaultCoinsName;

    public static ItemStack getDefaultCoinsIcon() {
        return defaultCoinsIcon;
    }

    public static String getDefaultCoinsColor() {
        return defaultCoinsColor;
    }

    public static String getDefaultCoinsName() {
        return defaultCoinsName;
    }

    protected static ArrayList<String> inputMessage;

    protected static String convertSuccess;

    private static boolean usesMultiCurrency;

    public static boolean usesMultiCurrency() {
        return usesMultiCurrency;
    }

    public MultiCurrencyHandler(){
        usesMultiCurrency = RoyaleEconomy.multiCurrencyCfg.getBoolean("use-multicurrency");
        if(!usesMultiCurrency) {
            return;
        }
        currencies=new ArrayList<>();

        DynamicCommandsSetup.getCommandSettings().put("multi-currency", new CurrencyCommand());

        convertSuccess=Utils.chat(RoyaleEconomy.multiCurrencyCfg.getString("global-command-messages.convert-success"));

        inputMessage=new ArrayList<>();

        for(String line : RoyaleEconomy.multiCurrencyCfg.getStringList("exchange-input-message"))
            inputMessage.add(Utils.chat(line));

        noPermissionMessage=Utils.chat(RoyaleEconomy.multiCurrencyCfg.getString("global-command-messages.no-permission"));
        noOwnOtherPlayer=Utils.chat(RoyaleEconomy.multiCurrencyCfg.getString("no-own-send"));

        defaultCoinsIcon=RoyaleEconomy.itemConstructor.getItemFromMaterial(RoyaleEconomy.multiCurrencyCfg.getString("default.item-icon"));
        defaultCoinsColor=Utils.chat(RoyaleEconomy.multiCurrencyCfg.getString("default.currency-color"));
        defaultCoinsName=Utils.chat(RoyaleEconomy.multiCurrencyCfg.getString("default.currency-name"));

        noPlayerMessage= Utils.chat(RoyaleEconomy.multiCurrencyCfg.getString("global-command-messages.no-player"));
        notEnoughMessage=Utils.chat(RoyaleEconomy.multiCurrencyCfg.getString("global-command-messages.not-enough"));
        sellMenuMessage=Utils.chat(RoyaleEconomy.multiCurrencyCfg.getString("global-command-messages.sell-menu"));
        buyMenuMessage=Utils.chat(RoyaleEconomy.multiCurrencyCfg.getString("global-command-messages.buy-menu"));
        invalidAmountMessage=Utils.chat(RoyaleEconomy.multiCurrencyCfg.getString("global-command-messages.invalid-amount"));

        boolean hasPapi = Bukkit.getPluginManager().getPlugin("PlaceholderAPI")!=null;
        for(String currency : RoyaleEconomy.multiCurrencyCfg.getStringList("currency-ids-to-use")) {
            Currency currencyOb = new Currency(RoyaleEconomy.multiCurrencyCfg.getConfigurationSection("currencies." + currency), currency);
            currencies.add(currencyOb);
            if(hasPapi){
                new CurrencyPlaceholders(currencyOb).register();
            }
        }

        CurrencyExchangeMainMenu.loadConfigData();
        CurrencyExchangeMenu.loadConfigData();
        CurrencyExchangeSellBuyMenu.loadConfigData();

        new ShopsLoad();
        new ShopPlayerCache();
    }

    public static ArrayList<Currency> getCurrencies() {
        return currencies;
    }
}
