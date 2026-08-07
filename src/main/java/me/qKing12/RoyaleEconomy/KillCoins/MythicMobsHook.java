package me.qKing12.RoyaleEconomy.KillCoins;

import io.lumine.mythic.bukkit.BukkitAPIHelper;
import io.lumine.mythic.bukkit.events.MythicMobDeathEvent;
import me.qKing12.RoyaleEconomy.API.Events.CustomEntityKillCoinsEvent;
import me.qKing12.RoyaleEconomy.Boosters.BoostersActive;
import me.qKing12.RoyaleEconomy.Commands.KillCoinsCommand;
import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import me.qKing12.RoyaleEconomy.utils.PlayerMessageHandler;
import me.qKing12.RoyaleEconomy.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Random;

import static me.qKing12.RoyaleEconomy.RoyaleEconomy.utilsAPI;

public class MythicMobsHook implements Listener {

    static boolean isHooked=false;
    static BukkitAPIHelper api;

    public MythicMobsHook(){
        Bukkit.getPluginManager().registerEvents(this, RoyaleEconomy.plugin);
        isHooked=true;
        api=new BukkitAPIHelper();
    }

    private double coinsHandler(String coins){
        coins=coins.replace(" ", "");
        double toReturn;
        try{
            toReturn=Double.parseDouble(coins);
        }catch(Exception x){
            String[] limits = coins.split("-");
            Random rand = new Random();
            toReturn=rand.nextInt(Integer.parseInt(limits[1])-Integer.parseInt(limits[0])+1)+Integer.parseInt(limits[0]);

        }
        return toReturn;
    }

    @EventHandler
    public void killEvent(MythicMobDeathEvent e){
        RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runAsync((task) -> {
            if (e.getKiller() instanceof Player && !RoyaleEconomy.killCoinsMainHandle.disabledWorlds.contains(e.getEntity().getWorld().getName())) {
                String key = e.getMobType().getInternalName();
                Player killer = (Player) e.getKiller();
                ConfigurationSection killCfg = RoyaleEconomy.killCoinsAndPurseDeathCfg.getConfigurationSection("kill-coins.mythic-mobs-events." + key);
                if (killCfg == null)
                    return;
                String permission = killCfg.getString("permission");
                if (permission.equalsIgnoreCase("none") || killer.hasPermission(permission)) {
                    double coins = coinsHandler(killCfg.getString("coins-to-give"));
                    if (coins != 0) {
                        if (RoyaleEconomy.killCoinsMainHandle.killCoins.hasReachedLimit(killer, "mythic-mobs-" + key, coins, true))
                            return;
                        CustomEntityKillCoinsEvent killCoinsEvent = new CustomEntityKillCoinsEvent(killer, e.getEntity(), coins);
                        Bukkit.getPluginManager().callEvent(killCoinsEvent);
                        if (!killCoinsEvent.isCancelled()) {
                            BoostersActive.Booster percent=RoyaleEconomy.boosters.getBoosterPercentKillCoins(killer);
                            double value=0;
                            if(percent!=null){
                                value= BoostersActive.getValueFromPercent(coins, percent.percent);
                                if(value!=0){
                                    PlayerMessageHandler.messageSend(killer, (Utils.chat(RoyaleEconomy.boostersCfg.getString("boosters.kill-coins.message").replace("%booster-name%", percent.boosterName).replace("%amount%", RoyaleEconomy.messageHelper.numberFormat(value)))));
                                }
                            }
                            RoyaleEconomy.dataManager.addMoneyToFile(killer.getUniqueId().toString(), (double) coins+value);
                            if (RoyaleEconomy.killCoinsMainHandle.sendKillMessage && KillCoinsCommand.canReceiveMessages(killer.getUniqueId().toString()))
                                RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runNextTick((task2) -> PlayerMessageHandler.messageSend(killer, (utilsAPI.chat(killer, RoyaleEconomy.killCoinsMainHandle.killMessage.replace("%amount%", RoyaleEconomy.messageHelper.numberFormat(coins)).replace("%entity-display%", killCfg.getString("display-name"))))));
                        }
                    }
                }
            }
        });
    }

}
