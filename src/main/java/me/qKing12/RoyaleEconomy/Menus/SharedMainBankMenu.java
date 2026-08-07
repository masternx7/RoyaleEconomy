package me.qKing12.RoyaleEconomy.Menus;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static me.qKing12.RoyaleEconomy.RoyaleEconomy.*;


public class SharedMainBankMenu  {
    private Inventory inventory;
    int bankUpgrade;

    public SharedMainBankMenu(Player p){
        RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runAsync((task) -> {
            String bankID = RoyaleEconomy.dataManager.getSharedBankManager().getSharedBankId(p.getUniqueId().toString());
            if (bankID == null) {
                RoyaleEconomy.dataManager.updateUsername(p);
                PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.sharedbank.no-shared-bank")));
                return;
            } else if (bankID.equalsIgnoreCase("")) {
                PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.sharedbank.no-shared-bank")));
                return;
            }
            inventory = Bukkit.createInventory(null, RoyaleEconomy.staticValues.sharedmainBankMenuSize, utilsAPI.chat(p, RoyaleEconomy.menusCfg.getString("shared-menus.main-bank-menu.title")));
            bankUpgrade = RoyaleEconomy.dataManager.getSharedBankManager().getSharedBankUpgrade(bankID);
            Utils.playSound(p, "menus.main-bank-open");

            if (RoyaleEconomy.staticValues.sharedbackgroundGlass != null)
                for (int i = 0; i < RoyaleEconomy.staticValues.sharedmainBankMenuSize; i++)
                    inventory.setItem(i, RoyaleEconomy.staticValues.sharedbackgroundGlass);

            Double number = RoyaleEconomy.dataManager.getSharedBankManager().getSharedBankMoneyFromFile(bankID);
            String formatNumber = RoyaleEconomy.messageHelper.numberFormat(number);


            ArrayList<String> lore = new ArrayList<>();
            for (String line : RoyaleEconomy.menusCfg.getStringList("shared-menus.main-bank-menu.deposit-item.lore"))
                lore.add(utilsAPI.chat(p, line
                        .replace("%interest-cooldown%", RoyaleEconomy.messageHelper.getInterestCooldown("normal"))
                        .replace("%interest-cooldown-short%", RoyaleEconomy.messageHelper.getInterestCooldown("short"))
                        .replace("%interest-cooldown-detailed%", RoyaleEconomy.messageHelper.getInterestCooldown("detailed"))
                        .replace("%balance-limit%", RoyaleEconomy.messageHelper.numberFormat(bankUpgradesCfg.getDouble("shared-bank-upgrades." + bankUpgrade + ".maximum-balance")))
                        .replace("%balance%", formatNumber)));

            for (int slot : staticValues.shareddepositItemSlot)
                inventory.setItem(slot, RoyaleEconomy.itemConstructor.getItem(RoyaleEconomy.menusCfg.getString("shared-menus.main-bank-menu.deposit-item.item-id"), utilsAPI.chatApiOnly(p, RoyaleEconomy.menusCfg.getString("shared-menus.main-bank-menu.deposit-item.name")), lore));

            lore = new ArrayList<>();
            for (String line : RoyaleEconomy.menusCfg.getStringList("shared-menus.main-bank-menu.withdraw-item.lore"))
                lore.add(utilsAPI.chat(p, line
                        .replace("%interest-cooldown%", RoyaleEconomy.messageHelper.getInterestCooldown("normal"))
                        .replace("%interest-cooldown-short%", RoyaleEconomy.messageHelper.getInterestCooldown("short"))
                        .replace("%interest-cooldown-detailed%", RoyaleEconomy.messageHelper.getInterestCooldown("detailed"))
                        .replace("%balance-limit%", RoyaleEconomy.messageHelper.numberFormat(bankUpgradesCfg.getDouble("shared-bank-upgrades." + bankUpgrade + ".maximum-balance")))
                        .replace("%balance%", formatNumber)));

            for (int slot : staticValues.sharedwithdrawItemSlot)
                inventory.setItem(slot, RoyaleEconomy.itemConstructor.getItem(RoyaleEconomy.menusCfg.getString("shared-menus.main-bank-menu.withdraw-item.item-id"), utilsAPI.chatApiOnly(p, RoyaleEconomy.menusCfg.getString("shared-menus.main-bank-menu.withdraw-item.name")), lore));

            ArrayList<String> members = RoyaleEconomy.dataManager.getSharedBankManager().getMembersSharedBank(bankID, true);
            lore = new ArrayList<>();
            for (String line : RoyaleEconomy.menusCfg.getStringList("shared-menus.main-bank-menu.shared-information-item.lore-header")) {
                lore.add(utilsAPI.chat(p, line
                        .replace("%bank-owner%", RoyaleEconomy.messageHelper.getPlayerName(members.get(0)))
                ));
            }
            members.remove(0);
            String loreStructure = RoyaleEconomy.menusCfg.getString("shared-menus.main-bank-menu.shared-information-item.lore-members-structure");
            if (members.isEmpty())
                lore.add(utilsAPI.chat(p, RoyaleEconomy.menusCfg.getString("shared-menus.main-bank-menu.shared-information-item.no-members")));
            else
                for (String member : members) {
                    lore.add(utilsAPI.chat(p, loreStructure.replace("%member%", RoyaleEconomy.messageHelper.getPlayerName(member))));
                }
            for (String line : RoyaleEconomy.menusCfg.getStringList("shared-menus.main-bank-menu.shared-information-item.lore-footer"))
                lore.add(utilsAPI.chat(p, line));

            for (int slot : staticValues.sharedPlayersInformationSlot)
                inventory.setItem(slot, RoyaleEconomy.itemConstructor.getItem(RoyaleEconomy.menusCfg.getString("shared-menus.main-bank-menu.shared-information-item.item-id"), utilsAPI.chatApiOnly(p, RoyaleEconomy.menusCfg.getString("shared-menus.main-bank-menu.shared-information-item.name")), lore));

            for (int slot : staticValues.sharedtransactionItemSlot)
                inventory.setItem(slot, RoyaleEconomy.itemConstructor.getItem(RoyaleEconomy.menusCfg.getString("shared-menus.main-bank-menu.transaction-history.item-id"), utilsAPI.chatApiOnly(p, RoyaleEconomy.menusCfg.getString("shared-menus.main-bank-menu.transaction-history.name")), RoyaleEconomy.dataManager.getSharedBankManager().getSharedTransactionLogFromFile(bankID)));

            lore = new ArrayList<>();
            for (String line : RoyaleEconomy.menusCfg.getStringList("shared-menus.main-bank-menu.close-item.lore"))
                lore.add(utilsAPI.chat(p, line)
                        .replace("%balance%", formatNumber));

            for (int slot : staticValues.sharedcloseItemSlot)
                inventory.setItem(slot, RoyaleEconomy.itemConstructor.getItem(RoyaleEconomy.menusCfg.getString("shared-menus.main-bank-menu.close-item.item-id"), utilsAPI.chatApiOnly(p, RoyaleEconomy.menusCfg.getString("shared-menus.main-bank-menu.close-item.name")), lore));

            lore = new ArrayList<>();
            for (String line : RoyaleEconomy.menusCfg.getStringList("shared-menus.main-bank-menu.information-item.lore"))
                lore.add(utilsAPI.chat(p, line
                        .replace("%interest-value%", RoyaleEconomy.messageHelper.numberFormat(RoyaleEconomy.dataManager.interestCalculator(p.getUniqueId().toString(), true, true)))
                        .replace("%interest-cooldown%", RoyaleEconomy.messageHelper.getInterestCooldown("normal"))
                        .replace("%interest-cooldown-short%", RoyaleEconomy.messageHelper.getInterestCooldown("short"))
                        .replace("%interest-cooldown-detailed%", RoyaleEconomy.messageHelper.getInterestCooldown("detailed")))
                        .replace("%balance%", formatNumber)
                        .replace("%balance-limit%", RoyaleEconomy.messageHelper.numberFormat(bankUpgradesCfg.getDouble("shared-bank-upgrades." + bankUpgrade + ".maximum-balance")))
                        .replace("%account-name%", Utils.chat(bankUpgradesCfg.getString("shared-bank-upgrades." + bankUpgrade + ".name"))));
            for (int slot : staticValues.sharedinformationSlot)
                inventory.setItem(slot, RoyaleEconomy.itemConstructor.getItem(RoyaleEconomy.menusCfg.getString("shared-menus.main-bank-menu.information-item.item-id"), utilsAPI.chatApiOnly(p, RoyaleEconomy.menusCfg.getString("shared-menus.main-bank-menu.information-item.name")), lore));


            if(RoyaleEconomy.staticValues.sharedbankUpgradesSlot!=null && !staticValues.sharedbankUpgradesSlot.isEmpty() && staticValues.sharedbankUpgradesSlot.get(0)!=-1) {
                lore = new ArrayList<>();
                for (String line : RoyaleEconomy.menusCfg.getStringList("shared-menus.main-bank-menu.bank-upgrades.lore"))
                    lore.add(utilsAPI.chat(p, line
                            .replace("%balance%", formatNumber)
                            .replace("%balance-limit%", RoyaleEconomy.messageHelper.numberFormat(bankUpgradesCfg.getDouble("shared-bank-upgrades." + bankUpgrade + ".maximum-balance")))
                            .replace("%account-name%", Utils.chat(bankUpgradesCfg.getString("shared-bank-upgrades." + bankUpgrade + ".name")))));

                for (int slot : staticValues.sharedbankUpgradesSlot)
                    inventory.setItem(slot, RoyaleEconomy.itemConstructor.getItem(RoyaleEconomy.menusCfg.getString("shared-menus.main-bank-menu.bank-upgrades.item-id"), utilsAPI.chatApiOnly(p, RoyaleEconomy.menusCfg.getString("shared-menus.main-bank-menu.bank-upgrades.name")), lore));
            }

            HashMap<Integer, CustomItem> customItems = customItemsHandler.getItems("shared-bank-main-menu");
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

    private class ClickListener implements Listener {

        @EventHandler
        public void onClick(InventoryClickEvent e){
            if(!e.getInventory().equals(inventory))
                return;
            e.setCancelled(true);
            if(e.getSlot()<0 || e.getCurrentItem()==null || e.getCurrentItem().getType().equals(Material.AIR)) {
                return;
            }
            //if(inventory.getViewers().isEmpty()) RoyaleEconomy.plugin.getLogger().warning("A closed listener is still active.");
            if(e.getInventory().equals(inventory)){
                e.setCancelled(true);
                if(!e.getClickedInventory().equals(e.getWhoClicked().getInventory())){
                    Player p = (Player) e.getWhoClicked();

                    if(customItemsHandler.tryClick("shared-bank-main-menu", e.getSlot(), p)){
                        return;
                    }

                    if(staticValues.sharedcloseItemSlot!=null && staticValues.sharedcloseItemSlot.contains(e.getSlot())){
                        p.closeInventory();
                        if(!RoyaleEconomy.staticValues.backCommandsMainBank.isEmpty()){
                            for(String command : RoyaleEconomy.staticValues.backCommandsMainBank)
                                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", p.getName()));
                        }
                    }
                    else if(staticValues.sharedbankUpgradesSlot!=null && staticValues.sharedbankUpgradesSlot.contains(e.getSlot())){
                        new SharedBankUpgradesMenu(p);
                    }
                    else if(staticValues.shareddepositItemSlot!=null && staticValues.shareddepositItemSlot.contains(e.getSlot())){
                        new SharedBankDepositMenu(p);
                    }
                    else if(staticValues.sharedwithdrawItemSlot!=null && staticValues.sharedwithdrawItemSlot.contains(e.getSlot())){
                        new SharedBankWithdrawMenu(p);
                    }
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
