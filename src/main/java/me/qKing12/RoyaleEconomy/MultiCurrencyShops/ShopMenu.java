package me.qKing12.RoyaleEconomy.MultiCurrencyShops;

import de.tr7zw.changeme.nbtapi.NBTItem;
import me.qKing12.RoyaleEconomy.API.Events.ShopBuyMultiCurrencyEvent;
import me.qKing12.RoyaleEconomy.MultiCurrency.internal.Currency;
import me.qKing12.RoyaleEconomy.MultiCurrency.internal.MultiCurrencyHandler;
import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import me.qKing12.RoyaleEconomy.utils.CancelHotSwapInShops;
import me.qKing12.RoyaleEconomy.utils.CleanExtraSlotForShops;
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

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static me.qKing12.RoyaleEconomy.MultiCurrencyShops.ShopPlayerCache.lengthSellLore;
import static me.qKing12.RoyaleEconomy.RoyaleEconomy.*;


public class ShopMenu {
    private Inventory inventory;
    private Shop shop;
    private HashMap<Integer, ItemStack> playerInventory = new HashMap<>();
    private ArrayList<ShopHistoryItem> shopHistoryItems = new ArrayList<>();
    private HashMap<Integer, Shop.ShopItem> items = new HashMap<>();

    private int page;

    private boolean closed = true;

    private double limitSell = -1;
    private double coinsUsedSell;
    private double limitBuy = -1;
    private double coinsUsedBuy;

    private ArrayList<Integer> disabledSlots = new ArrayList<>();

    private boolean hasItems(Player p, ArrayList<ItemStack> toCheck) {
        if (toCheck == null)
            return true;

        Map<ItemStack, Integer> itemsToCheck = new HashMap<>();
        for (ItemStack item : toCheck) {
            ItemStack keyToCheck = item.clone();
            keyToCheck.setAmount(1);
            if (itemsToCheck.containsKey(keyToCheck))
                itemsToCheck.put(keyToCheck, itemsToCheck.get(keyToCheck) + item.getAmount());
            else
                itemsToCheck.put(keyToCheck, item.getAmount());
        }

        for (Map.Entry<ItemStack, Integer> entry : itemsToCheck.entrySet()) {
            ItemStack item = entry.getKey();
            int amount = entry.getValue();
            if (!p.getInventory().containsAtLeast(item, amount)) {
                int contorAmount = 0;
                for (Map.Entry<Integer, ItemStack> complexItemEntry : playerInventory.entrySet()) {
                    int slot = complexItemEntry.getKey();
                    ItemStack complexItem = complexItemEntry.getValue();
                    if (complexItem.isSimilar(item)) {
                        //this was added later in case of a sellall that makes the
                        //item disappear from inventory, we check if it is still in the inventory
                        //and not just the cache, it is poorly done checking just for amount and material
                        //mai jos e != inloc de < la amounturi pentru ca se compara cache-ul cu inventarul, daca difera, trecem peste
                        ItemStack inInventory = p.getInventory().getItem(slot);
                        if (inInventory == null || inInventory.getAmount() != complexItem.getAmount() || !inInventory.getType().equals(complexItem.getType()))
                            continue;

                        contorAmount += complexItem.getAmount();
                        if (contorAmount >= amount) {
                            contorAmount = -1;
                            break;
                        }
                    }
                }
                if (contorAmount != -1)
                    return false;
            }
        }

        return true;
    }

