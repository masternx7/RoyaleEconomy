package me.qKing12.RoyaleEconomy.TimeRewards;

import me.qKing12.RoyaleEconomy.DataManager.StaticValues;
import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import me.qKing12.RoyaleEconomy.utils.Utils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Map;

public class TimeReward {
    String name;
    TimeStreak timeStreak;

    int slot;
    int hours;
    double moneyRequirement;
    String moneyDeny;
    String permissionSee;
    String permissionClaim;

    ArrayList<String> commandRewards;

    ItemStack materialClaim;
    String nameClaim;
    ArrayList<String> loreClaim;

    ItemStack materialCooldown;
    String nameCooldown;
    ArrayList<String> loreCooldown;

    ItemStack materialDeny;
    String nameDeny;
    ArrayList<String> loreDeny;


    public TimeReward(ConfigurationSection cfg, String name){
        this.name=name;
        slot=cfg.getInt("slot");
        hours=cfg.getInt("reset-time-hours");
        moneyRequirement=cfg.getDouble("money-requirements");
        moneyDeny=Utils.chat(cfg.getString("money-deny-message"));
        permissionSee=cfg.getString("see-permission");
        permissionClaim=cfg.getString("claim-permission");

        commandRewards=(ArrayList<String>)cfg.getStringList("command-rewards");

        materialClaim= RoyaleEconomy.itemConstructor.getItemFromMaterial(cfg.getString("items.claim.item"));
        nameClaim= Utils.chat(cfg.getString("items.claim.name"));
        loreClaim=new ArrayList<>();
        for(String line : cfg.getStringList("items.claim.lore"))
            loreClaim.add(Utils.chat(line));

        materialCooldown= RoyaleEconomy.itemConstructor.getItemFromMaterial(cfg.getString("items.in-cooldown.item"));
        nameCooldown= Utils.chat(cfg.getString("items.in-cooldown.name"));
        loreCooldown=new ArrayList<>();
        for(String line : cfg.getStringList("items.in-cooldown.lore"))
            loreCooldown.add(Utils.chat(line));

        materialDeny= RoyaleEconomy.itemConstructor.getItemFromMaterial(cfg.getString("items.no-permission.item"));
        nameDeny= Utils.chat(cfg.getString("items.no-permission.name"));
        loreDeny=new ArrayList<>();
        for(String line : cfg.getStringList("items.no-permission.lore"))
            loreDeny.add(Utils.chat(line));
    }

    public boolean canSee(Player player){
        return permissionSee.equals("none") || player.hasPermission(permissionSee);
    }

    public boolean canClaim(Player player){
        return permissionClaim.equals("none") || player.hasPermission(permissionClaim);
    }

    //0=no
    //1=yes
    //-1=lost streak
    //2=has streak
    public Map.Entry<ItemStack, Integer> getCurrentItem(Player player) {
        int canGet=0;
        if (canClaim(player)) {
            TimeRewardPlayerData.TimeRewardData data = TimeRewardPlayerData.playerData.get(player);
            int streak = data.getStreak(name);
            TimeStreak.TimeStreakInfo streakInfo;
            if(timeStreak!=null)
                streakInfo = timeStreak.streaks.getOrDefault(streak + 1, null);
            else
                streakInfo=null;
            ItemStack item;
            String itemName;
            ArrayList<String> lore;
            long cooldown = data.getCooldown(name, hours);
            String cooldownString = RoyaleEconomy.messageHelper.formatTimeDetailed(cooldown * 1000);
            if(cooldown==-1){
                canGet=-1;
                item = materialClaim.clone();
                itemName = nameClaim;
                lore=new ArrayList<>();
                for(String line : loreClaim)
                    lore.add(line.replace("%streak%", String.valueOf(0)));
            }
            else if (streakInfo == null) {
                if (cooldown > 0) {
                    item = materialCooldown.clone();
                    lore = new ArrayList<>();
                    for (String line : loreCooldown)
                        lore.add(line.replace("%cooldown%", cooldownString).replace("%streak%", String.valueOf(streak)));
                    itemName = nameCooldown;
                } else {
                    canGet=1;
                    item = materialClaim.clone();
                    itemName = nameClaim;
                    lore=new ArrayList<>();
                    for(String line : loreClaim)
                        lore.add(line.replace("%streak%", String.valueOf(streak)));
                }
            } else {
                if (cooldown > 0) {
                    item = streakInfo.overwriteItemCooldown.clone();
                    itemName = nameCooldown;
                    lore = new ArrayList<>();
                    for (String line : loreCooldown)
                        lore.add(line.replace("%cooldown%", cooldownString).replace("%streak%", String.valueOf(streak)));
                } else {
                    canGet=2;
                    item = streakInfo.overwriteItem.clone();
                    itemName = nameClaim;
                    lore=new ArrayList<>();
                    for(String line : loreClaim)
                        lore.add(line.replace("%streak%", String.valueOf(streak)));
                }
                lore.addAll(streakInfo.beforeStreakLore);
            }

            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(itemName);
            meta.setLore(lore);
            item.setItemMeta(meta);
            return new AbstractMap.SimpleEntry<>(item, canGet);
        } else {
            ItemStack item = materialDeny.clone();
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(nameDeny);
            meta.setLore(loreDeny);
            item.setItemMeta(meta);
            return new AbstractMap.SimpleEntry<>(item, canGet);
        }
    }

    public ItemStack getCooldownItem(TimeStreak.TimeStreakInfo streakInfo, int streak) {
        ItemStack item;
        ArrayList<String> lore;
        int days = hours / 24;
        String cooldownString;
        if (days == 0) {
            if (hours == 1)
                cooldownString = "1 " + RoyaleEconomy.staticValues.hour;
            else
                cooldownString = hours + " " + RoyaleEconomy.staticValues.hours;
        } else {
            int hours = this.hours % 24;
            if (days == 1) {
                cooldownString = "1 " + RoyaleEconomy.staticValues.day;
            } else
                cooldownString = days + " " + RoyaleEconomy.staticValues.days;
            if (hours == 1)
                cooldownString = cooldownString + " 1 " + RoyaleEconomy.staticValues.hour;
            else if (hours > 1)
                cooldownString = cooldownString + " " + hours + " " + RoyaleEconomy.staticValues.hours;
        }
        if (streakInfo == null) {
            item = materialCooldown.clone();
            lore = new ArrayList<>();
            for (String line : loreCooldown)
                lore.add(line.replace("%cooldown%", cooldownString).replace("%streak%", String.valueOf(streak+1)));
        } else {
            item = streakInfo.overwriteItemCooldown.clone();
            lore = new ArrayList<>();
            for (String line : loreCooldown)
                lore.add(line.replace("%cooldown%", cooldownString).replace("%streak%", String.valueOf(streak+1)));
            lore.addAll(streakInfo.beforeStreakLore);
        }

        ItemMeta meta=item.getItemMeta();
        meta.setDisplayName(nameCooldown);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

}
