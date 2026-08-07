package me.qKing12.RoyaleEconomy.Boosters;

import me.qKing12.RoyaleEconomy.Shops.Shop;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public interface Boosters {

    BoostersActive.Booster getBoosterPercentShopBuy(Player p, String shop);

    BoostersActive.Booster getBoosterPercentShopSell(Player p, String shop);

    BoostersActive.Booster getBoosterPercentKillCoins(Player p);

    void sendMessage(Player p);

    void sendMessageForPlayer(CommandSender sender, Player p);

    ArrayList<BoostersActive.Booster> getBoostersShopBuy();

    ArrayList<BoostersActive.Booster> getBoostersShopSell();

    ArrayList<BoostersActive.Booster> getBoostersKillCoins();

}
