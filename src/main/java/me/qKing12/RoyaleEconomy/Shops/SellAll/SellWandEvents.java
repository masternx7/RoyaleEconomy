package me.qKing12.RoyaleEconomy.Shops.SellAll;

import de.tr7zw.changeme.nbtapi.NBTItem;
import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import me.qKing12.RoyaleEconomy.utils.PlayerMessageHandler;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class SellWandEvents implements Listener {

    public SellWandEvents(){
        Bukkit.getPluginManager().registerEvents(this, RoyaleEconomy.plugin);
    }

    @EventHandler(priority= EventPriority.HIGHEST)
    public void onChestClick(PlayerInteractEvent e){
        if (e.getItem()==null || (!e.getAction().equals(Action.RIGHT_CLICK_BLOCK) && !e.getAction().equals(Action.LEFT_CLICK_BLOCK)) || !e.getClickedBlock().getType().equals(Material.CHEST))
            return;
        if(!(Bukkit.getVersion().contains("1.8") && !Bukkit.getVersion().contains("1.21")) && !e.getHand().equals(EquipmentSlot.HAND))
            return;

        if(e.isCancelled()){
            if(e.getItem()!=null && !e.getItem().getType().equals(Material.AIR) && new NBTItem(e.getItem()).getString("RECSellWand").length()>0)
                PlayerMessageHandler.messageSend(e.getPlayer(), SellAllManager.needToClickChest);
        }
        else{
            NBTItem nbtItem=new NBTItem(e.getItem());
            String wandType=nbtItem.getString("RECSellWand");
            if(!wandType.equals("")) {
                SellWand wand=SellWand.getWandByName(wandType);
                if(wand!=null) {
                    e.setCancelled(true);
                    Chest chest = (Chest) e.getClickedBlock().getState();
                    if(wand.sellItems(chest.getInventory(), e.getPlayer())){
                        int uses=nbtItem.getInteger("RECUses");
                        if(uses==1)
                            e.getPlayer().getInventory().removeItem(e.getItem());
                        else if(uses!=0)
                            e.getPlayer().getInventory().setItem(e.getPlayer().getInventory().getHeldItemSlot(), wand.getWand(uses-1));
                    }
                }

            }
        }
    }
}
