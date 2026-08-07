package me.qKing12.RoyaleEconomy.Events;

import de.tr7zw.changeme.nbtapi.NBTItem;
import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import me.qKing12.RoyaleEconomy.Shops.ShopPlayerCache;
import me.qKing12.RoyaleEconomy.utils.PermissionChecker;
import me.qKing12.RoyaleEconomy.utils.PlayerMessageHandler;
import me.qKing12.RoyaleEconomy.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import static me.qKing12.RoyaleEconomy.RoyaleEconomy.utilsAPI;



public class Events implements Listener {

    public Events(){
        Bukkit.getPluginManager().registerEvents(this, RoyaleEconomy.plugin);
        if(RoyaleEconomy.killCoinsAndPurseDeathCfg.getBoolean("purse-coins-handle.use-purse-coins-death")){
            new PurseDeathEvent();
        }
        if(RoyaleEconomy.coinBagsAndTalismansCfg.getBoolean("talismans.use-talismans"))
            new TalismansEvents();
    }

    @EventHandler
    public void onMoneyBagClick(PlayerInteractEvent e){
        if(e.getItem()==null || e.getItem().getType().equals(Material.AIR))
            return;
        if(e.getAction().equals(Action.RIGHT_CLICK_BLOCK) || e.getAction().equals(Action.RIGHT_CLICK_AIR)){
            double amount = new NBTItem(e.getItem()).getDouble("RoyaleEconomyBag");
            if(amount!=0){
                e.setCancelled(true);
                if(!(Bukkit.getVersion().contains("1.8") && !Bukkit.getVersion().contains("1.21")) && e.getHand().equals(EquipmentSlot.OFF_HAND)){
                    return;
                }
                if (!PermissionChecker.checkPermission("commands.moneybag.claim-bag", e.getPlayer())) {
                    return;
                }
                ItemStack temporaryItem = e.getItem().clone();
                temporaryItem.setAmount(e.getItem().getAmount()-1);
                e.getPlayer().getInventory().setItem(e.getPlayer().getInventory().getHeldItemSlot(), temporaryItem);
                Utils.playSound(e.getPlayer(), "others.coins-bag-claim");
                RoyaleEconomy.dataManager.addMoneyToFile(e.getPlayer().getUniqueId().toString(), amount);
                PlayerMessageHandler.messageSend(e.getPlayer(), utilsAPI.chat(e.getPlayer(), RoyaleEconomy.coinBagsAndTalismansCfg.getString("money-bags.money-bag-click").replace("%amount%", RoyaleEconomy.messageHelper.numberFormat(amount))));
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){
        RoyaleEconomy.dataManager.updateUsername(e.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e){
        ShopPlayerCache.shopHistory.remove(e.getPlayer());
    }

}
