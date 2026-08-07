package me.qKing12.RoyaleEconomy.Shops;

import me.qKing12.RoyaleEconomy.DataManager.StaticValues;
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
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;



public class ShopItemEditMenu  {
    private Inventory inventory;
    private final ClickListener listener=new ClickListener();
    private Shop shop;
    private Shop.ShopItem item;

    private ItemStack noAmount;
    private ItemStack amountItem;

    private int page;
    private int index;

    public ShopItemEditMenu(Player p, Shop shop, int page, int index){
        this.index=index;
        inventory = Bukkit.createInventory(null, 54, Utils.chat(shop.getTitle())+" - "+shop.getItems().get(index).getName());
        this.page=page;
        ItemStack background = shop.getBackgroundGlass().clone();
        this.shop=shop;
        this.item=shop.getItems().get(index);
        for(int i=0;i<54;i++) {
            inventory.setItem(i, background);
        }

        ItemStack itemStack = item.getItemStack().clone();
        ArrayList<String> lore = new ArrayList<>();
        if(itemStack.getItemMeta().getLore()!=null)
            lore=(ArrayList<String>)itemStack.getItemMeta().getLore();

        lore.add(Utils.chat("&8&m----------------------"));
        lore.add(Utils.chat("&fClick an item in your"));
        lore.add(Utils.chat("&finventory to set it here."));
        lore.add("");
        lore.add(Utils.chat("&bLeft Click&f to increase"));
        lore.add(Utils.chat("&fdefault amount."));
        lore.add(Utils.chat("&bRight Click&f to decrease"));
        lore.add(Utils.chat("&fdefault amount."));
        ItemMeta meta = itemStack.getItemMeta();
        meta.setLore(lore);
        itemStack.setItemMeta(meta);

        noAmount = new ItemStack(Material.STONE_BUTTON);
        meta=noAmount.getItemMeta();
        lore=new ArrayList<>();
        lore.add(Utils.chat("&fSince the amount is 0"));
        lore.add(Utils.chat("&fnothing will be displayed"));
        lore.add("");
        lore.add(Utils.chat("&bLeft Click &fto increase"));
        lore.add(Utils.chat("&bShift + Left Click &fto increase 10"));
        meta.setLore(lore);
        noAmount.setItemMeta(meta);

        if(RoyaleEconomy.upperVersion && !Bukkit.getVersion().contains("1.13")){
            ItemStack dataModel;
            if(item.getCustomDataModel()){
                dataModel=new ItemStack(Material.ITEM_FRAME, 1);
                meta= dataModel.getItemMeta();
                meta.setDisplayName(Utils.chat("&fCustom Model Data &aENABLED"));
                lore=new ArrayList<>();
                lore.add(Utils.chat("&fThis setting is enabled and"));
                lore.add(Utils.chat("&fit also checks for Model Data"));
                lore.add(Utils.chat("&fbesides the material!"));
                lore.add(Utils.chat("&c(For Not Detailed Mode!)"));
            }
            else{
                dataModel=new ItemStack(Material.PAPER, 1);
                meta= dataModel.getItemMeta();
                meta.setDisplayName(Utils.chat("&fCustom Model Data &cDISABLED"));
                lore=new ArrayList<>();
                lore.add(Utils.chat("&fEnable this setting and"));
                lore.add(Utils.chat("&fit will also check for Model Data"));
                lore.add(Utils.chat("&fbesides the material!"));
                lore.add(Utils.chat("&c(For Not Detailed Mode!)"));
            }
            meta.setLore(lore);
            dataModel.setItemMeta(meta);
            inventory.setItem(51, dataModel);
        }

        inventory.setItem(13, itemStack);
        inventory.setItem(49, RoyaleEconomy.staticValues.goBackShopItem);
        inventory.setItem(48, RoyaleEconomy.staticValues.deleteShopItem);
        if(item.getDetailed())
            inventory.setItem(50, RoyaleEconomy.staticValues.detailed);
        else
            inventory.setItem(50, RoyaleEconomy.staticValues.notDetailed);

        ItemStack tempValue = RoyaleEconomy.staticValues.sellValue.clone();
        meta=tempValue.getItemMeta();
        meta.setDisplayName(Utils.chat("&fSell Value: &6"+ RoyaleEconomy.messageHelper.numberFormat(item.getSellingValue())));
        tempValue.setItemMeta(meta);
        inventory.setItem(10, tempValue);

        tempValue = RoyaleEconomy.staticValues.buyPermissionItem.clone();
        meta=tempValue.getItemMeta();
        String permission = item.getBuyPermission() == null ? "None" : item.getBuyPermission();
        meta.setDisplayName(Utils.chat("&fBuy Permission: &c"+permission));
        tempValue.setItemMeta(meta);
        inventory.setItem(15, tempValue);

        tempValue = RoyaleEconomy.staticValues.buyValue.clone();
        meta=tempValue.getItemMeta();
        meta.setDisplayName(Utils.chat("&fBuy Value: &6"+ RoyaleEconomy.messageHelper.numberFormat(item.getBuyValue())));
        tempValue.setItemMeta(meta);
        inventory.setItem(16, tempValue);

        ItemStack commands = RoyaleEconomy.itemConstructor.getItemFromMaterial("137");
        meta=commands.getItemMeta();
        meta.setDisplayName(Utils.chat("&cCommands"));
        lore=new ArrayList<>();
        lore.add(Utils.chat("&fMake item execute commands"));
        lore.add(Utils.chat("&fon buy. Click to edit"));
        lore.add(Utils.chat("&fthe commands."));
        lore.add("");
        lore.add(Utils.chat("&cATTENTION! &fIf you use commands"));
        lore.add(Utils.chat("&fyou can't open the detailed menu."));
        lore.add("");
        lore.add(Utils.chat("&fCurrent Commands: "));
        ArrayList<String> commandsToExecute = item.getCommands();
        if(commandsToExecute==null){
            lore.add(Utils.chat("&c  None"));
        }
        else{
            for(String command : commandsToExecute)
                lore.add(Utils.chat("&8 - &7"+command));
        }
        meta.setLore(lore);
        commands.setItemMeta(meta);
        inventory.setItem(21, commands);

        if(item.isExclusive())
            inventory.setItem(23, RoyaleEconomy.staticValues.exclusive.clone());
        else
            inventory.setItem(23, RoyaleEconomy.staticValues.notExclusive.clone());

        amountItem = new ItemStack(Material.BOOK);
        meta=amountItem.getItemMeta();
        lore=new ArrayList<>();
        lore.add(Utils.chat("&fThe amount to display in"));
        lore.add(Utils.chat("&fthe item's buying menu."));
        lore.add("");
        lore.add(Utils.chat("&bLeft Click &fto increase"));
        lore.add(Utils.chat("&bRight Click &fto decrease"));
        lore.add("");
        lore.add(Utils.chat("&bShift + Left Click &fto increase 10"));
        lore.add(Utils.chat("&bShift + Right Click &fto decrease 10"));
        meta.setLore(lore);

        int tempAmount = item.getAmount(1);
        if(tempAmount==0){
            ItemMeta meta2=noAmount.getItemMeta();
            meta2.setDisplayName(Utils.chat("&7Amount 1"));
            noAmount.setItemMeta(meta2);
            inventory.setItem(29, noAmount);
        }
        else {
            meta.setDisplayName(Utils.chat("&aAmount 1"));
            amountItem.setItemMeta(meta);
            amountItem.setAmount(tempAmount);
            inventory.setItem(29, amountItem.clone());
        }

        tempAmount = item.getAmount(2);
        if(tempAmount==0){
            ItemMeta meta2=noAmount.getItemMeta();
            meta2.setDisplayName(Utils.chat("&7Amount 2"));
            noAmount.setItemMeta(meta2);
            inventory.setItem(30, noAmount);
        }
        else {
            meta.setDisplayName(Utils.chat("&aAmount 2"));
            amountItem.setItemMeta(meta);
            amountItem.setAmount(tempAmount);
            inventory.setItem(30, amountItem.clone());
        }

        tempAmount = item.getAmount(3);
        if(tempAmount==0){
            ItemMeta meta2=noAmount.getItemMeta();
            meta2.setDisplayName(Utils.chat("&7Amount 3"));
            noAmount.setItemMeta(meta2);
            inventory.setItem(31, noAmount);
        }
        else {
            meta.setDisplayName(Utils.chat("&aAmount 3"));
            amountItem.setItemMeta(meta);
            amountItem.setAmount(tempAmount);
            inventory.setItem(31, amountItem.clone());
        }

        tempAmount = item.getAmount(4);
        if(tempAmount==0){
            ItemMeta meta2=noAmount.getItemMeta();
            meta2.setDisplayName(Utils.chat("&7Amount 4"));
            noAmount.setItemMeta(meta2);
            inventory.setItem(32, noAmount);
        }
        else {
            meta.setDisplayName(Utils.chat("&aAmount 4"));
            amountItem.setItemMeta(meta);
            amountItem.setAmount(tempAmount);
            inventory.setItem(32, amountItem.clone());
        }

        tempAmount = item.getAmount(5);
        if(tempAmount==0){
            ItemMeta meta2=noAmount.getItemMeta();
            meta2.setDisplayName(Utils.chat("&7Amount 5"));
            noAmount.setItemMeta(meta2);
            inventory.setItem(33, noAmount);
        }
        else {
            meta.setDisplayName(Utils.chat("&aAmount 5"));
            amountItem.setItemMeta(meta);
            amountItem.setAmount(tempAmount);
            inventory.setItem(33, amountItem.clone());
        }

        ItemStack itemsToBuyDefault = new ItemStack(Material.CHEST);
        meta=itemsToBuyDefault.getItemMeta();
        meta.setDisplayName(Utils.chat("&aRequired Items | Main Display"));
        lore=new ArrayList<>();
        lore.add(Utils.chat("&fClick to manage what items"));
        lore.add(Utils.chat("&fto take from the player on buy."));
        lore.add(Utils.chat("&cATTENTION! &fthis only applies to"));
        lore.add(Utils.chat("&fbuying from main shop, you need to"));
        lore.add(Utils.chat("&fadd the items for every amount below!"));
        lore.add("");
        lore.add(Utils.chat("&fCurrent items: "));
        ArrayList<ItemStack> items = item.getDefaultItemRequirements();
        if(items==null) {
            lore.add(Utils.chat("   &cNo items needed."));
        }
        else {
            for (ItemStack item : items)
                lore.add("   " + Utils.getDisplayName(item) + Utils.chat(" &8x" + item.getAmount()));
        }
        meta.setLore(lore);
        itemsToBuyDefault.setItemMeta(meta);
        inventory.setItem(4, itemsToBuyDefault);


        itemsToBuyDefault = new ItemStack(Material.CHEST);
        meta=itemsToBuyDefault.getItemMeta();
        meta.setDisplayName(Utils.chat("&aRequired Items | Amount 1"));
        lore=new ArrayList<>();
        lore.add(Utils.chat("&fClick to manage what items"));
        lore.add(Utils.chat("&fto take from the player on buy."));
        lore.add(Utils.chat("&cATTENTION! &fthis only applies to"));
        lore.add(Utils.chat("&fbuying &camount 1&f, you need to"));
        lore.add(Utils.chat("&fadd the items for every amount"));
        lore.add(Utils.chat("&fand main display!"));
        lore.add("");
        lore.add(Utils.chat("&fCurrent items: "));
        items = item.getAmount1Items();
        if(items==null) {
            lore.add(Utils.chat("   &cNo items needed."));
        }
        else {
            for (ItemStack item : items)
                lore.add("   " + Utils.getDisplayName(item) + Utils.chat(" &8x" + item.getAmount()));
        }
        meta.setLore(lore);
        itemsToBuyDefault.setItemMeta(meta);
        inventory.setItem(38, itemsToBuyDefault);


        itemsToBuyDefault = new ItemStack(Material.CHEST);
        meta=itemsToBuyDefault.getItemMeta();
        meta.setDisplayName(Utils.chat("&aRequired Items | Amount 2"));
        lore=new ArrayList<>();
        lore.add(Utils.chat("&fClick to manage what items"));
        lore.add(Utils.chat("&fto take from the player on buy."));
        lore.add(Utils.chat("&cATTENTION! &fthis only applies to"));
        lore.add(Utils.chat("&fbuying &camount 2&f, you need to"));
        lore.add(Utils.chat("&fadd the items for every amount"));
        lore.add(Utils.chat("&fand main display!"));
        lore.add("");
        lore.add(Utils.chat("&fCurrent items: "));
        items = item.getAmount2Items();
        if(items==null) {
            lore.add(Utils.chat("   &cNo items needed."));
        }
        else {
            for (ItemStack item : items)
                lore.add("   " + Utils.getDisplayName(item) + Utils.chat(" &8x" + item.getAmount()));
        }
        meta.setLore(lore);
        itemsToBuyDefault.setItemMeta(meta);
        inventory.setItem(39, itemsToBuyDefault);


        itemsToBuyDefault = new ItemStack(Material.CHEST);
        meta=itemsToBuyDefault.getItemMeta();
        meta.setDisplayName(Utils.chat("&aRequired Items | Amount 3"));
        lore=new ArrayList<>();
        lore.add(Utils.chat("&fClick to manage what items"));
        lore.add(Utils.chat("&fto take from the player on buy."));
        lore.add(Utils.chat("&cATTENTION! &fthis only applies to"));
        lore.add(Utils.chat("&fbuying &camount 3&f, you need to"));
        lore.add(Utils.chat("&fadd the items for every amount"));
        lore.add(Utils.chat("&fand main display!"));
        lore.add("");
        lore.add(Utils.chat("&fCurrent items: "));
        items = item.getAmount3Items();
        if(items==null) {
            lore.add(Utils.chat("   &cNo items needed."));
        }
        else {
            for (ItemStack item : items)
                lore.add("   " + Utils.getDisplayName(item) + Utils.chat(" &8x" + item.getAmount()));
        }
        meta.setLore(lore);
        itemsToBuyDefault.setItemMeta(meta);
        inventory.setItem(40, itemsToBuyDefault);


        itemsToBuyDefault = new ItemStack(Material.CHEST);
        meta=itemsToBuyDefault.getItemMeta();
        meta.setDisplayName(Utils.chat("&aRequired Items | Amount 4"));
        lore=new ArrayList<>();
        lore.add(Utils.chat("&fClick to manage what items"));
        lore.add(Utils.chat("&fto take from the player on buy."));
        lore.add(Utils.chat("&cATTENTION! &fthis only applies to"));
        lore.add(Utils.chat("&fbuying &camount 4&f, you need to"));
        lore.add(Utils.chat("&fadd the items for every amount"));
        lore.add(Utils.chat("&fand main display!"));
        lore.add("");
        lore.add(Utils.chat("&fCurrent items: "));
        items = item.getAmount4Items();
        if(items==null) {
            lore.add(Utils.chat("   &cNo items needed."));
        }
        else {
            for (ItemStack item : items)
                lore.add("   " + Utils.getDisplayName(item) + Utils.chat(" &8x" + item.getAmount()));
        }
        meta.setLore(lore);
        itemsToBuyDefault.setItemMeta(meta);
        inventory.setItem(41, itemsToBuyDefault);


        itemsToBuyDefault = new ItemStack(Material.CHEST);
        meta=itemsToBuyDefault.getItemMeta();
        meta.setDisplayName(Utils.chat("&aRequired Items | Amount 5"));
        lore=new ArrayList<>();
        lore.add(Utils.chat("&fClick to manage what items"));
        lore.add(Utils.chat("&fto take from the player on buy."));
        lore.add(Utils.chat("&cATTENTION! &fthis only applies to"));
        lore.add(Utils.chat("&fbuying &camount 5&f, you need to"));
        lore.add(Utils.chat("&fadd the items for every amount"));
        lore.add(Utils.chat("&fand main display!"));
        lore.add("");
        lore.add(Utils.chat("&fCurrent items: "));
        items = item.getAmount5Items();
        if(items==null) {
            lore.add(Utils.chat("   &cNo items needed."));
        }
        else {
            for (ItemStack item : items)
                lore.add("   " + Utils.getDisplayName(item) + Utils.chat(" &8x" + item.getAmount()));
        }
        meta.setLore(lore);
        itemsToBuyDefault.setItemMeta(meta);
        inventory.setItem(42, itemsToBuyDefault);

        inventory.setItem(28, item.getDetailedMenuMode()==0?RoyaleEconomy.staticValues.detailedMenuModeNotOnly :RoyaleEconomy.staticValues.detaimedMenuModeOnly);


        RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runNextTick((task) -> p.openInventory(inventory));

        Bukkit.getPluginManager().registerEvents(listener, RoyaleEconomy.plugin);
    }

