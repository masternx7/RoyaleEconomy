package me.qKing12.RoyaleEconomy.Commands;

import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import me.qKing12.RoyaleEconomy.Shops.Shop;
import me.qKing12.RoyaleEconomy.Shops.ShopMenu;
import me.qKing12.RoyaleEconomy.Shops.ShopsLoad;
import me.qKing12.RoyaleEconomy.utils.PermissionChecker;
import me.qKing12.RoyaleEconomy.utils.PlayerMessageHandler;
import me.qKing12.RoyaleEconomy.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static me.qKing12.RoyaleEconomy.RoyaleEconomy.utilsAPI;



public class ReShopCommand implements TabExecutor {

    public ReShopCommand(){
        //RoyaleEconomy.plugin.getCommand("reshop").setExecutor(this);
        //RoyaleEconomy.plugin.getCommand("reshop").setTabCompleter(this);
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {
        if(args.length==1) {
            ArrayList<String> values=new ArrayList<>();
            if(PermissionChecker.checkPermissionSilent("commands.reshop.open", commandSender))
                values.add("open");
            if(PermissionChecker.checkPermissionSilent("commands.reshop.forceopen", commandSender))
                values.add("forceopen");
            return StringUtil.copyPartialMatches(args[0], values, new ArrayList<>());
        }
        return null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            if(args.length>0){
                if(args[0].equals("open")){
                    if(PermissionChecker.checkPermission("commands.reshop.open", p)) {
                        if (args.length > 1) {
                            for (Shop shop : ShopsLoad.shops) {
                                if (shop.getShopName().equalsIgnoreCase(args[1])) {
                                    String permission = shop.getPermission();
                                    if(permission.equalsIgnoreCase("none") || p.hasPermission(permission)) {
                                        Utils.playSound(p, "commands.reshop.open");
                                        new ShopMenu(p, shop, 0, null);
                                    }
                                    else{
                                        for (String line : RoyaleEconomy.permissionsCfg.getStringList("no-permission-message"))
                                            PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, line.replace("%permission%", permission)));
                                    }
                                    return true;
                                }
                            }
                            PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.reshop.shop-not-found")));
                            return true;
                        }
                    }
                    else return true;
                }
                if(args[0].equalsIgnoreCase("forceopen")){
                    if(PermissionChecker.checkPermission("commands.reshop.forceopen", p)) {
                        if (args.length > 1) {
                            Player p2 = Bukkit.getPlayerExact(args[1]);
                            if (p2 != null) {
                                if (args.length > 2) {
                                    for (Shop shop : ShopsLoad.shops) {
                                        if (shop.getShopName().equalsIgnoreCase(args[2])) {
                                            Utils.playSound(p, "commands.reshop.forceopen.from-player");
                                            Utils.playSound(p2, "commands.reshop.forceopen.to-player");
                                            new ShopMenu(p2, shop, 0, null);
                                            PlayerMessageHandler.messageSend(p, Utils.chat(RoyaleEconomy.commandsCfg.getString("commands.reshop.success")));
                                            return true;
                                        }
                                    }
                                    PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.reshop.shop-not-found")));
                                    return true;
                                }
                            } else {
                                PlayerMessageHandler.messageSend(p, Utils.chat(RoyaleEconomy.commandsCfg.getString("commands.reshop.player-not-found")));
                                return true;
                            }
                        }
                    }
                    else return true;
                }
            }

            if(PermissionChecker.checkPermission("commands.reshop.command-help", p)) {
                Utils.playSound(p, "commands.reshop.command-help");
                for (String line : RoyaleEconomy.commandsCfg.getStringList("commands.reshop.command-help"))
                    PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, line));
            }
        }
        else if(sender instanceof ConsoleCommandSender){
            ConsoleCommandSender p = (ConsoleCommandSender) sender;
            if(args.length>0){
                if(args[0].equalsIgnoreCase("forceopen")){
                    if(args.length>1){
                        Player p2 = Bukkit.getPlayerExact(args[1]);
                        if(p2!=null){
                            if(args.length>2){
                                for(Shop shop : ShopsLoad.shops){
                                    if(shop.getShopName().equalsIgnoreCase(args[2])){
                                        //PlayerMessageHandler.messageSend(p, Utils.chat(RoyaleEconomy.commandsCfg.getString("commands.reshop.success")));
                                        Utils.playSound(p2, "commands.reshop.forceopen.to-player");
                                        new ShopMenu(p2, shop, 0, null);
                                        return true;
                                    }
                                }
                                PlayerMessageHandler.messageSend(p, Utils.chat(RoyaleEconomy.commandsCfg.getString("commands.reshop.shop-not-found")));
                                return true;
                            }
                        }
                        else{
                            PlayerMessageHandler.messageSend(p, Utils.chat(RoyaleEconomy.commandsCfg.getString("commands.reshop.player-not-found")));
                            return true;
                        }
                    }
                }
            }

            for(String line : RoyaleEconomy.commandsCfg.getStringList("commands.reshop.command-help"))
                PlayerMessageHandler.messageSend(p, Utils.chat(line));
        }
        return true;
    }
}
