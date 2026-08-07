package me.qKing12.RoyaleEconomy.DataManager.Cache;

import me.qKing12.RoyaleEconomy.DataManager.HikariCPDataSource;
import me.qKing12.RoyaleEconomy.DataManager.StaticValues;
import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static me.qKing12.RoyaleEconomy.RoyaleEconomy.bankUpgradesCfg;

public class PlayerMoneyCacheSQL implements Listener, PlayerMoneyCache {

    public static ConcurrentHashMap<Player, RECPlayer> onlinePlayers=new ConcurrentHashMap<>();

    public void giveInterest(){
        for(RECPlayer player : onlinePlayers.values()){
            try{
                player.bankAmount+=interestCalculateFromUpgrade(player.bankAmount, player.bankUpgrade);
                player.updateBank=true;
            }catch(Exception x){
                x.printStackTrace();
            }
        }
    }

    private Double interestCalculateFromUpgrade(Double currentCoins, int bankUpgrade) {
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

        if (currentCoins + toReturn >= maximumCoins)
            toReturn = maximumCoins - currentCoins;

        return Math.floor(toReturn);
    }

    public void reloadBalances(){
        for(RECPlayer player : onlinePlayers.values()){
            player.previousAmount=player.newAmount=getMoneyFromFile(player.player.getUniqueId().toString());
        }
    }

    public void finalSave(){
        try(
                Connection Database = HikariCPDataSource.getConnection();
                PreparedStatement updatePurse = Database.prepareStatement("UPDATE PlayerPurse SET coins = ? WHERE id = ?");
        ){
            Iterator<Map.Entry<Player, RECPlayer>> iterator = onlinePlayers.entrySet().iterator();
            while(iterator.hasNext()) {
                Map.Entry<Player, RECPlayer> player=iterator.next();
                    updatePurse.setDouble(1, player.getValue().newAmount);
                    updatePurse.setString(2, player.getKey().getUniqueId().toString());
                    updatePurse.addBatch();
            }
            updatePurse.executeBatch();
        }catch (Exception x){
            x.printStackTrace();
        }
    }

