package me.qKing12.RoyaleEconomy.TimeRewards;

import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import me.qKing12.RoyaleEconomy.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class TimeRewardMenu {
    public HashMap<Integer, TimeReward> itemsNoStreak=new HashMap<>();
    public HashMap<Integer, TimeReward> itemsStreak=new HashMap<>();
    private Inventory inventory;

    public void generateInventory(){
        TimeRewardsManager manager = RoyaleEconomy.plugin.timeRewardsManager;
        inventory= Bukkit.createInventory(null, manager.slots, manager.menuName);

        try {
            for (int i = 0; i < manager.slots; i++)
                inventory.setItem(i, manager.backgroundItem);
        }catch(Exception x){

        }

        for(TimeReward reward : RoyaleEconomy.plugin.timeRewardsManager.rewards){
            if(reward.canSee(player)){
                Map.Entry<ItemStack, Integer> item=reward.getCurrentItem(player);
                inventory.setItem(reward.slot, item.getKey());
                if(item.getValue()==1)
                    itemsNoStreak.put(reward.slot, reward);
                else if(item.getValue()==2)
                    itemsStreak.put(reward.slot, reward);
                else if(item.getValue()==-1){
                    if(reward.timeStreak!=null) {
                        TimeRewardPlayerData.TimeRewardData data = TimeRewardPlayerData.playerData.get(player);
                        data.setStreak(reward.name, 0);
                        data.saveToFile();
                    }
                    itemsNoStreak.put(reward.slot, reward);
                }
            }
        }

        inventory.setItem(RoyaleEconomy.plugin.timeRewardsManager.closeSlot, RoyaleEconomy.plugin.timeRewardsManager.closeItem);
    }


    Player player;
    ClickListen clickListen=new ClickListen();

    public TimeRewardMenu(Player p){
        player=p;
        generateInventory();
        p.openInventory(inventory);
        Bukkit.getPluginManager().registerEvents(clickListen, RoyaleEconomy.plugin);
    }

    public class ClickListen implements Listener {

        @EventHandler
        public void onClick(InventoryClickEvent e){
            if(!e.getInventory().equals(inventory))
                return;
            e.setCancelled(true);
            if(e.getSlot()<0)
                return;
            if(e.getWhoClicked().equals(player)){
                if(e.getClickedInventory().equals(inventory)) {
                    if (e.getSlot() == RoyaleEconomy.plugin.timeRewardsManager.closeSlot)
                        player.closeInventory();
                    else if(itemsStreak.containsKey(e.getSlot())){
                        TimeReward reward=itemsStreak.get(e.getSlot());
                        TimeRewardPlayerData.TimeRewardData data = TimeRewardPlayerData.playerData.get(player);
                        int streak = data.getStreak(reward.name);
                        TimeStreak.TimeStreakInfo streakInfo = reward.timeStreak.streaks.getOrDefault(streak + 1, null);
                        if(reward.moneyRequirement!=0){
                            if(RoyaleEconomy.dataManager.removeMoneyFromFile(player.getUniqueId().toString(), reward.moneyRequirement)){
                                data.resetCooldown(reward.name, reward.hours);
                                data.setStreak(reward.name, streak+1);
                                data.saveToFile();
                                RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runNextTick((task) -> {
                                   for(String command : reward.commandRewards)
                                       Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", player.getName()));

                                   for(String command : streakInfo.commandRewards)
                                       Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", player.getName()));
                                });
                                Utils.playSoundDirectly((Player) e.getWhoClicked(), RoyaleEconomy.plugin.timeRewardsManager.claimSound);
                            }
                            else {
                                player.sendMessage(reward.moneyDeny);
                                Utils.playSoundDirectly((Player) e.getWhoClicked(), RoyaleEconomy.plugin.timeRewardsManager.noMoneySound);
                                return;
                            }
                        }
                        else{
                            data.resetCooldown(reward.name, reward.hours);
                            data.setStreak(reward.name, streak+1);
                            data.saveToFile();
                            RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runNextTick((task) -> {
                                for(String command : reward.commandRewards)
                                    Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", player.getName()));

                                for(String command : streakInfo.commandRewards)
                                    Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", player.getName()));
                            });
                            Utils.playSoundDirectly((Player) e.getWhoClicked(), RoyaleEconomy.plugin.timeRewardsManager.claimSound);
                        }

                        itemsStreak.remove(e.getSlot());
                        inventory.setItem(e.getSlot(), reward.getCooldownItem(reward.timeStreak.streaks.getOrDefault(streak + 2, null), streak));
                    }
                    else if(itemsNoStreak.containsKey(e.getSlot())){
                        TimeReward reward=itemsNoStreak.get(e.getSlot());
                        if(reward.moneyRequirement!=0){
                            if(RoyaleEconomy.dataManager.removeMoneyFromFile(player.getUniqueId().toString(), reward.moneyRequirement)){
                                TimeRewardPlayerData.TimeRewardData data = TimeRewardPlayerData.playerData.get(player);
                                data.resetCooldown(reward.name, reward.hours);
                                data.saveToFile();
                                RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runNextTick((task) -> {
                                    for(String command : reward.commandRewards)
                                        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", player.getName()));

                                 });
                                Utils.playSoundDirectly((Player) e.getWhoClicked(), RoyaleEconomy.plugin.timeRewardsManager.claimSound);
                            }
                            else {
                                player.sendMessage(reward.moneyDeny);
                                Utils.playSoundDirectly((Player) e.getWhoClicked(), RoyaleEconomy.plugin.timeRewardsManager.noMoneySound);
                                return;
                            }
                        }
                        else{
                            TimeRewardPlayerData.TimeRewardData data = TimeRewardPlayerData.playerData.get(player);
                            data.resetCooldown(reward.name, reward.hours);
                            data.saveToFile();
                            RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runNextTick((task) -> {
                                for(String command : reward.commandRewards)
                                    Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", player.getName()));

                            });
                            Utils.playSoundDirectly((Player) e.getWhoClicked(), RoyaleEconomy.plugin.timeRewardsManager.claimSound);
                        }

                        itemsNoStreak.remove(e.getSlot());
                        if(reward.timeStreak!=null) {
                            TimeRewardPlayerData.TimeRewardData data = TimeRewardPlayerData.playerData.get(player);
                            int streak = data.getStreak(reward.name);
                            TimeStreak.TimeStreakInfo streakInfo = reward.timeStreak.streaks.getOrDefault(streak + 2, null);
                            inventory.setItem(e.getSlot(), reward.getCooldownItem(streakInfo, streak));
                            data.setStreak(reward.name, streak+1);
                            data.saveToFile();
                        }
                        else
                            inventory.setItem(e.getSlot(), reward.getCooldownItem(null, 0));
                    }
                    else if(e.getCurrentItem()!=null && !e.getCurrentItem().equals(RoyaleEconomy.plugin.timeRewardsManager.backgroundItem)){
                        Utils.playSoundDirectly((Player) e.getWhoClicked(), RoyaleEconomy.plugin.timeRewardsManager.noPermissionSound);
                    }
                }
            }
        }

        @EventHandler
        public void onClose(InventoryCloseEvent e){
            if(e.getInventory().equals(inventory)){
                HandlerList.unregisterAll(this);
            }
        }
    }
}
