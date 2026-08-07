package me.qKing12.RoyaleEconomy.Boosters;

import me.qKing12.RoyaleEconomy.Commands.dynamic.DynamicCommandsSetup;
import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import me.qKing12.RoyaleEconomy.utils.PlayerMessageHandler;
import me.qKing12.RoyaleEconomy.utils.Utf8YamlConfiguration;
import me.qKing12.RoyaleEconomy.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.io.File;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BoosterAdmin implements TabExecutor {

    public BoosterAdmin(){
        //RoyaleEconomy.plugin.getCommand("boosteradmin").setExecutor(this);
        //RoyaleEconomy.plugin.getCommand("boosteradmin").setTabCompleter(this);
        DynamicCommandsSetup.getCommandSettings().put("boosters-admin", this);
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {
        if(commandSender.hasPermission(RoyaleEconomy.boostersCfg.getString("booster-admin-command-permission"))) {
            if (args.length == 1)
                return new ArrayList<>(Arrays.asList("reload", "check", "shopBuy", "shopSell", "killCoins"));
            else if (args.length == 2) {
                if (args[0].equalsIgnoreCase("shopBuy") || args[0].equalsIgnoreCase("shopSell") || args[0].equalsIgnoreCase("killCoins")) {
                    return new ArrayList<>(Arrays.asList("global", "player", "list"));
                }
            } else if (args.length == 3) {
                if (args[0].equalsIgnoreCase("shopBuy") || args[0].equalsIgnoreCase("shopSell") || args[0].equalsIgnoreCase("killCoins")) {
                    return new ArrayList<>(Arrays.asList("remove", "add"));
                }
            }
        }
        return null;
    }

    @Override
    public boolean onCommand(CommandSender p, Command command, String s, String[] args) {
        if(p.hasPermission(RoyaleEconomy.boostersCfg.getString("booster-admin-command-permission")) || p instanceof ConsoleCommandSender){
            if(args.length>0){
                if(args[0].equalsIgnoreCase("reload")){
                    RoyaleEconomy.boostersCfg= Utf8YamlConfiguration.loadConfiguration(new File(RoyaleEconomy.plugin.getDataFolder(), "boosters.yml"));
                    RoyaleEconomy.boosters=null;
                    RoyaleEconomy.plugin.setupBoosters();
                    PlayerMessageHandler.messageSend(p, Utils.chat("&aBoosters reloaded!"));
                    return false;
                }
                else if(BoostersActive.shopBuyCategoryName!=null && args[0].equalsIgnoreCase("shopBuy")){
                    if(args.length>1){
                        if(args[1].equalsIgnoreCase("list")){
                            for(BoostersActive.Booster booster : RoyaleEconomy.boosters.getBoostersShopBuy()){
                                PlayerMessageHandler.messageSend(p, Utils.chat("&7&m--------------------"));
                                PlayerMessageHandler.messageSend(p, Utils.chat("&fBooster ID: &e"+booster.boosterKey));
                                PlayerMessageHandler.messageSend(p, Utils.chat("&fBooster Name: ")+booster.boosterName);
                                PlayerMessageHandler.messageSend(p, Utils.chat("&fBooster Percent: &e")+booster.percent);
                                if(booster.permission!=null)
                                    PlayerMessageHandler.messageSend(p, Utils.chat("&fBooster Permission: &e")+booster.permission);
                                if(booster.permissionPermanent!=null)
                                    PlayerMessageHandler.messageSend(p, Utils.chat("&fBooster Permanent Permission: &e")+booster.permissionPermanent);
                                if(booster.shops!=null) {
                                    PlayerMessageHandler.messageSend(p, Utils.chat("&fBooster Exclusive Shops:"));
                                    for(String shopName : booster.shops)
                                        PlayerMessageHandler.messageSend(p, Utils.chat(" &e- "+shopName));
                                }
                                PlayerMessageHandler.messageSend(p, Utils.chat("&7&m--------------------"));
                            }
                            return false;
                        }
                        else if(args[1].equalsIgnoreCase("global")){
                            if(args.length>3){
                                if(args[2].equalsIgnoreCase("add")) {
                                    try {
                                        int minutes = Integer.parseInt(args[4]);
                                        BoostersActive boosters = (BoostersActive) RoyaleEconomy.boosters;
                                        BoostersActive.Booster booster = boosters.getShopBuyBoosterByKey(args[3]);
                                        boosters.shopBuyActiveBoosters.put(booster, ZonedDateTime.now().toInstant().toEpochMilli() + minutes * 60000);
                                        String toBroadcast=RoyaleEconomy.boostersCfg.getString("global-booster-broadcast");
                                        for(Player p2 : Bukkit.getOnlinePlayers()){
                                            PlayerMessageHandler.messageSend(p2, Utils.chat(toBroadcast
                                                    .replace("%booster-name%", booster.boosterName)
                                                    .replace("%booster-category%", BoostersActive.shopBuyCategoryName)
                                                    .replace("%booster-duration%", RoyaleEconomy.messageHelper.formatTimeShort(minutes * 60000L))
                                            ));
                                        }
                                        Bukkit.getConsoleSender().sendMessage(Utils.chat(toBroadcast
                                                .replace("%booster-name%", booster.boosterName)
                                                .replace("%booster-category%", BoostersActive.shopBuyCategoryName)
                                                .replace("%booster-duration%", RoyaleEconomy.messageHelper.formatTimeShort(minutes * 60000L))
                                        ));
                                        /*Bukkit.broadcastMessage(Utils.chat(RoyaleEconomy.boostersCfg.getString("global-booster-broadcast")
                                                .replace("%booster-name%", booster.boosterName)
                                                .replace("%booster-category%", BoostersActive.shopBuyCategoryName)
                                                .replace("%booster-duration%", RoyaleEconomy.messageHelper.formatTimeShort(minutes * 60000L))
                                        ));*/
                                    } catch (Exception x) {
                                        PlayerMessageHandler.messageSend(p, Utils.chat("&cInvalid minutes number."));
                                    }
                                    return false;
                                }
                                else if(args[2].equalsIgnoreCase("remove")){
                                    BoostersActive boosters = (BoostersActive) RoyaleEconomy.boosters;
                                    BoostersActive.Booster booster = boosters.getShopBuyBoosterByKey(args[3]);
                                    boosters.shopBuyActiveBoosters.remove(booster);
                                    PlayerMessageHandler.messageSend(p, Utils.chat("&aIf booster was activated it is now inactive!"));
                                    return false;
                                }
                            }
                        }
                        else if(args[1].equalsIgnoreCase("player")){
                            if(args.length>4){
                                Player p2 = Bukkit.getPlayer(args[3]);
                                if(p2!=null) {
                                    if (args[2].equalsIgnoreCase("add")) {
                                        try {
                                            int minutes = Integer.parseInt(args[5]);
                                            BoostersActive boosters = (BoostersActive) RoyaleEconomy.boosters;
                                            BoostersActive.Booster booster = boosters.getShopBuyBoosterByKey(args[4]);
                                            booster.addPlayerToBooster(p2, minutes);
                                            PlayerMessageHandler.messageSend(p2, Utils.chat(RoyaleEconomy.boostersCfg.getString("personal-booster-broadcast")
                                                    .replace("%booster-name%", booster.boosterName)
                                                    .replace("%booster-category%", BoostersActive.shopBuyCategoryName)
                                                    .replace("%booster-duration%", RoyaleEconomy.messageHelper.formatTimeShort(minutes * 60000L))
                                            ));
                                        } catch (Exception x) {
                                            PlayerMessageHandler.messageSend(p, Utils.chat("&cInvalid minutes number."));
                                        }
                                        return false;
                                    } else if (args[2].equalsIgnoreCase("remove")) {
                                        BoostersActive boosters = (BoostersActive) RoyaleEconomy.boosters;
                                        BoostersActive.Booster booster = boosters.getShopBuyBoosterByKey(args[4]);
                                        booster.removePlayerBooster(p2);
                                        PlayerMessageHandler.messageSend(p, Utils.chat("&aIf booster was activated it is now inactive!"));
                                        return false;
                                    }
                                }
                                else {
                                    PlayerMessageHandler.messageSend(p, Utils.chat("&cPlayer not found."));
                                    return false;
                                }
                            }
                        }
                    }
                    PlayerMessageHandler.messageSend(p, Utils.chat("&e/boosteradmin shopBuy list &7- &fBoosters List"));
                    PlayerMessageHandler.messageSend(p, Utils.chat("&e/boosteradmin shopBuy global add <booster-id> <minutes number>"));
                    PlayerMessageHandler.messageSend(p, Utils.chat("&e/boosteradmin shopBuy global remove <booster-id>"));
                    PlayerMessageHandler.messageSend(p, Utils.chat("&e/boosteradmin shopBuy player add <player> <booster-id> <minutes number>"));
                    PlayerMessageHandler.messageSend(p, Utils.chat("&e/boosteradmin shopBuy player remove <player> <booster-id>"));
                    return false;
                }
                else if(BoostersActive.shopSellCategoryName!=null && args[0].equalsIgnoreCase("shopSell")){
                    if(args.length>1){
                        if(args[1].equalsIgnoreCase("list")){
                            for(BoostersActive.Booster booster : RoyaleEconomy.boosters.getBoostersShopSell()){
                                PlayerMessageHandler.messageSend(p, Utils.chat("&7&m--------------------"));
                                PlayerMessageHandler.messageSend(p, Utils.chat("&fBooster ID: &e"+booster.boosterKey));
                                PlayerMessageHandler.messageSend(p, Utils.chat("&fBooster Name: ")+booster.boosterName);
                                PlayerMessageHandler.messageSend(p, Utils.chat("&fBooster Percent: &e")+booster.percent);
                                if(booster.permission!=null)
                                    PlayerMessageHandler.messageSend(p, Utils.chat("&fBooster Permission: &e")+booster.permission);
                                if(booster.permissionPermanent!=null)
                                    PlayerMessageHandler.messageSend(p, Utils.chat("&fBooster Permanent Permission: &e")+booster.permissionPermanent);
                                if(booster.shops!=null) {
                                    PlayerMessageHandler.messageSend(p, Utils.chat("&fBooster Exclusive Shops:"));
                                    for(String shopName : booster.shops)
                                        PlayerMessageHandler.messageSend(p, Utils.chat(" &e- "+shopName));
                                }
                                PlayerMessageHandler.messageSend(p, Utils.chat("&7&m--------------------"));
                            }
                            return false;
                        }
                        else if(args[1].equalsIgnoreCase("global")){
                            if(args.length>3){
                                if(args[2].equalsIgnoreCase("add")) {
                                    try {
                                        int minutes = Integer.parseInt(args[4]);
                                        BoostersActive boosters = (BoostersActive) RoyaleEconomy.boosters;
                                        BoostersActive.Booster booster = boosters.getShopSellBoosterByKey(args[3]);
                                        boosters.shopSellActiveBoosters.put(booster, ZonedDateTime.now().toInstant().toEpochMilli() + minutes * 60000);
                                        String toBroadcast=RoyaleEconomy.boostersCfg.getString("global-booster-broadcast");
                                        for(Player p2 : Bukkit.getOnlinePlayers()){
                                            PlayerMessageHandler.messageSend(p2, Utils.chat(toBroadcast
                                                    .replace("%booster-name%", booster.boosterName)
                                                    .replace("%booster-category%", BoostersActive.shopSellCategoryName)
                                                    .replace("%booster-duration%", RoyaleEconomy.messageHelper.formatTimeShort(minutes * 60000L))
                                            ));
                                        }
                                        Bukkit.getConsoleSender().sendMessage(Utils.chat(toBroadcast
                                                .replace("%booster-name%", booster.boosterName)
                                                .replace("%booster-category%", BoostersActive.shopSellCategoryName)
                                                .replace("%booster-duration%", RoyaleEconomy.messageHelper.formatTimeShort(minutes * 60000L))
                                        ));
                                        /*Bukkit.broadcastMessage(Utils.chat(RoyaleEconomy.boostersCfg.getString("global-booster-broadcast")
                                                .replace("%booster-name%", booster.boosterName)
                                                .replace("%booster-category%", BoostersActive.shopSellCategoryName)
                                                .replace("%booster-duration%", RoyaleEconomy.messageHelper.formatTimeShort(minutes * 60000L))
                                        ));*/
                                    } catch (Exception x) {
                                        PlayerMessageHandler.messageSend(p, Utils.chat("&cInvalid minutes number."));
                                    }
                                    return false;
                                }
                                else if(args[2].equalsIgnoreCase("remove")){
                                    BoostersActive boosters = (BoostersActive) RoyaleEconomy.boosters;
                                    BoostersActive.Booster booster = boosters.getShopSellBoosterByKey(args[3]);
                                    boosters.shopSellActiveBoosters.remove(booster);
                                    PlayerMessageHandler.messageSend(p, Utils.chat("&aIf booster was activated it is now inactive!"));
                                    return false;
                                }
                            }
                        }
                        else if(args[1].equalsIgnoreCase("player")){
                            if(args.length>4){
                                Player p2 = Bukkit.getPlayer(args[3]);
                                if(p2!=null) {
                                    if (args[2].equalsIgnoreCase("add")) {
                                        try {
                                            int minutes = Integer.parseInt(args[5]);
                                            BoostersActive boosters = (BoostersActive) RoyaleEconomy.boosters;
                                            BoostersActive.Booster booster = boosters.getShopSellBoosterByKey(args[4]);
                                            booster.addPlayerToBooster(p2, minutes);
                                            PlayerMessageHandler.messageSend(p2, Utils.chat(RoyaleEconomy.boostersCfg.getString("personal-booster-broadcast")
                                                    .replace("%booster-name%", booster.boosterName)
                                                    .replace("%booster-category%", BoostersActive.shopSellCategoryName)
                                                    .replace("%booster-duration%", RoyaleEconomy.messageHelper.formatTimeShort(minutes * 60000L))
                                            ));
                                        } catch (Exception x) {
                                            PlayerMessageHandler.messageSend(p, Utils.chat("&cInvalid minutes number."));
                                        }
                                        return false;
                                    } else if (args[2].equalsIgnoreCase("remove")) {
                                        BoostersActive boosters = (BoostersActive) RoyaleEconomy.boosters;
                                        BoostersActive.Booster booster = boosters.getShopSellBoosterByKey(args[4]);
                                        booster.removePlayerBooster(p2);
                                        PlayerMessageHandler.messageSend(p, Utils.chat("&aIf booster was activated it is now inactive!"));
                                        return false;
                                    }
                                }
                                else {
                                    PlayerMessageHandler.messageSend(p, Utils.chat("&cPlayer not found."));
                                    return false;
                                }
                            }
                        }
                    }
                    PlayerMessageHandler.messageSend(p, Utils.chat("&e/boosteradmin shopSell list &7- &fBoosters List"));
                    PlayerMessageHandler.messageSend(p, Utils.chat("&e/boosteradmin shopSell global add <booster-id> <minutes number>"));
                    PlayerMessageHandler.messageSend(p, Utils.chat("&e/boosteradmin shopSell global remove <booster-id>"));
                    PlayerMessageHandler.messageSend(p, Utils.chat("&e/boosteradmin shopSell player add <player> <booster-id> <minutes number>"));
                    PlayerMessageHandler.messageSend(p, Utils.chat("&e/boosteradmin shopSell player remove <player> <booster-id>"));
                    return false;
                }
                else if(BoostersActive.killCoinsCategoryName!=null && args[0].equalsIgnoreCase("killCoins")){
                    if(args.length>1){
                        if(args[1].equalsIgnoreCase("list")){
                            for(BoostersActive.Booster booster : RoyaleEconomy.boosters.getBoostersKillCoins()){
                                PlayerMessageHandler.messageSend(p, Utils.chat("&7&m--------------------"));
                                PlayerMessageHandler.messageSend(p, Utils.chat("&fBooster ID: &e"+booster.boosterKey));
                                PlayerMessageHandler.messageSend(p, Utils.chat("&fBooster Name: ")+booster.boosterName);
                                PlayerMessageHandler.messageSend(p, Utils.chat("&fBooster Percent: &e")+booster.percent);
                                if(booster.permission!=null)
                                    PlayerMessageHandler.messageSend(p, Utils.chat("&fBooster Permission: &e")+booster.permission);
                                if(booster.permissionPermanent!=null)
                                    PlayerMessageHandler.messageSend(p, Utils.chat("&fBooster Permanent Permission: &e")+booster.permissionPermanent);
                                PlayerMessageHandler.messageSend(p, Utils.chat("&7&m--------------------"));
                            }
                            return false;
                        }
                        else if(args[1].equalsIgnoreCase("global")){
                            if(args.length>3){
                                if(args[2].equalsIgnoreCase("add")) {
                                    try {
                                        int minutes = Integer.parseInt(args[4]);
                                        BoostersActive boosters = (BoostersActive) RoyaleEconomy.boosters;
                                        BoostersActive.Booster booster = boosters.getKillCoinsBoosterByKey(args[3]);
                                        boosters.killCoinsActiveBoosters.put(booster, ZonedDateTime.now().toInstant().toEpochMilli() + minutes * 60000);
                                        String toBroadcast=RoyaleEconomy.boostersCfg.getString("global-booster-broadcast");
                                        for(Player p2 : Bukkit.getOnlinePlayers()){
                                            PlayerMessageHandler.messageSend(p2, Utils.chat(toBroadcast
                                                    .replace("%booster-name%", booster.boosterName)
                                                    .replace("%booster-category%", BoostersActive.killCoinsCategoryName)
                                                    .replace("%booster-duration%", RoyaleEconomy.messageHelper.formatTimeShort(minutes * 60000L))
                                            ));
                                        }
                                        Bukkit.getConsoleSender().sendMessage(Utils.chat(toBroadcast
                                                .replace("%booster-name%", booster.boosterName)
                                                .replace("%booster-category%", BoostersActive.killCoinsCategoryName)
                                                .replace("%booster-duration%", RoyaleEconomy.messageHelper.formatTimeShort(minutes * 60000L))
                                        ));
                                        /*Bukkit.broadcastMessage(Utils.chat(RoyaleEconomy.boostersCfg.getString("global-booster-broadcast")
                                                .replace("%booster-name%", booster.boosterName)
                                                .replace("%booster-category%", BoostersActive.killCoinsCategoryName)
                                                .replace("%booster-duration%", RoyaleEconomy.messageHelper.formatTimeShort(minutes * 60000L))
                                        ));*/
                                    } catch (Exception x) {
                                        PlayerMessageHandler.messageSend(p, Utils.chat("&cInvalid minutes number."));
                                    }
                                    return false;
                                }
                                else if(args[2].equalsIgnoreCase("remove")){
                                    BoostersActive boosters = (BoostersActive) RoyaleEconomy.boosters;
                                    BoostersActive.Booster booster = boosters.getKillCoinsBoosterByKey(args[3]);
                                    boosters.killCoinsActiveBoosters.remove(booster);
                                    PlayerMessageHandler.messageSend(p, Utils.chat("&aIf booster was activated it is now inactive!"));
                                    return false;
                                }
                            }
                        }
                        else if(args[1].equalsIgnoreCase("player")){
                            if(args.length>4){
                                Player p2 = Bukkit.getPlayer(args[3]);
                                if(p2!=null) {
                                    if (args[2].equalsIgnoreCase("add")) {
                                        try {
                                            int minutes = Integer.parseInt(args[5]);
                                            BoostersActive boosters = (BoostersActive) RoyaleEconomy.boosters;
                                            BoostersActive.Booster booster = boosters.getKillCoinsBoosterByKey(args[4]);
                                            booster.addPlayerToBooster(p2, minutes);
                                            PlayerMessageHandler.messageSend(p2, Utils.chat(RoyaleEconomy.boostersCfg.getString("personal-booster-broadcast")
                                                    .replace("%booster-name%", booster.boosterName)
                                                    .replace("%booster-category%", BoostersActive.killCoinsCategoryName)
                                                    .replace("%booster-duration%", RoyaleEconomy.messageHelper.formatTimeShort(minutes * 60000L))
                                            ));
                                        } catch (Exception x) {
                                            PlayerMessageHandler.messageSend(p, Utils.chat("&cInvalid minutes number."));
                                        }
                                        return false;
                                    } else if (args[2].equalsIgnoreCase("remove")) {
                                        BoostersActive boosters = (BoostersActive) RoyaleEconomy.boosters;
                                        BoostersActive.Booster booster = boosters.getKillCoinsBoosterByKey(args[4]);
                                        booster.removePlayerBooster(p2);
                                        PlayerMessageHandler.messageSend(p, Utils.chat("&aIf booster was activated it is now inactive!"));
                                        return false;
                                    }
                                }
                                else {
                                    PlayerMessageHandler.messageSend(p, Utils.chat("&cPlayer not found."));
                                    return false;
                                }
                            }
                        }
                    }
                    PlayerMessageHandler.messageSend(p, Utils.chat("&e/boosteradmin killCoins list &7- &fBoosters List"));
                    PlayerMessageHandler.messageSend(p, Utils.chat("&e/boosteradmin killCoins global add <booster-id> <minutes number>"));
                    PlayerMessageHandler.messageSend(p, Utils.chat("&e/boosteradmin killCoins global remove <booster-id>"));
                    PlayerMessageHandler.messageSend(p, Utils.chat("&e/boosteradmin killCoins player add <player> <booster-id> <minutes number>"));
                    PlayerMessageHandler.messageSend(p, Utils.chat("&e/boosteradmin killCoins player remove <player> <booster-id>"));
                    return false;
                }
                else if(args.length>1){
                    if(args[0].equalsIgnoreCase("check")){
                        Player player = Bukkit.getPlayer(args[1]);
                        if(player==null){
                            PlayerMessageHandler.messageSend(p, Utils.chat("&cPlayer not found."));
                            return false;
                        }
                        else{
                            RoyaleEconomy.boosters.sendMessageForPlayer(p, player);
                            return false;
                        }
                    }
                }
            }
            PlayerMessageHandler.messageSend(p, Utils.chat("&e/boosteradmin reload"));
            PlayerMessageHandler.messageSend(p, Utils.chat("&e/boosteradmin check <player>"));
            if(BoostersActive.shopBuyCategoryName!=null)
                PlayerMessageHandler.messageSend(p, Utils.chat("&e/boosteradmin shopBuy"));
            if(BoostersActive.shopSellCategoryName!=null)
                PlayerMessageHandler.messageSend(p, Utils.chat("&e/boosteradmin shopSell"));
            if(BoostersActive.killCoinsCategoryName!=null)
                PlayerMessageHandler.messageSend(p, Utils.chat("&e/boosteradmin killCoins"));
        }
        return false;
    }
}
