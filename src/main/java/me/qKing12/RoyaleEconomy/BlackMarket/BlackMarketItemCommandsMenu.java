package me.qKing12.RoyaleEconomy.BlackMarket;

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
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

public class BlackMarketItemCommandsMenu {
    private Inventory inventory;
    private BlackMarketItem item;
    private Player p;
    private int page;
    private ArrayList<String> commands;

    public BlackMarketItemCommandsMenu(Player p, BlackMarketItem item, int page){
        this.p=p;
        this.item=item;
        this.page=page;
        this.commands = item.getCommands();
        boolean giveItemOnBuy=item.getGiveItemOnBuy();
        inventory = Bukkit.createInventory(null, 45, Utils.chat("&8Commands Setup"));

        if(RoyaleEconomy.staticValues.backgroundGlass!=null)
            for(int i=0;i<45;i++){
                inventory.setItem(i, RoyaleEconomy.staticValues.backgroundGlass.clone());
            }

        ItemStack commands = RoyaleEconomy.itemConstructor.getItemFromMaterial("137");
        ItemMeta meta=commands.getItemMeta();
        meta.setDisplayName(Utils.chat("&cCommands"));
        ArrayList<String> lore=new ArrayList<>();
        lore.add(Utils.chat("&fMake item execute"));
        lore.add(Utils.chat("&fcommands on buy"));
        lore.add("");
        lore.add(Utils.chat("&fCurrent Commands:"));
        if(this.commands==null){
            lore.add(Utils.chat("&c  None"));
        }
        else{
            int i=1;
            for(String command : this.commands) {
                lore.add(Utils.chat("&8 (&b"+i+"&8) &7" + command));
            }
        }
        meta.setLore(lore);
        commands.setItemMeta(meta);
        inventory.setItem(13, commands);

        ItemStack addCommands = RoyaleEconomy.itemConstructor.getItemFromMaterial("159:5");
        meta=addCommands.getItemMeta();
        meta.setDisplayName(Utils.chat("&aAdd Command"));
        lore=new ArrayList<>();
        lore.add(Utils.chat("&fClick to add a command"));
        lore.add(Utils.chat("&fto the buy commands list."));
        lore.add("");
        lore.add(Utils.chat("&fUse &b%player% &ffor player name."));
        lore.add(Utils.chat("&fUse &b%price% &ffor the buying price."));
        meta.setLore(lore);
        addCommands.setItemMeta(meta);
        inventory.setItem(11, addCommands);

        ItemStack removeCommands = RoyaleEconomy.itemConstructor.getItemFromMaterial("159:14");
        meta=removeCommands.getItemMeta();
        meta.setDisplayName(Utils.chat("&cRemove Command"));
        lore=new ArrayList<>();
        lore.add(Utils.chat("&fClick to remove a command"));
        lore.add(Utils.chat("&ffrom the buy commands list."));
        lore.add("");
        lore.add(Utils.chat("&fEach command has a number asigned."));
        lore.add(Utils.chat("&fCheck the number of the command"));
        lore.add(Utils.chat("&fyou want to remove and enter it here."));
        meta.setLore(lore);
        removeCommands.setItemMeta(meta);
        inventory.setItem(15, removeCommands);

        ItemStack resetCommands = new ItemStack(Material.BARRIER);
        meta=resetCommands.getItemMeta();
        meta.setDisplayName(Utils.chat("&cReset Commands"));
        lore=new ArrayList<>();
        lore.add(Utils.chat("&fClick to remove all commands."));
        meta.setLore(lore);
        resetCommands.setItemMeta(meta);
        inventory.setItem(21, resetCommands);

        ItemStack giveItemStatus;
        if(this.commands==null || giveItemOnBuy){
            giveItemStatus=RoyaleEconomy.itemConstructor.getItemFromMaterial("351:10");
            meta=giveItemStatus.getItemMeta();
            meta.setDisplayName(Utils.chat("&fGive Item: &aENABLED"));
        }
        else{
            giveItemStatus=RoyaleEconomy.itemConstructor.getItemFromMaterial("351:8");
            meta=giveItemStatus.getItemMeta();
            meta.setDisplayName(Utils.chat("&fGive Item: &cDISABLED"));
        }
        lore=new ArrayList<>();
        lore.add(Utils.chat("&fShould the item be given too"));
        lore.add(Utils.chat("&for the commands only?"));
        lore.add("");
        lore.add(Utils.chat("&fIf this is disabled, only commands"));
        lore.add(Utils.chat("&fwill be given."));
        meta.setLore(lore);
        giveItemStatus.setItemMeta(meta);
        inventory.setItem(23, giveItemStatus);

        inventory.setItem(40, RoyaleEconomy.staticValues.goBackShopEdit.clone());

        RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runNextTick((task) -> p.openInventory(inventory));

        Bukkit.getPluginManager().registerEvents(new ClickListener(), RoyaleEconomy.plugin);
    }

