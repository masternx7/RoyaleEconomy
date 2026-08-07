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
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import static me.qKing12.RoyaleEconomy.RoyaleEconomy.*;
import static me.qKing12.RoyaleEconomy.RoyaleEconomy.plugin;

public class CurrencyExchangeMenu {
    private Inventory inventory;
    private static String name;
    private static int slots;
    private static ItemStack backgroundItem;
    private static ItemStack animationDefaultItem;
    private static ItemStack animationProcessingItem;
    private static ArrayList<ArrayList<Integer>> animationSlots;

    private static final NumberFormat numberFormat = NumberFormat.getInstance();
    static {
        numberFormat.setGroupingUsed(true);
        numberFormat.setMaximumFractionDigits(2);
    }


    private static int fromCurrencySlot;
    private static String fromCurrencyName;
    private static ArrayList<String> fromCurrencyLore;

    private static int toCurrencySlot;
    private static String toCurrencyName;
    private static ArrayList<String> toCurrencyLore;

    private static ItemStack selectAmountItem;
    private static int selectAmountSlot;
    private static String selectAmountName;
    private static ArrayList<String> selectAmountLore;

    private static ItemStack confirmItem;
    private static int confirmSlot;
    private static String confirmName;
    private static ArrayList<String> confirmLore;

    private static ItemStack cancelItem;
    private static ItemStack closeItem;
    private static int closeSlot;

    public static void loadConfigData(){
        name = Utils.chat(multiCurrencyCfg.getString("currency-exchange-menu.convert-submenu.name"));
        slots= multiCurrencyCfg.getInt("currency-exchange-menu.convert-submenu.slots");
        backgroundItem=itemConstructor.getItem(multiCurrencyCfg.getString("currency-exchange-menu.convert-submenu.background-item"), " ", new ArrayList<>(Collections.singletonList(" ")));
        animationDefaultItem=itemConstructor.getItem(multiCurrencyCfg.getString("currency-exchange-menu.convert-submenu.transfer-animation.default-item"), " ", new ArrayList<String>(Collections.singletonList(" ")));
        animationProcessingItem=itemConstructor.getItem(multiCurrencyCfg.getString("currency-exchange-menu.convert-submenu.transfer-animation.processing"), " ", new ArrayList<String>(Collections.singletonList(" ")));
        animationSlots=new ArrayList<>();
        for(String slot : multiCurrencyCfg.getStringList("currency-exchange-menu.convert-submenu.transfer-animation.slots")){
            ArrayList<Integer> sloturi=new ArrayList<>();
            for(String s : slot.split(",")) {
                sloturi.add(Integer.parseInt(s));
            }
            animationSlots.add(sloturi);
        }

        fromCurrencySlot=multiCurrencyCfg.getInt("currency-exchange-menu.convert-submenu.from-currency.slot");
        fromCurrencyName=Utils.chat(multiCurrencyCfg.getString("currency-exchange-menu.convert-submenu.from-currency.name"));
        fromCurrencyLore=new ArrayList<>();
        for(String line : multiCurrencyCfg.getStringList("currency-exchange-menu.convert-submenu.from-currency.lore"))
            fromCurrencyLore.add(Utils.chat(line));

        fromCurrencySlot=multiCurrencyCfg.getInt("currency-exchange-menu.convert-submenu.from-currency.slot");
        fromCurrencyName=Utils.chat(multiCurrencyCfg.getString("currency-exchange-menu.convert-submenu.from-currency.name"));
        fromCurrencyLore=new ArrayList<>();
        for(String line : multiCurrencyCfg.getStringList("currency-exchange-menu.convert-submenu.from-currency.lore"))
            fromCurrencyLore.add(Utils.chat(line));

        toCurrencySlot=multiCurrencyCfg.getInt("currency-exchange-menu.convert-submenu.to-currency.slot");
        toCurrencyName=Utils.chat(multiCurrencyCfg.getString("currency-exchange-menu.convert-submenu.to-currency.name"));
        toCurrencyLore=new ArrayList<>();
        for(String line : multiCurrencyCfg.getStringList("currency-exchange-menu.convert-submenu.to-currency.lore"))
            toCurrencyLore.add(Utils.chat(line));

        selectAmountSlot=multiCurrencyCfg.getInt("currency-exchange-menu.convert-submenu.select-amount.slot");
        selectAmountName=Utils.chat(multiCurrencyCfg.getString("currency-exchange-menu.convert-submenu.select-amount.name"));
        selectAmountLore=new ArrayList<>();
        for(String line : multiCurrencyCfg.getStringList("currency-exchange-menu.convert-submenu.select-amount.lore"))
            selectAmountLore.add(Utils.chat(line));

        selectAmountItem=itemConstructor.getItemFromMaterial(multiCurrencyCfg.getString("currency-exchange-menu.convert-submenu.select-amount.item"));

        confirmSlot=multiCurrencyCfg.getInt("currency-exchange-menu.convert-submenu.confirm.slot");
        confirmName=Utils.chat(multiCurrencyCfg.getString("currency-exchange-menu.convert-submenu.confirm.name"));
        confirmLore=new ArrayList<>();
        for(String line : multiCurrencyCfg.getStringList("currency-exchange-menu.convert-submenu.confirm.lore"))
            confirmLore.add(Utils.chat(line));

        confirmItem=itemConstructor.getItemFromMaterial(multiCurrencyCfg.getString("currency-exchange-menu.convert-submenu.confirm.item"));

        String cancelName=Utils.chat(multiCurrencyCfg.getString("currency-exchange-menu.convert-submenu.cancel.name"));
        ArrayList<String> cancelLore=new ArrayList<>();
        for(String line : multiCurrencyCfg.getStringList("currency-exchange-menu.convert-submenu.cancel.lore"))
            cancelLore.add(Utils.chat(line));

        cancelItem=itemConstructor.getItem(multiCurrencyCfg.getString("currency-exchange-menu.convert-submenu.cancel.item"), cancelName, cancelLore);

        closeSlot=multiCurrencyCfg.getInt("currency-exchange-menu.convert-submenu.close.slot");
        String closeName=Utils.chat(multiCurrencyCfg.getString("currency-exchange-menu.convert-submenu.close.name"));
        ArrayList<String> closeLore=new ArrayList<>();
        for(String line : multiCurrencyCfg.getStringList("currency-exchange-menu.convert-submenu.close.lore"))
            closeLore.add(Utils.chat(line));

        closeItem=itemConstructor.getItem(multiCurrencyCfg.getString("currency-exchange-menu.convert-submenu.close.item"), closeName, closeLore);

    }

