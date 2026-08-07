package me.qKing12.RoyaleEconomy.KillCoins;

import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import me.qKing12.RoyaleEconomy.utils.PermissionChecker;
import me.qKing12.RoyaleEconomy.utils.PlayerMessageHandler;
import me.qKing12.RoyaleEconomy.utils.Utf8YamlConfiguration;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;

import static me.qKing12.RoyaleEconomy.RoyaleEconomy.utilsAPI;


public class KillCoinsLimitHandle implements KillCoinsLimitInterface {
    private static int day;

    private static HashMap<String, PlayerKillCoinsData> killCoinsData=new HashMap<>();

    private static Double limitCoins;
    private static HashMap<String, Integer> limitMobs = new HashMap<>();
    private static ArrayList<String> hasReachedLimit = new ArrayList<>();

    public FileConfiguration getData() {
        return databaseCfg;
    }

    private static File database;
    private static FileConfiguration databaseCfg;

    private void checkDataReset() {
        RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runTimerAsync(() -> {
            if (day != ZonedDateTime.now().getDayOfMonth()) {
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

    public KillCoinsLimitHandle() {
        limitCoins = RoyaleEconomy.killCoinsAndPurseDeathCfg.getDouble("kill-coins.maximum-coins-per-day");
        try {
            for (String key : RoyaleEconomy.killCoinsAndPurseDeathCfg.getConfigurationSection("kill-coins.kill-events").getKeys(false)) {
                int limit = RoyaleEconomy.killCoinsAndPurseDeathCfg.getInt("kill-coins.kill-events." + key + ".maximum-kills");
                if (limit != 0)
                    limitMobs.put(key, limit);
            }
        } catch (Exception x) {
            RoyaleEconomy.plugin.getLogger().info("Kill Events Not Found. Ignoring and continuing.");
        }
        try {
            for (String key : RoyaleEconomy.killCoinsAndPurseDeathCfg.getConfigurationSection("kill-coins.custom-kill-events").getKeys(false)) {
                int limit = RoyaleEconomy.killCoinsAndPurseDeathCfg.getInt("kill-coins.custom-kill-events." + key + ".maximum-kills");
                if (limit != 0)
                    limitMobs.put("custom-kill-" + key, limit);
            }
        } catch (Exception x) {
            RoyaleEconomy.plugin.getLogger().info("Custom Kill Events Not Found. Ignoring and continuing.");
        }
        try {
            for (String key : RoyaleEconomy.killCoinsAndPurseDeathCfg.getConfigurationSection("kill-coins.mythic-mobs-events").getKeys(false)) {
                int limit = RoyaleEconomy.killCoinsAndPurseDeathCfg.getInt("kill-coins.mythic-mobs-events." + key + ".maximum-kills");
                if (limit != 0)
                    limitMobs.put("mythic-mobs-" + key, limit);
            }
        } catch (Exception x) {
            RoyaleEconomy.plugin.getLogger().info("Mythic Mobs Events Not Found. Ignoring and continuing.");
        }
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

    private boolean hasReachedLimitBypass(Player p, String mobType, Double coins) {
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

    public boolean hasReachedLimit(Player p, String mobType, Double coins, boolean custom) {
        if (PermissionChecker.checkKillCoinsBypass(p))
            return hasReachedLimitBypass(p, mobType, coins);
        PlayerKillCoinsData data = killCoinsData.getOrDefault(p.getUniqueId().toString(), null);
        if (data!=null) {
            double newCoins = data.totalCoins + coins;
            if (limitCoins != 0 && newCoins >= limitCoins) {
                if (!hasReachedLimit.contains(p.getUniqueId().toString())) {
                    for (String line : RoyaleEconomy.killCoinsAndPurseDeathCfg.getStringList("kill-coins.maximum-reached-message"))
                        PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, line));
                    hasReachedLimit.add(p.getUniqueId().toString());
                }
                return true;
            }
            if (data.mobKillCoins.containsKey(mobType)) {

                int limit = data.mobLimitsSaver.getOrDefault(mobType, 0);
                data.mobLimitsSaver.put(mobType, 1 + limit);
                databaseCfg.set("kills."+p.getUniqueId().toString()+".mobSaver."+mobType, limit+1);

                if (limitMobs.containsKey(mobType) && limit + 1 >= limitMobs.get(mobType)) {
                    if (limit + 1 == limitMobs.get(mobType)) {
                        if (custom) {
                            String configSection;
                            if (mobType.startsWith("mythic-mobs-"))
                                configSection = "mythic-mobs";
                            else
                                configSection = "custom-kill";
                            for (String line : RoyaleEconomy.killCoinsAndPurseDeathCfg.getStringList("kill-coins." + configSection + "-events." + mobType.substring(12) + ".maximum-reached-message"))
                                PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, line));
                        } else
                            for (String line : RoyaleEconomy.killCoinsAndPurseDeathCfg.getStringList("kill-coins.kill-events." + mobType + ".maximum-reached-message"))
                                PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, line));
                    }
                    return true;
                }
                double finalCoins = coins + data.mobKillCoins.get(mobType);
                data.mobKillCoins.put(mobType, finalCoins);
                databaseCfg.set("kills."+p.getUniqueId().toString()+".mobKillCoins."+mobType, finalCoins);
            } else {
                data.mobKillCoins.put(mobType, coins);
                data.mobLimitsSaver.put(mobType, 1);
                databaseCfg.set("kills."+p.getUniqueId().toString()+".mobKillCoins."+mobType, coins);
                databaseCfg.set("kills."+p.getUniqueId().toString()+".mobSaver."+mobType, 1);
            }
            data.totalCoins=newCoins;
            databaseCfg.set("kills."+p.getUniqueId().toString()+".totalCoins", newCoins);
            return false;
        } else {
            data=new PlayerKillCoinsData(coins, null, null);
            data.mobLimitsSaver.put(mobType, 1);
            data.mobKillCoins.put(mobType, coins);
            data.totalCoins=coins;
            killCoinsData.put(p.getUniqueId().toString(), data);

            databaseCfg.set("kills."+p.getUniqueId().toString()+".totalCoins", coins);
            databaseCfg.set("kills."+p.getUniqueId().toString()+".mobKillCoins."+mobType, coins);
            databaseCfg.set("kills."+p.getUniqueId().toString()+".mobSaver."+mobType, 1);
            return false;
        }
    }
}
