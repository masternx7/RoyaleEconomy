package me.qKing12.RoyaleEconomy.TimeRewards;

import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import me.qKing12.RoyaleEconomy.utils.Utils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;

public class TimeStreak {

    public HashMap<Integer, TimeStreakInfo> streaks=new HashMap<>();

    public TimeStreak(ConfigurationSection cfg){
        for(String streak : cfg.getKeys(false)){
            Integer nr=Integer.parseInt(streak);
            streaks.put(nr, new TimeStreakInfo(cfg.getConfigurationSection(streak)));
        }
    }

    public static class TimeStreakInfo{
        public String claimMessage;
        public ItemStack overwriteItem;
        public ItemStack overwriteItemCooldown;
        public ArrayList<String> beforeStreakLore;
        public ArrayList<String> commandRewards;

        public TimeStreakInfo(ConfigurationSection cfg){
            claimMessage= Utils.chat(cfg.getString("claim-message"));
            overwriteItem= RoyaleEconomy.itemConstructor.getItemFromMaterial(cfg.getString("streak-overwrite-item"));
            overwriteItemCooldown= RoyaleEconomy.itemConstructor.getItemFromMaterial(cfg.getString("streak-overwrite-item-cooldown"));
            beforeStreakLore=new ArrayList<>();
            for(String line : cfg.getStringList("before-streak-lore"))
                beforeStreakLore.add(Utils.chat(line));
            commandRewards=(ArrayList<String>)cfg.getStringList("command-rewards");
        }
    }
}
