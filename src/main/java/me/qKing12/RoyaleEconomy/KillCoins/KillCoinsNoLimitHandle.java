package me.qKing12.RoyaleEconomy.KillCoins;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import me.qKing12.RoyaleEconomy.utils.Utf8YamlConfiguration;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.HashMap;



public class KillCoinsNoLimitHandle implements KillCoinsLimitInterface {

    private static int day;
    private static HashMap<String, PlayerKillCoinsData> killCoinsData=new HashMap<>();
    private static File database;
    private static FileConfiguration databaseCfg;

    public FileConfiguration getData() {
        return databaseCfg;
    }

    private void checkDataReset(){
        RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runTimerAsync(() ->{
            if(day!=ZonedDateTime.now().getDayOfMonth()){
                databaseCfg.set("kills", null);
                day = ZonedDateTime.now().getDayOfMonth();
                databaseCfg.set("day", day);
                killCoinsData=new HashMap<>();
            }
            saveData();
        }, 2400, 2400);
    }

    private void loadData() {
        try {
            database = new File(RoyaleEconomy.plugin.getDataFolder(), "database/killCoinsData.yml");
            if (database.exists()) {
                databaseCfg = Utf8YamlConfiguration.loadConfiguration(database);
                day = databaseCfg.getInt("day");

                if (day != ZonedDateTime.now().getDayOfMonth()) {
                    databaseCfg.set("kills", null);
                    day = ZonedDateTime.now().getDayOfMonth();
                    databaseCfg.set("day", day);
                    return;
                }

                if(databaseCfg.contains("kills")){
                    for(String key : databaseCfg.getConfigurationSection("kills").getKeys(false)){
                        killCoinsData.put(key, new PlayerKillCoinsData(databaseCfg.getDouble("kills."+key+".totalCoins"), databaseCfg.getConfigurationSection("kills."+key+".mobSaver"), databaseCfg.getConfigurationSection("kills."+key+".mobKillCoins")));
                    }
                }
                checkDataReset();
            } else {
                day = ZonedDateTime.now().getDayOfMonth();
                database.createNewFile();
                databaseCfg = Utf8YamlConfiguration.loadConfiguration(database);
                databaseCfg.set("day", day);
            }
        } catch (Exception x) {
            x.printStackTrace();
        }
    }

    public void saveData() {
        try {
            databaseCfg.save(database);
        } catch (Exception x) {
            x.printStackTrace();
        }
    }

    public KillCoinsNoLimitHandle(){
        loadData();
    }

    public Double getTotalCoins(Player p) {
        PlayerKillCoinsData data = killCoinsData.getOrDefault(p.getUniqueId().toString(), null);
        if(data==null)
            return 0d;
        return data.totalCoins;
    }

    public int getCount(Player p, String mobType) {
        PlayerKillCoinsData data = killCoinsData.getOrDefault(p.getUniqueId().toString(), null);
        if(data==null)
            return 0;
        return data.mobLimitsSaver.getOrDefault(mobType, 0);
    }

    public double getMoney(Player p, String mobType) {
        PlayerKillCoinsData data = killCoinsData.getOrDefault(p.getUniqueId().toString(), null);
        if(data==null)
            return 0d;
        return data.mobKillCoins.getOrDefault(mobType, 0d);
    }

    public boolean hasReachedLimit(Player p, String mobType, Double coins, boolean custom){
        PlayerKillCoinsData data = killCoinsData.getOrDefault(p.getUniqueId().toString(), null);
        if (data!=null) {
            data.totalCoins += coins;
            databaseCfg.set("kills."+p.getUniqueId().toString()+".totalCoins", data.totalCoins);
            double finalCoins=coins + data.mobKillCoins.getOrDefault(mobType, 0d);
            data.mobKillCoins.put(mobType, finalCoins);
            databaseCfg.set("kills."+p.getUniqueId().toString()+".mobKillCoins."+mobType, finalCoins);
            int limit = data.mobLimitsSaver.getOrDefault(mobType, 0);
            data.mobLimitsSaver.put(mobType, limit + 1);
            databaseCfg.set("kills."+p.getUniqueId().toString()+".mobSaver."+mobType, limit+1);
        } else {
            data=new PlayerKillCoinsData(coins, null, null);
            data.mobLimitsSaver.put(mobType, 1);
            data.mobKillCoins.put(mobType, coins);
            data.totalCoins=coins;
            killCoinsData.put(p.getUniqueId().toString(), data);

            databaseCfg.set("kills."+p.getUniqueId().toString()+".totalCoins", coins);
            databaseCfg.set("kills."+p.getUniqueId().toString()+".mobKillCoins."+mobType, coins);
            databaseCfg.set("kills."+p.getUniqueId().toString()+".mobSaver."+mobType, 1);
        }
        return false;
    }


}
