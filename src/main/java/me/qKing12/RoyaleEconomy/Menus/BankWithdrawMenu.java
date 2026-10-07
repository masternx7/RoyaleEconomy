package me.qKing12.RoyaleEconomy.Menus;

import de.tr7zw.changeme.nbtapi.NBTItem;
import me.qKing12.RoyaleEconomy.API.Events.BankWithdrawEvent;
import me.qKing12.RoyaleEconomy.API.Events.PreBankWithdrawEvent;
import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import me.qKing12.RoyaleEconomy.utils.MoneyBag;
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

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static me.qKing12.RoyaleEconomy.RoyaleEconomy.*;



public class BankWithdrawMenu {
    private Inventory inventory;
    private Double bankBalance;
    private Boolean withdrawAsBag;
    private Double maximum_coins;

    public static ArrayList<String> withdrawAsBagPlayers = new ArrayList<>();

    public static void tryBagCreator(Double amount, Player p){
        RoyaleEconomy.dataManager.removeBankMoneyToFile(p.getUniqueId().toString(), amount);
        if(plugin.bankLogger != null)
            plugin.bankLogger.getLogger().info("[WITHDRAW BAG] " + p.getName() + " (" + p.getUniqueId() +")" + " withdrew " + amount + " coins.");
        if (amount < coinBagsAndTalismansCfg.getDouble("money-bags.minimum-amount")) {
            RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runNextTick((task) -> {
                        PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, coinBagsAndTalismansCfg.getString("money-bags.minimum-message")));
                        PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.menusCfg.getString("menus.withdraw-coins-menu.withdraw-as-bag.withdraw-as-purse")));
                    });
            RoyaleEconomy.dataManager.addMoneyToFile(p.getUniqueId().toString(), amount);
            return;
        }
        if (amount > coinBagsAndTalismansCfg.getDouble("money-bags.maximum-amount")) {
            RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runNextTick((task) -> {
                        PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, coinBagsAndTalismansCfg.getString("money-bags.maximum-message")));
                        PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.menusCfg.getString("menus.withdraw-coins-menu.withdraw-as-bag.withdraw-as-purse")));
                    });
            RoyaleEconomy.dataManager.addMoneyToFile(p.getUniqueId().toString(), amount);
            return;
        }
        p.getInventory().addItem(MoneyBag.generateMoneyBag(amount));
        RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runNextTick((task) -> PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.moneybag.output").replace("%amount%", RoyaleEconomy.messageHelper.numberFormat(amount)))));
    }

    public BankWithdrawMenu(Player p){
        RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runAsync((task) -> {
            if(!BankMenuCooldown.getInstance().tryAccess(p.getUniqueId()))
                return;

            bankBalance = RoyaleEconomy.dataManager.getBankMoneyFromFile(p.getUniqueId().toString());
            if(bankBalance==0){
                PlayerMessageHandler.messageSend(p, RoyaleEconomy.staticValues.bankEmpty);
                return;
            }
            inventory = Bukkit.createInventory(null, RoyaleEconomy.staticValues.bankWithdrawMenuSize, utilsAPI.chat(p, RoyaleEconomy.menusCfg.getString("menus.withdraw-coins-menu.title")));
            Utils.playSound(p, "menus.bank-withdaw-menu-open");
            maximum_coins = bankUpgradesCfg.getDouble("bank-upgrades." + RoyaleEconomy.dataManager.getBankUpgrade(p.getUniqueId().toString()) + ".maximum-balance");

            withdrawAsBag=withdrawAsBagPlayers.contains(p.getName());

            if(RoyaleEconomy.staticValues.backgroundGlassWithdraw!=null)
                for(int i=0; i<RoyaleEconomy.staticValues.bankWithdrawMenuSize; i++)
                    inventory.setItem(i, staticValues.backgroundGlassWithdraw);
            String balance = RoyaleEconomy.messageHelper.numberFormat(bankBalance);

            ArrayList<String> lore = new ArrayList<>();
            for(String line : RoyaleEconomy.menusCfg.getStringList("menus.withdraw-coins-menu.go-back-item.lore"))
                lore.add(utilsAPI.chat(p, line));

            for (int slot : staticValues.goBackItemSlotWithdraw)
                inventory.setItem(slot, RoyaleEconomy.itemConstructor.getItem(RoyaleEconomy.menusCfg.getString("menus.withdraw-coins-menu.go-back-item.item-id"), utilsAPI.chat(p, RoyaleEconomy.menusCfg.getString("menus.withdraw-coins-menu.go-back-item.name")), lore));

            String permission = permissionsCfg.getString("moneybag-bank-withdraw");
            if(permission.equalsIgnoreCase("none") || p.hasPermission(permission)) {
                bagSlots =RoyaleEconomy.staticValues.withdrawAsBagSlot;
                String selectType;
                if (withdrawAsBag)
                    selectType = "selected";
                else
                    selectType = "not-selected";
                ItemStack withdrawAsBag = RoyaleEconomy.staticValues.moneyBagItem.clone();
                ItemMeta meta = withdrawAsBag.getItemMeta();
                meta.setDisplayName(utilsAPI.chat(p, RoyaleEconomy.menusCfg.getString("menus.withdraw-coins-menu.withdraw-as-bag." + selectType + ".name")));
                lore = new ArrayList<>();
                for (String line : RoyaleEconomy.menusCfg.getStringList("menus.withdraw-coins-menu.withdraw-as-bag." + selectType + ".lore"))
                    lore.add(utilsAPI.chat(p, line));
                meta.setLore(lore);
                withdrawAsBag.setItemMeta(meta);
                for (int slot : RoyaleEconomy.staticValues.withdrawAsBagSlot)
                    inventory.setItem(slot, withdrawAsBag);
            }
            else
                bagSlots = Collections.emptyList();

            lore = new ArrayList<>();
            for(String line : RoyaleEconomy.menusCfg.getStringList("menus.withdraw-coins-menu.custom-amount-item.lore"))
                lore.add(utilsAPI.chat(p, line.replace("%balance%", balance).replace("%balance-limit%", RoyaleEconomy.messageHelper.numberFormat(maximum_coins))));

            for (int slot : RoyaleEconomy.staticValues.customAmountItemSlotWithdraw) {
                inventory.setItem(slot, RoyaleEconomy.itemConstructor.getItem(RoyaleEconomy.menusCfg.getString("menus.withdraw-coins-menu.custom-amount-item.item-id"), utilsAPI.chat(p, RoyaleEconomy.menusCfg.getString("menus.withdraw-coins-menu.custom-amount-item.name")), lore));
            }

            for(String key : RoyaleEconomy.menusCfg.getConfigurationSection("menus.withdraw-coins-menu.percent-items").getKeys(false)){
                lore = new ArrayList<>();
                int slot = RoyaleEconomy.menusCfg.getInt("menus.withdraw-coins-menu.percent-items."+key+".slot");
                int percent = RoyaleEconomy.menusCfg.getInt("menus.withdraw-coins-menu.percent-items."+key+".percent");

                //Double finalPercent = messageHelper.useDecimals ? bankBalance/100*percent : Math.floor(bankBalance/100*percent); exploit pt withdraw all
                double finalPercent;
                if (percent == 100)
                    finalPercent = bankBalance;
                else
                    finalPercent = messageHelper.useDecimals ? bankBalance/100*percent : Math.floor(bankBalance/100*percent);

                for(String line : RoyaleEconomy.menusCfg.getStringList("menus.withdraw-coins-menu.percent-items."+key+".lore"))
                    lore.add(utilsAPI.chat(p, line
                            .replace("%balance%", balance)
                            .replace("%balance-limit%", RoyaleEconomy.messageHelper.numberFormat(maximum_coins))
                            .replace("%to-withdraw%", RoyaleEconomy.messageHelper.numberFormat(finalPercent))
                    ));

                ItemStack percentItem = RoyaleEconomy.itemConstructor.getItem(RoyaleEconomy.menusCfg.getString("menus.withdraw-coins-menu.percent-items."+key+".item-id"), utilsAPI.chat(p, RoyaleEconomy.menusCfg.getString("menus.withdraw-coins-menu.percent-items."+key+".name")), lore);
                percentItem.setAmount(RoyaleEconomy.menusCfg.getInt("menus.withdraw-coins-menu.percent-items."+key+".display-amount"));
                NBTItem nbt = new NBTItem(percentItem);
                nbt.setDouble("RoyaleEconomy", finalPercent);
                inventory.setItem(slot, nbt.getItem());
            }

            RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runNextTick((task2) -> {
                p.openInventory(inventory);
                Bukkit.getPluginManager().registerEvents(new ClickListener(), RoyaleEconomy.plugin);
            });
        });
    }

    List<Integer> bagSlots;

    private class ClickListener implements Listener {
        private final AtomicBoolean clickCooldown=new AtomicBoolean();

        @EventHandler
        public void onClick(InventoryClickEvent e){
            if(!e.getInventory().equals(inventory))
                return;
            e.setCancelled(true);
            if(clickCooldown.get()){
                return;
            }
            if(e.getSlot()<0 || e.getCurrentItem()==null || e.getCurrentItem().getType().equals(Material.AIR)) {
                return;
            }
            //if(inventory.getViewers().isEmpty()) RoyaleEconomy.plugin.getLogger().warning("A closed listener is still active.");
            if(clickCooldown.compareAndSet(false, true)){
                if(!e.getClickedInventory().equals(e.getWhoClicked().getInventory())) {
                    Player p = (Player) e.getWhoClicked();

                    if (staticValues.goBackItemSlotWithdraw != null && staticValues.goBackItemSlotWithdraw.contains(e.getSlot())) {
                        RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runAsync((task) -> new MainBankMenu(p));
                    } else if (staticValues.customAmountItemSlotWithdraw != null && staticValues.customAmountItemSlotWithdraw.contains(e.getSlot())) {
                        if (withdrawAsBag)
                            RoyaleEconomy.plugin.selectInputMethod(p, "withdraw-coins", bankBalance, 1, null);
                        else
                            RoyaleEconomy.plugin.selectInputMethod(p, "withdraw-coins", bankBalance, 0, null);
                        clickCooldown.compareAndSet(true,false);
                    } else if (bagSlots.contains(e.getSlot())) {
                        Utils.playSound(p, "menus.withdraw-as-bag-click");
                        if (withdrawAsBag) {
                            ItemStack withdrawAsBagItem = RoyaleEconomy.staticValues.moneyBagItem.clone();
                            ItemMeta meta = withdrawAsBagItem.getItemMeta();
                            meta.setDisplayName(utilsAPI.chat(p, RoyaleEconomy.menusCfg.getString("menus.withdraw-coins-menu.withdraw-as-bag.not-selected.name")));
                            ArrayList<String> lore = new ArrayList<>();
                            for (String line : RoyaleEconomy.menusCfg.getStringList("menus.withdraw-coins-menu.withdraw-as-bag.not-selected.lore"))
                                lore.add(utilsAPI.chat(p, line));
                            meta.setLore(lore);
                            withdrawAsBagItem.setItemMeta(meta);
                            for (int slot : staticValues.withdrawAsBagSlot)
                                inventory.setItem(slot, withdrawAsBagItem);
                            withdrawAsBag = false;
                            withdrawAsBagPlayers.remove(p.getName());
                        } else {
                            ItemStack withdrawAsBagItem = RoyaleEconomy.staticValues.moneyBagItem.clone();
                            ItemMeta meta = withdrawAsBagItem.getItemMeta();
                            meta.setDisplayName(utilsAPI.chat(p, RoyaleEconomy.menusCfg.getString("menus.withdraw-coins-menu.withdraw-as-bag.selected.name")));
                            ArrayList<String> lore = new ArrayList<>();
                            for (String line : RoyaleEconomy.menusCfg.getStringList("menus.withdraw-coins-menu.withdraw-as-bag.selected.lore"))
                                lore.add(utilsAPI.chat(p, line));
                            meta.setLore(lore);
                            withdrawAsBagItem.setItemMeta(meta);

                            for (int slot : staticValues.withdrawAsBagSlot)
                                inventory.setItem(slot, withdrawAsBagItem);

                            withdrawAsBag = true;
                            withdrawAsBagPlayers.add(p.getName());
                        }
                        clickCooldown.compareAndSet(true, false);
                    } else {
                        Double amount = new NBTItem(e.getCurrentItem()).getDouble("RoyaleEconomy");
                        amount = Math.floor(amount * 100) / 100; //adaugat recent
                        if (amount != 0) {
                            PreBankWithdrawEvent event = new PreBankWithdrawEvent(p, amount, false);
                            Bukkit.getPluginManager().callEvent(event);
                            if (event.isCancelled()) {
                                clickCooldown.compareAndSet(true, false);
                                return;
                            }

                            p.closeInventory();
                            if (withdrawAsBag && p.getInventory().firstEmpty() != -1)
                                tryBagCreator(amount, p);
                            else {
                                if(bankBalance>=amount) {
                                    bankBalance-=amount;
                                    dataManager.addMoneyToFile(p.getUniqueId().toString(), amount);
                                    dataManager.removeBankMoneyToFile(p.getUniqueId().toString(), amount);
                                    if(plugin.bankLogger != null)
                                        plugin.bankLogger.getLogger().info("[WITHDRAW] " + p.getName() + " (" + p.getUniqueId() +")" + " withdrew " + amount + " coins.");
                                }
                            }
                            RoyaleEconomy.dataManager.addTransactionLog(p.getUniqueId().toString(), p.getName(), "&c-", amount);
                            Bukkit.getPluginManager().callEvent(new BankWithdrawEvent(p, amount));
                            Utils.playSound(p, "menus.bank-withdraw");
                            PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.menusCfg.getString("menus.withdraw-coins-menu.withdraw-message").replace("%amount%", RoyaleEconomy.messageHelper.numberFormat(amount))));
                            if (Math.floor(bankBalance)==0)
                                RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runLaterAsync(() -> new MainBankMenu(p), 10);
                            else {
                                RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runLaterAsync(() -> new BankWithdrawMenu(p), 10);
                            }
                        } else
                            clickCooldown.compareAndSet(true, false);
                    }
                }
                else{
                    clickCooldown.compareAndSet(true, false);
                }
            }
        }

        @EventHandler
        public void onClose(InventoryCloseEvent e){
            if(e.getInventory().equals(inventory)){
                inventory=null;
                HandlerList.unregisterAll(this);
            }
        }

    }
}
