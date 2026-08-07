package me.qKing12.RoyaleEconomy.Hooks;

import me.qKing12.RoyaleEconomy.DataManager.DataManagerSQL;
import me.qKing12.RoyaleEconomy.DataManager.MySQLLoad;
import me.qKing12.RoyaleEconomy.DataManager.SQLLoad;
import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import world.bentobox.bentobox.api.events.island.IslandCreatedEvent;
import world.bentobox.bentobox.api.events.island.IslandDeleteEvent;
import world.bentobox.bentobox.database.objects.Island;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

public class BentoBox implements Listener, SharedBankHook {

    @Override
    public String getSharedBankId(String playerUUID) {
        UUID uuid = UUID.fromString(playerUUID);

        for(World _world : world.bentobox.bentobox.BentoBox.getInstance().getIWM().getOverWorlds())
            if(world.bentobox.bentobox.BentoBox.getInstance().getIslandsManager().getPrimaryIsland(_world, uuid) != null)
                return world.bentobox.bentobox.BentoBox.getInstance().getIslandsManager().getPrimaryIsland(_world, uuid).getUniqueId();

        return "";
    }

    @Override
    public ArrayList<String> getMembers(String bankID, boolean owner) {
        Optional<Island> island = world.bentobox.bentobox.BentoBox.getInstance().getIslands().getIslandById(bankID);
        if(island.isPresent()){
            Island toWork = island.get();
            ArrayList<String> toReturn=new ArrayList<>();
            if(owner)
                toReturn.add(toWork.getOwner().toString());
            for(UUID member : toWork.getMemberSet())
                toReturn.add(member.toString());

            return toReturn;
        }
        return new ArrayList<>();
    }

    @Override
    public String getOwner(String bankId) {
        try {
            Optional<Island> island = world.bentobox.bentobox.BentoBox.getInstance().getIslands().getIslandById(bankId);
            return island.map(value -> value.getOwner().toString()).orElse("");
        }catch(Exception x){
            return "";
        }
    }

    public BentoBox(){
        if(RoyaleEconomy.hooked!=null)
            return;
        RoyaleEconomy.hooked=this;
        if(RoyaleEconomy.dataManager instanceof DataManagerSQL)
            SQLLoad.loadSharedBanks("BentoBox");
        else
            MySQLLoad.loadSharedBanksCustom("BentoBox");
        RoyaleEconomy.dataManager.getSharedBankManager().setTable("BentoBox");
        Bukkit.getPluginManager().registerEvents(this, RoyaleEconomy.plugin);
    }

    @EventHandler
    public void islandDeleteEvent(IslandDeleteEvent e){
        RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runAsync((task) -> {
            RoyaleEconomy.dataManager.getSharedBankManager().deleteSharedBank(e.getIsland().getUniqueId());
        });
    }

    @EventHandler
    public void islandCreateEvent(IslandCreatedEvent e) {
        if (e.isAsynchronous())
            RoyaleEconomy.dataManager.getSharedBankManager().createSharedBank(e.getIsland().getUniqueId());
        else
            RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runAsync((task) -> {
                RoyaleEconomy.dataManager.getSharedBankManager().createSharedBank(e.getIsland().getUniqueId());
            });
    }

}