    private class ClickListener implements Listener {

        @EventHandler
        public void onClick(InventoryClickEvent e){
            if(e.getSlot()<0 || e.getCurrentItem()==null || e.getCurrentItem().getType().equals(Material.AIR))
                return;
            //if(inventory.getViewers().isEmpty()) RoyaleEconomy.plugin.getLogger().warning("A closed listener is still active.");
            if(e.getInventory().equals(inventory)){
                e.setCancelled(true);
                if(!e.getClickedInventory().equals(e.getWhoClicked().getInventory())){
                    Player p = (Player) e.getWhoClicked();
                    if(e.getSlot()==13){
                        if(e.getClick().equals(ClickType.LEFT)){
                            if(e.getCurrentItem().getAmount()!=64) {
                                e.getCurrentItem().setAmount(e.getCurrentItem().getAmount() + 1);
                                item.getItemStack().setAmount(inventory.getItem(13).getAmount());
                            }
                        }
                        else if(e.getClick().equals(ClickType.RIGHT)){
                            if(e.getCurrentItem().getAmount()!=1) {
                                e.getCurrentItem().setAmount(e.getCurrentItem().getAmount() - 1);
                                item.getItemStack().setAmount(inventory.getItem(13).getAmount());
                            }
                        }
                    }
                    if(e.getSlot()==50){
                        if(item.getDetailed()){
                            item.setDetailed(false);
                            inventory.setItem(50, RoyaleEconomy.staticValues.notDetailed);
                        }
                        else{
                            item.setDetailed(true);
                            inventory.setItem(50, RoyaleEconomy.staticValues.detailed);
                        }
                    }
                    else if(e.getSlot()==28){
                        if(item.getDetailedMenuMode()==0){
                            item.setDetailedMenuMode(1);
                            inventory.setItem(28, RoyaleEconomy.staticValues.detaimedMenuModeOnly);
                        }
                        else{
                            item.setDetailedMenuMode(0);
                            inventory.setItem(28, RoyaleEconomy.staticValues.detailedMenuModeNotOnly);
                        }
                    }
                    else if(e.getSlot()==51){
                        if(RoyaleEconomy.upperVersion && !Bukkit.getVersion().contains("1.13")){
                            ItemStack dataModel;
                            ItemMeta meta;
                            ArrayList<String> lore;
                            if(item.getCustomDataModel()){
                                item.setCustomDataModel(false);
                                dataModel=new ItemStack(Material.PAPER, 1);
                                meta= dataModel.getItemMeta();
                                meta.setDisplayName(Utils.chat("&fCustom Model Data &cDISABLED"));
                                lore=new ArrayList<>();
                                lore.add(Utils.chat("&fEnable this setting and"));
                                lore.add(Utils.chat("&fit will also check for Model Data"));
                            }
                            else{
                                item.setCustomDataModel(true);
                                dataModel=new ItemStack(Material.ITEM_FRAME, 1);
                                meta= dataModel.getItemMeta();
                                meta.setDisplayName(Utils.chat("&fCustom Model Data &aENABLED"));
                                lore=new ArrayList<>();
                                lore.add(Utils.chat("&fThis setting is enabled and"));
                                lore.add(Utils.chat("&fit also checks for Model Data"));
                            }
                            lore.add(Utils.chat("&fbesides the material!"));
                            lore.add(Utils.chat("&c(For Not Detailed Mode!)"));
                            meta.setLore(lore);
                            dataModel.setItemMeta(meta);
                            inventory.setItem(51, dataModel);
                        }
                    }
                    else if(e.getSlot()==49){
                        new ShopEditorMenu(p, shop, page);
                    }
                    else if(e.getSlot()==48 && e.getClick().equals(ClickType.DOUBLE_CLICK)){
                        p.closeInventory();
                        item.remove();
                        PlayerMessageHandler.messageSend(p, Utils.chat("&aItem successfully removed!"));
                        new ShopEditorMenu(p, shop, page);
                    }
                    else if(e.getSlot()==21){
                        new ShopItemCommandsMenu(p, shop, page, index);
                    }
                    else if(e.getSlot()==23){
                        if(item.isExclusive()){
                            item.setExclusive(false);
                            inventory.setItem(23, RoyaleEconomy.staticValues.notExclusive);
                        }
                        else{
                            item.setExclusive(true);
                            inventory.setItem(23, RoyaleEconomy.staticValues.exclusive);
                        }
                    }
                    else if(e.getSlot()==10){
                        p.closeInventory();
                        PlayerMessageHandler.messageSend(p, Utils.chat("&7&m-------------------------"));
                        PlayerMessageHandler.messageSend(p, Utils.chat("Please enter the &asell value&f!"));
                        PlayerMessageHandler.messageSend(p, Utils.chat("Set it to 0 to disable selling!"));
                        new ChatListener(p, (reply) -> {
                            reply = reply.replaceAll("[^A-Za-z0-9.,-]+", "");
                            try {
                                double amount = Math.abs(RoyaleEconomy.messageHelper.getCoinsFromFormat(reply));
                                item.setSellingValue(amount);
                                PlayerMessageHandler.messageSend(p, Utils.chat("&aSelling value updated!"));
                                RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runNextTick((task) -> new ShopItemEditMenu(p, shop, page, index));
                            } catch (Exception x) {
                                PlayerMessageHandler.messageSend(p, Utils.chat("&cAmount is not valid"));
                            }
                        }, false);
                    }
                    else if (e.getSlot()==15) {
                        p.closeInventory();
                        PlayerMessageHandler.messageSend(p, Utils.chat("&7&m-------------------------"));
                        PlayerMessageHandler.messageSend(p, Utils.chat("Please enter the &apermission to buy&f!"));
                        PlayerMessageHandler.messageSend(p, Utils.chat("Type &cnone&f to disable permission!"));
                        new ChatListener(p, (reply) -> {
                            reply = reply.replaceAll("[^A-Za-z0-9.,-]+", "");
                            try {
                                item.setBuyPermission(reply);
                                PlayerMessageHandler.messageSend(p, Utils.chat("&aPermission to buy updated!"));
                                RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runNextTick((task) -> new ShopItemEditMenu(p, shop, page, index));
                            } catch (Exception x) {
                                PlayerMessageHandler.messageSend(p, Utils.chat("&cPermission is not valid"));
                            }
                        }, false);
                    }
                    else if(e.getSlot()==16){
                        p.closeInventory();
                        PlayerMessageHandler.messageSend(p, Utils.chat("&7&m-------------------------"));
                        PlayerMessageHandler.messageSend(p, Utils.chat("Please enter the &abuy value&f!"));
                        new ChatListener(p, (reply) -> {
                            reply = reply.replaceAll("[^A-Za-z0-9.,-]+", "");
                            try {
                                double amount = RoyaleEconomy.messageHelper.getCoinsFromFormat(reply);
                                item.setBuyValue(amount);
                                PlayerMessageHandler.messageSend(p, Utils.chat("&aBuying value updated!"));
                                RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runNextTick((task) -> new ShopItemEditMenu(p, shop, page, index));
                            } catch (Exception x) {
                                PlayerMessageHandler.messageSend(p, Utils.chat("&cAmount is not valid"));
                            }
                        }, false);
                    }
                    else if(e.getSlot()==29){
                        if(e.getClick().equals(ClickType.LEFT)){
                            if(e.getCurrentItem().getType().equals(Material.BOOK)) {
                                if (e.getCurrentItem().getAmount() != 64)
                                    e.getCurrentItem().setAmount(e.getCurrentItem().getAmount() + 1);
                            }
                            else{
                                ItemMeta meta2 = amountItem.getItemMeta();
                                meta2.setDisplayName(Utils.chat("&aAmount 1"));
                                amountItem.setItemMeta(meta2);
                                amountItem.setAmount(1);
                                inventory.setItem(29, amountItem.clone());
                            }
                        }
                        else if(e.getClick().equals(ClickType.RIGHT)){
                            if(e.getCurrentItem().getType().equals(Material.BOOK)){
                                if(e.getCurrentItem().getAmount()==1){
                                    ItemMeta meta2=noAmount.getItemMeta();
                                    meta2.setDisplayName(Utils.chat("&7Amount 1"));
                                    noAmount.setItemMeta(meta2);
                                    inventory.setItem(29, noAmount);
                                }
                                else
                                    e.getCurrentItem().setAmount(e.getCurrentItem().getAmount()-1);
                            }
                        }
                        else if(e.getClick().equals(ClickType.SHIFT_LEFT)){
                            if(e.getCurrentItem().getType().equals(Material.BOOK)) {
                                if (e.getCurrentItem().getAmount() + 10 <= 64)
                                    e.getCurrentItem().setAmount(e.getCurrentItem().getAmount() + 10);
                                else
                                    e.getCurrentItem().setAmount(64);
                            }
                            else{
                                ItemMeta meta2 = amountItem.getItemMeta();
                                meta2.setDisplayName(Utils.chat("&aAmount 1"));
                                amountItem.setItemMeta(meta2);
                                amountItem.setAmount(10);
                                inventory.setItem(29, amountItem.clone());
                            }
                        }
                        else if(e.getClick().equals(ClickType.SHIFT_RIGHT)){
                            if(e.getCurrentItem().getType().equals(Material.BOOK)){
                                if(e.getCurrentItem().getAmount()-10<1){
                                    ItemMeta meta2=noAmount.getItemMeta();
                                    meta2.setDisplayName(Utils.chat("&7Amount 1"));
                                    noAmount.setItemMeta(meta2);
                                    inventory.setItem(29, noAmount);
                                }
                                else
                                    e.getCurrentItem().setAmount(e.getCurrentItem().getAmount()-10);
                            }
                        }
                    }
                    else if(e.getSlot()==30){
                        if(e.getClick().equals(ClickType.LEFT)){
                            if(e.getCurrentItem().getType().equals(Material.BOOK)) {
                                if (e.getCurrentItem().getAmount() != 64)
                                    e.getCurrentItem().setAmount(e.getCurrentItem().getAmount() + 1);
                            }
                            else{
                                ItemMeta meta2 = amountItem.getItemMeta();
                                meta2.setDisplayName(Utils.chat("&aAmount 2"));
                                amountItem.setItemMeta(meta2);
                                amountItem.setAmount(1);
                                inventory.setItem(30, amountItem.clone());
                            }
                        }
                        else if(e.getClick().equals(ClickType.RIGHT)){
                            if(e.getCurrentItem().getType().equals(Material.BOOK)){
                                if(e.getCurrentItem().getAmount()==1){
                                    ItemMeta meta2=noAmount.getItemMeta();
                                    meta2.setDisplayName(Utils.chat("&7Amount 2"));
                                    noAmount.setItemMeta(meta2);
                                    inventory.setItem(30, noAmount);
                                }
                                else
                                    e.getCurrentItem().setAmount(e.getCurrentItem().getAmount()-1);
                            }
                        }
                        else if(e.getClick().equals(ClickType.SHIFT_LEFT)){
                            if(e.getCurrentItem().getType().equals(Material.BOOK)) {
                                if (e.getCurrentItem().getAmount() + 10 <= 64)
                                    e.getCurrentItem().setAmount(e.getCurrentItem().getAmount() + 10);
                                else
                                    e.getCurrentItem().setAmount(64);
                            }
                            else{
                                ItemMeta meta2 = amountItem.getItemMeta();
                                meta2.setDisplayName(Utils.chat("&aAmount 2"));
                                amountItem.setItemMeta(meta2);
                                amountItem.setAmount(10);
                                inventory.setItem(30, amountItem.clone());
                            }
                        }
                        else if(e.getClick().equals(ClickType.SHIFT_RIGHT)){
                            if(e.getCurrentItem().getType().equals(Material.BOOK)){
                                if(e.getCurrentItem().getAmount()-10<1){
                                    ItemMeta meta2=noAmount.getItemMeta();
                                    meta2.setDisplayName(Utils.chat("&7Amount 2"));
                                    noAmount.setItemMeta(meta2);
                                    inventory.setItem(30, noAmount);
                                }
                                else
                                    e.getCurrentItem().setAmount(e.getCurrentItem().getAmount()-10);
                            }
                        }
                    }
                    else if(e.getSlot()==31){
                        if(e.getClick().equals(ClickType.LEFT)){
                            if(e.getCurrentItem().getType().equals(Material.BOOK)) {
                                if (e.getCurrentItem().getAmount() != 64)
                                    e.getCurrentItem().setAmount(e.getCurrentItem().getAmount() + 1);
                            }
                            else{
                                ItemMeta meta2 = amountItem.getItemMeta();
                                meta2.setDisplayName(Utils.chat("&aAmount 3"));
                                amountItem.setItemMeta(meta2);
                                amountItem.setAmount(1);
                                inventory.setItem(31, amountItem.clone());
                            }
                        }
                        else if(e.getClick().equals(ClickType.RIGHT)){
                            if(e.getCurrentItem().getType().equals(Material.BOOK)){
                                if(e.getCurrentItem().getAmount()==1){
                                    ItemMeta meta2=noAmount.getItemMeta();
                                    meta2.setDisplayName(Utils.chat("&7Amount 3"));
                                    noAmount.setItemMeta(meta2);
                                    inventory.setItem(31, noAmount);
                                }
                                else
                                    e.getCurrentItem().setAmount(e.getCurrentItem().getAmount()-1);
                            }
                        }
                        else if(e.getClick().equals(ClickType.SHIFT_LEFT)){
                            if(e.getCurrentItem().getType().equals(Material.BOOK)) {
                                if (e.getCurrentItem().getAmount() + 10 <= 64)
                                    e.getCurrentItem().setAmount(e.getCurrentItem().getAmount() + 10);
                                else
                                    e.getCurrentItem().setAmount(64);
                            }
                            else{
                                ItemMeta meta2 = amountItem.getItemMeta();
                                meta2.setDisplayName(Utils.chat("&aAmount 3"));
                                amountItem.setItemMeta(meta2);
                                amountItem.setAmount(10);
                                inventory.setItem(31, amountItem.clone());
                            }
                        }
                        else if(e.getClick().equals(ClickType.SHIFT_RIGHT)){
                            if(e.getCurrentItem().getType().equals(Material.BOOK)){
                                if(e.getCurrentItem().getAmount()-10<1){
                                    ItemMeta meta2=noAmount.getItemMeta();
                                    meta2.setDisplayName(Utils.chat("&7Amount 3"));
                                    noAmount.setItemMeta(meta2);
                                    inventory.setItem(31, noAmount);
                                }
                                else
                                    e.getCurrentItem().setAmount(e.getCurrentItem().getAmount()-10);
                            }
                        }
                    }
                    else if(e.getSlot()==32){
                        if(e.getClick().equals(ClickType.LEFT)){
                            if(e.getCurrentItem().getType().equals(Material.BOOK)) {
                                if (e.getCurrentItem().getAmount() != 64)
                                    e.getCurrentItem().setAmount(e.getCurrentItem().getAmount() + 1);
                            }
                            else{
                                ItemMeta meta2 = amountItem.getItemMeta();
                                meta2.setDisplayName(Utils.chat("&aAmount 4"));
                                amountItem.setItemMeta(meta2);
                                amountItem.setAmount(1);
                                inventory.setItem(32, amountItem.clone());
                            }
                        }
                        else if(e.getClick().equals(ClickType.RIGHT)){
                            if(e.getCurrentItem().getType().equals(Material.BOOK)){
                                if(e.getCurrentItem().getAmount()==1){
                                    ItemMeta meta2=noAmount.getItemMeta();
                                    meta2.setDisplayName(Utils.chat("&7Amount 4"));
                                    noAmount.setItemMeta(meta2);
                                    inventory.setItem(32, noAmount);
                                }
                                else
                                    e.getCurrentItem().setAmount(e.getCurrentItem().getAmount()-1);
                            }
                        }
                        else if(e.getClick().equals(ClickType.SHIFT_LEFT)){
                            if(e.getCurrentItem().getType().equals(Material.BOOK)) {
                                if (e.getCurrentItem().getAmount() + 10 <= 64)
                                    e.getCurrentItem().setAmount(e.getCurrentItem().getAmount() + 10);
                                else
                                    e.getCurrentItem().setAmount(64);
                            }
                            else{
                                ItemMeta meta2 = amountItem.getItemMeta();
                                meta2.setDisplayName(Utils.chat("&aAmount 4"));
                                amountItem.setItemMeta(meta2);
                                amountItem.setAmount(10);
                                inventory.setItem(32, amountItem.clone());
                            }
                        }
                        else if(e.getClick().equals(ClickType.SHIFT_RIGHT)){
                            if(e.getCurrentItem().getType().equals(Material.BOOK)){
                                if(e.getCurrentItem().getAmount()-10<1){
                                    ItemMeta meta2=noAmount.getItemMeta();
                                    meta2.setDisplayName(Utils.chat("&7Amount 4"));
                                    noAmount.setItemMeta(meta2);
                                    inventory.setItem(32, noAmount);
                                }
                                else
                                    e.getCurrentItem().setAmount(e.getCurrentItem().getAmount()-10);
                            }
                        }
                    }
                    else if(e.getSlot()==33){
                        if(e.getClick().equals(ClickType.LEFT)){
                            if(e.getCurrentItem().getType().equals(Material.BOOK)) {
                                if (e.getCurrentItem().getAmount() != 64)
                                    e.getCurrentItem().setAmount(e.getCurrentItem().getAmount() + 1);
                            }
                            else{
                                ItemMeta meta2 = amountItem.getItemMeta();
                                meta2.setDisplayName(Utils.chat("&aAmount 5"));
                                amountItem.setItemMeta(meta2);
                                amountItem.setAmount(1);
                                inventory.setItem(33, amountItem.clone());
                            }
                        }
                        else if(e.getClick().equals(ClickType.RIGHT)){
                            if(e.getCurrentItem().getType().equals(Material.BOOK)){
                                if(e.getCurrentItem().getAmount()==1){
                                    ItemMeta meta2=noAmount.getItemMeta();
                                    meta2.setDisplayName(Utils.chat("&7Amount 5"));
                                    noAmount.setItemMeta(meta2);
                                    inventory.setItem(33, noAmount);
                                }
                                else
                                    e.getCurrentItem().setAmount(e.getCurrentItem().getAmount()-1);
                            }
                        }
                        else if(e.getClick().equals(ClickType.SHIFT_LEFT)){
                            if(e.getCurrentItem().getType().equals(Material.BOOK)) {
                                if (e.getCurrentItem().getAmount() + 10 <= 64)
                                    e.getCurrentItem().setAmount(e.getCurrentItem().getAmount() + 10);
                                else
                                    e.getCurrentItem().setAmount(64);
                            }
                            else{
                                ItemMeta meta2 = amountItem.getItemMeta();
                                meta2.setDisplayName(Utils.chat("&aAmount 5"));
                                amountItem.setItemMeta(meta2);
                                amountItem.setAmount(10);
                                inventory.setItem(33, amountItem.clone());
                            }
                        }
                        else if(e.getClick().equals(ClickType.SHIFT_RIGHT)){
                            if(e.getCurrentItem().getType().equals(Material.BOOK)){
                                if(e.getCurrentItem().getAmount()-10<1){
                                    ItemMeta meta2=noAmount.getItemMeta();
                                    meta2.setDisplayName(Utils.chat("&7Amount 5"));
                                    noAmount.setItemMeta(meta2);
                                    inventory.setItem(33, noAmount);
                                }
                                else
                                    e.getCurrentItem().setAmount(e.getCurrentItem().getAmount()-10);
                            }
                        }
                    }
                    else if(e.getSlot()==4){
                        new ShopItemsRequirementsMenu(p, shop, page, index, "Main Display");
                    }
                    else if(e.getSlot()==38){
                        new ShopItemsRequirementsMenu(p, shop, page, index, "Amount 1");
                    }
                    else if(e.getSlot()==39){
                        new ShopItemsRequirementsMenu(p, shop, page, index, "Amount 2");
                    }
                    else if(e.getSlot()==40){
                        new ShopItemsRequirementsMenu(p, shop, page, index, "Amount 3");
                    }
                    else if(e.getSlot()==41){
                        new ShopItemsRequirementsMenu(p, shop, page, index, "Amount 4");
                    }
                    else if(e.getSlot()==42){
                        new ShopItemsRequirementsMenu(p, shop, page, index, "Amount 5");
                    }
                }
                else{
                    ItemStack toSet=e.getCurrentItem().clone();
                    toSet.setAmount(1);
                    item.setItemStack(toSet.clone());
                    ArrayList<String> lore = new ArrayList<>();
                    if(toSet.getItemMeta().getLore()!=null)
                        lore=(ArrayList<String>)toSet.getItemMeta().getLore();

                    lore.add(Utils.chat("&8&m----------------------"));
                    lore.add(Utils.chat("&fClick an item in your"));
                    lore.add(Utils.chat("&finventory to set it here."));
                    lore.add("");
                    lore.add(Utils.chat("&bLeft Click&f to increase"));
                    lore.add(Utils.chat("&fdefault amount."));
                    lore.add(Utils.chat("&bRight Click&f to decrease"));
                    lore.add(Utils.chat("&fdefault amount."));
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
                ItemStack temp=inventory.getItem(29);
                if(temp.getType().equals(Material.BOOK))
                    item.setAmount(1, inventory.getItem(29).getAmount());
                else
                    item.setAmount(1, 0);

                temp=inventory.getItem(30);
                if(temp.getType().equals(Material.BOOK))
                    item.setAmount(2, inventory.getItem(30).getAmount());
                else
                    item.setAmount(2, 0);

                temp=inventory.getItem(31);
                if(temp.getType().equals(Material.BOOK))
                    item.setAmount(3, inventory.getItem(31).getAmount());
                else
                    item.setAmount(3, 0);

                temp=inventory.getItem(32);
                if(temp.getType().equals(Material.BOOK))
                    item.setAmount(4, inventory.getItem(32).getAmount());
                else
                    item.setAmount(4, 0);

                temp=inventory.getItem(33);
                if(temp.getType().equals(Material.BOOK))
                    item.setAmount(5, inventory.getItem(33).getAmount());
                else
                    item.setAmount(5, 0);

                shop.saveConfig();
                inventory=null;
            }
        }

    }
}
