package me.qKing12.RoyaleEconomy.Shops;

import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;



public class ShopsLoad {
    public static ArrayList<Shop> shops = new ArrayList<>();
    public static ArrayList<Shop.ShopItem> shopItems = new ArrayList<>();
    private static File log;
    private static DateFormat simple = new SimpleDateFormat("[MMM:dd:hh:ss] ");

    public static void injectToLog(String inject){
        if(log==null)
            return;
        RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runAsync((task) -> {
            if(!log.exists())
                try {
                    log.createNewFile();
                }catch(Exception e){
                    e.printStackTrace();
                }
            try {
                FileWriter fileWriter = new FileWriter(log, true);
                PrintWriter printWriter = new PrintWriter(fileWriter);
                printWriter.println(simple.format(new Date())+inject);
                printWriter.close();
            }catch(Exception e){
                e.printStackTrace();
            }
        });
    }

    public static void sortItems(){
        shopItems=(ArrayList<Shop.ShopItem>)shopItems.stream().sorted(Comparator.comparing(Shop.ShopItem::getDetailed,Comparator.reverseOrder())).collect(Collectors.toList());
        try{
            shopItems=(ArrayList<Shop.ShopItem>)shopItems.stream().sorted(Comparator.comparing(Shop.ShopItem::getCustomDataModel,Comparator.reverseOrder())).collect(Collectors.toList());
        }catch (Exception x){

        }
    }

    private static boolean hasCustomModelData(ItemStack item1, ItemStack item2){
        try {
             return item1.getItemMeta().getCustomModelData()==item2.getItemMeta().getCustomModelData();
        }catch(Exception x){
            return false;
        }
    }

    public static double getPrice(ItemStack item, Shop shop){
        item=item.clone();
        int multipler=item.getAmount();
        try {
            if (item.getType().getMaxDurability() > 0)
                item.setDurability((short)0);
        }catch (Exception x){
            x.printStackTrace();
        }
        for(Shop.ShopItem shopItem : shop.getExclusiveItems()){
            if(shopItem.getDetailed()){
                if(item.isSimilar(shopItem.getItemStack().clone()))
                    return shopItem.getSellingValue()*multipler;
            }
            else{
                if(RoyaleEconomy.upperVersion){
                    if((!shopItem.getCustomDataModel() || hasCustomModelData(item, shopItem.getItemStack())) && item.getType().equals(shopItem.getItemStack().getType()))
                        return shopItem.getSellingValue()*multipler;
                }
                else{
                    if(item.getData().equals(shopItem.getItemStack().getData()))
                        return shopItem.getSellingValue()*multipler;
                }
            }
        }
        for(Shop.ShopItem shopItem : shopItems){
            if(shopItem.getDetailed()){
                if(item.isSimilar(shopItem.getItemStack().clone()))
                    return shopItem.getSellingValue()*multipler;
            }
            else{
                if(RoyaleEconomy.upperVersion){
                    if((!shopItem.getCustomDataModel() || hasCustomModelData(item, shopItem.getItemStack())) && item.getType().equals(shopItem.getItemStack().getType()))
                        return shopItem.getSellingValue()*multipler;
                }
                else{
                    if(item.getData().equals(shopItem.getItemStack().getData()))
                        return shopItem.getSellingValue()*multipler;
                }
            }
        }
        return 0;
    }

    public static Map.Entry<Double, Double> getBothPricesShopOnly(ItemStack item, Shop shop){
        item=item.clone();
        int multipler=item.getAmount();
        try {
            if (item.getType().getMaxDurability() > 0) {
                item.setDurability((short) 0);
            }
        }catch(Exception x){
            x.printStackTrace();
        }
        for(Shop.ShopItem shopItem : shop.getExclusiveItems()){
            if(shopItem.getDetailed()){
                if(item.isSimilar(shopItem.getItemStack().clone()))
                    return new AbstractMap.SimpleEntry<>(shopItem.getSellingValue()*multipler, shopItem.getBuyValue()*multipler);
            }
            else{
                if(RoyaleEconomy.upperVersion){
                    if((!shopItem.getCustomDataModel() || hasCustomModelData(item, shopItem.getItemStack())) && item.getType().equals(shopItem.getItemStack().getType()))
                        return new AbstractMap.SimpleEntry<>(shopItem.getSellingValue()*multipler, shopItem.getBuyValue()*multipler);
                }
                else{
                    if(item.getData().equals(shopItem.getItemStack().getData()))
                        return new AbstractMap.SimpleEntry<>(shopItem.getSellingValue()*multipler, shopItem.getBuyValue()*multipler);
                }
            }
        }
        for(Shop.ShopItem shopItem : shop.getNotExclusiveItems()){
            if(shopItem.getDetailed()){
                if(item.isSimilar(shopItem.getItemStack().clone()))
                    return new AbstractMap.SimpleEntry<>(shopItem.getSellingValue()*multipler, shopItem.getBuyValue()*multipler);
            }
            else{
                if(RoyaleEconomy.upperVersion){
                    if((!shopItem.getCustomDataModel() || hasCustomModelData(item, shopItem.getItemStack())) && item.getType().equals(shopItem.getItemStack().getType()))
                        return new AbstractMap.SimpleEntry<>(shopItem.getSellingValue()*multipler, shopItem.getBuyValue()*multipler);
                }
                else{
                    if(item.getData().equals(shopItem.getItemStack().getData()))
                        return new AbstractMap.SimpleEntry<>(shopItem.getSellingValue()*multipler, shopItem.getBuyValue()*multipler);
                }
            }
        }
        return new AbstractMap.SimpleEntry<>(0d,0d);
    }

