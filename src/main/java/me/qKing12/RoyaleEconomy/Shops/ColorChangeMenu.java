package me.qKing12.RoyaleEconomy.Shops;

import me.qKing12.RoyaleEconomy.InputGUIs.ChatListener;
import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import me.qKing12.RoyaleEconomy.utils.PlayerMessageHandler;
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



public class ColorChangeMenu  {
    private Inventory inventory;
    private Shop shop;
    private int page;

    private boolean customModelData=false;

    public ColorChangeMenu(Player p, Shop shop, int page){
        this.shop=shop;
        this.page=page;
        inventory = Bukkit.createInventory(null, 18, Utils.chat("&8Change Background Color"));
        ArrayList<String> lore = new ArrayList<>();
        lore.add(Utils.chat("&fClick this glass pane"));
        lore.add(Utils.chat("&fto set it as background"));
        lore.add(Utils.chat("&fto shop &a")+shop.getShopName());

        if(RoyaleEconomy.upperVersion && !Bukkit.getVersion().contains("1.13")) {
            customModelData = true;
            ItemStack itemStack = new ItemStack(Material.PAPER);
            ItemMeta meta = itemStack.getItemMeta();
            meta.setDisplayName(Utils.chat("&aChange Custom Model Data"));
            ArrayList<String> lore2=new ArrayList<>();
            lore2.add(Utils.chat("&fClick to enter a number"));
            lore2.add(Utils.chat("&fthat represents the model data."));
            meta.setLore(lore);
            itemStack.setItemMeta(meta);
            inventory.setItem(17, itemStack);
        }

        for(int i=0;i<16;i++){
            inventory.setItem(i, RoyaleEconomy.itemConstructor.getItem("160:"+i, Utils.chat("&aChange Color"), lore));
        }

        RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runNextTick((task) -> p.openInventory(inventory));

        Bukkit.getPluginManager().registerEvents(new ClickListener(), RoyaleEconomy.plugin);
    }

    private class ClickListener implements Listener {

        @EventHandler
        public void onClick(InventoryClickEvent e){
            if(e.getSlot()<0 || e.getCurrentItem()==null || e.getCurrentItem().getType().equals(Material.AIR))
                return;
            if(e.getInventory().equals(inventory)){
                e.setCancelled(true);
                if(!e.getClickedInventory().equals(e.getWhoClicked().getInventory())){
                    Player p = (Player) e.getWhoClicked();
                    if(e.getSlot()==17 && customModelData){
                        p.closeInventory();
                        PlayerMessageHandler.messageSend(p, Utils.chat("&7&m-------------------------"));
                        PlayerMessageHandler.messageSend(p, Utils.chat("Please enter the &acustom model data&f!"));
                        new ChatListener(p, (reply) -> {
                            try {
                                int amount = Integer.parseInt(reply);
                                shop.setModelData(amount);
                                PlayerMessageHandler.messageSend(p, Utils.chat("&aCustom Model Data Updated!"));
                                new ShopEditorMenu(p, shop, page);
                            } catch (Exception x) {
                                PlayerMessageHandler.messageSend(p, Utils.chat("&cNumber is not valid"));
                            }
                        });
                    }
                    else {
                        shop.setBackgroundGlass(e.getSlot());
                        PlayerMessageHandler.messageSend(p, Utils.chat("&aColor updated!"));
                        new ShopEditorMenu(p, shop, page);
                    }
                }
            }
        }

        @EventHandler
        public void onClose(InventoryCloseEvent e){
            if(e.getInventory().equals(inventory)){
                inventory=null;
                HandlerList.unregisterAll(this);
            }
        }

    }
}
