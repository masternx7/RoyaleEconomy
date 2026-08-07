package me.qKing12.RoyaleEconomy.Hooks;

import me.qKing12.RoyaleEconomy.DataManager.DataManagerSQL;
import me.qKing12.RoyaleEconomy.DataManager.MySQLLoad;
import me.qKing12.RoyaleEconomy.DataManager.SQLLoad;
import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.stellardev.galacticskyblock.api.SkyBlockAPI;
import org.stellardev.galacticskyblock.event.IslandDeleteEvent;
import org.stellardev.galacticskyblock.event.IslandNewEvent;
import org.stellardev.galacticskyblock.object.Island;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

public class GalacticSkyBlock implements Listener, SharedBankHook {

    @Override
    public String getSharedBankId(String playerUUID) {
        try{
            String id = SkyBlockAPI.getIslandId(playerUUID);
            if(id==null)
                return "";
            else
                return id;
        }catch(Exception x){
            return "";
        }
    }

    @Override
    public ArrayList<String> getMembers(String bankID, boolean owner) {
        try {
            ArrayList<String> toReturn=new ArrayList<>();
            Island island = SkyBlockAPI.getIslandById(bankID);
            if(owner)
                toReturn.add(island.getOwnerUUID().toString());
            for (UUID uuid : island.getIslandMembers())
                toReturn.add(uuid.toString());
            return toReturn;
        }catch(Exception x) {
            return new ArrayList<>();
        }
    }

    @Override
    public String getOwner(String bankId) {
        try{
            return SkyBlockAPI.getIslandById(bankId).getOwnerUUID().toString();
        }catch(Exception x) {
            return "";
        }
    }

    public GalacticSkyBlock(){
        if(RoyaleEconomy.hooked!=null)
            return;
        RoyaleEconomy.hooked=this;
        if(RoyaleEconomy.dataManager instanceof DataManagerSQL)
            SQLLoad.loadSharedBanks("GalacticSkyBlock");
        else
            MySQLLoad.loadSharedBanksCustom("GalacticSkyBlock");
        RoyaleEconomy.dataManager.getSharedBankManager().setTable("GalacticSkyBlock");
        Bukkit.getPluginManager().registerEvents(this, RoyaleEconomy.plugin);
    }

    @EventHandler
    public void islandDeleteEvent(IslandDeleteEvent e){
        RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runAsync((task) -> {
            RoyaleEconomy.dataManager.getSharedBankManager().deleteSharedBank(e.getIsland().getId());
        });
    }

    @EventHandler
    public void islandCreateEvent(IslandNewEvent e) {
        if (e.isAsynchronous())
            RoyaleEconomy.dataManager.getSharedBankManager().createSharedBank(e.getIsland().getId());
        else
            RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runAsync((task) -> {
                RoyaleEconomy.dataManager.getSharedBankManager().createSharedBank(e.getIsland().getId());
            });
    }

}
