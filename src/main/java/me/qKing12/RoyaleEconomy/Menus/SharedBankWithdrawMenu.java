package me.qKing12.RoyaleEconomy.Menus;

import de.tr7zw.changeme.nbtapi.NBTItem;
import me.qKing12.RoyaleEconomy.API.Events.PreSharedBankWithdrawEvent;
import me.qKing12.RoyaleEconomy.API.Events.SharedBankWithdrawEvent;
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


public class SharedBankWithdrawMenu {
    private Inventory inventory;
    private Double bankBalance;
    private Boolean withdrawAsBag = false;
    private String bankID;
    private Double maximum_coins;

    public static void tryBagCreator(Double amount, Player p, String bankID) {
        if (RoyaleEconomy.dataManager.getSharedBankManager().removeSharedBankMoneyToFile(bankID, amount)) {
            if (plugin.bankLogger != null)
                plugin.bankLogger.getLogger().info("[WITHDRAW SHARED BAG] " + p.getName() + " (" + p.getUniqueId() + ")" + " withdrew " + amount + " coins.");
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
        } else
            PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.plugin.getConfig().getString("input-guis.withdraw-coins.fail-message").replace("%amount%", messageHelper.numberFormat(amount))));
    }

    public SharedBankWithdrawMenu(Player p) {
        RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runAsync((task) -> {
            if (!BankMenuCooldown.getInstance().tryAccess(p.getUniqueId()))
                return;

            bankID = dataManager.getSharedBankManager().getSharedBankId(p.getUniqueId().toString());

            if (bankID.equals("")) {
                RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runNextTick(task2 -> p.closeInventory());
                return;
            }

            bankBalance = RoyaleEconomy.dataManager.getSharedBankManager().getSharedBankMoneyFromFile(bankID);
            if (bankBalance == 0) {
                PlayerMessageHandler.messageSend(p, RoyaleEconomy.staticValues.bankEmpty);
                return;
            }

            maximum_coins = bankUpgradesCfg.getDouble("shared-bank-upgrades." + RoyaleEconomy.dataManager.getSharedBankManager().getSharedBankUpgrade(bankID) + ".maximum-balance");

            withdrawAsBag = BankWithdrawMenu.withdrawAsBagPlayers.contains(p.getName());

            inventory = Bukkit.createInventory(null, RoyaleEconomy.staticValues.sharedbankWithdrawMenuSize, utilsAPI.chat(p, RoyaleEconomy.menusCfg.getString("shared-menus.withdraw-coins-menu.title")));
            Utils.playSound(p, "menus.bank-withdaw-menu-open");
            if (RoyaleEconomy.staticValues.sharedbackgroundGlassWithdraw != null)
                for (int i = 0; i < RoyaleEconomy.staticValues.sharedbankWithdrawMenuSize; i++)
                    inventory.setItem(i, staticValues.sharedbackgroundGlassWithdraw);
            String balance = RoyaleEconomy.messageHelper.numberFormat(bankBalance);

            ArrayList<String> lore = new ArrayList<>();
            for (String line : RoyaleEconomy.menusCfg.getStringList("shared-menus.withdraw-coins-menu.go-back-item.lore"))
                lore.add(utilsAPI.chat(p, line));

            for (int slot : staticValues.sharedgoBackItemSlotWithdraw)
                inventory.setItem(slot, RoyaleEconomy.itemConstructor.getItem(RoyaleEconomy.menusCfg.getString("shared-menus.withdraw-coins-menu.go-back-item.item-id"), utilsAPI.chat(p, RoyaleEconomy.menusCfg.getString("shared-menus.withdraw-coins-menu.go-back-item.name")), lore));


            String permission = permissionsCfg.getString("moneybag-bank-withdraw");
            if (permission.equalsIgnoreCase("none") || p.hasPermission(permission)) {
                bagSlots = RoyaleEconomy.staticValues.sharedwithdrawAsBagSlot;
                String selectType;
                if (withdrawAsBag)
                    selectType = "selected";
                else
                    selectType = "not-selected";
                ItemStack withdrawAsBag = RoyaleEconomy.staticValues.moneyBagItem.clone();
                ItemMeta meta = withdrawAsBag.getItemMeta();
                meta.setDisplayName(utilsAPI.chat(p, RoyaleEconomy.menusCfg.getString("shared-menus.withdraw-coins-menu.withdraw-as-bag." + selectType + ".name")));
                lore = new ArrayList<>();
                for (String line : RoyaleEconomy.menusCfg.getStringList("shared-menus.withdraw-coins-menu.withdraw-as-bag." + selectType + ".lore"))
                    lore.add(utilsAPI.chat(p, line));
                meta.setLore(lore);
                withdrawAsBag.setItemMeta(meta);
                for (int slot : staticValues.sharedwithdrawAsBagSlot)
                    inventory.setItem(slot, withdrawAsBag);
            } else
                bagSlots = Collections.emptyList();

            lore = new ArrayList<>();
            for (String line : RoyaleEconomy.menusCfg.getStringList("shared-menus.withdraw-coins-menu.custom-amount-item.lore"))
                lore.add(utilsAPI.chat(p, line.replace("%balance%", balance).replace("%balance-limit%", RoyaleEconomy.messageHelper.numberFormat(maximum_coins))));

            for (int slot : staticValues.sharedcustomAmountItemSlotWithdraw)
                inventory.setItem(slot, RoyaleEconomy.itemConstructor.getItem(RoyaleEconomy.menusCfg.getString("shared-menus.withdraw-coins-menu.custom-amount-item.item-id"), utilsAPI.chat(p, RoyaleEconomy.menusCfg.getString("shared-menus.withdraw-coins-menu.custom-amount-item.name")), lore));

            for (String key : RoyaleEconomy.menusCfg.getConfigurationSection("shared-menus.withdraw-coins-menu.percent-items").getKeys(false)) {
                lore = new ArrayList<>();
                int slot = RoyaleEconomy.menusCfg.getInt("shared-menus.withdraw-coins-menu.percent-items." + key + ".slot");
                int percent = RoyaleEconomy.menusCfg.getInt("shared-menus.withdraw-coins-menu.percent-items." + key + ".percent");

                //Double finalPercent = messageHelper.useDecimals ? bankBalance / 100 * percent : Math.floor(bankBalance / 100 * percent);
                double finalPercent;
                if (percent == 100)
                    finalPercent = bankBalance;
                else
                    finalPercent = messageHelper.useDecimals ? bankBalance / 100 * percent : Math.floor(bankBalance / 100 * percent);

                for (String line : RoyaleEconomy.menusCfg.getStringList("shared-menus.withdraw-coins-menu.percent-items." + key + ".lore"))
                    lore.add(utilsAPI.chat(p, line
                            .replace("%balance%", balance)
                            .replace("%balance-limit%", RoyaleEconomy.messageHelper.numberFormat(maximum_coins))
                            .replace("%to-withdraw%", RoyaleEconomy.messageHelper.numberFormat(finalPercent))
                    ));

                ItemStack percentItem = RoyaleEconomy.itemConstructor.getItem(RoyaleEconomy.menusCfg.getString("shared-menus.withdraw-coins-menu.percent-items." + key + ".item-id"), utilsAPI.chat(p, RoyaleEconomy.menusCfg.getString("shared-menus.withdraw-coins-menu.percent-items." + key + ".name")), lore);
                percentItem.setAmount(RoyaleEconomy.menusCfg.getInt("shared-menus.withdraw-coins-menu.percent-items." + key + ".display-amount"));
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
        private final AtomicBoolean clickCooldown = new AtomicBoolean();

        @EventHandler
        public void onClick(InventoryClickEvent e) {
            if (!e.getInventory().equals(inventory))
                return;
            e.setCancelled(true);

            if (clickCooldown.get()) {
                return;
            }
            if (e.getSlot() < 0 || e.getCurrentItem() == null || e.getCurrentItem().getType().equals(Material.AIR)) {
                return;
            }
            if (clickCooldown.compareAndSet(false, true)) {
                //if(inventory.getViewers().isEmpty()) RoyaleEconomy.plugin.getLogger().warning("A closed listener is still active.");
                if (!e.getClickedInventory().equals(e.getWhoClicked().getInventory())) {
                    Player p = (Player) e.getWhoClicked();

                    if (staticValues.sharedgoBackItemSlotWithdraw != null && staticValues.sharedgoBackItemSlotWithdraw.contains(e.getSlot())) {
                        RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runAsync((task) -> new SharedMainBankMenu(p));
                    } else if (staticValues.sharedgoBackItemSlotWithdraw != null && staticValues.sharedcustomAmountItemSlotWithdraw.contains(e.getSlot())) {
                        if (withdrawAsBag)
                            RoyaleEconomy.plugin.selectInputMethod(p, "withdraw-coins", bankBalance, 1, bankID);
                        else
                            RoyaleEconomy.plugin.selectInputMethod(p, "withdraw-coins", bankBalance, 0, bankID);
                        clickCooldown.compareAndSet(true, false);
                    } else if (bagSlots != null && bagSlots.contains(e.getSlot())) {
                        Utils.playSound(p, "menus.withdraw-as-bag-click");
                        if (withdrawAsBag) {
                            ItemStack withdrawAsBagItem = RoyaleEconomy.staticValues.moneyBagItem.clone();
                            ItemMeta meta = withdrawAsBagItem.getItemMeta();
                            meta.setDisplayName(utilsAPI.chat(p, RoyaleEconomy.menusCfg.getString("shared-menus.withdraw-coins-menu.withdraw-as-bag.not-selected.name")));
                            ArrayList<String> lore = new ArrayList<>();
                            for (String line : RoyaleEconomy.menusCfg.getStringList("shared-menus.withdraw-coins-menu.withdraw-as-bag.not-selected.lore"))
                                lore.add(utilsAPI.chat(p, line));
                            meta.setLore(lore);
                            withdrawAsBagItem.setItemMeta(meta);
                            for (int slot : staticValues.sharedwithdrawAsBagSlot)
                                inventory.setItem(slot, withdrawAsBagItem);
                            withdrawAsBag = false;
                            BankWithdrawMenu.withdrawAsBagPlayers.remove(p.getName());
                        } else {
                            ItemStack withdrawAsBagItem = RoyaleEconomy.staticValues.moneyBagItem.clone();
                            ItemMeta meta = withdrawAsBagItem.getItemMeta();
                            meta.setDisplayName(utilsAPI.chat(p, RoyaleEconomy.menusCfg.getString("shared-menus.withdraw-coins-menu.withdraw-as-bag.selected.name")));
                            ArrayList<String> lore = new ArrayList<>();
                            for (String line : RoyaleEconomy.menusCfg.getStringList("shared-menus.withdraw-coins-menu.withdraw-as-bag.selected.lore"))
                                lore.add(utilsAPI.chat(p, line));
                            meta.setLore(lore);
                            withdrawAsBagItem.setItemMeta(meta);
                            for (int slot : staticValues.sharedwithdrawAsBagSlot)
                                inventory.setItem(slot, withdrawAsBagItem);
                            withdrawAsBag = true;
                            BankWithdrawMenu.withdrawAsBagPlayers.add(p.getName());
                        }
                        clickCooldown.compareAndSet(true, false);
                    } else {
                        Double amount = new NBTItem(e.getCurrentItem()).getDouble("RoyaleEconomy");
                        amount = Math.floor(amount * 100) / 100;
                        if (amount != 0) {

                            PreSharedBankWithdrawEvent event = new PreSharedBankWithdrawEvent(bankID, p, amount);
                            Bukkit.getPluginManager().callEvent(event);
                            if (event.isCancelled()) {
                                clickCooldown.compareAndSet(true, false);
                                return;
                            }

                            p.closeInventory();
                            if (withdrawAsBag && p.getInventory().firstEmpty() != -1) {
                                tryBagCreator(amount, p, bankID);
                                RoyaleEconomy.dataManager.getSharedBankManager().addSharedTransactionLog(bankID, p.getName(), "&c-", amount);
                            } else {
                                if (dataManager.getSharedBankManager().removeSharedBankMoneyToFile(bankID, amount)) {
                                    dataManager.addMoneyToFile(p.getUniqueId().toString(), amount);
                                    if (plugin.bankLogger != null)
                                        plugin.bankLogger.getLogger().info("[WITHDRAW SHARED] " + p.getName() + " (" + p.getUniqueId() + ")" + " withdrew " + amount + " coins.");
                                    RoyaleEconomy.dataManager.getSharedBankManager().addSharedTransactionLog(bankID, p.getName(), "&c-", amount);
                                }
                            }
                            Utils.playSound(p, "menus.bank-withdraw");
                            Bukkit.getPluginManager().callEvent(new SharedBankWithdrawEvent(p, amount));
                            PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.menusCfg.getString("shared-menus.withdraw-coins-menu.withdraw-message").replace("%amount%", RoyaleEconomy.messageHelper.numberFormat(amount))));
                            if (amount == bankBalance)
                                RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runLaterAsync(() -> new SharedMainBankMenu(p), 10);
                            else
                                RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runLaterAsync(() -> new SharedBankWithdrawMenu(p), 10);
                        } else
                            clickCooldown.compareAndSet(true, false);
                    }
                } else {
                    clickCooldown.compareAndSet(true, false);
                }
            }
        }

        @EventHandler
        public void onClose(InventoryCloseEvent e) {
            if (e.getInventory().equals(inventory)) {
                inventory = null;
                HandlerList.unregisterAll(this);
            }
            String closingGeneral = "!! CUSTOMIZED IN FUTURE !!";
        }

    }
}
