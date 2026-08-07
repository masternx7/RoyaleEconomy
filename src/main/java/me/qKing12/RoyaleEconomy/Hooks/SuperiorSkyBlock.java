package me.qKing12.RoyaleEconomy.Hooks;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.events.*;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import me.qKing12.RoyaleEconomy.DataManager.DataManagerSQL;
import me.qKing12.RoyaleEconomy.DataManager.MySQLLoad;
import me.qKing12.RoyaleEconomy.DataManager.SQLLoad;
import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.UUID;

public class SuperiorSkyBlock implements Listener, SharedBankHook {

    @Override
    public String getOwner(String bankId) {
        try{
            return SuperiorSkyblockAPI.getIslandByUUID(UUID.fromString(bankId)).getOwner().getUniqueId().toString();
        }catch(Exception x){
            return "";
        }
    }

    @Override
    public String getSharedBankId(String playerUUID) {
        try {
            return SuperiorSkyblockAPI.getPlayer(UUID.fromString(playerUUID)).getIsland().getUniqueId().toString();
        }catch(Exception x){
            return "";
        }
    }

    @Override
    public ArrayList<String> getMembers(String bankID, boolean owner) {
        try{
            ArrayList<String> toReturn=new ArrayList<>();
            Island island = SuperiorSkyblockAPI.getIslandByUUID(UUID.fromString(bankID));
            for(SuperiorPlayer player : island.getIslandMembers(owner))
                toReturn.add(player.getUniqueId().toString());
            return toReturn;
        }catch (Exception x){
            return new ArrayList<>();
        }
    }

    public SuperiorSkyBlock(){
        if(RoyaleEconomy.hooked!=null)
            return;
        RoyaleEconomy.hooked=this;

        if(RoyaleEconomy.dataManager instanceof DataManagerSQL)
            SQLLoad.loadSharedBanks("SuperiorSkyBlock");
        else
            MySQLLoad.loadSharedBanksCustom("SuperiorSkyBlock");
        RoyaleEconomy.dataManager.getSharedBankManager().setTable("SuperiorSkyBlock");

        Bukkit.getPluginManager().registerEvents(this, RoyaleEconomy.plugin);
    }

    @EventHandler
    public void islandDeleteEvent(IslandDisbandEvent e){
        RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runAsync((task) -> {
            RoyaleEconomy.dataManager.getSharedBankManager().deleteSharedBank(e.getIsland().getUniqueId().toString());
        });
    }

    @EventHandler
    public void islandCreateEvent(IslandCreateEvent e){
        Player p = e.getPlayer().asPlayer();
        if(p!=null)
            if(e.isAsynchronous())
                RoyaleEconomy.dataManager.getSharedBankManager().createSharedBank(e.getIsland().getUniqueId().toString());
            else
                RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runAsync((task) -> {
                    RoyaleEconomy.dataManager.getSharedBankManager().createSharedBank(e.getIsland().getUniqueId().toString());
                });
    }

}
