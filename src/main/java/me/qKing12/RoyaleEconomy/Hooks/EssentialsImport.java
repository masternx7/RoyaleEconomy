package me.qKing12.RoyaleEconomy.Hooks;

import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import me.qKing12.RoyaleEconomy.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

public class EssentialsImport implements CommandExecutor {

    public EssentialsImport(){
        RoyaleEconomy.plugin.getCommand("royaleeconomyimport_essentials").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(sender.isOp() || sender instanceof ConsoleCommandSender) {
            RoyaleEconomy.dataManager.importEssentials();
            sender.sendMessage(Utils.chat("&aIf essentials exists, player data was transfered to RoyaleEconomy!"));
            //sender.sendMessage(Utils.chat("&cNotice: If you are using EssentialsX there might be economy coliding problems, please consider setting no-economy to true in config.yml."));
            //sender.sendMessage(Utils.chat("&cIf you are uncertain of what to do or would like more clarification, please contact me on discord! qKing12#1918"));
        }
        return true;
    }

}
