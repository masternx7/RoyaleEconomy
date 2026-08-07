package me.qKing12.RoyaleEconomy.DataManager;

import com.tcoded.folialib.wrapper.task.WrappedTask;
import me.qKing12.RoyaleEconomy.DataManager.SellLimitGlobal.ISellLimitGlobal;
import me.qKing12.RoyaleEconomy.DataManager.SharedBank.SharedBank;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;


public interface DataManager {

    ISellLimitGlobal getSellLimit();

    void createCurrency(String currencyId);

    void deleteCurrency(String currencyId);

    double getCurrencyMoney(String playerUUID, String currencyId);

    void setCurrencyMoney(String playerUUID, double money, String currencyId);

    void addCurrencyMoney(String playerUUID, double money, String currencyId);

    void removeCurrencyMoney(String playerUUID, double money, String currencyId);

    List<String[]> getCurrencyBalanceTop(String currencyId);

    void exportEconomy();

    Double getMoneyFromExternal(String id);

    void addMoneyToExternal(String id, double amount);

    ArrayList<String> getIdsFromExternal();

    boolean deleteAccount(String id);

    void importEssentials();

    void importCMI();

    void addMoneyToFile(String player, Double amount);

    boolean removeMoneyFromFile(String player, Double amount);

    void setMoney(String player, Double amount);

    Double getMoneyFromFile(String player);

    void updateUsername(Player player);

    String getUUIDfromName(String name);

    void addBankMoneyToFile(String name, double amount);

    void removeBankMoneyToFile(String name, double amount);

    Double getBankMoneyFromFile(String player);

    void setBankMoney(String name, double amount);

    Long getInterestDate();

    void updateInterestDate(Long date);

    boolean triggerInterest();

    Double interestCalculator(String player, boolean display, boolean shared);

    int getBankUpgrade(String player);

    void setBankUpgrade(String player, int bankUpgrade);

    WrappedTask checkInterest();

    ArrayList<String> getTransactionLogFromFile(String p);

    void addTransactionLog(String uuid, String byWho, String symbol, double amount);

    void loadTops();

    boolean createAccount(String name);

    void removeUserFromDatabase(String toRemove, boolean id);

    SharedBank getSharedBankManager();

    /*ArrayList<String> getSharedTransactionLogFromFile(String bankID);

    void addSharedTransactionLog(String bankID, String byWho, String symbol, double amount);

    void addSharedBankMoneyToFile(String bankID, Double amount);

    void removeSharedBankMoneyToFile(String bankID, Double amount);

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

    void setSharedBankUpgrade(String bankID, int bankUpgrade);*/

}
