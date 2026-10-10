package me.qKing12.RoyaleEconomy.Commands;

import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import me.qKing12.RoyaleEconomy.Menus.*;
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

import java.util.ArrayList;
import java.util.List;

import static me.qKing12.RoyaleEconomy.RoyaleEconomy.utilsAPI;



public class OnlyBankCommand implements TabExecutor {

    public OnlyBankCommand(){
        //RoyaleEconomy.plugin.getCommand("bank").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            String sharedBank = RoyaleEconomy.dataManager.getSharedBankManager().getSharedBankId(p.getUniqueId().toString());
            if(sharedBank!=null && !sharedBank.equals("")){
                if(args.length>0){
                    if(args[0].equalsIgnoreCase("balance") || args[0].equalsIgnoreCase("bal")){
                        if(PermissionChecker.checkPermission("commands.sharedbank.sharedbank-balance", p)) {
                            if (args.length == 1) {
                                Utils.playSound(p, "commands.sharedbank.sharedbank-balance");
                                Double coins = RoyaleEconomy.dataManager.getSharedBankManager().getSharedBankMoneyFromFile(sharedBank);
                                PlayerMessageHandler.messageSend(p, RoyaleEconomy.messageHelper.getSharedBankBalanceMessage(coins));
                            } else {
                                if(PermissionChecker.checkPermission("commands.sharedbank.sharedbank-balance-other", p)) {
                                    Utils.playSound(p, "commands.sharedbank.sharedbank-balance-other");
                                    PlayerMessageHandler.messageSend(p, RoyaleEconomy.messageHelper.getSharedBankBalanceMessage(args[1], RoyaleEconomy.dataManager.getSharedBankManager().getSharedBankMoneyFromFile(sharedBank)));
                                }

                            }
                        }
                        return true;
                    }
                    else if(args[0].equalsIgnoreCase("deposit")){
                        if(PermissionChecker.checkPermission("commands.sharedbank.sharedbank-deposit-command", p))
                            if(!SharedBankCommand.isInCooldown(p))
                                new SharedBankDepositMenu(p);
                        return true;
                    }
                    else if(args[0].equalsIgnoreCase("withdraw")){
                        if(PermissionChecker.checkPermission("commands.sharedbank.sharedbank-withdraw-command", p))
                            if(!SharedBankCommand.isInCooldown(p))
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
                                        Utils.playSound(p, "commands.sharedbank.sharedbank-disband");
                                        PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.sharedbank.delete-success")));
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
                                String playerToRemove = RoyaleEconomy.dataManager.getUUIDfromName(args[1]);
                                if (playerToRemove != null) {
                                    Player p2 = Bukkit.getPlayerExact(args[1]);
                                    if (RoyaleEconomy.dataManager.getSharedBankManager().removePlayerFromSharedBank(playerToRemove, sharedBank)) {
                                        Utils.playSound(p, "commands.sharedbank.sharedbank-kick");
                                        PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.sharedbank.kick-success").replace("%player%", args[1])));
                                        if (p2 != null) {
                                            Utils.playSound(p2, "commands.sharedbank.sharedbank-kick");
                                            PlayerMessageHandler.messageSend(p2, utilsAPI.chat(p2, RoyaleEconomy.commandsCfg.getString("commands.sharedbank.kick-player")));
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
                                    if (RoyaleEconomy.dataManager.getSharedBankManager().removePlayerFromSharedBank(p.getUniqueId().toString(), sharedBank)) {
                                        PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.sharedbank.bank-leave")));
                                        Utils.playSound(p, "commands.sharedbank.sharedbank-leave");
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
                                    if (SharedBankCommand.invitations.contains(invitation)) {
                                        PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.sharedbank.invite-already-sent")));
                                    } else {
                                        SharedBankCommand.invitations.add(invitation);
                                        for (String line : RoyaleEconomy.commandsCfg.getStringList("commands.sharedbank.invite-sent"))
                                            PlayerMessageHandler.messageSend(p2, utilsAPI.chat(p2, line.replace("%from-player%", p.getName())));
                                        Utils.playSound(p2, "commands.sharedbank.sharedbank-invite.to-player");
                                        Utils.playSound(p, "commands.sharedbank.sharedbank-invite.from-player");
                                        PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.sharedbank.invite-sent2").replace("%to-player%", p2.getName())));
                                        RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runLater(() -> {
                                            SharedBankCommand.invitations.remove(invitation);
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
                                if (SharedBankCommand.invitations.contains(invitation)) {
                                    SharedBankCommand.invitations.remove(invitation);
                                    SharedBankCommand.invitations.remove(p.getName() + "." + args[1]);
                                    PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.sharedbank.invite-accept").replace("%from-player%", args[1])));
                                    Utils.playSound(p, "commands.sharedbank.sharedbank-accept");
                                    Player p2 = Bukkit.getPlayerExact(args[1]);
                                    if (p2 != null) {
                                        PlayerMessageHandler.messageSend(p2, utilsAPI.chat(p2, RoyaleEconomy.commandsCfg.getString("commands.sharedbank.invite-accept2").replace("%to-player%", p.getName())));
                                        Utils.playSound(p2, "commands.sharedbank.sharedbank-accept");
                                    }
                                    RoyaleEconomy.dataManager.getSharedBankManager().addPlayerToSharedBank(p, sharedBank);
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
                        if(!SharedBankCommand.isInCooldown(p))
                            new SharedMainBankMenu(p);
                }
            }
            else {
                RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runAsync((task) -> {
                    if(args.length>0){
                        if(args[0].equalsIgnoreCase("balance") || args[0].equalsIgnoreCase("bal")){
                            if(PermissionChecker.checkPermission("commands.bank.bank-balance", p)) {
                                if (args.length == 1) {
                                    Utils.playSound(p, "commands.bank.bank-balance");
                                    Double coins = RoyaleEconomy.dataManager.getBankMoneyFromFile(p.getUniqueId().toString());
                                    Utils.runOnPlayer(p, () -> PlayerMessageHandler.messageSend(p, RoyaleEconomy.messageHelper.getBankBalanceMessage(coins)));
                                } else {
                                    if(PermissionChecker.checkPermission("commands.bank.bank-balance-other", p)) {
                                        String uuid = RoyaleEconomy.dataManager.getUUIDfromName(args[1]);
                                        if (uuid == null)
                                            Utils.runOnPlayer(p, () -> PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.bank.player-not-found"))));
                                        else {
                                            Utils.playSound(p, "commands.bank.bank-balance-other");
                                            Utils.runOnPlayer(p, () -> PlayerMessageHandler.messageSend(p, RoyaleEconomy.messageHelper.getBankBalanceMessage(args[1], RoyaleEconomy.dataManager.getBankMoneyFromFile(uuid))));
                                        }
                                    }
                                }
                            }
                        }
                        else if(args[0].equalsIgnoreCase("deposit")){
                            if(PermissionChecker.checkPermission("commands.bank.bank-deposit-command", p)) {
                                if(!BankCommand.isInCooldown(p))
                                    new BankDepositMenu(p);
                            }
                        }
                        else if(args[0].equalsIgnoreCase("withdraw")){
                            if(PermissionChecker.checkPermission("commands.bank.bank-withdraw-command", p))
                                if(!BankCommand.isInCooldown(p))
                                    new BankWithdrawMenu(p);
                        }
                        else{
                            if(PermissionChecker.checkPermission("commands.bank.command-help", p)) {
                                Utils.playSound(p, "commands.bank.command-help");
                                for (String line : RoyaleEconomy.commandsCfg.getStringList("commands.bank.command-help"))
                                    Utils.runOnPlayer(p, () -> PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, line)));
                            }
                        }
                    }
                    else{
                        if(PermissionChecker.checkPermission("commands.bank.bank-command", p))
                            if(!BankCommand.isInCooldown(p))
                                new MainBankMenu(p);
                    }
                });
            }
        }
        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return null;
    }
}