    private Currency currency;
    private boolean selling;
    private double amountToConvert=100;
    private double coinsYouHave;
    private double currencyYouHave;
    private double amountConverted;

    private BukkitTask animationTask;
    private Player player;

    private void startAnimation(){
        inventory.setItem(confirmSlot, cancelItem);
        animationTask=new BukkitRunnable(){
            Iterator<ArrayList<Integer>> iterator=animationSlots.iterator();

            @Override
            public void run() {
                if(animationTask==null)
                    cancel();
                if(!iterator.hasNext()){
                    //chestii de facut conversia
                    cancel();
                    if(selling){
                        if(currency.removeAmount(player.getUniqueId().toString(), amountToConvert))
                            dataManager.addMoneyToFile(player.getUniqueId().toString(), amountConverted);
                    }
                    else{
                        double fee;
                        if(player.hasPermission(currency.getExchangePermission()))
                            fee = 0;
                        else
                            fee = currency.calculateBuyFee(amountToConvert);
                        if(dataManager.removeMoneyFromFile(player.getUniqueId().toString(), amountToConvert+fee)){
                            currency.addAmount(player.getUniqueId().toString(), amountConverted);
                        }
                    }

                    player.sendMessage(MultiCurrencyHandler.convertSuccess);

                    loadItems();
                    animationTask=null;
                    return;
                }

                ArrayList<Integer> slots = iterator.next();
                for(int slot : slots)
                    inventory.setItem(slot, animationProcessingItem);

            }
        }.runTaskTimerAsynchronously(plugin, 7, 7);
    }

