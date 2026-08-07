package me.qKing12.RoyaleEconomy.API;

import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import me.qKing12.RoyaleEconomy.Menus.SharedBankDepositMenu;
import me.qKing12.RoyaleEconomy.Menus.SharedBankUpgradesMenu;
import me.qKing12.RoyaleEconomy.Menus.SharedBankWithdrawMenu;
import me.qKing12.RoyaleEconomy.Menus.SharedMainBankMenu;
import org.bukkit.entity.Player;

import java.util.ArrayList;

import static me.qKing12.RoyaleEconomy.RoyaleEconomy.bankUpgradesCfg;

public class SharedBank {

    public void openBankMainMenu(Player p){
        new SharedMainBankMenu(p);
    }

    public void openBankWithdrawMenu(Player p){
        new SharedBankWithdrawMenu(p);
    }

    public void openBankDepositMenu(Player p){
        new SharedBankDepositMenu(p);
    }

    public void openBankUpgradeMenu(Player p){
        new SharedBankUpgradesMenu(p);
    }

    public boolean isSharedBankOwner(Player p){
        return RoyaleEconomy.dataManager.getSharedBankManager().isOwnerSharedBank(p);
    }

    public boolean createSharedBank(Player p){
        return RoyaleEconomy.dataManager.getSharedBankManager().createSharedBank(p);
    }

    public boolean deleteSharedBank(Player p){
        return RoyaleEconomy.dataManager.getSharedBankManager().deleteSharedBank(p);
    }

    public void deleteSharedBank(String bankID){
        RoyaleEconomy.dataManager.getSharedBankManager().deleteSharedBank(bankID);
    }

    public boolean addMemberToBank(Player toAdd, String bankID){
        return RoyaleEconomy.dataManager.getSharedBankManager().addPlayerToSharedBank(toAdd, bankID);
    }

    public boolean removeMemberFromSharedBank(String toRemoveUUID, String bankID){
        return RoyaleEconomy.dataManager.getSharedBankManager().removePlayerFromSharedBank(toRemoveUUID, bankID);
    }

    public String getSharedBankID(String playerUUID){
        return RoyaleEconomy.dataManager.getSharedBankManager().getSharedBankId(playerUUID);
    }

    public int getSharedBankUpgrade(String bankID){
        return RoyaleEconomy.dataManager.getSharedBankManager().getSharedBankUpgrade(bankID);
    }

    public void setSharedBankUpgrade(int upgrade, String bankID){
        RoyaleEconomy.dataManager.getSharedBankManager().setSharedBankUpgrade(bankID, upgrade);
    }

    public double getMaximumSharedBankCoins(String bankID){
        return bankUpgradesCfg.getDouble("shared-bank-upgrades." + RoyaleEconomy.dataManager.getBankUpgrade(bankID) + ".maximum-balance");
    }

    public void addTransactionLog(String bankID, String byWho, String symbol, double amount){
        RoyaleEconomy.dataManager.getSharedBankManager().addSharedTransactionLog(bankID, byWho, symbol, amount);
    }

    public ArrayList<String> getSharedBankMembers(String bankID, boolean owner){
        return RoyaleEconomy.dataManager.getSharedBankManager().getMembersSharedBank(bankID, owner);
    }

    public boolean transferOwnerShip(String oldOwnerUUID, String newOwnerUUID){
        return RoyaleEconomy.dataManager.getSharedBankManager().transferOwnershipSharedBank(oldOwnerUUID, newOwnerUUID);
    }

    public ArrayList<String> getTransactionLog(String bankID){
        return RoyaleEconomy.dataManager.getSharedBankManager().getSharedTransactionLogFromFile(bankID);
    }

}
