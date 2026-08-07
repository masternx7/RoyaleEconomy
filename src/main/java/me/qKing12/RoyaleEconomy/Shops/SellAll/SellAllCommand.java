package me.qKing12.RoyaleEconomy.Shops.SellAll;

import me.qKing12.RoyaleEconomy.Boosters.BoostersActive;
import me.qKing12.RoyaleEconomy.DataManager.StaticValues;
import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import me.qKing12.RoyaleEconomy.Shops.Shop;
import me.qKing12.RoyaleEconomy.Shops.ShopSellLimit;
import me.qKing12.RoyaleEconomy.Shops.ShopsLoad;
import me.qKing12.RoyaleEconomy.utils.PermissionChecker;
import me.qKing12.RoyaleEconomy.utils.PlayerMessageHandler;
import me.qKing12.RoyaleEconomy.utils.Utf8YamlConfiguration;
import me.qKing12.RoyaleEconomy.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static me.qKing12.RoyaleEconomy.Shops.SellAll.SellWand.wands;

public class SellAllCommand implements TabExecutor {
    private static ConcurrentHashMap<Player, Long> cooldowns=new ConcurrentHashMap<>();

    public static ItemStack[] getInventoryItems(Player player){
        if(Bukkit.getVersion().contains("1.8") && !Bukkit.getVersion().contains("1.21"))
            return player.getInventory().getContents();
        else
            return player.getInventory().getStorageContents();
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        ArrayList<String> values=new ArrayList<>();
        if(args.length==1) {
            values.add("");
            if(PermissionChecker.checkPermissionSilent("commands.royaleeconomy.shops-permission", sender)){
                values.add("forcesell");
                if(SellAllManager.useWands)
                    values.addAll(Arrays.asList("give", "wands", "reloadWands"));
            }
            return StringUtil.copyPartialMatches(args[0], values, new ArrayList<>());
        }
        else if(args.length==3 && args[0].equalsIgnoreCase("give")) {
            if(PermissionChecker.checkPermissionSilent("commands.royaleeconomy.shops-permission", sender)){
                for(SellWand wand : wands)
                    values.add(wand.getName());
            }
            return StringUtil.copyPartialMatches(args[2], values, new ArrayList<>());
        }
        return null;
    }

