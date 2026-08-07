package me.qKing12.RoyaleEconomy.BlackMarket;

import com.tcoded.folialib.wrapper.task.WrappedTask;
import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import me.qKing12.RoyaleEconomy.utils.Utf8YamlConfiguration;
import me.qKing12.RoyaleEconomy.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.ArrayList;

public class BlackMarket {
    public static FileConfiguration config;
    public static File file;
    public static long date;
    static long accessDate;
    public static WrappedTask refreshCheck;
    public static WrappedTask refreshItemCheck;
    static long refreshInterval;
    static boolean limitedAccess;
    static long refreshAccessTime;
    static String permission;
    static String adminPermission;
    static String refreshMessage;
    static ArrayList<Integer> slots;
    static String finishedStock;
    static String clickingCooldown;
    static String notEnoughMoney;
    static String buyMessage;
    static String fullInventory;
    static String refreshCooldownMessage;
    static String noPermission;

    static String scamMessage;
    static int chanceScam;
    static boolean useScam;


    public BlackMarket() {
        for (String line : RoyaleEconomy.blackMarketCfg.getStringList("item-price-lore-addition"))
            BlackMarketItem.costAddition.add(Utils.chat(line));
        for (String line : RoyaleEconomy.blackMarketCfg.getStringList("stock-lore-addition"))
            BlackMarketItem.stockAddition.add(Utils.chat(line));
        refreshInterval = 60 * RoyaleEconomy.blackMarketCfg.getInt("refresh-rate");
        limitedAccess = RoyaleEconomy.blackMarketCfg.getBoolean("use-limited-access-time");
        refreshAccessTime = RoyaleEconomy.blackMarketCfg.getInt("time-to-access") * 60;
        permission = RoyaleEconomy.blackMarketCfg.getString("blackmarket-permission");
        adminPermission = RoyaleEconomy.blackMarketCfg.getString("admin-permission");
        refreshMessage = Utils.chat(RoyaleEconomy.blackMarketCfg.getString("refresh-message"));
        slots = (ArrayList<Integer>) RoyaleEconomy.blackMarketCfg.getIntegerList("blackmarket-item-slots");
        finishedStock=Utils.chat(RoyaleEconomy.blackMarketCfg.getString("finished-stock-message"));
        clickingCooldown=Utils.chat(RoyaleEconomy.blackMarketCfg.getString("clicking-cooldown"));
        notEnoughMoney=Utils.chat(RoyaleEconomy.blackMarketCfg.getString("not-enough-money"));
        buyMessage=Utils.chat(RoyaleEconomy.blackMarketCfg.getString("buy-message"));
        fullInventory=Utils.chat(RoyaleEconomy.blackMarketCfg.getString("full-inventory"));
        refreshCooldownMessage=Utils.chat(RoyaleEconomy.blackMarketCfg.getString("blackmarket-refresh"));
        noPermission=Utils.chat(RoyaleEconomy.blackMarketCfg.getString("no-permission-message"));

        useScam=RoyaleEconomy.blackMarketCfg.getBoolean("blackmarket-scam.use");
        if(useScam){
            chanceScam=RoyaleEconomy.blackMarketCfg.getInt("blackmarket-scam.chance");
            scamMessage=Utils.chat(RoyaleEconomy.blackMarketCfg.getString("blackmarket-scam.scam-message"));
        }

        ArrayList<String> lore=new ArrayList<>();
        for(String line : RoyaleEconomy.blackMarketCfg.getStringList("no-item-to-display.lore"))
            lore.add(Utils.chat(line));
        BlackMarketMenu.noDisplay=RoyaleEconomy.itemConstructor.getItem(RoyaleEconomy.blackMarketCfg.getString("no-item-to-display.material"), Utils.chat(RoyaleEconomy.blackMarketCfg.getString("no-item-to-display.name")), lore);

        Rarity.loadRarities();
        file = new File(RoyaleEconomy.plugin.getDataFolder(), "blackMarketItems.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        config = Utf8YamlConfiguration.loadConfiguration(file);
        loadItems();

        date = config.getLong("refresh-date");
        if (date < ZonedDateTime.now().toEpochSecond()) {
            date = ZonedDateTime.now().toEpochSecond();
            config.set("refresh-date", date);
            try{
                config.save(file);
            }catch(Exception x){
                x.printStackTrace();
            }
        }
        if (limitedAccess) {
            accessDate = date + refreshAccessTime;
        }

        BlackMarketMenu.generateInventory();

        refreshCheck();
        refreshItemCheck();
    }

    public static void triggerRefresh() {
        if (limitedAccess) {
            accessDate = ZonedDateTime.now().toEpochSecond() + refreshAccessTime;
        }
        if (permission.equals("none"))
            Bukkit.broadcastMessage(refreshMessage);
        else {
            for (Player p : Bukkit.getOnlinePlayers())
                if (p.hasPermission(permission))
                    p.sendMessage(refreshMessage);
        }

        BlackMarketMenu.regenerateItems();
    }

    public void refreshCheck() {
        if (refreshCheck != null)
            refreshCheck.cancel();

        refreshCheck = RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runTimerAsync(() -> {
            if (date <= ZonedDateTime.now().toEpochSecond()) {
                date = ZonedDateTime.now().toEpochSecond() + refreshInterval;
                config.set("refresh-date", date);
                try {
                    config.save(file);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                triggerRefresh();
            }
        }, 20, 400);
//        refreshCheck = RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runTimerAsync(() -> {
//            if (date <= ZonedDateTime.now().toEpochSecond()) {
//                date = ZonedDateTime.now().toEpochSecond() + refreshInterval;
//                config.set("refresh-date", date);
//                try {
//                    config.save(file);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                triggerRefresh();
//            }
//        }, 20, 400);
    }

    public void refreshItemCheck() {
        if (refreshItemCheck != null)
            refreshItemCheck.cancel();

        if (RoyaleEconomy.staticValues.blackMarketRefreshItem != null) {
            refreshItemCheck = RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runTimerAsync(() -> {
                BlackMarketMenu.placeRefreshItem(true);
            }, 20, 1000);
        }
    }

    public void loadItems() {
        BlackMarketItem.blackMarketItems = new ArrayList<>();
        if (config.getConfigurationSection("items") != null) {
            for (String key : config.getConfigurationSection("items").getKeys(false)) {
                BlackMarketItem.blackMarketItems.add(new BlackMarketItem(config.getConfigurationSection("items." + key), key));
            }
        }
    }

    public static BlackMarketItem getRandomItem(ArrayList<BlackMarketItem> items) {
        double completeWeight = 0.0;
        for (BlackMarketItem item : items)
            completeWeight += item.rarity.chance;
        double r = Math.random() * completeWeight;
        double countWeight = 0.0;
        for (BlackMarketItem item : items) {
            countWeight += item.rarity.chance;
            if (countWeight >= r)
                return item;
        }
        throw new RuntimeException("Should never be shown.");
    }


}
