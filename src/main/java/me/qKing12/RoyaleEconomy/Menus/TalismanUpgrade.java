package me.qKing12.RoyaleEconomy.Menus;

import de.tr7zw.changeme.nbtapi.NBTItem;
import me.qKing12.RoyaleEconomy.Commands.Talismans;
import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import me.qKing12.RoyaleEconomy.utils.PlayerMessageHandler;
import me.qKing12.RoyaleEconomy.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

import static me.qKing12.RoyaleEconomy.RoyaleEconomy.*;



public class TalismanUpgrade {
    private Player p;
    private int slot;
    private Inventory inventory;
    private ItemStack toTalisman;
    private ItemStack upgradeItem;
    private Double cost;

    public TalismanUpgrade(Player p, int slot, ItemStack fromTalisman){
        this.p=p;
        this.slot=slot;
        NBTItem nbt = new NBTItem(fromTalisman);
        ConfigurationSection toTalismanCfg = coinBagsAndTalismansCfg.getConfigurationSection("talismans."+nbt.getString("RoyaleEconomyTalisman")+"."+nbt.getInteger("RoyaleEconomyNextUpgrade"));

        cost=toTalismanCfg.getDouble("upgrade-cost");

        switch(nbt.getString("RoyaleEconomyTalisman")){
            case "random-coins-talisman":
                toTalisman=Talismans.randomCoinsTalismans.get(nbt.getInteger("RoyaleEconomyNextUpgrade"));
                break;
            case "percent-reducer-talisman":
                toTalisman=Talismans.purseSaver.get(nbt.getInteger("RoyaleEconomyNextUpgrade"));
                break;
            default:
                toTalisman=Talismans.deathSaver.get(nbt.getInteger("RoyaleEconomyNextUpgrade"));
        }


        inventory = Bukkit.createInventory(null, InventoryType.HOPPER, utilsAPI.chat(p, coinBagsAndTalismansCfg.getString("talismans.upgrade-menu.title")));

        inventory.setItem(0, fromTalisman);
        inventory.setItem(4, toTalisman);

        upgradeItem= Utils.getSkull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTliZjMyOTJlMTI2YTEwNWI1NGViYTcxM2FhMWIxNTJkNTQxYTFkODkzODgyOWM1NjM2NGQxNzhlZDIyYmYifX19");
        ItemMeta meta = upgradeItem.getItemMeta();
        meta.setDisplayName(utilsAPI.chat(p, coinBagsAndTalismansCfg.getString("talismans.upgrade-menu.middle-item.name")));
        ArrayList<String> lore = new ArrayList<>();
        for(String line : coinBagsAndTalismansCfg.getStringList("talismans.upgrade-menu.middle-item.lore"))
            lore.add(utilsAPI.chat(p, line.replace("%fromTalisman%", fromTalisman.getItemMeta().getDisplayName()).replace("%toTalisman%", toTalisman.getItemMeta().getDisplayName()).replace("%cost%", messageHelper.numberFormat(cost))));
        meta.setLore(lore);
        upgradeItem.setItemMeta(meta);

        inventory.setItem(2, upgradeItem);

        RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runNextTick((task) -> {
            p.openInventory(inventory);
            Bukkit.getPluginManager().registerEvents(new ClickListener(), RoyaleEconomy.plugin);
        });
    }

    private class ClickListener implements Listener {

        @EventHandler
        public void onClick(InventoryClickEvent e){
            if(!e.getInventory().equals(inventory))
                return;
            e.setCancelled(true);
            if(e.getSlot()<0 || e.getCurrentItem()==null || e.getCurrentItem().getType().equals(Material.AIR)) {
                return;
            }
            //if(inventory.getViewers().isEmpty()) RoyaleEconomy.plugin.getLogger().warning("A closed listener is still active.");
                if(!e.getClickedInventory().equals(e.getWhoClicked().getInventory())){
                    Player p = (Player) e.getWhoClicked();
                    if(e.getCurrentItem().equals(toTalisman)){
                        if(dataManager.removeMoneyFromFile(p.getUniqueId().toString(), cost)){
                            p.getInventory().setItem(slot, toTalisman);
                            p.closeInventory();
                            PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, coinBagsAndTalismansCfg.getString("talismans.upgrade-menu.upgraded")));
                        }
                        else{
                            PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, coinBagsAndTalismansCfg.getString("talismans.upgrade-menu.not-enough-coins")));
                            p.closeInventory();
                        }
                    }
                }
        }

        @EventHandler
        public void onClose(InventoryCloseEvent e){
            if(e.getInventory().equals(inventory)){
                inventory=null;
                toTalisman=null;
                upgradeItem=null;
                cost=null;
                p=null;
                HandlerList.unregisterAll(this);
            }
        }

    }
}
