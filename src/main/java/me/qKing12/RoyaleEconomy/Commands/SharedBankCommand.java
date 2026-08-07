package me.qKing12.RoyaleEconomy.Commands;

import me.qKing12.RoyaleEconomy.DataManager.StaticValues;
import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import me.qKing12.RoyaleEconomy.Menus.*;
import me.qKing12.RoyaleEconomy.utils.PermissionChecker;
import me.qKing12.RoyaleEconomy.utils.PlayerMessageHandler;
import me.qKing12.RoyaleEconomy.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static me.qKing12.RoyaleEconomy.RoyaleEconomy.utilsAPI;

public class SharedBankCommand implements TabExecutor {
    private static HashMap<String, Long> cooldown=new HashMap<>();
    public static boolean isInCooldown(Player player){
        if(RoyaleEconomy.staticValues.cooldownSharedBank==0 || PermissionChecker.checkBankCooldownBypass(player, true))
            return false;
        if(cooldown.containsKey(player.getUniqueId().toString())){
            if(cooldown.get(player.getUniqueId().toString())<= ZonedDateTime.now().toInstant().toEpochMilli()){
                cooldown.put(player.getUniqueId().toString(), ZonedDateTime.now().toInstant().toEpochMilli()+RoyaleEconomy.staticValues.cooldownSharedBank);
                return false;
            }
            else{
                PlayerMessageHandler.messageSend(player, (utilsAPI.chat(player, RoyaleEconomy.plugin.getConfig().getString("bank-cooldown-message").replace("%cooldown%", String.valueOf((cooldown.get(player.getUniqueId().toString())-ZonedDateTime.now().toInstant().toEpochMilli())/1000)))));
                return true;
            }
        }
        else{
            cooldown.put(player.getUniqueId().toString(), ZonedDateTime.now().toInstant().toEpochMilli()+RoyaleEconomy.staticValues.cooldownSharedBank);
            return false;
        }
    }

