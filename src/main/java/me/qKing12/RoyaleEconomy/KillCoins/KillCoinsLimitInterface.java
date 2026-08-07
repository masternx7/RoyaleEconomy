package me.qKing12.RoyaleEconomy.KillCoins;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;



public interface KillCoinsLimitInterface {

    boolean hasReachedLimit(Player p, String mobType, Double coins, boolean custom);

    Double getTotalCoins(Player p);

    int getCount(Player p, String mobType);

    double getMoney(Player p, String mobType);

    void saveData();

    FileConfiguration getData();
}
