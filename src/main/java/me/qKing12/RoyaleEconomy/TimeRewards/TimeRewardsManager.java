package me.qKing12.RoyaleEconomy.TimeRewards;

import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import me.qKing12.RoyaleEconomy.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.Set;

public class TimeRewardsManager implements Listener {

    public ArrayList<TimeReward> rewards;

    String claimSound;
    String noPermissionSound;
    String noMoneySound;

    String usePermission;
    String denyMessage;
    String menuName;
    int slots;

    ItemStack backgroundItem;
    ItemStack closeItem;
    int closeSlot;

    String joinMessage;

    public TimeRewardsManager(){
        rewards=new ArrayList<>();
        usePermission= RoyaleEconomy.timeRewardsCfg.getString("use-permission");
        denyMessage=Utils.chat(RoyaleEconomy.timeRewardsCfg.getString("deny-message"));
        menuName=Utils.chat(RoyaleEconomy.timeRewardsCfg.getString("menu-name"));
        slots=RoyaleEconomy.timeRewardsCfg.getInt("menu-size");

        claimSound=RoyaleEconomy.timeRewardsCfg.getString("sounds.claim-sound");
        noPermissionSound=RoyaleEconomy.timeRewardsCfg.getString("sounds.no-permission-sound");
        noMoneySound=RoyaleEconomy.timeRewardsCfg.getString("sounds.no-money");
        backgroundItem=RoyaleEconomy.itemConstructor.getItemFromMaterial(RoyaleEconomy.timeRewardsCfg.getString("background-item"));

        ArrayList<String> lore=new ArrayList<>();
        for(String line : RoyaleEconomy.timeRewardsCfg.getStringList("close-item.lore"))
            lore.add(Utils.chat(line));
        closeItem=RoyaleEconomy.itemConstructor.getItem(RoyaleEconomy.timeRewardsCfg.getString("close-item.material"), Utils.chat(RoyaleEconomy.timeRewardsCfg.getString("close-item.name")), lore);
        closeSlot=RoyaleEconomy.timeRewardsCfg.getInt("close-item.slot");

        if(RoyaleEconomy.timeRewardsCfg.getBoolean("use-join-message")){
            Bukkit.getPluginManager().registerEvents(this, RoyaleEconomy.plugin);
            joinMessage= Utils.chat(RoyaleEconomy.timeRewardsCfg.getString("on-join-message"));
        }

        if(RoyaleEconomy.timeRewardsCfg.getBoolean("use-streaks")){
            Set<String> keys = RoyaleEconomy.timeRewardsCfg.getConfigurationSection("streaks").getKeys(false);
            for(String key : RoyaleEconomy.timeRewardsCfg.getConfigurationSection("rewards").getKeys(false)) {
                TimeReward reward=new TimeReward(RoyaleEconomy.timeRewardsCfg.getConfigurationSection("rewards." + key), key);
                rewards.add(reward);
                if(keys.contains(key))
                    reward.timeStreak=new TimeStreak(RoyaleEconomy.timeRewardsCfg.getConfigurationSection("streaks."+key));
            }
        }
        else
            for(String key : RoyaleEconomy.timeRewardsCfg.getConfigurationSection("rewards").getKeys(false))
                rewards.add(new TimeReward(RoyaleEconomy.timeRewardsCfg.getConfigurationSection("rewards."+key), key));

        File directory = new File(RoyaleEconomy.plugin.getDataFolder(), "ymlData");
        if(!directory.exists())
            directory.mkdir();

        new TimeRewardCommand();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e){
        RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runLaterAsync(() -> {
            if(TimeRewardPlayerData.unclaimedRewards(e.getPlayer()))
                e.getPlayer().sendMessage(joinMessage);
        }, 30L);
    }

}
