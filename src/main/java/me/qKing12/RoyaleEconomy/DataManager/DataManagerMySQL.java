package me.qKing12.RoyaleEconomy.DataManager;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.tcoded.folialib.wrapper.task.WrappedTask;
import me.qKing12.RoyaleEconomy.API.Events.CoinsAddToPurseEvent;
import me.qKing12.RoyaleEconomy.API.Events.CoinsRemoveFromPurseEvent;
import me.qKing12.RoyaleEconomy.Commands.InterestCommand;
import me.qKing12.RoyaleEconomy.DataManager.Cache.BungeeMessagingCacheMySQL;
import me.qKing12.RoyaleEconomy.DataManager.Cache.MySQLBankCache;
import me.qKing12.RoyaleEconomy.DataManager.Cache.PlayerMoneyCacheMySQL;
import me.qKing12.RoyaleEconomy.DataManager.Cache.redis.RedisHandler;
import me.qKing12.RoyaleEconomy.DataManager.SharedBank.SharedBank;
import me.qKing12.RoyaleEconomy.DataManager.SharedBank.SharedBankMySQL;
import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import me.qKing12.RoyaleEconomy.utils.PlayerMessageHandler;
import me.qKing12.RoyaleEconomy.utils.Utils;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static me.qKing12.RoyaleEconomy.RoyaleEconomy.*;


public class DataManagerMySQL implements DataManager {
    private String host;
    private Integer port;
    private String user;
    private String password;
    public static String database;
    public static MySQLBankCache bankCache;
    private SharedBank sharedBank;

    @Override
    public SharedBank getSharedBankManager() {
        return sharedBank;
    }

    @Override
    public void createCurrency(String currencyName) {
        try(Connection PlayerPurse = HikariCPDataSource.getConnection();
            Statement stmt = PlayerPurse.createStatement();
        ) {

            String sql = "CREATE TABLE IF NOT EXISTS "+database+".RoyaleEconomy"+currencyName +
                    "(id VARCHAR(36) not NULL, " +
                    " coins DOUBLE(25, 2), " +
                    " PRIMARY KEY ( id ))";

            stmt.execute(sql);
            RoyaleEconomy.plugin.getLogger().info("Succesfully connected to the Currency "+currencyName+" MySQL.");

        } catch (Exception e) {
            RoyaleEconomy.plugin.getLogger().info("An error has occured while connecting to the Currency "+currencyName+" MySQL.");
            RoyaleEconomy.plugin.getLogger().info(e.getMessage());
        }
    }

    @Override
    public void deleteCurrency(String currencyName) {
        try(Connection PlayerPurse = HikariCPDataSource.getConnection();
            Statement stmt = PlayerPurse.createStatement();
        ) {

            String sql = "DROP TABLE IF EXISTS "+database+".RoyaleEconomy"+currencyName;

            stmt.execute(sql);
            RoyaleEconomy.plugin.getLogger().info("Succesfully deleted the Currency "+currencyName+" SQL.");

        } catch (Exception e) {
            RoyaleEconomy.plugin.getLogger().info("An error has occured while deleting the Currency "+currencyName+" SQL.");
            RoyaleEconomy.plugin.getLogger().info(e.getMessage());
        }
    }