    private void removeItems(Player p, List<ItemStack> toCheck) {
        for (ItemStack item : toCheck) {
            int toRemoveAmount = item.getAmount();
            Iterator<Map.Entry<Integer, ItemStack>> inventoryItems = playerInventory.entrySet().iterator();
            while (inventoryItems.hasNext()) {
                Map.Entry<Integer, ItemStack> slotAndItemPair = inventoryItems.next();
                ItemStack inventoryItem = slotAndItemPair.getValue();

                if (inventoryItem.isSimilar(item)) {
                    if (inventoryItem.getAmount() <= toRemoveAmount) {
                        toRemoveAmount -= inventoryItem.getAmount();
                        inventoryItems.remove();
                        p.getInventory().setItem(slotAndItemPair.getKey(), new ItemStack(Material.AIR));
                    } else {
                        inventoryItem.setAmount(inventoryItem.getAmount() - toRemoveAmount);
                        playerInventory.put(slotAndItemPair.getKey(), inventoryItem.clone());
                        double price = ShopsLoad.getPrice(inventoryItem, shop);
                        if (price != 0) {
                            ItemMeta meta = inventoryItem.getItemMeta();
                            ArrayList<String> lore = new ArrayList<>();
                            if (meta.getLore() != null)
                                lore = (ArrayList<String>) meta.getLore();
                            for (String line : multiCurrencyShopsCfg.getStringList("lore-sell-addition"))
                                lore.add(Utils.chat(line.replace("%amount%", messageHelper.numberFormat(price))));
                            meta.setLore(lore);
                            inventoryItem.setItemMeta(meta);
                            NBTItem nbt = new NBTItem(inventoryItem);
                            nbt.setDouble("SellValue", price);
                            p.getInventory().setItem(slotAndItemPair.getKey(), nbt.getItem());
                        }
                        toRemoveAmount = 0;
                    }

                    if (toRemoveAmount <= 0)
                        break;
                }
            }

            if (toRemoveAmount > 0) {
                ItemStack toRemove = item.clone();
                toRemove.setAmount(toRemoveAmount);
                p.getInventory().removeItem(toRemove);
            }
        }
    }

//    private void removeItems(Player p, ArrayList<ItemStack> toCheck){
//        for(ItemStack item : toCheck){
//            int toRemoveAmount=item.getAmount();
//            Iterator<Integer> keys = playerInventory.keySet().iterator();
//            while(keys.hasNext()){
//                int key = keys.next();
//                ItemStack item2=playerInventory.get(key);
//                if(item2.isSimilar(item)){
//                    if(item2.getAmount()<toRemoveAmount){
//                        keys.remove();
//                        p.getInventory().setItem(key, new ItemStack(Material.AIR));
//                    }
//                    else{
//                        int finalAmount = item2.getAmount()-toRemoveAmount;
//                        if(finalAmount==0){
//                            keys.remove();
//                            p.getInventory().setItem(key, new ItemStack(Material.AIR));
//                        }
//                        else {
//                            item2.setAmount(item2.getAmount() - toRemoveAmount);
//                            playerInventory.put(key, item2.clone());
//                            double price = ShopsLoad.getPrice(item2, shop);
//                            if (price != 0) {
//                                ItemMeta meta = item2.getItemMeta();
//                                ArrayList<String> lore = new ArrayList<>();
//                                if (meta.getLore() != null)
//                                    lore = (ArrayList<String>) meta.getLore();
//                                for (String line : RoyaleEconomy.multiCurrencyShopsCfg.getStringList("lore-sell-addition"))
//                                    lore.add(Utils.chat(line.replace("%amount%", RoyaleEconomy.messageHelper.numberFormat(price))));
//                                meta.setLore(lore);
//                                item2.setItemMeta(meta);
//                                NBTItem nbt = new NBTItem(item2);
//                                nbt.setDouble("SellValue", price);
//                                p.getInventory().setItem(key, nbt.getItem());
//                            }
//                        }
//                        toRemoveAmount=-1;
//                        break;
//                    }
//                }
//            }
//            if(toRemoveAmount!=-1)
//                p.getInventory().removeItem(item);
//        }
//    }

    private HashMap<Integer, Double> buyValues = new HashMap<>();

    private void loadShopItems(Player p) {
        Iterator<Shop.ShopItem> shopItem = shop.getItems().iterator();

        for (int slot : items.keySet()) {
            inventory.setItem(slot, new ItemStack(Material.AIR));
        }

        for (int slot : disabledSlots) {
            inventory.setItem(slot, new ItemStack(Material.AIR));
        }

        items.clear();

        for (int i = 0; i < page * 28; i++)
            shopItem.next();

        while (inventory.firstEmpty() != -1 && shopItem.hasNext()) {
            Shop.ShopItem item = shopItem.next();
            int slot = inventory.firstEmpty();
            ItemStack toDisplay = item.getItemStack().clone();
            ArrayList<String> lore = new ArrayList<>();
            if (toDisplay.getItemMeta().getLore() != null)
                lore = (ArrayList<String>) toDisplay.getItemMeta().getLore();
            if (item.getBuyValue() == -1) {
                disabledSlots.add(slot);
                String sellValue;
                String currencyColor;
                String currencyName;
                if (item.getCurrency() == null) {
                    sellValue = RoyaleEconomy.messageHelper.numberFormat(toDisplay.getAmount() * item.getSellingValue());
                    currencyColor = MultiCurrencyHandler.getDefaultCoinsColor();
                    currencyName = MultiCurrencyHandler.getDefaultCoinsName();
                } else {
                    sellValue = item.getCurrency().formatMoney(toDisplay.getAmount() * item.getSellingValue());
                    currencyColor = item.getCurrency().getColor();
                    currencyName = item.getCurrency().getCurrencyName();
                }
                //item.getCurrency().formatMoney(toDisplay.getAmount() * item.getSellingValue());
                for (String line : multiCurrencyShopsCfg.getStringList("lore-cannot-buy-addition"))
                    lore.add(Utils.chat(line
                            .replace("%item-count%", String.valueOf(toDisplay.getAmount()))
                            .replace("%sell-amount%", sellValue)
                            .replace("%currency-color%", currencyColor)
                            .replace("%currency-name%", currencyName)
                    ));
            } else {
                String buyValue;
                String sellValue;
                String currencyColor;
                String currencyName;
                if (item.getCurrency() == null) {
                    buyValue = RoyaleEconomy.messageHelper.numberFormat(toDisplay.getAmount() * item.getBuyValue());
                    sellValue = RoyaleEconomy.messageHelper.numberFormat(toDisplay.getAmount() * item.getSellingValue());
                    currencyColor = MultiCurrencyHandler.getDefaultCoinsColor();
                    currencyName = MultiCurrencyHandler.getDefaultCoinsName();
                } else {
                    buyValue = item.getCurrency().formatMoney(toDisplay.getAmount() * item.getBuyValue());
                    sellValue = item.getCurrency().formatMoney(toDisplay.getAmount() * item.getSellingValue());
                    currencyColor = item.getCurrency().getColor();
                    currencyName = item.getCurrency().getCurrencyName();
                }
                for (String line : multiCurrencyShopsCfg.getStringList("lore-buy-addition")) {
                    line = line.replace("%item-count%", String.valueOf(toDisplay.getAmount()));
                    if (line.contains("%amount%")) {
                        if (item.getBuyValue() != 0)
                            lore.add(Utils.chat(line
                                    .replace("%amount%", buyValue)
                                    .replace("%sell-amount%", sellValue)
                                    .replace("%currency-color%", currencyColor)
                                    .replace("%currency-name%", currencyName)
                            ));
                    } else if (line.contains("%item-requirements%")) {
                        ArrayList<ItemStack> itemsReq = item.getDefaultItemRequirements();
                        if (itemsReq != null) {
                            LinkedHashMap<ItemStack, Integer> itemsBeyondStack = new LinkedHashMap<>();
                            for (ItemStack itemReq : itemsReq) {
                                if (itemsBeyondStack.containsKey(itemReq))
                                    itemsBeyondStack.put(itemReq, itemsBeyondStack.get(itemReq) + itemReq.getAmount());
                                else
                                    itemsBeyondStack.put(itemReq, itemReq.getAmount());
                            }

                            for (Map.Entry<ItemStack, Integer> itemAdd : itemsBeyondStack.entrySet()) {
                                lore.add(Utils.getDisplayName(itemAdd.getKey()) + Utils.chat(" &8x" + itemAdd.getValue()));
                            }
                        }
                    } else if (line.contains("%sell-amount%")) {
                        if (item.getSellingValue() != 0)
                            lore.add(Utils.chat(line
                                    .replace("%sell-amount%", sellValue)
                                    .replace("%currency-color%", currencyColor)
                                    .replace("%currency-name%", currencyName)
                            ));
                    } else
                        lore.add(Utils.chat(line));
                }
                if (item.hasRightClick() && item.getCommands() == null)
                    for (String line : multiCurrencyShopsCfg.getStringList("lore-detailed-buy-addition"))
                        lore.add(Utils.chat(line));
                /*BoostersActive.Booster percent=RoyaleEconomy.boosters.getBoosterPercentShopBuy(p, shop.getShopName());
                double value=0;
                double fullValue=item.getBuyValue()*toDisplay.getAmount();
                if(percent!=null) {
                    value = BoostersActive.getValueFromPercent(fullValue, percent.percent);
                    if (value != 0) {
                        for (String line : BoostersActive.shopBuyLoreAddition)
                            lore.add(line.replace("%booster-name%", percent.boosterName).replace("%amount%", RoyaleEconomy.messageHelper.numberFormat(value)));
                    }
                }
                 */
                //buyValues.put(slot, fullValue-value);
                buyValues.put(slot, item.getBuyValue() * toDisplay.getAmount());
                items.put(slot, item);
            }
            ItemMeta meta = toDisplay.getItemMeta();
            meta.setLore(lore);
            toDisplay.setItemMeta(meta);

            inventory.setItem(slot, toDisplay);
        }

        if (page != 0)
            inventory.setItem(48, RoyaleEconomy.staticValues.previousPageShopItem);
        else
            inventory.setItem(48, shop.getBackgroundGlass());
        if (shopItem.hasNext())
            inventory.setItem(50, RoyaleEconomy.staticValues.nextPageShopItem);
        else
            inventory.setItem(50, shop.getBackgroundGlass());
    }

