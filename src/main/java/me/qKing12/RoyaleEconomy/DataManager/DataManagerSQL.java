package me.qKing12.RoyaleEconomy.DataManager;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import com.tcoded.folialib.wrapper.task.WrappedTask;
import me.qKing12.RoyaleEconomy.API.Events.CoinsAddToPurseEvent;
import me.qKing12.RoyaleEconomy.API.Events.CoinsRemoveFromPurseEvent;
import me.qKing12.RoyaleEconomy.Commands.InterestCommand;
import me.qKing12.RoyaleEconomy.DataManager.Cache.PlayerMoneyCacheSQL;
import me.qKing12.RoyaleEconomy.DataManager.SellLimitGlobal.ISellLimitGlobal;
import me.qKing12.RoyaleEconomy.DataManager.SellLimitGlobal.SellLimitGlobalSQL;
import me.qKing12.RoyaleEconomy.DataManager.SharedBank.SharedBank;
import me.qKing12.RoyaleEconomy.DataManager.SharedBank.SharedBankSQL;
import me.qKing12.RoyaleEconomy.Economy.VaultHook;
import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import me.qKing12.RoyaleEconomy.utils.GenerateFiles;
import me.qKing12.RoyaleEconomy.utils.PlayerMessageHandler;
import me.qKing12.RoyaleEconomy.utils.Utils;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static me.qKing12.RoyaleEconomy.RoyaleEconomy.*;

public class DataManagerSQL implements DataManager {
    static String url;
    private EconomyFunctions economyFunctions;
    private SharedBank sharedBank;

    @Override
    public SharedBank getSharedBankManager() {
        return sharedBank;
    }

    private ISellLimitGlobal sellLimitGlobal = new SellLimitGlobalSQL();

    @Override
    public ISellLimitGlobal getSellLimit() {
        return sellLimitGlobal;
    }

    public DataManagerSQL(boolean noEconomy) {
        url = "jdbc:sqlite:" + RoyaleEconomy.plugin.getDataFolder() + "/database/royaleEconomyData.db";
        HikariCPDataSource.loadSettings(url, null, null, false);
        removeOldUsers();
        if (noEconomy) {
            economyFunctions = new noEconomyFunctions();
        } else {
            economyFunctions = new yesEconomyFunctions();
            VaultHook.banks = getIdsFromExternal();
        }
        sharedBank = new SharedBankSQL("SharedBank");
    }

    public void createCurrency(String name) {
        try (Connection PlayerPurse = HikariCPDataSource.getConnection();
             Statement stmt = PlayerPurse.createStatement();
        ) {

            String sql = "CREATE TABLE IF NOT EXISTS RoyaleEconomy" + name +
                    "(id VARCHAR(36) not NULL, " +
                    " coins DOUBLE(25, 2), " +
                    " PRIMARY KEY ( id ))";

            stmt.execute(sql);
            RoyaleEconomy.plugin.getLogger().info("Succesfully connected to the Currency " + name + " SQL.");

        } catch (Exception e) {
            RoyaleEconomy.plugin.getLogger().info("An error has occured while connecting to the Currency " + name + " SQL.");
            RoyaleEconomy.plugin.getLogger().info(e.getMessage());
        }
    }

    public void deleteCurrency(String name) {
        try (Connection PlayerPurse = HikariCPDataSource.getConnection();
             Statement stmt = PlayerPurse.createStatement();
        ) {

            String sql = "DROP TABLE IF EXISTS RoyaleEconomy" + name;

            stmt.execute(sql);
            RoyaleEconomy.plugin.getLogger().info("Succesfully deleted the Currency " + name + " SQL.");

        } catch (Exception e) {
            RoyaleEconomy.plugin.getLogger().info("An error has occured while deleting the Currency " + name + " SQL.");
            RoyaleEconomy.plugin.getLogger().info(e.getMessage());
        }
    }

