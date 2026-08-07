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

import static me.qKing12.RoyaleEconomy.RoyaleEconomy.*;

public class CurrencyExchangeSellBuyMenu {
    private Inventory inventory;
    private static String name;
    private static int slots;
    private static ItemStack backgroundItem;

    private static int sellSlot;
    private static ItemStack sellIcon;
    private static String sellIconName;
    private static ArrayList<String> sellIconLore;

    private static int buySlot;
    private static String buyIconName;
    private static ArrayList<String> buyIconLore;
    private static ItemStack buyIcon;

    private static int closeSlot;
    private static ItemStack closeIcon;

    public static void loadConfigData(){
        name = Utils.chat(multiCurrencyCfg.getString("currency-exchange-menu.exchange-currency-submenu.name"));
        slots = multiCurrencyCfg.getInt("currency-exchange-menu.exchange-currency-submenu.slots");
        backgroundItem=itemConstructor.getItem(
            multiCurrencyCfg.getString("currency-exchange-menu.exchange-currency-submenu.background-item"),
            " ",
            new ArrayList<>(Collections.singletonList(" "))
        );

        sellSlot = multiCurrencyCfg.getInt("currency-exchange-menu.exchange-currency-submenu.sell-currency.slot");
        buySlot = multiCurrencyCfg.getInt("currency-exchange-menu.exchange-currency-submenu.buy-currency.slot");

        sellIcon=itemConstructor.getItemFromMaterial(multiCurrencyCfg.getString("currency-exchange-menu.exchange-currency-submenu.sell-currency.item"));
        buyIcon=itemConstructor.getItemFromMaterial(multiCurrencyCfg.getString("currency-exchange-menu.exchange-currency-submenu.buy-currency.item"));

        sellIconName=Utils.chat(multiCurrencyCfg.getString("currency-exchange-menu.exchange-currency-submenu.sell-currency.name"));
        buyIconName=Utils.chat(multiCurrencyCfg.getString("currency-exchange-menu.exchange-currency-submenu.buy-currency.name"));

        sellIconLore=new ArrayList<>();

        for(String line : multiCurrencyCfg.getStringList("currency-exchange-menu.exchange-currency-submenu.sell-currency.lore"))
            sellIconLore.add(Utils.chat(line));

        buyIconLore=new ArrayList<>();
        for(String line : multiCurrencyCfg.getStringList("currency-exchange-menu.exchange-currency-submenu.buy-currency.lore"))
            buyIconLore.add(Utils.chat(line));

        closeSlot=multiCurrencyCfg.getInt("currency-exchange-menu.exchange-currency-submenu.close.slot");
        String closeName=Utils.chat(multiCurrencyCfg.getString("currency-exchange-menu.exchange-currency-submenu.close.name"));
        ArrayList<String> closeLore=new ArrayList<>();
        for(String line : multiCurrencyCfg.getStringList("currency-exchange-menu.exchange-currency-submenu.close.lore"))
            closeLore.add(Utils.chat(line));

        closeIcon=itemConstructor.getItem(multiCurrencyCfg.getString("currency-exchange-menu.exchange-currency-submenu.close.item"), closeName, closeLore);
    }

    private Player player;
    private Currency currency;

    public CurrencyExchangeSellBuyMenu(Player p, Currency currency){
        this.player=p;
        this.currency=currency;
        RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runAsync((task) -> {
            inventory = Bukkit.createInventory(null, slots, utilsAPI.chat(p, name));

            for(int i=0;i<slots;i++)
                inventory.setItem(i, backgroundItem);

            inventory.setItem(closeSlot, closeIcon);

            ItemStack sell = sellIcon.clone();
            ItemMeta meta = sell.getItemMeta();
            meta.setDisplayName(sellIconName
                    .replace("%currency-color%", currency.getColor())
                    .replace("%currency-name%", currency.getCurrencyName())
                    .replace("%value%", currency.formatMoney(currency.getCoinsReference()))
            );

            ArrayList<String> lore = new ArrayList<>();
            for(String line : sellIconLore)
                lore.add(line
                        .replace("%currency-color%", currency.getColor())
                        .replace("%currency-name%", currency.getCurrencyName())
                        .replace("%value%", currency.formatMoney(currency.getCoinsReference()))
                );
            meta.setLore(lore);

            sell.setItemMeta(meta);
            inventory.setItem(sellSlot, sell);


            ItemStack buy = buyIcon.clone();
            meta = buy.getItemMeta();
            meta.setDisplayName(buyIconName
                    .replace("%currency-color%", currency.getColor())
                    .replace("%currency-name%", currency.getCurrencyName())
                    .replace("%value%", currency.formatMoney(currency.getCoinsReference()+currency.calculateBuyFee(currency.getCoinsReference())))
            );

            lore = new ArrayList<>();
            for(String line : buyIconLore)
                lore.add(line
                        .replace("%currency-color%", currency.getColor())
                        .replace("%currency-name%", currency.getCurrencyName())
                        .replace("%value%", currency.formatMoney(currency.getCoinsReference()+currency.calculateBuyFee(currency.getCoinsReference())))
                );
            meta.setLore(lore);

            buy.setItemMeta(meta);
            inventory.setItem(buySlot, buy);


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
                    if(e.getSlot()==closeSlot){
                        player.closeInventory();
                    }
                    else if(e.getSlot()==buySlot){
                        player.closeInventory();
                        new CurrencyExchangeMenu(player, currency, false);
                    }
                    else if(e.getSlot()==sellSlot){
                        player.closeInventory();
                        new CurrencyExchangeMenu(player, currency, true);
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
