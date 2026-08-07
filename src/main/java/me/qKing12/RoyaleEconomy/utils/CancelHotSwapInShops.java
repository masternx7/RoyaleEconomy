package me.qKing12.RoyaleEconomy.utils;

import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

public class CancelHotSwapInShops implements Listener {

    private final Player player;
//    private final Inventory inventory;

    public CancelHotSwapInShops(Player player) {
        this.player = player;
//        this.inventory = inventory;
        Bukkit.getPluginManager().registerEvents(this, RoyaleEconomy.plugin);
    }

    public void cancel(){
        HandlerList.unregisterAll(this);
    }

    @EventHandler
    public void onSwap(PlayerSwapHandItemsEvent event) {
        if (player.equals(event.getPlayer())) {
//            if (inventory.getViewers().isEmpty()){
//                cancel();
//                return;
//            }

            event.setCancelled(true);
            RoyaleEconomy.plugin.getLogger().info("[Shop DUPE Attempt] " + player.getName() + " had tried a offhand swap with shop open and is suspected of item dupe attempt!");
        }
    }

    @EventHandler
    public void onInventory(PlayerInteractEvent event){
        if (player.equals(event.getPlayer())) {
            event.setCancelled(true);
           // RoyaleEconomy.plugin.getLogger().info("[Shop DUPE Attempt] " + player.getName() + " had tried an inventory interact event with shop open and is suspected of item dupe attempt!");
        }
    }

//    @EventHandler
//    public void onEntity(PlayerInteractEntityEvent event){
//        if (player.equals(event.getPlayer())) {
//            event.setCancelled(true);
//            RoyaleEconomy.plugin.getLogger().info("[Shop DUPE Attempt] " + player.getName() + " had tried a entity interact event with shop open and is suspected of item dupe attempt!");
//        }
//    }
}
