package me.qKing12.RoyaleEconomy.MultiCurrencyShops;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import de.tr7zw.changeme.nbtapi.NBTItem;
import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;


public class ShopPlayerCache implements Listener {

    //this class saves the players that have shops open so in case
    //of a crash they don't keep the items with modified nbt from shops

    private static ArrayList<String> playersInCache = new ArrayList<>();
    public static ConcurrentHashMap<Player, ArrayList<ShopMenu.ShopHistoryItem>> shopHistory=new ConcurrentHashMap<>();
    static int lengthSellLore;
    static int lengthBoughtLore;

    public ShopPlayerCache(){
        loadData();
        Bukkit.getPluginManager().registerEvents(this, RoyaleEconomy.plugin);
        lengthSellLore = RoyaleEconomy.multiCurrencyShopsCfg.getStringList("lore-sell-addition").size();
        lengthBoughtLore = RoyaleEconomy.multiCurrencyShopsCfg.getStringList("freshly-bought-lore").size();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e){
        if(playersInCache.contains(e.getPlayer().getName())){
            clearResidualNBT(e.getPlayer());
        }
    }

    public static void clearResidualNBT(Player p){
        Inventory playerInventory = p.getInventory();
        for(int i=0;i<36;i++){
            ItemStack item = playerInventory.getItem(i);
            if(item==null)
                continue;
            NBTItem nbt = new NBTItem(item);
            if(nbt.getDouble("SellValue")!=0){
                nbt.removeKey("SellValue");
                item=nbt.getItem();
                ItemMeta meta = item.getItemMeta();
                ArrayList<String> lore = (ArrayList<String>)meta.getLore();
                int toRemove=lengthSellLore;
                //boolean removeBoolean=false;
                /*if(BoostersActive.shopSellLoreAddition!=null && nbt.getBoolean("UseRECBooster")){
                    removeBoolean=true;
                    toRemove+=BoostersActive.shopSellLoreAddition.size();
                }*/
                lore.subList(lore.size() - toRemove, lore.size()).clear();
                meta.setLore(lore);
                item.setItemMeta(meta);
                /*if(removeBoolean){
                    nbt=new NBTItem(item);
                    nbt.removeKey("UseRECBooster");
                    item=nbt.getItem();
                }*/
                playerInventory.setItem(i, item);
            }
            else if(nbt.getBoolean("FreshlyBought")){
                nbt.removeKey("FreshlyBought");
                item=nbt.getItem();
                ItemMeta meta = item.getItemMeta();
                ArrayList<String> lore = (ArrayList<String>)meta.getLore();
                lore.subList(lore.size()-lengthBoughtLore, lore.size()).clear();
                meta.setLore(lore);
                item.setItemMeta(meta);
                playerInventory.setItem(i, item);
            }
        }
        removePlayer(p.getName());
    }

    private static void loadData(){
        try {
            File database = new File(RoyaleEconomy.plugin.getDataFolder(), "database/shopMultiCurrencyPlayerCache.txt");
            if (database.exists()) {
                GsonBuilder builder = new GsonBuilder();

                builder.serializeNulls();

                Gson gson = builder.create();

                BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(RoyaleEconomy.plugin.getDataFolder() + "/database/shopMultiCurrencyPlayerCache.txt"), StandardCharsets.UTF_8));
                Type type = new TypeToken<ArrayList<String>>() {}.getType();
                playersInCache=gson.fromJson(reader.readLine(), type);
                if(playersInCache==null)
                    playersInCache=new ArrayList<>();

                reader.close();
            }
            else
                database.createNewFile();
        }catch(Exception x){
            x.printStackTrace();
        }
    }

    private static void saveData(){
        try {
            GsonBuilder builder = new GsonBuilder();

            builder.serializeNulls();

            Gson gson = builder.create();

            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(RoyaleEconomy.plugin.getDataFolder() + "/database/shopMultiCurrencyPlayerCache.txt"), StandardCharsets.UTF_8));
            writer.write(gson.toJson(playersInCache));

            writer.close();
        }catch(Exception x){
            x.printStackTrace();
        }
    }

    static void addPlayer(String player){
        playersInCache.add(player);
        saveData();
    }

    static void removePlayer(String player){
        playersInCache.remove(player);
        saveData();
    }
}
