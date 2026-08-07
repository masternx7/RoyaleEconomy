package me.qKing12.RoyaleEconomy.Hooks;

import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import me.qKing12.RoyaleEconomy.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

public class CMIImport implements CommandExecutor {

    public CMIImport(){
        RoyaleEconomy.plugin.getCommand("royaleeconomyimport_cmi").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(sender.isOp() || sender instanceof ConsoleCommandSender) {
            RoyaleEconomy.dataManager.importCMI();
            sender.sendMessage(Utils.chat("&aIf CMI exists, player data was transfered to RoyaleEconomy!"));
        }
        return true;
    }

}
