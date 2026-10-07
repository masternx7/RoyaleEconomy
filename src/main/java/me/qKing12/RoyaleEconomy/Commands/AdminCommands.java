package me.qKing12.RoyaleEconomy.Commands;

import me.qKing12.RoyaleEconomy.DataManager.Cache.PlayerMoneyCacheSQL;
import me.qKing12.RoyaleEconomy.DataManager.DataManagerSQL;
import me.qKing12.RoyaleEconomy.DataManager.StaticValues;
import me.qKing12.RoyaleEconomy.Menus.BankUpgradesMenu;
import me.qKing12.RoyaleEconomy.Menus.MainBankMenu;
import me.qKing12.RoyaleEconomy.Menus.SharedBankUpgradesMenu;
import me.qKing12.RoyaleEconomy.Menus.SharedMainBankMenu;
import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import me.qKing12.RoyaleEconomy.utils.GenerateFiles;
import me.qKing12.RoyaleEconomy.utils.PermissionChecker;
import me.qKing12.RoyaleEconomy.utils.PlayerMessageHandler;
import me.qKing12.RoyaleEconomy.utils.Utils;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.util.StringUtil;

import java.io.File;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static me.qKing12.RoyaleEconomy.RoyaleEconomy.*;


public class AdminCommands implements TabExecutor {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String s, String[] args) {
        ArrayList<String> values=new ArrayList<>();
        if(args.length==1) {
            if(PermissionChecker.checkPermissionSilent("commands.royaleeconomy.reload-permission", sender))
                values.add("reload");
            if(PermissionChecker.checkPermissionSilent("commands.royaleeconomy.purse-permission", sender))
                values.add("purse");
            if(PermissionChecker.checkPermissionSilent("commands.royaleeconomy.bank-permission", sender))
                values.add("bank");
            if(PermissionChecker.checkPermissionSilent("commands.royaleeconomy.sharedbank-permission", sender))
                values.add("sharedbank");
            if(PermissionChecker.checkPermissionSilent("commands.royaleeconomy.command-help", sender))
                values.add("help");
            return StringUtil.copyPartialMatches(args[0], values, new ArrayList<>());
        }
        else if(args.length==2) {
            if(args[0].equalsIgnoreCase("purse") && PermissionChecker.checkPermissionSilent("commands.royaleeconomy.purse-permission", sender))
                values.addAll(Arrays.asList("add", "set", "remove"));
            else if(args[0].equalsIgnoreCase("bank") && PermissionChecker.checkPermissionSilent("commands.royaleeconomy.bank-permission", sender))
                values.addAll(Arrays.asList("add", "set", "setUpgrade", "remove", "forceopen", "transactionlog"));
            else if(args[0].equalsIgnoreCase("sharedbank") && PermissionChecker.checkPermissionSilent("commands.royaleeconomy.sharedbank-permission", sender))
                values.addAll(Arrays.asList("add", "set", "setUpgrade", "remove", "forceopen", "delete", "transactionlog", "create"));
            return StringUtil.copyPartialMatches(args[1], values, new ArrayList<>());
        }
        return null;
    }

    public AdminCommands(){
        RoyaleEconomy.plugin.getCommand("royaleeconomy").setExecutor(this);
        RoyaleEconomy.plugin.getCommand("royaleeconomy").setTabCompleter(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (sender instanceof Player) {
            Player p = (Player) sender;
            if(args.length>0){
                if(args[0].equalsIgnoreCase("reload")){
                    if(PermissionChecker.checkPermission("commands.royaleeconomy.reload-permission", p)) {
                        staticValues.reloadValues();
                        p.sendMessage(Utils.chat("&aReloaded config related messages and some settings!"));
                        p.sendMessage(Utils.chat("&aATTENTION! This command will not work for disabling/enabling features, you'll need a restart for those."));
                    }
                    return true;
                }
                else if(args[0].equalsIgnoreCase("dataRemove")){
                    if(p.hasPermission("royaleeconomy.data")){
                        if(args.length>1){
                            if(args.length>2 && args[2].equalsIgnoreCase("-id"))
                                RoyaleEconomy.dataManager.removeUserFromDatabase(args[1], true);
                            else
                                RoyaleEconomy.dataManager.removeUserFromDatabase(args[1], false);
                            PlayerMessageHandler.messageSend(p, Utils.chat("&aIf user was in database, it is removed now."));
                            return true;
                        }
                    }
                }
                else if(args[0].equalsIgnoreCase("removeSharedBankForced")){
                    if(p.hasPermission("royaleeconomy.sharedbank.delete")){
                        if(args.length>1){
                            RoyaleEconomy.dataManager.getSharedBankManager().deleteSharedBank(args[1]);
                            PlayerMessageHandler.messageSend(p, Utils.chat("&aIf the shared bank existed, it is removed now."));
                            return true;
                        }
                    }
                }
                else if(args[0].equalsIgnoreCase("purse")){
                    if(PermissionChecker.checkPermission("commands.royaleeconomy.purse-permission", p)) {
                        if (args.length > 1) {
                            RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runAsync((task) -> {
                                if (args[1].equalsIgnoreCase("add")) {
                                    if (args.length > 2) {
                                        if(args[2].equalsIgnoreCase("*")) {
                                            if (args.length == 3) {
                                                PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.invalid-number")));
                                                return;
                                            }
                                        }
                                        else if (RoyaleEconomy.dataManager.getMoneyFromFile(args[2]) == null) {
                                            PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.player-not-found")));
                                            return;
                                        }
                                        if (args.length > 3) {
                                            if(args[2].equalsIgnoreCase("*")){
                                                try {
                                                    Double toAdd = RoyaleEconomy.messageHelper.getCoinsFromFormat(args[3]);
                                                    if (toAdd <= 0)
                                                        PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.invalid-number")));
                                                    else {
                                                        p.sendMessage(Utils.chat("&6"+RoyaleEconomy.messageHelper.numberFormat(toAdd)+" coins &fsent to &eall players online!"));
                                                        for(Player p2 : Bukkit.getOnlinePlayers()) {
                                                            RoyaleEconomy.dataManager.addMoneyToFile(p2.getUniqueId().toString(), toAdd);
                                                            PlayerMessageHandler.messageSend(p2, utilsAPI.chat(p2, RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.purse.add-done-player-message")).replace("%amount%", RoyaleEconomy.messageHelper.numberFormat(toAdd)));
                                                            Utils.playSound(p2, "commands.royaleeconomy.purse.add-money");
                                                        }
                                                    }
                                                } catch (Exception x) {
                                                    PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.invalid-number")));
                                                }
                                            }
                                            else {
                                                try {
                                                    Double toAdd = RoyaleEconomy.messageHelper.getCoinsFromFormat(args[3]);
                                                    if (toAdd <= 0)
                                                        PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.invalid-number")));
                                                    else {
                                                        Player p2 = Bukkit.getPlayerExact(args[2]);
                                                        if (p2 != null) {
                                                            boolean sendMessage=true;
                                                            if(args.length>4){
                                                                for(int i=4; i<args.length; i++){
                                                                    if(args[i].equalsIgnoreCase("-silent")){
                                                                        sendMessage=false;
                                                                    }
                                                                }
                                                            }
                                                            if(sendMessage) {
                                                                PlayerMessageHandler.messageSend(p2, utilsAPI.chat(p2, RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.purse.add-done-player-message")).replace("%amount%", RoyaleEconomy.messageHelper.numberFormat(toAdd)));
                                                                Utils.playSound(p2, "commands.royaleeconomy.purse.add-money");
                                                            }
                                                        }
                                                        RoyaleEconomy.dataManager.addMoneyToFile(args[2], toAdd);
                                                        PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.purse.add-done-message")).replace("%amount%", RoyaleEconomy.messageHelper.numberFormat(toAdd)).replace("%player%", args[2]));
                                                    }
                                                } catch (Exception x) {
                                                    PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.invalid-number")));
                                                }
                                            }
                                            return;
                                        }
                                    }
                                } else if (args[1].equalsIgnoreCase("remove")) {
                                    if (args.length > 2) {
                                        Double currentAmount = RoyaleEconomy.dataManager.getMoneyFromFile(args[2]);
                                        if (currentAmount == null) {
                                            PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.player-not-found")));
                                            return;
                                        }
                                        if (args.length > 3) {
                                            try {
                                                Double toRemove = RoyaleEconomy.messageHelper.getCoinsFromFormat(args[3]);
                                                if (toRemove <= 0) {
                                                    PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.invalid-number")));
                                                } else {
                                                    if (toRemove > currentAmount) {
                                                        toRemove = currentAmount;
                                                    }
                                                    boolean sendMessage=true;
                                                    if(args.length>4){
                                                        for(int i=4; i<args.length; i++){
                                                            if(args[i].equalsIgnoreCase("-silent")){
                                                                sendMessage=false;
                                                            }
                                                        }
                                                    }
                                                    RoyaleEconomy.dataManager.removeMoneyFromFile(args[2], toRemove);
                                                    if(sendMessage) {
                                                        PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.purse.remove-done-message")).replace("%amount%", RoyaleEconomy.messageHelper.numberFormat(toRemove)).replace("%player%", args[2]));
                                                        Player p2 = Bukkit.getPlayer(args[2]);
                                                        if (p2 != null) {
                                                            PlayerMessageHandler.messageSend(p2, utilsAPI.chat(p2, RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.purse.remove-done-player-message")).replace("%amount%", RoyaleEconomy.messageHelper.numberFormat(toRemove)));
                                                            Utils.playSound(p2, "commands.royaleeconomy.purse.remove-money");
                                                        }
                                                    }
                                                }
                                            } catch (Exception x) {
                                                PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.invalid-number")));
                                            }
                                            return;
                                        }
                                    }
                                } else if (args[1].equalsIgnoreCase("set")) {
                                    if (args.length > 2) {
                                        Double currentAmount = RoyaleEconomy.dataManager.getMoneyFromFile(args[2]);
                                        if (currentAmount == null) {
                                            PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.player-not-found")));
                                            return;
                                        }
                                        if (args.length > 3) {
                                            try {
                                                Double toSet = RoyaleEconomy.messageHelper.getCoinsFromFormat(args[3]);
                                                if (toSet < 0) {
                                                    PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.invalid-number")));
                                                    return;
                                                } else if (toSet > Double.MAX_VALUE)
                                                    toSet = Double.MAX_VALUE;

                                                boolean sendMessage=true;
                                                if(args.length>4){
                                                    for(int i=4; i<args.length; i++){
                                                        if(args[i].equalsIgnoreCase("-silent")){
                                                            sendMessage=false;
                                                        }
                                                    }
                                                }

                                                RoyaleEconomy.dataManager.setMoney(args[2], toSet);
                                                if(sendMessage) {
                                                    PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.purse.set-done-message")).replace("%amount%", RoyaleEconomy.messageHelper.numberFormat(toSet)).replace("%player%", args[2]));
                                                    Player p2 = Bukkit.getPlayer(args[2]);
                                                    if (p2 != null) {
                                                        PlayerMessageHandler.messageSend(p2, utilsAPI.chat(p2, RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.purse.set-done-player-message")).replace("%amount%", RoyaleEconomy.messageHelper.numberFormat(toSet)));
                                                        Utils.playSound(p2, "commands.royaleeconomy.purse.set-money");
                                                    }
                                                }
                                            } catch (Exception x) {
                                                PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.invalid-number")));
                                            }
                                            return;
                                        }
                                    }
                                }
                                for (String line : RoyaleEconomy.commandsCfg.getStringList("commands.royaleeconomy.purse.command-help"))
                                    PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, line));
                            });
                        }
                        else
                            for (String line : RoyaleEconomy.commandsCfg.getStringList("commands.royaleeconomy.purse.command-help"))
                                PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, line));
                    }
                    return true;
                }
                else if(args[0].equalsIgnoreCase("bank")){
                    if(PermissionChecker.checkPermission("commands.royaleeconomy.bank-permission", p)) {
                        if (args.length > 1) {
                            String player;
                            if(args[1].equalsIgnoreCase("forceopen")){
                                if(args.length>2){
                                    Player p2 = Bukkit.getPlayerExact(args[2]);
                                    if(p2!=null) {
                                        PlayerMessageHandler.messageSend(p, Utils.chat("&aDone!"));
                                        RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runAsync((task) -> new MainBankMenu(p2));
                                    }
                                    else
                                        PlayerMessageHandler.messageSend(p, Utils.chat("&cPlayer not found."));
                                    return true;
                                }
                            }
                            else if(args[1].equalsIgnoreCase("forceopenUpgrades")){
                                if(args.length>2){
                                    Player p2 = Bukkit.getPlayerExact(args[2]);
                                    if(p2!=null) {
                                        RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runAsync((task) -> new BankUpgradesMenu(p2));
                                        PlayerMessageHandler.messageSend(p, Utils.chat("&aDone!"));
                                    }
                                    else
                                        PlayerMessageHandler.messageSend(p, Utils.chat("&cPlayer not found."));
                                    return true;
                                }
                            }
                            else if(args[1].equalsIgnoreCase("transactionlog")){
                                if(args.length>2){
                                    try {
                                        String uuid = RoyaleEconomy.dataManager.getUUIDfromName(args[2]);
                                        if(uuid==null){
                                            PlayerMessageHandler.messageSend(p, Utils.chat("&cPlayer is not in the database."));
                                            return true;
                                        }
                                        ArrayList<String> logs = RoyaleEconomy.dataManager.getTransactionLogFromFile(uuid);
                                        ItemStack toGive = new ItemStack(Material.PAPER, 1);
                                        ItemMeta meta = toGive.getItemMeta();
                                        meta.setDisplayName(Utils.chat("&aTransaction Log &c(ADMIN GENERATED)"));
                                        logs.add(0, "");
                                        logs.add(0, Utils.chat("&8Logs of player "+args[2]+" (Personal Bank)"));
                                        logs.add(0, "");
                                        Date date = new Date();
                                        logs.add(0, Utils.chat("&8at "+new SimpleDateFormat("hh:mm:ss").format(date)+" by "+p.getName()));
                                        logs.add(0, Utils.chat("&8Generated on "+new SimpleDateFormat("yyyy/MM/dd").format(date)));
                                        meta.setLore(logs);
                                        toGive.setItemMeta(meta);
                                        p.getInventory().addItem(toGive);
                                    }catch(Exception x){
                                        PlayerMessageHandler.messageSend(p, Utils.chat("&cAn error has occured. No Transaction Log to generate."));
                                    }
                                    return true;
                                }
                            }
                            else if (args[1].equalsIgnoreCase("add")) {
                                if (args.length > 2) {
                                    player = RoyaleEconomy.dataManager.getUUIDfromName(args[2]);
                                    if (player == null) {
                                        PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.player-not-found")));
                                        return true;
                                    }
                                    if (args.length > 3) {
                                        try {
                                            Double currentAmount = RoyaleEconomy.dataManager.getBankMoneyFromFile(player);
                                            Double toAdd = RoyaleEconomy.messageHelper.getCoinsFromFormat(args[3]);
                                            if (toAdd <= 0)
                                                PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.invalid-number")));
                                            else {
                                                if (toAdd + currentAmount > Double.MAX_VALUE)
                                                    toAdd = Double.MAX_VALUE - currentAmount;
                                                RoyaleEconomy.dataManager.addBankMoneyToFile(player, toAdd);
                                                RoyaleEconomy.dataManager.addTransactionLog(player, RoyaleEconomy.staticValues.serverExecutorName, "&a+", toAdd);
                                                boolean sendMessage=true;
                                                if(args.length>4){
                                                    for(int i=4; i<args.length; i++){
                                                        if(args[i].equalsIgnoreCase("-silent")){
                                                            sendMessage=false;
                                                        }
                                                    }
                                                }
                                                if(sendMessage) {
                                                    PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.bank.add-done-message")).replace("%amount%", RoyaleEconomy.messageHelper.numberFormat(toAdd)).replace("%player%", args[2]));
                                                    Player p2 = Bukkit.getPlayer(args[2]);
                                                    if (p2 != null) {
                                                        PlayerMessageHandler.messageSend(p2, utilsAPI.chat(p2, RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.bank.add-done-player-message")).replace("%amount%", RoyaleEconomy.messageHelper.numberFormat(toAdd)));
                                                        Utils.playSound(p2, "commands.royaleeconomy.bank.add-money");
                                                    }
                                                }
                                            }
                                        } catch (Exception x) {
                                            PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.invalid-number")));
                                        }
                                        return true;
                                    }
                                }
                            } else if (args[1].equalsIgnoreCase("remove")) {
                                if (args.length > 2) {
                                    player = RoyaleEconomy.dataManager.getUUIDfromName(args[2]);
                                    if (player == null) {
                                        PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.player-not-found")));
                                        return true;
                                    }
                                    Double currentAmount = RoyaleEconomy.dataManager.getBankMoneyFromFile(player);
                                    if (args.length > 3) {
                                        try {
                                            Double toRemove = RoyaleEconomy.messageHelper.getCoinsFromFormat(args[3]);
                                            if (toRemove <= 0) {
                                                PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.invalid-number")));
                                            } else {
                                                if (toRemove > currentAmount) {
                                                    toRemove = currentAmount;
                                                }
                                                RoyaleEconomy.dataManager.removeBankMoneyToFile(player, toRemove);
                                                RoyaleEconomy.dataManager.addTransactionLog(player, RoyaleEconomy.staticValues.serverExecutorName, "&c-", toRemove);
                                                boolean sendMessage=true;
                                                if(args.length>4){
                                                    for(int i=4; i<args.length; i++){
                                                        if(args[i].equalsIgnoreCase("-silent")){
                                                            sendMessage=false;
                                                        }
                                                    }
                                                }
                                                if(sendMessage) {
                                                    PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.bank.remove-done-message")).replace("%amount%", RoyaleEconomy.messageHelper.numberFormat(toRemove)).replace("%player%", args[2]));
                                                    Player p2 = Bukkit.getPlayer(args[2]);
                                                    if (p2 != null) {
                                                        PlayerMessageHandler.messageSend(p2, utilsAPI.chat(p2, RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.bank.remove-done-player-message")).replace("%amount%", RoyaleEconomy.messageHelper.numberFormat(toRemove)));
                                                        Utils.playSound(p2, "commands.royaleeconomy.bank.remove-money");
                                                    }
                                                }
                                            }
                                        } catch (Exception x) {
                                            PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.invalid-number")));
                                        }
                                        return true;
                                    }
                                }
                            }
                            else if (args[1].equalsIgnoreCase("set")) {
                                if (args.length > 2) {
                                    player = RoyaleEconomy.dataManager.getUUIDfromName(args[2]);
                                    if (player == null) {
                                        PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.player-not-found")));
                                        return true;
                                    }
                                    if (args.length > 3) {
                                        try {
                                            Double toSet = RoyaleEconomy.messageHelper.getCoinsFromFormat(args[3]);
                                            if (toSet < 0) {
                                                PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.invalid-number")));
                                                return true;
                                            } else if (toSet > Double.MAX_VALUE)
                                                toSet = Double.MAX_VALUE;

                                            RoyaleEconomy.dataManager.setBankMoney(player, toSet);
                                            RoyaleEconomy.dataManager.addTransactionLog(player, RoyaleEconomy.staticValues.serverExecutorName, "&8=", toSet);

                                            boolean sendMessage=true;
                                            if(args.length>4){
                                                for(int i=4; i<args.length; i++){
                                                    if(args[i].equalsIgnoreCase("-silent")){
                                                        sendMessage=false;
                                                    }
                                                }
                                            }
                                            if(sendMessage) {
                                                PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.bank.set-done-message")).replace("%amount%", RoyaleEconomy.messageHelper.numberFormat(toSet)).replace("%player%", args[2]));
                                                Player p2 = Bukkit.getPlayer(args[2]);
                                                if (p2 != null) {
                                                    PlayerMessageHandler.messageSend(p2, utilsAPI.chat(p2, RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.bank.set-done-player-message")).replace("%amount%", RoyaleEconomy.messageHelper.numberFormat(toSet)));
                                                    Utils.playSound(p2, "commands.royaleeconomy.bank.set-money");
                                                }
                                            }
                                        } catch (Exception x) {
                                            PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.invalid-number")));
                                        }
                                        return true;
                                    }
                                }
                            }
                            else if (args[1].equalsIgnoreCase("setUpgrade")) {
                                if (args.length > 2) {
                                    player = RoyaleEconomy.dataManager.getUUIDfromName(args[2]);
                                    if (player == null) {
                                        PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.player-not-found")));
                                        return true;
                                    }
                                    if (args.length > 3) {
                                        try {
                                            int toSet = Integer.parseInt(args[3]);
                                            if (toSet < 0) {
                                                PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.invalid-number")));
                                                return true;
                                            }

                                            RoyaleEconomy.dataManager.setBankUpgrade(player, toSet);
                                            if(playerMoneyCache instanceof PlayerMoneyCacheSQL)
                                                ((PlayerMoneyCacheSQL)playerMoneyCache).setBankUpgradeCache(player, toSet);
                                            p.sendMessage(Utils.chat("&aBank upgrade was updated!"));
                                        } catch (Exception x) {
                                            PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.invalid-number")));
                                        }
                                        return true;
                                    }
                                }
                            }
                        }
                        for (String line : RoyaleEconomy.commandsCfg.getStringList("commands.royaleeconomy.bank.command-help"))
                            PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, line));
                        return true;
                    }
                    else return true;
                }
                //else if(args[0].equalsIgnoreCase("dataLock")){
                //    DataManagerSQL.causeDatabaseLocked();
                //}
                else if(args[0].equalsIgnoreCase("sharedbank")){
                    if(PermissionChecker.checkPermission("commands.royaleeconomy.sharedbank-permission", p)) {
                        if (args.length > 1) {
                            String player;
                            if(args[1].equalsIgnoreCase("forceopen")){
                                if(args.length>2){
                                    Player p2 = Bukkit.getPlayerExact(args[2]);
                                    if(p2!=null) {
                                        RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runAsync((task) -> new SharedMainBankMenu(p2));
                                        PlayerMessageHandler.messageSend(p, Utils.chat("&aDone!"));
                                    }
                                    else
                                        PlayerMessageHandler.messageSend(p, Utils.chat("&cPlayer not found."));
                                    return true;
                                }
                            }
                            else if(args[1].equalsIgnoreCase("create")){
                                if(args.length>2){
                                    Player p2 = Bukkit.getPlayerExact(args[2]);
                                    if(p2==null){
                                        p.sendMessage(Utils.chat("&cUser not found."));
                                        return false;
                                    }
                                    RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runAsync((task) -> {
                                        if (RoyaleEconomy.dataManager.getSharedBankManager().createSharedBank(p2)) {
                                            p.sendMessage(Utils.chat("&aShared bank created for player!"));
                                        } else
                                            p.sendMessage(Utils.chat("&cI could not create a shared bank for that player."));
                                    });
                                    return true;
                                }
                            }
                            else if(args[1].equalsIgnoreCase("forceopenUpgrades")){
                                if(args.length>2){
                                    Player p2 = Bukkit.getPlayerExact(args[2]);
                                    if(p2!=null) {
                                        RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runAsync((task) -> new SharedBankUpgradesMenu(p2));
                                        PlayerMessageHandler.messageSend(p, Utils.chat("&aDone!"));
                                    }
                                    else
                                        PlayerMessageHandler.messageSend(p, Utils.chat("&cPlayer not found."));
                                    return true;
                                }
                            }
                            else if(args[1].equalsIgnoreCase("transactionlog")){
                                if(args.length>2){
                                    try {
                                        String bankID=RoyaleEconomy.dataManager.getSharedBankManager().getSharedBankId(args[2]);
                                        if(bankID.equals("")){
                                            PlayerMessageHandler.messageSend(p, Utils.chat("&cThat player doesn't have a shared bank."));
                                            return true;
                                        }
                                        ArrayList<String> logs = RoyaleEconomy.dataManager.getSharedBankManager().getSharedTransactionLogFromFile(bankID);
                                        ItemStack toGive = new ItemStack(Material.PAPER, 1);
                                        ItemMeta meta = toGive.getItemMeta();
                                        meta.setDisplayName(Utils.chat("&aTransaction Log &c(ADMIN GENERATED)"));
                                        logs.add(0, "");
                                        logs.add(0, Utils.chat("&8Logs of player "+args[2]+" (Shared Bank)"));
                                        logs.add(0, "");
                                        Date date = new Date();
                                        logs.add(0, Utils.chat("&8at "+new SimpleDateFormat("hh:mm:ss").format(date)+" by "+p.getName()));
                                        logs.add(0, Utils.chat("&8Generated on "+new SimpleDateFormat("yyyy/MM/dd").format(date)));
                                        meta.setLore(logs);
                                        toGive.setItemMeta(meta);
                                        p.getInventory().addItem(toGive);
                                    }catch(Exception x){
                                        PlayerMessageHandler.messageSend(p, Utils.chat("&cAn error has occured. No Transaction Log to generate."));
                                    }
                                    return true;
                                }
                            }
                            else if (args[1].equalsIgnoreCase("add")) {
                                if (args.length > 2) {
                                    player = RoyaleEconomy.dataManager.getUUIDfromName(args[2]);
                                    if (player == null) {
                                        PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.player-not-found")));
                                        return true;
                                    }
                                    if (args.length > 3) {
                                        try {
                                            String bankID = RoyaleEconomy.dataManager.getSharedBankManager().getSharedBankId(player);
                                            if (bankID.equals("")) {
                                                PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.sharedbank.not-shared-bank")));
                                                return true;
                                            }
                                            Double currentAmount = RoyaleEconomy.dataManager.getSharedBankManager().getSharedBankMoneyFromFile(bankID);
                                            Double toAdd = RoyaleEconomy.messageHelper.getCoinsFromFormat(args[3]);
                                            if (toAdd <= 0)
                                                PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.invalid-number")));
                                            else {
                                                if (toAdd + currentAmount > Double.MAX_VALUE)
                                                    toAdd = Double.MAX_VALUE - currentAmount;
                                                RoyaleEconomy.dataManager.getSharedBankManager().addSharedBankMoneyToFile(bankID, toAdd);
                                                RoyaleEconomy.dataManager.getSharedBankManager().addSharedTransactionLog(bankID, RoyaleEconomy.staticValues.serverExecutorName, "&a+", toAdd);
                                                boolean sendMessage=true;
                                                if(args.length>4){
                                                    for(int i=4; i<args.length; i++){
                                                        if(args[i].equalsIgnoreCase("-silent")){
                                                            sendMessage=false;
                                                        }
                                                    }
                                                }
                                                if(sendMessage) {
                                                    PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.sharedbank.add-done-message")).replace("%amount%", RoyaleEconomy.messageHelper.numberFormat(toAdd)).replace("%player%", args[2]));
                                                    Player p2 = Bukkit.getPlayer(args[2]);
                                                    if (p2 != null) {
                                                        PlayerMessageHandler.messageSend(p2, utilsAPI.chat(p2, RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.sharedbank.add-done-player-message")).replace("%amount%", RoyaleEconomy.messageHelper.numberFormat(toAdd)));
                                                        Utils.playSound(p2, "commands.royaleeconomy.sharedbank.add-money");
                                                    }
                                                }
                                            }
                                        } catch (Exception x) {
                                            PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.invalid-number")));
                                        }
                                        return true;
                                    }
                                }
                            } else if (args[1].equalsIgnoreCase("remove")) {
                                if (args.length > 2) {
                                    player = RoyaleEconomy.dataManager.getUUIDfromName(args[2]);
                                    if (player == null) {
                                        PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.player-not-found")));
                                        return true;
                                    }
                                    String bankID = RoyaleEconomy.dataManager.getSharedBankManager().getSharedBankId(player);
                                    if (bankID.equals("")) {
                                        PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.sharedbank.not-shared-bank")));
                                        return true;
                                    }
                                    Double currentAmount = RoyaleEconomy.dataManager.getSharedBankManager().getSharedBankMoneyFromFile(bankID);
                                    if (args.length > 3) {
                                        try {
                                            Double toRemove = RoyaleEconomy.messageHelper.getCoinsFromFormat(args[3]);
                                            if (toRemove <= 0) {
                                                PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.invalid-number")));
                                            } else {
                                                if (toRemove > currentAmount) {
                                                    toRemove = currentAmount;
                                                }
                                                if(RoyaleEconomy.dataManager.getSharedBankManager().removeSharedBankMoneyToFile(bankID, toRemove)) {
                                                    RoyaleEconomy.dataManager.getSharedBankManager().addSharedTransactionLog(bankID, RoyaleEconomy.staticValues.serverExecutorName, "&c-", toRemove);
                                                    boolean sendMessage=true;
                                                    if(args.length>4){
                                                        for(int i=4; i<args.length; i++){
                                                            if(args[i].equalsIgnoreCase("-silent")){
                                                                sendMessage=false;
                                                            }
                                                        }
                                                    }
                                                    if(sendMessage) {
                                                        PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.sharedbank.remove-done-message")).replace("%amount%", RoyaleEconomy.messageHelper.numberFormat(toRemove)).replace("%player%", args[2]));
                                                        Player p2 = Bukkit.getPlayer(args[2]);
                                                        if (p2 != null) {
                                                            PlayerMessageHandler.messageSend(p2, utilsAPI.chat(p2, RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.sharedbank.remove-done-player-message")).replace("%amount%", RoyaleEconomy.messageHelper.numberFormat(toRemove)));
                                                            Utils.playSound(p2, "commands.royaleeconomy.sharedbank.remove-money");
                                                        }
                                                    }
                                                }
                                            }
                                        } catch (Exception x) {
                                            PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.invalid-number")));
                                        }
                                        return true;
                                    }
                                }
                            } else if (args[1].equalsIgnoreCase("set")) {
                                if (args.length > 2) {
                                    player = RoyaleEconomy.dataManager.getUUIDfromName(args[2]);
                                    if (player == null) {
                                        PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.player-not-found")));
                                        return true;
                                    }
                                    if (args.length > 3) {
                                        try {
                                            Double toSet = RoyaleEconomy.messageHelper.getCoinsFromFormat(args[3]);
                                            if (toSet < 0) {
                                                PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.invalid-number")));
                                                return true;
                                            } else if (toSet > Double.MAX_VALUE)
                                                toSet = Double.MAX_VALUE;

                                            String bankID = RoyaleEconomy.dataManager.getSharedBankManager().getSharedBankId(player);
                                            if (bankID.equals("")) {
                                                PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.sharedbank.not-shared-bank")));
                                                return true;
                                            }

                                            RoyaleEconomy.dataManager.getSharedBankManager().setSharedBankMoney(bankID, toSet);
                                            RoyaleEconomy.dataManager.getSharedBankManager().addSharedTransactionLog(bankID, RoyaleEconomy.staticValues.serverExecutorName, "&8=", toSet);
                                            boolean sendMessage=true;
                                            if(args.length>4){
                                                for(int i=4; i<args.length; i++){
                                                    if(args[i].equalsIgnoreCase("-silent")){
                                                        sendMessage=false;
                                                    }
                                                }
                                            }
                                            if(sendMessage) {
                                                PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.sharedbank.set-done-message")).replace("%amount%", RoyaleEconomy.messageHelper.numberFormat(toSet)).replace("%player%", args[2]));
                                                Player p2 = Bukkit.getPlayer(args[2]);
                                                if (p2 != null) {
                                                    PlayerMessageHandler.messageSend(p2, utilsAPI.chat(p2, RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.sharedbank.set-done-player-message")).replace("%amount%", RoyaleEconomy.messageHelper.numberFormat(toSet)));
                                                    Utils.playSound(p2, "commands.royaleeconomy.sharedbank.set-money");
                                                }
                                            }
                                        } catch (Exception x) {
                                            PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.invalid-number")));
                                        }
                                        return true;
                                    }
                                }
                            }
                            else if (args[1].equalsIgnoreCase("setUpgrade")) {
                                if (args.length > 2) {
                                    player = RoyaleEconomy.dataManager.getUUIDfromName(args[2]);
                                    if (player == null) {
                                        PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.player-not-found")));
                                        return true;
                                    }
                                    if (args.length > 3) {
                                        try {
                                            int toSet = Integer.parseInt(args[3]);
                                            if (toSet < 0) {
                                                PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.invalid-number")));
                                                return true;
                                            }

                                            RoyaleEconomy.dataManager.getSharedBankManager().setSharedBankUpgradePlayer(player, toSet);
                                            p.sendMessage(Utils.chat("&aIf the shared bank exists, it's bank upgrade was updated!"));
                                        } catch (Exception x) {
                                            PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.invalid-number")));
                                        }
                                        return true;
                                    }
                                }
                            }
                            else if (args[1].equalsIgnoreCase("delete")) {
                                if (args.length > 2) {
                                    player = RoyaleEconomy.dataManager.getUUIDfromName(args[2]);
                                    if (player == null) {
                                        PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.player-not-found")));
                                        return true;
                                    }

                                    String bankID = RoyaleEconomy.dataManager.getSharedBankManager().getSharedBankId(player);
                                    if (bankID.equals("")) {
                                        PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.sharedbank.sharedbank-delete-not-found")));
                                        return true;
                                    }

                                    RoyaleEconomy.dataManager.getSharedBankManager().deleteSharedBank(bankID);
                                    PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.sharedbank.sharedbank-delete")));

                                    return true;
                                }
                            }
                        }
                        for (String line : RoyaleEconomy.commandsCfg.getStringList("commands.royaleeconomy.sharedbank.command-help"))
                            PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, line));
                        return true;
                    }
                    else return true;
                }
            }

            if(PermissionChecker.checkPermission("commands.royaleeconomy.command-help", p)) {
                for (String line : RoyaleEconomy.commandsCfg.getStringList("commands.royaleeconomy.command-help"))
                    PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, line));
            }
            return true;
        }
        else if (sender.isOp()) {
            CommandSender p = sender;
            if(args.length>0){
                if(args[0].equalsIgnoreCase("purse")){
                    if(args.length>1){
                        RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runAsync((task) -> {
                            if (args[1].equalsIgnoreCase("add")) {
                                if (args.length > 2) {
                                    if(args[2].equalsIgnoreCase("*")) {
                                        if (args.length == 3) {
                                            p.sendMessage(Utils.chat(RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.invalid-number")));
                                            return;
                                        }
                                    }
                                    else if (RoyaleEconomy.dataManager.getMoneyFromFile(args[2]) == null) {
                                        PlayerMessageHandler.messageSend(p, Utils.chat(RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.player-not-found")));
                                        return;
                                    }
                                    if (args.length > 3) {
                                        if(args[2].equalsIgnoreCase("*")){
                                            try {
                                                Double toAdd = RoyaleEconomy.messageHelper.getCoinsFromFormat(args[3]);
                                                if (toAdd <= 0)
                                                    p.sendMessage(Utils.chat(RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.invalid-number")));
                                                else {
                                                    p.sendMessage(Utils.chat("&6"+toAdd+" coins &fsent to &eall players online!"));
                                                    for(Player p2 : Bukkit.getOnlinePlayers()) {
                                                        RoyaleEconomy.dataManager.addMoneyToFile(p2.getUniqueId().toString(), toAdd);
                                                        PlayerMessageHandler.messageSend(p2, utilsAPI.chat(p2, RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.purse.add-done-player-message")).replace("%amount%", RoyaleEconomy.messageHelper.numberFormat(toAdd)));
                                                        Utils.playSound(p2, "commands.royaleeconomy.purse.add-money");
                                                    }
                                                }
                                            } catch (Exception x) {
                                                p.sendMessage(Utils.chat(RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.invalid-number")));
                                            }
                                        }
                                        else {
                                            try {
                                                Double currentAmount = RoyaleEconomy.dataManager.getMoneyFromFile(args[2]);
                                                Double toAdd = RoyaleEconomy.messageHelper.getCoinsFromFormat(args[3]);
                                                if (toAdd <= 0)
                                                    PlayerMessageHandler.messageSend(p, Utils.chat(RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.invalid-number")));
                                                else {
                                                    if (toAdd + currentAmount > Double.MAX_VALUE)
                                                        toAdd = Double.MAX_VALUE - currentAmount;
                                                    Player p2 = Bukkit.getPlayerExact(args[2]);
                                                    if (p2 != null) {
                                                        boolean sendMessage=true;
                                                        if(args.length>4){
                                                            for(int i=4; i<args.length; i++){
                                                                if(args[i].equalsIgnoreCase("-silent")){
                                                                    sendMessage=false;
                                                                }
                                                            }
                                                        }
                                                        if(sendMessage) {
                                                            PlayerMessageHandler.messageSend(p2, utilsAPI.chat(p2, RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.purse.add-done-player-message")).replace("%amount%", RoyaleEconomy.messageHelper.numberFormat(toAdd)));
                                                            Utils.playSound(p2, "commands.royaleeconomy.purse.add-money");
                                                        }
                                                    }
                                                    RoyaleEconomy.dataManager.addMoneyToFile(args[2], toAdd);
                                                    p.sendMessage(Utils.chat(RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.purse.add-done-message")).replace("%amount%", RoyaleEconomy.messageHelper.numberFormat(toAdd)).replace("%player%", args[2]));
                                                }
                                            } catch (Exception x) {
                                                PlayerMessageHandler.messageSend(p, Utils.chat(RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.invalid-number")));
                                            }
                                        }
                                        return;
                                    }
                                }
                            } else if (args[1].equalsIgnoreCase("remove")) {
                                if (args.length > 2) {
                                    Double currentAmount = RoyaleEconomy.dataManager.getMoneyFromFile(args[2]);
                                    if (currentAmount == null) {
                                        PlayerMessageHandler.messageSend(p, Utils.chat(RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.player-not-found")));
                                        return;
                                    }
                                    if (args.length > 3) {
                                        try {
                                            Double toRemove = RoyaleEconomy.messageHelper.getCoinsFromFormat(args[3]);
                                            if (toRemove <= 0) {
                                                PlayerMessageHandler.messageSend(p, Utils.chat(RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.invalid-number")));
                                            } else {
                                                if (toRemove > currentAmount) {
                                                    toRemove = currentAmount;
                                                }
                                                RoyaleEconomy.dataManager.removeMoneyFromFile(args[2], toRemove);
                                                boolean sendMessage=true;
                                                if(args.length>4){
                                                    for(int i=4; i<args.length; i++){
                                                        if(args[i].equalsIgnoreCase("-silent")){
                                                            sendMessage=false;
                                                        }
                                                    }
                                                }
                                                if(sendMessage) {
                                                    PlayerMessageHandler.messageSend(p, Utils.chat(RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.purse.remove-done-message")).replace("%amount%", RoyaleEconomy.messageHelper.numberFormat(toRemove)).replace("%player%", args[2]));
                                                    Player p2 = Bukkit.getPlayer(args[2]);
                                                    if (p2 != null) {
                                                        PlayerMessageHandler.messageSend(p2, utilsAPI.chat(p2, RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.purse.remove-done-player-message")).replace("%amount%", RoyaleEconomy.messageHelper.numberFormat(toRemove)));
                                                        Utils.playSound(p2, "commands.royaleeconomy.purse.remove-money");
                                                    }
                                                }
                                            }
                                        } catch (Exception x) {
                                            PlayerMessageHandler.messageSend(p, Utils.chat(RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.invalid-number")));
                                        }
                                        return;
                                    }
                                }
                            } else if (args[1].equalsIgnoreCase("set")) {
                                if (args.length > 2) {
                                    Double currentAmount = RoyaleEconomy.dataManager.getMoneyFromFile(args[2]);
                                    if (currentAmount == null) {
                                        PlayerMessageHandler.messageSend(p, Utils.chat(RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.player-not-found")));
                                        return;
                                    }
                                    if (args.length > 3) {
                                        try {
                                            Double toSet = RoyaleEconomy.messageHelper.getCoinsFromFormat(args[3]);
                                            if (toSet < 0) {
                                                PlayerMessageHandler.messageSend(p, Utils.chat(RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.invalid-number")));
                                                return;
                                            } else if (toSet > Double.MAX_VALUE)
                                                toSet = Double.MAX_VALUE;

                                            RoyaleEconomy.dataManager.setMoney(args[2], toSet);
                                            boolean sendMessage=true;
                                            if(args.length>4){
                                                for(int i=4; i<args.length; i++){
                                                    if(args[i].equalsIgnoreCase("-silent")){
                                                        sendMessage=false;
                                                    }
                                                }
                                            }
                                            if(sendMessage) {
                                                PlayerMessageHandler.messageSend(p, Utils.chat(RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.purse.set-done-message")).replace("%amount%", RoyaleEconomy.messageHelper.numberFormat(toSet)).replace("%player%", args[2]));
                                                Player p2 = Bukkit.getPlayer(args[2]);
                                                if (p2 != null) {
                                                    PlayerMessageHandler.messageSend(p2, utilsAPI.chat(p2, RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.purse.set-done-player-message")).replace("%amount%", RoyaleEconomy.messageHelper.numberFormat(toSet)));
                                                    Utils.playSound(p2, "commands.royaleeconomy.purse.set-money");
                                                }
                                            }
                                        } catch (Exception x) {
                                            PlayerMessageHandler.messageSend(p, Utils.chat(RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.invalid-number")));
                                        }
                                        return;
                                    }
                                }
                            }
                            for(String line : RoyaleEconomy.commandsCfg.getStringList("commands.royaleeconomy.purse.command-help"))
                                PlayerMessageHandler.messageSend(p, Utils.chat(line));
                        });
                    }
                    else
                        for(String line : RoyaleEconomy.commandsCfg.getStringList("commands.royaleeconomy.purse.command-help"))
                            PlayerMessageHandler.messageSend(p, Utils.chat(line));
                    return true;
                }
                else if(args[0].equalsIgnoreCase("bank")){
                    if(args.length>1) {
                        String player;
                        if(args[1].equalsIgnoreCase("forceopen")){
                            if(args.length>2){
                                Player p2 = Bukkit.getPlayerExact(args[2]);
                                if(p2!=null) {
                                    //PlayerMessageHandler.messageSend(p, Utils.chat("&aDone!"));
                                    new MainBankMenu(p2);
                                }
                                else
                                    PlayerMessageHandler.messageSend(p, Utils.chat("&cPlayer not found."));
                                return true;
                            }
                        }
                        else if(args[1].equalsIgnoreCase("forceopenUpgrades")){
                            if(args.length>2){
                                Player p2 = Bukkit.getPlayerExact(args[2]);
                                if(p2!=null) {
                                    RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runAsync((task) -> new BankUpgradesMenu(p2));
                                    //PlayerMessageHandler.messageSend(p, Utils.chat("&aDone!"));
                                }
                                else
                                    PlayerMessageHandler.messageSend(p, Utils.chat("&cPlayer not found."));
                                return true;
                            }
                        }
                        else if (args[1].equalsIgnoreCase("setUpgrade")) {
                            if (args.length > 2) {
                                player = RoyaleEconomy.dataManager.getUUIDfromName(args[2]);
                                if (player == null) {
                                    PlayerMessageHandler.messageSend(p, Utils.chat(RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.player-not-found")));
                                    return true;
                                }
                                if (args.length > 3) {
                                    try {
                                        int toSet = Integer.parseInt(args[3]);
                                        if (toSet < 0) {
                                            PlayerMessageHandler.messageSend(p, Utils.chat(RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.invalid-number")));
                                            return true;
                                        }

                                        RoyaleEconomy.dataManager.setBankUpgrade(player, toSet);
                                        if(playerMoneyCache instanceof PlayerMoneyCacheSQL)
                                            ((PlayerMoneyCacheSQL)playerMoneyCache).setBankUpgradeCache(player, toSet);
                                        p.sendMessage(Utils.chat("&aBank upgrade was updated!"));
                                    } catch (Exception x) {
                                        PlayerMessageHandler.messageSend(p, Utils.chat(RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.invalid-number")));
                                    }
                                    return true;
                                }
                            }
                        }
                        else if (args[1].equalsIgnoreCase("add")) {
                            if (args.length > 2) {
                                player= RoyaleEconomy.dataManager.getUUIDfromName(args[2]);
                                if (player == null) {
                                    PlayerMessageHandler.messageSend(p, Utils.chat(RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.player-not-found")));
                                    return true;
                                }
                                if (args.length > 3) {
                                    try {
                                        Double currentAmount = RoyaleEconomy.dataManager.getBankMoneyFromFile(player);
                                        Double toAdd = RoyaleEconomy.messageHelper.getCoinsFromFormat(args[3]);
                                        if (toAdd <= 0)
                                            PlayerMessageHandler.messageSend(p, Utils.chat(RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.invalid-number")));
                                        else {
                                            if (toAdd + currentAmount > Double.MAX_VALUE)
                                                toAdd = Double.MAX_VALUE - currentAmount;
                                            RoyaleEconomy.dataManager.addBankMoneyToFile(player, toAdd);
                                            RoyaleEconomy.dataManager.addTransactionLog(player, RoyaleEconomy.staticValues.serverExecutorName, "&a+", toAdd);
                                            boolean sendMessage=true;
                                            if(args.length>4){
                                                for(int i=4; i<args.length; i++){
                                                    if(args[i].equalsIgnoreCase("-silent")){
                                                        sendMessage=false;
                                                    }
                                                }
                                            }
                                            if(sendMessage) {
                                                PlayerMessageHandler.messageSend(p, Utils.chat(RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.bank.add-done-message")).replace("%amount%", RoyaleEconomy.messageHelper.numberFormat(toAdd)).replace("%player%", args[2]));
                                                Player p2 = Bukkit.getPlayer(args[2]);
                                                if (p2 != null) {
                                                    PlayerMessageHandler.messageSend(p2, utilsAPI.chat(p2, RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.bank.add-done-player-message")).replace("%amount%", RoyaleEconomy.messageHelper.numberFormat(toAdd)));
                                                    Utils.playSound(p2, "commands.royaleeconomy.bank.add-money");
                                                }
                                            }
                                        }
                                    } catch (Exception x) {
                                        PlayerMessageHandler.messageSend(p, Utils.chat(RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.invalid-number")));
                                    }
                                    return true;
                                }
                            }
                        } else if (args[1].equalsIgnoreCase("remove")) {
                            if (args.length > 2) {
                                player = RoyaleEconomy.dataManager.getUUIDfromName(args[2]);
                                if (player == null) {
                                    PlayerMessageHandler.messageSend(p, Utils.chat(RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.player-not-found")));
                                    return true;
                                }
                                Double currentAmount = RoyaleEconomy.dataManager.getBankMoneyFromFile(player);
                                if (args.length > 3) {
                                    try {
                                        Double toRemove = RoyaleEconomy.messageHelper.getCoinsFromFormat(args[3]);
                                        if (toRemove <= 0) {
                                            PlayerMessageHandler.messageSend(p, Utils.chat(RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.invalid-number")));
                                        } else {
                                            if (toRemove > currentAmount) {
                                                toRemove = currentAmount;
                                            }
                                            RoyaleEconomy.dataManager.removeBankMoneyToFile(player, toRemove);
                                            RoyaleEconomy.dataManager.addTransactionLog(player, RoyaleEconomy.staticValues.serverExecutorName, "&c-", toRemove);
                                            boolean sendMessage=true;
                                            if(args.length>4){
                                                for(int i=4; i<args.length; i++){
                                                    if(args[i].equalsIgnoreCase("-silent")){
                                                        sendMessage=false;
                                                    }
                                                }
                                            }
                                            if(sendMessage) {
                                                PlayerMessageHandler.messageSend(p, Utils.chat(RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.bank.remove-done-message")).replace("%amount%", RoyaleEconomy.messageHelper.numberFormat(toRemove)).replace("%player%", args[2]));
                                                Player p2 = Bukkit.getPlayer(args[2]);
                                                if (p2 != null) {
                                                    PlayerMessageHandler.messageSend(p2, utilsAPI.chat(p2, RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.bank.remove-done-player-message")).replace("%amount%", RoyaleEconomy.messageHelper.numberFormat(toRemove)));
                                                    Utils.playSound(p2, "commands.royaleeconomy.bank.remove-money");
                                                }
                                            }
                                        }
                                    } catch (Exception x) {
                                        PlayerMessageHandler.messageSend(p, Utils.chat(RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.invalid-number")));
                                    }
                                    return true;
                                }
                            }
                        } else if (args[1].equalsIgnoreCase("set")) {
                            if (args.length > 2) {
                                player = RoyaleEconomy.dataManager.getUUIDfromName(args[2]);
                                if (player == null) {
                                    PlayerMessageHandler.messageSend(p, Utils.chat(RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.player-not-found")));
                                    return true;
                                }
                                if (args.length > 3) {
                                    try {
                                        Double toSet = RoyaleEconomy.messageHelper.getCoinsFromFormat(args[3]);
                                        if (toSet < 0) {
                                            PlayerMessageHandler.messageSend(p, Utils.chat(RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.invalid-number")));
                                            return true;
                                        } else if (toSet > Double.MAX_VALUE)
                                            toSet = Double.MAX_VALUE;

                                        RoyaleEconomy.dataManager.setBankMoney(player, toSet);
                                        RoyaleEconomy.dataManager.addTransactionLog(player, RoyaleEconomy.staticValues.serverExecutorName, "&8=", toSet);
                                        boolean sendMessage=true;
                                        if(args.length>4){
                                            for(int i=4; i<args.length; i++){
                                                if(args[i].equalsIgnoreCase("-silent")){
                                                    sendMessage=false;
                                                }
                                            }
                                        }
                                        if(sendMessage) {
                                            PlayerMessageHandler.messageSend(p, Utils.chat(RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.bank.set-done-message")).replace("%amount%", RoyaleEconomy.messageHelper.numberFormat(toSet)).replace("%player%", args[2]));
                                            Player p2 = Bukkit.getPlayer(args[2]);
                                            if (p2 != null) {
                                                PlayerMessageHandler.messageSend(p2, utilsAPI.chat(p2, RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.bank.set-done-player-message")).replace("%amount%", RoyaleEconomy.messageHelper.numberFormat(toSet)));
                                                Utils.playSound(p2, "commands.royaleeconomy.bank.set-money");
                                            }
                                        }
                                    } catch (Exception x) {
                                        PlayerMessageHandler.messageSend(p, Utils.chat(RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.invalid-number")));
                                    }
                                    return true;
                                }
                            }
                        }
                        for(String line : RoyaleEconomy.commandsCfg.getStringList("commands.royaleeconomy.bank.command-help"))
                            PlayerMessageHandler.messageSend(p, Utils.chat(line));
                        return true;
                    }
                }
                else if(args[0].equalsIgnoreCase("sharedbank")){
                    if(args.length>1) {
                        String player;
                        if(args[1].equalsIgnoreCase("forceopen")){
                            if(args.length>2){
                                Player p2 = Bukkit.getPlayerExact(args[2]);
                                if(p2!=null) {
                                    //PlayerMessageHandler.messageSend(p, Utils.chat("&aDone!"));
                                    new SharedMainBankMenu(p2);
                                }
                                else
                                    PlayerMessageHandler.messageSend(p, Utils.chat("&cPlayer not found."));
                                return true;
                            }
                        }
                        else if(args[1].equalsIgnoreCase("create")){
                            if(args.length>2){
                                Player p2 = Bukkit.getPlayerExact(args[2]);
                                if(p2==null){
                                    p.sendMessage(Utils.chat("&cUser not found."));
                                    return false;
                                }
                                RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runAsync((task) -> RoyaleEconomy.dataManager.getSharedBankManager().createSharedBank(p2));
                            }
                        }
                        else if(args[1].equalsIgnoreCase("forceopenUpgrades")){
                            if(args.length>2){
                                Player p2 = Bukkit.getPlayerExact(args[2]);
                                if(p2!=null) {
                                    RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runAsync((task) -> new SharedBankUpgradesMenu(p2));
                                }
                                else
                                    PlayerMessageHandler.messageSend(p, Utils.chat("&cPlayer not found."));
                                return true;
                            }
                        }
                        else if (args[1].equalsIgnoreCase("add")) {
                            if (args.length > 2) {
                                player= RoyaleEconomy.dataManager.getUUIDfromName(args[2]);
                                if (player == null) {
                                    PlayerMessageHandler.messageSend(p, Utils.chat(RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.player-not-found")));
                                    return true;
                                }
                                if (args.length > 3) {
                                    try {
                                        String bankID = RoyaleEconomy.dataManager.getSharedBankManager().getSharedBankId(player);
                                        if(bankID.equals("")){
                                            PlayerMessageHandler.messageSend(p, Utils.chat(RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.sharedbank.not-shared-bank")));
                                            return true;
                                        }
                                        Double currentAmount = RoyaleEconomy.dataManager.getSharedBankManager().getSharedBankMoneyFromFile(bankID);
                                        Double toAdd = RoyaleEconomy.messageHelper.getCoinsFromFormat(args[3]);
                                        if (toAdd <= 0)
                                            PlayerMessageHandler.messageSend(p, Utils.chat(RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.invalid-number")));
                                        else {
                                            if (toAdd + currentAmount > Double.MAX_VALUE)
                                                toAdd = Double.MAX_VALUE - currentAmount;
                                            RoyaleEconomy.dataManager.getSharedBankManager().addSharedBankMoneyToFile(bankID, toAdd);
                                            RoyaleEconomy.dataManager.getSharedBankManager().addSharedTransactionLog(bankID, RoyaleEconomy.staticValues.serverExecutorName, "&a+", toAdd);
                                            boolean sendMessage=true;
                                            if(args.length>4){
                                                for(int i=4; i<args.length; i++){
                                                    if(args[i].equalsIgnoreCase("-silent")){
                                                        sendMessage=false;
                                                    }
                                                }
                                            }
                                            if(sendMessage) {
                                                PlayerMessageHandler.messageSend(p, Utils.chat(RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.sharedbank.add-done-message")).replace("%amount%", RoyaleEconomy.messageHelper.numberFormat(toAdd)).replace("%player%", args[2]));
                                                Player p2 = Bukkit.getPlayer(args[2]);
                                                if (p2 != null) {
                                                    PlayerMessageHandler.messageSend(p2, utilsAPI.chat(p2, RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.sharedbank.add-done-player-message")).replace("%amount%", RoyaleEconomy.messageHelper.numberFormat(toAdd)));
                                                    Utils.playSound(p2, "commands.royaleeconomy.sharedbank.add-money");
                                                }
                                            }
                                        }
                                    } catch (Exception x) {
                                        PlayerMessageHandler.messageSend(p, Utils.chat(RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.invalid-number")));
                                    }
                                    return true;
                                }
                            }
                        } else if (args[1].equalsIgnoreCase("remove")) {
                            if (args.length > 2) {
                                player = RoyaleEconomy.dataManager.getUUIDfromName(args[2]);
                                if (player == null) {
                                    PlayerMessageHandler.messageSend(p, Utils.chat(RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.player-not-found")));
                                    return true;
                                }
                                String bankID = RoyaleEconomy.dataManager.getSharedBankManager().getSharedBankId(player);
                                if(bankID.equals("")){
                                    PlayerMessageHandler.messageSend(p, Utils.chat(RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.sharedbank.no-shared-bank")));
                                    return true;
                                }
                                Double currentAmount = RoyaleEconomy.dataManager.getSharedBankManager().getSharedBankMoneyFromFile(bankID);
                                if (args.length > 3) {
                                    try {
                                        Double toRemove = RoyaleEconomy.messageHelper.getCoinsFromFormat(args[3]);
                                        if (toRemove <= 0) {
                                            PlayerMessageHandler.messageSend(p, Utils.chat(RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.invalid-number")));
                                        } else {
                                            if (toRemove > currentAmount) {
                                                toRemove = currentAmount;
                                            }
                                            if(RoyaleEconomy.dataManager.getSharedBankManager().removeSharedBankMoneyToFile(bankID, toRemove)) {
                                                RoyaleEconomy.dataManager.getSharedBankManager().addSharedTransactionLog(bankID, RoyaleEconomy.staticValues.serverExecutorName, "&c-", toRemove);
                                                boolean sendMessage=true;
                                                if(args.length>4){
                                                    for(int i=4; i<args.length; i++){
                                                        if(args[i].equalsIgnoreCase("-silent")){
                                                            sendMessage=false;
                                                        }
                                                    }
                                                }
                                                if(sendMessage) {
                                                    PlayerMessageHandler.messageSend(p, Utils.chat(RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.sharedbank.remove-done-message")).replace("%amount%", RoyaleEconomy.messageHelper.numberFormat(toRemove)).replace("%player%", args[2]));
                                                    Player p2 = Bukkit.getPlayer(args[2]);
                                                    if (p2 != null) {
                                                        PlayerMessageHandler.messageSend(p2, utilsAPI.chat(p2, RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.sharedbank.remove-done-player-message")).replace("%amount%", RoyaleEconomy.messageHelper.numberFormat(toRemove)));
                                                        Utils.playSound(p2, "commands.royaleeconomy.sharedbank.remove-money");
                                                    }
                                                }
                                            }
                                        }
                                    } catch (Exception x) {
                                        PlayerMessageHandler.messageSend(p, Utils.chat(RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.invalid-number")));
                                    }
                                    return true;
                                }
                            }
                        } else if (args[1].equalsIgnoreCase("set")) {
                            if (args.length > 2) {
                                player = RoyaleEconomy.dataManager.getUUIDfromName(args[2]);
                                if (player == null) {
                                    PlayerMessageHandler.messageSend(p, Utils.chat(RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.player-not-found")));
                                    return true;
                                }
                                if (args.length > 3) {
                                    try {
                                        Double toSet = RoyaleEconomy.messageHelper.getCoinsFromFormat(args[3]);
                                        if (toSet < 0) {
                                            PlayerMessageHandler.messageSend(p, Utils.chat(RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.invalid-number")));
                                            return true;
                                        } else if (toSet > Double.MAX_VALUE)
                                            toSet = Double.MAX_VALUE;

                                        String bankID= RoyaleEconomy.dataManager.getSharedBankManager().getSharedBankId(player);
                                        if(bankID.equals("")){
                                            PlayerMessageHandler.messageSend(p, Utils.chat(RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.sharedbank.no-shared-bank")));
                                            return true;
                                        }

                                        RoyaleEconomy.dataManager.getSharedBankManager().setSharedBankMoney(bankID, toSet);
                                        RoyaleEconomy.dataManager.getSharedBankManager().addSharedTransactionLog(bankID, RoyaleEconomy.staticValues.serverExecutorName, "&8=", toSet);
                                        boolean sendMessage=true;
                                        if(args.length>4){
                                            for(int i=4; i<args.length; i++){
                                                if(args[i].equalsIgnoreCase("-silent")){
                                                    sendMessage=false;
                                                }
                                            }
                                        }
                                        if(sendMessage) {
                                            PlayerMessageHandler.messageSend(p, Utils.chat(RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.sharedbank.set-done-message")).replace("%amount%", RoyaleEconomy.messageHelper.numberFormat(toSet)).replace("%player%", args[2]));
                                            Player p2 = Bukkit.getPlayer(args[2]);
                                            if (p2 != null) {
                                                PlayerMessageHandler.messageSend(p2, utilsAPI.chat(p2, RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.sharedbank.set-done-player-message")).replace("%amount%", RoyaleEconomy.messageHelper.numberFormat(toSet)));
                                                Utils.playSound(p2, "commands.royaleeconomy.sharedbank.set-money");
                                            }
                                        }
                                    } catch (Exception x) {
                                        PlayerMessageHandler.messageSend(p, Utils.chat(RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.invalid-number")));
                                    }
                                    return true;
                                }
                            }
                        }
                        for(String line : RoyaleEconomy.commandsCfg.getStringList("commands.royaleeconomy.sharedbank.command-help"))
                            PlayerMessageHandler.messageSend(p, Utils.chat(line));
                        return true;
                    }
                }
            }
            for(String line : RoyaleEconomy.commandsCfg.getStringList("commands.royaleeconomy.command-help"))
                PlayerMessageHandler.messageSend(p, Utils.chat(line));
            return true;
        }
        return true;
    }
}
