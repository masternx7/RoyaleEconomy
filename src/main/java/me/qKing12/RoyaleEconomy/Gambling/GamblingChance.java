package me.qKing12.RoyaleEconomy.Gambling;

import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import me.qKing12.RoyaleEconomy.utils.Utils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

public class GamblingChance {
    public boolean losing;
    public String message;
    public String broadcastMessage;
    double percent;
    private double chance;

    public ItemStack item1;
    public ItemStack item2;
    public ItemStack item3;

    public GamblingChance(boolean losing, ConfigurationSection chanceCfg){
        this.losing=losing;
        if(chanceCfg.getKeys(false).contains("message"))
            message=Utils.chat(chanceCfg.getString("message"));
        percent=chanceCfg.getDouble(losing?"percent-to-lose":"percent-to-win");
        chance=chanceCfg.getDouble("chance-to-happen");
        if(chanceCfg.getKeys(false).contains("broadcast-message")){
            broadcastMessage= Utils.chat(chanceCfg.getString("broadcast-message"));
        }
        else
            broadcastMessage=null;
        item1= RoyaleEconomy.itemConstructor.getItem(chanceCfg.getString("items-to-display.item1"), " ", null);
        item2= RoyaleEconomy.itemConstructor.getItem(chanceCfg.getString("items-to-display.item2"), " ", null);
        item3= RoyaleEconomy.itemConstructor.getItem(chanceCfg.getString("items-to-display.item3"), " ", null);
    }

    public double getCoinsFromPercent(double amount){
        double percentToWork=amount;
        percentToWork*=percent;
        percentToWork/=100;
        if(losing)
            amount-=percentToWork;
        else
            amount+=percentToWork;
        return amount;
    }

    public double getChance(){
        return this.chance;
    }
}
