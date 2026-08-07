package me.qKing12.RoyaleEconomy.MultiCurrencyShops;

import me.qKing12.RoyaleEconomy.API.Events.ShopBuyMultiCurrencyEvent;
import me.qKing12.RoyaleEconomy.Boosters.BoostersActive;
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
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static me.qKing12.RoyaleEconomy.RoyaleEconomy.multiCurrencyShopsCfg;
import static me.qKing12.RoyaleEconomy.RoyaleEconomy.utilsAPI;



public class ShopDetailMenu  {
    private Inventory inventory;
    private Shop shop;
    private Shop.ShopItem item;
    private double buyValue;
    private ItemStack background;

    private int page;

    private double limitBuy=-1;
    private double coinsUsedBuy;

    private HashMap<Integer, Double> buyValues=new HashMap<>();

    public ShopDetailMenu(Player p, Shop shop, int page, Shop.ShopItem index, Double coinsUsedBuy){
        RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runAsync((task) -> {
            inventory = Bukkit.createInventory(null, 36, utilsAPI.chat(p, shop.getShopDetailedTitle()));
            this.page = page;
            background = shop.getBackgroundGlass().clone();
            this.shop = shop;
            this.item = index;
            for (int i = 0; i < 36; i++) {
                inventory.setItem(i, background);
            }

            this.buyValue = item.getBuyValue();

            for (int i = 1; i < 6; i++) {
                int tempAmount = item.getAmount(i);
                if (tempAmount != 0) {
                    ItemStack itemStack = item.getItemStack().clone();
                    ItemMeta meta = itemStack.getItemMeta();
                    ArrayList<String> lore = new ArrayList<>();
                    if (meta.getLore() != null)
                        lore = (ArrayList<String>) meta.getLore();
                    ArrayList<ItemStack> itemsReq;
                    if (i == 1)
                        itemsReq = item.getAmount1Items();
                    else if (i == 2)
                        itemsReq = item.getAmount2Items();
                    else if (i == 3)
                        itemsReq = item.getAmount3Items();
                    else if (i == 4)
                        itemsReq = item.getAmount4Items();
                    else
                        itemsReq = item.getAmount5Items();
                    String buyValueText;
                    String sellValueText;
                    String currencyColor;
                    String currencyName;
                    if(item.getCurrency()==null){
                        buyValueText = RoyaleEconomy.messageHelper.numberFormat(tempAmount* item.getBuyValue());
                        sellValueText = RoyaleEconomy.messageHelper.numberFormat(tempAmount * item.getSellingValue());
                        currencyColor = MultiCurrencyHandler.getDefaultCoinsColor();
                        currencyName = MultiCurrencyHandler.getDefaultCoinsName();
                    }
                    else{
                        sellValueText = item.getCurrency().formatMoney(tempAmount * item.getSellingValue());
                        buyValueText = item.getCurrency().formatMoney(tempAmount * item.getBuyValue());
                        currencyColor = item.getCurrency().getColor();
                        currencyName = item.getCurrency().getCurrencyName();
                    }
                    for (String line : RoyaleEconomy.multiCurrencyShopsCfg.getStringList("lore-buy-addition")) {
                        if (line.contains("%amount%")) {
                            if (item.getBuyValue() != 0)
                                lore.add(Utils.chat(line
                                        .replace("%amount%", buyValueText)
                                        .replace("%sell-amount%", sellValueText)
                                        .replace("%currency-color%", currencyColor)
                                        .replace("%currency-name%", currencyName)
                                ));
                        } else if (line.contains("%item-requirements%")) {
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
                        }
                        else if(line.contains("%sell-amount%")){
                            if (item.getSellingValue() != 0)
                                lore.add(Utils.chat(line
                                        .replace("%sell-amount%", sellValueText)
                                        .replace("%currency-color%", currencyColor)
                                        .replace("%currency-name%", currencyName)
                                ));
                        }else
                            lore.add(Utils.chat(line));
                    }
//                    BoostersActive.Booster percent=RoyaleEconomy.boosters.getBoosterPercentShopBuy(p, shop.getShopName());
//                    double value=0;
//                    double fullValue=buyValue*tempAmount;
//                    if(percent!=null) {
//                        value = BoostersActive.getValueFromPercent(fullValue, percent.percent);
//                        if (value != 0) {
//                            for (String line : BoostersActive.shopBuyLoreAddition)
//                                lore.add(line.replace("%booster-name%", percent.boosterName).replace("%amount%", RoyaleEconomy.messageHelper.numberFormat(value)));
//                        }
//                    }
//                    buyValues.put(10+i, fullValue-value);
                    buyValues.put(10+i, buyValue*tempAmount);
                    meta.setLore(lore);
                    itemStack.setItemMeta(meta);
                    itemStack.setAmount(tempAmount);
                    inventory.setItem(10 + i, itemStack);
                }
            }

            inventory.setItem(31, RoyaleEconomy.staticValues.goBackShopItem);

            /*if(ShopBuyLimit.playerValues!=null){
                limitBuy= ShopBuyLimit.getLimit(p);
                if(coinsUsedBuy==null)
                    this.coinsUsedBuy= ShopBuyLimit.playerValues.getOrDefault(p.getUniqueId().toString(), 0d);
                else
                    this.coinsUsedBuy=coinsUsedBuy;
            }*/

            RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runNextTick((task2) -> {
                p.openInventory(inventory);
                Bukkit.getPluginManager().registerEvents(new ClickListener(), RoyaleEconomy.plugin);
            });
        });
    }

