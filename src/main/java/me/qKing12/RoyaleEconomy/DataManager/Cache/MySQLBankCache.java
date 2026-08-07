package me.qKing12.RoyaleEconomy.DataManager.Cache;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.qKing12.RoyaleEconomy.DataManager.Cache.redis.RedisHandler;
import me.qKing12.RoyaleEconomy.DataManager.DataManagerMySQL;
import me.qKing12.RoyaleEconomy.DataManager.HikariCPDataSource;
import me.qKing12.RoyaleEconomy.Menus.MainBankMenu;
import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static me.qKing12.RoyaleEconomy.RoyaleEconomy.hooked;
import static me.qKing12.RoyaleEconomy.RoyaleEconomy.plugin;

public class MySQLBankCache implements Listener {

    public MySQLBankCache(){
        Bukkit.getPluginManager().registerEvents(this, RoyaleEconomy.plugin);
    }

    //cache pt bank id la shared
    //cache pt bani in bank
    //cache pt bani in shared bank
    private final ConcurrentHashMap<String, String> sharedBankIds=new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, BankData> playerBanks=new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, BankData> sharedBanks=new ConcurrentHashMap<>();

    public String getSharedBankId(String player){
        if(RoyaleEconomy.hooked!=null)
            if(player.length()>16)
                return RoyaleEconomy.hooked.getSharedBankId(player);
            else{
                return RoyaleEconomy.hooked.getSharedBankId(RoyaleEconomy.dataManager.getUUIDfromName(player));
            }

        String id=sharedBankIds.getOrDefault(player, null);
        if(id==null)
            id=getSharedBankIdIntern(player);

        return id;
    }

    public void removeFromCache(String playerUUID){
        playerBanks.remove(playerUUID);
    }

