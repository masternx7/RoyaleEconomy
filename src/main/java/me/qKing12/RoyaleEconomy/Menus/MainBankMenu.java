package me.qKing12.RoyaleEconomy.Menus;

import me.qKing12.RoyaleEconomy.DataManager.StaticValues;
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

import java.time.ZonedDateTime;
import java.util.ArrayList;

import static me.qKing12.RoyaleEconomy.RoyaleEconomy.*;


public class MainBankMenu  {
    private Inventory inventory;
    int bankUpgrade;

    public MainBankMenu(Player p) {
        RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runAsync((task) -> {
            inventory = Bukkit.createInventory(null, RoyaleEconomy.staticValues.mainBankMenuSize, utilsAPI.chat(p, RoyaleEconomy.menusCfg.getString("menus.main-bank-menu.title")));
            bankUpgrade= RoyaleEconomy.dataManager.getBankUpgrade(p.getUniqueId().toString());
            Utils.playSound(p, "menus.main-bank-open");

            if(RoyaleEconomy.staticValues.backgroundGlass!=null)
                for(int i=0; i<RoyaleEconomy.staticValues.mainBankMenuSize; i++)
                    inventory.setItem(i, RoyaleEconomy.staticValues.backgroundGlass);

            Double number = RoyaleEconomy.dataManager.getBankMoneyFromFile(p.getUniqueId().toString());
            if(number==null) {
                RoyaleEconomy.dataManager.updateUsername(p);
                new MainBankMenu(p);
                inventory=null;
                return;
            }
            String formatNumber = RoyaleEconomy.messageHelper.numberFormat(number);


            ArrayList<String> lore = new ArrayList<>();
            for(String line : RoyaleEconomy.menusCfg.getStringList("menus.main-bank-menu.deposit-item.lore"))
                lore.add(utilsAPI.chat(p, line
                        .replace("%interest-cooldown%", RoyaleEconomy.messageHelper.getInterestCooldown("normal"))
                        .replace("%interest-cooldown-short%", RoyaleEconomy.messageHelper.getInterestCooldown("short"))
                        .replace("%interest-cooldown-detailed%", RoyaleEconomy.messageHelper.getInterestCooldown("detailed"))
                        .replace("%balance-limit%", RoyaleEconomy.messageHelper.numberFormat(bankUpgradesCfg.getDouble("bank-upgrades."+bankUpgrade+".maximum-balance")))
                        .replace("%balance%", formatNumber)));
            for(Integer slot : staticValues.depositItemSlot)
                inventory.setItem(slot, RoyaleEconomy.itemConstructor.getItem(RoyaleEconomy.menusCfg.getString("menus.main-bank-menu.deposit-item.item-id"), utilsAPI.chatApiOnly(p, RoyaleEconomy.menusCfg.getString("menus.main-bank-menu.deposit-item.name")), lore));


            lore = new ArrayList<>();
            for(String line : RoyaleEconomy.menusCfg.getStringList("menus.main-bank-menu.withdraw-item.lore"))
                lore.add(utilsAPI.chat(p, line
                        .replace("%interest-cooldown%", RoyaleEconomy.messageHelper.getInterestCooldown("normal"))
                        .replace("%interest-cooldown-short%", RoyaleEconomy.messageHelper.getInterestCooldown("short"))
                        .replace("%interest-cooldown-detailed%", RoyaleEconomy.messageHelper.getInterestCooldown("detailed"))
                        .replace("%balance-limit%", RoyaleEconomy.messageHelper.numberFormat(bankUpgradesCfg.getDouble("bank-upgrades."+bankUpgrade+".maximum-balance")))
                        .replace("%balance%", formatNumber)));
            for(Integer slot : staticValues.withdrawItemSlot)
                inventory.setItem(slot, RoyaleEconomy.itemConstructor.getItem(RoyaleEconomy.menusCfg.getString("menus.main-bank-menu.withdraw-item.item-id"), utilsAPI.chatApiOnly(p, RoyaleEconomy.menusCfg.getString("menus.main-bank-menu.withdraw-item.name")), lore));

            for(Integer slot : staticValues.transactionItemSlot)
                inventory.setItem(slot, RoyaleEconomy.itemConstructor.getItem(RoyaleEconomy.menusCfg.getString("menus.main-bank-menu.transaction-history.item-id"), utilsAPI.chatApiOnly(p, RoyaleEconomy.menusCfg.getString("menus.main-bank-menu.transaction-history.name")), RoyaleEconomy.dataManager.getTransactionLogFromFile(p.getUniqueId().toString())));


            lore = new ArrayList<>();
            for(String line : RoyaleEconomy.menusCfg.getStringList("menus.main-bank-menu.close-item.lore"))
                lore.add(utilsAPI.chat(p, line)
                        .replace("%balance%", formatNumber));
            for(Integer slot : staticValues.closeItemSlot)
                inventory.setItem(slot, RoyaleEconomy.itemConstructor.getItem(RoyaleEconomy.menusCfg.getString("menus.main-bank-menu.close-item.item-id"), utilsAPI.chatApiOnly(p, RoyaleEconomy.menusCfg.getString("menus.main-bank-menu.close-item.name")), lore));


            lore = new ArrayList<>();
            for(String line : RoyaleEconomy.menusCfg.getStringList("menus.main-bank-menu.information-item.lore"))
                lore.add(utilsAPI.chat(p, line
                        .replace("%interest-value%", RoyaleEconomy.messageHelper.numberFormat(RoyaleEconomy.dataManager.interestCalculator(p.getUniqueId().toString(), true, false)))
                        .replace("%interest-cooldown%", RoyaleEconomy.messageHelper.getInterestCooldown("normal"))
                        .replace("%interest-cooldown-short%", RoyaleEconomy.messageHelper.getInterestCooldown("short"))
                        .replace("%interest-cooldown-detailed%", RoyaleEconomy.messageHelper.getInterestCooldown("detailed")))
                        .replace("%balance%", formatNumber)
                        .replace("%balance-limit%", RoyaleEconomy.messageHelper.numberFormat(bankUpgradesCfg.getDouble("bank-upgrades."+bankUpgrade+".maximum-balance")))
                        .replace("%account-name%", Utils.chat(bankUpgradesCfg.getString("bank-upgrades."+bankUpgrade+".name"))));
            for(Integer slot : staticValues.informationSlot)
                inventory.setItem(slot, RoyaleEconomy.itemConstructor.getItem(RoyaleEconomy.menusCfg.getString("menus.main-bank-menu.information-item.item-id"), utilsAPI.chatApiOnly(p, RoyaleEconomy.menusCfg.getString("menus.main-bank-menu.information-item.name")), lore));


            if(staticValues.bankUpgradesSlot!= null && !staticValues.bankUpgradesSlot.isEmpty() && RoyaleEconomy.staticValues.bankUpgradesSlot.get(0)!=-1) {
                lore = new ArrayList<>();
                for (String line : RoyaleEconomy.menusCfg.getStringList("menus.main-bank-menu.bank-upgrades.lore"))
                    lore.add(utilsAPI.chat(p, line
                            .replace("%balance%", formatNumber)
                            .replace("%balance-limit%", RoyaleEconomy.messageHelper.numberFormat(bankUpgradesCfg.getDouble("bank-upgrades." + bankUpgrade + ".maximum-balance")))
                            .replace("%account-name%", Utils.chat(bankUpgradesCfg.getString("bank-upgrades." + bankUpgrade + ".name")))));
                for(Integer slot : staticValues.bankUpgradesSlot)
                    inventory.setItem(slot, RoyaleEconomy.itemConstructor.getItem(RoyaleEconomy.menusCfg.getString("menus.main-bank-menu.bank-upgrades.item-id"), utilsAPI.chatApiOnly(p, RoyaleEconomy.menusCfg.getString("menus.main-bank-menu.bank-upgrades.name")), lore));
            }

            Utils.runOnPlayer(p, () -> {
                if (!p.isOnline() || inventory == null)
                    return;
                p.openInventory(inventory);
                Bukkit.getPluginManager().registerEvents(new ClickListener(), RoyaleEconomy.plugin);
            });
        });
    }

