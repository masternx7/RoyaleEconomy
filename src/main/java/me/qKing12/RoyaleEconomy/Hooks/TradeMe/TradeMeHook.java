package me.qKing12.RoyaleEconomy.Hooks.TradeMe;

import me.Zrips.TradeMe.Containers.AmountClickAction;
import me.Zrips.TradeMe.Containers.TradeAction;
import me.Zrips.TradeMe.TradeMe;
import me.qKing12.RoyaleEconomy.MultiCurrency.internal.Currency;
import me.qKing12.RoyaleEconomy.MultiCurrency.internal.MultiCurrencyHandler;

public class TradeMeHook {

    public TradeMeHook(){

        for(Currency currency : MultiCurrencyHandler.getCurrencies()) {
            TradeAction tradeAction = new TradeAction("RoyaleEconomy_"+currency.getCurrencyId(), AmountClickAction.Amounts, false);
            TradeMe.getInstance().addNewTradeMode(tradeAction, new CustomCurrencyTrade(TradeMe.getInstance(), currency));
        }

        // Reloads TradeMe config files to implement new trade mode
        TradeMe.getInstance().getConfigManager().reload();
    }
}
