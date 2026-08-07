package me.qKing12.RoyaleEconomy.BlackMarket;

import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import me.qKing12.RoyaleEconomy.utils.Utils;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;

public class Rarity {
    public static ArrayList<Rarity> rarities;

    public static Rarity getRarity(String name){
        for(Rarity rarity : rarities)
            if(rarity.name.equals(name))
                return rarity;
        return null;
    }

    public static void loadRarities(){
        rarities=new ArrayList<>();
        for(String key : RoyaleEconomy.blackMarketCfg.getConfigurationSection("rarities").getKeys(false)){
            rarities.add(new Rarity(RoyaleEconomy.blackMarketCfg.getConfigurationSection("rarities."+key), key));
        }
    }

    String name;
    double chance;
    ArrayList<String> loreAddition=new ArrayList<>();

    public Rarity(ConfigurationSection section, String name){
        this.name=name;
        chance=section.getDouble("chance");
        for(String line : section.getStringList("lore-addition"))
            loreAddition.add(Utils.chat(line));
    }
}
