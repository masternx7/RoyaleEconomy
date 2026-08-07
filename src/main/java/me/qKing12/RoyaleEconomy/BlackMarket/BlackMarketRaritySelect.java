package me.qKing12.RoyaleEconomy.BlackMarket;

import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import me.qKing12.RoyaleEconomy.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

public class BlackMarketRaritySelect {
    private Inventory inventory;
    private Player player;
    private BlackMarketItem item;
    int page;

    public BlackMarketRaritySelect(Player p, BlackMarketItem item, int page){
        player=p;
        this.item=item;
        this.page=page;
        inventory= Bukkit.createInventory(null, 54, Utils.chat("&5&lItem Editor | Rarity"));
        for(Rarity rarity : Rarity.rarities){
            ItemStack itemToAdd=new ItemStack(Material.BOOK);
            ItemMeta meta= itemToAdd.getItemMeta();
            meta.setDisplayName(Utils.chat("&a"+rarity.name));
            ArrayList<String> lore = new ArrayList<>(rarity.loreAddition);
            lore.add(" ");
            lore.add(Utils.chat("&aClick to select!"));
            meta.setLore(lore);
            itemToAdd.setItemMeta(meta);
            inventory.addItem(itemToAdd);
        }

        p.openInventory(inventory);

        Bukkit.getPluginManager().registerEvents(new ClickListener(), RoyaleEconomy.plugin);
    }

    private class ClickListener implements Listener {
        @EventHandler
        public void onClick(InventoryClickEvent e){
            if(e.getSlot()<0 || e.getCurrentItem()==null)
                return;
            if(e.getInventory().equals(inventory)){
                e.setCancelled(true);
                item.setRarity(Rarity.rarities.get(e.getSlot()));
                player.closeInventory();
            }
        }

        @EventHandler
        public void onClose(InventoryCloseEvent e){
            if(e.getInventory().equals(inventory)){
                HandlerList.unregisterAll(this);
                RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runLater(() -> new BlackMarketItemEditor(player, item, page), 2);
            }
        }
    }
}
