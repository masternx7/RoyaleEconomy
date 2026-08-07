package me.qKing12.RoyaleEconomy.Shops;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import me.qKing12.RoyaleEconomy.DataManager.DataManagerMySQL;
import me.qKing12.RoyaleEconomy.DataManager.MySQLLoad;
import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import me.qKing12.RoyaleEconomy.utils.PermissionChecker;
import me.qKing12.RoyaleEconomy.utils.PlayerMessageHandler;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static me.qKing12.RoyaleEconomy.RoyaleEconomy.shopsCfg;
import static me.qKing12.RoyaleEconomy.RoyaleEconomy.utilsAPI;

public class ShopSellLimit implements Listener {

    private static ArrayList<String> denyMessage;
    private static LinkedHashMap<String, Double> limits = new LinkedHashMap<>();
    public static ConcurrentHashMap<String, Double> playerValues;
    private static int day;
    private static boolean syncToMysql;

    public ShopSellLimit(){
        denyMessage=(ArrayList<String>)shopsCfg.getStringList("shop-sell-limit.limit-reached-message");
        for(String key : shopsCfg.getConfigurationSection("shop-sell-limit.limit-groups").getKeys(false)){
            ConfigurationSection limit = shopsCfg.getConfigurationSection("shop-sell-limit.limit-groups."+key);
            limits.put(limit.getString("permission"), limit.getDouble("maximum-coins"));
        }
        playerValues=new ConcurrentHashMap<>();

        day = RoyaleEconomy.dataManager.getSellLimit().loadSellLimit();

        syncToMysql=shopsCfg.getBoolean("shop-sell-limit.sync-to-mysql");

        Bukkit.getPluginManager().registerEvents(this, RoyaleEconomy.plugin);
        checkDataReset();

    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e){
        RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runLater(() -> {
            playerValues.put(e.getPlayer().getUniqueId().toString(), RoyaleEconomy.dataManager.getSellLimit().getLimit(e.getPlayer().getUniqueId().toString()));
        }, 20L);
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e){
        double bal = playerValues.getOrDefault(e.getPlayer().getUniqueId().toString(), 0d);
        if(bal!=0)
            RoyaleEconomy.dataManager.getSellLimit().setLimit(e.getPlayer().getUniqueId().toString(), bal);
        playerValues.remove(e.getPlayer().getUniqueId().toString());
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
            if(day!= ZonedDateTime.now(RoyaleEconomy.dataManager.getSellLimit().getZoneId()).getDayOfMonth()){
                playerValues=new ConcurrentHashMap<>();
                day=ZonedDateTime.now(RoyaleEconomy.dataManager.getSellLimit().getZoneId()).getDayOfMonth();
                RoyaleEconomy.dataManager.getSellLimit().resetLimit();
            }
        }, 100, 100);
    }

    public static void saveLimits(){
        if (playerValues == null)
            return;

        RoyaleEconomy.dataManager.getSellLimit().setLimit(playerValues.entrySet().stream()
                .map(entry -> new Object[]{entry.getKey(), entry.getValue()})
                .collect(Collectors.toList())
        );
    }

}
