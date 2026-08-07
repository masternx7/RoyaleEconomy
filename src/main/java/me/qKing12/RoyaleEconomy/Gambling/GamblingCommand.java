package me.qKing12.RoyaleEconomy.Gambling;

import me.qKing12.RoyaleEconomy.Commands.dynamic.DynamicCommandsSetup;
import me.qKing12.RoyaleEconomy.utils.PlayerMessageHandler;
import me.qKing12.RoyaleEconomy.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class GamblingCommand implements TabExecutor {

    public GamblingCommand(){
        //RoyaleEconomy.plugin.getCommand("gambling").setExecutor(this);
        DynamicCommandsSetup.getCommandSettings().put("gambling", this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if(sender instanceof Player){
            Player player=(Player)sender;
            if(Gambling.permission.equals("none") || player.hasPermission(Gambling.permission)) {
                if(args.length>0 && sender.isOp()) {
                    if (args[0].equalsIgnoreCase("forceOpen")) {
                        if (args.length > 1) {
                            Player target = Bukkit.getPlayerExact(args[1]);
                            if (target != null) {
                                new GamblingMenu(target);
                            } else
                                player.sendMessage(Utils.chat("&cPlayer not found."));
                        } else
                            player.sendMessage(Utils.chat("&cNo player specified."));
                    }
                    return false;
                }
                new GamblingMenu((Player)sender);
            }else
                PlayerMessageHandler.messageSend(player, Gambling.noPermission);
        }
        else if(sender instanceof ConsoleCommandSender){
            ConsoleCommandSender player=(ConsoleCommandSender)sender;
            if(args.length>0) {
                if (args[0].equalsIgnoreCase("forceOpen")) {
                    if (args.length > 1) {
                        Player target = Bukkit.getPlayerExact(args[1]);
                        if (target != null) {
                            new GamblingMenu(target);
                        } else
                            player.sendMessage(Utils.chat("&cPlayer not found."));
                    } else
                        player.sendMessage(Utils.chat("&cNo player specified."));
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
}