    public double getCurrencyMoney(String playerUUID, String currency) {
        //double onlineAmount= playerMoneyCache.getBalance(player);
        //if(onlineAmount!=-1){
        //    return onlineAmount;
        //}
        double toReturn = 0;

        try (
                Connection Database = HikariCPDataSource.getConnection();
                ResultSet r = Database.prepareStatement("SELECT coins FROM RoyaleEconomy" + currency + " WHERE id = '" + playerUUID + "'").executeQuery();

        ) {
            if (r.next())
                toReturn = r.getDouble("coins");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return toReturn;
    }

    public void setCurrencyMoney(String playerUUID, double amount, String currency) {
        try (
                Connection Database = HikariCPDataSource.getConnection();
                PreparedStatement stmt = Database.prepareStatement("UPDATE RoyaleEconomy" + currency + " SET coins = ? WHERE id = ?");
                PreparedStatement stmt2 = Database.prepareStatement("INSERT INTO RoyaleEconomy" + currency + " VALUES(?, ?)");
        ) {
            stmt.setDouble(1, amount);
            stmt.setString(2, playerUUID);
            int c = stmt.executeUpdate();
            if (c == 0) {
                stmt2.setString(1, playerUUID);
                stmt2.setDouble(2, amount);
                stmt2.executeUpdate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addCurrencyMoney(String playerUUID, double amount, String currency) {
        try (
                Connection Database = HikariCPDataSource.getConnection();
                PreparedStatement stmt = Database.prepareStatement("UPDATE RoyaleEconomy" + currency + " SET coins = coins+? WHERE id = ?");
                PreparedStatement stmt2 = Database.prepareStatement("INSERT INTO RoyaleEconomy" + currency + " VALUES(?, ?)");
        ) {
            stmt.setDouble(1, amount);
            stmt.setString(2, playerUUID);
            int c = stmt.executeUpdate();
            if (c == 0) {
                stmt2.setString(1, playerUUID);
                stmt2.setDouble(2, amount);
                stmt2.executeUpdate();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void removeCurrencyMoney(String playerUUID, double amount, String currency) {
        try (
                Connection Database = HikariCPDataSource.getConnection();
                PreparedStatement stmt = Database.prepareStatement("UPDATE RoyaleEconomy" + currency + " SET coins = coins - ? WHERE id = ?");
        ) {
            stmt.setDouble(1, amount);
            stmt.setString(2, playerUUID);
            stmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<String[]> getCurrencyBalanceTop(String currencyId) {
        try (
                Connection Database = HikariCPDataSource.getConnection();
                ResultSet r = Database.prepareStatement("SELECT * FROM RoyaleEconomy" + currencyId + " ORDER BY coins DESC LIMIT 10").executeQuery();
        ) {
            List<String[]> toReturn = new ArrayList<>();
            while (r.next()) {
                String[] toAdd = new String[2];
                toAdd[0] = Bukkit.getOfflinePlayer(UUID.fromString(r.getString("id"))).getName();
                toAdd[1] = r.getString("coins");
                toReturn.add(toAdd);
            }
            return toReturn;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    private interface EconomyFunctions {

        void setMoney(String player, Double amount);

        void addMoneyToFile(String player, Double amount);

        boolean removeMoneyFromFile(String player, Double amount);

        Double getMoneyFromFile(String player);
    }

    private class noEconomyFunctions implements EconomyFunctions {
        public void setMoney(String player, Double amount) {
            double toExport = amount - economy.getBalance(player);
            if (player.length() < 17)
                player = getUUIDfromName(player);
            if (player == null)
                return;
            if (toExport < 0)
                economy.withdrawPlayer(Bukkit.getOfflinePlayer(UUID.fromString(player)), toExport * -1);
            else
                economy.depositPlayer(Bukkit.getOfflinePlayer(UUID.fromString(player)), toExport);
        }

        public void addMoneyToFile(String player, Double amount) {
            if (player.length() < 17)
                player = getUUIDfromName(player);
            economy.depositPlayer(Bukkit.getOfflinePlayer(UUID.fromString(player)), amount);
            String playerFinal = player;
            RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runNextTick((task) -> Bukkit.getPluginManager().callEvent(new CoinsAddToPurseEvent(playerFinal, amount)));
        }

        public boolean removeMoneyFromFile(String player, Double amount) {
            if (player.length() < 17)
                player = getUUIDfromName(player);
            final Double amountFloored = Math.floor(amount * 100) / 100;
            if (getMoneyFromFile(player) >= amountFloored) {
                EconomyResponse response = economy.withdrawPlayer(Bukkit.getOfflinePlayer(UUID.fromString(player)), amountFloored);
                if (!response.transactionSuccess()) {
                    plugin.getLogger().warning("[DEBUG EXTERNAL ECONOMY] Player " + player + ", amount = " + amountFloored + ", balance = " + getMoneyFromFile(player) + ": " + response.errorMessage);
                }
                return response.transactionSuccess();
            }
            return false;
        }

        public Double getMoneyFromFile(String player) {
            if (player.length() < 17)
                player = getUUIDfromName(player);
            try {
                return economy.getBalance(Bukkit.getOfflinePlayer(UUID.fromString(player)));
            } catch (Exception x) {
                if (RoyaleEconomy.economy == null) {
                    Plugin vault = Bukkit.getPluginManager().getPlugin("Vault");
                    if (vault != null) {
                        RoyaleEconomy.economy = Bukkit.getServer().getServicesManager().getRegistration(Economy.class).getProvider();
                    } else {
                        RoyaleEconomy.plugin.getLogger().warning("No economy found, the plugin will shut down.");
                        Bukkit.getPluginManager().disablePlugin(RoyaleEconomy.plugin);
                    }
                    return economy.getBalance(Bukkit.getOfflinePlayer(UUID.fromString(player)));
                }
                return 0d;
            }
        }
    }

    private class yesEconomyFunctions implements EconomyFunctions {
        public void setMoney(String player, Double amount) {
            if (playerMoneyCache.setBalance(player, amount)) {
                return;
            }
            //RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runAsync((task) -> {
            try (
                    Connection Database = HikariCPDataSource.getConnection();
                    PreparedStatement stmt = Database.prepareStatement(player.length() > 16 ? "UPDATE PlayerPurse SET coins = ? WHERE id = ?" : "UPDATE PlayerPurse SET coins = ? WHERE upper(username) = ?");
            ) {
                stmt.setDouble(1, amount);
                stmt.setString(2, player.length() > 16 ? player : player.toUpperCase());
                stmt.executeUpdate();

            } catch (Exception e) {
                if (e.getMessage() != null && e.getMessage().startsWith("[SQLITE_BUSY]")) {
                    RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runLaterAsync(() -> setMoney(player, amount), 7);
                } else
                    e.printStackTrace();
            }
            //});
        }

        public void addMoneyToFile(String player, Double amount) {
            if (playerMoneyCache.addBalance(player, amount)) {
                RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runNextTick((task) -> Bukkit.getPluginManager().callEvent(new CoinsAddToPurseEvent(player, amount)));
                return;
            }
            //RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runAsync((task) -> {
            try (Connection Database = HikariCPDataSource.getConnection();
                 PreparedStatement stmt1 = Database.prepareStatement(player.length() > 16 ? "UPDATE PlayerPurse SET coins = coins+? WHERE id = ?" : "UPDATE PlayerPurse SET coins = coins+? WHERE upper(username) = ?");
                 PreparedStatement stmt2 = Database.prepareStatement("UPDATE ExternalGeneratedData SET coins = coins+? WHERE id = ?");
            ) {
                RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runNextTick((task) -> Bukkit.getPluginManager().callEvent(new CoinsAddToPurseEvent(player, amount)));
                stmt1.setDouble(1, amount);
                stmt1.setString(2, player.length() > 16 ? player : player.toUpperCase());
                int updated = stmt1.executeUpdate();
                if (updated == 0) {
                    stmt2.setDouble(1, amount);
                    stmt2.setString(2, player);
                    stmt2.executeUpdate();
                }

            } catch (Exception e) {
                if (e.getMessage() != null && e.getMessage().startsWith("[SQLITE_BUSY]")) {
                    RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runLaterAsync(() -> addMoneyToFile(player, amount), 7);
                } else
                    e.printStackTrace();
            }
            //});
        }

        public boolean removeMoneyFromFile(String player, Double amount) {
            final double amountFloored = Math.floor(amount * 100) / 100;
            int onlineRemove = playerMoneyCache.removeBalance(player, amount);
            if (onlineRemove == 0)
                return false;
            else if (onlineRemove == 1) {
                RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runNextTick((task) -> Bukkit.getPluginManager().callEvent(new CoinsRemoveFromPurseEvent(player, amountFloored)));
                return true;
            }
            try {
                if (getMoneyFromFile(player) >= amountFloored) {
                    try (
                            Connection Database = HikariCPDataSource.getConnection();
                            PreparedStatement stmt1 = Database.prepareStatement(player.length() > 16 ? "UPDATE PlayerPurse SET coins = coins-? WHERE id = ?" : "UPDATE PlayerPurse SET coins = coins-? WHERE upper(username) = ?");
                            PreparedStatement stmt2 = Database.prepareStatement("UPDATE ExternalGeneratedData SET coins = coins-? WHERE id = ?");
                    ) {
                        RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runNextTick((task) -> Bukkit.getPluginManager().callEvent(new CoinsRemoveFromPurseEvent(player, amountFloored)));
                        stmt1.setDouble(1, amountFloored);
                        stmt1.setString(2, player.length() > 16 ? player : player.toUpperCase());
                        //stmt1.setDouble(3, amountFloored);
                        int updated = stmt1.executeUpdate();
                        if (updated == 0) {
                            stmt2.setDouble(1, amountFloored);
                            stmt2.setString(2, player);
                            //stmt2.setDouble(3, amountFloored);
                            updated = stmt2.executeUpdate();
                            if (updated != 0) {
                                RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runNextTick((task) -> Bukkit.getPluginManager().callEvent(new CoinsRemoveFromPurseEvent(player, amountFloored)));
                                return true;
                            }
                        } else {
                            RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runNextTick((task) -> Bukkit.getPluginManager().callEvent(new CoinsRemoveFromPurseEvent(player, amountFloored)));
                            return true;
                        }
                    } catch (SQLException x) {
                        x.printStackTrace();
                        RoyaleEconomy.plugin.getLogger().warning("[DEBUG MESSAGE] The database was closed, reestablishing connection.");
                        return false;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception x) {
                x.printStackTrace();
            }
            return false;
        }

        public Double getMoneyFromFile(String player) {
            double onlineAmount = playerMoneyCache.getBalance(player);
            if (onlineAmount != -1) {
                return onlineAmount;
            }
            Double toReturn = null;

            try (
                    Connection Database = HikariCPDataSource.getConnection();
                    ResultSet r = Database.prepareStatement(player.length() > 16 ? "SELECT coins FROM PlayerPurse WHERE id = '" + player + "'" : "SELECT coins FROM PlayerPurse WHERE upper(username) = '" + player.toUpperCase() + "'").executeQuery();
                    ResultSet r2 = Database.prepareStatement("SELECT coins FROM ExternalGeneratedData WHERE id = '" + player + "'").executeQuery();

            ) {
                if (r.next())
                    toReturn = r.getDouble("coins");
                else {
                    if (r2.next())
                        toReturn = r2.getDouble("coins");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (toReturn == null)
                return null;
            return BigDecimal.valueOf(toReturn).setScale(2, RoundingMode.HALF_UP).doubleValue();
        }
    }

    public void exportEconomy() {
        try (
                Connection Database = HikariCPDataSource.getConnection();
                ResultSet r = Database.prepareStatement("SELECT id,coins FROM PlayerPurse").executeQuery();
        ) {
            while (r.next()) {
                try {
                    OfflinePlayer p = Bukkit.getOfflinePlayer(UUID.fromString(r.getString(1)));
                    double toExport = r.getDouble(2) - economy.getBalance(p);
                    if (toExport < 0)
                        economy.withdrawPlayer(p, toExport * -1);
                    else
                        economy.depositPlayer(p, toExport);
                } catch (Exception x) {
                    plugin.getLogger().warning("An error occured while loading the player " + r.getString(1));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setMoney(String player, Double amount) {
        economyFunctions.setMoney(player, amount);
    }

    @Override
    public void addMoneyToFile(String player, Double amount) {
        economyFunctions.addMoneyToFile(player, amount);
    }

    @Override
    public boolean removeMoneyFromFile(String player, Double amount) {
        return economyFunctions.removeMoneyFromFile(player, amount);
    }

    @Override
    public Double getMoneyFromFile(String player) {
        return economyFunctions.getMoneyFromFile(player);
    }

    public void importEssentials() {
        RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runAsync((task) -> {

            File essentialsFolder = new File(RoyaleEconomy.plugin.getDataFolder().toString().replace("RoyaleEconomy", "Essentials") + "/userdata");
            if (!essentialsFolder.exists() || essentialsFolder.listFiles() == null || essentialsFolder.listFiles().length == 0) {
                return;
            }

            String nameCheck;
            FileConfiguration testCfg = YamlConfiguration.loadConfiguration(essentialsFolder.listFiles()[0]);
            if (testCfg.contains("last-account-name"))
                nameCheck = "last-account-name";
            else
                nameCheck = "lastAccountName";

            try (Connection Database = HikariCPDataSource.getConnection();
                 PreparedStatement u = Database.prepareStatement(
                         "INSERT OR IGNORE INTO PlayerPurse VALUES (?, ? ,0, '')"
                 );

                 PreparedStatement u1 = Database.prepareStatement(
                         "UPDATE PlayerPurse SET coins = ? WHERE id = ?"
                 );

                 PreparedStatement u2 = Database.prepareStatement(
                         "INSERT OR IGNORE INTO PersonalBank VALUES (?, " + RoyaleEconomy.staticValues.defaultBankCoins + " ," + 0 + ", '')"
                 );
            ) {
                Database.setAutoCommit(false);

                for (File player : essentialsFolder.listFiles()) {
                    FileConfiguration playerCfg = YamlConfiguration.loadConfiguration(player);
                    String uuid = player.getName().replace(".yml", "");
                    String name = playerCfg.getString(nameCheck);
                    String coinsString = playerCfg.getString("money");
                    double coins;
                    if (coinsString == null)
                        coins = 0;
                    else
                        coins = Double.parseDouble(coinsString);

                    u.setString(1, uuid);
                    u.setString(2, name);
                    u1.setDouble(1, coins);
                    u1.setString(2, uuid);
                    u2.setString(1, uuid);
                    u.addBatch();
                    u1.addBatch();
                    u2.addBatch();
                }

                u.executeBatch();
                u1.executeBatch();
                u2.executeBatch();
                Database.commit();
                Database.setAutoCommit(true);
                playerMoneyCache.reloadBalances();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void addMoneyToExternal(String id, double amount) {
        try (Connection Database = HikariCPDataSource.getConnection();
             PreparedStatement stmt = Database.prepareStatement("UPDATE ExternalGeneratedData SET coins = coins+? WHERE id = ?");
        ) {
            stmt.setDouble(1, amount);
            stmt.setString(2, id);
            stmt.executeUpdate();
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().startsWith("[SQLITE_BUSY]")) {
                RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runLaterAsync(() -> addMoneyToExternal(id, amount), 7);
            } else
                e.printStackTrace();
        }
    }

    public Double getMoneyFromExternal(String id) {
        Double toReturn = null;
        try (
                Connection Database = HikariCPDataSource.getConnection();
                ResultSet r = Database.prepareStatement("SELECT coins FROM ExternalGeneratedData WHERE id = '" + id + "'").executeQuery();

        ) {
            if (r.next())
                toReturn = r.getDouble("coins");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return toReturn;
    }

    public ArrayList<String> getIdsFromExternal() {
        ArrayList<String> ids = new ArrayList<>();
        try (
                Connection Database = HikariCPDataSource.getConnection();
                ResultSet r = Database.prepareStatement("SELECT id FROM ExternalGeneratedData").executeQuery();

        ) {
            while (r.next())
                ids.add(r.getString(1));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ids;
    }

    public void importCMI() {
        RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runAsync((task) -> {
            try (
                    Connection Database = HikariCPDataSource.getConnection();
                    PreparedStatement u = Database.prepareStatement(
                            "INSERT OR IGNORE INTO PlayerPurse VALUES (?, ? ,0, '')"
                    );

                    PreparedStatement u1 = Database.prepareStatement(
                            "UPDATE PlayerPurse SET coins = ? WHERE id = ?"
                    );

                    PreparedStatement u2 = Database.prepareStatement(
                            "INSERT OR IGNORE INTO PersonalBank VALUES (?, " + RoyaleEconomy.staticValues.defaultBankCoins + " ," + 0 + ", '')"
                    );
            ) {
                Database.setAutoCommit(false);
                for (CMIUser player : CMI.getInstance().getPlayerManager().getAllUsers().values()) {
                    String uuid = player.getUniqueId().toString();
                    String name = player.getName();
                    double coins = player.getBalance();

                    u.setString(1, uuid);
                    u.setString(2, name);
                    u1.setDouble(1, coins);
                    u1.setString(2, uuid);
                    u2.setString(1, uuid);
                    u.addBatch();
                    u1.addBatch();
                    u2.addBatch();
                }

                u.executeBatch();
                u1.executeBatch();
                u2.executeBatch();
                Database.commit();
                Database.setAutoCommit(true);
                playerMoneyCache.reloadBalances();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void setBankMoney(String player, double amount) {
        if (((PlayerMoneyCacheSQL) playerMoneyCache).setBalanceBank(player, amount)) {
            return;
        }
        //RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runAsync((task) -> {
        try (
                Connection Database = HikariCPDataSource.getConnection();
                PreparedStatement stmt = Database.prepareStatement("UPDATE PersonalBank SET coins = ? WHERE id = ?");
        ) {
            stmt.setDouble(1, amount);
            stmt.setString(2, player);
            stmt.executeUpdate();
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().startsWith("[SQLITE_BUSY]")) {
                RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runLaterAsync(() -> setBankMoney(player, amount), 7);
            } else
                e.printStackTrace();
        }
        //});
    }

    public void addBankMoneyToFile(String player, double amount2) {
        double amount = Math.floor(amount2 * 100) / 100;
        if (((PlayerMoneyCacheSQL) playerMoneyCache).addBalanceBank(player, amount)) {
            //RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runNextTick((task) -> Bukkit.getPluginManager().callEvent(new CoinsAddToPurseEvent(player, amount)));
            return;
        }
        //RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runAsync((task) -> {
        try (
                Connection Database = HikariCPDataSource.getConnection();
                PreparedStatement stmt = Database.prepareStatement("UPDATE PersonalBank SET coins = coins +? WHERE id = ?");
        ) {
            stmt.setDouble(1, amount);
            stmt.setString(2, player);
            stmt.executeUpdate();

        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().startsWith("[SQLITE_BUSY]")) {
                RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runLaterAsync(() -> addBankMoneyToFile(player, amount), 7);
            } else
                e.printStackTrace();
        }
        //});
    }

    public void removeBankMoneyToFile(String player, double amount) {
        amount = Math.floor(amount * 100) / 100;
        int onlineRemove = ((PlayerMoneyCacheSQL) playerMoneyCache).removeBalanceBank(player, amount);
        if (onlineRemove == 0)
            return;
        else if (onlineRemove == 1) {
            return;
        }
        try (
                Connection Database = HikariCPDataSource.getConnection();
                PreparedStatement stmt = Database.prepareStatement("UPDATE PersonalBank SET coins = coins -? WHERE id = ?");
        ) {
            stmt.setDouble(1, amount);
            stmt.setString(2, player);
            stmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Double getBankMoneyFromFile(String player) {
        double onlineAmount = ((PlayerMoneyCacheSQL) playerMoneyCache).getBalanceBank(player);
        if (onlineAmount != -1) {
            return onlineAmount;
        }
        Double toReturn = null;

        try (
                Connection Database = HikariCPDataSource.getConnection();
                ResultSet r = Database.prepareStatement("SELECT coins FROM PersonalBank WHERE id = '" + player + "'").executeQuery();
        ) {
            if (r.next())
                toReturn = r.getDouble("coins");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return toReturn;
    }

    public Long getInterestDate() {
        Long toReturn = null;

        try (
                Connection Database = HikariCPDataSource.getConnection();
                ResultSet r = Database.prepareStatement("SELECT date FROM Interest WHERE id = 'personalInterest'").executeQuery()
        ) {
            if (r.next())
                toReturn = r.getLong("date");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return toReturn;
    }

    public void updateInterestDate(Long date) {
        RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runAsync((task) -> {
            try (Connection Database = HikariCPDataSource.getConnection();
                 PreparedStatement stmt = Database.prepareStatement("UPDATE Interest SET date = ? WHERE id = 'personalInterest'");) {

                stmt.setLong(1, date);
                stmt.executeUpdate();

            } catch (Exception e) {
                if (e.getMessage() != null && e.getMessage().startsWith("[SQLITE_BUSY]")) {
                    RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runLaterAsync(() -> updateInterestDate(date), 7);
                } else
                    e.printStackTrace();
            }
        });
    }

    public String getUUIDfromName(String name) {
        String toReturn = null;

        try (Connection Database = HikariCPDataSource.getConnection();
             ResultSet r = Database.prepareStatement("SELECT id FROM PlayerPurse WHERE upper(username) = '" + name.toUpperCase() + "'").executeQuery();
        ) {
            if (r.next()) {
                toReturn = r.getString("id");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return toReturn;
    }

    private boolean usernameExists(Player player) {
        try (Connection Database = HikariCPDataSource.getConnection();
             PreparedStatement stmt1 = Database.prepareStatement("UPDATE SharedBank SET owner = ? WHERE id = ?");
             PreparedStatement stmt2 = Database.prepareStatement("UPDATE SharedBank SET members = ? WHERE id = ?");
             PreparedStatement stmt3 = Database.prepareStatement("UPDATE PlayerPurse SET id = ? WHERE upper(username) = ?");
             PreparedStatement stmt4 = Database.prepareStatement("UPDATE PersonalBank SET id = ? WHERE id = ?");
             ResultSet r = Database.prepareStatement("SELECT id FROM PlayerPurse WHERE upper(username) = '" + player.getName().toUpperCase() + "'").executeQuery()) {
            if (r.next()) {
                String oldID = r.getString("id");
                if (!oldID.equals(player.getUniqueId().toString())) {
                    RoyaleEconomy.plugin.getLogger().info("Found a new UUID for player " + player.getName() + ". Trying to migrate from " + oldID + " to " + player.getUniqueId());
                    String sharedBankID = sharedBank.getSharedBankId(oldID);
                    if (sharedBankID != null && !sharedBankID.equals("")) {
                        ArrayList<String> members = sharedBank.getMembersSharedBank(sharedBankID, true);
                        if (members.get(0).equals(oldID)) {
                            stmt1.setString(1, player.getUniqueId().toString());
                            stmt1.setString(2, oldID);
                            stmt1.executeUpdate();
                        } else {
                            members.remove(0);

                            String finalMembers = "";
                            for (String member : members)
                                if (member.equals(oldID))
                                    finalMembers = finalMembers.concat("." + player.getUniqueId());
                                else
                                    finalMembers = finalMembers.concat("." + member);
                            if (!finalMembers.equals(""))
                                finalMembers = finalMembers.substring(1);
                            stmt2.setString(1, finalMembers);
                            stmt2.setString(2, sharedBankID);
                            stmt2.executeUpdate();
                        }
                    }

                    stmt3.setString(1, player.getUniqueId().toString());
                    stmt3.setString(2, player.getName().toUpperCase());
                    stmt3.executeUpdate();
                    stmt4.setString(1, player.getUniqueId().toString());
                    stmt4.setString(2, oldID);
                    stmt4.executeUpdate();
                    PlayerMoneyCacheSQL.onlinePlayers.remove(player);
                }
                return true;
            }
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().startsWith("[SQLITE_BUSY]")) {
                RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runLaterAsync(() -> usernameExists(player), 7);
            } else
                e.printStackTrace();
        }

        return false;
    }

    public boolean createAccount(String name) {
        try (Connection Database = HikariCPDataSource.getConnection();
             PreparedStatement stmt = Database.prepareStatement("INSERT INTO ExternalGeneratedData VALUES (?, 0)");) {
            stmt.setString(1, name);
            stmt.executeUpdate();
            VaultHook.banks.add(name);
            return true;
        } catch (Exception x) {
            return false;
        }
    }

    public boolean deleteAccount(String id) {
        try (Connection Database = HikariCPDataSource.getConnection();
             PreparedStatement stmt = Database.prepareStatement("DELETE FROM ExternalGeneratedData WHERE id = ?");) {
            stmt.setString(1, id);
            int deleted = stmt.executeUpdate();
            if (deleted != 0) {
                VaultHook.banks.remove(id);
                return true;
            }
            return false;
        } catch (Exception x) {
            return false;
        }
    }

    public void removeUserFromDatabase(String toRemove, boolean id) {
        RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runAsync((task) -> {
            try (Connection Database = HikariCPDataSource.getConnection();
                 PreparedStatement stmt1 = Database.prepareStatement(id ? "DELETE FROM PlayerPurse WHERE id = ?" : "DELETE FROM PlayerPurse WHERE username = ?");
                 PreparedStatement stmt2 = Database.prepareStatement("DELETE FROM PersonalBank WHERE id = ?");
                 PreparedStatement stmt3 = Database.prepareStatement("DELETE FROM SharedBank WHERE id = ?");
            ) {
                String uuid;
                if (id) {
                    uuid = toRemove;
                } else {
                    uuid = getUUIDfromName(toRemove);
                }
                if (uuid != null) {
                    stmt2.setString(1, uuid);
                    stmt2.executeUpdate();
                    String sharedBankId = sharedBank.getSharedBankId(uuid);
                    if (sharedBankId != null && !sharedBankId.equals("")) {
                        ArrayList<String> sharedBankMembers = sharedBank.getMembersSharedBank(sharedBankId, true);
                        if (sharedBankMembers.get(0).equals(uuid)) {
                            if (sharedBankMembers.size() == 1) {
                                stmt3.setString(1, sharedBankId);
                                stmt3.executeUpdate();
                            } else {
                                sharedBank.transferOwnershipSharedBank(uuid, sharedBankMembers.get(1));
                                sharedBank.removePlayerFromSharedBank(uuid, sharedBankId);
                            }
                        } else
                            sharedBank.removePlayerFromSharedBank(uuid, sharedBankId);
                    }
                }
                stmt1.setString(1, toRemove);
                stmt1.executeUpdate();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void removeOldUsers() {
//        if(!RoyaleEconomy.plugin.getConfig().getBoolean("use-old-players-delete"))
//            return;
//        long currentDate = ZonedDateTime.now().toInstant().toEpochMilli();
//        long limit = (long)RoyaleEconomy.plugin.getConfig().getInt("inactivity-days")*86400000L;
//        if(limit==0)
//            return;
//        RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runAsync((task) -> {
//            try (Connection Database = HikariCPDataSource.getConnection();
//                 ResultSet r = Database.prepareStatement("SELECT id,sharedBank FROM PlayerPurse").executeQuery();
//                 PreparedStatement stmt1 = Database.prepareStatement("DELETE FROM PlayerPurse WHERE id = ?");
//                 PreparedStatement stmt2 = Database.prepareStatement("DELETE FROM PersonalBank WHERE id = ?");
//                 PreparedStatement stmt3 = Database.prepareStatement("DELETE FROM SharedBank WHERE id = ?");
//                 PreparedStatement stmt = Database.prepareStatement("UPDATE SharedBank SET owner=?, members = ? WHERE id = ?");
//                 PreparedStatement stmtE = Database.prepareStatement("UPDATE SharedBank SET members = ? WHERE id = ?");
//            ) {
//                boolean found=false;
//                while(r.next()) {
//                    String uuid=r.getString(1);
//                    String bankID=r.getString(2);
//                    OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(uuid));
//                    if (player != null && player.hasPlayedBefore()) {
//                        if(currentDate-player.getLastPlayed()>limit) {
//                            found=true;
//                            //Bukkit.broadcastMessage(currentDate+" "+player.getLastPlayed()+" "+limit);
//                            plugin.getLogger().info("[USER REMOVAL] User "+uuid+" was removed from database | Data for Developer Debug: "+player.getLastPlayed()+" "+currentDate+" "+limit);
//                            stmt1.setString(1, uuid);
//                            stmt1.addBatch();
//                            stmt2.setString(1, uuid);
//                            stmt2.addBatch();
//                            if (!bankID.equals("")) {
//                                ArrayList<String> sharedBankMembers = sharedBank.getMembersSharedBank(bankID, true);
//                                if (sharedBankMembers.get(0).equals(uuid)) {
//                                    if (sharedBankMembers.size() == 1) {
//                                        stmt3.setString(1, bankID);
//                                        stmt3.addBatch();
//                                    } else {
//                                        sharedBankMembers.remove(0);
//                                        String newOwner = sharedBankMembers.get(0);
//                                        sharedBankMembers.remove(newOwner);
//                                        String finalMembers = "";
//                                        for (String member : sharedBankMembers)
//                                            finalMembers = finalMembers.concat("." + member);
//                                        if (!finalMembers.isEmpty())
//                                            finalMembers = finalMembers.substring(1);
//                                        stmt.setString(1, newOwner);
//                                        stmt.setString(2, finalMembers);
//                                        stmt.setString(3, bankID);
//                                        stmt.addBatch();
//
//                                    }
//                                } else {
//                                    sharedBankMembers.remove(0);
//                                    sharedBankMembers.remove(uuid);
//                                    String finalMembers = "";
//                                    for (String member : sharedBankMembers)
//                                        finalMembers = finalMembers.concat("." + member);
//                                    if (!sharedBankMembers.isEmpty())
//                                        finalMembers = finalMembers.substring(1);
//                                    stmtE.setString(1, finalMembers);
//                                    stmtE.setString(2, bankID);
//                                    stmtE.executeUpdate();
//                                }
//                            }
//                        }
//                    }
//                }
//                if(found) {
//                    GenerateFiles.archiveDatabase();
//                    Database.setAutoCommit(false);
//                    stmt.executeBatch();
//                    stmt1.executeBatch();
//                    stmt2.executeBatch();
//                    stmt3.executeBatch();
//                    Database.commit();
//                    Database.setAutoCommit(true);
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        });
    }

    public void updateUsername(Player player) {
        RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runAsync((task) -> {

            try (Connection Database = HikariCPDataSource.getConnection();
                 ResultSet r = Database.prepareStatement("SELECT username FROM PlayerPurse WHERE id = '" + player.getUniqueId() + "'").executeQuery();
                 PreparedStatement stmt1 = Database.prepareStatement("UPDATE PlayerPurse SET username = ? WHERE id = ?");
                 PreparedStatement stmt2 = Database.prepareStatement("INSERT INTO PlayerPurse VALUES (?, ? ,?, '')");
                 PreparedStatement stmt3 = Database.prepareStatement("INSERT INTO PersonalBank VALUES (?, ? , 0, '')");
            ) {
                if (r.next()) {
                    String name = r.getString("username");
                    if (name == null || !name.equals(player.getName())) {
                        stmt1.setString(1, player.getName());
                        stmt1.setString(2, player.getUniqueId().toString());
                        stmt1.executeUpdate();
                    }
                } else {
                    if (!usernameExists(player)) {
                        stmt2.setString(1, player.getUniqueId().toString());
                        stmt2.setString(2, player.getName());
                        stmt2.setDouble(3, RoyaleEconomy.staticValues.defaultCoins);
                        stmt2.executeUpdate();

                        stmt3.setString(1, player.getUniqueId().toString());
                        stmt3.setDouble(2, RoyaleEconomy.staticValues.defaultBankCoins);
                        stmt3.executeUpdate();
                    }
                }
            } catch (Exception e) {
                if (e.getMessage() != null && e.getMessage().startsWith("[SQLITE_BUSY]")) {
                    RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runLaterAsync(() -> updateUsername(player), 7);
                } else
                    e.printStackTrace();
            }
        });
    }

    public boolean triggerInterest() {
        RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runAsync((task) -> {
            String extra = "";
            if (staticValues.onlineInterestOnly) {
                if (Bukkit.getOnlinePlayers().isEmpty())
                    extra = " WHERE 1=0";
                else {
                    extra += (" WHERE id IN (");
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        extra += "'" + p.getUniqueId() + "',";
                    }
                    extra = extra.substring(0, extra.length() - 1);
                    extra += ")";
                }
            }
            try (
                    Connection Database = HikariCPDataSource.getConnection();
                    ResultSet rs = Database.prepareStatement("SELECT id,coins,upgrade,transactionlog FROM PersonalBank" + extra).executeQuery();
                    PreparedStatement u = Database.prepareStatement(
                            "UPDATE PersonalBank SET coins = ?, transactionlog = ? WHERE id = ?"
                    );
                    ResultSet rs2 = Database.prepareStatement("SELECT id,coins,upgrade,transactionlog FROM " + sharedBank.getTable()).executeQuery();
                    PreparedStatement u2 = Database.prepareStatement(
                            "UPDATE " + sharedBank.getTable() + " SET coins = coins + ?, transactionlog = ? WHERE id = ?"
                    );
            ) {

                String toInsert = RoyaleEconomy.menusCfg.getString("menus.main-bank-menu.transaction-history.lore-structure").replace("%player-name%", RoyaleEconomy.menusCfg.getString("menus.main-bank-menu.transaction-history.bank-interest-name"));
                while (rs.next()) {
                    double coins = rs.getDouble(2);
                    Double coinsAmount = interestCalculateFromUpgrade(coins, rs.getInt(3), false);
                    String finalToInsert = Utils.chat("%nl%" + ZonedDateTime.now().toInstant().toEpochMilli() + "###" + toInsert
                            .replace("%symbol%", "&a+")
                            .replace("%transaction-coins%", RoyaleEconomy.messageHelper.numberFormat(coinsAmount))
                    );
                    coins += coinsAmount;
                    String precedentLog = rs.getString(4);
                    if (precedentLog.split("%nl%").length == 11)
                        precedentLog = precedentLog.substring(4 + precedentLog.split("%nl%")[1].length());
                    String id = rs.getString(1);
                    u.setDouble(1, coins);
                    u.setString(2, precedentLog + finalToInsert);
                    u.setString(3, id);
                    u.addBatch();
                }

                String toInsert2 = RoyaleEconomy.menusCfg.getString("shared-menus.main-bank-menu.transaction-history.lore-structure").replace("%player-name%", RoyaleEconomy.menusCfg.getString("shared-menus.main-bank-menu.transaction-history.bank-interest-name"));
                while (rs2.next()) {
                    Double coinsAmount = SharedinterestCalculateFromUpgrade(rs2.getDouble(2), rs2.getInt(3), false);
                    String finalToInsert = Utils.chat("%nl%" + ZonedDateTime.now().toInstant().toEpochMilli() + "###" + toInsert2
                            .replace("%symbol%", "&a+")
                            .replace("%transaction-coins%", RoyaleEconomy.messageHelper.numberFormat(coinsAmount))
                    );
                    String precedentLog = rs2.getString(4);
                    if (precedentLog.split("%nl%").length == 11)
                        precedentLog = precedentLog.substring(4 + precedentLog.split("%nl%")[1].length());
                    u2.setDouble(1, coinsAmount);
                    u2.setString(2, precedentLog + finalToInsert);
                    u2.setString(3, rs2.getString(1));
                    u2.addBatch();
                }
                Database.setAutoCommit(false);
                u.executeBatch();
                u2.executeBatch();
                Database.commit();
                Database.setAutoCommit(true);
            } catch (Exception e) {
                e.printStackTrace();
            }

            ((PlayerMoneyCacheSQL) playerMoneyCache).giveInterest();

            String permission = RoyaleEconomy.commandsCfg.getString("commands.interest.broadcast.permission");
            if (permission.equals("none")) {
                for (Player p : Bukkit.getOnlinePlayers())
                    PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.interest.broadcast.message")));
            } else {
                for (Player p : Bukkit.getOnlinePlayers())
                    if (p.hasPermission(permission))
                        PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.interest.broadcast.message")));
            }
        });
        return true;
    }

    public Double interestCalculator(String p, boolean display, boolean shared) {
        int upgrade = 0;
        double coins = 0d;
        String sql;
        if (shared) {
            sql = "SELECT coins,upgrade FROM " + sharedBank.getTable() + " WHERE id = '" + sharedBank.getSharedBankId(p) + "'";
            try (
                    Connection Database = HikariCPDataSource.getConnection();
                    ResultSet r = Database.prepareStatement(sql).executeQuery();
            ) {
                if (r.next()) {
                    upgrade = r.getInt("upgrade");
                    coins = r.getDouble("coins");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            upgrade = getBankUpgrade(p);
            coins = getBankMoneyFromFile(p);
        }

        return shared ? SharedinterestCalculateFromUpgrade(coins, upgrade, display) : interestCalculateFromUpgrade(coins, upgrade, display);
    }

    private Double interestCalculateFromUpgrade(Double currentCoins, int bankUpgrade, boolean display) {
        Double toReturn = 0d;
        Double maximumCoins = bankUpgradesCfg.getDouble("bank-upgrades." + bankUpgrade + ".maximum-balance");

        ConfigurationSection f = bankUpgradesCfg.getConfigurationSection("bank-upgrades." + bankUpgrade + ".percentages");
        for (String key : f.getKeys(false)) {
            Double localVariable = 0d;
            if (currentCoins >= f.getDouble(key + ".minimum-coins")) {
                if (currentCoins >= f.getDouble(key + ".maximum-coins"))
                    localVariable += f.getDouble(key + ".maximum-coins") - f.getDouble(key + ".minimum-coins") + 1;
                else
                    localVariable += currentCoins - f.getDouble(key + ".minimum-coins") + 1;
                localVariable /= 100;
                localVariable *= f.getDouble(key + ".percent");
            }
            toReturn += localVariable;
        }

        if (!display && currentCoins + toReturn >= maximumCoins)
            toReturn = maximumCoins - currentCoins;

        return Math.floor(toReturn);
    }

    private Double SharedinterestCalculateFromUpgrade(Double currentCoins, int bankUpgrade, boolean display) {
        Double toReturn = 0d;
        Double maximumCoins = bankUpgradesCfg.getDouble("shared-bank-upgrades." + bankUpgrade + ".maximum-balance");

        ConfigurationSection f = bankUpgradesCfg.getConfigurationSection("shared-bank-upgrades." + bankUpgrade + ".percentages");
        for (String key : f.getKeys(false)) {
            Double localVariable = 0d;
            if (currentCoins >= f.getDouble(key + ".minimum-coins")) {
                if (currentCoins >= f.getDouble(key + ".maximum-coins"))
                    localVariable += f.getDouble(key + ".maximum-coins") - f.getDouble(key + ".minimum-coins") + 1;
                else
                    localVariable += currentCoins - f.getDouble(key + ".minimum-coins") + 1;
                localVariable /= 100;
                localVariable *= f.getDouble(key + ".percent");
            }
            toReturn += localVariable;
        }

        if (!display && currentCoins + toReturn >= maximumCoins)
            toReturn = maximumCoins - currentCoins;

        return Math.floor(toReturn);
    }

    public int getBankUpgrade(String player) {
        int upgrade = ((PlayerMoneyCacheSQL) playerMoneyCache).getBankUpgradeCache(player);
        if (upgrade != -1)
            return upgrade;
        else
            upgrade = 0;
        try (
                Connection Database = HikariCPDataSource.getConnection();
                ResultSet r = Database.prepareStatement("SELECT upgrade FROM PersonalBank WHERE id = '" + player + "'").executeQuery();
        ) {
            if (r.next()) {
                upgrade = r.getInt("upgrade");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return upgrade;
    }

    public void setBankUpgrade(String p, int bankUpgrade) {
        //RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runAsync((task) -> {
        try (Connection Database = HikariCPDataSource.getConnection();
             PreparedStatement stmt = Database.prepareStatement("UPDATE PersonalBank SET upgrade = ? WHERE id = ?");) {
            stmt.setInt(1, bankUpgrade);
            stmt.setString(2, p);
            stmt.executeUpdate();

        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().startsWith("[SQLITE_BUSY]")) {
                RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runLaterAsync(() -> setBankUpgrade(p, bankUpgrade), 7);
            } else
                e.printStackTrace();
        }
        //});
    }

    public WrappedTask checkInterest() {
        return RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runTimer(() -> {
            if (ZonedDateTime.now().toInstant().toEpochMilli() >= InterestCommand.interestDate) {
                Long timp = ZonedDateTime.now().toInstant().toEpochMilli();
                timp += RoyaleEconomy.plugin.getConfig().getInt("interest-cooldown") * 3600000L;
                RoyaleEconomy.dataManager.updateInterestDate(timp);
                InterestCommand.interestDate = timp;
                triggerInterest();
            }
        }, 400, 400);
    }

    public ArrayList<String> getTransactionLogFromFile(String player) {
        ArrayList<String> toReturn = new ArrayList<>();
        try (Connection Database = HikariCPDataSource.getConnection();
             ResultSet r = Database.prepareStatement("SELECT transactionlog FROM PersonalBank WHERE id = '" + player + "'").executeQuery();
        ) {
            if (r.next()) {
                if (r.getString("transactionlog").equals("")) {
                    for (String line : RoyaleEconomy.menusCfg.getStringList("menus.main-bank-menu.transaction-history.no-transaction-lore"))
                        toReturn.add(Utils.chat(line));
                } else {
                    String[] temp = r.getString("transactionlog").substring(4).split("%nl%");
                    for (String transaction : temp) {
                        Long date = Long.parseLong(transaction.split("###")[0]);
                        transaction = transaction.split("###")[1];
                        toReturn.add(0, transaction.replace("%transaction-countdown%", RoyaleEconomy.messageHelper.formatTimeNotDetailed(ZonedDateTime.now().toInstant().toEpochMilli() - date)));
                    }
                }
            } else {
                for (String line : RoyaleEconomy.menusCfg.getStringList("menus.main-bank-menu.transaction-history.no-transaction-lore"))
                    toReturn.add(Utils.chat(line));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return toReturn;
    }

    public void addTransactionLog(String uuid, String byWho, String symbol, double amount) {
        //RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runAsync((task) -> {
        String toInsert = RoyaleEconomy.menusCfg.getString("menus.main-bank-menu.transaction-history.lore-structure");
        toInsert = Utils.chat("%nl%" + ZonedDateTime.now().toInstant().toEpochMilli() + "###" + toInsert
                .replace("%symbol%", symbol)
                .replace("%transaction-coins%", RoyaleEconomy.messageHelper.numberFormat(amount))
                .replace("%player-name%", byWho)
        );

        try (Connection Database = HikariCPDataSource.getConnection();
             ResultSet r = Database.prepareStatement("SELECT transactionlog FROM PersonalBank WHERE id = '" + uuid + "'").executeQuery();
             PreparedStatement stmt = Database.prepareStatement("UPDATE PersonalBank SET transactionlog = ? WHERE id = ?");
        ) {
            String transactionLog = "";
            if (r.next()) {
                transactionLog = r.getString("transactionlog");
            }

            if (transactionLog.split("%nl%").length == 11)
                transactionLog = transactionLog.substring(4 + transactionLog.split("%nl%")[1].length());

            stmt.setString(1, transactionLog + toInsert);
            stmt.setString(2, uuid);
            stmt.executeUpdate();
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().startsWith("[SQLITE_BUSY]")) {
                RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runLaterAsync(() -> addTransactionLog(uuid, byWho, symbol, amount), 7);
            } else
                e.printStackTrace();
        }
        //});
    }

    public void loadTops() {
        sharedBank.loadTops();
    }

    /*public ArrayList<String> getSharedTransactionLogFromFile(String bankID) {
        ArrayList<String> toReturn = new ArrayList<>();
            try (
                    Connection Database = HikariCPDataSource.getConnection();
                    ResultSet r = Database.prepareStatement("SELECT transactionlog FROM SharedBank WHERE id = '" + bankID + "'").executeQuery();
            ) {
                if (r.next()) {
                    if (r.getString("transactionlog").equals("")) {
                        for (String line : RoyaleEconomy.menusCfg.getStringList("shared-menus.main-bank-menu.transaction-history.no-transaction-lore"))
                            toReturn.add(Utils.chat(line));
                    } else {
                        String[] temp = r.getString("transactionlog").substring(4).split("%nl%");
                        for (String transaction : temp) {
                            Long date = Long.parseLong(transaction.split("###")[0]);
                            transaction = transaction.split("###")[1];
                            toReturn.add(0, transaction.replace("%transaction-countdown%", RoyaleEconomy.messageHelper.formatTimeNotDetailed(ZonedDateTime.now().toInstant().toEpochMilli() - date)));
                        }
                    }
                } else {
                    for (String line : RoyaleEconomy.menusCfg.getStringList("shared-menus.main-bank-menu.transaction-history.no-transaction-lore"))
                        toReturn.add(Utils.chat(line));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

        return toReturn;
    }

    public ArrayList<String> getSharedTransactionLogPlayer(String playerUUID) {
        ArrayList<String> toReturn = new ArrayList<>();
        try (
                Connection Database = HikariCPDataSource.getConnection();
                ResultSet r = Database.prepareStatement("SELECT transactionlog FROM SharedBank WHERE owner = '" + playerUUID + "' OR members LIKE '%"+playerUUID+"%'").executeQuery();
        ) {
            if (r.next()) {
                if (r.getString("transactionlog").equals("")) {
                    for (String line : RoyaleEconomy.menusCfg.getStringList("shared-menus.main-bank-menu.transaction-history.no-transaction-lore"))
                        toReturn.add(Utils.chat(line));
                } else {
                    String[] temp = r.getString("transactionlog").substring(4).split("%nl%");
                    for (String transaction : temp) {
                        Long date = Long.parseLong(transaction.split("###")[0]);
                        transaction = transaction.split("###")[1];
                        toReturn.add(0, transaction.replace("%transaction-countdown%", RoyaleEconomy.messageHelper.formatTimeNotDetailed(ZonedDateTime.now().toInstant().toEpochMilli() - date)));
                    }
                }
            } else {
                for (String line : RoyaleEconomy.menusCfg.getStringList("shared-menus.main-bank-menu.transaction-history.no-transaction-lore"))
                    toReturn.add(Utils.chat(line));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return toReturn;
    }

    public void addSharedTransactionLog(String bankID, String byWho, String symbol, double amount) {
        //RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runAsync((task) -> {
            String toInsert = RoyaleEconomy.menusCfg.getString("shared-menus.main-bank-menu.transaction-history.lore-structure");
            toInsert = Utils.chat("%nl%" + ZonedDateTime.now().toInstant().toEpochMilli() + "###" + toInsert
                    .replace("%symbol%", symbol)
                    .replace("%transaction-coins%", RoyaleEconomy.messageHelper.numberFormat(amount))
                    .replace("%player-name%", byWho)
            );

            try (
                    Connection Database = HikariCPDataSource.getConnection();
                    PreparedStatement stmt = Database.prepareStatement("UPDATE SharedBank SET transactionlog = ? WHERE id = ?");
                    ResultSet r = Database.prepareStatement("SELECT transactionlog FROM SharedBank WHERE id = '" + bankID + "'").executeQuery();
            ) {
                String transactionLog = "";
                if (r.next()) {
                    transactionLog = r.getString("transactionlog");
                }

                if (transactionLog.split("%nl%").length == 11)
                    transactionLog = transactionLog.substring(4 + transactionLog.split("%nl%")[1].length());

                stmt.setString(1, transactionLog+toInsert);
                stmt.setString(2, bankID);
                stmt.executeUpdate();
            } catch (Exception e) {
                if(e.getMessage()!=null && e.getMessage().startsWith("[SQLITE_BUSY]")) {
                    RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runLaterAsync(() -> addSharedTransactionLog(bankID, byWho, symbol, amount), 7);
                }
                else
                    e.printStackTrace();
            }
        //});
    }

    public void addSharedTransactionLogPlayer(String playerUUID, String byWho, String symbol, double amount) {
        //RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runAsync((task) -> {
        String toInsert = RoyaleEconomy.menusCfg.getString("shared-menus.main-bank-menu.transaction-history.lore-structure");
        toInsert = Utils.chat("%nl%" + ZonedDateTime.now().toInstant().toEpochMilli() + "###" + toInsert
                .replace("%symbol%", symbol)
                .replace("%transaction-coins%", RoyaleEconomy.messageHelper.numberFormat(amount))
                .replace("%player-name%", byWho)
        );

        try (
                Connection Database = HikariCPDataSource.getConnection();
                PreparedStatement stmt = Database.prepareStatement("UPDATE SharedBank SET transactionlog = ? WHERE id = ?");
                ResultSet r = Database.prepareStatement("SELECT transactionlog,id FROM SharedBank WHERE owner = '" + playerUUID + "' OR members LIKE '%"+playerUUID+"%'").executeQuery();
        ) {
            String transactionLog = "";
            if (r.next()) {
                transactionLog = r.getString("transactionlog");
            }

            if (transactionLog.split("%nl%").length == 11)
                transactionLog = transactionLog.substring(4 + transactionLog.split("%nl%")[1].length());

            stmt.setString(1, transactionLog+toInsert);
            stmt.setString(2, r.getString(2));
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setSharedBankMoney(String bankID, Double amount) {
        //RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runAsync((task) -> {
            try (
                    Connection Database = HikariCPDataSource.getConnection();
                    PreparedStatement stmt = Database.prepareStatement("UPDATE SharedBank SET coins = ? WHERE id = ?");
            ) {
                stmt.setDouble(1, amount);
                stmt.setString(2, bankID);
                stmt.executeUpdate();

            } catch (Exception e) {
                if(e.getMessage()!=null && e.getMessage().startsWith("[SQLITE_BUSY]")) {
                    RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runLaterAsync(() -> setSharedBankMoney(bankID, amount), 7);
                }
                else
                    e.printStackTrace();
            }
        //});
    }

    public void setSharedBankMoneyPlayer(String playerUUID, Double amount) {
        //RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runAsync((task) -> {
        try (
                Connection Database = HikariCPDataSource.getConnection();
                PreparedStatement stmt = Database.prepareStatement("UPDATE SharedBank SET coins = ? WHERE owner = ? OR members LIKE ?");
        ) {
            stmt.setDouble(1, amount);
            stmt.setString(2, playerUUID);
            stmt.setString(3, "%"+playerUUID+"%");
            stmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
        //});
    }

    public void addSharedBankMoneyToFile(String bankID, Double amount) {
        //RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runAsync((task) -> {
            try (
                    Connection Database = HikariCPDataSource.getConnection();
                    PreparedStatement stmt = Database.prepareStatement("UPDATE SharedBank SET coins = coins +? WHERE id = ?");
            ) {
                stmt.setDouble(1, amount);
                stmt.setString(2, bankID);
                stmt.executeUpdate();
            } catch (Exception e) {
                e.printStackTrace();
                if(e.getMessage()!=null && e.getMessage().startsWith("[SQLITE_BUSY]")) {
                    RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runLaterAsync(() -> addSharedBankMoneyToFile(bankID, amount), 7);
                }
                else
                    e.printStackTrace();
            }
        //});
    }

    public void addSharedBankMoneyPlayer(String playerUUID, Double amount) {
        //RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runAsync((task) -> {
        try (
                Connection Database = HikariCPDataSource.getConnection();
                PreparedStatement stmt = Database.prepareStatement("UPDATE SharedBank SET coins = coins + ? WHERE owner = ? OR members LIKE ?");
        ) {
            stmt.setDouble(1, amount);
            stmt.setString(2, playerUUID);
            stmt.setString(3, "%"+playerUUID+"%");
            stmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
        //});
    }

    public void removeSharedBankMoneyToFile(String bankID, Double amount) {
        try (
                Connection Database = HikariCPDataSource.getConnection();
                PreparedStatement stmt = Database.prepareStatement("UPDATE SharedBank SET coins = coins - ? WHERE id = ?");
        ) {
            stmt.setDouble(1, amount);
            stmt.setString(2, bankID);
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void removeSharedBankMoneyPlayer(String playerUUID, Double amount) {
        //RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runAsync((task) -> {
        try (
                Connection Database = HikariCPDataSource.getConnection();
                PreparedStatement stmt = Database.prepareStatement("UPDATE SharedBank SET coins = coins - ? WHERE owner = ? OR members LIKE ?");
        ) {
            stmt.setDouble(1, amount);
            stmt.setString(2, playerUUID);
            stmt.setString(3, "%"+playerUUID+"%");
            stmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
        //});
    }

    public Double getSharedBankMoneyFromFile(String bankID) {
        Double toReturn = null;

        try (Connection Database = HikariCPDataSource.getConnection();
             ResultSet r=Database.prepareStatement("SELECT coins FROM SharedBank WHERE id = '" + bankID + "'").executeQuery();
        ) {
            if (r.next())
                toReturn = r.getDouble("coins");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return toReturn;
    }

    public Double getSharedBankMoneyPlayer(String playerUUID) {
        Double toReturn = null;

        try (Connection Database = HikariCPDataSource.getConnection();
             ResultSet r=Database.prepareStatement("SELECT coins FROM SharedBank WHERE owner = '" + playerUUID + "' OR members LIKE '%"+playerUUID+"%'").executeQuery();
        ) {
            if (r.next())
                toReturn = r.getDouble("coins");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return toReturn;
    }

    public boolean createSharedBank(Player p) {
        boolean success = false;
        try (Connection Database = HikariCPDataSource.getConnection();
             ResultSet r = Database.prepareStatement("SELECT sharedBank FROM PlayerPurse WHERE id = '" + p.getUniqueId() + "'").executeQuery();
             PreparedStatement stmt1 = Database.prepareStatement("INSERT INTO SharedBank VALUES (?, ?, '' ,? ,0, '')");
             PreparedStatement stmt2 = Database.prepareStatement("UPDATE PlayerPurse SET sharedBank = ? WHERE id = ? AND sharedBank = ''");
        ) {
            if (r.next()) {
                if (r.getString("sharedBank").equals("")) {
                    success = true;
                    String newBank = UUID.randomUUID().toString();
                    SharedBankCreateEvent event = new SharedBankCreateEvent(p, newBank);
                    Bukkit.getPluginManager().callEvent(event);
                    if (!event.isCancelled()) {
                        stmt1.setString(1, newBank);
                        stmt1.setString(2, p.getUniqueId().toString());
                        stmt1.setDouble(3, RoyaleEconomy.staticValues.defaultBankCoins);
                        stmt1.executeUpdate();

                        stmt2.setString(1, newBank);
                        stmt2.setString(2, p.getUniqueId().toString());
                        stmt2.executeUpdate();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return success;
    }

    public boolean deleteSharedBank(Player p) {
        boolean success = false;
        try (
                Connection Database = HikariCPDataSource.getConnection();
                ResultSet r = Database.prepareStatement("SELECT sharedBank FROM PlayerPurse WHERE id = '" + p.getUniqueId() + "'").executeQuery();
                PreparedStatement stmt1 = Database.prepareStatement("DELETE FROM SharedBank WHERE id=?");
                PreparedStatement stmt2 = Database.prepareStatement("UPDATE PlayerPurse SET sharedBank = '' WHERE sharedBank = ?");
        ) {
            if (r.next()) {
                String bank = r.getString("sharedBank");
                if (!bank.equals("")) {
                    success = true;
                            SharedBankDeleteEvent event = new SharedBankDeleteEvent(p, bank);
                            Bukkit.getPluginManager().callEvent(event);
                            if (!event.isCancelled()) {
                                stmt1.setString(1, bank);
                                stmt1.executeUpdate();

                                stmt2.setString(1, bank);
                                stmt2.executeUpdate();
                            }
                }
            }
        } catch (Exception e) {
            if(e.getMessage()!=null && e.getMessage().startsWith("[SQLITE_BUSY]")) {
                RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runLaterAsync(() -> deleteSharedBank(p), 7);
            }
            else
                e.printStackTrace();
        }
        return success;
    }

    public void deleteSharedBank(String bank) {
        //RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runAsync((task) -> {
            try (Connection Database = HikariCPDataSource.getConnection();
                 PreparedStatement stmt1 = Database.prepareStatement("DELETE FROM SharedBank WHERE id=?");
                 PreparedStatement stmt2 = Database.prepareStatement("UPDATE PlayerPurse SET sharedBank = '' WHERE sharedBank = ?");
               ) {
                if (!bank.equals("")) {
                    stmt1.setString(1, bank);
                    stmt1.executeUpdate();

                    stmt2.setString(1, bank);
                    stmt2.executeUpdate();
                }

            } catch (Exception e) {
                if(e.getMessage()!=null && e.getMessage().startsWith("[SQLITE_BUSY]")) {
                    RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runLaterAsync(() -> deleteSharedBank(bank), 7);
                }
                else
                    e.printStackTrace();
            }
        //});
    }

    public String getSharedBankId(String player) {
        String toReturn = null;

        try (
                Connection Database = HikariCPDataSource.getConnection();
                ResultSet r = Database.prepareStatement(player.length()>16?"SELECT sharedBank FROM PlayerPurse WHERE id = '" + player + "'":"SELECT sharedBank FROM PlayerPurse WHERE upper(username) = '" + player.toUpperCase() + "'").executeQuery()
        ) {
            if (r.next())
                toReturn = r.getString("sharedBank");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return toReturn;
    }

    public boolean isOwnerSharedBank(Player p) {
        boolean isOwner = false;
        try (
                Connection Database = HikariCPDataSource.getConnection();
                ResultSet r = Database.prepareStatement("SELECT owner FROM SharedBank WHERE owner = '" + p.getUniqueId() + "' LIMIT 1").executeQuery();
        ) {
            if (r.next()) {
                isOwner = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return isOwner;
    }

    public boolean transferOwnershipSharedBank(String oldOwner, String newOwner) {
        boolean success = false;
            try (Connection Database = HikariCPDataSource.getConnection();
                 PreparedStatement stmt = Database.prepareStatement("UPDATE SharedBank SET owner=?, members = REPLACE(members, ?, ?) WHERE owner = ? AND members LIKE ?");) {

                stmt.setString(1, newOwner);
                stmt.setString(2, newOwner);
                stmt.setString(3, oldOwner);
                stmt.setString(4, oldOwner);
                stmt.setString(5, "%"+newOwner+"%");
                int c=stmt.executeUpdate();
                if(c!=0)
                    success=true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        return success;
    }

    public boolean transferOwnershipSharedBank(String newOwner) {
        boolean success = false;
        String bankID = getSharedBankId(newOwner);
        ArrayList<String> members = getMembersSharedBank(bankID, true);
        String oldOwner = members.get(0);
        try (Connection Database = HikariCPDataSource.getConnection();
             PreparedStatement stmt = Database.prepareStatement("UPDATE SharedBank SET owner=?, members = ? WHERE id = ?");) {
            members.remove(newOwner);
            success = true;
            String finalMembers = "";
            for (String member : members)
                finalMembers = finalMembers.concat("." + member);
            finalMembers = finalMembers.substring(1);
            stmt.setString(1, newOwner);
            stmt.setString(2, finalMembers);
            stmt.setString(3, bankID);
            stmt.executeUpdate();
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().startsWith("[SQLITE_BUSY]")) {
                RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runLaterAsync(() -> transferOwnershipSharedBank(oldOwner, newOwner), 7);
            } else
                e.printStackTrace();
        }
        return success;
    }

    public ArrayList<String> getMembersSharedBank(String bankID, boolean owner) {
        ArrayList<String> members = new ArrayList<>();
        try (
                Connection Database = HikariCPDataSource.getConnection();
                ResultSet r = Database.prepareStatement(owner?"SELECT owner,members FROM SharedBank WHERE id = '" + bankID + "'":"SELECT members FROM SharedBank WHERE id = '" + bankID + "'").executeQuery();
        ) {
            if (r.next()) {
                if (owner)
                    members.add(r.getString("owner"));
                String membersString = r.getString("members");
                if (!membersString.equals(""))
                    members.addAll(Arrays.asList(membersString.split("\\.")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return members;
    }

    public ArrayList<String> getMembersSharedBankPlayer(String playerUUID, boolean owner) {
        ArrayList<String> members = new ArrayList<>();
        try (
                Connection Database = HikariCPDataSource.getConnection();
                ResultSet r = Database.prepareStatement(owner?"SELECT owner,members FROM SharedBank WHERE owner = '" + playerUUID + "' OR members LIKE '%"+playerUUID+"%'":"SELECT members FROM SharedBank WHERE owner = '" + playerUUID + "' OR members LIKE '%"+playerUUID+"%'").executeQuery();
        ) {
            if (r.next()) {
                if (owner)
                    members.add(r.getString("owner"));
                String membersString = r.getString("members");
                if (!membersString.equals(""))
                    members.addAll(Arrays.asList(membersString.split("\\.")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return members;
    }

    public boolean addPlayerToSharedBank(Player p, String bankID) {
        boolean added = false;
        int upgrade;
        ArrayList<String> members = new ArrayList<>();
        try (
                Connection Database = HikariCPDataSource.getConnection();
                ResultSet r = Database.prepareStatement("SELECT members,upgrade FROM SharedBank WHERE id = '" + bankID + "'").executeQuery();
                PreparedStatement stmt1 = Database.prepareStatement("UPDATE SharedBank SET members = ? WHERE id = ?");
                PreparedStatement stmt2 = Database.prepareStatement("UPDATE PlayerPurse SET sharedBank = ? WHERE id = ?");
        ) {
            if (r.next()) {
                upgrade = r.getInt("upgrade");
                String membersString = r.getString("members");
                if (!membersString.equals(""))
                    members.addAll(Arrays.asList(membersString.split("\\.")));
                if (members.size() < bankUpgradesCfg.getInt("shared-bank-upgrades." + upgrade + ".player-limit")) {
                    if (!members.contains(p.getUniqueId().toString())) {
                        added = true;
                        members.add(p.getUniqueId().toString());
                        String finalMembers = "";
                        for (String member : members)
                            finalMembers = finalMembers.concat("." + member);
                        finalMembers = finalMembers.substring(1);
                        stmt1.setString(1, finalMembers);
                        stmt1.setString(2, bankID);
                        stmt1.executeUpdate();

                        stmt2.setString(1, bankID);
                        stmt2.setString(2, p.getUniqueId().toString());
                        stmt2.executeUpdate();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return added;
    }

    public boolean addPlayerToSharedBank(String pUUID, String bankID) {
        boolean added = false;
        int upgrade;
        ArrayList<String> members = new ArrayList<>();
        try (
                Connection Database = HikariCPDataSource.getConnection();
                ResultSet r = Database.prepareStatement("SELECT members,upgrade FROM SharedBank WHERE id = '" + bankID + "'").executeQuery();
                PreparedStatement stmt1 = Database.prepareStatement("UPDATE SharedBank SET members = ? WHERE id = ?");
                PreparedStatement stmt2 = Database.prepareStatement("UPDATE PlayerPurse SET sharedBank = ? WHERE id = ?");
        ) {
            if (r.next()) {
                upgrade = r.getInt("upgrade");
                String membersString = r.getString("members");
                if (!membersString.equals(""))
                    members.addAll(Arrays.asList(membersString.split("\\.")));
                if (members.size() < bankUpgradesCfg.getInt("shared-bank-upgrades." + upgrade + ".player-limit")) {
                    if (!members.contains(pUUID)) {
                        added = true;
                        members.add(pUUID);
                        String finalMembers = "";
                        for (String member : members)
                            finalMembers = finalMembers.concat("." + member);
                        finalMembers = finalMembers.substring(1);
                        stmt1.setString(1, finalMembers);
                        stmt1.setString(2, bankID);
                        stmt1.executeUpdate();

                        stmt2.setString(1, bankID);
                        stmt2.setString(2, pUUID);
                        stmt2.executeUpdate();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return added;
    }


    public boolean removePlayerFromSharedBank(String player, String bankID) {
        boolean removed = false;
        ArrayList<String> members;
        if (player.length() < 17)
            player = getUUIDfromName(player);
        final String finalPlayer = player;
        members = getMembersSharedBank(bankID, false);
        try (
                Connection Database = HikariCPDataSource.getConnection();
                PreparedStatement stmt1 = Database.prepareStatement("UPDATE SharedBank SET members = ? WHERE id = ?");
                PreparedStatement stmt2 = Database.prepareStatement("UPDATE PlayerPurse SET sharedBank = '' WHERE id = ?");
        ) {
            if (members.contains(player)) {
                removed = true;
                members.remove(player);
                String finalMembers = "";
                for (String member : members)
                    finalMembers = finalMembers.concat("." + member);
                if (!members.isEmpty())
                    finalMembers = finalMembers.substring(1);
                stmt1.setString(1, finalMembers);
                stmt1.setString(2, bankID);
                stmt1.executeUpdate();

                stmt2.setString(1, player);
                stmt2.executeUpdate();
            }
        } catch (Exception e) {
            if(e.getMessage()!=null && e.getMessage().startsWith("[SQLITE_BUSY]")) {
                RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runLaterAsync(() -> removePlayerFromSharedBank(finalPlayer, bankID), 7);
            }
            else
                e.printStackTrace();
        }
        return removed;
    }

    public int getSharedBankUpgrade(String bankID) {
        int upgrade = 0;
        try (
                Connection Database = HikariCPDataSource.getConnection();
                ResultSet r = Database.prepareStatement("SELECT upgrade FROM SharedBank WHERE id = '" + bankID + "'").executeQuery();
        ) {
            if (r.next()) {
                upgrade = r.getInt("upgrade");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return upgrade;
    }

    public int getSharedBankUpgradePlayer(String playerUUID) {
        int upgrade = 0;
        try (
                Connection Database = HikariCPDataSource.getConnection();
                ResultSet r = Database.prepareStatement("SELECT upgrade FROM SharedBank WHERE owner = '" + playerUUID + "' OR members LIKE '%"+playerUUID+"%'").executeQuery();
        ) {
            if (r.next()) {
                upgrade = r.getInt("upgrade");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return upgrade;
    }

    public void setSharedBankUpgrade(String bankID, int bankUpgrade) {
        //RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runAsync((task) -> {
            try (
                    Connection Database = HikariCPDataSource.getConnection();
                    PreparedStatement stmt = Database.prepareStatement("UPDATE SharedBank SET upgrade = ? WHERE id = ?");
            ) {
                stmt.setInt(1, bankUpgrade);
                stmt.setString(2, bankID);
                stmt.executeUpdate();

            } catch (Exception e) {
                if(e.getMessage()!=null && e.getMessage().startsWith("[SQLITE_BUSY]")) {
                    RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runLaterAsync(() -> setSharedBankUpgrade(bankID, bankUpgrade), 7);
                }
                else
                    e.printStackTrace();
            }
        //});
    }

    public void setSharedBankUpgradePlayer(String playerUUID, int bankUpgrade) {
        //RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runAsync((task) -> {
        try (
                Connection Database = HikariCPDataSource.getConnection();
                PreparedStatement stmt = Database.prepareStatement("UPDATE SharedBank SET upgrade = ? WHERE owner = ? OR members LIKE ?");
        ) {
            stmt.setInt(1, bankUpgrade);
            stmt.setString(2, playerUUID);
            stmt.setString(3, "%"+playerUUID+"%");
            stmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
        //});
    }*/
}
