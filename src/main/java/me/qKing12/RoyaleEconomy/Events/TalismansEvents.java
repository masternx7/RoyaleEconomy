package me.qKing12.RoyaleEconomy.Events;

import de.tr7zw.changeme.nbtapi.NBTItem;
import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import me.qKing12.RoyaleEconomy.Menus.TalismanUpgrade;
import me.qKing12.RoyaleEconomy.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;


public class TalismansEvents implements Listener {

    public TalismansEvents(){
        if(!RoyaleEconomy.coinBagsAndTalismansCfg.getBoolean("talismans.talismans-drop"))
            Bukkit.getPluginManager().registerEvents(new dropHandler(), RoyaleEconomy.plugin);
        Bukkit.getPluginManager().registerEvents(new upgradeHandler(), RoyaleEconomy.plugin);
    }

    private class upgradeHandler implements Listener{
        @EventHandler
        public void onTalismanClick(PlayerInteractEvent e){
            if(e.getItem()==null || e.getItem().getType().equals(Material.AIR))
                return;
            if(e.getAction().equals(Action.RIGHT_CLICK_BLOCK) || e.getAction().equals(Action.RIGHT_CLICK_AIR)){
                NBTItem nbt = new NBTItem(e.getItem());
                if(!nbt.getString("RoyaleEconomyTalisman").equals("")) {
                    e.setCancelled(true);
                    if(!(Bukkit.getVersion().contains("1.8") && !Bukkit.getVersion().contains("1.21")) && e.getHand().equals(EquipmentSlot.OFF_HAND)){
                        return;
                    }
                    if (nbt.getInteger("RoyaleEconomyNextUpgrade") != 0) {
                        Utils.playSound(e.getPlayer(), "menus.talisman-upgrade");
                        new TalismanUpgrade(e.getPlayer(), e.getPlayer().getInventory().getHeldItemSlot(), e.getItem());
                    }
                }
            }
        }
    }

    private class dropHandler implements Listener{
        private ConcurrentHashMap<Player, ArrayList<ItemStack>> talismanHandler = new ConcurrentHashMap<>();

        @EventHandler
        public void onDeath(PlayerDeathEvent e){
            if (e.getKeepInventory())
                return;

            ArrayList<ItemStack> toGive = new ArrayList<>();
            Iterator<ItemStack> it = e.getDrops().iterator();
            while(it.hasNext()){
                ItemStack next = it.next();
                if(!new NBTItem(next).getString("RoyaleEconomyTalisman").equalsIgnoreCase("")){
                    it.remove();
                    toGive.add(next);
                }
            }
            talismanHandler.put(e.getEntity(), toGive);
        }

        @EventHandler
        public void onRespawn(PlayerRespawnEvent e){
            if(talismanHandler.containsKey(e.getPlayer())){
                for(ItemStack item : talismanHandler.get(e.getPlayer()))
                    e.getPlayer().getInventory().addItem(item);
                talismanHandler.remove(e.getPlayer());
            }
        }
    }

}
