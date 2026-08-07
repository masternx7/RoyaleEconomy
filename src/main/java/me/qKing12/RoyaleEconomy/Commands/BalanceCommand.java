package me.qKing12.RoyaleEconomy.Commands;

import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import me.qKing12.RoyaleEconomy.utils.PermissionChecker;
import me.qKing12.RoyaleEconomy.utils.PlayerMessageHandler;
import me.qKing12.RoyaleEconomy.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;


public class BalanceCommand implements TabExecutor {

    public BalanceCommand(){
        //RoyaleEconomy.plugin.getCommand("balance").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runAsync((task) -> {
            if (sender instanceof Player) {
                Player p = (Player) sender;
                if(args.length==0){
                    if(PermissionChecker.checkPermission("commands.balance.own", p)) {
                        Double coins = RoyaleEconomy.dataManager.getMoneyFromFile(p.getUniqueId().toString());
                        if (coins == null) {
                            RoyaleEconomy.dataManager.updateUsername(p);
                            coins = 0d;
                        }
                        double coinsFinal = coins;
                        RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runNextTick((task2) -> PlayerMessageHandler.messageSend(p, RoyaleEconomy.utilsAPI.chatApiOnly(p, RoyaleEconomy.messageHelper.getBalanceMessage(coinsFinal))));
                        Utils.playSound(p, "commands.balance.own");
                    }
                }
                else{
                    if(PermissionChecker.checkPermission("commands.balance.other", p)) {
                        Double coins = RoyaleEconomy.dataManager.getMoneyFromFile(args[0]);
                        if (coins == null)
                            coins = 0d;
                        double coinsFinal=coins;
                        RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runNextTick((task2) -> PlayerMessageHandler.messageSend(p, RoyaleEconomy.utilsAPI.chatApiOnly(p, RoyaleEconomy.messageHelper.getBalanceMessage(args[0], coinsFinal))));
                        Utils.playSound(p, "commands.balance.other");
                    }
                }
            }
        });
        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return null;
    }
}
