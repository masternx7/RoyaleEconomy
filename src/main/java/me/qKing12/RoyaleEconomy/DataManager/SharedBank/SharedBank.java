package me.qKing12.RoyaleEconomy.DataManager.SharedBank;

import org.bukkit.entity.Player;

import java.util.ArrayList;

public interface SharedBank {

    void loadTops();

    void setTable(String table);

    String getTable();

    ArrayList<String> getSharedTransactionLogFromFile(String bankID);

    void addSharedTransactionLog(String bankID, String byWho, String symbol, double amount);

    void addSharedBankMoneyToFile(String bankID, Double amount);

    boolean removeSharedBankMoneyToFile(String bankID, Double amount);

    Double getSharedBankMoneyFromFile(String bankID);

    void setSharedBankMoney(String bankID, Double amount);

    boolean createSharedBank(Player p);

    boolean deleteSharedBank(Player p);

    void deleteSharedBank(String bankID);

    boolean addPlayerToSharedBank(Player p, String bankID);

    boolean addPlayerToSharedBank(String pUUID, String bankID);

    boolean removePlayerFromSharedBank(String player, String bankID);

    boolean transferOwnershipSharedBank(String oldOwner, String newOwner);

    boolean transferOwnershipSharedBank(String newOwner);

    String getSharedBankId(String player);

    boolean isOwnerSharedBank(Player p);

    ArrayList<String> getMembersSharedBank(String bankID, boolean owner);

    int getSharedBankUpgrade(String bankID);

    void setSharedBankUpgrade(String bankID, int bankUpgrade);

    ArrayList<String> getSharedTransactionLogPlayer(String playerUUID);

    void addSharedTransactionLogPlayer(String playerUUID, String byWho, String symbol, double amount);

    void setSharedBankMoneyPlayer(String playerUUID, Double amount);

    void addSharedBankMoneyPlayer(String playerUUID, Double amount);

    void removeSharedBankMoneyPlayer(String playerUUID, Double amount);

    Double getSharedBankMoneyPlayer(String playerUUID);

    ArrayList<String> getMembersSharedBankPlayer(String playerUUID, boolean owner);

    String getOwnerByPlayerId(String playerUUID);

    int getSharedBankUpgradePlayer(String playerUUID);

    void setSharedBankUpgradePlayer(String playerUUID, int bankUpgrade);

    boolean createSharedBank(String bankID);


}