    public ShopMenu(Player p, Shop shop, int page, Double coinsUsedBuy) {
        inventory = Bukkit.createInventory(null, 54, utilsAPI.chat(p, shop.getTitle()));
        if (ShopPlayerCache.shopHistory.containsKey(p)) {
            shopHistoryItems = ShopPlayerCache.shopHistory.get(p);
//            for (ShopHistoryItem historyItem : ShopPlayerCache.shopHistory.get(p))
//                this.shopHistoryItems.add(new ShopHistoryItem(historyItem.currency, historyItem.amount, historyItem.toGiveBack.clone(), historyItem.itemStack.clone(), historyItem.toGiveBackSlot, p, historyItem.toDisplay.clone()));
        }
        for (int i = 0; i < 36; i++) {
            ItemStack item = p.getInventory().getItem(i);
            if (item != null) {
                NBTItem nbt = new NBTItem(item);
                if (nbt.getDouble("SellValue") != 0) {
                    ItemStack item2 = item.clone();
                    NBTItem nbt2 = new NBTItem(item2);
                    nbt2.removeKey("SellValue");
                    item2 = nbt2.getItem();
                    try {
                        ItemMeta meta = item2.getItemMeta();
                        ArrayList<String> lore = (ArrayList<String>) meta.getLore();
                        int toRemove = lengthSellLore;
                        //boolean removeBoolean=false;
                        //if(BoostersActive.shopSellLoreAddition!=null && nbt2.getBoolean("UseRECBooster")){
                        //    removeBoolean=true;
                        //    toRemove+=BoostersActive.shopSellLoreAddition.size();
                        //}
                        lore.subList(lore.size() - toRemove, lore.size()).clear();
                        meta.setLore(lore);
                        item2.setItemMeta(meta);
                        //if(removeBoolean){
                        //    nbt2=new NBTItem(item2);
                        //    nbt2.removeKey("UseRECBooster");
                        //    item2=nbt2.getItem();
                        //}
                        nbt.setDouble("SellValue", ShopsLoad.getPrice(item2, shop));
                        item = nbt.getItem();
                        p.getInventory().setItem(i, item);
                    } catch (Exception x) {
                        x.printStackTrace();
                        RoyaleEconomy.plugin.getLogger().warning("I couldn't remove lore parts from an item that should have that lore: " + item2.toString());
                        continue;
                    }
                    playerInventory.put(i, item2);
                } else if (nbt.getBoolean("FreshlyBought")) {
                    nbt.removeKey("FreshlyBought");
                    item = nbt.getItem();
                    ItemMeta meta;
                    ArrayList<String> lore;
                    try {
                        meta = item.getItemMeta();
                        lore = (ArrayList<String>) meta.getLore();
                        lore.subList(lore.size() - ShopPlayerCache.lengthBoughtLore, lore.size()).clear();
                        meta.setLore(lore);
                        item.setItemMeta(meta);
                    } catch (Exception x) {
                        x.printStackTrace();
                        RoyaleEconomy.plugin.getLogger().warning("I couldn't remove lore parts from an item that should have that lore: " + item.toString());
                    }
                    playerInventory.put(i, item.clone());
                    Shop.ShopItem itemSold = ShopsLoad.getShopItem(item, shop);
                    if (itemSold != null && itemSold.getSellingValue() > 0) {
                        meta = item.getItemMeta();
                        lore = new ArrayList<>();
                        if (meta.getLore() != null)
                            lore = (ArrayList<String>) meta.getLore();

                        String sellValue;
                        String currencyColor;
                        String currencyName;
                        if (itemSold.getCurrency() == null) {
                            sellValue = RoyaleEconomy.messageHelper.numberFormat(item.getAmount() * itemSold.getSellingValue());
                            currencyColor = MultiCurrencyHandler.getDefaultCoinsColor();
                            currencyName = MultiCurrencyHandler.getDefaultCoinsName();
                        } else {
                            sellValue = itemSold.getCurrency().formatMoney(item.getAmount() * itemSold.getSellingValue());
                            currencyColor = itemSold.getCurrency().getColor();
                            currencyName = itemSold.getCurrency().getCurrencyName();
                        }

                        for (String line : RoyaleEconomy.multiCurrencyShopsCfg.getStringList("lore-sell-addition"))
                            lore.add(Utils.chat(line
                                    .replace("%amount%", sellValue)
                                    .replace("%currency-name%", currencyName)
                                    .replace("%currency-color%", currencyColor)
                            ));
                        meta.setLore(lore);
                        item.setItemMeta(meta);
                        nbt = new NBTItem(item);
                        nbt.setDouble("SellValue", itemSold.getSellingValue() * item.getAmount());
                        p.getInventory().setItem(i, nbt.getItem());
                    }
                } else {
                    Shop.ShopItem shopItem = ShopsLoad.getShopItem(item.clone(), shop);
                    if (shopItem != null && shopItem.getSellingValue() > 0) {
                        this.playerInventory.put(i, item.clone());
                        ItemMeta meta = item.getItemMeta();
                        ArrayList<String> lore = new ArrayList<>();
                        if (meta.getLore() != null)
                            lore = (ArrayList<String>) meta.getLore();

                        String sellValue;
                        String currencyColor;
                        String currencyName;
                        if (shopItem.getCurrency() == null) {
                            sellValue = RoyaleEconomy.messageHelper.numberFormat(item.getAmount() * shopItem.getSellingValue());
                            currencyColor = MultiCurrencyHandler.getDefaultCoinsColor();
                            currencyName = MultiCurrencyHandler.getDefaultCoinsName();
                        } else {
                            sellValue = shopItem.getCurrency().formatMoney(item.getAmount() * shopItem.getSellingValue());
                            currencyColor = shopItem.getCurrency().getColor();
                            currencyName = shopItem.getCurrency().getCurrencyName();
                        }

                        for (String line : RoyaleEconomy.multiCurrencyShopsCfg.getStringList("lore-sell-addition")) {
                            lore.add(Utils.chat(line
                                    .replace("%amount%", sellValue)
                                    .replace("%currency-name%", currencyName)
                                    .replace("%currency-color%", currencyColor)
                            ));
                        }
                        /*BoostersActive.Booster percent=RoyaleEconomy.boosters.getBoosterPercentShopSell(p, shop.getShopName());
                        double value=0;
                        if(percent!=null) {
                            value = BoostersActive.getValueFromPercent(price, percent.percent);
                            if(prices.getValue()>0 && price+value>prices.getValue())
                                value=prices.getValue()-price;
                            if (value != 0) {
                                for (String line : BoostersActive.shopSellLoreAddition)
                                    lore.add(line.replace("%booster-name%", percent.boosterName).replace("%amount%", RoyaleEconomy.messageHelper.numberFormat(value)));
                            }
                        }*/
                        meta.setLore(lore);
                        item.setItemMeta(meta);
                        nbt = new NBTItem(item);
                        nbt.setDouble("SellValue", shopItem.getSellingValue() * item.getAmount());
                        //if(percent!=null)
                        //    nbt.setBoolean("UseRECBooster", true);
                        p.getInventory().setItem(i, nbt.getItem());
                    }
                }
            }
        }
        this.page = page;
        ItemStack background = shop.getBackgroundGlass().clone();
        this.shop = shop;
        for (int i = 0; i < 9; i++) {
            inventory.setItem(i, background);
            inventory.setItem(i + 45, background);
        }
        for (int i = 9; i < 45; i += 9) {
            inventory.setItem(i, background);
            inventory.setItem(i + 8, background);
        }

        loadShopItems(p);

        if (RoyaleEconomy.staticValues.useShopBackItem) {
            for (int slot : RoyaleEconomy.staticValues.shopBackItemSlot)
                inventory.setItem(slot, RoyaleEconomy.staticValues.shopBackItem);
        }

        if (!staticValues.disableBuyBack) {
            if (this.shopHistoryItems.isEmpty())
                inventory.setItem(49, RoyaleEconomy.staticValues.sellItemsGetBack);
            else
                inventory.setItem(49, this.shopHistoryItems.get(0).toDisplay);
        }
        RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runNextTick((task) -> p.openInventory(inventory));
        ShopPlayerCache.addPlayer(p.getName());

        /*if(ShopSellLimit.playerValues!=null){
            limitSell= ShopSellLimit.getLimit(p);
            coinsUsedSell= ShopSellLimit.playerValues.getOrDefault(p.getUniqueId().toString(), 0d);
        }

        if(ShopBuyLimit.playerValues!=null){
            limitBuy= ShopBuyLimit.getLimit(p);
            if(coinsUsedBuy==null)
                this.coinsUsedBuy= ShopBuyLimit.playerValues.getOrDefault(p.getUniqueId().toString(), 0d);
            else
                this.coinsUsedBuy=coinsUsedBuy;
        }*/

        p.updateInventory();
        Bukkit.getPluginManager().registerEvents(new ClickListener(), RoyaleEconomy.plugin);

        if (!is18) {
            cancelHotSwapInShops = new CancelHotSwapInShops(p);
        }
    }

