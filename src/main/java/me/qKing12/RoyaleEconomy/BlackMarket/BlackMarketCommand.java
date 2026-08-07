package me.qKing12.RoyaleEconomy.BlackMarket;

import me.qKing12.RoyaleEconomy.Commands.dynamic.DynamicCommandsSetup;
import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import me.qKing12.RoyaleEconomy.utils.PlayerMessageHandler;
import me.qKing12.RoyaleEconomy.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BlackMarketCommand implements TabExecutor {

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {
        ArrayList<String> values=new ArrayList<>();
        if(args.length==1) {
            values.addAll(Arrays.asList("", "refresh"));
            if(commandSender.hasPermission(BlackMarket.adminPermission))
                values.addAll(Arrays.asList("itemManager", "open", "forceOpen", "reStock", "reloadRarities"));
        }
        return StringUtil.copyPartialMatches(args[0], values, new ArrayList<>()).isEmpty()?null:values;
    }

    public BlackMarketCommand(){
        //RoyaleEconomy.plugin.getCommand("blackmarket").setExecutor(this);
        //RoyaleEconomy.plugin.getCommand("blackmarket").setTabCompleter(this);

        DynamicCommandsSetup.getCommandSettings().put("black-market", this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if(sender instanceof Player){
            Player player=(Player)sender;
            if(BlackMarket.permission.equals("none") || player.hasPermission(BlackMarket.permission)) {
                if (args.length > 0) {
                    if(args[0].equalsIgnoreCase("refresh")){
                        PlayerMessageHandler.messageSend(player, BlackMarket.refreshCooldownMessage.replace("%cooldown%", RoyaleEconomy.messageHelper.formatTimeDetailed((BlackMarket.date-ZonedDateTime.now().toEpochSecond())*1000)));
                    }
                    else if(player.hasPermission(BlackMarket.adminPermission)){
                        if(args[0].equalsIgnoreCase("open")){
                            if(args.length>1){
                                Player target= Bukkit.getPlayerExact(args[1]);
                                if(target!=null){
                                    if(BlackMarket.permission.equals("none") || target.hasPermission(BlackMarket.permission)){
                                        if(BlackMarket.limitedAccess && BlackMarket.accessDate<= ZonedDateTime.now().toEpochSecond()){
                                            PlayerMessageHandler.messageSend(target, BlackMarket.refreshCooldownMessage.replace("%cooldown%", RoyaleEconomy.messageHelper.formatTimeDetailed((BlackMarket.date-ZonedDateTime.now().toEpochSecond())*1000)));
                                            player.sendMessage(Utils.chat("&cOpen failed. Target doesn't meet the requirements."));
                                        }
                                        else{
                                            new BlackMarketMenu(target);
                                        }
                                    }
                                    else
                                        player.sendMessage(Utils.chat("&cOpen failed. Target doesn't meet the requirements."));
                                }
                                else
                                    player.sendMessage(Utils.chat("&cPlayer not found."));
                            }
                            else
                                player.sendMessage(Utils.chat("&cNo player specified."));
                        }
                        else if(args[0].equalsIgnoreCase("forceOpen")){
                            if(args.length>1){
                                Player target= Bukkit.getPlayerExact(args[1]);
                                if(target!=null){
                                    new BlackMarketMenu(target);
                                    player.sendMessage(Utils.chat("&aDone!"));
                                }
                                else
                                    player.sendMessage(Utils.chat("&cPlayer not found."));
                            }
                            else
                                player.sendMessage(Utils.chat("&cNo player specified."));
                        }
                        else if(args[0].equalsIgnoreCase("itemManager")){
                            new BlackMarketItemsManagerMenu(player, 0);
                        }
                        else if(args[0].equalsIgnoreCase("restock")){
                            BlackMarket.triggerRefresh();
                            player.sendMessage(Utils.chat("&aThe black market items have been refreshed!"));

                        }
                        else if(args[0].equalsIgnoreCase("reloadRarities")){
                            Rarity.loadRarities();
                            player.sendMessage(Utils.chat("&aBlack market rarities reloaded!"));
                        }
                        else {
                            player.sendMessage(Utils.chat("&a/blackmarket"));
                            player.sendMessage(Utils.chat("&a/blackmarket refresh"));
                            player.sendMessage(Utils.chat("&a/blackmarket itemManager &7- &fManages black market items"));
                            player.sendMessage(Utils.chat("&a/blackmarket open <player> &7- &fAttempts to open the market for a player"));
                            player.sendMessage(Utils.chat("&a/blackmarket forceOpen <player> &7- &fForce opens the market, with out any permission check"));
                            player.sendMessage(Utils.chat("&a/blackmarket reStock &7- &fSpeeds the restock, timer will get reset too"));
                            player.sendMessage(Utils.chat("&a/blackmarket reloadRarities &7- &fReloads most of the black market rarities"));
                        }
                    }
                    else{
                        player.sendMessage(Utils.chat("&a/blackmarket"));
                        player.sendMessage(Utils.chat("&a/blackmarket refresh"));
                        if(player.hasPermission(BlackMarket.adminPermission)) {
                            player.sendMessage(Utils.chat("&a/blackmarket itemManager &7- &fManages black market items"));
                            player.sendMessage(Utils.chat("&a/blackmarket open <player> &7- &fAttempts to open the market for a player"));
                            player.sendMessage(Utils.chat("&a/blackmarket forceOpen <player> &7- &fForce opens the market, with out any permission check"));
                            player.sendMessage(Utils.chat("&a/blackmarket reStock &7- &fSpeeds the restock, timer will get reset too"));
                            player.sendMessage(Utils.chat("&a/blackmarket reloadRarities &7- &fReloads the black market rarities"));
                        }
                    }
                } else {
                    if(BlackMarket.limitedAccess && BlackMarket.accessDate<= ZonedDateTime.now().toEpochSecond()){
                        PlayerMessageHandler.messageSend(player, BlackMarket.refreshCooldownMessage.replace("%cooldown%", RoyaleEconomy.messageHelper.formatTimeDetailed((BlackMarket.date-ZonedDateTime.now().toEpochSecond())*1000)));
                    }
                    else{
                        new BlackMarketMenu(player);
                    }
                }
            }else
                PlayerMessageHandler.messageSend(player, BlackMarket.noPermission);
        }
        else if(sender instanceof ConsoleCommandSender){
            ConsoleCommandSender player=(ConsoleCommandSender)sender;
            if(args.length>0){
                if(args[0].equalsIgnoreCase("open")){
                    if(args.length>1){
                        Player target= Bukkit.getPlayerExact(args[1]);
                        if(target!=null){
                            if(BlackMarket.permission.equals("none") || target.hasPermission(BlackMarket.permission)){
                                if(BlackMarket.limitedAccess && BlackMarket.accessDate<= ZonedDateTime.now().toEpochSecond()){
                                    PlayerMessageHandler.messageSend(target, BlackMarket.refreshCooldownMessage.replace("%cooldown%", RoyaleEconomy.messageHelper.formatTimeDetailed((BlackMarket.date-ZonedDateTime.now().toEpochSecond())*1000)));
                                }
                                else{
                                    new BlackMarketMenu(target);
                                }
                            }
                            else
                                player.sendMessage(Utils.chat("&cOpen failed. Target doesn't meet the requirements."));
                        }
                        else
                            player.sendMessage(Utils.chat("&cPlayer not found."));
                    }
                    else
                        player.sendMessage(Utils.chat("&cNo player specified."));
                }
                else if(args[0].equalsIgnoreCase("forceOpen")){
                    if(args.length>1){
                        Player target= Bukkit.getPlayerExact(args[1]);
                        if(target!=null){
                            new BlackMarketMenu(target);
                        }
                        else
                            player.sendMessage(Utils.chat("&cPlayer not found."));
                    }
                    else
                        player.sendMessage(Utils.chat("&cNo player specified."));
                }
                else if(args[0].equalsIgnoreCase("restock")){
                    BlackMarket.triggerRefresh();
                    player.sendMessage(Utils.chat("&aThe black market items have been refreshed!"));

                }
                else if(args[0].equalsIgnoreCase("reload")){
                    new BlackMarket();
                    player.sendMessage(Utils.chat("&aBlack market config reloaded!"));
                }
                else{
                    player.sendMessage(Utils.chat("&a/blackmarket refresh"));
                    player.sendMessage(Utils.chat("&a/blackmarket open <player> &7- &fAttempts to open the market for a player"));
                    player.sendMessage(Utils.chat("&a/blackmarket forceOpen <player> &7- &fForce opens the market, with out any permission check"));
                    player.sendMessage(Utils.chat("&a/blackmarket reStock &7- &fSpeeds the restock, timer will get reset too"));
                    player.sendMessage(Utils.chat("&a/blackmarket reload &7- &fReloads most of the black market config"));
                }
            }
            else {
                player.sendMessage(Utils.chat("&a/blackmarket refresh"));
                player.sendMessage(Utils.chat("&a/blackmarket open <player> &7- &fAttempts to open the market for a player"));
                player.sendMessage(Utils.chat("&a/blackmarket forceOpen <player> &7- &fForce opens the market, with out any permission check"));
                player.sendMessage(Utils.chat("&a/blackmarket reStock &7- &fSpeeds the restock, timer will get reset too"));
                player.sendMessage(Utils.chat("&a/blackmarket reload &7- &fReloads most of the black market config"));
            }
        }

        return false;
    }
}
