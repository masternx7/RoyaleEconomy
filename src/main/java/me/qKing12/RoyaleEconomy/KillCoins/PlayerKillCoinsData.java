package me.qKing12.RoyaleEconomy.KillCoins;

import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;

public class PlayerKillCoinsData {
    HashMap<String, Integer> mobLimitsSaver = new HashMap<>();
    HashMap<String, Double> mobKillCoins = new HashMap<>();
    double totalCoins;

    public PlayerKillCoinsData(double totalCoins, ConfigurationSection mobLimits, ConfigurationSection mobKillCoins){
        this.totalCoins=totalCoins;
        if(mobLimits!=null) {
            for (String key : mobLimits.getKeys(false))
                mobLimitsSaver.put(key, mobLimits.getInt(key));
        }

        if(mobKillCoins!=null){
            for(String key : mobKillCoins.getKeys(false))
                this.mobKillCoins.put(key, mobKillCoins.getDouble(key));
        }
    }
}
