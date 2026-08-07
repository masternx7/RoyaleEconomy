package me.qKing12.RoyaleEconomy.MultiCurrency.internal;

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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static me.qKing12.RoyaleEconomy.RoyaleEconomy.*;

public class CurrencyExchangeMainMenu {
    private Inventory inventory;
    private static String name;
    private static int slots;
    private static ItemStack backgroundItem;
    private static boolean hideIfNotPermission;
    private static ArrayList<String> noPermissionLore;
    private static ArrayList<String> clickLore;
    private static String currencyName;
    private static ArrayList<String> currencyDescription;
    private static HashMap<Integer, Currency> currencySlots;

    public static void loadConfigData(){
        name=Utils.chat(multiCurrencyCfg.getString("currency-exchange-menu.select-currency-submenu.name"));
        slots= multiCurrencyCfg.getInt("currency-exchange-menu.select-currency-submenu.slots");
        backgroundItem=itemConstructor.getItem(multiCurrencyCfg.getString("currency-exchange-menu.select-currency-submenu.background-item"), " ", new ArrayList<>(Collections.singletonList(" ")));
        hideIfNotPermission= multiCurrencyCfg.getBoolean("currency-exchange-menu.select-currency-submenu.hide-currency-if-not-permission");
        noPermissionLore=new ArrayList<>();
        for(String line : multiCurrencyCfg.getStringList("currency-exchange-menu.select-currency-submenu.no-permission-lore"))
            noPermissionLore.add(Utils.chat(line));

        clickLore=new ArrayList<>();
        for(String line : multiCurrencyCfg.getStringList("currency-exchange-menu.select-currency-submenu.click-lore"))
            clickLore.add(Utils.chat(line));

        currencyName=Utils.chat(multiCurrencyCfg.getString("currency-exchange-menu.select-currency-submenu.currency-name"));
        currencyDescription=new ArrayList<>();
        for(String line : multiCurrencyCfg.getStringList("currency-exchange-menu.select-currency-submenu.currency-description"))
            currencyDescription.add(Utils.chat(line));

        currencySlots=new HashMap<>();
        for(String key : multiCurrencyCfg.getConfigurationSection("currency-exchange-menu.select-currency-submenu.currency-slots").getKeys(false)) {
            Currency curr = MultiCurrencyHandler.findCurrencyById(key);
            if (curr == null)
                continue;

            currencySlots.put(multiCurrencyCfg.getInt("currency-exchange-menu.select-currency-submenu.currency-slots." + key), curr);
        }
    }

    public CurrencyExchangeMainMenu(Player p){
        RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runAsync((task) -> {
            inventory = Bukkit.createInventory(null, slots, utilsAPI.chat(p, name));

            for(int i=0;i<slots;i++)
                inventory.setItem(i, backgroundItem);

            for(Map.Entry<Integer, Currency> entry : currencySlots.entrySet()){
                if(hideIfNotPermission && (!entry.getValue().getExchangePermission().equals("none") && !p.hasPermission(entry.getValue().getExchangePermission())))
                    if(!p.hasPermission(entry.getValue().getBypassExchangePermission()))
                        continue;
                ItemStack toSet=entry.getValue().getIcon().clone();
                ItemMeta meta = toSet.getItemMeta();
                meta.setDisplayName(Utils.chat(currencyName.replace("%currency-color%", entry.getValue().getColor()).replace("%currency-name%", entry.getValue().getCurrencyName())));
                ArrayList<String> lore = new ArrayList<>();
                for(String line : currencyDescription) {
                    double sellValue = entry.getValue().getCoinsReference();
                    double buyValue;
                    if(p.hasPermission(entry.getValue().getBypassExchangePermission()))
                        buyValue=sellValue;
                    else {
                        buyValue = sellValue + sellValue * entry.getValue().getExchangePercent() / 100;
                        if(!entry.getValue().hasDecimals())
                            buyValue=Math.floor(buyValue);
                    }
                    lore.add(line.replace("%sell-value%", RoyaleEconomy.messageHelper.numberFormat(sellValue)).replace("%buy-value%", messageHelper.numberFormat(buyValue)));
                }
                if(p.hasPermission(entry.getValue().getExchangePermission()))
                    lore.addAll(clickLore);
                else
                    lore.addAll(noPermissionLore);
                meta.setLore(lore);
                toSet.setItemMeta(meta);
                inventory.setItem(entry.getKey(), toSet);
            }

            RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runNextTick((task2) -> {
                p.openInventory(inventory);
                Bukkit.getPluginManager().registerEvents(new ClickListener(), RoyaleEconomy.plugin);
            });
        });
    }

    private class ClickListener implements Listener {

        @EventHandler
        public void onClick(InventoryClickEvent e) {
            if (e.getSlot() < 0 || e.getCurrentItem() == null || e.getCurrentItem().getType().equals(Material.AIR)) {
                return;
            }
            //if(inventory.getViewers().isEmpty()) RoyaleEconomy.plugin.getLogger().warning("A closed listener is still active.");
            if (e.getInventory().equals(inventory)) {
                e.setCancelled(true);
                if (!e.getClickedInventory().equals(e.getWhoClicked().getInventory())) {
                    if(currencySlots.containsKey(e.getSlot())){
                        Currency currency = currencySlots.get(e.getSlot());
                        if(currency.getExchangePermission().equals("none") || e.getWhoClicked().hasPermission(currency.getExchangePermission())) {
                            e.getWhoClicked().closeInventory();
                            new CurrencyExchangeMenu((Player)e.getWhoClicked(), currency, e.isLeftClick());
                        }

                    }
                }
            }

        }

        @EventHandler
        public void onClose(InventoryCloseEvent e) {
            if (e.getInventory().equals(inventory)) {
                inventory = null;
                HandlerList.unregisterAll(this);
            }
        }

    }

}
