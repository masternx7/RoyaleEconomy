package me.qKing12.RoyaleEconomy.Hooks;

import com.craftaro.skyblock.api.SkyBlockAPI;
import com.craftaro.skyblock.api.event.island.IslandCreateEvent;
import com.craftaro.skyblock.api.event.island.IslandDeleteEvent;
import com.craftaro.skyblock.api.island.Island;
import me.qKing12.RoyaleEconomy.DataManager.DataManagerSQL;
import me.qKing12.RoyaleEconomy.DataManager.MySQLLoad;
import me.qKing12.RoyaleEconomy.DataManager.SQLLoad;
import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.UUID;

public class FabledSkyBlock implements Listener, SharedBankHook {

    @Override
    public String getOwner(String bankId) {
        try {
            return SkyBlockAPI.getIslandManager().getIslandByUUID(UUID.fromString(bankId)).getOwnerUUID().toString();
        }catch(Exception x){
            return "";
        }
    }

    @Override
    public String getSharedBankId(String playerUUID) {
        try {
            OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(playerUUID));
            return SkyBlockAPI.getIslandManager().getIsland(player).getIslandUUID().toString();
        }catch(Exception x){
            return "";
        }
    }

    @Override
    public ArrayList<String> getMembers(String bankID, boolean owner) {
        try {
            Island island = SkyBlockAPI.getIslandManager().getIslandByUUID(UUID.fromString(bankID));
            ArrayList<String> toReturn = new ArrayList<>();
            if (owner)
                toReturn.add(island.getOwnerUUID().toString());
            for (UUID uuid : island.getCoopPlayers().keySet())
                toReturn.add(uuid.toString());
            return toReturn;
        }catch(Exception x){
            return new ArrayList<>();
        }
    }

    public FabledSkyBlock(){
        if(RoyaleEconomy.hooked!=null)
            return;
        RoyaleEconomy.hooked=this;

        if(RoyaleEconomy.dataManager instanceof DataManagerSQL)
            SQLLoad.loadSharedBanks("FabledSkyBlock");
        else
            MySQLLoad.loadSharedBanksCustom("FabledSkyBlock");
        RoyaleEconomy.dataManager.getSharedBankManager().setTable("FabledSkyBlock");
        Bukkit.getPluginManager().registerEvents(this, RoyaleEconomy.plugin);
    }

    @EventHandler
    public void islandDeleteEvent(IslandDeleteEvent e){
        RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runAsync((task) -> {
            RoyaleEconomy.dataManager.getSharedBankManager().deleteSharedBank(e.getIsland().getIslandUUID().toString());
        });
    }

    @EventHandler
    public void islandCreateEvent(IslandCreateEvent e) {
        if (e.isAsynchronous())
            RoyaleEconomy.dataManager.getSharedBankManager().createSharedBank(e.getIsland().getIslandUUID().toString());
        else
            RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runAsync((task) -> {
                RoyaleEconomy.dataManager.getSharedBankManager().createSharedBank(e.getIsland().getIslandUUID().toString());
            });
    }
}
