package me.qKing12.RoyaleEconomy.utils;

import de.tr7zw.changeme.nbtapi.NBTItem;
import me.qKing12.RoyaleEconomy.API.Events.RandomCoinsTalismanTrigger;
import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;



public class TalismanHandler implements Listener {
    private static ArrayList<Player> playersListened = new ArrayList<>();

    private boolean useRandomCoins;
    private boolean useReducePercent;
    private boolean useDeathSave;

    public void updateRandomTalisman(Player p){
        if(!playersListened.contains(p))
            new RandomCoinsHandler(p);
    }

    @EventHandler
    public void onItemPickUp(PlayerPickupItemEvent e){
        if(new NBTItem(e.getItem().getItemStack()).getString("RoyaleEconomyTalisman").equals("random-coins-talisman") && !playersListened.contains(e.getPlayer())) {
            RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runLaterAsync(() -> new RandomCoinsHandler(e.getPlayer()), 2);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e){
        if(getRandomCoinsTier(e.getPlayer())!=-1)
            new RandomCoinsHandler(e.getPlayer());
    }

    public TalismanHandler(){
        if(RoyaleEconomy.coinBagsAndTalismansCfg.getBoolean("talismans.use-talismans")) {
            useRandomCoins = RoyaleEconomy.coinBagsAndTalismansCfg.getBoolean("talismans.random-coins-talisman.use-this-talisman");
            if(useRandomCoins)
                Bukkit.getPluginManager().registerEvents(this, RoyaleEconomy.plugin);
            useReducePercent = RoyaleEconomy.coinBagsAndTalismansCfg.getBoolean("talismans.percent-reducer-talisman.use-this-talisman");
            useDeathSave = RoyaleEconomy.coinBagsAndTalismansCfg.getBoolean("talismans.death-save-talisman.use-this-talisman");
        }else{
            useReducePercent=false;
            useRandomCoins=false;
            useDeathSave=false;
        }
    }

    private int getRandomCoinsTier(Player p){
        int toReturn=-1;
        for(ItemStack item : p.getInventory().getContents()) {
            if(item==null)
                continue;
            NBTItem nbt = new NBTItem(item);
            if (nbt.getString("RoyaleEconomyTalisman").equals("random-coins-talisman")){
                int tier = nbt.getInteger("RoyaleEconomyIndex");
                if(toReturn<tier)
                    toReturn=tier;
            }
        }
        return toReturn;
    }

    private int findPercent(Player p, List<ItemStack> contents){
        int toReturn=0;
        for(ItemStack item : contents) {
            if(item==null)
                continue;
            NBTItem nbt = new NBTItem(item);
            if (nbt.getString("RoyaleEconomyTalisman").equals("percent-reducer-talisman")){
                int percent = RoyaleEconomy.coinBagsAndTalismansCfg.getInt("talismans.percent-reducer-talisman."+nbt.getInteger("RoyaleEconomyIndex")+".reduce-percent");
                if(toReturn<percent)
                    toReturn=percent;
            }
        }
        return toReturn;
    }

    private int findPercent2(Player p, List<ItemStack> contents){
        int toReturn=0;
        for(ItemStack item : contents) {
            if(item==null)
                continue;
            NBTItem nbt = new NBTItem(item);
            if (nbt.getString("RoyaleEconomyTalisman").equals("death-save-talisman")){
                int percent = RoyaleEconomy.coinBagsAndTalismansCfg.getInt("talismans.death-save-talisman."+nbt.getInteger("RoyaleEconomyIndex")+".percent");
                if(toReturn<percent)
                    toReturn=percent;
            }
        }
        return toReturn;
    }

    public double reducePercent(double initialPercent, Player p, List<ItemStack> contents){
        if(useReducePercent){
            double percent = initialPercent-findPercent(p, contents);
            return Math.max(percent, 0);
        }
        else
            return initialPercent;
    }

    public boolean saveCoins(Player p, List<ItemStack> contents){
        if(useDeathSave){
            Random rand = new Random();
            int random = rand.nextInt(100);
            return random < findPercent2(p, contents);
        }
        else
            return false;
    }

    private class RandomCoinsHandler implements Listener{
        private Player player;

        RandomCoinsHandler(Player p){
            player=p;
            playersListened.add(player);
            generateInterval(true);
            Bukkit.getPluginManager().registerEvents(this, RoyaleEconomy.plugin);
        }

        @EventHandler
        public void onLeave(PlayerQuitEvent e){
            if(e.getPlayer().equals(player)){
                playersListened.remove(player);
                HandlerList.unregisterAll(this);
            }
        }

        private void generateInterval(boolean firstGenerate){
            if(!player.isOnline()){
                playersListened.remove(player);
                HandlerList.unregisterAll(this);
                return;
            }
            int tier=getRandomCoinsTier(player);
            if(tier==-1) {
                playersListened.remove(player);
                HandlerList.unregisterAll(this);
            }
            else{
                ConfigurationSection talisman = RoyaleEconomy.coinBagsAndTalismansCfg.getConfigurationSection("talismans.random-coins-talisman."+tier);
                int minimumInterval = talisman.getInt("minimum-interval");
                int maximumInterval = talisman.getInt("maximum-interval");
                Random rand = new Random();
                int randomTime = rand.nextInt(maximumInterval-minimumInterval+1)+minimumInterval;
                if(!firstGenerate){
                    int minimumCoins = talisman.getInt("minimum-coins");
                    int maximumCoins = talisman.getInt("maximum-coins");
                    int randomCoins = rand.nextInt(maximumCoins-minimumCoins)+minimumCoins;
                    RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runNextTick((task)-> {
                        RandomCoinsTalismanTrigger randomCoinsTalismanTrigger = new RandomCoinsTalismanTrigger(player, randomCoins);
                        Bukkit.getPluginManager().callEvent(randomCoinsTalismanTrigger);
                        if(!randomCoinsTalismanTrigger.isCancelled()) {
                            Utils.playSound(player, "others.coins-talisman-give-sound");
                            RoyaleEconomy.dataManager.addMoneyToFile(player.getUniqueId().toString(), (double) randomCoins);
                        }
                        PlayerMessageHandler.messageSend(player, (Utils.chat(RoyaleEconomy.coinBagsAndTalismansCfg.getString("talismans.random-coins-talisman.talisman-give-message").replace("%amount%", RoyaleEconomy.messageHelper.numberFormat((double)randomCoins)).replace("%talisman-name%", talisman.getString("name")))));
                    });
                }
                RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runLaterAsync(() -> generateInterval(false), randomTime*1200);
            }
        }


    }
}
