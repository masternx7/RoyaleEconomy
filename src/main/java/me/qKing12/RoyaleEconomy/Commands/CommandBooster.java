package me.qKing12.RoyaleEconomy.Commands;

import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import org.bukkit.entity.Player;

import java.util.LinkedHashMap;
import java.util.Map;

public class CommandBooster {

    public static LinkedHashMap<String, Double> percents=new LinkedHashMap<>();

    public static double getAmountWithBooster(Player player, double amount){
        for(Map.Entry<String, Double> entry : percents.entrySet())
            if(player.hasPermission(entry.getKey()))
                return amount+amount*entry.getValue()/100;
        return amount;
    }

    public CommandBooster(){
        for(String key : RoyaleEconomy.boostersCfg.getConfigurationSection("command-boosters").getKeys(false)){
            percents.put(RoyaleEconomy.boostersCfg.getString("command-boosters."+key+".permission"), RoyaleEconomy.boostersCfg.getDouble("command-boosters."+key+".percent-boost"));
        }
    }
}