    @Override
    public double getCurrencyMoney(String playerUUID, String currency) {
        double toReturn = 0;

        try (
                Connection Database = HikariCPDataSource.getConnection();
                ResultSet r = Database.prepareStatement("SELECT coins FROM "+database+".RoyaleEconomy"+currency+" WHERE id = '" + playerUUID + "'").executeQuery();

        ) {
            if (r.next())
                toReturn = r.getDouble("coins");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return toReturn;
    }

    @Override
    public void setCurrencyMoney(String playerUUID, double amount, String currency) {
        try (
                Connection Database = HikariCPDataSource.getConnection();
                PreparedStatement stmt2 = Database.prepareStatement("INSERT INTO "+database+".RoyaleEconomy"+currency+" VALUES(?, ?) ON DUPLICATE KEY UPDATE coins = ?");
        ) {
            stmt2.setString(1, playerUUID);
            stmt2.setDouble(2, amount);
            stmt2.setDouble(3, amount);
            stmt2.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addCurrencyMoney(String playerUUID, double amount, String currency) {
        try (
                Connection Database = HikariCPDataSource.getConnection();
                PreparedStatement stmt2 = Database.prepareStatement("INSERT INTO "+database+".RoyaleEconomy"+currency+" VALUES(?, ?) ON DUPLICATE KEY UPDATE coins = coins + ?");
        ) {
            stmt2.setString(1, playerUUID);
            stmt2.setDouble(2, amount);
            stmt2.setDouble(3, amount);
            stmt2.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeCurrencyMoney(String playerUUID, double amount, String currency) {
        try (
                Connection Database = HikariCPDataSource.getConnection();
                PreparedStatement stmt2 = Database.prepareStatement("INSERT INTO "+database+".RoyaleEconomy"+currency+" VALUES(?, ?) ON DUPLICATE KEY UPDATE coins = coins - ?");
        ) {
            stmt2.setString(1, playerUUID);
            stmt2.setDouble(2, amount);
            stmt2.setDouble(3, amount);
            stmt2.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<String[]> getCurrencyBalanceTop(String currencyId){
        try(
                Connection Database = HikariCPDataSource.getConnection();
                PreparedStatement stmt = Database.prepareStatement("SELECT * FROM "+database+".RoyaleEconomy"+currencyId+" ORDER BY coins DESC LIMIT 10");
                ResultSet r = stmt.executeQuery();
        ){
            List<String[]> toReturn = new ArrayList<>();
            while(r.next()){
                String[] toAdd = new String[2];
                toAdd[0] = getNameFromUUID(r.getString("id"));
                toAdd[1] = String.format("%.2f", r.getDouble("coins"));
                toReturn.add(toAdd);
            }
            return toReturn;
        }
        catch(SQLException x){
            x.printStackTrace();
        }

        return new ArrayList<>();
    }

    public static void resetSellLimit(int day){
        try (Connection Database = HikariCPDataSource.getConnection();
             PreparedStatement stmt = Database.prepareStatement("DELETE FROM " + database + ".RECSellLimit");
             PreparedStatement stmt2 = Database.prepareStatement("INSERT INTO "+database+".RECSellLimit VALUES(?, ?)");
        ) {
            stmt.executeUpdate();
            stmt2.setString(1, "day");
            stmt2.setDouble(2, day);
            stmt2.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private EconomyFunctions economyFunctions;

    public DataManagerMySQL(boolean noEconomy) {
        host = RoyaleEconomy.plugin.getConfig().getString("mysql.host");
        port = RoyaleEconomy.plugin.getConfig().getInt("mysql.port");
        user = RoyaleEconomy.plugin.getConfig().getString("mysql.username");
        password = RoyaleEconomy.plugin.getConfig().getString("mysql.password");
        database = "`" + RoyaleEconomy.plugin.getConfig().getString("mysql.database") + "`";
        boolean useSsl = RoyaleEconomy.plugin.getConfig().getBoolean("mysql.useSSL");
        boolean allowPublicKeyRetrieval = RoyaleEconomy.plugin.getConfig().getBoolean("mysql.allowPublicKeyRetrieval");
        Connection con = null;
        try {
            HikariCPDataSource.loadSettings("jdbc:mysql://" + host + ":" + port + "/"+RoyaleEconomy.plugin.getConfig().getString("mysql.database")+"?useUnicode=true&characterEncoding=utf8&useSSL="+useSsl+"&verifyServerCertificate="+useSsl+"&allowPublicKeyRetrieval="+allowPublicKeyRetrieval, user, password, true);
            //Database = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/?useUnicode=true&characterEncoding=utf8&useSSL=false&verifyServerCertificate=false", user, password);
            String sql = "CREATE DATABASE IF NOT EXISTS " + database;
            con = HikariCPDataSource.getConnection();
            final Statement stmt = con.createStatement();
            stmt.executeUpdate(sql);
            stmt.close();
            new MySQLLoad(database);
            if(!plugin.getConfig().getBoolean("mysql.bungeecord"))
                removeOldUsers();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        }
        if (noEconomy) {
            economyFunctions = new noEconomyFunctions();
            if(plugin.getConfig().getBoolean("redis.use-redis"))
                new RedisHandler();
            else if (plugin.getConfig().getBoolean("mysql.bungeecord")) {
                new BungeeMessagingCacheMySQL();
            }
        } else {
            if(plugin.getConfig().getBoolean("redis.use-redis")) {
                new RedisHandler();
                economyFunctions = new yesEconomyRedisFunctions();
            }
            else if (plugin.getConfig().getBoolean("mysql.bungeecord")) {
                new BungeeMessagingCacheMySQL();
                economyFunctions = new yesEconomyBungeecordFunctions();
            } else
                economyFunctions = new yesEconomyFunctions();
        }
        bankCache=new MySQLBankCache();
        sharedBank = new SharedBankMySQL(database, "SharedBank");
    }

    public interface EconomyFunctions {

        public void setMoney(String player, Double amount);

        public void addMoneyToFile(String player, Double amount);

        public void addMoneyFromPay(String player, double amount, String message);

        public boolean removeMoneyFromFile(String player, Double amount);

        public Double getMoneyFromFile(String player);
    }

    private class noEconomyFunctions implements EconomyFunctions {
        @Override
        public void addMoneyFromPay(String player, double amount, String message) {

        }

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
                if(!response.transactionSuccess()){
                    plugin.getLogger().warning("[DEBUG EXTERNAL ECONOMY] Player "+player+", amount = "+amountFloored+", balance = "+getMoneyFromFile(player)+": "+response.errorMessage);
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
                return 0d;
            }
        }
    }

    private class yesEconomyFunctions implements EconomyFunctions {
        @Override
        public void addMoneyFromPay(String player, double amount, String message) {

        }

        public void setMoney(String player, Double amount) {
            if (playerMoneyCache.setBalance(player, amount)) {
                return;
            }
            //RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runAsync((task) -> {
            try (
                    Connection Database = HikariCPDataSource.getConnection();
                    PreparedStatement stmt = Database.prepareStatement(player.length() > 16 ? "UPDATE " + database + ".PlayerPurse SET coins = ? WHERE id = ?" : "UPDATE " + database + ".PlayerPurse SET coins = ? WHERE upper(username) = ?");
            ) {
                stmt.setDouble(1, amount);
                stmt.setString(2, player.length() > 16 ? player : player.toUpperCase());
                stmt.executeUpdate();

            } catch (Exception e) {
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
                 PreparedStatement stmt1 = Database.prepareStatement(player.length() > 16 ? "UPDATE " + database + ".PlayerPurse SET coins = coins+? WHERE id = ?" : "UPDATE " + database + ".PlayerPurse SET coins = coins+? WHERE upper(username) = ?");
                 PreparedStatement stmt2 = Database.prepareStatement("UPDATE " + database + ".ExternalGeneratedData SET coins = coins+? WHERE id = ?");
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
                            PreparedStatement stmt1 = Database.prepareStatement(player.length() > 16 ? "UPDATE " + database + ".PlayerPurse SET coins = coins-? WHERE id = ?" : "UPDATE " + database + ".PlayerPurse SET coins = coins-? WHERE upper(username) = ?");
                            PreparedStatement stmt2 = Database.prepareStatement("UPDATE " + database + ".ExternalGeneratedData SET coins = coins-? WHERE id = ?");
                    ) {
                        RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runNextTick((task) -> Bukkit.getPluginManager().callEvent(new CoinsRemoveFromPurseEvent(player, amountFloored)));
                        stmt1.setDouble(1, amountFloored);
                        stmt1.setString(2, player.length() > 16 ? player : player.toUpperCase());
                        int updated = stmt1.executeUpdate();
                        if (updated == 0) {
                            stmt2.setDouble(1, amountFloored);
                            stmt2.setString(2, player);
                            stmt2.executeUpdate();
                        }
                        return true;
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
                    ResultSet r = Database.prepareStatement(player.length() > 16 ? "SELECT coins FROM " + database + ".PlayerPurse WHERE id = '" + player + "'" : "SELECT coins FROM " + database + ".PlayerPurse WHERE upper(username) = '" + player.toUpperCase() + "'").executeQuery();
                    ResultSet r2 = Database.prepareStatement("SELECT coins FROM " + database + ".ExternalGeneratedData WHERE id = '" + player + "'").executeQuery();

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

    private class yesEconomyBungeecordFunctions implements EconomyFunctions {
        public void setMoney(String player, Double amount) {
            if (playerMoneyCache.setBalance(player, amount)) {
                return;
            }
            //RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runAsync((task) -> {
            try (
                    Connection Database = HikariCPDataSource.getConnection();
                    PreparedStatement stmt = Database.prepareStatement(player.length() > 16 ? "UPDATE " + database + ".PlayerPurse SET coins = ? WHERE id = ?" : "UPDATE " + database + ".PlayerPurse SET coins = ? WHERE upper(username) = ?");
            ) {
                stmt.setDouble(1, amount);
                stmt.setString(2, player.length() > 16 ? player : player.toUpperCase());
                stmt.executeUpdate();

            } catch (Exception e) {
                e.printStackTrace();
            }

            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("RoyaleEconomyMoneyTransfer"); // the channel could be whatever you want
            out.writeUTF("setMoney");
            out.writeUTF(player); // this data could be whatever you want
            out.writeDouble(amount);

            if(Bukkit.getOnlinePlayers().isEmpty()){
                plugin.getLogger().warning("There were no players online so the message wasn't sent to the bungeecord!");
                plugin.getLogger().warning("This can cause synchronization problems and money loss.");
                plugin.getLogger().warning("Potential Fixes:");
                plugin.getLogger().warning(" - Make sure you don't give money through console unless the player is not online on any server or it is in the same server.");
                plugin.getLogger().warning(" - Consider setting up a redis server.");
            }
                //Bukkit.getServer().sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
            else {
                Bukkit.getOnlinePlayers().stream().findAny().get().sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
                Bukkit.getOnlinePlayers().stream().findAny().get().sendPluginMessage(plugin, "royaleeconomy:main", out.toByteArray());
            }
        }

        public void addMoneyToFile(String player, Double amount) {
            if (playerMoneyCache.addBalance(player, amount)) {
                RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runNextTick((task) -> Bukkit.getPluginManager().callEvent(new CoinsAddToPurseEvent(player, amount)));
                return;
            }
            //RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runAsync((task) -> {
            try (Connection Database = HikariCPDataSource.getConnection();
                 PreparedStatement stmt1 = Database.prepareStatement(player.length() > 16 ? "UPDATE " + database + ".PlayerPurse SET coins = coins+? WHERE id = ?" : "UPDATE " + database + ".PlayerPurse SET coins = coins+? WHERE upper(username) = ?");
                 PreparedStatement stmt2 = Database.prepareStatement("UPDATE " + database + ".ExternalGeneratedData SET coins = coins+? WHERE id = ?");
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
                e.printStackTrace();
            }
            //});
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("RoyaleEconomyMoneyTransfer"); // the channel could be whatever you want
            out.writeUTF("addMoney");
            out.writeUTF(player); // this data could be whatever you want
            out.writeDouble(amount);

            if(Bukkit.getOnlinePlayers().isEmpty()){
                plugin.getLogger().warning("There were no players online so the message wasn't sent to the bungeecord!");
                plugin.getLogger().warning("This can cause synchronization problems and money loss.");
                plugin.getLogger().warning("Potential Fixes:");
                plugin.getLogger().warning(" - Make sure you don't give money through console unless the player is not online on any server or it is in the same server.");
                plugin.getLogger().warning(" - Consider setting up a redis server.");
            }
                //Bukkit.getServer().sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
            else {
                Bukkit.getOnlinePlayers().stream().findAny().get().sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
                Bukkit.getOnlinePlayers().stream().findAny().get().sendPluginMessage(plugin, "royaleeconomy:main", out.toByteArray());
            }
        }

        public void addMoneyFromPay(String player, double amount, String message) {
            //RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runAsync((task) -> {
            try (Connection Database = HikariCPDataSource.getConnection();
                 PreparedStatement stmt1 = Database.prepareStatement(player.length() > 16 ? "UPDATE " + database + ".PlayerPurse SET coins = coins+? WHERE id = ?" : "UPDATE " + database + ".PlayerPurse SET coins = coins+? WHERE upper(username) = ?");
                 PreparedStatement stmt2 = Database.prepareStatement("UPDATE " + database + ".ExternalGeneratedData SET coins = coins+? WHERE id = ?");
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
                e.printStackTrace();
            }

            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("RoyaleEconomyMoneyTransfer"); // the channel could be whatever you want
            out.writeUTF("payMoney");
            out.writeUTF(player); // this data could be whatever you want
            out.writeDouble(amount);
            out.writeUTF(message);

            if(Bukkit.getOnlinePlayers().isEmpty()){
                plugin.getLogger().warning("There were no players online so the message wasn't sent to the bungeecord!");
                plugin.getLogger().warning("This can cause synchronization problems and money loss.");
                plugin.getLogger().warning("Potential Fixes:");
                plugin.getLogger().warning(" - Make sure you don't give money through console unless the player is not online on any server or it is in the same server.");
                plugin.getLogger().warning(" - Consider setting up a redis server.");
            }
                //Bukkit.getServer().sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
            else {
                Bukkit.getOnlinePlayers().stream().findAny().get().sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
                Bukkit.getOnlinePlayers().stream().findAny().get().sendPluginMessage(plugin, "royaleeconomy:main", out.toByteArray());
            }
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
                            PreparedStatement stmt1 = Database.prepareStatement(player.length() > 16 ? "UPDATE " + database + ".PlayerPurse SET coins = coins-? WHERE id = ?" : "UPDATE " + database + ".PlayerPurse SET coins = coins-? WHERE upper(username) = ?");
                            PreparedStatement stmt2 = Database.prepareStatement("UPDATE " + database + ".ExternalGeneratedData SET coins = coins-? WHERE id = ?");
                    ) {
                        RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runNextTick((task) -> Bukkit.getPluginManager().callEvent(new CoinsRemoveFromPurseEvent(player, amountFloored)));
                        stmt1.setDouble(1, amountFloored);
                        stmt1.setString(2, player.length() > 16 ? player : player.toUpperCase());
                        int updated = stmt1.executeUpdate();
                        if (updated == 0) {
                            stmt2.setDouble(1, amountFloored);
                            stmt2.setString(2, player);
                            stmt2.executeUpdate();
                        }


                        ByteArrayDataOutput out = ByteStreams.newDataOutput();
                        out.writeUTF("RoyaleEconomyMoneyTransfer"); // the channel could be whatever you want
                        out.writeUTF("removeMoney");
                        out.writeUTF(player); // this data could be whatever you want
                        out.writeDouble(amount);

                        if(Bukkit.getOnlinePlayers().isEmpty()){
                            plugin.getLogger().warning("There were no players online so the message wasn't sent to the bungeecord!");
                            plugin.getLogger().warning("This can cause synchronization problems and money loss.");
                            plugin.getLogger().warning("Potential Fixes:");
                            plugin.getLogger().warning(" - Make sure you don't give money through console unless the player is not online on any server or it is in the same server.");
                            plugin.getLogger().warning(" - Consider setting up a redis server.");
                        }
                        //Bukkit.getServer().sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
                        else {
                            Bukkit.getOnlinePlayers().stream().findAny().get().sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
                            Bukkit.getOnlinePlayers().stream().findAny().get().sendPluginMessage(plugin, "royaleeconomy:main", out.toByteArray());
                        }
                        return true;
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
                    ResultSet r = Database.prepareStatement(player.length() > 16 ? "SELECT coins FROM " + database + ".PlayerPurse WHERE id = '" + player + "'" : "SELECT coins FROM " + database + ".PlayerPurse WHERE upper(username) = '" + player.toUpperCase() + "'").executeQuery();
                    ResultSet r2 = Database.prepareStatement("SELECT coins FROM " + database + ".ExternalGeneratedData WHERE id = '" + player + "'").executeQuery();

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

    private class yesEconomyRedisFunctions implements EconomyFunctions {
        public void setMoney(String player, Double amount) {
            if (playerMoneyCache.setBalance(player, amount)) {
                return;
            }
            //RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runAsync((task) -> {
            try (
                    Connection Database = HikariCPDataSource.getConnection();
                    PreparedStatement stmt = Database.prepareStatement(player.length() > 16 ? "UPDATE " + database + ".PlayerPurse SET coins = ? WHERE id = ?" : "UPDATE " + database + ".PlayerPurse SET coins = ? WHERE upper(username) = ?");
            ) {
                stmt.setDouble(1, amount);
                stmt.setString(2, player.length() > 16 ? player : player.toUpperCase());
                stmt.executeUpdate();

            } catch (Exception e) {
                e.printStackTrace();
            }

            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF(player);
            out.writeUTF("RoyaleEconomyMoneyTransfer"); // the channel could be whatever you want
            out.writeUTF("setMoney");
            out.writeUTF(player); // this data could be whatever you want
            out.writeDouble(amount);

            RedisHandler.sendData(out.toByteArray());
        }

        public void addMoneyToFile(String player, Double amount) {
            if (playerMoneyCache.addBalance(player, amount)) {
                RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runNextTick((task) -> Bukkit.getPluginManager().callEvent(new CoinsAddToPurseEvent(player, amount)));
                return;
            }
            //RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runAsync((task) -> {
            try (Connection Database = HikariCPDataSource.getConnection();
                 PreparedStatement stmt1 = Database.prepareStatement(player.length() > 16 ? "UPDATE " + database + ".PlayerPurse SET coins = coins+? WHERE id = ?" : "UPDATE " + database + ".PlayerPurse SET coins = coins+? WHERE upper(username) = ?");
                 PreparedStatement stmt2 = Database.prepareStatement("UPDATE " + database + ".ExternalGeneratedData SET coins = coins+? WHERE id = ?");
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
                e.printStackTrace();
            }
            //});
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF(player);
            out.writeUTF("RoyaleEconomyMoneyTransfer"); // the channel could be whatever you want
            out.writeUTF("addMoney");
            out.writeUTF(player); // this data could be whatever you want
            out.writeDouble(amount);

            RedisHandler.sendData(out.toByteArray());
        }

        public void addMoneyFromPay(String player, double amount, String message) {
            //RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runAsync((task) -> {
            try (Connection Database = HikariCPDataSource.getConnection();
                 PreparedStatement stmt1 = Database.prepareStatement(player.length() > 16 ? "UPDATE " + database + ".PlayerPurse SET coins = coins+? WHERE id = ?" : "UPDATE " + database + ".PlayerPurse SET coins = coins+? WHERE upper(username) = ?");
                 PreparedStatement stmt2 = Database.prepareStatement("UPDATE " + database + ".ExternalGeneratedData SET coins = coins+? WHERE id = ?");
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
                e.printStackTrace();
            }

            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF(player);
            out.writeUTF("RoyaleEconomyMoneyTransfer"); // the channel could be whatever you want
            out.writeUTF("payMoney");
            out.writeUTF(player); // this data could be whatever you want
            out.writeDouble(amount);
            out.writeUTF(message);

            RedisHandler.sendData(out.toByteArray());
        }

        public boolean removeMoneyFromFile(String player, Double amount) {
            final double amountFloored = Math.floor(amount * 100) / 100;
            int onlineRemove = playerMoneyCache.removeBalance(player, amount);
            //plugin.getLogger().info("[DEBUG] online status of player " + player + " is " + onlineRemove);
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
                            PreparedStatement stmt1 = Database.prepareStatement(player.length() > 16 ? "UPDATE " + database + ".PlayerPurse SET coins = coins-? WHERE id = ?" : "UPDATE " + database + ".PlayerPurse SET coins = coins-? WHERE upper(username) = ?");
                            PreparedStatement stmt2 = Database.prepareStatement("UPDATE " + database + ".ExternalGeneratedData SET coins = coins-? WHERE id = ?");
                    ) {
                        RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runNextTick((task) -> Bukkit.getPluginManager().callEvent(new CoinsRemoveFromPurseEvent(player, amountFloored)));
                        stmt1.setDouble(1, amountFloored);
                        stmt1.setString(2, player.length() > 16 ? player : player.toUpperCase());
                        int updated = stmt1.executeUpdate();
                        if (updated == 0) {
                            stmt2.setDouble(1, amountFloored);
                            stmt2.setString(2, player);
                            stmt2.executeUpdate();
                        }

                        ByteArrayDataOutput out = ByteStreams.newDataOutput();
                        out.writeUTF(player);
                        out.writeUTF("RoyaleEconomyMoneyTransfer"); // the channel could be whatever you want
                        out.writeUTF("removeMoney");
                        out.writeUTF(player); // this data could be whatever you want
                        out.writeDouble(amount);

                        RedisHandler.sendData(out.toByteArray());
                        //plugin.getLogger().info("[DEBUG] Sent removeMoney to redis");

                        return true;

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
                    ResultSet r = Database.prepareStatement(player.length() > 16 ? "SELECT coins FROM " + database + ".PlayerPurse WHERE id = '" + player + "'" : "SELECT coins FROM " + database + ".PlayerPurse WHERE upper(username) = '" + player.toUpperCase() + "'").executeQuery();
                    ResultSet r2 = Database.prepareStatement("SELECT coins FROM " + database + ".ExternalGeneratedData WHERE id = '" + player + "'").executeQuery();

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

    public void addMoneyToExternal(String id, double amount) {
        try (Connection Database = HikariCPDataSource.getConnection();
             PreparedStatement stmt = Database.prepareStatement("UPDATE " + database + ".ExternalGeneratedData SET coins = coins+? WHERE id = ?");
        ) {
            stmt.setDouble(1, amount);
            stmt.setString(2, id);
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Double getMoneyFromExternal(String id) {
        Double toReturn = null;
        try (
                Connection Database = HikariCPDataSource.getConnection();
                ResultSet r = Database.prepareStatement("SELECT coins FROM " + database + ".ExternalGeneratedData WHERE id = '" + id + "'").executeQuery();

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
                ResultSet r = Database.prepareStatement("SELECT id FROM " + database + ".ExternalGeneratedData").executeQuery();

        ) {
            while (r.next())
                ids.add(r.getString(1));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ids;
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
                         "INSERT IGNORE INTO " + database + ".PlayerPurse VALUES (?, ? ,0, '')"
                 );

                 PreparedStatement u1 = Database.prepareStatement(
                         "UPDATE " + database + ".PlayerPurse SET coins = ? WHERE id = ?"
                 );

                 PreparedStatement u2 = Database.prepareStatement(
                         "INSERT IGNORE INTO " + database + ".PersonalBank VALUES (?, " + RoyaleEconomy.staticValues.defaultBankCoins + " ," + 0 + ", '')"
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

    public void importCMI() {
        RoyaleEconomy.plugin.getLogger().warning("CMI import is not included in this build.");
    }
    public void exportEconomy() {
        RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runAsync((task) -> {
            try (
                    Connection Database = HikariCPDataSource.getConnection();
                    ResultSet r = Database.prepareStatement("SELECT id,coins FROM " + database + ".PlayerPurse").executeQuery();
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
        });
    }

    public void setMoney(String player, Double amount) {
        economyFunctions.setMoney(player, amount);
    }

    public void setBankMoney(String player, double amount) {
        //RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runAsync((task) -> {
        try (
                Connection Database = HikariCPDataSource.getConnection();
                PreparedStatement stmt = Database.prepareStatement("UPDATE " + database + ".PersonalBank SET coins = ? WHERE id = ?");
        ) {
            stmt.setDouble(1, amount);
            stmt.setString(2, player);
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
        bankCache.bankModification(player, true);
        //});
    }

    public void addMoneyToFile(String player, Double amount) {
        economyFunctions.addMoneyToFile(player, amount);
    }

    public EconomyFunctions getEconomyFunctions() {
        return economyFunctions;
    }

    public void addBankMoneyToFile(String player, double amount) {
        amount=Math.floor(amount*100)/100;
        //RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runAsync((task) -> {
        try (
                Connection Database = HikariCPDataSource.getConnection();
                PreparedStatement stmt = Database.prepareStatement("UPDATE " + database + ".PersonalBank SET coins = coins +? WHERE id = ?");
        ) {
            stmt.setDouble(1, amount);
            stmt.setString(2, player);
            stmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
        bankCache.bankModification(player, true);
        //});
    }

    public boolean removeMoneyFromFile(String player, Double amount) {
        return economyFunctions.removeMoneyFromFile(player, amount);
    }

    public void removeBankMoneyToFile(String player, double amount) {
        amount=Math.floor(amount*100)/100;
        try (
                Connection Database = HikariCPDataSource.getConnection();
                PreparedStatement stmt = Database.prepareStatement("UPDATE " + database + ".PersonalBank SET coins = coins -? WHERE id = ?");
        ) {
            stmt.setDouble(1, amount);
            stmt.setString(2, player);
            stmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
        bankCache.bankModification(player, true);
    }

    public Double getMoneyFromFile(String player) {
        return economyFunctions.getMoneyFromFile(player);
    }

    public Double getBankMoneyFromFile(String player) {
        return bankCache.getBankMoneyCache(player);
        /*Double toReturn = null;

        try (
                Connection Database = HikariCPDataSource.getConnection();
                ResultSet r = Database.prepareStatement("SELECT coins FROM " + database + ".PersonalBank WHERE id = '" + player + "'").executeQuery();
        ) {
            if (r.next())
                toReturn = r.getDouble("coins");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return toReturn;*/
    }

    public Long getInterestDate() {
        Long toReturn = null;

        try (
                Connection Database = HikariCPDataSource.getConnection();
                ResultSet r = Database.prepareStatement("SELECT date FROM " + database + ".Interest WHERE id = 'personalInterest'").executeQuery()
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
                 PreparedStatement stmt = Database.prepareStatement("UPDATE " + database + ".Interest SET date = ? WHERE id = 'personalInterest'");) {

                stmt.setLong(1, date);
                stmt.executeUpdate();

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public String getUUIDfromName(String name) {
        String toReturn = null;

        try (Connection Database = HikariCPDataSource.getConnection();
             ResultSet r = Database.prepareStatement("SELECT id FROM " + database + ".PlayerPurse WHERE upper(username) = '" + name.toUpperCase() + "'").executeQuery();
        ) {
            if (r.next()) {
                toReturn = r.getString("id");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return toReturn;
    }

    public String getNameFromUUID(String id) {
        String toReturn = "UnknownUsername";

        try (Connection Database = HikariCPDataSource.getConnection();
             ResultSet r = Database.prepareStatement("SELECT username FROM " + database + ".PlayerPurse WHERE id = '" + id + "'").executeQuery();
        ) {
            if (r.next()) {
                toReturn = r.getString("username");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return toReturn;
    }

    private boolean usernameExists(Player player) {
        try (Connection Database = HikariCPDataSource.getConnection();
             PreparedStatement stmt1 = Database.prepareStatement("UPDATE " + database + ".SharedBank SET owner = ? WHERE id = ?");
             PreparedStatement stmt2 = Database.prepareStatement("UPDATE " + database + ".SharedBank SET members = ? WHERE id = ?");
             PreparedStatement stmt3 = Database.prepareStatement("UPDATE " + database + ".PlayerPurse SET id = ? WHERE upper(username) = ?");
             PreparedStatement stmt4 = Database.prepareStatement("UPDATE " + database + ".PersonalBank SET id = ? WHERE id = ?");
             ResultSet r = Database.prepareStatement("SELECT id FROM " + database + ".PlayerPurse WHERE upper(username) = '" + player.getName().toUpperCase() + "'").executeQuery()) {
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
                    PlayerMoneyCacheMySQL.onlinePlayers.remove(player);
                    bankCache.removeFromCache(player.getUniqueId().toString());
                }
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public void removeUserFromDatabase(String toRemove, boolean id) {
        RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runAsync((task) -> {
            try (Connection Database = HikariCPDataSource.getConnection();
                 PreparedStatement stmt1 = Database.prepareStatement(id ? "DELETE FROM " + database + ".PlayerPurse WHERE id = ?" : "DELETE FROM " + database + ".PlayerPurse WHERE username = ?");
                 PreparedStatement stmt2 = Database.prepareStatement("DELETE FROM " + database + ".PersonalBank WHERE id = ?");
                 PreparedStatement stmt3 = Database.prepareStatement("DELETE FROM " + database + ".SharedBank WHERE id = ?");
            ) {
                String uuid;
                if (id) {
                    uuid = toRemove;
                } else {
                    uuid = getUUIDfromName(toRemove);
                }
                stmt1.setString(1, toRemove);
                stmt1.executeUpdate();
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
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public boolean createAccount(String name) {
        try (Connection Database = HikariCPDataSource.getConnection();
             PreparedStatement stmt = Database.prepareStatement("INSERT INTO " + database + ".ExternalGeneratedData VALUES (?, 0)");) {
            stmt.setString(1, name);
            stmt.executeUpdate();
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
            return deleted != 0;
        } catch (Exception x) {
            return false;
        }
    }

    private void removeOldUsers() {
//        if (!RoyaleEconomy.plugin.getConfig().getBoolean("use-old-players-delete"))
//            return;
//        long currentDate = ZonedDateTime.now().toInstant().toEpochMilli();
//        long limit = (long) RoyaleEconomy.plugin.getConfig().getInt("inactivity-days") * 86400000L;
//        if (limit == 0)
//            return;
//        RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runAsync((task) -> {
//            try (Connection Database = HikariCPDataSource.getConnection();
//                 Statement backUp = Database.createStatement();
//                 ResultSet r = Database.prepareStatement("SELECT id,sharedBank FROM " + database + ".PlayerPurse").executeQuery();
//                 PreparedStatement stmt1 = Database.prepareStatement("DELETE FROM " + database + ".PlayerPurse WHERE id = ?");
//                 PreparedStatement stmt2 = Database.prepareStatement("DELETE FROM " + database + ".PersonalBank WHERE id = ?");
//                 PreparedStatement stmt3 = Database.prepareStatement("DELETE FROM " + database + ".SharedBank WHERE id = ?");
//                 PreparedStatement stmt = Database.prepareStatement("UPDATE " + database + ".SharedBank SET owner=?, members = ? WHERE id = ?");
//                 PreparedStatement stmtE = Database.prepareStatement("UPDATE " + database + ".SharedBank SET members = ? WHERE id = ?");
//            ) {
//                boolean found = false;
//                while (r.next()) {
//                    String uuid = r.getString(1);
//                    String bankID = r.getString(2);
//                    OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(uuid));
//                    if (player != null && player.hasPlayedBefore()) {
//                        if (currentDate - player.getLastPlayed() > limit) {
//                            found = true;
//                            plugin.getLogger().info("[USER REMOVAL] User " + uuid + " was removed from database | Data for Developer Debug: " + player.getLastPlayed() + " " + currentDate + " " + limit);
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
//                                    stmtE.executeBatch();
//                                }
//                            }
//                        }
//                    }
//                }
//                if (found) {
//                    backUp.addBatch("DROP TABLE IF EXISTS " + database + ".PlayerPurseBackUp");
//                    backUp.addBatch("CREATE TABLE " + database + ".PlayerPurseBackUp AS SELECT * FROM " + database + ".PlayerPurse");
//                    backUp.addBatch("DROP TABLE IF EXISTS " + database + ".PersonalBankBackUp");
//                    backUp.addBatch("CREATE TABLE " + database + ".PersonalBankBackUp AS SELECT * FROM " + database + ".PersonalBank");
//                    backUp.executeBatch();
//
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
                 ResultSet r = Database.prepareStatement("SELECT username FROM " + database + ".PlayerPurse WHERE id = '" + player.getUniqueId() + "'").executeQuery();
                 PreparedStatement stmt1 = Database.prepareStatement("UPDATE " + database + ".PlayerPurse SET username = ? WHERE id = ?");
                 PreparedStatement stmt2 = Database.prepareStatement("INSERT INTO " + database + ".PlayerPurse VALUES (?, ? ,?, '')");
                 PreparedStatement stmt3 = Database.prepareStatement("INSERT INTO " + database + ".PersonalBank VALUES (?, ? , 0, '')");
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
                e.printStackTrace();
            }
        });
    }

    public boolean triggerInterest() {
        try (Connection Database1 = HikariCPDataSource.getConnection();
             ResultSet testSR = Database1.prepareStatement("SELECT server FROM " + database + ".Interest").executeQuery();
        ) {

            if (!InterestCommand.isForced && testSR.next()) {
                if (!testSR.getString("server").equals(Bukkit.getIp() + ":" + Bukkit.getPort()))
                    return false;
            }

            RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runAsync((task) -> {
                String extra="";
                if(staticValues.onlineInterestOnly){
                    if(Bukkit.getOnlinePlayers().isEmpty())
                        extra=" WHERE 1=0";
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
                        ResultSet rs = Database.prepareStatement("SELECT id,coins,upgrade,transactionlog FROM " + database + ".PersonalBank"+extra).executeQuery();
                        PreparedStatement u = Database.prepareStatement(
                                "UPDATE " + database + ".PersonalBank SET coins = coins + ?, transactionlog = ? WHERE id = ?"
                        );
                        ResultSet rs2 = Database.prepareStatement("SELECT id,coins,upgrade,transactionlog FROM " + sharedBank.getTable()).executeQuery();
                        PreparedStatement u2 = Database.prepareStatement(
                                "UPDATE " + sharedBank.getTable() + " SET coins = coins + ?, transactionlog = ? WHERE id = ?"
                        );
                ) {

                    String toInsert = RoyaleEconomy.menusCfg.getString("menus.main-bank-menu.transaction-history.lore-structure").replace("%player-name%", RoyaleEconomy.menusCfg.getString("menus.main-bank-menu.transaction-history.bank-interest-name"));
                    while (rs.next()) {
                        double coinsAmount = interestCalculateFromUpgrade(rs.getDouble(2), rs.getInt(3), false);
                        String finalToInsert = Utils.chat("%nl%" + ZonedDateTime.now().toInstant().toEpochMilli() + "###" + toInsert
                                .replace("%symbol%", "&a+")
                                .replace("%transaction-coins%", RoyaleEconomy.messageHelper.numberFormat(coinsAmount))
                        );
                        String precedentLog = rs.getString(4);
                        if (precedentLog.split("%nl%").length == 11)
                            precedentLog = precedentLog.substring(4 + precedentLog.split("%nl%")[1].length());
                        u.setDouble(1, coinsAmount);
                        u.setString(2, precedentLog + finalToInsert);
                        u.setString(3, rs.getString(1));
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

                    u.executeBatch();
                    u2.executeBatch();
                } catch (Exception e) {
                    e.printStackTrace();
                }

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

        } catch (Exception e) {
            e.printStackTrace();
        }
        bankCache.clearCache(true);
        return true;
    }

    public Double interestCalculator(String p, boolean display, boolean shared) {
        int upgrade = 0;
        double coins = 0d;
        String sql;
        if (shared)
            sql = "SELECT coins,upgrade FROM " + sharedBank.getTable() + " WHERE id = '" + sharedBank.getSharedBankId(p) + "'";
        else
            sql = "SELECT coins,upgrade FROM " + database + ".PersonalBank WHERE id = '" + p + "'";
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

        return shared ? SharedinterestCalculateFromUpgrade(coins, upgrade, display) : interestCalculateFromUpgrade(coins, upgrade, display);
    }

    private double interestCalculateFromUpgrade(Double currentCoins, int bankUpgrade, boolean display) {
        double toReturn = 0d;
        double maximumCoins = bankUpgradesCfg.getDouble("bank-upgrades." + bankUpgrade + ".maximum-balance");

        ConfigurationSection f = bankUpgradesCfg.getConfigurationSection("bank-upgrades." + bankUpgrade + ".percentages");
        for (String key : f.getKeys(false)) {
            double localVariable = 0d;
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
        return bankCache.getBankUpgradeCache(player);
        /*int upgrade = 0;
        try (
                Connection Database = HikariCPDataSource.getConnection();
                ResultSet r = Database.prepareStatement("SELECT upgrade FROM " + database + ".PersonalBank WHERE id = '" + player + "'").executeQuery();
        ) {
            if (r.next()) {
                upgrade = r.getInt("upgrade");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return upgrade;*/
    }

    public void setBankUpgrade(String p, int bankUpgrade) {
        //RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runAsync((task) -> {
        try (Connection Database = HikariCPDataSource.getConnection();
             PreparedStatement stmt = Database.prepareStatement("UPDATE " + database + ".PersonalBank SET upgrade = ? WHERE id = ?");) {
            stmt.setInt(1, bankUpgrade);
            stmt.setString(2, p);
            stmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
        bankCache.bankModification(p, true);
        //});
    }

    public WrappedTask checkInterest() {
        return RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runTimer(() -> {
                    if (ZonedDateTime.now().toInstant().toEpochMilli() >= InterestCommand.interestDate) {
                        Long timp = ZonedDateTime.now().toInstant().toEpochMilli();
                        timp += RoyaleEconomy.plugin.getConfig().getInt("interest-cooldown") * 3600000L;
                        updateInterestDate(timp);
                        InterestCommand.interestDate = timp;
                        if (!triggerInterest())
                            InterestCommand.getInterestChecker().cancel();
                    }
                }, 400L, 400L
        );
    }

    public ArrayList<String> getTransactionLogFromFile(String player) {
        ArrayList<String> toReturn = new ArrayList<>();
        try (Connection Database = HikariCPDataSource.getConnection();
             ResultSet r = Database.prepareStatement("SELECT transactionlog FROM " + database + ".PersonalBank WHERE id = '" + player + "'").executeQuery();
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
             ResultSet r = Database.prepareStatement("SELECT transactionlog FROM " + database + ".PersonalBank WHERE id = '" + uuid + "'").executeQuery();
             PreparedStatement stmt = Database.prepareStatement("UPDATE " + database + ".PersonalBank SET transactionlog = ? WHERE id = ?");
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
                ResultSet r = Database.prepareStatement("SELECT transactionlog FROM "+database+".SharedBank WHERE id = '" + bankID + "'").executeQuery();
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
                    PreparedStatement stmt = Database.prepareStatement("UPDATE "+database+".SharedBank SET transactionlog = ? WHERE id = ?");
                    ResultSet r = Database.prepareStatement("SELECT transactionlog FROM "+database+".SharedBank WHERE id = '" + bankID + "'").executeQuery();
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
                //if(e.getMessage()!=null && e.getMessage().startsWith("[SQLITE_BUSY]")) {
                //    RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runLaterAsync(() -> addSharedTransactionLog(bankID, byWho, symbol, amount), 7);
                //}
                //else
                e.printStackTrace();
            }
        //});
    }

    public void setSharedBankMoney(String bankID, Double amount) {
        //RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runAsync((task) -> {
            try (
                    Connection Database = HikariCPDataSource.getConnection();
                    PreparedStatement stmt = Database.prepareStatement("UPDATE "+database+".SharedBank SET coins = ? WHERE id = ?");
            ) {
                stmt.setDouble(1, amount);
                stmt.setString(2, bankID);
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
                    PreparedStatement stmt = Database.prepareStatement("UPDATE "+database+".SharedBank SET coins = coins +? WHERE id = ?");
            ) {
                stmt.setDouble(1, amount);
                stmt.setString(2, bankID);
                stmt.executeUpdate();
            } catch (Exception e) {
                e.printStackTrace();
            }
        //});
    }

    public void removeSharedBankMoneyToFile(String bankID, Double amount) {
        try (
                Connection Database = HikariCPDataSource.getConnection();
                PreparedStatement stmt = Database.prepareStatement("UPDATE "+database+".SharedBank SET coins = coins -? WHERE id = ?");
        ) {
            stmt.setDouble(1, amount);
            stmt.setString(2, bankID);
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Double getSharedBankMoneyFromFile(String bankID) {
        Double toReturn = null;

        try (Connection Database = HikariCPDataSource.getConnection();
             ResultSet r = Database.prepareStatement("SELECT coins FROM "+database+".SharedBank WHERE id = '" + bankID + "'").executeQuery();
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
             ResultSet r = Database.prepareStatement("SELECT sharedBank FROM "+database+".PlayerPurse WHERE id = '" + p.getUniqueId() + "'").executeQuery();
             PreparedStatement stmt1 = Database.prepareStatement("INSERT INTO "+database+".SharedBank VALUES (?, ?, '' ,? ,0, '')");
             PreparedStatement stmt2 = Database.prepareStatement("UPDATE "+database+".PlayerPurse SET sharedBank = ? WHERE id = ?");
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
                ResultSet r = Database.prepareStatement("SELECT sharedBank FROM "+database+".PlayerPurse WHERE id = '" + p.getUniqueId() + "'").executeQuery();
                PreparedStatement stmt1 = Database.prepareStatement("DELETE FROM "+database+".SharedBank WHERE id=?");
                PreparedStatement stmt2 = Database.prepareStatement("UPDATE "+database+".PlayerPurse SET sharedBank = '' WHERE sharedBank = ?");
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
            e.printStackTrace();
        }
        return success;
    }

    public void deleteSharedBank(String bank) {
        //RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runAsync((task) -> {
            try (Connection Database = HikariCPDataSource.getConnection();
                 PreparedStatement stmt1 = Database.prepareStatement("DELETE FROM "+database+".SharedBank WHERE id=?");
                 PreparedStatement stmt2 = Database.prepareStatement("UPDATE "+database+".PlayerPurse SET sharedBank = '' WHERE sharedBank = ?");
            ) {
                if (!bank.equals("")) {
                    stmt1.setString(1, bank);
                    stmt1.executeUpdate();

                    stmt2.setString(1, bank);
                    stmt2.executeUpdate();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        //});
    }

    public String getSharedBankId(String player) {
        String toReturn = null;

        try (
                Connection Database = HikariCPDataSource.getConnection();
                ResultSet r = Database.prepareStatement(player.length() > 16 ? "SELECT sharedBank FROM "+database+".PlayerPurse WHERE id = '" + player + "'" : "SELECT sharedBank FROM "+database+".PlayerPurse WHERE upper(username) = '" + player.toUpperCase() + "'").executeQuery()
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
                ResultSet r = Database.prepareStatement("SELECT owner FROM "+database+".SharedBank WHERE owner = '" + p.getUniqueId() + "'").executeQuery();
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
        String bankID = getSharedBankId(oldOwner);
        ArrayList<String> members = getMembersSharedBank(bankID, false);
        if (members.contains(newOwner)) {
            try (Connection Database = HikariCPDataSource.getConnection();
                 PreparedStatement stmt = Database.prepareStatement("UPDATE "+database+".SharedBank SET owner=?, members = ? WHERE id = ?");) {
                members.remove(newOwner);
                members.add(oldOwner);
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
                e.printStackTrace();
            }
        }
        return success;
    }

    public boolean transferOwnershipSharedBank(String newOwner) {
        boolean success = false;
        String bankID = getSharedBankId(newOwner);
        ArrayList<String> members = getMembersSharedBank(bankID, true);
        try (Connection Database = HikariCPDataSource.getConnection();
             PreparedStatement stmt = Database.prepareStatement("UPDATE "+database+".SharedBank SET owner=?, members = ? WHERE id = ?");) {
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
            e.printStackTrace();
        }
        return success;
    }

    public ArrayList<String> getMembersSharedBank(String bankID, boolean owner) {
        ArrayList<String> members = new ArrayList<>();
        try (
                Connection Database = HikariCPDataSource.getConnection();
                ResultSet r = Database.prepareStatement(owner ? "SELECT owner,members FROM "+database+".SharedBank WHERE id = '" + bankID + "'" : "SELECT members FROM "+database+".SharedBank WHERE id = '" + bankID + "'").executeQuery();
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
                ResultSet r = Database.prepareStatement("SELECT members,upgrade FROM "+database+".SharedBank WHERE id = '" + bankID + "'").executeQuery();
                PreparedStatement stmt1 = Database.prepareStatement("UPDATE "+database+".SharedBank SET members = ? WHERE id = ?");
                PreparedStatement stmt2 = Database.prepareStatement("UPDATE "+database+".PlayerPurse SET sharedBank = ? WHERE id = ?");
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
                ResultSet r = Database.prepareStatement("SELECT members,upgrade FROM "+database+".SharedBank WHERE id = '" + bankID + "'").executeQuery();
                PreparedStatement stmt1 = Database.prepareStatement("UPDATE "+database+".SharedBank SET members = ? WHERE id = ?");
                PreparedStatement stmt2 = Database.prepareStatement("UPDATE "+database+".PlayerPurse SET sharedBank = ? WHERE id = ?");
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
        try (
                Connection Database = HikariCPDataSource.getConnection();
                PreparedStatement stmt1 = Database.prepareStatement("UPDATE "+database+".SharedBank SET members = ? WHERE id = ?");
                PreparedStatement stmt2 = Database.prepareStatement("UPDATE "+database+".PlayerPurse SET sharedBank = '' WHERE id = ?");
        ) {
            members = getMembersSharedBank(bankID, false);
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
            e.printStackTrace();
        }
        return removed;
    }

    public int getSharedBankUpgrade(String bankID) {
        int upgrade = 0;
        try (
                Connection Database = HikariCPDataSource.getConnection();
                ResultSet r = Database.prepareStatement("SELECT upgrade FROM "+database+".SharedBank WHERE id = '" + bankID + "'").executeQuery();
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
                    PreparedStatement stmt = Database.prepareStatement("UPDATE "+database+".SharedBank SET upgrade = ? WHERE id = ?");
            ) {
                stmt.setInt(1, bankUpgrade);
                stmt.setString(2, bankID);
                stmt.executeUpdate();

            } catch (Exception e) {
                e.printStackTrace();
            }
        //});
    }*/
}
