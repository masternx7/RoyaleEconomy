package me.qKing12.RoyaleEconomy.Events;

import me.qKing12.RoyaleEconomy.API.CoinsBags;
import me.qKing12.RoyaleEconomy.Commands.MoneyBagCommand;
import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import me.qKing12.RoyaleEconomy.utils.PlayerMessageHandler;
import me.qKing12.RoyaleEconomy.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static me.qKing12.RoyaleEconomy.RoyaleEconomy.utilsAPI;



public class PurseDeathEvent implements Listener {

    private ArrayList<String> disabledWorlds = new ArrayList<>();
    private final boolean dropBag;
    private final double dropBagMinimum;

    private void dropBag(double amount, Player p){
        if(amount<dropBagMinimum)
            return;
        ItemStack bag = MoneyBagCommand.generateMoneyBag(amount);
        p.getWorld().dropItemNaturally(p.getLocation(), bag);
    }

    private void handleDeathCause(String cause, Player p, List<ItemStack> contents){
        RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runAsync((task) -> {
            if(RoyaleEconomy.talismanHandler.saveCoins(p, contents))
                return;
            double coins = RoyaleEconomy.dataManager.getMoneyFromFile(p.getUniqueId().toString());
            if(RoyaleEconomy.killCoinsAndPurseDeathCfg.getConfigurationSection("purse-coins-handle.death-events")!=null) {
                for (String key : RoyaleEconomy.killCoinsAndPurseDeathCfg.getConfigurationSection("purse-coins-handle.death-events").getKeys(false)) {
                    if (cause.equalsIgnoreCase(RoyaleEconomy.killCoinsAndPurseDeathCfg.getString("purse-coins-handle.death-events." + key + ".death-cause"))) {
                        double percent = RoyaleEconomy.talismanHandler.reducePercent(RoyaleEconomy.killCoinsAndPurseDeathCfg.getDouble("purse-coins-handle.death-events." + key + ".percent-to-take"), p, contents);
                        double toTake = RoyaleEconomy.messageHelper.useDecimals ? coins * percent / 100 : Math.floor(coins * percent / 100);
                        if (toTake != 0) {
                            me.qKing12.RoyaleEconomy.API.Events.PurseDeathEvent purseDeathEvent = new me.qKing12.RoyaleEconomy.API.Events.PurseDeathEvent(p, toTake);
                            Bukkit.getPluginManager().callEvent(purseDeathEvent);
                            if (!purseDeathEvent.isCancelled()) {
                                Utils.playSound(p, "others.purse-death-coins-take");
                                RoyaleEconomy.dataManager.removeMoneyFromFile(p.getUniqueId().toString(), toTake);
                                RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runNextTick((task2) -> {
                                    PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.killCoinsAndPurseDeathCfg.getString("purse-coins-handle.death-events." + key + ".message-to-give").replace("%coins%", RoyaleEconomy.messageHelper.numberFormat(toTake))));
                                    if (dropBag)
                                        dropBag(toTake, p);
                                });
                            }
                        }
                        return;
                    }
                }
            }
            double percent = RoyaleEconomy.talismanHandler.reducePercent(RoyaleEconomy.killCoinsAndPurseDeathCfg.getDouble("purse-coins-handle.default-percent"), p, contents);
            double toTake = RoyaleEconomy.messageHelper.useDecimals ? coins * percent / 100 : Math.floor(coins * percent / 100);;
            if(toTake!=0) {
                    me.qKing12.RoyaleEconomy.API.Events.PurseDeathEvent purseDeathEvent = new me.qKing12.RoyaleEconomy.API.Events.PurseDeathEvent(p, toTake);
                    Bukkit.getPluginManager().callEvent(purseDeathEvent);
                    if (!purseDeathEvent.isCancelled()) {
                        Utils.playSound(p, "others.purse-death-coins-take");
                        RoyaleEconomy.dataManager.removeMoneyFromFile(p.getUniqueId().toString(), toTake);
                        RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runNextTick((task2) -> {
                            PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.killCoinsAndPurseDeathCfg.getString("purse-coins-handle.default-message").replace("%coins%", RoyaleEconomy.messageHelper.numberFormat(toTake))));
                            if(dropBag)
                                dropBag(toTake, p);
                        });
                    }
            }
        });
    }

    PurseDeathEvent(){
        Bukkit.getPluginManager().registerEvents(this, RoyaleEconomy.plugin);
        dropBag=RoyaleEconomy.killCoinsAndPurseDeathCfg.getBoolean("purse-coins-handle.drop-coins-bag-at-death-place");
        dropBagMinimum=RoyaleEconomy.killCoinsAndPurseDeathCfg.getDouble("purse-coins-handle.minimum-coins-for-bag");
        disabledWorlds=(ArrayList<String>)RoyaleEconomy.killCoinsAndPurseDeathCfg.getStringList("purse-coins-handle.disabled-worlds");
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e){
        try {
            if(!e.getEntity().isOnline())
                return;
            String permission = RoyaleEconomy.killCoinsAndPurseDeathCfg.getString("purse-coins-handle.bypass-permission");
            if (!disabledWorlds.contains(e.getEntity().getWorld().getName()) && (permission.equalsIgnoreCase("none") || !e.getEntity().hasPermission(permission))) {
                String cause = e.getEntity().getLastDamageCause().getCause().toString();
                handleDeathCause(cause, e.getEntity(), (e.getKeepInventory() ? Arrays.asList(e.getEntity().getInventory().getContents().clone()) : new ArrayList<>(e.getDrops())));
            }
        }catch(Exception x){

        }
    }
}
