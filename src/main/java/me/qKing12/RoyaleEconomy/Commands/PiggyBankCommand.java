package me.qKing12.RoyaleEconomy.Commands;

import de.tr7zw.changeme.nbtapi.NBTItem;
import me.qKing12.RoyaleEconomy.API.Events.PiggyBankClickEvent;
import me.qKing12.RoyaleEconomy.DataManager.StaticValues;
import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import me.qKing12.RoyaleEconomy.Menus.MainBankMenu;
import me.qKing12.RoyaleEconomy.Menus.SharedMainBankMenu;
import me.qKing12.RoyaleEconomy.utils.PermissionChecker;
import me.qKing12.RoyaleEconomy.utils.PlayerMessageHandler;
import me.qKing12.RoyaleEconomy.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;

import java.time.ZonedDateTime;
import java.util.*;

import static me.qKing12.RoyaleEconomy.RoyaleEconomy.utilsAPI;



public class PiggyBankCommand  implements TabExecutor, Listener {

    private static HashMap<String, Long> cooldown = new HashMap<>();

    private void givePiggyBank(Player p){
        ItemStack piggyBank = RoyaleEconomy.staticValues.piggyBank.clone();
        NBTItem nbt = new NBTItem(piggyBank);
        nbt.setString("PiggyBank", UUID.randomUUID().toString());
        if(p.getInventory().firstEmpty()==-1)
            p.getWorld().dropItemNaturally(p.getLocation(), nbt.getItem());
        else
            p.getInventory().addItem(nbt.getItem());
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {
        if(args.length==1) {
            ArrayList<String> values = new ArrayList<>();
            if(PermissionChecker.checkPermissionSilent("commands.piggy-bank.piggy-bank-get", commandSender))
                values.add("get");
            if(PermissionChecker.checkPermissionSilent("commands.piggy-bank.piggy-bank-give", commandSender))
                values.add("give");
            return StringUtil.copyPartialMatches(args[0], values, new ArrayList<>());
        }
        return null;
    }

    public PiggyBankCommand() {
        //RoyaleEconomy.plugin.getCommand("piggybank").setExecutor(this);
        //RoyaleEconomy.plugin.getCommand("piggybank").setTabCompleter(this);
    }

    public void extraSetup(){
        Bukkit.getPluginManager().registerEvents(this, RoyaleEconomy.plugin);
        onlyOneBank= RoyaleEconomy.plugin.getConfig().getBoolean("use-only-one-bank");
    }

    private boolean onlyOneBank;

    public void openBank(Player p){
        RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runNextTick((task) -> {
            if (onlyOneBank) {
                String bankID = RoyaleEconomy.dataManager.getSharedBankManager().getSharedBankId(p.getUniqueId().toString());
                if (bankID.equals("")) {
                    PiggyBankClickEvent piggyBankClick = new PiggyBankClickEvent(p, false);
                    Bukkit.getPluginManager().callEvent(piggyBankClick);
                    if (!piggyBankClick.isCancelled())
                        new MainBankMenu(p);
                } else {
                    PiggyBankClickEvent piggyBankClick = new PiggyBankClickEvent(p, true);
                    Bukkit.getPluginManager().callEvent(piggyBankClick);
                    if (!piggyBankClick.isCancelled())
                        new SharedMainBankMenu(p);
                }
            } else {
                PiggyBankClickEvent piggyBankClick = new PiggyBankClickEvent(p, false);
                Bukkit.getPluginManager().callEvent(piggyBankClick);
                if (!piggyBankClick.isCancelled())
                    new MainBankMenu(p);
            }
        });
    }

    @EventHandler
    public void piggyBankClick(PlayerInteractEvent e){
        if(e.getItem()==null || e.getItem().getType().equals(Material.AIR))
            return;
        if(e.getAction().equals(Action.RIGHT_CLICK_BLOCK) || e.getAction().equals(Action.RIGHT_CLICK_AIR)) {
            if (!new NBTItem(e.getItem()).getString("PiggyBank").equals("") && PermissionChecker.checkPermission("piggy-bank-use", e.getPlayer())) {
                e.setCancelled(true);
                if(!(Bukkit.getVersion().contains("1.8") && !Bukkit.getVersion().contains("1.21")) && e.getHand().equals(EquipmentSlot.OFF_HAND)){
                    return;
                }
                if (RoyaleEconomy.staticValues.piggyBankCooldown != 0 && !PermissionChecker.checkPiggyBankBypass(e.getPlayer())) {
                    if (cooldown.containsKey(e.getPlayer().getName())) {
                        if (ZonedDateTime.now().toInstant().getEpochSecond() < cooldown.get(e.getPlayer().getName())) {
                            PlayerMessageHandler.messageSend(e.getPlayer(), utilsAPI.chat(e.getPlayer(), RoyaleEconomy.plugin.getConfig().getString("piggy-bank.cooldown-message")));
                            return;
                        } else {
                            cooldown.remove(e.getPlayer().getName());
                        }
                    }
                    openBank(e.getPlayer());
                    cooldown.put(e.getPlayer().getName(), ZonedDateTime.now().toInstant().getEpochSecond() + RoyaleEconomy.staticValues.piggyBankCooldown);
                }
                else
                    openBank(e.getPlayer());
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            if (args.length > 0) {
                if (args[0].equalsIgnoreCase("get")) {
                    if(PermissionChecker.checkPermission("commands.piggy-bank.piggy-bank-get", p)) {
                        givePiggyBank(p);
                        Utils.playSound(p, "commands.piggy-bank.piggy-bank-get");
                        PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.piggybank.success")));
                    }
                    return true;
                } else if (args[0].equalsIgnoreCase("give")) {
                    if(PermissionChecker.checkPermission("commands.piggy-bank.piggy-bank-give", p)) {
                        if (args.length > 1) {
                            Player p2 = Bukkit.getPlayerExact(args[1]);
                            if (p2 != null) {
                                givePiggyBank(p2);
                                Utils.playSound(p, "commands.piggy-bank.piggy-bank-give.from-player");
                                Utils.playSound(p2, "commands.piggy-bank.piggy-bank-give.to-player");
                                PlayerMessageHandler.messageSend(p2, utilsAPI.chat(p2, RoyaleEconomy.commandsCfg.getString("commands.piggybank.piggy-bank-received")));
                                PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.piggybank.success")));
                            } else
                                PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.piggybank.player-not-found")));
                            return true;
                        }
                    }
                }
            }
            if(PermissionChecker.checkPermission("commands.piggy-bank.command-help", p)) {
                Utils.playSound(p, "commands.piggy-bank.command-help");
                for (String line : RoyaleEconomy.commandsCfg.getStringList("commands.piggybank.command-help"))
                    PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, line));
            }
        } else if(sender.isOp()){
            CommandSender p = sender;
            if (args.length > 1) {
                if (args[0].equalsIgnoreCase("give")) {
                    Player p2 = Bukkit.getPlayerExact(args[1]);
                    if (p2 != null) {
                        givePiggyBank(p2);
                        Utils.playSound(p2, "commands.piggy-bank.piggy-bank-give.to-player");
                        PlayerMessageHandler.messageSend(p2, Utils.chat(RoyaleEconomy.commandsCfg.getString("commands.piggybank.piggy-bank-received")));
                        PlayerMessageHandler.messageSend(p, Utils.chat(RoyaleEconomy.commandsCfg.getString("commands.piggybank.success")));
                    } else
                        PlayerMessageHandler.messageSend(p, Utils.chat(RoyaleEconomy.commandsCfg.getString("commands.piggybank.player-not-found")));
                    return true;
                }
            }
            for (String line : RoyaleEconomy.commandsCfg.getStringList("commands.piggybank.command-help"))
                PlayerMessageHandler.messageSend(p, Utils.chat(line));
        }


        return true;
    }
}
