package me.qKing12.RoyaleEconomy.API;

import me.qKing12.RoyaleEconomy.RoyaleEconomy;

public class Balance {

    public double getBalance(String player){
        return RoyaleEconomy.dataManager.getMoneyFromFile(player);
    }

    public void addBalance(String player, double toAdd){
        RoyaleEconomy.dataManager.addMoneyToFile(player, toAdd);
    }

    public void removeBalance(String player, double toRemove){
        RoyaleEconomy.dataManager.removeMoneyFromFile(player, toRemove);
    }

    public void setBalance(String player, double toSet){
        RoyaleEconomy.dataManager.setMoney(player, toSet);
    }

    public double getBankBalance(String playerUUID){
        return RoyaleEconomy.dataManager.getBankMoneyFromFile(playerUUID);
    }

    public void addBankBalance(String playerUUID, double toAdd){
        RoyaleEconomy.dataManager.addBankMoneyToFile(playerUUID, toAdd);
    }

    public void removeBankBalance(String playerUUID, double toRemove){
        RoyaleEconomy.dataManager.removeBankMoneyToFile(playerUUID, toRemove);
    }

    public void setBankBalance(String playerUUID, double toSet){
        RoyaleEconomy.dataManager.setBankMoney(playerUUID, toSet);
    }

    public double getSharedBankBalance(String bankID){
        return RoyaleEconomy.dataManager.getSharedBankManager().getSharedBankMoneyFromFile(bankID);
    }

    public void addSharedBankBalance(String player, double toAdd){
        RoyaleEconomy.dataManager.getSharedBankManager().addSharedBankMoneyPlayer(player, toAdd);
    }

    public void removeSharedBankBalance(String player, double toRemove){
        RoyaleEconomy.dataManager.getSharedBankManager().removeSharedBankMoneyPlayer(player, toRemove);
    }

    public void setSharedBankBalance(String player, double toSet){
        RoyaleEconomy.dataManager.getSharedBankManager().setSharedBankMoneyPlayer(player, toSet);
    }

}
