package me.qKing12.RoyaleEconomy.Boosters;

import me.qKing12.RoyaleEconomy.Commands.dynamic.DynamicCommandsSetup;
import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import me.qKing12.RoyaleEconomy.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BoosterCommand implements TabExecutor {

    public BoosterCommand(){
        //RoyaleEconomy.plugin.getCommand("booster").setExecutor(this);
        DynamicCommandsSetup.getCommandSettings().put("boosters", this);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        String permission = BoostersActive.permission;
        if(permission.equalsIgnoreCase("none") || commandSender.hasPermission(permission)){
            if(commandSender instanceof ConsoleCommandSender){
                commandSender.sendMessage("You can not see the boosters from console.");
                return false;
            }
            RoyaleEconomy.boosters.sendMessage((Player) commandSender);
        }
        else
            commandSender.sendMessage(Utils.chat(RoyaleEconomy.boostersCfg.getString("booster-command.no-permission")));
        return false;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return null;
    }
}