    private class ClickListener implements Listener {

        @EventHandler
        public void onClick(InventoryClickEvent e){
            if(e.getSlot()<0 || e.getCurrentItem()==null || e.getCurrentItem().getType().equals(Material.AIR))
                return;
            //if(inventory.getViewers().isEmpty()) RoyaleEconomy.plugin.getLogger().warning("A closed listener is still active.");
            if(e.getInventory().equals(inventory)){
                e.setCancelled(true);
                if(e.getSlot()==40) {
                    p.closeInventory();
                    new BlackMarketItemEditor(p, item, page);
                }
                else if(e.getSlot()==23) {
                    if (commands != null) {
                        ItemStack giveItemStatus;
                        ItemMeta meta;
                        if (e.getCurrentItem().getItemMeta().getDisplayName().contains("ENABLED")) {
                            item.setGiveItemOnBuy(false);
                            giveItemStatus = RoyaleEconomy.itemConstructor.getItemFromMaterial("351:8");
                            meta = giveItemStatus.getItemMeta();
                            meta.setDisplayName(Utils.chat("&fGive Item: &cDISABLED"));
                        } else {
                            item.setGiveItemOnBuy(true);
                            giveItemStatus = RoyaleEconomy.itemConstructor.getItemFromMaterial("351:10");
                            meta = giveItemStatus.getItemMeta();
                            meta.setDisplayName(Utils.chat("&fGive Item: &aENABLED"));
                        }
                        ArrayList<String> lore = new ArrayList<>();
                        lore.add(Utils.chat("&fShould the item be given too"));
                        lore.add(Utils.chat("&for the commands only?"));
                        lore.add("");
                        lore.add(Utils.chat("&fIf this is disabled, only commands"));
                        lore.add(Utils.chat("&fwill be given."));
                        meta.setLore(lore);
                        giveItemStatus.setItemMeta(meta);
                        inventory.setItem(23, giveItemStatus);
                    }
                }
                else if(e.getSlot()==21){
                    item.setCommands(new ArrayList<>());
                    p.closeInventory();
                    new BlackMarketItemEditor(p, item, page);
                }
                else if(e.getSlot()==11){
                    p.closeInventory();
                    PlayerMessageHandler.messageSend(p, Utils.chat("&7&m-------------------------"));
                    PlayerMessageHandler.messageSend(p, Utils.chat("&fEnter the command you want to add"));
                    PlayerMessageHandler.messageSend(p, Utils.chat("&fwithout the /"));
                    PlayerMessageHandler.messageSend(p, "");
                    PlayerMessageHandler.messageSend(p, Utils.chat("&fUse &b%player% &ffor player name."));
                    PlayerMessageHandler.messageSend(p, Utils.chat("&fUse &b%price% &ffor the buying price."));
                    new ChatListener(p, (reply) -> {
                        if(commands==null)
                            commands=new ArrayList<>();
                        commands.add(reply);
                        item.setCommands(commands);
                        new BlackMarketItemCommandsMenu(p, item, page);
                    }, false);
                }
                else if(e.getSlot()==15){
                    if(commands!=null) {
                        p.closeInventory();
                        PlayerMessageHandler.messageSend(p, Utils.chat("&7&m-------------------------"));
                        PlayerMessageHandler.messageSend(p, Utils.chat("&fEnter the number asigned to the"));
                        PlayerMessageHandler.messageSend(p, Utils.chat("&fcommand you want to remove"));
                        PlayerMessageHandler.messageSend(p, "");
                        int i = 1;
                        for (String command : commands) {
                            PlayerMessageHandler.messageSend(p, Utils.chat("&8(&b" + i + "&8) &7" + command));
                        }
                        new ChatListener(p, (reply) -> {
                            try {
                                commands.remove(Integer.valueOf(reply) - 1);
                                item.setCommands(commands);
                            }catch(Exception x){
                                PlayerMessageHandler.messageSend(p, Utils.chat("&cCouldn't find the command for that number."));
                            }
                            new BlackMarketItemCommandsMenu(p, item, page);
                        });
                    }
                }
            }
        }

        @EventHandler
        public void onClose(InventoryCloseEvent e){
            if(e.getInventory().equals(inventory)){
                inventory=null;
                HandlerList.unregisterAll(this);
                item.saveToFile();
            }
        }

    }
}