    public void bankModification(String playerUUID, boolean sending){
        playerBanks.remove(playerUUID);
        if(sending && RedisHandler.useRedis){
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF(playerUUID);
            out.writeUTF("RoyaleEconomyBankTransfer"); // the channel could be whatever you want
            out.writeUTF("bankModification");
            out.writeUTF(playerUUID); // this data could be whatever you want

            RedisHandler.sendData(out.toByteArray());
        }
        else if(BungeeMessagingCacheMySQL.bungeecord && sending){
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("RoyaleEconomyBankTransfer"); // the channel could be whatever you want
            out.writeUTF("bankModification");
            out.writeUTF(playerUUID); // this data could be whatever you want

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
    }

    public void deleteSharedBankCache(String bankId, boolean sending){
        for(Map.Entry<String, String> entry : sharedBankIds.entrySet())
            if(entry.getValue().equals(bankId))
                sharedBankIds.remove(entry.getKey());
        sharedBanks.remove(bankId);
        if(sending && RedisHandler.useRedis){
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("bankCreation.");
            out.writeUTF("RoyaleEconomyBankTransfer"); // the channel could be whatever you want
            out.writeUTF("bankCreation");
            out.writeUTF(bankId); // this data could be whatever you want

            RedisHandler.sendData(out.toByteArray());
        }
        else if(BungeeMessagingCacheMySQL.bungeecord && sending){
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("RoyaleEconomyBankTransfer"); // the channel could be whatever you want
            out.writeUTF("bankCreation");
            out.writeUTF(bankId); // this data could be whatever you want

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
    }

    public void sharedBankModification(String bankId, boolean sending){
        sharedBanks.remove(bankId);
        if(sending && RedisHandler.useRedis){
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("sharedBankModification.");
            out.writeUTF("RoyaleEconomyBankTransfer"); // the channel could be whatever you want
            out.writeUTF("sharedBankModification");
            out.writeUTF(bankId); // this data could be whatever you want

            RedisHandler.sendData(out.toByteArray());
        }
        else if(BungeeMessagingCacheMySQL.bungeecord && sending){
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("RoyaleEconomyBankTransfer"); // the channel could be whatever you want
            out.writeUTF("sharedBankModification");
            out.writeUTF(bankId); // this data could be whatever you want

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
    }

    public void sharedBankMemberModification(String player, boolean sending){
        sharedBankIds.remove(player);
        if(sending && RedisHandler.useRedis){
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF(player);
            out.writeUTF("RoyaleEconomyBankTransfer"); // the channel could be whatever you want
            out.writeUTF("sharedBankMemberModification");
            out.writeUTF(player); // this data could be whatever you want

            RedisHandler.sendData(out.toByteArray());
        }
        else if(BungeeMessagingCacheMySQL.bungeecord && sending){
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("RoyaleEconomyBankTransfer"); // the channel could be whatever you want
            out.writeUTF("sharedBankMemberModification");
            out.writeUTF(player); // this data could be whatever you want

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
    }

    private String getSharedBankIdIntern(String player) {
        String toReturn = null;

        try (
                Connection Database = HikariCPDataSource.getConnection();
                ResultSet r = Database.prepareStatement(player.length()>16?"SELECT sharedBank FROM "+DataManagerMySQL.database+".PlayerPurse WHERE id = '" + player + "'":"SELECT sharedBank FROM "+DataManagerMySQL.database+".PlayerPurse WHERE upper(username) = '" + player.toUpperCase() + "'").executeQuery()
        ) {
            if (r.next())
                toReturn = r.getString("sharedBank");
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(toReturn!=null)
            sharedBankIds.put(player, toReturn);
        return toReturn;
    }

    public class BankData{
        /*double previousAmount;
        double newAmount;

        int oldBankId;
        int newBankId;*/

        double bankAmount;
        int bankUpgrade;

        public BankData(double bankAmount, int bankId){
            this.bankAmount=bankAmount;
            this.bankUpgrade=bankId;
        }
    }

    public void clearCache(boolean isSending){
        sharedBanks.clear();
        playerBanks.clear();
        sharedBankIds.clear();

        if(isSending && RedisHandler.useRedis){
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("bankInterest.");
            out.writeUTF("RoyaleEconomyBankInterest");

            RedisHandler.sendData(out.toByteArray());
        }
        else if(isSending && BungeeMessagingCacheMySQL.bungeecord) {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("RoyaleEconomyBankInterest"); // the channel could be whatever you want

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
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e){
        String uuid = e.getPlayer().getUniqueId().toString();
        String bankId=sharedBankIds.getOrDefault(uuid, null);
        sharedBankIds.remove(uuid);

        if(bankId!=null && !sharedBankIds.containsValue(bankId)){
            sharedBanks.remove(bankId);
        }

        playerBanks.remove(uuid);
    }

    private boolean hasPassedCooldown(String id) {
        if (!BungeeMessagingCacheMySQL.bungeecord && !RedisHandler.useRedis)
            return true;

        Player player = Bukkit.getPlayer(UUID.fromString(id));
        if (player == null || !player.isOnline())
            return true;

        PlayerMoneyCacheMySQL.RECPlayer recPlayer = PlayerMoneyCacheMySQL.onlinePlayers.getOrDefault(player, null);
        if (recPlayer == null)
            return false;

        return recPlayer.previousAmount != -1;
    }

    public Double getBankMoneyCache(String id){
        if (!hasPassedCooldown(id))
            return 0d;

        BankData data=playerBanks.getOrDefault(id, null);
        if(data==null){
            Double number = getBankMoneyFromFile(id);
            if(number!=null){
                playerBanks.put(id, new BankData(number, getBankUpgrade(id)));
            }
            return number;
        }

        return data.bankAmount;
    }

    public int getBankUpgradeCache(String id){
        if (!hasPassedCooldown(id))
            return 0;

        BankData data=playerBanks.getOrDefault(id, null);
        if(data==null){
            Double number = getBankMoneyFromFile(id);
            if(number!=null){
                int upgrade=getBankUpgrade(id);
                playerBanks.put(id, new BankData(number, upgrade));
                return upgrade;
            }
            return getBankUpgrade(id);
        }

        return data.bankUpgrade;
    }

    public Double getSharedBankMoneyCache(String id){
        if (!hasPassedCooldown(id))
            return 0d;

        BankData data=sharedBanks.getOrDefault(id, null);
        if(data==null){
            Double number = getSharedBankMoneyFromFile(id);
            if(number!=null){
                playerBanks.put(id, new BankData(number, getSharedBankUpgrade(id)));
            }
            return number;
        }

        return data.bankAmount;
    }

    public int getSharedBankUpgradeCache(String id){
        if (!hasPassedCooldown(id))
            return 0;

        BankData data=sharedBanks.getOrDefault(id, null);
        if(data==null){
            Double number = getSharedBankMoneyFromFile(id);
            if(number!=null){
                int upgrade=getSharedBankUpgrade(id);
                sharedBanks.put(id, new BankData(number, upgrade));
                return upgrade;
            }
            return getSharedBankUpgrade(id);
        }

        return data.bankUpgrade;
    }

    //nu trebuie on join, folosim get cache sau get din database daca nu e in cache
    //stergem on quit

    //de adaugat cache pt shared id
    //dupa ce se fac toate cacheurile se pun in mysql classes (sharedbankmanager si datamanagermysql)

    public Double getBankMoneyFromFile(String player) {
        Double toReturn = null;

        try (
                Connection Database = HikariCPDataSource.getConnection();
                ResultSet r = Database.prepareStatement("SELECT coins FROM " + DataManagerMySQL.database + ".PersonalBank WHERE id = '" + player + "'").executeQuery();
        ) {
            if (r.next())
                toReturn = r.getDouble("coins");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return toReturn;
    }

    public int getBankUpgrade(String player) {
        int upgrade = 0;
        try (
                Connection Database = HikariCPDataSource.getConnection();
                ResultSet r = Database.prepareStatement("SELECT upgrade FROM " + DataManagerMySQL.database + ".PersonalBank WHERE id = '" + player + "'").executeQuery();
        ) {
            if (r.next()) {
                upgrade = r.getInt("upgrade");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return upgrade;
    }

    public Double getSharedBankMoneyFromFile(String bankID) {
        Double toReturn = null;

        try (Connection Database = HikariCPDataSource.getConnection();
             ResultSet r=Database.prepareStatement("SELECT coins FROM "+RoyaleEconomy.dataManager.getSharedBankManager().getTable()+"  WHERE id = '" + bankID + "'").executeQuery();
        ) {
            if (r.next())
                toReturn = r.getDouble("coins");
            else if(hooked!=null){
                RoyaleEconomy.dataManager.getSharedBankManager().createSharedBank(bankID);
                return (double) RoyaleEconomy.staticValues.defaultBankCoins;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return toReturn;
    }

    public int getSharedBankUpgrade(String bankID) {
        int upgrade = 0;
        try (
                Connection Database = HikariCPDataSource.getConnection();
                ResultSet r = Database.prepareStatement("SELECT upgrade FROM "+RoyaleEconomy.dataManager.getSharedBankManager().getTable()+"  WHERE id = '" + bankID + "'").executeQuery();
        ) {
            if (r.next()) {
                upgrade = r.getInt("upgrade");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return upgrade;
    }
}
