package me.qKing12.RoyaleEconomy.TimeRewards;


import me.qKing12.RoyaleEconomy.Commands.dynamic.DynamicCommandsSetup;
import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import me.qKing12.RoyaleEconomy.utils.PlayerMessageHandler;
import me.qKing12.RoyaleEconomy.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TimeRewardCommand implements TabExecutor, Listener {

    public TimeRewardCommand(){
        Bukkit.getPluginManager().registerEvents(this, RoyaleEconomy.plugin);
        //RoyaleEconomy.plugin.getCommand("rerewards").setExecutor(this);
        DynamicCommandsSetup.getCommandSettings().put("time-rewards", this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if(sender instanceof Player){
            Player player=(Player)sender;
            String usePermission = RoyaleEconomy.plugin.timeRewardsManager.usePermission;
            if(usePermission.equals("none") || player.hasPermission(usePermission)) {
                if(args.length>0 && sender.isOp()) {
                    if (args[0].equalsIgnoreCase("forceOpen")) {
                        if (args.length > 1) {
                            Player target = Bukkit.getPlayerExact(args[1]);
                            if (target != null) {
                                new TimeRewardMenu(target);
                            } else
                                player.sendMessage(Utils.chat("&cPlayer not found."));
                        } else
                            player.sendMessage(Utils.chat("&cNo player specified."));
                    }
                    else if (args[0].equalsIgnoreCase("reset")) {
                        if (args.length > 2) {
                            Player target = Bukkit.getPlayerExact(args[1]);
                            if (target != null) {
                                TimeRewardPlayerData.TimeRewardData data = TimeRewardPlayerData.playerData.getOrDefault(target, null);
                                if(data!=null) {
                                    if(data.containsReward(args[2])) {
                                        data.setStreak(args[2], 0);
                                        data.saveToFile();
                                        player.sendMessage(Utils.chat("&aThe streak of the player was reset successfully!"));
                                    }
                                    else
                                        player.sendMessage(Utils.chat("&cThe specified reward does not exist!"));
                                }
                            } else
                                player.sendMessage(Utils.chat("&cPlayer not found."));
                        } else
                            player.sendMessage(Utils.chat("&cNo player and reward specified.\n/rerewards reset <player> <reward>"));
                    }
                    else if (args[0].equalsIgnoreCase("resetCooldown")) {
                        if (args.length > 2) {
                            Player target = Bukkit.getPlayerExact(args[1]);
                            if (target != null) {
                                TimeRewardPlayerData.TimeRewardData data = TimeRewardPlayerData.playerData.getOrDefault(target, null);
                                if(data!=null) {
                                    if(data.containsReward(args[2])) {
                                        data.removeCooldown(args[2]);
                                        data.saveToFile();
                                        player.sendMessage(Utils.chat("&aThe cooldown of the player for that reward was reset successfully!"));
                                    }
                                    else
                                        player.sendMessage(Utils.chat("&cThe specified reward does not exist!"));
                                }
                            } else
                                player.sendMessage(Utils.chat("&cPlayer not found."));
                        } else
                            player.sendMessage(Utils.chat("&cNo player and reward specified.\n/rerewards resetCooldown <player> <reward>"));
                    }
                    return false;
                }
                new TimeRewardMenu((Player)sender);
            }else
                PlayerMessageHandler.messageSend(player, RoyaleEconomy.plugin.timeRewardsManager.denyMessage);
        }
        else if(sender instanceof ConsoleCommandSender){
            ConsoleCommandSender player=(ConsoleCommandSender)sender;
            if(args.length>0) {
                if (args[0].equalsIgnoreCase("forceOpen")) {
                    if (args.length > 1) {
                        Player target = Bukkit.getPlayerExact(args[1]);
                        if (target != null) {
                            new TimeRewardMenu(target);
                        } else
                            player.sendMessage(Utils.chat("&cPlayer not found."));
                    } else
                        player.sendMessage(Utils.chat("&cNo player specified."));
                }
                else if (args[0].equalsIgnoreCase("reset")) {
                    if (args.length > 2) {
                        Player target = Bukkit.getPlayerExact(args[1]);
                        if (target != null) {
                            TimeRewardPlayerData.TimeRewardData data = TimeRewardPlayerData.playerData.getOrDefault(target, null);
                            if(data!=null) {
                                if(data.containsReward(args[2])) {
                                    data.setStreak(args[2], 0);
                                    data.saveToFile();
                                }
                                else
                                    player.sendMessage(Utils.chat("&cThe specified reward does not exist!"));
                            }
                        } else
                            player.sendMessage(Utils.chat("&cPlayer not found."));
                    } else
                        player.sendMessage(Utils.chat("&cNo player and reward specified.\n/rerewards reset <player> <reward>"));
                }
                else if (args[0].equalsIgnoreCase("resetCooldown")) {
                    if (args.length > 2) {
                        Player target = Bukkit.getPlayerExact(args[1]);
                        if (target != null) {
                            TimeRewardPlayerData.TimeRewardData data = TimeRewardPlayerData.playerData.getOrDefault(target, null);
                            if(data!=null) {
                                if(data.containsReward(args[2])) {
                                    data.removeCooldown(args[2]);
                                    data.saveToFile();
                                }
                                else
                                    player.sendMessage(Utils.chat("&cThe specified reward does not exist!"));
                            }
                        } else
                            player.sendMessage(Utils.chat("&cPlayer not found."));
                    } else
                        player.sendMessage(Utils.chat("&cNo player and reward specified.\n/rerewards resetCooldown <player> <reward>"));
                }
            }
        }
        return false;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return null;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e){
        new TimeRewardPlayerData.TimeRewardData(e.getPlayer());
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e){
        TimeRewardPlayerData.playerData.remove(e.getPlayer());
    }
}