    public SellAllCommand(){
        //RoyaleEconomy.plugin.getCommand("recsellall").setExecutor(this);
        //RoyaleEconomy.plugin.getCommand("recsellall").setTabCompleter(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if(sender instanceof Player){
            Player p=(Player)sender;
            if(args.length>0){
                if(args[0].equalsIgnoreCase("help")){
                    p.sendMessage(Utils.chat("&a/sellall &7- &fSells items from inventory globally"));
                    p.sendMessage(Utils.chat("&a/sellall [shopName] &7- &fSells items from inventory to a specific shop"));
                    if(PermissionChecker.checkPermissionSilent("commands.royaleeconomy.shops-permission", p)) {
                        p.sendMessage(Utils.chat("&a/sellall forcesell &7- &fForce sells globally items from a player's inventory"));
                        if(SellAllManager.useWands) {
                            p.sendMessage(Utils.chat("&a/sellall wands &7- &fLists wands"));
                            p.sendMessage(Utils.chat("&a/sellall give <player> <wandType> [uses]"));
                            p.sendMessage(Utils.chat("&a/sellall reloadWands"));
                        }
                    }
                }
                else if(args[0].equalsIgnoreCase("wands")){
                    if(SellAllManager.useWands)
                        if(PermissionChecker.checkPermission("commands.royaleeconomy.shops-permission", p)){
                            new SellWandMenu(p);
                        }
                }
                else if(args[0].equalsIgnoreCase("reloadWands")) {
                    if (SellAllManager.useWands)
                        if (PermissionChecker.checkPermission("commands.royaleeconomy.shops-permission", p)) {
                            RoyaleEconomy.shopsCfg= Utf8YamlConfiguration.loadConfiguration(new File(RoyaleEconomy.plugin.getDataFolder(), "shops.yml"));
                            SellWand.loadWands();
                            p.sendMessage(Utils.chat("&aWands reloaded!"));
                        }
                }
                else if(args[0].equalsIgnoreCase("give")){
                    if(PermissionChecker.checkPermission("commands.royaleeconomy.shops-permission", p)){
                        if(args.length>2){
                            Player target=Bukkit.getPlayerExact(args[1]);
                            if(target!=null){
                                int uses=-1;
                                if(args.length>3){
                                    try{
                                        uses=Math.abs(Integer.parseInt(args[3]));
                                    }catch(Exception x){
                                        p.sendMessage(Utils.chat("&cInvalid number of uses."));
                                        return false;
                                    }
                                }
                                SellWand wand=SellWand.getWandByName(args[2]);
                                if(wand!=null){
                                    if(uses==-1)
                                        uses=wand.getDefaultUses();
                                    ItemStack wandToGive=wand.getWand(uses);
                                    if(target.getInventory().firstEmpty()==-1){
                                        target.getWorld().dropItemNaturally(target.getLocation(), wandToGive);
                                    }
                                    else
                                        target.getInventory().addItem(wandToGive);
                                    p.sendMessage(Utils.chat("&aWand has been sent!"));
                                }
                                else
                                    p.sendMessage(Utils.chat("&cWand not found."));
                            }
                            else{
                                p.sendMessage(Utils.chat("&cPlayer not found."));
                            }
                        }
                        else{
                            p.sendMessage(Utils.chat("&a/sellall give <player> <wandType> [uses]"));
                        }
                    }
                }
                else if(args[0].equalsIgnoreCase("forceSell")){
                    if(PermissionChecker.checkPermission("commands.royaleeconomy.shops-permission", p)){
                        if(args.length>1){
                            Player player= Bukkit.getPlayerExact(args[1]);
                            if(player!=null) {
                                double coins = 0;
                                int count = 0;
                                Shop randomShop = ShopsLoad.shops.get(0);
                                try {
                                    double sellLimit = ShopSellLimit.getLimit(player);
                                    if(sellLimit==-1) {
                                        for (ItemStack item : getInventoryItems(p)) {
                                            if (item != null && (SellAllManager.sellAllEnchanted || item.getEnchantments().size() == 0)) {
                                                Map.Entry<Double, Double> tempValue = ShopsLoad.getBothPrices(item, randomShop);
                                                if (tempValue.getKey() != 0) {
                                                    player.getInventory().removeItem(item);
                                                    coins += tempValue.getKey();
                                                    count += item.getAmount();
                                                    BoostersActive.Booster percent = RoyaleEconomy.boosters.getBoosterPercentShopSell(p, randomShop.getShopName());
                                                    if (percent != null) {
                                                        double value = BoostersActive.getValueFromPercent(tempValue.getKey(), percent.percent);
                                                        if (tempValue.getValue() > -1 && tempValue.getKey() + value > tempValue.getValue())
                                                            value = tempValue.getValue() - tempValue.getKey();
                                                        coins += value;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    else{
                                        double balance=ShopSellLimit.playerValues.getOrDefault(player.getUniqueId().toString(), 0d);
                                        for (ItemStack item : getInventoryItems(p)) {
                                            if (item != null && (SellAllManager.sellAllEnchanted || item.getEnchantments().size() == 0)) {
                                                Map.Entry<Double, Double> tempValue = ShopsLoad.getBothPrices(item, randomShop);
                                                if (tempValue.getKey() != 0) {
                                                    if(balance+coins+tempValue.getKey()>sellLimit){
                                                        ShopSellLimit.sendMessage(player, sellLimit);
                                                        p.sendMessage(Utils.chat("&cPlayer has exceeded their sell limit "+sellLimit));
                                                        break;
                                                    }
                                                    player.getInventory().removeItem(item);
                                                    coins += tempValue.getKey();
                                                    count += item.getAmount();
                                                    BoostersActive.Booster percent = RoyaleEconomy.boosters.getBoosterPercentShopSell(p, randomShop.getShopName());
                                                    if (percent != null) {
                                                        double value = BoostersActive.getValueFromPercent(tempValue.getKey(), percent.percent);
                                                        if (tempValue.getValue() > -1 && tempValue.getKey() + value > tempValue.getValue())
                                                            value = tempValue.getValue() - tempValue.getKey();
                                                        coins += value;
                                                    }
                                                }
                                            }
                                        }
                                        ShopSellLimit.playerValues.put(player.getUniqueId().toString(), coins+balance);
                                    }
                                } catch (Exception x) {
                                    x.printStackTrace();
                                }
                                if (coins != 0) {
                                    RoyaleEconomy.dataManager.addMoneyToFile(player.getUniqueId().toString(), coins);
                                    PlayerMessageHandler.messageSend(player, SellAllManager.sellMessage.replace("%item-count%", RoyaleEconomy.messageHelper.numberFormat((double) count)).replace("%coins%", RoyaleEconomy.messageHelper.numberFormat(coins)));
                                } else {
                                    PlayerMessageHandler.messageSend(player, SellAllManager.noItemsToSell);
                                }
                                p.sendMessage(Utils.chat("&aForced selling on "+player.getName()+"'s inventory!"));
                            }
                            else
                                p.sendMessage(Utils.chat("&cPlayer not found!"));
                        }
                        else
                            p.sendMessage(Utils.chat("&a/sellall forcesell <player> &7- &fForcefully triggers a global sellall on player's inventory"));
                    }
                }
                else {
                    if (p.hasPermission(SellAllManager.globalPermission) || p.hasPermission("rec.shop.sellall." + args[0])) {
                        Shop shop=RoyaleEconomy.apiHandler.shops.getShopByName(args[0]);
                        if(shop==null){
                            PlayerMessageHandler.messageSend(p, SellAllManager.noItemsToSell);
                        }
                        else {
                            if (SellAllManager.commandCooldown!=0 && !p.hasPermission(SellAllManager.cooldownBypassPerm)) {
                                if (cooldowns.containsKey(p)) {
                                    if (ZonedDateTime.now().toInstant().toEpochMilli() < cooldowns.get(p)) {
                                        PlayerMessageHandler.messageSend(p, SellAllManager.cooldownMessage.replace("%cooldown%", RoyaleEconomy.messageHelper.formatTimeShort(cooldowns.get(p) - ZonedDateTime.now().toInstant().toEpochMilli())));
                                        return false;
                                    } //else if (SellAllManager.commandCooldown == 0)
                                       // cooldowns.remove(p);
                                }
                                cooldowns.put(p, ZonedDateTime.now().toInstant().toEpochMilli() + SellAllManager.commandCooldown);
                            }
                            double coins = 0;
                            int count = 0;
                            try {
                                double sellLimit = ShopSellLimit.getLimit(p);
                                if(sellLimit==-1) {
                                    for (ItemStack item : getInventoryItems(p)) {
                                        if (item != null && (SellAllManager.sellAllEnchanted || item.getEnchantments().size() == 0)) {
                                            Map.Entry<Double, Double> tempValue = ShopsLoad.getBothPricesShopOnly(item, shop);
                                            if (tempValue.getKey() != 0) {
                                                p.getInventory().removeItem(item);
                                                coins += tempValue.getKey();
                                                count += item.getAmount();
                                                BoostersActive.Booster percent = RoyaleEconomy.boosters.getBoosterPercentShopSell(p, shop.getShopName());
                                                if (percent != null) {
                                                    double value = BoostersActive.getValueFromPercent(tempValue.getKey(), percent.percent);
                                                    if (tempValue.getValue() > -1 && tempValue.getKey() + value > tempValue.getValue())
                                                        value = tempValue.getValue() - tempValue.getKey();
                                                    coins += value;
                                                }
                                            }
                                        }
                                    }
                                }
                                else{
                                    double balance=ShopSellLimit.playerValues.getOrDefault(p.getUniqueId().toString(), 0d);
                                    for (ItemStack item : getInventoryItems(p)) {
                                        if (item != null && (SellAllManager.sellAllEnchanted || item.getEnchantments().size() == 0)) {
                                            Map.Entry<Double, Double> tempValue = ShopsLoad.getBothPricesShopOnly(item, shop);
                                            if (tempValue.getKey() != 0) {
                                                if(balance+coins+tempValue.getKey()>sellLimit){
                                                    ShopSellLimit.sendMessage(p, sellLimit);
                                                    break;
                                                }
                                                p.getInventory().removeItem(item);
                                                coins += tempValue.getKey();
                                                count += item.getAmount();
                                                BoostersActive.Booster percent = RoyaleEconomy.boosters.getBoosterPercentShopSell(p, shop.getShopName());
                                                if (percent != null) {
                                                    double value = BoostersActive.getValueFromPercent(tempValue.getKey(), percent.percent);
                                                    if (tempValue.getValue() > -1 && tempValue.getKey() + value > tempValue.getValue())
                                                        value = tempValue.getValue() - tempValue.getKey();
                                                    coins += value;
                                                }
                                            }
                                        }
                                    }
                                    ShopSellLimit.playerValues.put(p.getUniqueId().toString(), coins+balance);
                                }
                            } catch (Exception x) {
                                x.printStackTrace();
                            }
                            if (coins != 0) {
                                RoyaleEconomy.dataManager.addMoneyToFile(p.getUniqueId().toString(), coins);
                                PlayerMessageHandler.messageSend(p, SellAllManager.sellMessage.replace("%item-count%", RoyaleEconomy.messageHelper.numberFormat((double) count)).replace("%coins%", RoyaleEconomy.messageHelper.numberFormat(coins)));
                            } else {
                                PlayerMessageHandler.messageSend(p, SellAllManager.noItemsToSell);
                            }
                        }
                    } else
                        PlayerMessageHandler.messageSend(p, SellAllManager.noLocalPermission);
                }
            }
            else{
                if(SellAllManager.globalPermission.equalsIgnoreCase("none") || p.hasPermission(SellAllManager.globalPermission)){
                    //if(!Bukkit.getVersion().contains("1.8") && !p.getInventory().getItemInOffHand().getType().equals(Material.AIR)){
                    //    p.sendMessage(RoyaleEconomy.staticValues.canNotSell);
                    //    return false;
                    //}
                    if(SellAllManager.commandCooldown!=0 && !p.hasPermission(SellAllManager.cooldownBypassPerm)) {
                        if (cooldowns.containsKey(p)) {
                            if (ZonedDateTime.now().toInstant().toEpochMilli() < cooldowns.get(p)) {
                                PlayerMessageHandler.messageSend(p, SellAllManager.cooldownMessage.replace("%cooldown%", RoyaleEconomy.messageHelper.formatTimeShort(cooldowns.get(p) - ZonedDateTime.now().toInstant().toEpochMilli())));
                                return false;
                            } //else if (SellAllManager.commandCooldown == 0)
                                //cooldowns.remove(p);
                        }
                        cooldowns.put(p, ZonedDateTime.now().toInstant().toEpochMilli() + SellAllManager.commandCooldown);
                    }
                    double coins=0;
                    int count=0;
                    Shop randomShop=ShopsLoad.shops.get(0);
                    try{
                        double sellLimit = ShopSellLimit.getLimit(p);
                        if(sellLimit==-1) {
                            for (ItemStack item : getInventoryItems(p)) {
                                if (item != null && (SellAllManager.sellAllEnchanted || item.getEnchantments().size() == 0)) {
                                    Map.Entry<Double, Double> tempValue = ShopsLoad.getBothPrices(item, randomShop);
                                    if (tempValue.getKey() != 0) {
                                        p.getInventory().removeItem(item);
                                        coins += tempValue.getKey();
                                        count += item.getAmount();
                                        BoostersActive.Booster percent = RoyaleEconomy.boosters.getBoosterPercentShopSell(p, randomShop.getShopName());
                                        if (percent != null) {
                                            double value = BoostersActive.getValueFromPercent(tempValue.getKey(), percent.percent);
                                            if (tempValue.getValue() > -1 && tempValue.getKey() + value > tempValue.getValue())
                                                value = tempValue.getValue() - tempValue.getKey();
                                            coins += value;
                                        }
                                    }
                                }
                            }
                        }
                        else{
                            double balance=ShopSellLimit.playerValues.getOrDefault(p.getUniqueId().toString(), 0d);
                            for (ItemStack item : getInventoryItems(p)) {
                                if (item != null && (SellAllManager.sellAllEnchanted || item.getEnchantments().size() == 0)) {
                                    Map.Entry<Double, Double> tempValue = ShopsLoad.getBothPrices(item, randomShop);
                                    if (tempValue.getKey() != 0) {
                                        if(balance+coins+tempValue.getKey()>sellLimit){
                                            ShopSellLimit.sendMessage(p, sellLimit);
                                            break;
                                        }
                                        p.getInventory().removeItem(item);
                                        coins += tempValue.getKey();
                                        count += item.getAmount();
                                        BoostersActive.Booster percent = RoyaleEconomy.boosters.getBoosterPercentShopSell(p, randomShop.getShopName());
                                        if (percent != null) {
                                            double value = BoostersActive.getValueFromPercent(tempValue.getKey(), percent.percent);
                                            if (tempValue.getValue() > -1 && tempValue.getKey() + value > tempValue.getValue())
                                                value = tempValue.getValue() - tempValue.getKey();
                                            coins += value;
                                        }
                                    }
                                }
                            }
                            ShopSellLimit.playerValues.put(p.getUniqueId().toString(), coins+balance);
                        }
                    }catch (Exception x){
                        x.printStackTrace();
                    }
                    if(coins!=0){
                        RoyaleEconomy.dataManager.addMoneyToFile(p.getUniqueId().toString(), coins);
                        PlayerMessageHandler.messageSend(p, SellAllManager.sellMessage.replace("%item-count%", RoyaleEconomy.messageHelper.numberFormat((double)count)).replace("%coins%", RoyaleEconomy.messageHelper.numberFormat(coins)));
                    }
                    else{
                        PlayerMessageHandler.messageSend(p, SellAllManager.noItemsToSell);
                    }
                }
                else
                    PlayerMessageHandler.messageSend(p, SellAllManager.noGlobalPermission);
            }
        }
        else{
            if(args.length>0 && args[0].equalsIgnoreCase("give")){
                if(args.length>2){
                    Player target=Bukkit.getPlayerExact(args[1]);
                    if(target!=null){
                        int uses=-1;
                        if(args.length>3){
                            try{
                                uses=Math.abs(Integer.parseInt(args[3]));
                            }catch(Exception x){
                                sender.sendMessage(Utils.chat("&cInvalid number of uses."));
                                return false;
                            }
                        }
                        SellWand wand=SellWand.getWandByName(args[2]);
                        if(wand!=null){
                            if(uses==-1)
                                uses=wand.getDefaultUses();
                            ItemStack wandToGive=wand.getWand(uses);
                            if(target.getInventory().firstEmpty()==-1){
                                target.getWorld().dropItemNaturally(target.getLocation(), wandToGive);
                            }
                            else
                                target.getInventory().addItem(wandToGive);
                        }
                        else
                            sender.sendMessage(Utils.chat("&cWand not found."));
                    }
                    else{
                        sender.sendMessage(Utils.chat("&cPlayer not found."));
                    }
                }
                else{
                    sender.sendMessage(Utils.chat("&a/sellall give <player> <wandType> [uses]"));
                }
            }
            else if(args.length>0 && args[0].equalsIgnoreCase("forcesell")){
                if(args.length>1){
                    Player player= Bukkit.getPlayerExact(args[1]);
                    if(player!=null) {
                        double coins = 0;
                        int count = 0;
                        Shop randomShop = ShopsLoad.shops.get(0);
                        try {
                            double sellLimit = ShopSellLimit.getLimit(player);
                            if(sellLimit==-1) {
                                for (ItemStack item : getInventoryItems(player)) {
                                    if (item != null && (SellAllManager.sellAllEnchanted || item.getEnchantments().size() == 0)) {
                                        Map.Entry<Double, Double> tempValue = ShopsLoad.getBothPrices(item, randomShop);
                                        if (tempValue.getKey() != 0) {
                                            player.getInventory().removeItem(item);
                                            coins += tempValue.getKey();
                                            count += item.getAmount();
                                            BoostersActive.Booster percent = RoyaleEconomy.boosters.getBoosterPercentShopSell(player, randomShop.getShopName());
                                            if (percent != null) {
                                                double value = BoostersActive.getValueFromPercent(tempValue.getKey(), percent.percent);
                                                if (tempValue.getValue() > -1 && tempValue.getKey() + value > tempValue.getValue())
                                                    value = tempValue.getValue() - tempValue.getKey();
                                                coins += value;
                                            }
                                        }
                                    }
                                }
                            }
                            else{
                                double balance=ShopSellLimit.playerValues.getOrDefault(player.getUniqueId().toString(), 0d);
                                for (ItemStack item : getInventoryItems(player)) {
                                    if (item != null && (SellAllManager.sellAllEnchanted || item.getEnchantments().size() == 0)) {
                                        Map.Entry<Double, Double> tempValue = ShopsLoad.getBothPrices(item, randomShop);
                                        if (tempValue.getKey() != 0) {
                                            if(balance+coins+tempValue.getKey()>sellLimit){
                                                ShopSellLimit.sendMessage(player, sellLimit);
                                                sender.sendMessage(Utils.chat("&cPlayer has exceeded their sell limit "+sellLimit));
                                                break;
                                            }
                                            player.getInventory().removeItem(item);
                                            coins += tempValue.getKey();
                                            count += item.getAmount();
                                            BoostersActive.Booster percent = RoyaleEconomy.boosters.getBoosterPercentShopSell(player, randomShop.getShopName());
                                            if (percent != null) {
                                                double value = BoostersActive.getValueFromPercent(tempValue.getKey(), percent.percent);
                                                if (tempValue.getValue() > -1 && tempValue.getKey() + value > tempValue.getValue())
                                                    value = tempValue.getValue() - tempValue.getKey();
                                                coins += value;
                                            }
                                        }
                                    }
                                }
                                ShopSellLimit.playerValues.put(player.getUniqueId().toString(), coins+balance);
                            }
                        } catch (Exception x) {
                            x.printStackTrace();
                        }
                        if (coins != 0) {
                            RoyaleEconomy.dataManager.addMoneyToFile(player.getUniqueId().toString(), coins);
                            PlayerMessageHandler.messageSend(player, SellAllManager.sellMessage.replace("%item-count%", RoyaleEconomy.messageHelper.numberFormat((double) count)).replace("%coins%", RoyaleEconomy.messageHelper.numberFormat(coins)));
                        } else {
                            PlayerMessageHandler.messageSend(player, SellAllManager.noItemsToSell);
                        }
                        sender.sendMessage(Utils.chat("&aForced selling on "+player.getName()+"'s inventory!"));
                    }
                    else
                        sender.sendMessage(Utils.chat("&cPlayer not found!"));
                }
                else
                    sender.sendMessage(Utils.chat("&a/sellall forcesell <player> &7- &fForcefully triggers a global sellall on player's inventory"));
            }
            else
                sender.sendMessage("You can only use the give and forcesell commands here.");
        }
        return false;
    }
}
