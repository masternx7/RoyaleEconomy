package me.qKing12.RoyaleEconomy.Menus;

import me.qKing12.RoyaleEconomy.CustomMenuItems.CustomItem;
import me.qKing12.RoyaleEconomy.DataManager.StaticValues;
import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import me.qKing12.RoyaleEconomy.utils.PlayerMessageHandler;
import me.qKing12.RoyaleEconomy.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static me.qKing12.RoyaleEconomy.RoyaleEconomy.*;



public class SharedBankUpgradesMenu {
    private Inventory inventory;
    int bankUpgrade;
    private ArrayList<Integer> nextUpgradeSlot = new ArrayList<>();
    private String bankID;

    private boolean hasRequirements(Player p){
        int bankToCheck = bankUpgrade+1;
        String permission = bankUpgradesCfg.getString("shared-bank-upgrades."+bankToCheck+".requirements.permission");
        if(!permission.equals("none") && !p.hasPermission(permission))
            return false;
        double coins = bankUpgradesCfg.getDouble("shared-bank-upgrades."+bankToCheck+".requirements.coins");
        return RoyaleEconomy.dataManager.removeMoneyFromFile(p.getUniqueId().toString(), coins);
    }

    public SharedBankUpgradesMenu(Player p){
        RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runAsync((task) -> {
            bankID = RoyaleEconomy.dataManager.getSharedBankManager().getSharedBankId(p.getUniqueId().toString());

            if (bankID.equals("")) {
                RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runNextTick(task1 -> p.closeInventory());
                return;
            }

            inventory = Bukkit.createInventory(null, RoyaleEconomy.staticValues.sharedbankUpgradesMenuSize, utilsAPI.chat(p, RoyaleEconomy.menusCfg.getString("menus.bank-upgrades-menu.title")));
            bankUpgrade = RoyaleEconomy.dataManager.getSharedBankManager().getSharedBankUpgrade(bankID);
            Utils.playSound(p, "menus.bank-upgrades-menu-open");

            if (RoyaleEconomy.staticValues.sharedbackgroundGlassBankUpgrades != null)
                for (int i = 0; i < RoyaleEconomy.staticValues.sharedbankUpgradesMenuSize; i++)
                    inventory.setItem(i, staticValues.sharedbackgroundGlassBankUpgrades);

            ArrayList<String> lore = new ArrayList<>();
            for (String line : RoyaleEconomy.menusCfg.getStringList("shared-menus.bank-upgrades-menu.go-back-item.lore"))
                lore.add(utilsAPI.chat(p, line));

            for (int slot : staticValues.sharedgoBackItemSlot)
                inventory.setItem(slot, RoyaleEconomy.itemConstructor.getItem(RoyaleEconomy.menusCfg.getString("shared-menus.bank-upgrades-menu.go-back-item.item-id"), utilsAPI.chat(p, RoyaleEconomy.menusCfg.getString("shared-menus.bank-upgrades-menu.go-back-item.name")), lore));

            String previousAdd = RoyaleEconomy.menusCfg.getString("shared-menus.bank-upgrades-menu.lore-additions.previous-upgrade");
            String ownAdd = RoyaleEconomy.menusCfg.getString("shared-menus.bank-upgrades-menu.lore-additions.own-upgrade");
            String betterAdd = RoyaleEconomy.menusCfg.getString("shared-menus.bank-upgrades-menu.lore-additions.better-upgrade");
            String needUpgrade = RoyaleEconomy.menusCfg.getString("shared-menus.bank-upgrades-menu.lore-additions.need-previous-upgrade");
            for (String key : RoyaleEconomy.menusCfg.getConfigurationSection("shared-menus.bank-upgrades-menu.bank-upgrades-items").getKeys(false)) {
                lore = new ArrayList<>();
                int currentBank = RoyaleEconomy.menusCfg.getInt("shared-menus.bank-upgrades-menu.bank-upgrades-items." + key + ".bank-upgrade-index");
                int slot = RoyaleEconomy.menusCfg.getInt("shared-menus.bank-upgrades-menu.bank-upgrades-items." + key + ".slot");

                for (String line : bankUpgradesCfg.getStringList("shared-bank-upgrades." + currentBank + ".lore"))
                    lore.add(utilsAPI.chat(p, line));

                if (currentBank < bankUpgrade) {
                    if (!previousAdd.equals(""))
                        lore.add(utilsAPI.chat(p, previousAdd));
                } else if (currentBank == bankUpgrade) {
                    if (!ownAdd.equals(""))
                        lore.add(utilsAPI.chat(p, ownAdd));
                } else {
                    if (currentBank == bankUpgrade + 1)
                        nextUpgradeSlot.add(slot);
                    else if (!needUpgrade.equals(""))
                        lore.add(utilsAPI.chat(p, needUpgrade));
                    if (!betterAdd.equals(""))
                        lore.add(utilsAPI.chat(p, betterAdd));
                }

                ItemStack toDisplay = RoyaleEconomy.itemConstructor.getItem(RoyaleEconomy.menusCfg.getString("shared-menus.bank-upgrades-menu.bank-upgrades-items." + key + ".item-id"), utilsAPI.chat(p, bankUpgradesCfg.getString("shared-bank-upgrades." + currentBank + ".name")), lore);

                if (currentBank == bankUpgrade) {
                    toDisplay.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
                    ItemMeta meta = toDisplay.getItemMeta();
                    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
                    toDisplay.setItemMeta(meta);
                }

                inventory.setItem(slot, toDisplay);
            }

            HashMap<Integer, CustomItem> customItems = customItemsHandler.getItems("shared-bank-upgrades-menu");
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

                    if(customItemsHandler.tryClick("shared-bank-upgrades-menu", e.getSlot(), p)){
                        return;
                    }

                    if(staticValues.sharedgoBackItemSlot != null && staticValues.sharedgoBackItemSlot.contains(e.getSlot())){
                        if(menusCfg.contains("shared-menus.bank-upgrades-menu.go-back-item.commands")){
                            p.closeInventory();
                            for(String line : menusCfg.getStringList("shared-menus.bank-upgrades-menu.go-back-item.commands"))
                                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), line.replace("%player-name%", p.getName()));
                        }
                        else {
                            RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runAsync((task) -> new SharedMainBankMenu(p));
                        }
                    }
                    else if(nextUpgradeSlot.contains(e.getSlot())){
                        if(hasRequirements(p)){
                            p.closeInventory();
                            Utils.playSound(p, "menus.bank-upgrade");
                            RoyaleEconomy.dataManager.getSharedBankManager().setSharedBankUpgrade(bankID, bankUpgrade+1);
                            for(String line : bankUpgradesCfg.getStringList("shared-bank-upgrades."+(bankUpgrade+1)+".upgrade-message"))
                                PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, line));

                            if(bankUpgradesCfg.contains("shared-bank-upgrades."+(bankUpgrade+1)+".upgrade-commands")) {
                                for (String cmd : bankUpgradesCfg.getStringList("shared-bank-upgrades." + (bankUpgrade + 1) + ".upgrade-commands"))
                                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd
                                            .replace("%player-name%", p.getName())
                                    );
                            }
                        }
                        else
                            PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, bankUpgradesCfg.getString("shared-bank-upgrades."+(bankUpgrade+1)+".deny-message")));
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
