package me.qKing12.RoyaleEconomy.Shops.SellAll;

import de.tr7zw.changeme.nbtapi.NBTItem;
import me.qKing12.RoyaleEconomy.API.Shops;
import me.qKing12.RoyaleEconomy.Boosters.BoostersActive;
import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import me.qKing12.RoyaleEconomy.Shops.Shop;
import me.qKing12.RoyaleEconomy.Shops.ShopSellLimit;
import me.qKing12.RoyaleEconomy.Shops.ShopsLoad;
import me.qKing12.RoyaleEconomy.utils.PlayerMessageHandler;
import me.qKing12.RoyaleEconomy.utils.Utils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SellWand {
    static ArrayList<SellWand> wands = new ArrayList<>();
    private ConcurrentHashMap<Player, Long> cooldowns=new ConcurrentHashMap<>();

    public static void loadWands(){
        wands=new ArrayList<>();
        for(String key : RoyaleEconomy.shopsCfg.getConfigurationSection("sell-all.sell-wands").getKeys(false))
            new SellWand(RoyaleEconomy.shopsCfg.getConfigurationSection("sell-all.sell-wands."+key), key);
    }

    public static SellWand getWandByName(String name){
        for(SellWand wand : wands)
            if(wand.name.equalsIgnoreCase(name))
                return wand;
        return null;
    }

    private String name;
    private int defaultUses;
    private ItemStack defaultWand;
    private long cooldownSeconds;

    private ArrayList<Shop> shopsToCheck=new ArrayList<>();

    public SellWand(ConfigurationSection section, String key){
        this.name=key;
        this.defaultUses=section.getInt("uses");

        ArrayList<String> lore=new ArrayList<>();
        for(String line : section.getStringList("lore"))
            lore.add(Utils.chat(line));
        defaultWand=RoyaleEconomy.itemConstructor.getItem(section.getString("material"), Utils.chat(section.getString("name")), lore);

        NBTItem nbtItem=new NBTItem(defaultWand);
        nbtItem.setString("RECSellWand", name);
        defaultWand=nbtItem.getItem();

        for(String shopName : section.getStringList("shops-to-check")){
            Shop shop= RoyaleEconomy.apiHandler.shops.getShopByName(shopName);
            if(shop!=null)
                shopsToCheck.add(shop);
        }

        cooldownSeconds=section.getInt("cooldown")*1000;

        wands.add(this);
    }

    public String getName(){
        return name;
    }

    public int getDefaultUses() {
        return defaultUses;
    }

    public ItemStack getWand(int uses){
        ItemStack wand=defaultWand.clone();
        ItemMeta meta=wand.getItemMeta();
        String usesString=RoyaleEconomy.messageHelper.numberFormat((double)uses);
        meta.setDisplayName(meta.getDisplayName().replace("%uses%", usesString));
        ArrayList<String> lore=new ArrayList<>();
        for(String line : meta.getLore())
            lore.add(line.replace("%uses%", usesString));
        lore.forEach(line -> line=line.replace("%uses%", usesString));
        meta.setLore(lore);
        wand.setItemMeta(meta);
        NBTItem nbtItem=new NBTItem(wand);
        nbtItem.setInteger("RECUses", uses);
        nbtItem.setUUID("REC_Unstackable", UUID.randomUUID());
        return nbtItem.getItem();
    }

    public boolean sellItems(Inventory inventory, Player p) {
        if(cooldownSeconds!=0 && !p.hasPermission(SellAllManager.cooldownBypassPerm)) {
            if (cooldowns.containsKey(p)) {
                if (ZonedDateTime.now().toInstant().toEpochMilli() < cooldowns.get(p)) {
                    PlayerMessageHandler.messageSend(p, SellAllManager.cooldownMessage.replace("%cooldown%", RoyaleEconomy.messageHelper.formatTimeShort(cooldowns.get(p) - ZonedDateTime.now().toInstant().toEpochMilli())));
                    return false;
                } //else
                    //SellAllManager.cooldowns.remove(p);
            }
            cooldowns.put(p, ZonedDateTime.now().toInstant().toEpochMilli() + cooldownSeconds);
        }
        if (shopsToCheck.isEmpty()) {
            if (SellAllManager.globalPermission.equalsIgnoreCase("none") || p.hasPermission(SellAllManager.globalPermission)) {
                double coins = 0;
                int count = 0;
                Shop randomShop = ShopsLoad.shops.get(0);
                try {
                    double sellLimit = ShopSellLimit.getLimit(p);
                    if(sellLimit==-1) {
                        for (ItemStack item : inventory) {
                            if (item != null && (SellAllManager.sellAllEnchanted || item.getEnchantments().size() == 0)) {
                                Map.Entry<Double, Double> tempValue = ShopsLoad.getBothPrices(item, randomShop);
                                if (tempValue.getKey() != 0) {
                                    inventory.removeItem(item);
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
                        for (ItemStack item : inventory) {
                            if (item != null && (SellAllManager.sellAllEnchanted || item.getEnchantments().size() == 0)) {
                                Map.Entry<Double, Double> tempValue = ShopsLoad.getBothPrices(item, randomShop);
                                if (tempValue.getKey() != 0) {
                                    if(balance+coins+tempValue.getKey()>sellLimit){
                                        ShopSellLimit.sendMessage(p, sellLimit);
                                        break;
                                    }
                                    inventory.removeItem(item);
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
                } catch (Exception x) {
                    x.printStackTrace();
                }
                if (coins != 0) {
                    RoyaleEconomy.dataManager.addMoneyToFile(p.getUniqueId().toString(), coins);
                    PlayerMessageHandler.messageSend(p, SellAllManager.sellMessage.replace("%item-count%", RoyaleEconomy.messageHelper.numberFormat((double) count)).replace("%coins%", RoyaleEconomy.messageHelper.numberFormat(coins)));
                    return true;
                } else {
                    PlayerMessageHandler.messageSend(p, SellAllManager.noItemsToSell);
                }
            } else
                PlayerMessageHandler.messageSend(p, SellAllManager.noGlobalPermission);
        } else {
            double coins = 0;
            int count = 0;

            for (Shop shop : shopsToCheck) {
                if (SellAllManager.globalPermission.equalsIgnoreCase("none") || p.hasPermission(SellAllManager.globalPermission) || p.hasPermission("rec.shop.sellall." + shop.getShopName())) {
                    try {
                        double sellLimit = ShopSellLimit.getLimit(p);
                        if(sellLimit==-1) {
                            for (ItemStack item : inventory) {
                                if (item != null && (SellAllManager.sellAllEnchanted || item.getEnchantments().size() == 0)) {
                                    Map.Entry<Double, Double> tempValue = ShopsLoad.getBothPricesShopOnly(item, shop);
                                    if (tempValue.getKey() != 0) {
                                        inventory.removeItem(item);
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
                            for (ItemStack item : inventory) {
                                if (item != null && (SellAllManager.sellAllEnchanted || item.getEnchantments().size() == 0)) {
                                    Map.Entry<Double, Double> tempValue = ShopsLoad.getBothPricesShopOnly(item, shop);
                                    if (tempValue.getKey() != 0) {
                                        if(balance+coins+tempValue.getKey()>sellLimit){
                                            ShopSellLimit.sendMessage(p, sellLimit);
                                            break;
                                        }
                                        inventory.removeItem(item);
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
                }
            }
            if (coins != 0) {
                RoyaleEconomy.dataManager.addMoneyToFile(p.getUniqueId().toString(), coins);
                PlayerMessageHandler.messageSend(p, SellAllManager.sellMessage.replace("%item-count%", RoyaleEconomy.messageHelper.numberFormat((double) count)).replace("%coins%", RoyaleEconomy.messageHelper.numberFormat(coins)));
                return true;
            } else {
                PlayerMessageHandler.messageSend(p, SellAllManager.noItemsToSell);
            }
        }
        return false;
    }

}