    public PlayerMoneyCacheSQL(){
        Bukkit.getPluginManager().registerEvents(this, RoyaleEconomy.plugin);
        if(RoyaleEconomy.noEconomy){
            RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runTimerAsync(() -> {
                ArrayList<RECPlayer> bankUpdates = new ArrayList<>();
                try {
                    Iterator<Map.Entry<Player, RECPlayer>> iterator = onlinePlayers.entrySet().iterator();
                    while (iterator.hasNext()) {
                        Map.Entry<Player, RECPlayer> player = iterator.next();
                        if (player.getValue().updateBank)
                            bankUpdates.add(player.getValue());
                        if (!player.getKey().isOnline()) {
                            iterator.remove();
                        }
                    }
                } catch (Exception x) {
                    x.printStackTrace();
                    return;
                }
                if (!bankUpdates.isEmpty())
                    runLargeUpdateBankOnly(bankUpdates);

            }, 100, 30);
        }
        else {
            RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runTimerAsync(() -> {
                HashMap<RECPlayer, Double> values = new HashMap<>();
                ArrayList<RECPlayer> bankUpdates = new ArrayList<>();
                try {
                    Iterator<Map.Entry<Player, RECPlayer>> iterator = onlinePlayers.entrySet().iterator();
                    while (iterator.hasNext()) {
                        Map.Entry<Player, RECPlayer> player = iterator.next();
                        Double value = player.getValue().getBalance();
                        if (value != null) {
                            values.put(player.getValue(), value);
                        }
                        if (player.getValue().updateBank)
                            bankUpdates.add(player.getValue());
                        if (!player.getKey().isOnline()) {
                            iterator.remove();
                        }
                    }
                } catch (Exception x) {
                    x.printStackTrace();
                    for (RECPlayer player : values.keySet())
                        player.previousAmount--;
                    return;
                }
                if (!values.isEmpty() || !bankUpdates.isEmpty())
                    runLargeUpdate(values, bankUpdates);
                //RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runNextTick((task) -> runLargeUpdate(values));
            }, 100, 30);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e){
        if(onlinePlayers.containsKey(e.getPlayer()))
            return;
        onlinePlayers.put(e.getPlayer(), new RECPlayer(e.getPlayer()));
    }

    /*@EventHandler
    public void onQuit(PlayerQuitEvent e){
        RECPlayer rec=getRecPlayer(e.getPlayer());
        RoyaleEconomy.plugin.getLogger().info("[DEBUG] "+e.getPlayer().getName()+" has "+rec.newAmount+" coins with previous amount of "+rec.previousAmount+".");
    }*/

    public class RECPlayer{
        Player player;
        double previousAmount;
        public double newAmount;

        double bankAmount;
        int bankUpgrade;
        boolean updateBank=false;

        public RECPlayer(Player player){
            this.player=player;
            if(!RoyaleEconomy.noEconomy)
                previousAmount=newAmount=getMoneyFromFile(player.getUniqueId().toString());
            bankAmount=getBankMoneyFromFile(player.getUniqueId().toString());
            bankUpgrade=getBankUpgrade(player.getUniqueId().toString());
            //RoyaleEconomy.plugin.getLogger().info("[DEBUG] "+player.getName()+" has "+newAmount+" coins.");
        }

        public Double getBalance(){
            if(previousAmount==newAmount)
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
        return getRecPlayer(p).newAmount;
    }

    public double getBalanceBank(String player){
        Player p=null;
        try {
            p = player.length() > 16 ? Bukkit.getPlayer(UUID.fromString(player)) : Bukkit.getPlayerExact(player);
        }catch(Exception x){

        }
        if(p==null)
            return -1;
        return getRecPlayer(p).bankAmount;
    }

    public int getBankUpgradeCache(String player){
        Player p=null;
        try {
            p = player.length() > 16 ? Bukkit.getPlayer(UUID.fromString(player)) : Bukkit.getPlayerExact(player);
        }catch(Exception x){

        }
        if(p==null)
            return -1;
        return getRecPlayer(p).bankUpgrade;
    }

    public void setBankUpgradeCache(String player, int bankUpgrade){
        Player p=null;
        try {
            p = player.length() > 16 ? Bukkit.getPlayer(UUID.fromString(player)) : Bukkit.getPlayerExact(player);
        }catch(Exception x){

        }
        if(p!=null)
            getRecPlayer(p).bankUpgrade=bankUpgrade;
    }

    public void setBankUpgradeCache(Player player, int bankUpgrade){
        getRecPlayer(player).bankUpgrade=bankUpgrade;
    }

    public double getBalance(Player player){
        try {
            return onlinePlayers.get(player).newAmount;
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
        recPlayer.newAmount+=coins;
        return true;
    }

    public boolean addBalanceBank(String player, double coins){
        Player p=null;
        try {
            p = player.length() > 16 ? Bukkit.getPlayer(UUID.fromString(player)) : Bukkit.getPlayerExact(player);
        }catch(Exception x){

        }
        if(p==null)
            return false;
        RECPlayer recPlayer=getRecPlayer(p);
        recPlayer.bankAmount+=coins;
        recPlayer.updateBank=true;
        return true;
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
        recPlayer.newAmount=coins;
        return true;
    }

    public boolean setBalanceBank(String player, double coins){
        Player p=null;
        try {
            p = player.length() > 16 ? Bukkit.getPlayer(UUID.fromString(player)) : Bukkit.getPlayerExact(player);
        }catch(Exception x){

        }
        if(p==null)
            return false;
        RECPlayer recPlayer=getRecPlayer(p);
        recPlayer.bankAmount=coins;
        recPlayer.updateBank=true;
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
        if(recPlayer.newAmount<coins)
            return 0;
        recPlayer.newAmount-=coins;
        return 1;
    }

    public int removeBalanceBank(String player, double coins){
        Player p=null;
        try {
            p = player.length() > 16 ? Bukkit.getPlayer(UUID.fromString(player)) : Bukkit.getPlayerExact(player);
        }catch(Exception x){

        }
        if(p==null)
            return -1;
        RECPlayer recPlayer=getRecPlayer(p);
        if(recPlayer.bankAmount<coins)
            return 0;
        recPlayer.bankAmount-=coins;
        recPlayer.updateBank=true;
        return 1;
    }

    public double getMoneyFromFile(String player) {
        double toReturn = RoyaleEconomy.staticValues.defaultCoins;

        try (
                Connection Database = HikariCPDataSource.getConnection();
                ResultSet r = Database.prepareStatement("SELECT coins FROM PlayerPurse WHERE id = '" + player + "'").executeQuery();

        ) {
            if (r.next())
                toReturn = r.getDouble("coins");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return BigDecimal.valueOf(toReturn).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    public double getBankMoneyFromFile(String player) {
        double toReturn = RoyaleEconomy.staticValues.defaultBankCoins;

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

    public int getBankUpgrade(String player) {
        int upgrade = 0;
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

    public void runLargeUpdate(HashMap<RECPlayer, Double> values, ArrayList<RECPlayer> bank){
        try(
                Connection Database = HikariCPDataSource.getConnection();
                PreparedStatement updatePurse = Database.prepareStatement("UPDATE PlayerPurse SET coins = ? WHERE id = ?");
                PreparedStatement updateBank = Database.prepareStatement("UPDATE PersonalBank SET coins = ? WHERE id = ?");
                ){
                    for(Map.Entry<RECPlayer, Double> player : values.entrySet()) {
                        updatePurse.setDouble(1, player.getValue());
                        updatePurse.setString(2, player.getKey().player.getUniqueId().toString());
                        updatePurse.addBatch();
                    }
                    updatePurse.executeBatch();

                    for(RECPlayer player : bank){
                        updateBank.setDouble(1, player.bankAmount);
                        updateBank.setString(2, player.player.getUniqueId().toString());
                        updateBank.addBatch();
                        player.updateBank=false;
                    }
                    updateBank.executeBatch();
        }catch (Exception x){
            x.printStackTrace();
            for(RECPlayer player : values.keySet())
                player.previousAmount--;
        }
        //RoyaleEconomy.plugin.getLogger().info("--------- new data --------------");
        ////for(Map.Entry<RECPlayer, Double> entry : values.entrySet())
        //    RoyaleEconomy.plugin.getLogger().info(entry.getKey().player.getName()+" "+entry.getValue());
        //RoyaleEconomy.plugin.getLogger().info("---------------------------------");
    }

    public void runLargeUpdateBankOnly(ArrayList<RECPlayer> bank){
        try(
                Connection Database = HikariCPDataSource.getConnection();
                PreparedStatement updateBank = Database.prepareStatement("UPDATE PersonalBank SET coins = ? WHERE id = ?");
        ){
            for(RECPlayer player : bank){
                updateBank.setDouble(1, player.bankAmount);
                updateBank.setString(2, player.player.getUniqueId().toString());
                updateBank.addBatch();
                player.updateBank=false;
            }
            updateBank.executeBatch();
        }catch (Exception x){
            x.printStackTrace();
        }
    }
}