    public SharedBankCommand(){
        //RoyaleEconomy.plugin.getCommand("sharedbank").setExecutor(this);
        //RoyaleEconomy.plugin.getCommand("sharedbank").setTabCompleter(this);
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {
        if(args.length==1) {
            ArrayList<String> values=new ArrayList<>();
            values.add("");
            if(PermissionChecker.checkPermissionSilent("commands.sharedbank.sharedbank-balance", commandSender))
                values.add("balance");
            if(PermissionChecker.checkPermissionSilent("commands.sharedbank.sharedbank-upgrades-command", commandSender))
                values.add("upgrades");
            if(PermissionChecker.checkPermissionSilent("commands.sharedbank.sharedbank-deposit-command", commandSender))
                values.add("deposit");
            if(PermissionChecker.checkPermissionSilent("commands.sharedbank.sharedbank-withdraw-command", commandSender))
                values.add("withdraw");
            if(PermissionChecker.checkPermissionSilent("commands.sharedbank.command-help", commandSender))
                values.add("help");
            if(PermissionChecker.checkPermissionSilent("commands.sharedbank.sharedbank-create", commandSender))
                values.add("create");
            if(PermissionChecker.checkPermissionSilent("commands.sharedbank.sharedbank-disband", commandSender))
                values.add("disband");
            if(PermissionChecker.checkPermissionSilent("commands.sharedbank.sharedbank-kick", commandSender))
                values.add("kick");
            if(PermissionChecker.checkPermissionSilent("commands.sharedbank.sharedbank-leave", commandSender))
                values.add("leave");
            if(PermissionChecker.checkPermissionSilent("commands.sharedbank.sharedbank-invite", commandSender))
                values.add("invite");
            if(PermissionChecker.checkPermissionSilent("commands.sharedbank.sharedbank-accept", commandSender))
                values.add("accept");
            if(PermissionChecker.checkPermissionSilent("commands.sharedbank.sharedbank-transfer", commandSender))
                values.add("transfer");
            if(PermissionChecker.checkPermissionSilent("commands.sharedbank.sharedbank-info", commandSender))
                values.add("info");
            if(PermissionChecker.checkPermissionSilent("commands.sharedbank.command-help", commandSender))
                values.add("help");
            return StringUtil.copyPartialMatches(args[0], values, new ArrayList<>());

        }
        //else if(args.length==2){
        //    if(args[0].equalsIgnoreCase("deposit") || args[0].equalsIgnoreCase("withdraw"))
        //        return new ArrayList<>(Arrays.asList("1", "10", "150", "50", "500"));
        //}
        return null;
    }

    protected static ArrayList<String> invitations=new ArrayList<>();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            if(args.length>0){
                if(args[0].equalsIgnoreCase("balance") || args[0].equalsIgnoreCase("bal")){
                    if(PermissionChecker.checkPermission("commands.sharedbank.sharedbank-balance", p)) {
                        if (args.length == 1) {
                            Utils.playSound(p, "commands.sharedbank.sharedbank-balance");
                            Double coins = RoyaleEconomy.dataManager.getSharedBankManager().getSharedBankMoneyPlayer(p.getUniqueId().toString());
                            PlayerMessageHandler.messageSend(p, RoyaleEconomy.messageHelper.getSharedBankBalanceMessage(coins));
                        } else {
                            if(PermissionChecker.checkPermission("commands.sharedbank.sharedbank-balance-other", p)) {
                                Utils.playSound(p, "commands.sharedbank.sharedbank-balance-other");
                                String bankID = RoyaleEconomy.dataManager.getSharedBankManager().getSharedBankId(args[1]);
                                if (bankID == null)
                                    PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.sharedbank.player-not-found")));
                                else {
                                    PlayerMessageHandler.messageSend(p, RoyaleEconomy.messageHelper.getSharedBankBalanceMessage(args[1], RoyaleEconomy.dataManager.getSharedBankManager().getSharedBankMoneyFromFile(bankID)));
                                }
                            }
                        }
                    }
                    return true;
                }
                else if(args[0].equalsIgnoreCase("upgrades")){
                    if(PermissionChecker.checkPermission("commands.sharedbank.sharedbank-upgrades-command", p)) {
                        if(!isInCooldown(p))
                            new SharedBankUpgradesMenu(p);
                    }
                    return true;
                }
                else if(args[0].equalsIgnoreCase("deposit")){
                    if(PermissionChecker.checkPermission("commands.sharedbank.sharedbank-deposit-command", p))
                        if(!isInCooldown(p))
                            new SharedBankDepositMenu(p);
                    return true;
                }
                else if(args[0].equalsIgnoreCase("withdraw")){
                    if(PermissionChecker.checkPermission("commands.sharedbank.sharedbank-withdraw-command", p))
                        if(!isInCooldown(p))
                            new SharedBankWithdrawMenu(p);
                    return true;
                }
                else if(args[0].equalsIgnoreCase("create")){
                    RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runAsync((task) -> {
                        if (PermissionChecker.checkPermission("commands.sharedbank.sharedbank-create", p)) {
                            if (RoyaleEconomy.dataManager.getSharedBankManager().createSharedBank(p)) {
                                PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.sharedbank.create-success")));
                                Utils.playSound(p, "commands.sharedbank.sharedbank-create");
                            } else
                                PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.sharedbank.create-fail")));
                        }
                    });
                    return true;
                }
                else if(args[0].equalsIgnoreCase("delete") || args[0].equalsIgnoreCase("disband")){
                    if(PermissionChecker.checkPermission("commands.sharedbank.sharedbank-disband", p)) {
                        if (RoyaleEconomy.dataManager.getSharedBankManager().isOwnerSharedBank(p)) {
                            if (args.length > 1 && args[1].equalsIgnoreCase("confirm")) {
                                if (RoyaleEconomy.dataManager.getSharedBankManager().deleteSharedBank(p)) {
                                    PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.sharedbank.delete-success")));
                                    Utils.playSound(p, "commands.sharedbank.sharedbank-disband");
                                }
                                else
                                    PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.sharedbank.no-shared-bank")));
                            } else {
                                PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.sharedbank.confirm-message").replace("%command%", "/sharedbank disband confirm")));
                            }
                        } else
                            PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.sharedbank.must-be-owner")));
                    }
                    return true;
                }
                else if(args[0].equalsIgnoreCase("kick")){
                    if(PermissionChecker.checkPermission("commands.sharedbank.sharedbank-kick", p)) {
                        if (args.length > 1) {
                            String bankID = RoyaleEconomy.dataManager.getSharedBankManager().getSharedBankId(p.getUniqueId().toString());
                            if (bankID.equals("")) {
                                PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.sharedbank.no-shared-bank")));
                                return true;
                            }

                            String playerToRemove = RoyaleEconomy.dataManager.getUUIDfromName(args[1]);
                            if (playerToRemove != null) {
                                Player p2 = Bukkit.getPlayerExact(args[1]);
                                if (RoyaleEconomy.dataManager.getSharedBankManager().removePlayerFromSharedBank(playerToRemove, bankID)) {
                                    Utils.playSound(p, "commands.sharedbank.sharedbank-kick");
                                    PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.sharedbank.kick-success").replace("%player%", args[1])));
                                    if (p2 != null) {
                                        PlayerMessageHandler.messageSend(p2, utilsAPI.chat(p2, RoyaleEconomy.commandsCfg.getString("commands.sharedbank.kick-player")));
                                        Utils.playSound(p2, "commands.sharedbank.sharedbank-kick");
                                    }
                                } else
                                    PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.sharedbank.kick-fail")));
                            } else
                                PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.sharedbank.player-not-found")));
                            return true;
                        }
                    }
                    else return true;
                }
                else if(args[0].equalsIgnoreCase("leave")){
                    if(PermissionChecker.checkPermission("commands.sharedbank.sharedbank-leave", p)) {
                        if (args.length > 1 && args[1].equalsIgnoreCase("confirm")) {
                            if (RoyaleEconomy.dataManager.getSharedBankManager().isOwnerSharedBank(p)) {
                                PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.sharedbank.bank-leave-fail")));
                            } else {
                                String bankID = RoyaleEconomy.dataManager.getSharedBankManager().getSharedBankId(p.getUniqueId().toString());
                                if (bankID.equals("")) {
                                    PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.sharedbank.no-shared-bank")));
                                } else {
                                    if (RoyaleEconomy.dataManager.getSharedBankManager().removePlayerFromSharedBank(p.getUniqueId().toString(), bankID)) {
                                        PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.sharedbank.bank-leave")));
                                        Utils.playSound(p, "commands.sharedbank.sharedbank-kick");
                                    }
                                }
                            }
                        } else
                            PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.sharedbank.confirm-message").replace("%command%", "/sharedbank leave confirm")));
                    }
                    return true;
                }
                else if(args[0].equalsIgnoreCase("invite")){
                    if(PermissionChecker.checkPermission("commands.sharedbank.sharedbank-invite", p)) {
                        if (args.length > 1) {
                            if (RoyaleEconomy.dataManager.getSharedBankManager().getSharedBankId(p.getUniqueId().toString()).equals("")) {
                                PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.sharedbank.no-shared-bank")));
                                return true;
                            }
                            if (args[0].equalsIgnoreCase(p.getName()))
                                return true;
                            Player p2 = Bukkit.getPlayerExact(args[1]);
                            if (p2 != null) {
                                String bankID = RoyaleEconomy.dataManager.getSharedBankManager().getSharedBankId(p2.getUniqueId().toString());
                                if (!bankID.equals("")) {
                                    PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.sharedbank.player-has-bank")));
                                    return true;
                                }
                                String invitation = p.getName() + "." + p2.getName();
                                if (invitations.contains(invitation)) {
                                    PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.sharedbank.invite-already-sent")));
                                } else {
                                    invitations.add(invitation);
                                    for (String line : RoyaleEconomy.commandsCfg.getStringList("commands.sharedbank.invite-sent"))
                                        PlayerMessageHandler.messageSend(p2, utilsAPI.chat(p2, line.replace("%from-player%", p.getName())));
                                    Utils.playSound(p2, "commands.sharedbank.sharedbank-invite.to-player");
                                    Utils.playSound(p, "commands.sharedbank.sharedbank-invite.from-player");
                                    PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.sharedbank.invite-sent2").replace("%to-player%", p2.getName())));
                                    RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runLater(() -> {
                                        invitations.remove(invitation);
                                    }, 400);
                                }
                            } else {
                                PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.sharedbank.player-online")));
                            }
                            return true;
                        }
                    }
                    else return true;
                }
                else if(args[0].equalsIgnoreCase("accept")){
                    if(PermissionChecker.checkPermission("commands.sharedbank.sharedbank-accept", p)) {
                        if (args.length > 1) {
                            String invitation = args[1] + "." + p.getName();
                            if (invitations.contains(invitation)) {
                                invitations.remove(invitation);
                                invitations.remove(p.getName() + "." + args[1]);
                                Utils.playSound(p, "commands.sharedbank.sharedbank-accept");
                                PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.sharedbank.invite-accept").replace("%from-player%", args[1])));
                                Player p2 = Bukkit.getPlayerExact(args[1]);
                                if (p2 != null) {
                                    PlayerMessageHandler.messageSend(p2, utilsAPI.chat(p2, RoyaleEconomy.commandsCfg.getString("commands.sharedbank.invite-accept2").replace("%to-player%", p.getName())));
                                    Utils.playSound(p2, "commands.sharedbank.sharedbank-accept");
                                }
                                RoyaleEconomy.dataManager.getSharedBankManager().addPlayerToSharedBank(p, RoyaleEconomy.dataManager.getSharedBankManager().getSharedBankId(args[1]));
                            } else {
                                PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.sharedbank.invite-no-invite")));
                            }
                            return true;
                        }
                    }
                    else return true;
                }
                else if(args[0].equalsIgnoreCase("transfer")){
                    if(PermissionChecker.checkPermission("commands.sharedbank.sharedbank-transfer", p)) {
                        if (args.length > 1) {
                            if (!RoyaleEconomy.dataManager.getSharedBankManager().isOwnerSharedBank(p)) {
                                PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.sharedbank.must-be-owner")));
                                return true;
                            }
                            Player p2 = Bukkit.getPlayerExact(args[1]);
                            if (p2 != null) {
                                if (RoyaleEconomy.dataManager.getSharedBankManager().transferOwnershipSharedBank(p.getUniqueId().toString(), p2.getUniqueId().toString())) {
                                    Utils.playSound(p, "commands.sharedbank.sharedbank-transfer");
                                    Utils.playSound(p2, "commands.sharedbank.sharedbank-transfer");
                                    PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.sharedbank.transfer-ownership-success").replace("%to-player%", p2.getName())));
                                    PlayerMessageHandler.messageSend(p2, utilsAPI.chat(p2, RoyaleEconomy.commandsCfg.getString("commands.sharedbank.transfer-ownership-success2")));
                                } else
                                    PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.sharedbank.transfer-ownership-fail")));
                            } else
                                PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.sharedbank.player-online")));

                            return true;
                        }
                    }
                    else return true;
                }
                else if(args[0].equals("info")){
                    if(PermissionChecker.checkPermission("commands.sharedbank.sharedbank-info", p)) {
                        if (args.length > 1) {
                            String bankID = RoyaleEconomy.dataManager.getSharedBankManager().getSharedBankId(args[1]);
                            if (bankID == null) {
                                PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.sharedbank.player-not-found")));
                                return true;
                            } else if (bankID.equals("")) {
                                PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.sharedbank.bank-info.no-bank")));
                                return true;
                            }

                            Utils.playSound(p, "commands.sharedbank.sharedbank-info");
                            ArrayList<String> members = RoyaleEconomy.dataManager.getSharedBankManager().getMembersSharedBank(bankID, true);
                            for (String line : RoyaleEconomy.commandsCfg.getStringList("commands.sharedbank.bank-info.header-message"))
                                PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, line.replace("%owner%", RoyaleEconomy.messageHelper.getPlayerName(members.get(0)))));
                            members.remove(0);
                            if (members.isEmpty())
                                PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.sharedbank.bank-info.no-members")));
                            else {
                                String structure = RoyaleEconomy.commandsCfg.getString("commands.sharedbank.bank-info.members-display-structure");
                                for (String member : members)
                                    PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, structure.replace("%member%", RoyaleEconomy.messageHelper.getPlayerName(member))));
                            }

                            String number = RoyaleEconomy.messageHelper.numberFormat(RoyaleEconomy.dataManager.getSharedBankManager().getSharedBankMoneyFromFile(bankID));
                            for (String line : RoyaleEconomy.commandsCfg.getStringList("commands.sharedbank.bank-info.footer-message"))
                                PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, line.replace("%balance%", number)));

                            return true;
                        }
                    }
                    else
                        return true;
                }

                if(PermissionChecker.checkPermission("commands.sharedbank.command-help", p)) {
                    Utils.playSound(p, "commands.sharedbank.command-help");
                    for (String line : RoyaleEconomy.commandsCfg.getStringList("commands.sharedbank.command-help"))
                        PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, line));
                }
            }
            else{
                if(PermissionChecker.checkPermission("commands.sharedbank.sharedbank-command", p))
                    if(!isInCooldown(p))
                        new SharedMainBankMenu(p);
            }
        }
        return true;
    }
}