    private class ClickListener implements Listener{

        @EventHandler
        public void onClick(InventoryClickEvent e){
            if(!e.getInventory().equals(inventory))
                return;
            e.setCancelled(true);
            if(e.getSlot()<0 || e.getCurrentItem()==null || e.getCurrentItem().getType().equals(Material.AIR)) {
                return;
            }
            //if(inventory.getViewers().isEmpty()) RoyaleEconomy.plugin.getLogger().warning("A closed listener is still active.");
                if(!e.getClickedInventory().equals(e.getWhoClicked().getInventory())){
                    Player p = (Player) e.getWhoClicked();

                    if(RoyaleEconomy.staticValues.closeItemSlot.contains(e.getSlot())){
                        p.closeInventory();
                        if(!RoyaleEconomy.staticValues.backCommandsMainBank.isEmpty()){
                            for(String command : RoyaleEconomy.staticValues.backCommandsMainBank)
                                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", p.getName()));
                        }
                    }
                    else if(RoyaleEconomy.staticValues.bankUpgradesSlot.contains(e.getSlot())){
                        new BankUpgradesMenu(p);
                    }
                    else if(RoyaleEconomy.staticValues.depositItemSlot.contains(e.getSlot())){
                        new BankDepositMenu(p);
                    }
                    else if(RoyaleEconomy.staticValues.withdrawItemSlot.contains(e.getSlot())){
                        new BankWithdrawMenu(p);
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
