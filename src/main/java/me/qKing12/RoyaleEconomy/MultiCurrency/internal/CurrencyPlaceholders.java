package me.qKing12.RoyaleEconomy.MultiCurrency.internal;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.qKing12.RoyaleEconomy.Commands.BalanceTopCommand;
import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import org.bukkit.entity.Player;

public class CurrencyPlaceholders extends PlaceholderExpansion {

    Currency currency;

    public CurrencyPlaceholders(Currency currency){
        this.currency=currency;
    }

    @Override
    public boolean persist(){
        return true;
    }

    @Override
    public boolean canRegister(){
        return true;
    }

    @Override
    public String getAuthor(){
        return RoyaleEconomy.plugin.getDescription().getAuthors().toString();
    }

    @Override
    public String getIdentifier(){
        return "reconomy-"+currency.getCurrencyId();
    }

    @Override
    public String getVersion(){
        return RoyaleEconomy.plugin.getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if(identifier.startsWith("balancetop_name_")){
            if (currency.getCurrencyBalanceTop() == null) {
                return "BalanceTop not enabled for currency";
            }

            try{
                int position = Integer.valueOf(identifier.substring(identifier.lastIndexOf('_')+1))-1;
                return currency.getCurrencyBalanceTop().getTop().get(position)[0];
            }catch(Exception x){
                return "N/A";
            }
        }
        else if(identifier.startsWith("balancetop_balance_")){
            if (currency.getCurrencyBalanceTop() == null) {
                return "BalanceTop not enabled for currency";
            }

            try{
                int position = Integer.valueOf(identifier.substring(identifier.lastIndexOf('_')+1))-1;
                return currency.formatMoney(Double.parseDouble(currency.getCurrencyBalanceTop().getTop().get(position)[1]));
            }catch(Exception x){
                return "N/A";
            }
        }
        else if(identifier.startsWith("balancetop_balance_no_commas_")){
            if (currency.getCurrencyBalanceTop() == null) {
                return "BalanceTop not enabled for currency";
            }

            try{
                int position = Integer.valueOf(identifier.substring(identifier.lastIndexOf('_')+1))-1;
                return currency.getCurrencyBalanceTop().getTop().get(position)[1];
            }catch(Exception x){
                return "N/A";
            }
        }

        if(player==null)
            return "";

        if(identifier.equals("balance_no_commas")){
            return String.format("%.2f", currency.getAmount(player.getUniqueId().toString()));
        }
        else if(identifier.equals("balance")){
            return currency.formatMoney(currency.getAmount(player.getUniqueId().toString()));
        }

        return null;
    }
}
