package me.qKing12.RoyaleEconomy.API;

import me.qKing12.RoyaleEconomy.DataManager.Cache.PlayerMoneyCacheSQL;
import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import me.qKing12.RoyaleEconomy.Menus.BankDepositMenu;
import me.qKing12.RoyaleEconomy.Menus.BankUpgradesMenu;
import me.qKing12.RoyaleEconomy.Menus.BankWithdrawMenu;
import me.qKing12.RoyaleEconomy.Menus.MainBankMenu;
import org.bukkit.entity.Player;

import java.util.ArrayList;

import static me.qKing12.RoyaleEconomy.RoyaleEconomy.bankUpgradesCfg;
import static me.qKing12.RoyaleEconomy.RoyaleEconomy.playerMoneyCache;

public class Bank {

    public void openBankMainMenu(Player p){
        new MainBankMenu(p);
    }

    public void openBankWithdrawMenu(Player p){
        new BankWithdrawMenu(p);
    }

    public void openBankDepositMenu(Player p){
        new BankDepositMenu(p);
    }

    public void openBankUpgradeMenu(Player p){
        new BankUpgradesMenu(p);
    }

    public void addTransactionLog(String playerUUID, String byWho, String symbol, double amount){
        RoyaleEconomy.dataManager.addTransactionLog(playerUUID, byWho, symbol, amount);
    }

    public double getMaximumBankCoins(String playerUUID){
        return bankUpgradesCfg.getDouble("bank-upgrades." + RoyaleEconomy.dataManager.getBankUpgrade(playerUUID) + ".maximum-balance");
    }

    public int getBankUpgrade(String playerUUID){
        return RoyaleEconomy.dataManager.getBankUpgrade(playerUUID);
    }

    public void setBankUpgrade(int upgrade, String playerUUID){
        if(playerMoneyCache instanceof PlayerMoneyCacheSQL)
            ((PlayerMoneyCacheSQL)playerMoneyCache).setBankUpgradeCache(playerUUID, upgrade);
        RoyaleEconomy.dataManager.setBankUpgrade(playerUUID, upgrade);
    }

    public ArrayList<String> getTransactionLog(Player p){
        return RoyaleEconomy.dataManager.getTransactionLogFromFile(p.getUniqueId().toString());
    }

}
