package me.qKing12.RoyaleEconomy.DataManager.Cache;

import me.qKing12.RoyaleEconomy.DataManager.Cache.redis.RedisHandler;
import me.qKing12.RoyaleEconomy.DataManager.DataManagerMySQL;
import me.qKing12.RoyaleEconomy.DataManager.HikariCPDataSource;
import me.qKing12.RoyaleEconomy.DataManager.StaticValues;
import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerMoneyCacheMySQL implements Listener, PlayerMoneyCache {
    public static ConcurrentHashMap<Player, RECPlayer> onlinePlayers=new ConcurrentHashMap<>();
    private static String database;
    private final int syncCooldown;

    public void reloadBalances(){
        for(RECPlayer player : onlinePlayers.values()){
            player.previousAmount=player.newAmount=getMoneyFromFile(player.player.getUniqueId().toString());
        }
    }

    public void finalSave(){
        try(
                Connection Database = HikariCPDataSource.getConnection();
                PreparedStatement updatePurse = Database.prepareStatement("UPDATE "+database+".PlayerPurse SET coins = ? WHERE id = ?");
        ){
            Iterator<Map.Entry<Player, RECPlayer>> iterator = onlinePlayers.entrySet().iterator();
            while(iterator.hasNext()) {
                Map.Entry<Player, RECPlayer> player=iterator.next();
                    updatePurse.setDouble(1, player.getValue().getNewAmount());
                    updatePurse.setString(2, player.getKey().getUniqueId().toString());
                    updatePurse.addBatch();
            }
            updatePurse.executeBatch();
        }catch (Exception x){
            x.printStackTrace();
        }
    }

    public PlayerMoneyCacheMySQL(){
        database= DataManagerMySQL.database;
        syncCooldown= RoyaleEconomy.plugin.getConfig().getInt("mysql.sync-cooldown-seconds", 2);

        Bukkit.getPluginManager().registerEvents(this, RoyaleEconomy.plugin);
        RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runTimerAsync(() -> {
            HashMap<RECPlayer, Double> values=new HashMap<>();
            try {
                Iterator<Map.Entry<Player, RECPlayer>> iterator = onlinePlayers.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<Player, RECPlayer> player = iterator.next();
                    Double value = player.getValue().getBalance();
                    if (value != null) {
                        values.put(player.getValue(), value);
                    }
                    if (!player.getKey().isOnline()) {
                        iterator.remove();
                    }
                }
            }catch (Exception x){
                x.printStackTrace();
                for(RECPlayer player : values.keySet())
                    player.previousAmount--;
                return;
            }
            if(!values.isEmpty())
                runLargeUpdate(values);
                //RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runNextTick((task) -> runLargeUpdate(values));
        }, 100, 30);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e){
        if(onlinePlayers.containsKey(e.getPlayer()))
            return;
        onlinePlayers.put(e.getPlayer(), new RECPlayer(e.getPlayer()));
    }

    public class RECPlayer{
        Player player;
        double previousAmount;
        private double newAmount;

        public synchronized void addAmount(double amount){
            newAmount+=amount;
        }

        public synchronized void removeAmount(double amount){
            if (newAmount < amount)
                newAmount = 0;
            else
                newAmount-=amount;
        }

        public synchronized void setAmount(double amount){
            newAmount=amount;
        }

        public synchronized double getNewAmount(){
            return newAmount;
        }

        public Long syncMilliseconds;

        public RECPlayer(Player player){
            this.player=player;
            if(BungeeMessagingCacheMySQL.bungeecord || RedisHandler.useRedis) {
                previousAmount = -1;
                setAmount(0);
                RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runLaterAsync(() -> {
                    previousAmount = 0;
                    addAmount(getMoneyFromFile(player.getUniqueId().toString()));
                    syncMilliseconds = System.currentTimeMillis();
                }, 20L*syncCooldown);
            }
            else{
                previousAmount=newAmount=getMoneyFromFile(player.getUniqueId().toString());
            }
        }

        public Double getBalance(){
            if(previousAmount==newAmount || previousAmount==-1)
                return null;
            else {
                previousAmount=newAmount;
                return newAmount;
            }
        }
    }

    public RECPlayer getRecPlayer(Player player){
        RECPlayer toReturn = onlinePlayers.getOrDefault(player, null);
        if(toReturn==null && player.isOnline()){
            toReturn=new RECPlayer(player);
            onlinePlayers.put(player, toReturn);
            RoyaleEconomy.plugin.getLogger().info("I couldn't find the cache for player "+player.getName()+" so I generated it now.");
        }
        return toReturn;
    }

    public double getBalance(String player){
        Player p=null;
        try {
            p = player.length() > 16 ? Bukkit.getPlayer(UUID.fromString(player)) : Bukkit.getPlayerExact(player);
        }catch(Exception x){

        }
        if(p==null)
            return -1;
        return getRecPlayer(player.length()>16? Bukkit.getPlayer(UUID.fromString(player)):Bukkit.getPlayerExact(player)).getNewAmount();
    }

    public double getBalance(Player player){
        try {
            return onlinePlayers.get(player).getNewAmount();
        }catch(Exception x){
            return -1d;
        }
    }

    public boolean addBalance(String player, double coins){
        Player p=null;
        try {
            p = player.length() > 16 ? Bukkit.getPlayer(UUID.fromString(player)) : Bukkit.getPlayerExact(player);
        }catch(Exception x){

        }
        if(p==null)
            return false;
        RECPlayer recPlayer=getRecPlayer(p);
        recPlayer.addAmount(coins);
        return true;
    }

    public int removeBalance(String player, double coins){
        Player p=null;
        try {
            p = player.length() > 16 ? Bukkit.getPlayer(UUID.fromString(player)) : Bukkit.getPlayerExact(player);
        }catch(Exception x){

        }
        if(p==null)
            return -1;
        RECPlayer recPlayer=getRecPlayer(p);
        synchronized (recPlayer) {
            if (recPlayer.newAmount < coins)
                return 0;

            recPlayer.newAmount -= coins;
        }
        return 1;
    }

    public boolean setBalance(String player, double coins){
        Player p=null;
        try {
            p = player.length() > 16 ? Bukkit.getPlayer(UUID.fromString(player)) : Bukkit.getPlayerExact(player);
        }catch(Exception x){

        }
        if(p==null)
            return false;
        RECPlayer recPlayer=getRecPlayer(p);
        recPlayer.setAmount(coins);
        return true;
    }

    public double getMoneyFromFile(String player) {
        double toReturn = RoyaleEconomy.staticValues.defaultCoins;

        try (
                Connection Database = HikariCPDataSource.getConnection();
                ResultSet r = Database.prepareStatement("SELECT coins FROM "+database+".PlayerPurse WHERE id = '" + player + "'").executeQuery();

        ) {
            if (r.next())
                toReturn = r.getDouble("coins");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return BigDecimal.valueOf(toReturn).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    public void runLargeUpdate(HashMap<RECPlayer, Double> values){
        try(
                Connection Database = HikariCPDataSource.getConnection();
                PreparedStatement updatePurse = Database.prepareStatement("UPDATE "+database+".PlayerPurse SET coins = ? WHERE id = ?");
                ){
                    for(Map.Entry<RECPlayer, Double> player : values.entrySet()) {
                        updatePurse.setDouble(1, player.getValue());
                        updatePurse.setString(2, player.getKey().player.getUniqueId().toString());
                        updatePurse.addBatch();
                    }
                    updatePurse.executeBatch();
        }catch (Exception x){
            x.printStackTrace();
            for(RECPlayer player : values.keySet())
                player.previousAmount--;
        }
    }
}
