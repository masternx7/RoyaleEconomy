package me.qKing12.RoyaleEconomy.Commands;

import me.qKing12.RoyaleEconomy.RoyaleEconomy;
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

import static me.qKing12.RoyaleEconomy.RoyaleEconomy.noEconomy;
import static me.qKing12.RoyaleEconomy.RoyaleEconomy.utilsAPI;



public class BalanceTopCommand implements TabExecutor {
    public static ArrayList<String> purseTop  = new ArrayList<>();
    public static ArrayList<String> bankTop = new ArrayList<>();
    public static ArrayList<String> sharedBankTop = new ArrayList<>();
    public static ArrayList<String> hiddenPlayers;

    private static ArrayList<String> togglePlayers = new ArrayList<>();

    private static boolean purseBroadcast;
    private static boolean bankBroadcast;
    private static boolean sharedbankBroadcast;


    public BalanceTopCommand() {
        //RoyaleEconomy.plugin.getCommand("balancetop").setExecutor(this);
        //RoyaleEconomy.plugin.getCommand("balancetop").setTabCompleter(this);
    }

    public void extraSetup(){
        hiddenPlayers=(ArrayList<String>)RoyaleEconomy.commandsCfg.getStringList("commands.balancetop.hidden-players");
        purseBroadcast=RoyaleEconomy.commandsCfg.getBoolean("commands.balancetop.broadcast-top-changes.broadcast-purse");
        bankBroadcast=RoyaleEconomy.commandsCfg.getBoolean("commands.balancetop.broadcast-top-changes.broadcast-bank");
        sharedbankBroadcast=RoyaleEconomy.commandsCfg.getBoolean("commands.balancetop.broadcast-top-changes.broadcast-sharedbank");
        if(purseBroadcast || bankBroadcast || sharedbankBroadcast){
            RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runTimerAsync(() -> {
                ArrayList<String> topTenPurse=new ArrayList<>();
                ArrayList<String> topTenBank=new ArrayList<>();
                ArrayList<String> topTenSharedBank=new ArrayList<>();
                if(!purseTop.isEmpty()){
                    if(purseBroadcast) {
                        try {
                            for (int i = 0; i < 20; i++) {
                                topTenPurse.add(purseTop.get(i));
                            }
                        } catch (Exception x) {

                        }
                    }
                    if(bankBroadcast) {
                        try {
                            for (int i = 0; i < 20; i++) {
                                topTenBank.add(bankTop.get(i));
                            }
                        } catch (Exception x) {

                        }
                    }
                    if(sharedbankBroadcast) {
                        try {
                            for (int i = 0; i < 20; i++) {
                                topTenSharedBank.add(sharedBankTop.get(i));
                            }
                        } catch (Exception x) {

                        }
                    }
                }
                RoyaleEconomy.dataManager.loadTops();

                RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runLater(() -> {
                    try {
                        ArrayList<String> changes = new ArrayList<>();
                        if (purseBroadcast) {
                            String structure = Utils.chat(RoyaleEconomy.commandsCfg.getString("commands.balancetop.broadcast-top-changes.broadcast-purse-structure"));
                            int position = 1;
                            for (int i = 0; i < topTenPurse.size(); i += 2) {
                                if (!topTenPurse.get(i).equalsIgnoreCase(purseTop.get(i)) && !purseTop.get(i+1).equalsIgnoreCase("0"))
                                    changes.add(structure
                                            .replace("%position%", String.valueOf(position))
                                            .replace("%from-player-name%", topTenPurse.get(i))
                                            .replace("%from-player-coins%", topTenPurse.get(i + 1))
                                            .replace("%to-player-name%", purseTop.get(i))
                                            .replace("%to-player-coins%", purseTop.get(i + 1))
                                    );
                                position++;
                            }
                        }

                        if (bankBroadcast) {
                            String structure = Utils.chat(RoyaleEconomy.commandsCfg.getString("commands.balancetop.broadcast-top-changes.broadcast-bank-structure"));
                            int position = 1;
                            for (int i = 0; i < topTenBank.size(); i += 2) {
                                if (!topTenBank.get(i).equalsIgnoreCase(bankTop.get(i)) && !bankTop.get(i+1).equalsIgnoreCase("0"))
                                    changes.add(structure
                                            .replace("%position%", String.valueOf(position))
                                            .replace("%from-player-name%", topTenBank.get(i))
                                            .replace("%from-player-coins%", topTenBank.get(i + 1))
                                            .replace("%to-player-name%", bankTop.get(i))
                                            .replace("%to-player-coins%", bankTop.get(i + 1))
                                    );
                                position++;
                            }
                        }

                        if (sharedbankBroadcast) {
                            String structure = Utils.chat(RoyaleEconomy.commandsCfg.getString("commands.balancetop.broadcast-top-changes.broadcast-sharedbank-structure"));
                            int position = 1;
                            for (int i = 0; i < topTenSharedBank.size(); i += 2) {
                                if (!topTenSharedBank.get(i).equalsIgnoreCase(sharedBankTop.get(i)) && !sharedBankTop.get(i+1).equalsIgnoreCase("0"))
                                    changes.add(structure
                                            .replace("%position%", String.valueOf(position))
                                            .replace("%from-player-name%", topTenSharedBank.get(i))
                                            .replace("%from-player-coins%", topTenSharedBank.get(i + 1))
                                            .replace("%to-player-name%", sharedBankTop.get(i))
                                            .replace("%to-player-coins%", sharedBankTop.get(i + 1))
                                    );
                                position++;
                            }
                        }

                        if (!changes.isEmpty()) {
                            ArrayList<Player> players = new ArrayList<>();
                            for (Player p : Bukkit.getOnlinePlayers())
                                if (!togglePlayers.contains(p.getName()))
                                    players.add(p);
                            for (String line : RoyaleEconomy.commandsCfg.getStringList("commands.balancetop.broadcast-top-changes.broadcast-header")) {
                                for (Player p : players) {
                                    PlayerMessageHandler.messageSend(p, Utils.chat(line));
                                }
                            }


                            for (String change : changes) {
                                for (Player p : players)
                                    PlayerMessageHandler.messageSend(p, change);
                            }

                            for (String line : RoyaleEconomy.commandsCfg.getStringList("commands.balancetop.broadcast-top-changes.broadcast-footer")) {
                                for (Player p : players) {
                                    PlayerMessageHandler.messageSend(p, Utils.chat(line));
                                }
                            }
                        }
                    } catch (Exception x) {
                        x.printStackTrace();
                    }
                }, 40);
            }, 0,6000);
        }
        else
            RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runTimerAsync(() -> RoyaleEconomy.dataManager.loadTops(), 0,6000);
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {
        if(args.length==1) {
            ArrayList<String> values=new ArrayList<>();
            if(PermissionChecker.checkPermissionSilent("commands.balancetop.top-bank", commandSender))
                values.add("bank");
            if(PermissionChecker.checkPermissionSilent("commands.balancetop.top-sharedbank", commandSender))
                values.add("sharedbank");
            if(PermissionChecker.checkPermissionSilent("commands.balancetop.top-purse", commandSender))
                values.add("purse");
            return StringUtil.copyPartialMatches(args[0], values, new ArrayList<>());
        }
        return null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            if(args.length>0){
                if(args[0].equalsIgnoreCase("bank")){
                    if(PermissionChecker.checkPermission("commands.balancetop.top-bank", p)) {
                        int page;
                        try {
                            page = Integer.parseInt(args[1]) - 1;
                        } catch (Exception x) {
                            page = 0;
                        }
                        int displayPerPage = RoyaleEconomy.staticValues.balanceTopDisplayPerPage * 2;
                        PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.balancetop.top-format.bank-top.header").replace("%page-number%", String.valueOf(page + 1))));
                        try {
                            String structure = RoyaleEconomy.commandsCfg.getString("commands.balancetop.top-format.bank-top.top-player-structure");
                            int finalI = displayPerPage * (page + 1);
                            for (int i = displayPerPage * page; i < finalI; i += 2) {
                                PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, structure.replace("%top-position%", String.valueOf(i / 2 + 1)).replace("%player-name%", bankTop.get(i+1)).replace("%coins%", bankTop.get(i))));
                            }
                        } catch (Exception x) {

                        }
                        PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.balancetop.top-format.bank-top.footer").replace("%page-number%", String.valueOf(page + 1))));
                        Utils.playSound(p, "commands.balancetop.top-bank");
                    }
                    return true;
                }
                else if(args[0].equalsIgnoreCase("sharedbank")){
                    if(PermissionChecker.checkPermission("commands.balancetop.top-sharedbank", p)) {
                        int page;
                        try {
                            page = Integer.parseInt(args[1]) - 1;
                        } catch (Exception x) {
                            page = 0;
                        }
                        int displayPerPage = RoyaleEconomy.staticValues.balanceTopDisplayPerPage * 2;
                        PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.balancetop.top-format.sharedbank-top.header").replace("%page-number%", String.valueOf(page + 1))));
                        try {
                            String structure = RoyaleEconomy.commandsCfg.getString("commands.balancetop.top-format.sharedbank-top.top-player-structure");
                            int finalI = displayPerPage * (page + 1);
                            for (int i = displayPerPage * page; i < finalI; i += 2) {
                                PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, structure.replace("%top-position%", String.valueOf(i / 2 + 1)).replace("%player-name%", sharedBankTop.get(i+1)).replace("%coins%", sharedBankTop.get(i))));
                            }
                        } catch (Exception x) {

                        }
                        PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.balancetop.top-format.sharedbank-top.footer").replace("%page-number%", String.valueOf(page + 1))));
                        Utils.playSound(p, "commands.balancetop.top-sharedbank");
                    }
                    return true;
                }
                else if(args[0].equalsIgnoreCase("purse") || args[0].equalsIgnoreCase("wallet") || args[0].equalsIgnoreCase("balance") || args[0].equalsIgnoreCase("bal") || args[0].equalsIgnoreCase("money")){
                    if(PermissionChecker.checkPermission("commands.balancetop.top-purse", p)) {
                        int page;
                        try {
                            page = Integer.parseInt(args[1]) - 1;
                        } catch (Exception x) {
                            page = 0;
                        }
                        int displayPerPage = RoyaleEconomy.staticValues.balanceTopDisplayPerPage * 2;
                        PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.balancetop.top-format.purse-top.header").replace("%page-number%", String.valueOf(page + 1))));
                        try {
                            String structure = RoyaleEconomy.commandsCfg.getString("commands.balancetop.top-format.purse-top.top-player-structure");
                            int finalI = displayPerPage * (page + 1);
                            for (int i = displayPerPage * page; i < finalI; i += 2) {
                                PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, structure.replace("%top-position%", String.valueOf(i / 2 + 1)).replace("%player-name%", purseTop.get(i+1)).replace("%coins%", purseTop.get(i))));
                            }
                        } catch (Exception x) {
                            //x.printStackTrace();
                        }
                        PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.balancetop.top-format.purse-top.footer").replace("%page-number%", String.valueOf(page + 1))));
                        Utils.playSound(p, "commands.balancetop.top-purse");
                    }
                    return true;
                }
                else if(args[0].equalsIgnoreCase("refresh")){
                    if(p.hasPermission("rec.balancetop.refresh")){
                        RoyaleEconomy.dataManager.loadTops();
                        PlayerMessageHandler.messageSend(p, Utils.chat("&aTops refreshed!"));
                    }
                    else{
                        for (String line : RoyaleEconomy.permissionsCfg.getStringList("no-permission-message"))
                            PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, line.replace("%permission%", "rec.balancetop.refresh")));
                    }
                    return true;
                }
                else if(args[0].equalsIgnoreCase("toggleBroadcast") && (purseBroadcast || bankBroadcast || sharedbankBroadcast)){
                    if(togglePlayers.contains(p.getName())){
                        togglePlayers.remove(p.getName());
                        PlayerMessageHandler.messageSend(p, Utils.chat(RoyaleEconomy.commandsCfg.getString("commands.balancetop.broadcast-top-changes.toggle-enabled")));
                    }
                    else{
                        togglePlayers.add(p.getName());
                        PlayerMessageHandler.messageSend(p, Utils.chat(RoyaleEconomy.commandsCfg.getString("commands.balancetop.broadcast-top-changes.toggle-disabled")));
                    }
                    return true;
                }
            }
            if(PermissionChecker.checkPermission("commands.balancetop.command-help", p)) {
                Utils.playSound(p, "commands.balancetop.command-help");
                for (String line : RoyaleEconomy.commandsCfg.getStringList("commands.balancetop.command-help"))
                    PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, line));
            }
        }
        return true;
    }
}
