package me.qKing12.RoyaleEconomy.DataManager.Cache;

import org.bukkit.entity.Player;

public interface PlayerMoneyCache {

    void reloadBalances();

    void finalSave();

    double getBalance(String player);

    double getBalance(Player player);

    boolean addBalance(String player, double coins);

    int removeBalance(String player, double coins);

    boolean setBalance(String player, double coins);
}