    public static double getPriceShopOnly(ItemStack item, Shop shop){
        item=item.clone();
        int multipler=item.getAmount();
        try {
            if (item.getType().getMaxDurability() > 0)
                item.setDurability((short)0);
        }catch (Exception x){
            x.printStackTrace();
        }
        for(Shop.ShopItem shopItem : shop.getExclusiveItems()){
            if(shopItem.getDetailed()){
                if(item.isSimilar(shopItem.getItemStack().clone()))
                    return shopItem.getSellingValue()*multipler;
            }
            else{
                if(RoyaleEconomy.upperVersion){
                    if((!shopItem.getCustomDataModel() || hasCustomModelData(item, shopItem.getItemStack())) && item.getType().equals(shopItem.getItemStack().getType()))
                        return shopItem.getSellingValue()*multipler;
                }
                else{
                    if(item.getData().equals(shopItem.getItemStack().getData()))
                        return shopItem.getSellingValue()*multipler;
                }
            }
        }
        for(Shop.ShopItem shopItem : shop.getNotExclusiveItems()){
            if(shopItem.getDetailed()){
                if(item.isSimilar(shopItem.getItemStack().clone()))
                    return shopItem.getSellingValue()*multipler;
            }
            else{
                if(RoyaleEconomy.upperVersion){
                    if((!shopItem.getCustomDataModel() || hasCustomModelData(item, shopItem.getItemStack())) && item.getType().equals(shopItem.getItemStack().getType()))
                        return shopItem.getSellingValue()*multipler;
                }
                else{
                    if(item.getData().equals(shopItem.getItemStack().getData()))
                        return shopItem.getSellingValue()*multipler;
                }
            }
        }
        return 0;
    }

    public static Map.Entry<Double, Double> getBothPrices(ItemStack item, Shop shop){
        item=item.clone();
        int multipler=item.getAmount();
        try {
            if (item.getType().getMaxDurability() > 0) {
                item.setDurability((short) 0);
            }
        }catch(Exception x){
            x.printStackTrace();
        }
        for(Shop.ShopItem shopItem : shop.getExclusiveItems()){
            if(shopItem.getDetailed()){
                if(item.isSimilar(shopItem.getItemStack().clone()))
                    return new AbstractMap.SimpleEntry<>(shopItem.getSellingValue()*multipler, shopItem.getBuyValue()*multipler);
            }
            else{
                if(RoyaleEconomy.upperVersion){
                    if((!shopItem.getCustomDataModel() || hasCustomModelData(item, shopItem.getItemStack())) && item.getType().equals(shopItem.getItemStack().getType()))
                        return new AbstractMap.SimpleEntry<>(shopItem.getSellingValue()*multipler, shopItem.getBuyValue()*multipler);
                }
                else{
                    if(item.getData().equals(shopItem.getItemStack().getData()))
                        return new AbstractMap.SimpleEntry<>(shopItem.getSellingValue()*multipler, shopItem.getBuyValue()*multipler);
                }
            }
        }
        for(Shop.ShopItem shopItem : shopItems){
            if(shopItem.getDetailed()){
                if(item.isSimilar(shopItem.getItemStack().clone()))
                    return new AbstractMap.SimpleEntry<>(shopItem.getSellingValue()*multipler, shopItem.getBuyValue()*multipler);
            }
            else{
                if(RoyaleEconomy.upperVersion){
                    if((!shopItem.getCustomDataModel() || hasCustomModelData(item, shopItem.getItemStack())) && item.getType().equals(shopItem.getItemStack().getType()))
                        return new AbstractMap.SimpleEntry<>(shopItem.getSellingValue()*multipler, shopItem.getBuyValue()*multipler);
                }
                else{
                    if(item.getData().equals(shopItem.getItemStack().getData()))
                        return new AbstractMap.SimpleEntry<>(shopItem.getSellingValue()*multipler, shopItem.getBuyValue()*multipler);
                }
            }
        }
        return new AbstractMap.SimpleEntry<>(0d,0d);
    }

    public static void loadShopItems(){
        File directory = new File(RoyaleEconomy.plugin.getDataFolder(), "shops");
        shopItems=new ArrayList<>();
        shops=new ArrayList<>();
        if(directory.listFiles()!=null)
            for(File shop : directory.listFiles()) {
                Shop shopToAdd = new Shop(shop, null);
                shops.add(shopToAdd);
                shopItems.addAll(shopToAdd.getNotExclusiveItems());
            }
    }

    public ShopsLoad(){
        File directory = new File(RoyaleEconomy.plugin.getDataFolder(), "shops");
        if(!directory.exists())
            directory.mkdir();
        if(RoyaleEconomy.shopsCfg.getBoolean("log-shop-activity"))
            log=new File(RoyaleEconomy.plugin.getDataFolder(), "shopLogs.txt");


        loadShopItems();
        sortItems();
    }

}
