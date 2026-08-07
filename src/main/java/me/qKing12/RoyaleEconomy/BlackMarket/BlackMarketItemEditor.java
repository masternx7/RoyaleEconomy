package me.qKing12.RoyaleEconomy.BlackMarket;

import me.qKing12.RoyaleEconomy.InputGUIs.ChatListener;
import me.qKing12.RoyaleEconomy.MultiCurrency.internal.Currency;
import me.qKing12.RoyaleEconomy.MultiCurrency.internal.MultiCurrencyHandler;
import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import me.qKing12.RoyaleEconomy.utils.PlayerMessageHandler;
import me.qKing12.RoyaleEconomy.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

public class BlackMarketItemEditor {
    private BlackMarketItem item;
    int page;
    Player player;
    Inventory inventory;

    public BlackMarketItemEditor(Player p, BlackMarketItem item, int page) {
        this.item = item;
        this.page = page;
        this.player = p;

        inventory = Bukkit.createInventory(null, 54, Utils.chat("&5&lBlack Market | Edit Item"));

        ItemStack background = RoyaleEconomy.itemConstructor.getItemFromMaterial("160:15");
        ItemMeta meta = background.getItemMeta();

        meta.setDisplayName(" ");
        background.setItemMeta(meta);

        for (int i = 0; i < 54; i++)
            inventory.setItem(i, background);

        ItemStack toSet;
        if(item.itemStack == null)
            toSet = new ItemStack(Material.BARRIER);
        else
            toSet = item.itemStack.clone();
        ArrayList<String> lore = new ArrayList<>();
        if (toSet.getItemMeta().getLore() != null)
            lore = (ArrayList<String>) toSet.getItemMeta().getLore();

        lore.add(Utils.chat("&8&m----------------------"));
        lore.add(Utils.chat("&fClick an item in your"));
        lore.add(Utils.chat("&finventory to set it here."));
        meta = toSet.getItemMeta();
        meta.setLore(lore);
        toSet.setItemMeta(meta);
        inventory.setItem(13, toSet);

        ItemStack setPrice = new ItemStack(Material.GOLD_NUGGET);
        meta = setPrice.getItemMeta();
        meta.setDisplayName(Utils.chat("&fPrice: &6" + RoyaleEconomy.messageHelper.numberFormat(item.price)));
        lore = new ArrayList<>();
        lore.add("");
        lore.add(Utils.chat("&fClick to edit the"));
        lore.add(Utils.chat("&eprice &fof this item!"));
        meta.setLore(lore);
        setPrice.setItemMeta(meta);
        inventory.setItem(29, setPrice);

        ItemStack setStock = new ItemStack(Material.CHEST);
        meta = setStock.getItemMeta();
        meta.setDisplayName(Utils.chat("&fStock: &e" + RoyaleEconomy.messageHelper.numberFormat((double) item.maximumStock)));
        lore = new ArrayList<>();
        lore.add("");
        lore.add(Utils.chat("&fClick to edit the"));
        lore.add(Utils.chat("&estock &fof this item!"));
        meta.setLore(lore);
        setStock.setItemMeta(meta);
        inventory.setItem(33, setStock);

        ItemStack goBack = new ItemStack(Material.ARROW);
        meta = goBack.getItemMeta();
        meta.setDisplayName(Utils.chat("&cGo Back"));
        lore = new ArrayList<>();
        lore.add("");
        lore.add(Utils.chat("&7Click to go back."));
        meta.setLore(lore);
        goBack.setItemMeta(meta);
        inventory.setItem(49, goBack);

        ItemStack rarity = new ItemStack(Material.BOOK);
        meta = rarity.getItemMeta();
        meta.setDisplayName(Utils.chat("&fRarity: &a" + item.rarity.name));
        lore = new ArrayList<>();
        lore.add(Utils.chat("&7Rarity Chance: &e" + String.format("%,.2f", item.rarity.chance)));
        lore.addAll(item.rarity.loreAddition);
        lore.add(Utils.chat("&8&m--------------------"));
        lore.add(Utils.chat("&fClick to change the"));
        lore.add(Utils.chat("&crarity &fof this item!"));
        meta.setLore(lore);
        rarity.setItemMeta(meta);
        inventory.setItem(31, rarity);

        ItemStack delete = new ItemStack(Material.BARRIER);
        meta = delete.getItemMeta();
        meta.setDisplayName(Utils.chat("&cDelete Item"));
        lore = new ArrayList<>();
        lore.add("");
        lore.add(Utils.chat("&bDouble Click &fto delete this"));
        lore.add(Utils.chat("&fitem. If the item is"));
        lore.add(Utils.chat("&fselected in &5black market"));
        lore.add(Utils.chat("&fthe item will get fully deleted"));
        lore.add(Utils.chat("&fon stock refresh!"));
        meta.setLore(lore);
        delete.setItemMeta(meta);
        inventory.setItem(0, delete);

        ItemStack commands = RoyaleEconomy.itemConstructor.getItemFromMaterial("137");
        meta = commands.getItemMeta();
        meta.setDisplayName(Utils.chat("&cCommands"));
        lore = new ArrayList<>();
        lore.add(Utils.chat("&fMake item execute commands"));
        lore.add(Utils.chat("&fon buy. Click to edit"));
        lore.add(Utils.chat("&fthe commands."));
        lore.add("");
        lore.add(Utils.chat("&fCurrent Commands: "));
        ArrayList<String> commandsToExecute = item.getCommands();
        if (commandsToExecute == null) {
            lore.add(Utils.chat("&c  None"));
        } else {
            for (String command : commandsToExecute)
                lore.add(Utils.chat("&8 - &7" + command));
        }
        meta.setLore(lore);
        commands.setItemMeta(meta);
        inventory.setItem(21, commands);

        ItemStack scam = new ItemStack(Material.ANVIL);
        meta = scam.getItemMeta();
        meta.setDisplayName(Utils.chat("&cScam Chance: " + item.scamChance + "%"));
        lore = new ArrayList<>();
        lore.add("");
        lore.add(Utils.chat("&bClick &fto edit the scam chance"));
        lore.add(Utils.chat("&fof this specific item."));
        lore.add(Utils.chat("&fIt will overwrite the default"));
        lore.add(Utils.chat("&fscam chance! Set to -1 for"));
        lore.add(Utils.chat("&fdefault setting!"));
        if(!BlackMarket.useScam){
            lore.add("");
            lore.add(Utils.chat("&cTo use this setting"));
            lore.add(Utils.chat("&cenable scam chance in"));
            lore.add(Utils.chat("&cblackMarket.yml!"));
        }
        meta.setLore(lore);
        scam.setItemMeta(meta);
        inventory.setItem(23, scam);

        if(MultiCurrencyHandler.getCurrencies()!=null) {
            lore.clear();
            ItemStack currencies;
            if(item.currency==null){
                currencies= MultiCurrencyHandler.getDefaultCoinsIcon();
                lore.add(Utils.chat("&fChange the currency for"));
                lore.add(Utils.chat("&fthis item."));
                lore.add("");
                lore.add(Utils.chat("&fCurrently selected:"));
                lore.add(MultiCurrencyHandler.getDefaultCoinsColor()+MultiCurrencyHandler.getDefaultCoinsName());
            }
            else{
                currencies= item.currency.getIcon();
                lore.add(Utils.chat("&fChange the currency for"));
                lore.add(Utils.chat("&fthis item."));
                lore.add("");
                lore.add(Utils.chat("&fCurrently selected:"));
                lore.add(item.currency.getColor()+item.currency.getCurrencyName());
            }
            lore.add("");
            lore.add(Utils.chat("&fClick to change!"));
            meta=currencies.getItemMeta();
            meta.setLore(lore);
            meta.setDisplayName(Utils.chat("&aChange currency"));
            currencies.setItemMeta(meta);
            inventory.setItem(22, currencies);
        }


        player.openInventory(inventory);

        Bukkit.getPluginManager().registerEvents(new ClickListener(), RoyaleEconomy.plugin);
    }