    private static final boolean is18 = Bukkit.getVersion().contains("1.8");
    private CancelHotSwapInShops cancelHotSwapInShops;

    private class ClickListener implements Listener {

        AtomicBoolean giveBackCooldown = new AtomicBoolean(false);

        @EventHandler
        public void onClick(InventoryClickEvent e) {
            //if(inventory.getViewers().isEmpty()) RoyaleEconomy.plugin.getLogger().warning("A closed listener is still active.");
            if (e.getInventory().equals(inventory)) {
                e.setCancelled(true);
                if (e.getSlot() < 0 || e.getCurrentItem() == null || e.getCurrentItem().getType().equals(Material.AIR))
                    return;

                if (playerInventory == null)
                    return;

                if (e.getClick().equals(ClickType.DOUBLE_CLICK) || e.getClick().equals(ClickType.RIGHT) || e.getClick().equals(ClickType.LEFT)) {
                    if (!e.getClickedInventory().equals(e.getWhoClicked().getInventory())) {
                        Player p = (Player) e.getWhoClicked();

                        if (RoyaleEconomy.staticValues.useShopBackItem && staticValues.shopBackItemSlot.contains(e.getSlot())) {
                            p.closeInventory();
                            RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runNextTick((task) -> {
                                for (String line : RoyaleEconomy.staticValues.shopBackItemCommands)
                                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), line.replace("%player%", p.getName()));
                            });
                        } else if (e.getSlot() == 49 && !staticValues.disableBuyBack) {
                            if (!shopHistoryItems.isEmpty()) {
                                Utils.playSound(p, "menus.shop-buyback");
                                if (giveBackCooldown.compareAndSet(false, true)) {
                                    shopHistoryItems.get(0).giveBack(p);
                                    giveBackCooldown.set(false);
                                }
                            }
                        } else if (e.getCurrentItem().equals(RoyaleEconomy.staticValues.previousPageShopItem)) {
                            page--;
                            loadShopItems(p);
                        } else if (e.getCurrentItem().equals(RoyaleEconomy.staticValues.nextPageShopItem)) {
                            page++;
                            loadShopItems(p);
                        } else {
                            if (items.containsKey(e.getSlot())) {
                                Shop.ShopItem item = items.get(e.getSlot());
                                String buyPermission = item.getBuyPermission();
                                if (buyPermission != null && !p.hasPermission(buyPermission)) {
                                    PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, shopsCfg.getString("no-buy-permission-message")));
                                    return;
                                }
                                if (e.getClick().equals(ClickType.RIGHT) && item.hasRightClick() && item.getCommands() == null) {
                                    closed = false;
                                    new ShopDetailMenu(p, shop, page, item, coinsUsedBuy);
                                } else {
                                    if (item.getDetailedMenuMode() == 1) {
                                        closed = false;
                                        new ShopDetailMenu(p, shop, page, item, coinsUsedBuy);
                                        return;
                                    }
                                    if (clicksCache != 0) {
                                        PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, multiCurrencyShopsCfg.getString("too-fast-click")));
                                        return;
                                    }
                                    if (p.getInventory().firstEmpty() != -1) {
                                        clicksCache++;
                                        RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runAsync((task) -> {
                                            ArrayList<ItemStack> toCheck = item.getDefaultItemRequirements();
                                            if (!hasItems(p, toCheck)) {
                                                clicksCache--;
                                                PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, multiCurrencyShopsCfg.getString("not-enough-items")));
                                                return;
                                            }
                                            //int slot = p.getInventory().firstEmpty();
                                            double value = buyValues.get(e.getSlot());
                                            /*if (limitBuy != -1) {
                                                if (coinsUsedBuy + value > limitBuy) {
                                                    clicksCache--;
                                                    ShopBuyLimit.sendMessage(p, limitBuy);
                                                    return;
                                                }
                                            }*/
                                            boolean moneyRemoved;
                                            if (item.getCurrency() == null)
                                                moneyRemoved = RoyaleEconomy.dataManager.removeMoneyFromFile(p.getUniqueId().toString(), value);
                                            else {
                                                moneyRemoved = item.getCurrency().removeAmount(p.getUniqueId().toString(), value);
                                            }
                                            if (moneyRemoved) {
                                                ShopBuyMultiCurrencyEvent buyEvent = new ShopBuyMultiCurrencyEvent(p, value, e.getCurrentItem().getAmount(), item, shop);
                                                Bukkit.getPluginManager().callEvent(buyEvent);
                                                if (buyEvent.isCancelled()) {
                                                    if (item.getCurrency() == null)
                                                        RoyaleEconomy.dataManager.addMoneyToFile(p.getUniqueId().toString(), value);
                                                    else
                                                        item.getCurrency().addAmount(p.getUniqueId().toString(), value);
                                                    clicksCache--;
                                                    return;
                                                }

                                                if (inventory == null || playerInventory == null) {
                                                    if (item.getCurrency() == null)
                                                        RoyaleEconomy.dataManager.addMoneyToFile(p.getUniqueId().toString(), value);
                                                    else
                                                        item.getCurrency().addAmount(p.getUniqueId().toString(), value);
                                                    return;
                                                }

                                                coinsUsedBuy += value;
                                                //clicksCache++;
                                                //RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runAsync((task) -> {

                                                if (toCheck != null) {
                                                    removeItems(p, toCheck);
                                                }
                                                int slot = p.getInventory().firstEmpty();
                                                if (item.getCommands() == null || item.getGiveItemOnBuy()) {
                                                    if (playerInventory == null) {
                                                        p.getInventory().addItem(item.getItemStack());
                                                    } else {
                                                        ItemStack itemToPost = item.getItemStack().clone();
                                                        ItemMeta meta = itemToPost.getItemMeta();
                                                        ArrayList<String> lore = new ArrayList<>();
                                                        if (meta.getLore() != null)
                                                            lore = (ArrayList<String>) meta.getLore();
                                                        for (String line : RoyaleEconomy.multiCurrencyShopsCfg.getStringList("freshly-bought-lore"))
                                                            lore.add(Utils.chat(line));
                                                        meta.setLore(lore);
                                                        itemToPost.setItemMeta(meta);
                                                        NBTItem nbt = new NBTItem(itemToPost);
                                                        nbt.setBoolean("FreshlyBought", true);
                                                        itemToPost = nbt.getItem();
                                                        p.getInventory().addItem(itemToPost);
                                                    }
                                                }
                                                String itemName = Utils.getDisplayName(e.getCurrentItem());
                                                String valueStr;
                                                String currencyName;
                                                String currencyColor;
                                                if (item.getCurrency() == null) {
                                                    valueStr = RoyaleEconomy.messageHelper.numberFormat(value);
                                                    currencyName = MultiCurrencyHandler.getDefaultCoinsName();
                                                    currencyColor = MultiCurrencyHandler.getDefaultCoinsColor();
                                                } else {
                                                    valueStr = item.getCurrency().formatMoney(value);
                                                    currencyName = item.getCurrency().getCurrencyName();
                                                    currencyColor = item.getCurrency().getColor();
                                                }
                                                if (value != 0)
                                                    PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.multiCurrencyShopsCfg.getString("buy-message")
                                                            .replace("%amount%", valueStr)
                                                            .replace("%item-count%", String.valueOf(e.getCurrentItem().getAmount()))
                                                            .replace("%item-display-name%", itemName)
                                                            .replace("%currency-name%", currencyName)
                                                            .replace("%currency-color%", currencyColor)
                                                    ));
                                                if (item.getCommands() != null) {
                                                    RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runNextTick((task2) -> {
                                                        for (String command : item.getCommands()) {
                                                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", p.getName()).replace("%price%", String.valueOf(value)));
                                                        }
                                                    });
                                                }
                                                //ShopsLoad.injectToLog("[Buying from Main Menu] " + p.getName() + " bought " + e.getCurrentItem().getAmount() + "x " + itemName + " for " + value);
                                                Utils.playSound(p, "menus.shop-buy");
                                                //clicksCache--;
                                                //});
                                            } else {
                                                String currencyName;
                                                String currencyColor;
                                                if (item.getCurrency() == null) {
                                                    currencyName = MultiCurrencyHandler.getDefaultCoinsName();
                                                    currencyColor = MultiCurrencyHandler.getDefaultCoinsColor();
                                                } else {
                                                    currencyName = item.getCurrency().getCurrencyName();
                                                    currencyColor = item.getCurrency().getColor();
                                                }
                                                PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.multiCurrencyShopsCfg.getString("not-enough-coins")
                                                        .replace("%currency-color%", currencyColor)
                                                        .replace("%currency-name%", currencyName)
                                                ));
                                            }
                                            clicksCache--;
                                        });
                                    } else {
                                        PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.multiCurrencyShopsCfg.getString("not-enough-space")));
                                    }
                                }
                            }
                        }


                    } else {
                        if (playerInventory.containsKey(e.getSlot())) {
                            double coins = new NBTItem(e.getCurrentItem()).getDouble("SellValue");
                            if (coins == 0) {
                                coins = ShopsLoad.getPrice(playerInventory.get(e.getSlot()), shop);
                                //RoyaleEconomy.plugin.getLogger().warning("[DEBUG MESSAGE] A player tried to sell an item for 0 coins -> " + e.getCurrentItem().toString());
                                //RoyaleEconomy.plugin.getLogger().warning("[DEBUG MESSAGE2] Price of the item on recalculation: " + coins);
                                //RoyaleEconomy.plugin.getLogger().warning("[DEBUG MESSAGE3] Stored item at slot: " + playerInventory.get(e.getSlot()).toString());
                            }
                            /*if (limitSell != -1) {
                                //double coins = new NBTItem(e.getCurrentItem()).getDouble("SellValue");
                                if (coinsUsedSell + coins > limitSell) {
                                    ShopSellLimit.sendMessage((Player) e.getWhoClicked(), limitSell);
                                    return;
                                } else
                                    coinsUsedSell += coins;
                            }*/
                            Shop.ShopItem shopItem = ShopsLoad.getShopItem(playerInventory.get(e.getSlot()), shop);
                            if (shopItem != null) {
                                shopHistoryItems.add(0, new ShopHistoryItem(shopItem.getCurrency(), shopItem.getSellingValue() * e.getCurrentItem().getAmount(), e.getCurrentItem(), playerInventory.get(e.getSlot()), e.getSlot(), (Player) e.getWhoClicked(), null));
                                playerInventory.remove(e.getSlot());
                                Utils.playSound((Player) e.getWhoClicked(), "menus.shop-sell");
                                e.getWhoClicked().getInventory().setItem(e.getSlot(), new ItemStack(Material.AIR));
                            }
                        }
                    }
                }
            }
        }


        private ItemStack removeFreshBoughtLore(ItemStack itemStack) {
            NBTItem nbt = new NBTItem(itemStack);
            ItemStack toReturn;

            if (nbt.getBoolean("FreshlyBought")) {
                nbt.removeKey("FreshlyBought");
                toReturn = nbt.getItem();
                ItemMeta meta;
                ArrayList<String> lore;
                try {
                    meta = toReturn.getItemMeta();
                    lore = (ArrayList<String>) meta.getLore();
                    lore.subList(lore.size() - ShopPlayerCache.lengthBoughtLore, lore.size()).clear();
                    meta.setLore(lore);
                    toReturn.setItemMeta(meta);
                } catch (Exception x) {
                    x.printStackTrace();
                    RoyaleEconomy.plugin.getLogger().warning("I couldn't remove lore parts from an item that should have that lore: " + toReturn.toString());
                }
            } else
                toReturn = itemStack;

            return toReturn;
        }

        private int clicksCache = 0;

        @EventHandler
        public void onClose(InventoryCloseEvent e) {
            if (e.getInventory().equals(inventory)) {
                if (cancelHotSwapInShops != null)
                    cancelHotSwapInShops.cancel();

                HandlerList.unregisterAll(this);
                //if(limitSell!=-1)
                //    ShopSellLimit.playerValues.put(e.getPlayer().getUniqueId().toString(), coinsUsedSell);
                //if(limitBuy!=-1)
                //    ShopBuyLimit.playerValues.put(e.getPlayer().getUniqueId().toString(), coinsUsedBuy);
                //RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runAsync((task) -> {
//                    for(Integer slot : playerInventory.keySet()){
//                        ItemStack toCheck = e.getPlayer().getInventory().getItem(slot);
//                        ItemStack tempItem = playerInventory.get(slot);
//                        if(toCheck==null || !toCheck.getData().equals(tempItem.getData()))
//                            continue;
//                        tempItem.setAmount(toCheck.getAmount());
//                        e.getPlayer().getInventory().setItem(slot, tempItem);
//                    }

                for (int slot = 0; slot < 40; slot++) {
                    ItemStack item = e.getPlayer().getInventory().getItem(slot);
                    if (item == null || item.getType().equals(Material.AIR))
                        continue;
                    NBTItem nbt = new NBTItem(item);
                    if (nbt.getDouble("SellValue") != 0) {
                        nbt.removeKey("SellValue");
                        item = nbt.getItem();
                        ItemMeta meta = item.getItemMeta();
                        ArrayList<String> lore = (ArrayList<String>) meta.getLore();
                        int toRemove = lengthSellLore;
                            /*boolean removeBoolean=false;
                            if(BoostersActive.shopSellLoreAddition!=null && nbt.getBoolean("UseRECBooster")){
                                removeBoolean=true;
                                toRemove+=BoostersActive.shopSellLoreAddition.size();
                            }*/
                        lore.subList(lore.size() - toRemove, lore.size()).clear();
                        meta.setLore(lore);
                        item.setItemMeta(meta);
                            /*if(removeBoolean){
                                nbt=new NBTItem(item);
                                nbt.removeKey("UseRECBooster");
                                item=nbt.getItem();
                            }*/
                        e.getPlayer().getInventory().setItem(slot, item);
                    }
                }

                removeFreshBoughtLore(e);

                ShopPlayerCache.removePlayer(e.getPlayer().getName());
                ShopPlayerCache.shopHistory.put((Player) e.getPlayer(), shopHistoryItems);
//                shopHistoryItems = null;
                playerInventory = null;
                if (shop.getCommands() != null) {
                    if (closed)
                        RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runNextTick((task) -> {
                            for (String command : shop.getCommands())
                                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", e.getPlayer().getName()));
                        });
                }
                items = null;
                //});

                CleanExtraSlotForShops.cleanExtraSlotForShops((Player) e.getPlayer());
            }
        }

        private void removeFreshBoughtLore(InventoryCloseEvent e) {
            for (int slot = 0; slot < 36; slot++) {
                ItemStack item = e.getPlayer().getInventory().getItem(slot);
                if (item == null || item.getType().equals(Material.AIR))
                    continue;

                ItemStack removedFreshlyBoughtLore = removeFreshBoughtLore(item);
                if (!removedFreshlyBoughtLore.equals(item))
                    e.getPlayer().getInventory().setItem(slot, removedFreshlyBoughtLore);
            }
        }

    }

    public class ShopHistoryItem {
        private final ItemStack toGiveBack;
        private int toGiveBackSlot;
        private final ItemStack itemStack;
        private final ItemStack toDisplay;
        private final double amount;

        private final Currency currency;

        public ShopHistoryItem(Currency currency, double coins, ItemStack clicked, ItemStack previous, int slot, Player p, ItemStack existentToDisplay) {
            this.currency = currency;
            toGiveBackSlot = slot;
            amount = coins;
            this.toGiveBack = clicked;
            this.itemStack = previous;
            if (existentToDisplay == null) {
                this.toDisplay = previous.clone();
                if (currency == null)
                    RoyaleEconomy.dataManager.addMoneyToFile(p.getUniqueId().toString(), amount);
                else
                    currency.addAmount(p.getUniqueId().toString(), amount);
                String itemName = Utils.getDisplayName(previous.clone());
                String valueStr;
                String currencyName;
                String currencyColor;
                if (currency == null) {
                    valueStr = RoyaleEconomy.messageHelper.numberFormat(coins);
                    currencyName = MultiCurrencyHandler.getDefaultCoinsName();
                    currencyColor = MultiCurrencyHandler.getDefaultCoinsColor();
                } else {
                    valueStr = currency.formatMoney(coins);
                    currencyName = currency.getCurrencyName();
                    currencyColor = currency.getColor();
                }
                PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.multiCurrencyShopsCfg.getString("sell-message")
                        .replace("%amount%", valueStr)
                        .replace("%item-count%", String.valueOf(previous.getAmount()))
                        .replace("%item-display-name%", itemName)
                        .replace("%currency-name%", currencyName)
                        .replace("%currency-color%", currencyColor)
                ));
                //ShopsLoad.injectToLog("[Selling] "+p.getName()+" sold "+previous.getAmount()+"x "+itemName+" for "+amount);
                ArrayList<String> lore = new ArrayList<>();
                if (toDisplay.getItemMeta().getLore() != null)
                    lore = (ArrayList<String>) toDisplay.getItemMeta().getLore();
                for (String line : RoyaleEconomy.multiCurrencyShopsCfg.getStringList("lore-buyback-addition"))
                    lore.add(Utils.chat(line
                            .replace("%amount%", valueStr)
                            .replace("%currency-name%", currencyName)
                            .replace("%currency-color%", currencyColor)
                    ));
                ItemMeta meta = toDisplay.getItemMeta();
                meta.setLore(lore);
                toDisplay.setItemMeta(meta);
            } else
                toDisplay = existentToDisplay;

            inventory.setItem(49, toDisplay);
        }

        private boolean alreadyGiven = false;

        public synchronized void giveBack(Player p) {
            if (alreadyGiven)
                return;

            if (amount == 0) {
                plugin.getLogger().info("[DEBUG MultiCurrency Shops] Player " + p.getName() + " tried to buy back an item with 0 value. Cancelled.");
                return;
            }

            if (toGiveBackSlot == -1 || p.getInventory().getItem(toGiveBackSlot) != null) {
                toGiveBackSlot = p.getInventory().firstEmpty();
                if (toGiveBackSlot == -1) {
                    PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.multiCurrencyShopsCfg.getString("not-enough-space")));
                    return;
                }
            }
            boolean moneyRemoved;
            if (currency == null)
                moneyRemoved = RoyaleEconomy.dataManager.removeMoneyFromFile(p.getUniqueId().toString(), amount);
            else
                moneyRemoved = currency.removeAmount(p.getUniqueId().toString(), amount);

            String valueStr;
            String currencyName;
            String currencyColor;
            if (currency == null) {
                valueStr = RoyaleEconomy.messageHelper.numberFormat(amount);
                currencyName = MultiCurrencyHandler.getDefaultCoinsName();
                currencyColor = MultiCurrencyHandler.getDefaultCoinsColor();
            } else {
                valueStr = currency.formatMoney(amount);
                currencyName = currency.getCurrencyName();
                currencyColor = currency.getColor();
            }
            if (moneyRemoved) {
                alreadyGiven = true;

                p.getInventory().setItem(toGiveBackSlot, toGiveBack);
                if (playerInventory != null)
                    playerInventory.put(toGiveBackSlot, itemStack);
                shopHistoryItems.remove(0);
                String itemName = Utils.getDisplayName(itemStack);
                PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.multiCurrencyShopsCfg.getString("buyback-message")
                        .replace("%amount%", valueStr)
                        .replace("%item-count%", String.valueOf(itemStack.getAmount()))
                        .replace("%item-display-name%", itemName)
                        .replace("%currency-name%", currencyName)
                        .replace("%currency-color%", currencyColor)
                ));
                //ShopsLoad.injectToLog("[Buyback] "+p.getName()+" sold "+itemStack.getAmount()+"x "+itemName+" for "+amount);
                if (shopHistoryItems.isEmpty()) {
                    inventory.setItem(49, RoyaleEconomy.staticValues.sellItemsGetBack);
                } else
                    inventory.setItem(49, shopHistoryItems.get(0).toDisplay);
            } else {
                PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.multiCurrencyShopsCfg.getString("not-enough-coins")
                        .replace("%currency-name%", currencyName)
                        .replace("%currency-color%", currencyColor)
                ));
            }
        }
    }
}
