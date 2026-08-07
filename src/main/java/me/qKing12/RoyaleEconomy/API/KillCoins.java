package me.qKing12.RoyaleEconomy.API;

import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import org.bukkit.entity.Player;

public class KillCoins {

    public double getTotalCoins(Player p){
        return RoyaleEconomy.killCoinsMainHandle.killCoins.getTotalCoins(p);
    }

    public double getCoinsForKey(Player p, String key){
        return RoyaleEconomy.killCoinsMainHandle.killCoins.getMoney(p, key);
    }

    public double getCoinsForCustomKey(Player p, String customKey){
        return RoyaleEconomy.killCoinsMainHandle.killCoins.getMoney(p, "custom-kill-"+customKey);
    }

    public double getCountForKey(Player p, String key){
        return RoyaleEconomy.killCoinsMainHandle.killCoins.getCount(p, key);
    }

    public double getCountForCustomKey(Player p, String customKey){
        return RoyaleEconomy.killCoinsMainHandle.killCoins.getCount(p, "custom-kill-"+customKey);
    }

}
