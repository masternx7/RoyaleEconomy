package me.qKing12.RoyaleEconomy.utils;

import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static me.qKing12.RoyaleEconomy.RoyaleEconomy.utilsAPI;



public class PermissionChecker {

    public static boolean checkPermission(String key, Player p){
        try{
            Utils.playSound(p, "no-permission-sound");
        }
        catch(Exception x){
            x.printStackTrace();
        }
        try {
            String permission = RoyaleEconomy.permissionsCfg.getString(key, "none");
            if (permission.equalsIgnoreCase("none") || p.hasPermission(permission)) {
                return true;
            } else {
                for (String line : RoyaleEconomy.permissionsCfg.getStringList("no-permission-message"))
                    PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, line.replace("%permission%", permission)));
                return false;
            }
        }
        catch(Exception x){
            RoyaleEconomy.plugin.getLogger().warning("I couldn't check permission: "+key+" please report this!");
            return true;
        }
    }

    public static boolean checkPermissionSilent(String key, CommandSender p){
        try {
            String permission = RoyaleEconomy.permissionsCfg.getString(key);
            if (permission.equalsIgnoreCase("none") || p.hasPermission(permission)) {
                return true;
            } else {
                return false;
            }
        }
        catch(Exception x){
            RoyaleEconomy.plugin.getLogger().warning("I couldn't check permission: "+key+" please report this!");
            return true;
        }
    }

    public static boolean checkKillCoinsBypass(Player p){
        String permission = RoyaleEconomy.permissionsCfg.getString("kill-coins-bypass-limit");
        return permission.equalsIgnoreCase("none") || p.hasPermission(permission);
    }

    public static boolean checkPiggyBankBypass(Player p){
        String permission = RoyaleEconomy.permissionsCfg.getString("piggy-bank-cooldown-bypass");
        return p.hasPermission(permission);
    }

    public static boolean checkBankCooldownBypass(Player p, boolean shared){
        String permission = shared?RoyaleEconomy.permissionsCfg.getString("commands.sharedbank.sharedbank-cooldown-bypass"):RoyaleEconomy.permissionsCfg.getString("commands.bank.bank-cooldown-bypass");
        return p.hasPermission(permission);
    }

}
