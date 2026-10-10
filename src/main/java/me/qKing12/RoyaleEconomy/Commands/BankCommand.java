package me.qKing12.RoyaleEconomy.Commands;

import me.qKing12.RoyaleEconomy.Menus.BankUpgradesMenu;
import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import me.qKing12.RoyaleEconomy.Menus.BankDepositMenu;
import me.qKing12.RoyaleEconomy.Menus.BankWithdrawMenu;
import me.qKing12.RoyaleEconomy.Menus.MainBankMenu;
import me.qKing12.RoyaleEconomy.utils.PermissionChecker;
import me.qKing12.RoyaleEconomy.utils.PlayerMessageHandler;
import me.qKing12.RoyaleEconomy.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static me.qKing12.RoyaleEconomy.RoyaleEconomy.utilsAPI;

public class BankCommand implements TabExecutor {
    private static HashMap<String, Long> cooldown=new HashMap<>();
    public static boolean isInCooldown(Player player){
        if(RoyaleEconomy.staticValues.cooldownBank==0 || PermissionChecker.checkBankCooldownBypass(player, true))
            return false;
        if(cooldown.containsKey(player.getUniqueId().toString())){
            if(cooldown.get(player.getUniqueId().toString())<= ZonedDateTime.now().toInstant().toEpochMilli()){
                cooldown.put(player.getUniqueId().toString(), ZonedDateTime.now().toInstant().toEpochMilli()+RoyaleEconomy.staticValues.cooldownBank);
                return false;
            }
            else{
                PlayerMessageHandler.messageSend(player, (utilsAPI.chat(player, RoyaleEconomy.plugin.getConfig().getString("bank-cooldown-message").replace("%cooldown%", String.valueOf((cooldown.get(player.getUniqueId().toString())-ZonedDateTime.now().toInstant().toEpochMilli())/1000)))));
                return true;
            }
        }
        else{
            cooldown.put(player.getUniqueId().toString(), ZonedDateTime.now().toInstant().toEpochMilli()+RoyaleEconomy.staticValues.cooldownBank);
            return false;
        }
    }

    public BankCommand(){
        //RoyaleEconomy.plugin.getCommand("bank").setExecutor(this);
        //RoyaleEconomy.plugin.getCommand("bank").setTabCompleter(this);
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {
        if(args.length==1) {
            ArrayList<String> values=new ArrayList<>();
            values.add("");
            if(PermissionChecker.checkPermissionSilent("commands.bank.bank-balance", commandSender))
                values.add("balance");
            if(PermissionChecker.checkPermissionSilent("commands.bank.bank-upgrades-command", commandSender))
                values.add("upgrades");
            if(PermissionChecker.checkPermissionSilent("commands.bank.bank-deposit-command", commandSender))
                values.add("deposit");
            if(PermissionChecker.checkPermissionSilent("commands.bank.bank-withdraw-command", commandSender))
                values.add("withdraw");
            if(PermissionChecker.checkPermissionSilent("commands.bank.command-help", commandSender))
                values.add("help");
            return StringUtil.copyPartialMatches(args[0], values, new ArrayList<>());
        }
        //else if(args.length==2){
            //if(args[0].equalsIgnoreCase("deposit") || args[0].equalsIgnoreCase("withdraw"))
            //    return new ArrayList<>(Arrays.asList("1", "10", "150", "50", "500"));
        //}
        return null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runAsync((task) -> {
            if (sender instanceof Player) {
                Player p = (Player) sender;
                if(args.length>0){
                    if(args[0].equalsIgnoreCase("balance") || args[0].equalsIgnoreCase("bal")){
                        if(PermissionChecker.checkPermission("commands.bank.bank-balance", p)) {
                            if (args.length == 1) {
                                Utils.playSound(p, "commands.bank.bank-balance");
                                Double coins = RoyaleEconomy.dataManager.getBankMoneyFromFile(p.getUniqueId().toString());
                                Utils.runOnPlayer(p, () -> PlayerMessageHandler.messageSend(p, RoyaleEconomy.messageHelper.getBankBalanceMessage(coins)));
                            } else {
                                if(PermissionChecker.checkPermission("commands.bank.bank-balance-other", p)) {
                                    String uuid = RoyaleEconomy.dataManager.getUUIDfromName(args[1]);
                                    if (uuid == null)
                                        Utils.runOnPlayer(p, () -> PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.bank.player-not-found"))));
                                    else {
                                        Utils.playSound(p, "commands.bank.bank-balance-other");
                                        Utils.runOnPlayer(p, () -> PlayerMessageHandler.messageSend(p, RoyaleEconomy.messageHelper.getBankBalanceMessage(args[1], RoyaleEconomy.dataManager.getBankMoneyFromFile(uuid))));
                                    }
                                }
                            }
                        }
                    }
                    else if(args[0].equalsIgnoreCase("upgrades")){
                        if(PermissionChecker.checkPermission("commands.bank.bank-upgrades-command", p)) {
                            if(!isInCooldown(p))
                                new BankUpgradesMenu(p);
                        }
                    }
                    else if(args[0].equalsIgnoreCase("deposit")){
                        if(PermissionChecker.checkPermission("commands.bank.bank-deposit-command", p)) {
                            if(!isInCooldown(p))
                                new BankDepositMenu(p);
                        }
                    }
                    else if(args[0].equalsIgnoreCase("withdraw")){
                        if(PermissionChecker.checkPermission("commands.bank.bank-withdraw-command", p))
                            if(!isInCooldown(p))
                                new BankWithdrawMenu(p);
                    }
                    else{
                        if(PermissionChecker.checkPermission("commands.bank.command-help", p)) {
                            Utils.playSound(p, "commands.bank.command-help");
                            for (String line : RoyaleEconomy.commandsCfg.getStringList("commands.bank.command-help"))
                                PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, line));
                        }
                    }
                }
                else{
                    if(PermissionChecker.checkPermission("commands.bank.bank-command", p))
                        if(!isInCooldown(p))
                            new MainBankMenu(p);
                }
            }
        });
        return true;
    }
}
