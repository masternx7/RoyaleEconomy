package me.qKing12.RoyaleEconomy.MultiCurrency.internal;

import me.qKing12.RoyaleEconomy.MultiCurrencyShops.Shop;
import me.qKing12.RoyaleEconomy.MultiCurrencyShops.ShopManagerMenu;
import me.qKing12.RoyaleEconomy.MultiCurrencyShops.ShopMenu;
import me.qKing12.RoyaleEconomy.MultiCurrencyShops.ShopsLoad;
import me.qKing12.RoyaleEconomy.RoyaleEconomy;
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
import java.util.UUID;

public class CurrencyCommand implements TabExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if(!commandSender.isOp()){
            commandSender.sendMessage(Utils.chat("&cYou need to have OP for this command."));
            return false;
        }
        if(args.length>0){
            if(args[0].equalsIgnoreCase("shop") || args[0].equalsIgnoreCase("shops")){
                    if (args.length > 1) {
                        if (args[1].equalsIgnoreCase("refreshItemsVersion")){
                            for (Shop shop : ShopsLoad.shops) {
                                for (Shop.ShopItem item : shop.getItems()) {
                                    item.addToConfiguration();
                                }
                                shop.saveConfig();
                            }
                            PlayerMessageHandler.messageSend(commandSender, Utils.chat("&aRefreshed items to current version."));
                            return true;
                        }
                        else if(args[1].equals("list")) {
                            commandSender.sendMessage(Utils.chat("&fShops list:"));
                            for(Shop shop : ShopsLoad.shops){
                                commandSender.sendMessage(Utils.chat("  &a- "+shop.getShopName()));
                            }
                            return true;
                        }else if (args[1].equals("manager")) {
                            if(commandSender instanceof Player){
                                new ShopManagerMenu((Player)commandSender);
                            }
                            else
                                commandSender.sendMessage(Utils.chat("&c")+"You need to be a player for this.");
                            return true;
                        }
                        else if(args[1].equalsIgnoreCase("forceopen")){
                            if(args.length>2){
                                Player p2 = Bukkit.getPlayerExact(args[2]);
                                if(p2!=null){
                                    if(args.length>3){
                                        for(Shop shop : ShopsLoad.shops){
                                            if(shop.getShopName().equalsIgnoreCase(args[3])){
                                                //PlayerMessageHandler.messageSend(p, Utils.chat(RoyaleEconomy.commandsCfg.getString("commands.reshop.success")));
                                                Utils.playSound(p2, "commands.reshop.forceopen.to-player");
                                                new ShopMenu(p2, shop, 0, null);
                                                return true;
                                            }
                                        }
                                        commandSender.sendMessage(Utils.chat(RoyaleEconomy.commandsCfg.getString("commands.reshop.shop-not-found")));
                                        return true;
                                    }
                                }
                                else{
                                    commandSender.sendMessage(Utils.chat(RoyaleEconomy.commandsCfg.getString("commands.reshop.player-not-found")));
                                    return true;
                                }
                            }
                        }
                        else if(args[1].equalsIgnoreCase("reload")){
                            ShopsLoad.loadShopItems();
                            ShopsLoad.sortItems();
                            commandSender.sendMessage(Utils.chat("&aReloaded items from file."));
                            return true;
                        }
                    }

                commandSender.sendMessage(Utils.chat("&c")+"/recurrency shops reload");
                commandSender.sendMessage(Utils.chat("&c")+"/recurrency shops forceopen <player> <shopName>");
                commandSender.sendMessage(Utils.chat("&c")+"/recurrency shops manager");
                commandSender.sendMessage(Utils.chat("&c")+"/recurrency shops list");
                return true;
            }
            else if(args[0].equalsIgnoreCase("give")){
                if(args.length>3){
                    Currency currency=MultiCurrencyHandler.findCurrencyById(args[3]);
                    if(currency==null){
                        commandSender.sendMessage(Utils.chat("&cCurrency not found"));
                        return false;
                    }

                    double amount;
                    try{
                        amount=Double.parseDouble(args[2]);
                    }catch(Exception x){
                        commandSender.sendMessage(Utils.chat("&cInvalid amount."));
                        return false;
                    }

                    String uuid=RoyaleEconomy.dataManager.getUUIDfromName(args[1]);
                    if(uuid==null){
                        commandSender.sendMessage(Utils.chat("&cPlayer was not found."));
                        return false;
                    }

                    currency.addAmount(uuid, amount);
                    Player p= Bukkit.getPlayer(UUID.fromString(uuid));
                    if(p!=null)
                        p.sendMessage(currency.getReceiveCurrencyMessage().replace("%amount%", RoyaleEconomy.messageHelper.numberFormat(amount)).replace("%player%", "Server"));
                    commandSender.sendMessage(Utils.chat("&aAdded "+amount+" "+currency.getCurrencyName()+" to player "+args[1]));
                    return false;
                }
            }
            else if(args[0].equalsIgnoreCase("giveSilent")){
                if(args.length>3){
                    Currency currency=MultiCurrencyHandler.findCurrencyById(args[3]);
                    if(currency==null){
                        commandSender.sendMessage(Utils.chat("&cCurrency not found"));
                        return false;
                    }

                    double amount;
                    try{
                        amount=Double.parseDouble(args[2]);
                    }catch(Exception x){
                        commandSender.sendMessage(Utils.chat("&cInvalid amount."));
                        return false;
                    }

                    String uuid=RoyaleEconomy.dataManager.getUUIDfromName(args[1]);
                    if(uuid==null){
                        commandSender.sendMessage(Utils.chat("&cPlayer was not found."));
                        return false;
                    }

                    currency.addAmount(uuid, amount);
                    //commandSender.sendMessage(Utils.chat("&aAdded "+amount+" "+currency.getCurrencyName()+" to player "+args[1]));
                    return false;
                }
            }
            else if(args[0].equalsIgnoreCase("remove")){
                if(args.length>3){
                    Currency currency=MultiCurrencyHandler.findCurrencyById(args[3]);
                    if(currency==null){
                        commandSender.sendMessage(Utils.chat("&cCurrency not found"));
                        return false;
                    }

                    double amount;
                    try{
                        amount=Double.parseDouble(args[2]);
                    }catch(Exception x){
                        commandSender.sendMessage(Utils.chat("&cInvalid amount."));
                        return false;
                    }

                    String uuid=RoyaleEconomy.dataManager.getUUIDfromName(args[1]);
                    if(uuid==null){
                        commandSender.sendMessage(Utils.chat("&cPlayer was not found."));
                        return false;
                    }

                    currency.removeAmount(uuid, amount);
                    commandSender.sendMessage(Utils.chat("&aRemoved "+amount+" "+currency.getCurrencyName()+" from player "+args[1]));
                    return false;
                }
            }
            else if(args[0].equalsIgnoreCase("set")){
                if(args.length>3){
                    Currency currency=MultiCurrencyHandler.findCurrencyById(args[3]);
                    if(currency==null){
                        commandSender.sendMessage(Utils.chat("&cCurrency not found"));
                        return false;
                    }

                    double amount;
                    try{
                        amount=Double.parseDouble(args[2]);
                    }catch(Exception x){
                        commandSender.sendMessage(Utils.chat("&cInvalid amount."));
                        return false;
                    }

                    String uuid=RoyaleEconomy.dataManager.getUUIDfromName(args[1]);
                    if(uuid==null){
                        commandSender.sendMessage(Utils.chat("&cPlayer was not found."));
                        return false;
                    }

                    currency.setAmount(uuid, amount);
                    commandSender.sendMessage(Utils.chat("&aSet "+amount+" "+currency.getCurrencyName()+" to player "+args[1]));
                    return false;
                }
            }
            else if(args[0].equalsIgnoreCase("balance")){
                if(args.length>2){
                    Currency currency=MultiCurrencyHandler.findCurrencyById(args[2]);
                    if(currency==null){
                        commandSender.sendMessage(Utils.chat("&cCurrency not found"));
                        return false;
                    }

                    String uuid=RoyaleEconomy.dataManager.getUUIDfromName(args[1]);
                    if(uuid==null){
                        commandSender.sendMessage(Utils.chat("&cPlayer was not found."));
                        return false;
                    }

                    double amount=currency.getAmount(uuid);
                    commandSender.sendMessage(Utils.chat("&a"+args[1]+" has "+RoyaleEconomy.messageHelper.numberFormat(amount)+" "+currency.getCurrencyName()));

                    return false;
                }
            }
            else if(args[0].equalsIgnoreCase("openExchange")){
                if(args.length>1){
                    Player player=Bukkit.getPlayerExact(args[1]);
                    if(player==null){
                        commandSender.sendMessage(Utils.chat("&cPlayer not found!"));
                        return false;
                    }

                    new CurrencyExchangeMainMenu(player);
                }
                return true;
            }
            else if(args[0].equalsIgnoreCase("openSell")){
                if(args.length>2){
                    Currency currency=MultiCurrencyHandler.findCurrencyById(args[2]);
                    if(currency==null){
                        commandSender.sendMessage(Utils.chat("&cCurrency not found"));
                        return false;
                    }

                    Player player=Bukkit.getPlayerExact(args[1]);
                    if(player==null){
                        commandSender.sendMessage(Utils.chat("&cPlayer not found!"));
                        return false;
                    }

                    if(!currency.getExchangePermission().equals("none") && !player.hasPermission(currency.getExchangePermission()) && !player.hasPermission(currency.getBypassExchangePermission())){
                        player.sendMessage(MultiCurrencyHandler.noPermissionMessage);
                        return false;
                    }

                    new CurrencyExchangeMenu(player, currency, true);
                    return false;
                }
            }
            else if(args[0].equalsIgnoreCase("openBuy")){
                if(args.length>2){
                    Currency currency=MultiCurrencyHandler.findCurrencyById(args[2]);
                    if(currency==null){
                        commandSender.sendMessage(Utils.chat("&cCurrency not found"));
                        return false;
                    }

                    Player player=Bukkit.getPlayerExact(args[1]);
                    if(player==null){
                        commandSender.sendMessage(Utils.chat("&cPlayer not found!"));
                        return false;
                    }

                    if(!currency.getExchangePermission().equals("none") && !player.hasPermission(currency.getExchangePermission()) && !player.hasPermission(currency.getBypassExchangePermission())){
                        player.sendMessage(MultiCurrencyHandler.noPermissionMessage);
                        return false;
                    }

                    new CurrencyExchangeMenu(player, currency, false);
                    return false;
                }
            }
            else if(args[0].equalsIgnoreCase("openExchangeCurrency")){
                if(args.length>2){
                    Currency currency=MultiCurrencyHandler.findCurrencyById(args[2]);
                    if(currency==null){
                        commandSender.sendMessage(Utils.chat("&cCurrency not found"));
                        return false;
                    }

                    Player player=Bukkit.getPlayerExact(args[1]);
                    if(player==null){
                        commandSender.sendMessage(Utils.chat("&cPlayer not found!"));
                        return false;
                    }

                    if(!currency.getExchangePermission().equals("none") && !player.hasPermission(currency.getExchangePermission()) && !player.hasPermission(currency.getBypassExchangePermission())){
                        player.sendMessage(MultiCurrencyHandler.noPermissionMessage);
                        return false;
                    }

                    new CurrencyExchangeSellBuyMenu(player, currency);
                    return false;
                }
            }
        }
        commandSender.sendMessage(Utils.chat("&c/recurrency give [player] [amount] [currencyId]"));
        commandSender.sendMessage(Utils.chat("&c/recurrency giveSilent [player] [amount] [currencyId]"));
        commandSender.sendMessage(Utils.chat("&c/recurrency remove [player] [amount] [currencyId]"));
        commandSender.sendMessage(Utils.chat("&c/recurrency set [player] [amount] [currencyId]"));
        commandSender.sendMessage(Utils.chat("&c/recurrency balance [player] [currencyId]"));
        commandSender.sendMessage(Utils.chat("&c/recurrency openExchange [player]"));
        commandSender.sendMessage(Utils.chat("&c/recurrency openExchangeCurrency [player] [currencyId]"));
        commandSender.sendMessage(Utils.chat("&c/recurrency openSell [player] [currencyId]"));
        commandSender.sendMessage(Utils.chat("&c/recurrency openBuy [player] [currencyId]"));
        commandSender.sendMessage(Utils.chat("&c")+"/recurrency shops");
        return false;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return null;
    }
}
