package me.qKing12.RoyaleEconomy.Commands;

import de.tr7zw.changeme.nbtapi.NBTItem;
import me.qKing12.RoyaleEconomy.DataManager.StaticValues;
import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import me.qKing12.RoyaleEconomy.utils.PermissionChecker;
import me.qKing12.RoyaleEconomy.utils.PlayerMessageHandler;
import me.qKing12.RoyaleEconomy.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.StringUtil;

import java.util.*;

import static me.qKing12.RoyaleEconomy.RoyaleEconomy.coinBagsAndTalismansCfg;
import static me.qKing12.RoyaleEconomy.RoyaleEconomy.utilsAPI;



public class MoneyBagCommand implements TabExecutor {

    public static ItemStack generateMoneyBag(double coins){
        String amount = RoyaleEconomy.messageHelper.numberFormat(coins);
        ItemStack bag = RoyaleEconomy.staticValues.moneyBagItem.clone();
        ItemMeta meta = bag.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        for(String line : meta.getLore())
            lore.add(line.replace("%amount%", amount));
        meta.setLore(lore);
        meta.setDisplayName(meta.getDisplayName().replace("%amount%", amount));
        bag.setItemMeta(meta);
        NBTItem nbt = new NBTItem(bag);
        nbt.setDouble("RoyaleEconomyBag", coins);
        if(!coinBagsAndTalismansCfg.getBoolean("money-bags.allow-stack"))
            nbt.setString("RoyaleEconomyUnique", UUID.randomUUID().toString());
        return nbt.getItem();
    }

    public MoneyBagCommand(){
        //RoyaleEconomy.plugin.getCommand("moneybag").setExecutor(this);
        //RoyaleEconomy.plugin.getCommand("moneybag").setTabCompleter(this);
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {
        if(args.length==1) {
            ArrayList<String> values=new ArrayList<>();
            if(PermissionChecker.checkPermissionSilent("commands.moneybag.get-command", commandSender))
                values.add("get");
            if(PermissionChecker.checkPermissionSilent("commands.moneybag.adminget-command", commandSender))
                values.add("adminget");
            if(PermissionChecker.checkPermissionSilent("commands.moneybag.give-command", commandSender))
                values.add("give");
            return StringUtil.copyPartialMatches(args[0], values, new ArrayList<>());
        }
        else if(args.length==2){
            if(args[0].equalsIgnoreCase("get"))
                return new ArrayList<>(Arrays.asList("1", "10", "150", "50", "500"));
        }
        return null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;

            if(args.length>1){
                if(args[0].equalsIgnoreCase("get")){
                    if(PermissionChecker.checkPermission("commands.moneybag.get-command", p)) {
                        if (p.getInventory().firstEmpty() == -1) {
                            PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, coinBagsAndTalismansCfg.getString("money-bags.full-inventory-message")));
                            return true;
                        }
                        try {
                            double amount;
                            if(args[1].contains("-")){
                                int inferior=Integer.parseInt(args[1].split("-")[0]);
                                if(inferior<coinBagsAndTalismansCfg.getInt("money-bags.minimum-amount"))
                                    inferior=coinBagsAndTalismansCfg.getInt("money-bags.minimum-amount");
                                int superior=Integer.parseInt(args[1].split("-")[1]);
                                if(superior>coinBagsAndTalismansCfg.getInt("money-bags.maximum-amount"))
                                    superior=coinBagsAndTalismansCfg.getInt("money-bags.maximum-amount");
                                Random random=new Random();
                                amount=random.nextInt(superior-inferior)+inferior;
                            }
                            else {
                                amount = Math.abs(RoyaleEconomy.messageHelper.getCoinsFromFormat(args[1]));
                                if (amount < coinBagsAndTalismansCfg.getDouble("money-bags.minimum-amount")) {
                                    PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, coinBagsAndTalismansCfg.getString("money-bags.minimum-message")));
                                    return true;
                                }
                                if (amount > coinBagsAndTalismansCfg.getDouble("money-bags.maximum-amount")) {
                                    PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, coinBagsAndTalismansCfg.getString("money-bags.maximum-message")));
                                    return true;
                                }
                            }

