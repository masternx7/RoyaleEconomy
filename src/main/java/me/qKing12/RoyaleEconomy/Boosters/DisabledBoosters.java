package me.qKing12.RoyaleEconomy.Boosters;

import me.qKing12.RoyaleEconomy.Shops.Shop;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class DisabledBoosters implements Boosters{

    public BoostersActive.Booster getBoosterPercentShopBuy(Player p, String shop){
        return null;
    }

    public BoostersActive.Booster getBoosterPercentShopSell(Player p, String shop){
        return null;
    }

    public BoostersActive.Booster getBoosterPercentKillCoins(Player p){
        return null;
    }

    public void sendMessage(Player p){

    }

    public void sendMessageForPlayer(CommandSender sender, Player p){

    }

    @Override
    public ArrayList<BoostersActive.Booster> getBoostersKillCoins() {
        return null;
    }

    @Override
    public ArrayList<BoostersActive.Booster> getBoostersShopBuy() {
        return null;
    }

    @Override
    public ArrayList<BoostersActive.Booster> getBoostersShopSell() {
        return null;
    }
}
