package me.qKing12.RoyaleEconomy.Commands;

import me.qKing12.RoyaleEconomy.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class DisabledCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        commandSender.sendMessage(Utils.chat("&cThis command is disabled."));
        return false;
    }

}
