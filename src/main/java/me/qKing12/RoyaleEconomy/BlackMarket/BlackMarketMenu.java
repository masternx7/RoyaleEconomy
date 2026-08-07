package me.qKing12.RoyaleEconomy.BlackMarket;

import me.qKing12.RoyaleEconomy.CustomMenuItems.CustomItem;
import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import me.qKing12.RoyaleEconomy.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static me.qKing12.RoyaleEconomy.RoyaleEconomy.customItemsHandler;
import static me.qKing12.RoyaleEconomy.RoyaleEconomy.staticValues;

public class BlackMarketMenu {
    public static ConcurrentHashMap<Integer, BlackMarketItem> items=new ConcurrentHashMap<>();
    public static ItemStack noDisplay;
    private static Inventory inventory;
    private static List<Integer> closeSlot;

    public static void generateInventory(){
        int size=RoyaleEconomy.blackMarketCfg.getInt("blackmarket-menu-size");
        inventory= Bukkit.createInventory(null, size, Utils.chat(RoyaleEconomy.blackMarketCfg.getString("blackmarket-menu-name")));

        try {
            ItemStack background = RoyaleEconomy.itemConstructor.getItem(RoyaleEconomy.blackMarketCfg.getString("blackmarket-background-item"), " ", new ArrayList<>());
            for (int i = 0; i < size; i++)
                inventory.setItem(i, background);
        }catch(Exception x){

        }

        closeSlot = staticValues.loadSlots(RoyaleEconomy.blackMarketCfg, "close-menu-slot");

        ArrayList<String> lore=new ArrayList<>();
        for(String line : RoyaleEconomy.blackMarketCfg.getStringList("close-menu-item.lore"))
            lore.add(Utils.chat(line));
        for (int slot : closeSlot)
            inventory.setItem(slot, RoyaleEconomy.itemConstructor.getItem(RoyaleEconomy.blackMarketCfg.getString("close-menu-item.material"), Utils.chat(RoyaleEconomy.blackMarketCfg.getString("close-menu-item.name")), lore));

        HashMap<Integer, CustomItem> customItems = customItemsHandler.getItems("black-market-menu");
        if(customItems!=null) {
            for (Map.Entry<Integer, CustomItem> item : customItems.entrySet())
                inventory.setItem(item.getKey(), item.getValue().getItem());
        }

        regenerateItems();
    }

