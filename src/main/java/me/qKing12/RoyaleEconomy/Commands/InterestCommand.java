package me.qKing12.RoyaleEconomy.Commands;

import com.tcoded.folialib.wrapper.task.WrappedTask;
import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import me.qKing12.RoyaleEconomy.utils.PermissionChecker;
import me.qKing12.RoyaleEconomy.utils.PlayerMessageHandler;
import me.qKing12.RoyaleEconomy.utils.Utils;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static me.qKing12.RoyaleEconomy.RoyaleEconomy.utilsAPI;



public class InterestCommand implements TabExecutor {

    public static Long interestDate;
    private static WrappedTask interestChecker;
    public static boolean isForced=false;

    public static WrappedTask getInterestChecker(){
        return interestChecker;
    }

    public InterestCommand() {
        //RoyaleEconomy.plugin.getCommand("interest").setExecutor(this);
        //RoyaleEconomy.plugin.getCommand("interest").setTabCompleter(this);
    }

    public void extraSetup(){
        interestDate = RoyaleEconomy.dataManager.getInterestDate();
        interestChecker = RoyaleEconomy.dataManager.checkInterest();
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {
        if(args.length==1) {
            ArrayList<String> values=new ArrayList<>();
            values.add("");
            if(PermissionChecker.checkPermissionSilent("commands.interest.reset", commandSender))
                values.add("reset");
            if(PermissionChecker.checkPermissionSilent("commands.interest.slowdown", commandSender))
                values.add("slowdown");
            if(PermissionChecker.checkPermissionSilent("commands.interest.speedup", commandSender))
                values.add("speedup");
            if(PermissionChecker.checkPermissionSilent("commands.interest.force", commandSender))
                values.add("force");
            if(PermissionChecker.checkPermissionSilent("commands.interest.command-help", commandSender))
                values.add("help");
            return StringUtil.copyPartialMatches(args[0], values, new ArrayList<>());
        }
        return null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            if(args.length==0){
                if(PermissionChecker.checkPermission("commands.interest.interest-check", p)) {
                    Utils.playSound(p, "commands.interest.interest-check");
                    for (String line : RoyaleEconomy.commandsCfg.getStringList("commands.interest.interest-output"))
                        PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, line.replace("%interest-cooldown%", RoyaleEconomy.messageHelper.getInterestCooldown("normal")).replace("%interest-cooldown-short%", RoyaleEconomy.messageHelper.getInterestCooldown("short")).replace("%interest-cooldown-detailed%", RoyaleEconomy.messageHelper.getInterestCooldown("detailed"))));
                }
                else
                    return true;
            }
            else {
                if (args[0].equalsIgnoreCase("reset")) {
                    if(PermissionChecker.checkPermission("commands.interest.reset", p)) {
                        Long timp = ZonedDateTime.now().toInstant().toEpochMilli();
                        timp += RoyaleEconomy.plugin.getConfig().getInt("interest-cooldown") * 3600000;
                        RoyaleEconomy.dataManager.updateInterestDate(timp);
                        interestDate = timp;
                        Utils.playSound(p, "commands.interest.reset");
                        PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.interest.reset.output")));
                    }
                    return true;
                } else if (args[0].equalsIgnoreCase("slowdown")) {
                    if (PermissionChecker.checkPermission("commands.interest.slowdown", p))
                        if (args.length > 1) {
                            try {
                                int ore = Integer.parseInt(args[1]);
                                interestDate += ore * 3600000;
                                RoyaleEconomy.dataManager.updateInterestDate(interestDate);
                                PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.interest.slowdown.output")));
                                Utils.playSound(p, "commands.interest.slowdown");
                            } catch (Exception x) {
                                PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.interest.slowdown.invalid-number")));
                            }
                            return true;
                        }
                    else
                        return true;
                } else if(args[0].equalsIgnoreCase("speedup")) {
                    if (PermissionChecker.checkPermission("commands.interest.speedup", p)) {
                        if (args.length > 1) {
                            try {
                                int ore = Integer.parseInt(args[1]);
                                interestDate -= ore * 3600000;
                                Long prezent = ZonedDateTime.now().toInstant().toEpochMilli();
                                if (prezent > interestDate)
                                    interestDate = prezent;
                                RoyaleEconomy.dataManager.updateInterestDate(interestDate);
                                PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.interest.speedup.output")));
                                Utils.playSound(p, "commands.interest.speedup");
                            } catch (Exception x) {
                                PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.interest.speedup.invalid-number")));
                            }
                            return true;
                        }
                    }
                    else
                        return true;
                }
                else if(args[0].equalsIgnoreCase("force")){
                    if(PermissionChecker.checkPermission("commands.interest.force", p)) {
                        Utils.playSound(p, "commands.interest.force");
                        isForced=true;
                        RoyaleEconomy.dataManager.triggerInterest();
                        isForced=false;
                        PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.interest.force.output")));
                    }
                    return true;
                }
                if(PermissionChecker.checkPermission("commands.interest.command-help", p)) {
                    Utils.playSound(p, "commands.interest.command-help");
                    for (String line : RoyaleEconomy.commandsCfg.getStringList("commands.interest.command-help"))
                        PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, line));
                }
            }
        }
        else if(sender.isOp()) {
            CommandSender p = sender;
            if (args.length > 0) {
                if (args[0].equalsIgnoreCase("reset")) {
                    Long timp = ZonedDateTime.now().toInstant().toEpochMilli();
                    timp += RoyaleEconomy.plugin.getConfig().getInt("interest-cooldown") * 3600000;
                    RoyaleEconomy.dataManager.updateInterestDate(timp);
                    interestDate = timp;
                    PlayerMessageHandler.messageSend(p, Utils.chat(RoyaleEconomy.commandsCfg.getString("commands.interest.reset.output")));
                    return true;
                } else if (args[0].equalsIgnoreCase("slowdown")) {
                    if (args.length > 1) {
                        try {
                            int ore = Integer.parseInt(args[1]);
                            interestDate += ore * 3600000;
                            RoyaleEconomy.dataManager.updateInterestDate(interestDate);
                            PlayerMessageHandler.messageSend(p, Utils.chat(RoyaleEconomy.commandsCfg.getString("commands.interest.slowdown.output")));
                        } catch (Exception x) {
                            PlayerMessageHandler.messageSend(p, Utils.chat(RoyaleEconomy.commandsCfg.getString("commands.interest.slowdown.invalid-number")));
                        }
                        return true;
                    }
                } else if (args[0].equalsIgnoreCase("speedup")) {
                    if (args.length > 1) {
                        try {
                            int ore = Integer.parseInt(args[1]);
                            interestDate -= ore * 3600000;
                            Long prezent = ZonedDateTime.now().toInstant().toEpochMilli();
                            if (prezent > interestDate)
                                interestDate = prezent;
                            RoyaleEconomy.dataManager.updateInterestDate(interestDate);
                            PlayerMessageHandler.messageSend(p, Utils.chat(RoyaleEconomy.commandsCfg.getString("commands.interest.speedup.output")));
                        } catch (Exception x) {
                            PlayerMessageHandler.messageSend(p, Utils.chat(RoyaleEconomy.commandsCfg.getString("commands.interest.speedup.invalid-number")));
                        }
                        return true;
                    }
                    return true;
                } else if (args[0].equalsIgnoreCase("force")) {
                    isForced=true;
                    RoyaleEconomy.dataManager.triggerInterest();
                    isForced=false;
                    PlayerMessageHandler.messageSend(p, Utils.chat(RoyaleEconomy.commandsCfg.getString("commands.interest.force.output")));
                    return true;
                }
            }
            for (String line : RoyaleEconomy.commandsCfg.getStringList("commands.interest.command-help"))
                PlayerMessageHandler.messageSend(p, Utils.chat(line));
        }
        return true;
    }

}
