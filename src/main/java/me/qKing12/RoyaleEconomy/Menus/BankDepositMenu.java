package me.qKing12.RoyaleEconomy.Menus;

import de.tr7zw.changeme.nbtapi.NBTItem;
import me.qKing12.RoyaleEconomy.API.Events.BankDepositEvent;
import me.qKing12.RoyaleEconomy.API.Events.PreBankDepositEvent;
import me.qKing12.RoyaleEconomy.CustomMenuItems.CustomItem;
import me.qKing12.RoyaleEconomy.DataManager.StaticValues;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static me.qKing12.RoyaleEconomy.RoyaleEconomy.*;



public class BankDepositMenu  {
    private Inventory inventory;
    private Double maximum_coins;
    private Double bankBalance;
    private Double purse;

    public BankDepositMenu(Player p){
        RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runAsync((task) -> {
            if(!BankMenuCooldown.getInstance().tryAccess(p.getUniqueId()))
                return;

            maximum_coins = bankUpgradesCfg.getDouble("bank-upgrades." + RoyaleEconomy.dataManager.getBankUpgrade(p.getUniqueId().toString()) + ".maximum-balance");
            purse = RoyaleEconomy.dataManager.getMoneyFromFile(p.getUniqueId().toString());
            if(purse<0)
                return;
            bankBalance = RoyaleEconomy.dataManager.getBankMoneyFromFile(p.getUniqueId().toString());
            if(maximum_coins.equals(bankBalance)){
                PlayerMessageHandler.messageSend(p, RoyaleEconomy.staticValues.bankFull);
                return;
            }
            if(messageHelper.useDecimals)
                bankBalance=Math.floor(bankBalance*100)/100;
            String balance = RoyaleEconomy.messageHelper.numberFormat(bankBalance);

            inventory = Bukkit.createInventory(null, RoyaleEconomy.staticValues.bankDepositMenuSize, utilsAPI.chat(p, RoyaleEconomy.menusCfg.getString("menus.deposit-coins-menu.title")));
            Utils.playSound(p, "menus.bank-deposit-menu-open");

            if (RoyaleEconomy.staticValues.backgroundGlassDeposit != null)
                for (int i = 0; i < RoyaleEconomy.staticValues.bankDepositMenuSize; i++)
                    inventory.setItem(i, staticValues.backgroundGlassDeposit);

            ArrayList<String> lore = new ArrayList<>();
            for (String line : RoyaleEconomy.menusCfg.getStringList("menus.deposit-coins-menu.go-back-item.lore"))
                lore.add(utilsAPI.chat(p, line));

            for (int slot : staticValues.goBackItemSlotDeposit)
                inventory.setItem(slot, RoyaleEconomy.itemConstructor.getItem(RoyaleEconomy.menusCfg.getString("menus.deposit-coins-menu.go-back-item.item-id"), utilsAPI.chat(p, RoyaleEconomy.menusCfg.getString("menus.deposit-coins-menu.go-back-item.name")), lore));

            lore = new ArrayList<>();
            for (String line : RoyaleEconomy.menusCfg.getStringList("menus.deposit-coins-menu.custom-amount-item.lore"))
                lore.add(utilsAPI.chat(p, line.replace("%balance%", balance).replace("%balance-limit%", RoyaleEconomy.messageHelper.numberFormat(maximum_coins))));

            for (int slot : staticValues.customAmountItemSlot)
                inventory.setItem(slot, RoyaleEconomy.itemConstructor.getItem(RoyaleEconomy.menusCfg.getString("menus.deposit-coins-menu.custom-amount-item.item-id"), utilsAPI.chat(p, RoyaleEconomy.menusCfg.getString("menus.deposit-coins-menu.custom-amount-item.name")), lore));

            for (String key : RoyaleEconomy.menusCfg.getConfigurationSection("menus.deposit-coins-menu.percent-items").getKeys(false)) {
                lore = new ArrayList<>();
                int slot = RoyaleEconomy.menusCfg.getInt("menus.deposit-coins-menu.percent-items." + key + ".slot");
                int percent = RoyaleEconomy.menusCfg.getInt("menus.deposit-coins-menu.percent-items." + key + ".percent");

                Double finalPercent;
                if (percent == 100)
                    finalPercent = purse;
                else
                    finalPercent = messageHelper.useDecimals ? purse / 100 * percent : Math.floor(purse / 100 * percent);

                if (bankBalance + finalPercent > maximum_coins)
                    finalPercent = maximum_coins - bankBalance;

                for (String line : RoyaleEconomy.menusCfg.getStringList("menus.deposit-coins-menu.percent-items." + key + ".lore"))
                    lore.add(utilsAPI.chat(p, line
                            .replace("%balance%", balance)
                            .replace("%balance-limit%", RoyaleEconomy.messageHelper.numberFormat(maximum_coins))
                            .replace("%to-deposit%", RoyaleEconomy.messageHelper.numberFormat(finalPercent))
                    ));

                ItemStack percentItem = RoyaleEconomy.itemConstructor.getItem(RoyaleEconomy.menusCfg.getString("menus.deposit-coins-menu.percent-items." + key + ".item-id"), utilsAPI.chat(p, RoyaleEconomy.menusCfg.getString("menus.deposit-coins-menu.percent-items." + key + ".name")), lore);
                percentItem.setAmount(RoyaleEconomy.menusCfg.getInt("menus.deposit-coins-menu.percent-items." + key + ".display-amount"));
                NBTItem nbt = new NBTItem(percentItem);
                nbt.setDouble("RoyaleEconomy", Math.floor(finalPercent*100)/100);
                inventory.setItem(slot, nbt.getItem());
            }

            HashMap<Integer, CustomItem> customItems = customItemsHandler.getItems("personal-bank-deposit-menu");
            if(customItems!=null) {
                for (Map.Entry<Integer, CustomItem> item : customItems.entrySet())
                    inventory.setItem(item.getKey(), item.getValue().getItem(p));
            }

            RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runNextTick((task2) -> {
                p.openInventory(inventory);
                Bukkit.getPluginManager().registerEvents(new ClickListener(), RoyaleEconomy.plugin);
            });
        });
    }