    private void loadItems(){
        coinsYouHave= dataManager.getMoneyFromFile(player.getUniqueId().toString());
        currencyYouHave= currency.getAmount(player.getUniqueId().toString());
        for(ArrayList<Integer> slots : animationSlots)
            for(int slot : slots)
                inventory.setItem(slot, animationDefaultItem);

        ItemStack fromCurrency;
        ItemStack toCurrency;
        ItemStack confirm;
        ItemStack selectAmount;

        if(!selling){
            amountConverted=amountToConvert/currency.getCoinsReference();
            if(!currency.hasDecimals()) {
                amountConverted=Math.floor(amountConverted);
                amountToConvert = amountConverted * currency.getCoinsReference();
            }
            String amountS= currency.formatMoney(amountToConvert);
            fromCurrency=MultiCurrencyHandler.defaultCoinsIcon.clone();
            ItemMeta meta = fromCurrency.getItemMeta();
            meta.setDisplayName(fromCurrencyName
                    .replace("%currency-color%", MultiCurrencyHandler.defaultCoinsColor)
                    .replace("%currency-balance%", messageHelper.numberFormat(coinsYouHave))
                    .replace("%amount%", amountS)
                    .replace("%currency-name%", MultiCurrencyHandler.defaultCoinsName));
            ArrayList<String> lore = new ArrayList<>();
            for(String line : fromCurrencyLore)
                lore.add(line
                        .replace("%currency-color%", MultiCurrencyHandler.defaultCoinsColor)
                        .replace("%currency-name%", MultiCurrencyHandler.defaultCoinsName)
                        .replace("%currency-balance%", messageHelper.numberFormat(coinsYouHave))
                        .replace("%amount%", amountS));
            meta.setLore(lore);
            fromCurrency.setItemMeta(meta);

            toCurrency=currency.getIcon().clone();
            meta=toCurrency.getItemMeta();
            meta.setDisplayName(toCurrencyName
                    .replace("%currency-color%", currency.getColor())
                    .replace("%currency-name%", currency.getCurrencyName())
                    .replace("%from-currency%", MultiCurrencyHandler.defaultCoinsName)
                    .replace("%currency-balance%", currency.formatMoney(currencyYouHave))
                    .replace("%from-currency-color%", MultiCurrencyHandler.defaultCoinsColor)
            );

            String amountS2 = messageHelper.numberFormat(amountConverted);
            String amountFee = messageHelper.numberFormat(amountToConvert*currency.getExchangePercent()/100);
            lore=new ArrayList<>();
            for(String line : toCurrencyLore){
                lore.add(line.replace("%currency-color%", currency.getColor())
                        .replace("%currency-name%", currency.getCurrencyName())
                        .replace("%from-currency%", MultiCurrencyHandler.defaultCoinsName)
                        .replace("%from-currency-color%", MultiCurrencyHandler.defaultCoinsColor)
                        .replace("%currency-balance%", currency.formatMoney(currencyYouHave))
                        .replace("%amount%", amountS2)
                        .replace("%fee-amount%", amountFee)
                );
            }
            meta.setLore(lore);
            toCurrency.setItemMeta(meta);

            confirm=confirmItem.clone();
            meta=confirm.getItemMeta();
            meta.setDisplayName(confirmName
                    .replace("%from-currency-color%", MultiCurrencyHandler.defaultCoinsColor)
                    .replace("%from-currency-amount%", amountS)
                    .replace("%from-currency-name%", MultiCurrencyHandler.defaultCoinsName)
                    .replace("%to-currency-color%", currency.getColor())
                    .replace("%to-currency-amount%", amountS2)
                    .replace("%to-currency-name%", currency.getCurrencyName())
                    .replace("%exchange-fee%", amountFee)
            );
            lore=new ArrayList<>();
            for(String line : confirmLore)
                lore.add(line
                        .replace("%from-currency-color%", MultiCurrencyHandler.defaultCoinsColor)
                        .replace("%from-currency-amount%", amountS)
                        .replace("%from-currency-name%", MultiCurrencyHandler.defaultCoinsName)
                        .replace("%to-currency-color%", currency.getColor())
                        .replace("%to-currency-amount%", amountS2)
                        .replace("%to-currency-name%", currency.getCurrencyName())
                        .replace("%exchange-fee%", amountFee)
                );
            meta.setLore(lore);
            confirm.setItemMeta(meta);

            selectAmount=selectAmountItem.clone();
            meta=selectAmount.getItemMeta();
            meta.setDisplayName(selectAmountName
                    .replace("%currency-color%", MultiCurrencyHandler.defaultCoinsColor)
                    .replace("%currency-name%", MultiCurrencyHandler.defaultCoinsName)
                    .replace("%currency-balance%", messageHelper.numberFormat(coinsYouHave))
                    .replace("%amount%", amountS)
                    .replace("%fee-percent%", numberFormat.format(currency.getExchangePercent()))
            );

            lore=new ArrayList<>();
            for(String line : selectAmountLore)
                lore.add(line
                        .replace("%currency-color%", MultiCurrencyHandler.defaultCoinsColor)
                        .replace("%currency-name%", MultiCurrencyHandler.defaultCoinsName)
                        .replace("%currency-balance%", messageHelper.numberFormat(coinsYouHave))
                        .replace("%amount%", amountS)
                        .replace("%fee-percent%", numberFormat.format(currency.getExchangePercent()))
                );
            meta.setLore(lore);
            selectAmount.setItemMeta(meta);
        }
        else{
            amountConverted=amountToConvert*currency.getCoinsReference();
            String amountS= currency.formatMoney(amountToConvert);
            fromCurrency=currency.getIcon().clone();
            ItemMeta meta = fromCurrency.getItemMeta();
            meta.setDisplayName(fromCurrencyName
                    .replace("%currency-color%", currency.getColor())
                    .replace("%amount%", amountS)
                    .replace("%currency-balance%", currency.formatMoney(currencyYouHave))
                    .replace("%currency-name%", currency.getCurrencyName()));
            ArrayList<String> lore = new ArrayList<>();
            for(String line : fromCurrencyLore)
                lore.add(line
                        .replace("%currency-color%", currency.getColor())
                        .replace("%currency-name%", currency.getCurrencyName())
                        .replace("%currency-balance%", currency.formatMoney(currencyYouHave))
                        .replace("%amount%", amountS));
            meta.setLore(lore);
            fromCurrency.setItemMeta(meta);



            toCurrency=MultiCurrencyHandler.defaultCoinsIcon.clone();
            meta=toCurrency.getItemMeta();
            meta.setDisplayName(toCurrencyName
                    .replace("%currency-color%", MultiCurrencyHandler.defaultCoinsColor)
                    .replace("%currency-name%", MultiCurrencyHandler.defaultCoinsName)
                    .replace("%from-currency%", currency.getCurrencyName())
                    .replace("%currency-balance%", messageHelper.numberFormat(coinsYouHave))
                    .replace("%from-currency-color%", currency.getColor())
            );

            String amountS2 = messageHelper.numberFormat(amountConverted);
            lore=new ArrayList<>();
            for(String line : toCurrencyLore){
                lore.add(line.replace("%currency-color%", MultiCurrencyHandler.defaultCoinsColor)
                        .replace("%currency-name%", MultiCurrencyHandler.defaultCoinsName)
                        .replace("%currency-balance%", messageHelper.numberFormat(coinsYouHave))
                        .replace("%from-currency%", currency.getCurrencyName())
                        .replace("%from-currency-color%", currency.getColor())
                        .replace("%amount%", amountS2)
                        .replace("%fee-amount%", "0")
                );
            }
            meta.setLore(lore);
            toCurrency.setItemMeta(meta);

            confirm=confirmItem.clone();
            meta=confirm.getItemMeta();
            meta.setDisplayName(confirmName
                    .replace("%from-currency-color%", currency.getColor())
                    .replace("%from-currency-amount%", amountS2)
                    .replace("%from-currency-name%", currency.getCurrencyName())
                    .replace("%to-currency-color%", MultiCurrencyHandler.defaultCoinsColor)
                    .replace("%to-currency-amount%", amountS)
                    .replace("%to-currency-name%", MultiCurrencyHandler.defaultCoinsName)
                    .replace("%exchange-fee%", "0")
            );
            lore=new ArrayList<>();
            for(String line : confirmLore)
                lore.add(line
                        .replace("%from-currency-color%", currency.getColor())
                        .replace("%from-currency-amount%", amountS)
                        .replace("%from-currency-name%", currency.getCurrencyName())
                        .replace("%to-currency-color%", MultiCurrencyHandler.defaultCoinsColor)
                        .replace("%to-currency-amount%", amountS2)
                        .replace("%to-currency-name%", MultiCurrencyHandler.defaultCoinsName)
                        .replace("%exchange-fee%", "0")
                );
            meta.setLore(lore);
            confirm.setItemMeta(meta);


            selectAmount=selectAmountItem.clone();
            meta=selectAmount.getItemMeta();
            meta.setDisplayName(selectAmountName
                    .replace("%currency-color%", currency.getColor())
                    .replace("%currency-name%", currency.getCurrencyName())
                    .replace("%currency-balance%", currency.formatMoney(currencyYouHave))
                    .replace("%amount%", amountS)
                    .replace("%fee-percent%", "0")
            );

            lore=new ArrayList<>();
            for(String line : selectAmountLore)
                lore.add(line
                        .replace("%currency-color%", currency.getColor())
                        .replace("%currency-name%", currency.getCurrencyName())
                        .replace("%currency-balance%", currency.formatMoney(currencyYouHave))
                        .replace("%amount%", amountS)
                        .replace("%fee-percent%", "0")
                );
            meta.setLore(lore);
            selectAmount.setItemMeta(meta);
        }

        inventory.setItem(fromCurrencySlot, fromCurrency);
        inventory.setItem(toCurrencySlot, toCurrency);
        inventory.setItem(selectAmountSlot, selectAmount);
        inventory.setItem(confirmSlot, confirm);
    }

