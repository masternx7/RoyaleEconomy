package me.qKing12.RoyaleEconomy.BlackMarket;

import me.qKing12.RoyaleEconomy.MultiCurrency.internal.Currency;
import me.qKing12.RoyaleEconomy.MultiCurrency.internal.MultiCurrencyHandler;
import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

public class BlackMarketItem {
    public static ArrayList<BlackMarketItem> blackMarketItems=new ArrayList<>();
    static ArrayList<String> costAddition=new ArrayList<>();
    static ArrayList<String> stockAddition=new ArrayList<>();

    public static BlackMarketItem getItemByName(String name){
        for(BlackMarketItem item : blackMarketItems)
            if(item.id.equalsIgnoreCase(name))
                return item;
        return null;
    }

    String id;
    ItemStack itemStack;
    Rarity rarity;
    int maximumStock;
    int stock;
    double price;
    int scamChance=-1;

    Currency currency;

    private ArrayList<String> commands;
    public BlackMarketItem(ConfigurationSection item, String id){
        this.id=id;
        try {
            itemStack = item.getItemStack("itemStack");
        }catch(Exception x){
            itemStack=null;
            RoyaleEconomy.plugin.getLogger().warning("The item "+id+" in blackMarket.yml is broken! You need to reset it.");
        }
        rarity=Rarity.getRarity(item.getString("rarity"));
        if(rarity==null)
            rarity=Rarity.rarities.get(0);
        maximumStock=stock=item.getInt("stock");
        price=item.getDouble("price");
        if (item.getKeys(false).contains("commands")) {
            this.commands = (ArrayList<String>) item.getStringList("commands");
        }
        if(item.getKeys(false).contains("scam-chance"))
            this.scamChance=item.getInt("scam-chance");
        if(MultiCurrencyHandler.getCurrencies() != null && item.getKeys(false).contains("currency"))
            currency=MultiCurrencyHandler.findCurrencyById(item.getString("currency"));
        giveItemOnBuy=item.getBoolean("give-item-on-buy");
    }

    public BlackMarketItem(String id){
        this.id=id;
        setItemStack(new ItemStack(Material.DIRT));
        setRarity(Rarity.rarities.get(0));
        setMaximumStock(500);
        stock=500;
        setPrice(5);
        saveToFile();
        blackMarketItems.add(this);
    }

    public ItemStack getDisplayItem(){
        ItemStack toReturn=itemStack.clone();
        ArrayList<String> lore=new ArrayList<>();
        if(itemStack.hasItemMeta() && itemStack.getItemMeta().hasLore())
            lore=(ArrayList<String>)itemStack.getItemMeta().getLore();
        int scam=this.scamChance;
        if(scam==-1)
            scam=BlackMarket.chanceScam;
        if(MultiCurrencyHandler.getCurrencies() == null) {
            String price= RoyaleEconomy.messageHelper.numberFormat(this.price);
            for (String line : costAddition)
                lore.add(line.replace("%price%", price).replace("%scam-chance%", String.valueOf(scam)));
        }
        else{
            String price;
            String currencyName;
            String currencyColor;
            if(currency == null){
                price = RoyaleEconomy.messageHelper.numberFormat(this.price);
                currencyName = MultiCurrencyHandler.getDefaultCoinsName();
                currencyColor = MultiCurrencyHandler.getDefaultCoinsColor();
            }
            else{
                price = currency.formatMoney(this.price);
                currencyName = currency.getCurrencyName();
                currencyColor = currency.getColor();
            }
            for (String line : costAddition)
                lore.add(line
                        .replace("%price%", price)
                        .replace("%scam-chance%", String.valueOf(scam))
                        .replace("%currency-name%", currencyName)
                        .replace("%currency-color%", currencyColor)
                );
        }


        if(maximumStock!=0)
            for(String line : stockAddition)
                lore.add(line.replace("%stock%", RoyaleEconomy.messageHelper.numberFormat((double)stock)));

        lore.addAll(rarity.loreAddition);

        ItemMeta meta=toReturn.getItemMeta();
        meta.setLore(lore);
        toReturn.setItemMeta(meta);
        return toReturn;
    }

    private boolean giveItemOnBuy;

    public boolean getGiveItemOnBuy(){
        return giveItemOnBuy;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
        BlackMarket.config.set("items."+id+".currency", currency.getCurrencyId());
    }

    public void setGiveItemOnBuy(boolean toSet){
        giveItemOnBuy=toSet;
        BlackMarket.config.set("items."+id+".give-item-on-buy", giveItemOnBuy);
    }

    public ArrayList<String> getCommands(){
        return commands;
    }

    public void setCommands(ArrayList<String> commands){
        if(commands.isEmpty())
            this.commands=null;
        else
            this.commands=commands;
        BlackMarket.config.set("items."+id+".commands", this.commands);
    }

    public void setItemStack(ItemStack itemStack){
        this.itemStack=itemStack;
        BlackMarket.config.set("items."+id+".itemStack", itemStack);
    }

    public void setRarity(Rarity rarity){
        this.rarity=rarity;
        BlackMarket.config.set("items."+id+".rarity", rarity.name);
    }

    public void setMaximumStock(int stock){
        maximumStock=stock;
        BlackMarket.config.set("items."+id+".stock", maximumStock);
    }

    public void setPrice(double price){
        this.price=price;
        BlackMarket.config.set("items."+id+".price", price);
    }

    public void setScamChance(int scamChance){
        this.scamChance=scamChance;
        BlackMarket.config.set("items."+id+".scam-chance", scamChance);
    }

    public void delete(){
        BlackMarket.config.set("items."+id, null);
        blackMarketItems.remove(this);
    }

    public void saveToFile(){
        try{
            BlackMarket.config.save(BlackMarket.file);
        }catch(Exception x){
            x.printStackTrace();
        }
    }
}
