package me.qKing12.RoyaleEconomy.MultiCurrencyShops;

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

    public static Shop.ShopItem getShopItem(ItemStack item, Shop shop){
        item=item.clone();
        try {
            if (item.getType().getMaxDurability() > 0)
                item.setDurability((short)0);
        }catch (Exception x){
            x.printStackTrace();
        }
        for(Shop.ShopItem shopItem : shop.getExclusiveItems()){
            if(shopItem.getDetailed()){
                if(item.isSimilar(shopItem.getItemStack().clone()))
                    return shopItem;
            }
            else{
                if(RoyaleEconomy.upperVersion){
                    if((!shopItem.getCustomDataModel() || hasCustomModelData(item, shopItem.getItemStack())) && item.getType().equals(shopItem.getItemStack().getType()))
                        return shopItem;
                }
                else{
                    if(item.getData().equals(shopItem.getItemStack().getData()))
                        return shopItem;
                }
            }
        }
        for(Shop.ShopItem shopItem : shopItems){
            if(shopItem.getDetailed()){
                if(item.isSimilar(shopItem.getItemStack().clone()))
                    return shopItem;
            }
            else{
                if(RoyaleEconomy.upperVersion){
                    if((!shopItem.getCustomDataModel() || hasCustomModelData(item, shopItem.getItemStack())) && item.getType().equals(shopItem.getItemStack().getType()))
                        return shopItem;
                }
                else{
                    if(item.getData().equals(shopItem.getItemStack().getData()))
                        return shopItem;
                }
            }
        }
        return null;
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
        File directory = new File(RoyaleEconomy.plugin.getDataFolder(), "shopsMultiCurrency");
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
        File directory = new File(RoyaleEconomy.plugin.getDataFolder(), "shopsMultiCurrency");
        if(!directory.exists())
            directory.mkdir();

        loadShopItems();
        sortItems();
    }

}