                            if(RoyaleEconomy.dataManager.removeMoneyFromFile(p.getUniqueId().toString(), amount)){
                                p.getInventory().addItem(generateMoneyBag(amount));
                                Utils.playSound(p, "commands.moneybag.get-command");
                                PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.moneybag.output").replace("%amount%", RoyaleEconomy.messageHelper.numberFormat(amount))));
                            }
                            else {
                                PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.moneybag.output-fail")));
                                return true;
                            }
                        } catch (NumberFormatException x) {
                            PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.moneybag.invalid-number")));
                        }
                    }
                    return true;
                }
                else if(args[0].equalsIgnoreCase("adminget")) {
                    if(PermissionChecker.checkPermission("commands.moneybag.adminget-command", p)) {
                        if (p.getInventory().firstEmpty() == -1) {
                            PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, coinBagsAndTalismansCfg.getString("money-bags.full-inventory-message")));
                            return true;
                        }
                        try {
                            double amount;
                            if(args[1].contains("-")){
                                int inferior=Integer.parseInt(args[1].split("-")[0]);
                                if(inferior<coinBagsAndTalismansCfg.getInt("money-bags.minimum-amount"))
                                    inferior=coinBagsAndTalismansCfg.getInt("money-bags.minimum-amount");
                                int superior=Integer.parseInt(args[1].split("-")[1]);
                                if(superior>coinBagsAndTalismansCfg.getInt("money-bags.maximum-amount"))
                                    superior=coinBagsAndTalismansCfg.getInt("money-bags.maximum-amount");
                                Random random=new Random();
                                amount=random.nextInt(superior-inferior)+inferior;
                            }
                            else {
                                amount = Math.abs(RoyaleEconomy.messageHelper.getCoinsFromFormat(args[1]));
                                if (amount < coinBagsAndTalismansCfg.getDouble("money-bags.minimum-amount")) {
                                    PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, coinBagsAndTalismansCfg.getString("money-bags.minimum-message")));
                                    return true;
                                }
                                if (amount > coinBagsAndTalismansCfg.getDouble("money-bags.maximum-amount")) {
                                    PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, coinBagsAndTalismansCfg.getString("money-bags.maximum-message")));
                                    return true;
                                }
                            }
                            p.getInventory().addItem(generateMoneyBag(amount));
                            Utils.playSound(p, "commands.moneybag.adminget-command");
                            PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.moneybag.output").replace("%amount%", RoyaleEconomy.messageHelper.numberFormat(amount))));
                        } catch (NumberFormatException x) {
                            PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.moneybag.invalid-number")));
                        }
                    }
                    return true;
                }
                else if(args[0].equalsIgnoreCase("give")){
                    if(PermissionChecker.checkPermission("commands.moneybag.give-command", p)) {
                        if (args.length < 3) {
                            PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, "&cPlease use /moneybag give <player> <amount>"));
                            return true;
                        }

                        Player p2 = Bukkit.getPlayerExact(args[1]);
                        if (p2 == null) {
                            PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.moneybag.give-invalid-player")));
                            return true;
                        }
                        try {
                            double amount;
                            if(args[2].contains("-")){
                                int inferior=Integer.parseInt(args[2].split("-")[0]);
                                int superior=Integer.parseInt(args[2].split("-")[1]);
                                if (superior > 100000000d)
                                    superior = 100000000;
                                Random random=new Random();
                                amount=random.nextInt(superior-inferior)+inferior;
                            }
                            else {
                                amount = RoyaleEconomy.messageHelper.getCoinsFromFormat(args[2]);
                                if (amount > 100000000d)
                                    amount = 100000000d;
                            }
                            if (p2.getInventory().firstEmpty() == -1)
                                p2.getWorld().dropItemNaturally(p2.getLocation(), generateMoneyBag(amount));
                            else
                                p2.getInventory().addItem(generateMoneyBag(amount));
                            Utils.playSound(p, "commands.moneybag.give-command.from-player");
                            Utils.playSound(p2, "commands.moneybag.give-command.to-player");
                            PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.moneybag.output-give")));
                            PlayerMessageHandler.messageSend(p2, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.moneybag.output").replace("%amount%", RoyaleEconomy.messageHelper.numberFormat(amount))));
                        } catch (NumberFormatException x) {
                            PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.moneybag.invalid-number")));
                        }
                    }
                    return true;
                }
            }

            if(PermissionChecker.checkPermission("commands.moneybag.command-help", p)) {
                Utils.playSound(p, "commands.moneybag.command-help");
                for (String line : RoyaleEconomy.commandsCfg.getStringList("commands.moneybag.command-help"))
                    PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, line));
            }
        }
        else if(sender.isOp()){
            CommandSender p = sender;
            if(args.length>0) {
                if (args[0].equalsIgnoreCase("give")) {
                    if (args.length < 3) {
                        p.sendMessage( "Please use /moneybag give <player> <amount>");
                        return true;
                    }

                    Player p2 = Bukkit.getPlayerExact(args[1]);
                    if (p2 == null) {
                        PlayerMessageHandler.messageSend(p, Utils.chat(RoyaleEconomy.commandsCfg.getString("commands.moneybag.give-invalid-player")));
                        return true;
                    }
                    try {
                        double amount;
                        if(args[2].contains("-")){
                            int inferior=Integer.parseInt(args[2].split("-")[0]);
                            int superior=Integer.parseInt(args[2].split("-")[1]);
                            if (superior > 100000000d)
                                superior = 100000000;
                            Random random=new Random();
                            amount=random.nextInt(superior-inferior)+inferior;
                        }
                        else {
                            amount = RoyaleEconomy.messageHelper.getCoinsFromFormat(args[2]);
                            if (amount > 100000000d)
                                amount = 100000000d;
                        }
                        if (p2.getInventory().firstEmpty() == -1)
                            p2.getWorld().dropItemNaturally(p2.getLocation(), generateMoneyBag(amount));
                        else
                            p2.getInventory().addItem(generateMoneyBag(amount));
                        PlayerMessageHandler.messageSend(p, Utils.chat(RoyaleEconomy.commandsCfg.getString("commands.moneybag.output-give")));
                        PlayerMessageHandler.messageSend(p2, Utils.chat(RoyaleEconomy.commandsCfg.getString("commands.moneybag.output").replace("%amount%", RoyaleEconomy.messageHelper.numberFormat(amount))));
                    } catch (NumberFormatException x) {
                        PlayerMessageHandler.messageSend(p, Utils.chat(RoyaleEconomy.commandsCfg.getString("commands.moneybag.invalid-number")));
                    }
                    return true;
                }
            }

            for(String line : RoyaleEconomy.commandsCfg.getStringList("commands.moneybag.command-help"))
                PlayerMessageHandler.messageSend(p, Utils.chat(line));

        }
        return true;
    }

}