    public CurrencyExchangeMenu(Player p, Currency currency, boolean selling){
        this.player=p;
        this.currency=currency;
        this.selling=selling;
        RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runAsync((task) -> {
            inventory = Bukkit.createInventory(null, slots, utilsAPI.chat(p, name));

            for(int i=0;i<slots;i++)
                inventory.setItem(i, backgroundItem);

            loadItems();
            inventory.setItem(closeSlot, closeItem);

            RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runNextTick((task2) -> {
                p.openInventory(inventory);
                Bukkit.getPluginManager().registerEvents(new ClickListener(), RoyaleEconomy.plugin);
            });

        });
    }

    public CurrencyExchangeMenu(Player p, Currency currency, boolean selling, double amount){
        this.player=p;
        this.amountToConvert=Math.abs(amount);
        if(selling && !currency.hasDecimals())
            amountToConvert=Math.floor(amountToConvert);

        this.currency=currency;
        this.selling=selling;
        RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runAsync((task) -> {
            inventory = Bukkit.createInventory(null, slots, utilsAPI.chat(p, name));

            for(int i=0;i<slots;i++)
                inventory.setItem(i, backgroundItem);

            loadItems();
            inventory.setItem(closeSlot, closeItem);

            RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runNextTick((task2) -> {
                p.openInventory(inventory);
                Bukkit.getPluginManager().registerEvents(new ClickListener(), RoyaleEconomy.plugin);
            });

            //de adaugat click eventuri
            //de adaugat animatia in sine

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
                    if(e.getSlot()==selectAmountSlot){
                        e.getWhoClicked().closeInventory();
                        new CurrencyInputMenu((Player)e.getWhoClicked(), currency, selling);
                    }
                    else if(e.getSlot()==closeSlot){
                        e.getWhoClicked().closeInventory();
                    }
                    else if(e.getSlot()==confirmSlot){
                        //prerequirments
                        if(amountToConvert==0 || amountConverted==0)
                            return;

                        double fee;
                        if(player.hasPermission(currency.getExchangePermission()))
                            fee = 0;
                        else
                            fee = currency.calculateBuyFee(amountToConvert);
                        if(amountToConvert+(selling?0:fee)>(selling?currencyYouHave:coinsYouHave)){
                            player.sendMessage(MultiCurrencyHandler.notEnoughMessage);
                            return;
                        }

                        if(animationTask!=null){
                            animationTask.cancel();
                            loadItems();
                            animationTask=null;
                        }
                        else{
                            startAnimation();
                        }
                    }
                }
            }

        }

        @EventHandler
        public void onClose(InventoryCloseEvent e) {
            if (e.getInventory().equals(inventory)) {
                if(animationTask!=null)
                    animationTask.cancel();
                inventory = null;
                HandlerList.unregisterAll(this);
            }
        }

    }

}
