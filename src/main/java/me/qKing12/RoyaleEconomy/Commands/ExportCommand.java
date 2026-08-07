package me.qKing12.RoyaleEconomy.Commands;

import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import me.qKing12.RoyaleEconomy.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ExportCommand implements CommandExecutor {

    public ExportCommand(){
        RoyaleEconomy.plugin.getCommand("royaleeconomy_export").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(sender.isOp()) {
            RoyaleEconomy.dataManager.exportEconomy();
            sender.sendMessage(Utils.chat("&aEconomy export attempted!"));
        }
        return true;
    }
}
