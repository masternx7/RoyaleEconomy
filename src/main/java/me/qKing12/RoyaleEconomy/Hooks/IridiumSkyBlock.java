package me.qKing12.RoyaleEconomy.Hooks;

import com.iridium.iridiumskyblock.IridiumSkyblock;
import com.iridium.iridiumskyblock.api.IridiumSkyblockAPI;
import com.iridium.iridiumskyblock.api.IslandCreateEvent;
import com.iridium.iridiumskyblock.api.IslandDeleteEvent;
import com.iridium.iridiumskyblock.database.Island;
import com.iridium.iridiumskyblock.database.User;
import me.qKing12.RoyaleEconomy.DataManager.DataManagerSQL;
import me.qKing12.RoyaleEconomy.DataManager.MySQLLoad;
import me.qKing12.RoyaleEconomy.DataManager.SQLLoad;
import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

public class IridiumSkyBlock implements Listener, SharedBankHook {

    @Override
    public String getSharedBankId(String playerUUID) {
        try {
            return String.valueOf(IridiumSkyblock.getInstance().getUserManager().getUserByUUID(UUID.fromString(playerUUID)).get().getIsland().get().getId());
        }catch(Exception x) {
            return "";
        }
    }

    @Override
    public ArrayList<String> getMembers(String bankID, boolean owner) {
        try {
            Optional<Island> island = IridiumSkyblock.getInstance().getIslandManager().getIslandById(Integer.parseInt(bankID));
            if (island.isPresent()) {
                Island toWork = island.get();
                ArrayList<String> toReturn = new ArrayList<>();
                if (owner)
                    toReturn.add(toWork.getOwner().getUuid().toString());
                for (User member : toWork.getMembers())
                    toReturn.add(member.getUuid().toString());

                return toReturn;
            }
            return new ArrayList<>();
        }catch(Exception x) {
            return new ArrayList<>();
        }
    }

    @Override
    public String getOwner(String bankId) {
        try {
            Optional<Island> island = IridiumSkyblock.getInstance().getIslandManager().getIslandById(Integer.parseInt(bankId));
            return island.map(value -> value.getOwner().getUuid().toString()).orElse("");
        }catch (Exception x){
            return "";
        }
    }

    public IridiumSkyBlock(){
        if(RoyaleEconomy.hooked!=null)
            return;
        RoyaleEconomy.hooked=this;
        if(RoyaleEconomy.dataManager instanceof DataManagerSQL)
            SQLLoad.loadSharedBanks("IridiumSkyBlock");
        else
            MySQLLoad.loadSharedBanksCustom("IridiumSkyBlock");
        RoyaleEconomy.dataManager.getSharedBankManager().setTable("IridiumSkyBlock");
        Bukkit.getPluginManager().registerEvents(this, RoyaleEconomy.plugin);
    }

    @EventHandler
    public void islandDeleteEvent(IslandDeleteEvent e){
        RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runAsync((task) -> {
            RoyaleEconomy.dataManager.getSharedBankManager().deleteSharedBank(String.valueOf(e.getIsland().getId()));
        });
    }

}