    private int clicksCache=0;

    private class ClickListener implements Listener {

        @EventHandler
        public void onClick(InventoryClickEvent e) {
            //if(inventory.getViewers().isEmpty()) RoyaleEconomy.plugin.getLogger().warning("A closed listener is still active.");
            if (e.getInventory().equals(inventory)) {
                e.setCancelled(true);
                if (e.getSlot() < 0 || e.getCurrentItem() == null || e.getCurrentItem().getType().equals(Material.AIR))
                    return;
                if (!e.getClickedInventory().equals(e.getWhoClicked().getInventory())) {
                    Player p = (Player) e.getWhoClicked();
                    if(e.getSlot()==31){
                        new ShopMenu(p, shop, page, null);
                    }
                    else if(!e.getCurrentItem().equals(background)){
                        if(clicksCache!=0){
                            PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, multiCurrencyShopsCfg.getString("too-fast-click")));
                            return;
                        }
                        if(p.getInventory().firstEmpty()!=-1) {
                            clicksCache++;
                            RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runAsync((task) -> {
                                int slot = e.getSlot() - 10;
                                ArrayList<ItemStack> itemsToCheck;
                                if (slot == 1)
                                    itemsToCheck = item.getAmount1Items();
                                else if (slot == 2)
                                    itemsToCheck = item.getAmount2Items();
                                else if (slot == 3)
                                    itemsToCheck = item.getAmount3Items();
                                else if (slot == 4)
                                    itemsToCheck = item.getAmount4Items();
                                else
                                    itemsToCheck = item.getAmount5Items();
                                if (!Utils.hasItems(p, itemsToCheck)) {
                                    PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, multiCurrencyShopsCfg.getString("not-enough-items")));
                                    clicksCache--;
                                    return;
                                }
                                double value = buyValues.get(e.getSlot());
                                /*if(limitBuy!=-1){
                                    if(coinsUsedBuy+value>limitBuy){
                                        clicksCache--;
                                        ShopBuyLimit.sendMessage(p, limitBuy);
                                        return;
                                    }
                                }*/
                                boolean moneyRemoved;
                                if(item.getCurrency()==null)
                                    moneyRemoved=RoyaleEconomy.dataManager.removeMoneyFromFile(p.getUniqueId().toString(), value);
                                else{
                                    moneyRemoved=item.getCurrency().removeAmount(p.getUniqueId().toString(), value);
                                }

                                if (moneyRemoved) {
                                    ShopBuyMultiCurrencyEvent buyEvent=new ShopBuyMultiCurrencyEvent(p, value, e.getCurrentItem().getAmount(), item, shop);
                                    Bukkit.getPluginManager().callEvent(buyEvent);
                                    if(buyEvent.isCancelled()){
                                        clicksCache--;
                                        return;
                                    }
                                    coinsUsedBuy+=value;
                                    ItemStack toAdd = item.getItemStack().clone();
                                    if (itemsToCheck != null)
                                        for (ItemStack itemStack : itemsToCheck)
                                            p.getInventory().removeItem(itemStack);
                                    toAdd.setAmount(e.getCurrentItem().getAmount());
                                    p.getInventory().addItem(toAdd);
                                    String itemName = Utils.getDisplayName(e.getCurrentItem());
                                    String valueStr;
                                    String currencyName;
                                    String currencyColor;
                                    if(item.getCurrency()==null){
                                        valueStr = RoyaleEconomy.messageHelper.numberFormat(value);
                                        currencyName = MultiCurrencyHandler.getDefaultCoinsName();
                                        currencyColor = MultiCurrencyHandler.getDefaultCoinsColor();
                                    }
                                    else{
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
                                    //ShopsLoad.injectToLog("[Buying from Detailed Menu] " + p.getName() + " bought " + e.getCurrentItem().getAmount() + "x " + itemName + " for " + value);
                                    Utils.playSound(p, "menus.shop-buy");
                                } else
                                     PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.multiCurrencyShopsCfg.getString("not-enough-coins")));

                                clicksCache--;
                            });
                        }
                        else{
                            PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.multiCurrencyShopsCfg.getString("not-enough-space")));
                        }
                    }
                }
            }
        }

        @EventHandler
        public void onClose(InventoryCloseEvent e){
            if(e.getInventory().equals(inventory)){
                inventory=null;
                shop=null;
                background=null;
                buyValue=0;
                page=0;
                //if(limitBuy!=-1)
                //    ShopBuyLimit.playerValues.put(e.getPlayer().getUniqueId().toString(), coinsUsedBuy);
                HandlerList.unregisterAll(this);
            }
        }

    }
}
