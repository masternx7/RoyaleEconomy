package me.qKing12.RoyaleEconomy.API;

import me.qKing12.RoyaleEconomy.Boosters.BoostersActive;
import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import me.qKing12.RoyaleEconomy.Shops.SellAll.SellAllManager;
import me.qKing12.RoyaleEconomy.Shops.Shop;
import me.qKing12.RoyaleEconomy.Shops.ShopMenu;
import me.qKing12.RoyaleEconomy.Shops.ShopsLoad;
import me.qKing12.RoyaleEconomy.utils.PlayerMessageHandler;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Map;

public class Shops {

    public ArrayList<Shop> getShopsList(){
        return ShopsLoad.shops;
    }

    public Shop getShopByName(String name){
        for(Shop shop : ShopsLoad.shops)
            if(shop.getShopName().equalsIgnoreCase(name))
                return shop;
        return null;
    }

    public boolean openShopToPlayer(Player player, String shopName){
        Shop shop=getShopByName(shopName);
        if(shop==null)
            return false;
        new ShopMenu(player, shop, 0, null);
        return true;
    }

    public double getPriceOfItem(ItemStack item){
        return ShopsLoad.getPrice(item, ShopsLoad.shops.get(0));
    }

    public Map.Entry<Double, Double> getSellAndBuyPrice(ItemStack item){
        return ShopsLoad.getBothPrices(item, ShopsLoad.shops.get(0));
    }

    public double getPriceOfItem(ItemStack item, Shop shop){
        return ShopsLoad.getPriceShopOnly(item, shop);
    }

    public Map.Entry<Double, Double> getSellAndBuyPrice(ItemStack item, Shop shop){
        return ShopsLoad.getBothPricesShopOnly(item, shop);
    }

    public double getPriceOfItemWithBoosters(Player player, ItemStack item){
        double initialValue=ShopsLoad.getPrice(item, ShopsLoad.shops.get(0));
        BoostersActive.Booster percent=RoyaleEconomy.boosters.getBoosterPercentShopSell(player, ShopsLoad.shops.get(0).getShopName());
        if(percent!=null) {
            double value = BoostersActive.getValueFromPercent(initialValue, percent.percent);
            initialValue+=value;
        }
        return initialValue;
    }

    public Map.Entry<Double, Double> getSellAndBuyPriceWithBoosters(Player player, ItemStack item){
        Map.Entry<Double, Double> initialValue= ShopsLoad.getBothPrices(item, ShopsLoad.shops.get(0));
        BoostersActive.Booster percent=RoyaleEconomy.boosters.getBoosterPercentShopSell(player, ShopsLoad.shops.get(0).getShopName());
        if(percent!=null) {
            double value = BoostersActive.getValueFromPercent(initialValue.getKey(), percent.percent);
            if(initialValue.getKey()+value>initialValue.getValue())
                value=initialValue.getValue()-initialValue.getKey();
            if(value!=0)
                return new AbstractMap.SimpleEntry<>(initialValue.getKey()+value, initialValue.getValue());
        }
        return initialValue;
    }

    public double getPriceOfItemWithBoosters(Player player, ItemStack item, Shop shop){
        double initialValue=ShopsLoad.getPriceShopOnly(item, shop);
        BoostersActive.Booster percent=RoyaleEconomy.boosters.getBoosterPercentShopSell(player, shop.getShopName());
        if(percent!=null) {
            double value = BoostersActive.getValueFromPercent(initialValue, percent.percent);
            initialValue+=value;
        }
        return initialValue;
    }

    public Map.Entry<Double, Double> getSellAndBuyPriceWithBoosters(Player player, ItemStack item, Shop shop){
        Map.Entry<Double, Double> initialValue= ShopsLoad.getBothPricesShopOnly(item, shop);
        BoostersActive.Booster percent=RoyaleEconomy.boosters.getBoosterPercentShopSell(player, shop.getShopName());
        if(percent!=null) {
            double value = BoostersActive.getValueFromPercent(initialValue.getKey(), percent.percent);
            if(initialValue.getKey()+value>initialValue.getValue())
                value=initialValue.getValue()-initialValue.getKey();
            if(value!=0)
                return new AbstractMap.SimpleEntry<>(initialValue.getKey()+value, initialValue.getValue());
        }
        return initialValue;
    }

}