    public static void saveCache(){
        ArrayList<String> cache=new ArrayList<>();
        for(BlackMarketItem item : items.values()){
            cache.add(item.id+" "+item.stock);
        }
        BlackMarket.config.set("items-cache", cache);
        try {
            BlackMarket.config.save(BlackMarket.file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void placeRefreshItem(boolean ignoreViewers) {
        if (!ignoreViewers) {
            if (inventory == null || inventory.getViewers().size() == 0) {
                return;
            }
        }

        String time;
        try {
            time = RoyaleEconomy.messageHelper.formatTimeDetailed((BlackMarket.date - ZonedDateTime.now().toEpochSecond()) * 1000);
        } catch (Exception x){
            time = "N/A";
        }

        ItemStack item = RoyaleEconomy.staticValues.blackMarketRefreshItem.clone();
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(Utils.chat(RoyaleEconomy.blackMarketCfg.getString("refresh-item.name", "").replace("%time%", time)));

        ArrayList<String> lore = new ArrayList<>();
        for (String line : RoyaleEconomy.blackMarketCfg.getStringList("refresh-item.lore")) {
            lore.add(Utils.chat(line.replace("%time%", time)));
        }

        meta.setLore(lore);
        item.setItemMeta(meta);

        for (Integer slot : RoyaleEconomy.staticValues.blackMarketRefreshItemSlot) {
            BlackMarketMenu.inventory.setItem(slot, item);
        }
    }

    public static void regenerateItems(){
        if(!BlackMarket.config.getStringList("items-cache").isEmpty()){
            ArrayList<BlackMarketItem> itemsCopy=new ArrayList<>();
            for(String line : BlackMarket.config.getStringList("items-cache")){
                try {
                    String[] separated = line.split(" ");
                    BlackMarketItem item = BlackMarketItem.getItemByName(separated[0]);
                    if (item != null) {
                        item.stock = Integer.parseInt(separated[1]);
                        itemsCopy.add(item);
                    }
                }catch(Exception x){
                    x.printStackTrace();
                }
            }
            BlackMarket.config.set("items-cache", null);
            try {
                BlackMarket.config.save(BlackMarket.file);
            } catch (IOException e) {
                e.printStackTrace();
            }
            for (Integer slot : BlackMarket.slots) {
                if (itemsCopy.isEmpty()) {
                    items.remove(slot);
                    inventory.setItem(slot, noDisplay);
                } else {
                    BlackMarketItem item = itemsCopy.get(0);
                    items.put(slot, item);
                    inventory.setItem(slot, item.getDisplayItem());
                    itemsCopy.remove(item);
                }
            }
        }
        else {
            ArrayList<BlackMarketItem> itemsCopy=new ArrayList<>(BlackMarketItem.blackMarketItems);
            Collections.shuffle(itemsCopy);

            for (Integer slot : BlackMarket.slots) {
                if (itemsCopy.isEmpty()) {
                    items.remove(slot);
                    inventory.setItem(slot, noDisplay);
                } else {
                    BlackMarketItem item = BlackMarket.getRandomItem(itemsCopy);
                    if(item.itemStack==null)
                        continue;
                    item.stock= item.maximumStock;
                    items.put(slot, item);
                    inventory.setItem(slot, item.getDisplayItem());
                    itemsCopy.remove(item);
                }
            }
        }
    }

    Player player;
    ClickListen clickListen=new ClickListen();

    public BlackMarketMenu(Player p) {
        player=p;
        p.openInventory(inventory);
        Bukkit.getPluginManager().registerEvents(clickListen, RoyaleEconomy.plugin);
    }

    public boolean buyItem(BlackMarketItem item){
        if(item.currency!=null){
            return item.currency.removeAmount(player.getUniqueId().toString(), item.price);
        }
        return RoyaleEconomy.dataManager.removeMoneyFromFile(player.getUniqueId().toString(), item.price);
    }

    public static boolean clickingCooldown=false;
    public class ClickListen implements Listener {

        @EventHandler
        public void onClick(InventoryClickEvent e){
            if(!e.getInventory().equals(inventory))
                return;
            e.setCancelled(true);
            if(e.getSlot()<0) {
                return;
            }
            if(e.getWhoClicked().equals(player)){
                if(e.getClickedInventory().equals(inventory)) {

                    if(customItemsHandler.tryClick("black-market-menu", e.getSlot(), player)){
                        return;
                    }

                    //e.setCancelled(true);
                    if (closeSlot.contains(e.getSlot()))
                        player.closeInventory();
                    else if (items.containsKey(e.getSlot())) {
                        if(clickingCooldown){
                            player.sendMessage(BlackMarket.clickingCooldown);
                            return;
                        }
                        if(player.getInventory().firstEmpty()==-1) {
                            player.sendMessage(BlackMarket.fullInventory);
                            return;
                        }
                        clickingCooldown=true;
                        try {
                            BlackMarketItem item = items.get(e.getSlot());
                            if (item.maximumStock != 0) {
                                if (item.stock == 0) {
                                    player.sendMessage(BlackMarket.finishedStock);
                                    clickingCooldown=false;
                                    return;
                                }
                            }
                            if (buyItem(item)) {
                                if(BlackMarket.useScam){
                                    double scam=BlackMarket.chanceScam;
                                    if(item.scamChance!=-1)
                                        scam=item.scamChance;
                                    Random rand=new Random();
                                    if(rand.nextInt(100)<scam){
                                        player.sendMessage(BlackMarket.scamMessage);
                                        clickingCooldown=false;
                                        return;
                                    }
                                }
                                if (item.stock > 0) {
                                    item.stock--;
                                    inventory.setItem(e.getSlot(), item.getDisplayItem());
                                }

                                if (item.getCommands() != null) {
                                    RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runNextTick((task) -> {
                                        for (String command : item.getCommands()) {
                                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", player.getName()).replace("%price%", String.valueOf(item.price)));
                                        }
                                    });
                                    if(item.getGiveItemOnBuy())
                                        player.getInventory().addItem(item.itemStack);
                                }
                                else
                                    player.getInventory().addItem(item.itemStack);
                                player.sendMessage(BlackMarket.buyMessage.replace("%amount%", String.valueOf(item.itemStack.getAmount())).replace("%item-name%", Utils.getDisplayName(item.itemStack)));
                            }
                            else{
                                player.sendMessage(BlackMarket.notEnoughMoney);
                            }
                        }catch(Exception x){
                            x.printStackTrace();
                        }
                        clickingCooldown=false;
                    }
                }
            }
        }

        @EventHandler
        public void onClose(InventoryCloseEvent e){
            if(e.getInventory().equals(inventory) && e.getPlayer().equals(player)){
                HandlerList.unregisterAll(this);
            }
        }
    }
}