    private final AtomicBoolean clickCooldown=new AtomicBoolean();

    private class ClickListener implements Listener {

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
            if(clickCooldown.compareAndSet(false, true)) {
                if (!e.getClickedInventory().equals(e.getWhoClicked().getInventory())) {
                    Player p = (Player) e.getWhoClicked();

                    if(customItemsHandler.tryClick("personal-bank-deposit-menu", e.getSlot(), p)){
                        clickCooldown.compareAndSet(true,false);
                        return;
                    }

                    if (staticValues.goBackItemSlotDeposit != null && staticValues.goBackItemSlotDeposit.contains(e.getSlot())) {
                        RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runAsync((task) -> new MainBankMenu(p));
                    } else if (staticValues.customAmountItemSlot != null && staticValues.customAmountItemSlot.contains(e.getSlot())) {
                        RoyaleEconomy.plugin.selectInputMethod(p, "deposit-coins", purse, maximum_coins, null);
                        clickCooldown.compareAndSet(true,false);
                    } else {
                        Double amount = new NBTItem(e.getCurrentItem()).getDouble("RoyaleEconomy");
                        amount = Math.floor(amount * 100) / 100; //adaugat recent
                        if (amount != 0) {
                            PreBankDepositEvent event = new PreBankDepositEvent(p, amount, false);
                            Bukkit.getPluginManager().callEvent(event);
                            if (event.isCancelled()) {
                                clickCooldown.compareAndSet(true, false);
                                return;
                            }

                            p.closeInventory();
                            //RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runAsync((task) -> {
                            String uuid = p.getUniqueId().toString();
                            if (RoyaleEconomy.dataManager.removeMoneyFromFile(uuid, amount)) {
                                RoyaleEconomy.dataManager.addBankMoneyToFile(uuid, amount);
                                if(plugin.bankLogger != null)
                                    plugin.bankLogger.getLogger().info("[DEPOSIT] " + p.getName() + " (" + uuid +")" + " deposited " + amount + " coins.");
                                RoyaleEconomy.dataManager.addTransactionLog(p.getUniqueId().toString(), p.getName(), "&a+", amount);
                                Utils.playSound(p, "menus.bank-deposit");
                                Double amountCopy = amount;
                                RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runAsync((task) -> {
                                    Bukkit.getPluginManager().callEvent(new BankDepositEvent(p, amountCopy));
                                });

                                //RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runNextTick((task) ->
                                                PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.menusCfg.getString("menus.deposit-coins-menu.deposit-message").replace("%amount%", RoyaleEconomy.messageHelper.numberFormat(amount))));
                                //);
                            } else {
                                plugin.getLogger().info("Deposit attempted without the needed money for " + p.getName());
                                plugin.getLogger().info(uuid + " " + amount);
                                plugin.getLogger().info(dataManager.getMoneyFromFile(uuid) + "");
                            }
                            //});
                            if (bankBalance + amount == maximum_coins)
                                RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runLaterAsync(() -> new MainBankMenu(p), 10);
                            else
                                RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runLaterAsync(() -> new BankDepositMenu(p), 10);
                        } else {
                            clickCooldown.compareAndSet(true,false);
                        }
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