    private class ClickListener implements Listener {
        @EventHandler
        public void onClick(InventoryClickEvent e){
            if(e.getSlot()<0 || e.getCurrentItem()==null || e.getCurrentItem().getType().equals(Material.AIR))
                return;
            if(e.getInventory().equals(inventory)){
                e.setCancelled(true);
                if(e.getClickedInventory().equals(inventory)){
                    if(e.getSlot()==49)
                        new BlackMarketItemsManagerMenu(player, page);
                    else if(e.getSlot()==0 && e.getClick().equals(ClickType.DOUBLE_CLICK)){
                        item.delete();
                        new BlackMarketItemsManagerMenu(player, page);
                    }
                    else if(e.getSlot()==22 && MultiCurrencyHandler.getCurrencies()!=null){
                        player.closeInventory();
                        PlayerMessageHandler.messageSend(player, Utils.chat("&7&m-------------------------"));
                        PlayerMessageHandler.messageSend(player, Utils.chat("Please enter the &acurrency id&f, not the name!"));
                        new ChatListener(player, (reply) -> {
                            try {
                                Currency currency = MultiCurrencyHandler.findCurrencyById(reply);
                                if(currency == null)
                                    throw new NullPointerException();

                                item.setCurrency(currency);
                                PlayerMessageHandler.messageSend(player, Utils.chat("&aCurrency updated!"));
                                RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runNextTick((task) -> new BlackMarketItemEditor(player, item, page));
                            } catch (Exception x) {
                                PlayerMessageHandler.messageSend(player, Utils.chat("&cCurrency is not valid"));
                            }
                        }, false);
                    }
                    else if(e.getSlot()==29){
                        player.closeInventory();
                        PlayerMessageHandler.messageSend(player, Utils.chat("&7&m-------------------------"));
                        PlayerMessageHandler.messageSend(player, Utils.chat("Please enter the &aprice&f!"));
                        new ChatListener(player, (reply) -> {
                            reply = reply.replaceAll("[^A-Za-z0-9.,-]+", "");
                            try {
                                double amount = Math.abs(RoyaleEconomy.messageHelper.getCoinsFromFormat(reply));
                                item.setPrice(amount);
                                PlayerMessageHandler.messageSend(player, Utils.chat("&aBuying value updated!"));
                                RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runNextTick((task) -> new BlackMarketItemEditor(player, item, page));
                            } catch (Exception x) {
                                PlayerMessageHandler.messageSend(player, Utils.chat("&cAmount is not valid"));
                            }
                        }, false);
                    }
                    else if(e.getSlot()==21){
                        new BlackMarketItemCommandsMenu(player, item, page);
                    }
                    else if(e.getSlot()==23 && BlackMarket.useScam){
                        player.closeInventory();
                        PlayerMessageHandler.messageSend(player, Utils.chat("&7&m-------------------------"));
                        PlayerMessageHandler.messageSend(player, Utils.chat("Please enter the &ascam chance&f!"));
                        PlayerMessageHandler.messageSend(player, Utils.chat("If you set it to -1, it will use the default chance!"));
                        new ChatListener(player, (reply) -> {
                            reply = reply.replaceAll("[^A-Za-z0-9.,-]+", "");
                            try {
                                int amount = Math.abs(Integer.parseInt(reply));
                                item.setScamChance(amount);
                                PlayerMessageHandler.messageSend(player, Utils.chat("&aScam chance updated!"));
                                RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runNextTick((task) -> new BlackMarketItemEditor(player, item, page));
                            } catch (Exception x) {
                                PlayerMessageHandler.messageSend(player, Utils.chat("&cAmount is not valid"));
                            }
                        }, false);
                    }
                    else if(e.getSlot()==31){
                        new BlackMarketRaritySelect(player, item, page);
                    }
                    else if(e.getSlot()==33){
                        player.closeInventory();
                        PlayerMessageHandler.messageSend(player, Utils.chat("&7&m-------------------------"));
                        PlayerMessageHandler.messageSend(player, Utils.chat("Please enter the &astock&f!"));
                        PlayerMessageHandler.messageSend(player, Utils.chat("If you set it to 0, the stock is &cdisabled&f!"));
                        new ChatListener(player, (reply) -> {
                            reply = reply.replaceAll("[^A-Za-z0-9.,-]+", "");
                            try {
                                int amount = Math.abs(Integer.parseInt(reply));
                                item.setMaximumStock(amount);
                                if(item.maximumStock<item.stock)
                                    item.stock=item.maximumStock;
                                PlayerMessageHandler.messageSend(player, Utils.chat("&aStock value updated!"));
                                RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runNextTick((task) -> new BlackMarketItemEditor(player, item, page));
                            } catch (Exception x) {
                                PlayerMessageHandler.messageSend(player, Utils.chat("&cAmount is not valid"));
                            }
                        }, false);
                    }
                }
                else{
                    ItemStack toSet=e.getCurrentItem().clone();
                    item.setItemStack(toSet.clone());
                    ArrayList<String> lore = new ArrayList<>();
                    if(toSet.getItemMeta().getLore()!=null)
                        lore=(ArrayList<String>)toSet.getItemMeta().getLore();

                    lore.add(Utils.chat("&8&m----------------------"));
                    lore.add(Utils.chat("&fClick an item in your"));
                    lore.add(Utils.chat("&finventory to set it here."));
                    ItemMeta meta = toSet.getItemMeta();
                    meta.setLore(lore);
                    toSet.setItemMeta(meta);
                    inventory.setItem(13, toSet);
                }
            }
        }

        @EventHandler
        public void onClose(InventoryCloseEvent e){
            if(e.getInventory().equals(inventory)){
                HandlerList.unregisterAll(this);
                item.saveToFile();
            }
        }
    }
}
