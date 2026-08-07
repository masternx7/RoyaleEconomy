package me.qKing12.RoyaleEconomy.KillCoins;

import me.qKing12.RoyaleEconomy.API.Events.CustomEntityKillCoinsEvent;
import me.qKing12.RoyaleEconomy.API.Events.EntityKillCoinsEvent;
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
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.ArrayList;
import java.util.Random;

import static me.qKing12.RoyaleEconomy.RoyaleEconomy.utilsAPI;


public class KillCoinsMainHandle implements Listener {
    public KillCoinsLimitInterface killCoins;
    String killMessage;
    ArrayList<String> disabledWorlds;
    boolean sendKillMessage=true;

    private ArrayList<KillEntityData> killCoinsEntities = new ArrayList<>();
    private ArrayList<KillEntityData> customKillCoinsEntities = new ArrayList<>();

    private class KillEntityData{
        private String entityType;
        private String key;
        private String permission;
        private String coinsToGive;
        private String displayName;

        public KillEntityData(ConfigurationSection section, String key){
            permission=section.getString("permission");
            displayName=Utils.chat(section.getString("display-name"));
            coinsToGive=section.getString("coins-to-give");
            entityType=section.getString("entity-type");
            this.key=key;
        }
    }


    public KillCoinsMainHandle(){
        if(RoyaleEconomy.killCoinsAndPurseDeathCfg.getBoolean("kill-coins.use-maximum-coins"))
            killCoins=new KillCoinsLimitHandle();
        else
            killCoins=new KillCoinsNoLimitHandle();
        ConfigurationSection section = RoyaleEconomy.killCoinsAndPurseDeathCfg.getConfigurationSection("kill-coins.custom-kill-events");
        if(section!=null)
            for (String key : section.getKeys(false))
                customKillCoinsEntities.add(new KillEntityData(RoyaleEconomy.killCoinsAndPurseDeathCfg.getConfigurationSection("kill-coins.custom-kill-events." + key), key));
        section = RoyaleEconomy.killCoinsAndPurseDeathCfg.getConfigurationSection("kill-coins.kill-events");
        if(section!=null)
            for (String key : section.getKeys(false))
                killCoinsEntities.add(new KillEntityData(RoyaleEconomy.killCoinsAndPurseDeathCfg.getConfigurationSection("kill-coins.kill-events." + key), key));
        Bukkit.getPluginManager().registerEvents(this, RoyaleEconomy.plugin);
        killMessage= RoyaleEconomy.killCoinsAndPurseDeathCfg.getString("kill-coins.message-on-kill");
        disabledWorlds=(ArrayList<String>)RoyaleEconomy.killCoinsAndPurseDeathCfg.getStringList("kill-coins.disabled-worlds");
        if(killMessage.equals(""))
            sendKillMessage=false;
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
    public void killCoins(EntityDeathEvent e){
        RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runAtEntity(e.getEntity(), (task) -> {
            Player killer = e.getEntity().getKiller();
            if(killer!=null && !killer.equals(e.getEntity()) && !disabledWorlds.contains(e.getEntity().getWorld().getName())){
                try {
                    if (MythicMobsHook.isHooked && MythicMobsHook.api.isMythicMob(e.getEntity()))
                        return;
                }catch(Exception x){
                    RoyaleEconomy.plugin.getLogger().warning("There was a problem checking for MythicMobs.");
                    x.printStackTrace();
                }
                for (KillEntityData entityData : customKillCoinsEntities) {
                    if (e.getEntity().getCustomName() != null && e.getEntity().getCustomName().equals(entityData.displayName)) {
                        String permission = entityData.permission;
                        if (permission == null || permission.equalsIgnoreCase("none") || killer.hasPermission(permission)) {
                            double coins = coinsHandler(entityData.coinsToGive);
                            if (coins != 0) {
                                if (killCoins.hasReachedLimit(killer, "custom-kill-" + entityData.key, coins, true))
                                    return;
                                CustomEntityKillCoinsEvent killCoinsEvent = new CustomEntityKillCoinsEvent(killer, e.getEntity(), coins);
                                Bukkit.getPluginManager().callEvent(killCoinsEvent);
                                if (!killCoinsEvent.isCancelled()) {
                                    Utils.playSound(killer, "others.kill-coins-custom-entity-kill");
                                    BoostersActive.Booster percent=RoyaleEconomy.boosters.getBoosterPercentKillCoins(killer);
                                    double value=0;
                                    if(percent!=null){
                                        value= BoostersActive.getValueFromPercent(coins, percent.percent);
                                        if(value!=0){
                                            PlayerMessageHandler.messageSend(killer, (Utils.chat(RoyaleEconomy.boostersCfg.getString("boosters.kill-coins.message").replace("%booster-name%", percent.boosterName).replace("%amount%", RoyaleEconomy.messageHelper.numberFormat(value)))));
                                        }
                                    }
                                    RoyaleEconomy.dataManager.addMoneyToFile(killer.getUniqueId().toString(),  coins+value);
                                    if (sendKillMessage && KillCoinsCommand.canReceiveMessages(killer.getUniqueId().toString()))
                                        RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runNextTick((task2) -> PlayerMessageHandler.messageSend(killer, (utilsAPI.chat(killer, killMessage.replace("%amount%", RoyaleEconomy.messageHelper.numberFormat( coins)).replace("%entity-display%", entityData.displayName)))));
                                }
                            }
                            return;
                        }
                    }
                }

                for (KillEntityData entityData : killCoinsEntities) {
                    if(entityData.entityType.equalsIgnoreCase(e.getEntityType().toString())){
                        String permission = entityData.permission;
                        if (permission == null || permission.equalsIgnoreCase("none") || killer.hasPermission(permission)) {
                            double coins = coinsHandler(entityData.coinsToGive);
                            if (coins != 0) {
                                if (killCoins.hasReachedLimit(killer, entityData.key,  coins, false))
                                    return;
                                EntityKillCoinsEvent killCoinsEvent = new EntityKillCoinsEvent(killer, e.getEntity(), coins);
                                Bukkit.getPluginManager().callEvent(killCoinsEvent);
                                if (!killCoinsEvent.isCancelled()) {
                                    Utils.playSound(killer, "others.kill-coins-entity-kill");
                                    BoostersActive.Booster percent=RoyaleEconomy.boosters.getBoosterPercentKillCoins(killer);
                                    double value=0;
                                    if(percent!=null){
                                        value= BoostersActive.getValueFromPercent(coins, percent.percent);
                                        if(value!=0){
                                            PlayerMessageHandler.messageSend(killer, Utils.chat(RoyaleEconomy.boostersCfg.getString("boosters.kill-coins.message").replace("%booster-name%", percent.boosterName).replace("%amount%", RoyaleEconomy.messageHelper.numberFormat(value))));
                                        }
                                    }
                                    RoyaleEconomy.dataManager.addMoneyToFile(killer.getUniqueId().toString(),  coins+value);
                                    if (sendKillMessage && KillCoinsCommand.canReceiveMessages(killer.getUniqueId().toString()))
                                        RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runNextTick((task2) -> PlayerMessageHandler.messageSend(killer, utilsAPI.chat(killer, killMessage.replace("%amount%", RoyaleEconomy.messageHelper.numberFormat( coins)).replace("%entity-display%", entityData.displayName))));
                                }
                            }
                            return;
                        }
                    }
                }
                String permission = RoyaleEconomy.killCoinsAndPurseDeathCfg.getString("kill-coins.default-permission");
                if(permission == null || permission.equalsIgnoreCase("none") || killer.hasPermission(permission)) {
                    double coins = coinsHandler(RoyaleEconomy.killCoinsAndPurseDeathCfg.getString("kill-coins.default-coins"));
                    if (coins != 0) {
                        if (killCoins.hasReachedLimit(killer, "default", coins, false))
                            return;
                        EntityKillCoinsEvent killCoinsEvent = new EntityKillCoinsEvent(killer, e.getEntity(), coins);
                        Bukkit.getPluginManager().callEvent(killCoinsEvent);
                        if (!killCoinsEvent.isCancelled()) {
                            Utils.playSound(killer, "others.kill-coins-entity-kill");
                            BoostersActive.Booster percent=RoyaleEconomy.boosters.getBoosterPercentKillCoins(killer);
                            double value=0;
                            if(percent!=null){
                                value= BoostersActive.getValueFromPercent(coins, percent.percent);
                                if(value!=0){
                                    PlayerMessageHandler.messageSend(killer, Utils.chat(RoyaleEconomy.boostersCfg.getString("boosters.kill-coins.message").replace("%booster-name%", percent.boosterName).replace("%amount%", RoyaleEconomy.messageHelper.numberFormat(value))));
                                }
                            }
                            RoyaleEconomy.dataManager.addMoneyToFile(killer.getUniqueId().toString(), coins+value);
                            if (sendKillMessage && KillCoinsCommand.canReceiveMessages(killer.getUniqueId().toString()))
                                RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runNextTick((task2) -> PlayerMessageHandler.messageSend(killer, utilsAPI.chat(killer, killMessage.replace("%amount%", RoyaleEconomy.messageHelper.numberFormat(coins)).replace("%entity-display%", RoyaleEconomy.killCoinsAndPurseDeathCfg.getString("kill-coins.default-display-name")))));
                        }
                    }
                }
            }
        });
    }

}
