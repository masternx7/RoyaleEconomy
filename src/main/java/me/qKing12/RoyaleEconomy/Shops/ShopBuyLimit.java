package me.qKing12.RoyaleEconomy.Shops;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import me.qKing12.RoyaleEconomy.utils.PlayerMessageHandler;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static me.qKing12.RoyaleEconomy.RoyaleEconomy.shopsCfg;
import static me.qKing12.RoyaleEconomy.RoyaleEconomy.utilsAPI;

public class ShopBuyLimit {

    private static ArrayList<String> denyMessage;
    private static LinkedHashMap<String, Double> limits = new LinkedHashMap<>();
    public static HashMap<String, Double> playerValues;
    private static int day;

    public ShopBuyLimit(){
        denyMessage=(ArrayList<String>)shopsCfg.getStringList("shop-buy-limit.limit-reached-message");
        for(String key : shopsCfg.getConfigurationSection("shop-buy-limit.limit-groups").getKeys(false)){
            ConfigurationSection limit = shopsCfg.getConfigurationSection("shop-buy-limit.limit-groups."+key);
            limits.put(limit.getString("permission"), limit.getDouble("maximum-coins"));
        }
        playerValues=new HashMap<>();
        loadData();
    }

    public static double getLimit(Player p){
        if(p.isOp())
            return -1;
        for(Map.Entry<String, Double> entry : limits.entrySet()){
            if(p.hasPermission(entry.getKey()))
                return entry.getValue();
        }
        return -1;
    }

    public static void sendMessage(Player p, double limit){
        for(String line : denyMessage)
            PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, line.replace("%coins%", RoyaleEconomy.messageHelper.numberFormat(limit))));
    }

    private void checkDataReset(){
        RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runTimerAsync(() ->{
            if(day!= ZonedDateTime.now().getDayOfMonth()){
                playerValues=new HashMap<>();
                day=ZonedDateTime.now().getDayOfMonth();
            }
        }, 2400, 2400);
    }

    private void loadData(){
        try {
            File database = new File(RoyaleEconomy.plugin.getDataFolder(), "database/shopBuyLimit.txt");
            if (database.exists()) {
                GsonBuilder builder = new GsonBuilder();

                builder.serializeNulls();

                Gson gson = builder.create();

                BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(RoyaleEconomy.plugin.getDataFolder() + "/database/shopBuyLimit.txt"), StandardCharsets.UTF_8));

                day = Integer.parseInt(reader.readLine());

                if(day!=ZonedDateTime.now().getDayOfMonth()){
                    reader.close();
                    database.delete();
                    return;
                }

                Type type = new TypeToken<HashMap<String, Double>>() {
                }.getType();
                playerValues = gson.fromJson(reader.readLine(), type);
                if(playerValues==null)
                    playerValues=new HashMap<>();


                reader.close();
                checkDataReset();
            }
            else day=ZonedDateTime.now().getDayOfMonth();
        }catch(Exception x){
            x.printStackTrace();
        }
    }

    public static void saveData(){
        try {
            GsonBuilder builder = new GsonBuilder();

            builder.serializeNulls();

            Gson gson = builder.create();

            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(RoyaleEconomy.plugin.getDataFolder() + "/database/shopBuyLimit.txt"), StandardCharsets.UTF_8));
            writer.write(ZonedDateTime.now().getDayOfMonth()+"\n"+gson.toJson(playerValues));

            writer.close();
        }catch(Exception x){
            x.printStackTrace();
        }
    }

}
