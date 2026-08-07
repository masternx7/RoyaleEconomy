package me.qKing12.RoyaleEconomy.TimeRewards;

import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class TimeRewardPlayerData {

    public static ConcurrentHashMap<Player, TimeRewardData> playerData=new ConcurrentHashMap<>();

    public static boolean unclaimedRewards(Player player) {
        TimeRewardData data = playerData.getOrDefault(player, null);
        if (data == null)
            return true;
        long current = ZonedDateTime.now().toEpochSecond();
        for (TimeReward reward : RoyaleEconomy.plugin.timeRewardsManager.rewards) {
            if (reward.canSee(player) && reward.canClaim(player)) {
                long endingDate = data.cooldowns.getOrDefault(reward.name, 0L);
                if (endingDate < current) {
                    return true;
                }
            }
        }


        return false;
    }

    public static class TimeRewardData{
        private File file;
        private FileConfiguration cfg;
        private ConcurrentHashMap<String, Integer> streaks;
        private ConcurrentHashMap<String, Long> cooldowns;

        public boolean containsReward(String reward){
            return RoyaleEconomy.timeRewardsCfg.contains("rewards."+reward);
        }

        public TimeRewardData(Player player){
            file=new File(RoyaleEconomy.plugin.getDataFolder(), "ymlData/"+player.getUniqueId().toString()+".yml");
            streaks=new ConcurrentHashMap<>();
            cooldowns=new ConcurrentHashMap<>();
            if(!file.exists()){
                try {
                    file.createNewFile();
                    cfg=YamlConfiguration.loadConfiguration(file);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else {
                cfg = YamlConfiguration.loadConfiguration(file);
                if (cfg.contains("rewards"))
                    for (String key : cfg.getConfigurationSection("rewards").getKeys(false)) {
                        streaks.put(key, cfg.getInt("rewards." + key + ".streak"));
                        cooldowns.put(key, cfg.getLong("rewards." + key + ".cooldown"));
                    }
            }
            playerData.put(player, this);
        }

        public long getCooldown(String reward, int hours){
            long endingDate=cooldowns.getOrDefault(reward, 0L);
            if(endingDate==0)
                return 0L;
            long startDate=ZonedDateTime.now().toEpochSecond();
            if(endingDate<startDate) {
                if(endingDate+hours*3600<startDate)
                    return -1;
                return 0L;
            }
            return endingDate-startDate;
        }

        public long getCooldown(String reward){
            long endingDate=cooldowns.getOrDefault(reward, 0L);
            if(endingDate==0)
                return 0L;
            long startDate=ZonedDateTime.now().toEpochSecond();
            if(endingDate<startDate) {
                int hours=-1;
                for(TimeReward r : RoyaleEconomy.plugin.timeRewardsManager.rewards){
                    if(r.name.equalsIgnoreCase(reward)){
                        hours=r.hours;
                        break;
                    }
                }
                if(hours==-1)
                    return 0L;
                if(endingDate+hours*3600<startDate)
                    return -1;
                return 0L;
            }
            return endingDate-startDate;
        }

        public void resetCooldown(String reward, int hours){
            long time=ZonedDateTime.now().toEpochSecond()+hours*3600L;
            cooldowns.put(reward, time);
            cfg.set("rewards."+reward+".cooldown", time);
        }

        public void removeCooldown(String reward){
            long time = ZonedDateTime.now().toEpochSecond();
            cooldowns.put(reward, time);
            cfg.set("rewards."+reward+".cooldown", time);
        }

        public int getStreak(String reward){
            return streaks.getOrDefault(reward, 0);
        }

        public void setStreak(String reward, int streak){
            streaks.put(reward, streak);
            cfg.set("rewards."+reward+".streak", streak);
        }

        public void saveToFile(){
            try {
                cfg.save(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
