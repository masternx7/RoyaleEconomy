package me.qKing12.RoyaleEconomy.Boosters;

import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import me.qKing12.RoyaleEconomy.Shops.Shop;
import me.qKing12.RoyaleEconomy.utils.PlayerMessageHandler;
import me.qKing12.RoyaleEconomy.utils.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BoostersActive implements Boosters{

    public static String permission;
    private static String noExpire;

    static String shopBuyCategoryName;
    final ArrayList<Booster> shopBuyBoosters=new ArrayList<>();
    final ConcurrentHashMap<Booster, Long> shopBuyActiveBoosters = new ConcurrentHashMap<>();

    static String shopSellCategoryName;
    final ArrayList<Booster> shopSellBoosters=new ArrayList<>();
    final ConcurrentHashMap<Booster, Long> shopSellActiveBoosters = new ConcurrentHashMap<>();

    static String killCoinsCategoryName;
    final ArrayList<Booster> killCoinsBoosters=new ArrayList<>();
    final ConcurrentHashMap<Booster, Long> killCoinsActiveBoosters = new ConcurrentHashMap<>();

    @Override
    public ArrayList<BoostersActive.Booster> getBoostersKillCoins() {
        return killCoinsBoosters;
    }

    @Override
    public ArrayList<BoostersActive.Booster> getBoostersShopBuy() {
        return shopBuyBoosters;
    }

    @Override
    public ArrayList<BoostersActive.Booster> getBoostersShopSell() {
        return shopSellBoosters;
    }

    private final ArrayList<String> footer=new ArrayList<>();
    private final ArrayList<String> header=new ArrayList<>();
    private final ArrayList<String> noActiveBoosters=new ArrayList<>();
    private final String structure;

    public static ArrayList<String> shopSellLoreAddition;
    public static ArrayList<String> shopBuyLoreAddition;

    public void sendMessage(Player p){
        for(String line : header)
            PlayerMessageHandler.messageSend(p, line);
        long date = ZonedDateTime.now().toInstant().toEpochMilli();
        boolean oneIsActive=false;
        if(shopBuyCategoryName!=null) {
            for (Map.Entry<Booster, Long> boosterEntry : shopBuyActiveBoosters.entrySet()) {
                Booster booster = boosterEntry.getKey();
                if (booster.permission == null || p.hasPermission(booster.permission)) {
                    oneIsActive = true;
                    String timeLeft = RoyaleEconomy.messageHelper.formatTimeShort(boosterEntry.getValue() - ZonedDateTime.now().toInstant().toEpochMilli());
                    PlayerMessageHandler.messageSend(p, structure
                            .replace("%booster-name%", booster.boosterName)
                            .replace("%category-name%", shopBuyCategoryName)
                            .replace("%time-left%", timeLeft)
                    );
                }
            }
            for(Booster booster : shopBuyBoosters){
                if(booster.playerHasBooster(p, date)){
                    oneIsActive = true;
                    String timeLeft;
                    try {
                        timeLeft = RoyaleEconomy.messageHelper.formatTimeShort(booster.personalPlayers.get(p.getUniqueId().toString()) - ZonedDateTime.now().toInstant().toEpochMilli());
                    }catch(Exception x){
                        timeLeft=noExpire;
                    }
                    PlayerMessageHandler.messageSend(p, structure
                            .replace("%booster-name%", booster.boosterName)
                            .replace("%category-name%", shopBuyCategoryName)
                            .replace("%time-left%", timeLeft)
                    );
                }
            }
        }
        if(shopSellCategoryName!=null) {
            for (Map.Entry<Booster, Long> boosterEntry : shopSellActiveBoosters.entrySet()) {
                Booster booster = boosterEntry.getKey();
                if (booster.permission == null || p.hasPermission(booster.permission)) {
                    oneIsActive = true;
                    String timeLeft = RoyaleEconomy.messageHelper.formatTimeShort(boosterEntry.getValue() - ZonedDateTime.now().toInstant().toEpochMilli());
                    PlayerMessageHandler.messageSend(p, structure
                            .replace("%booster-name%", booster.boosterName)
                            .replace("%category-name%", shopSellCategoryName)
                            .replace("%time-left%", timeLeft)
                    );
                }
            }
            for(Booster booster : shopSellBoosters){
                if(booster.playerHasBooster(p, date)){
                    oneIsActive = true;
                    String timeLeft;
                    try {
                        timeLeft = RoyaleEconomy.messageHelper.formatTimeShort(booster.personalPlayers.get(p.getUniqueId().toString()) - ZonedDateTime.now().toInstant().toEpochMilli());
                    }catch (Exception x){
                        timeLeft=noExpire;
                    }
                    PlayerMessageHandler.messageSend(p, structure
                            .replace("%booster-name%", booster.boosterName)
                            .replace("%category-name%", shopSellCategoryName)
                            .replace("%time-left%", timeLeft)
                    );
                }
            }
        }
        if(killCoinsCategoryName!=null) {
            for (Map.Entry<Booster, Long> boosterEntry : killCoinsActiveBoosters.entrySet()) {
                Booster booster = boosterEntry.getKey();
                if (booster.permission == null || p.hasPermission(booster.permission)) {
                    oneIsActive = true;
                    String timeLeft = RoyaleEconomy.messageHelper.formatTimeShort(boosterEntry.getValue() - ZonedDateTime.now().toInstant().toEpochMilli());
                    PlayerMessageHandler.messageSend(p, structure
                            .replace("%booster-name%", booster.boosterName)
                            .replace("%category-name%", killCoinsCategoryName)
                            .replace("%time-left%", timeLeft)
                    );
                }
            }
            for(Booster booster : killCoinsBoosters){
                if(booster.playerHasBooster(p, date)){
                    oneIsActive = true;
                    String timeLeft;
                    try {
                        timeLeft = RoyaleEconomy.messageHelper.formatTimeShort(booster.personalPlayers.get(p.getUniqueId().toString()) - ZonedDateTime.now().toInstant().toEpochMilli());
                    }catch(Exception x){
                        timeLeft=noExpire;
                    }
                    PlayerMessageHandler.messageSend(p, structure
                            .replace("%booster-name%", booster.boosterName)
                            .replace("%category-name%", killCoinsCategoryName)
                            .replace("%time-left%", timeLeft)
                    );
                }
            }
        }

        if(!oneIsActive)
            for(String line : noActiveBoosters)
                PlayerMessageHandler.messageSend(p, line);
        for(String line : footer)
            PlayerMessageHandler.messageSend(p, line);
    }

    public void sendMessageForPlayer(CommandSender sender, Player p){
        for(String line : header)
            sender.sendMessage(line);
        long date = ZonedDateTime.now().toInstant().toEpochMilli();
        boolean oneIsActive=false;
        if(shopBuyCategoryName!=null) {
            for (Map.Entry<Booster, Long> boosterEntry : shopBuyActiveBoosters.entrySet()) {
                Booster booster = boosterEntry.getKey();
                if (booster.permission == null || p.hasPermission(booster.permission)) {
                    oneIsActive = true;
                    String timeLeft = RoyaleEconomy.messageHelper.formatTimeShort(boosterEntry.getValue() - ZonedDateTime.now().toInstant().toEpochMilli());
                    sender.sendMessage(structure
                            .replace("%booster-name%", booster.boosterName)
                            .replace("%category-name%", shopBuyCategoryName)
                            .replace("%time-left%", timeLeft)
                    );
                }
            }
            for(Booster booster : shopBuyBoosters){
                if(booster.playerHasBooster(p, date)){
                    oneIsActive = true;
                    String timeLeft;
                    try {
                        timeLeft = RoyaleEconomy.messageHelper.formatTimeShort(booster.personalPlayers.get(p.getUniqueId().toString()) - ZonedDateTime.now().toInstant().toEpochMilli());
                    }catch(Exception x){
                        timeLeft=noExpire;
                    }
                    sender.sendMessage(structure
                            .replace("%booster-name%", booster.boosterName)
                            .replace("%category-name%", shopBuyCategoryName)
                            .replace("%time-left%", timeLeft)
                    );
                }
            }
        }
        if(shopSellCategoryName!=null) {
            for (Map.Entry<Booster, Long> boosterEntry : shopSellActiveBoosters.entrySet()) {
                Booster booster = boosterEntry.getKey();
                if (booster.permission == null || p.hasPermission(booster.permission)) {
                    oneIsActive = true;
                    String timeLeft = RoyaleEconomy.messageHelper.formatTimeShort(boosterEntry.getValue() - ZonedDateTime.now().toInstant().toEpochMilli());
                    sender.sendMessage(structure
                            .replace("%booster-name%", booster.boosterName)
                            .replace("%category-name%", shopSellCategoryName)
                            .replace("%time-left%", timeLeft)
                    );
                }
            }
            for(Booster booster : shopSellBoosters){
                if(booster.playerHasBooster(p, date)){
                    oneIsActive = true;
                    String timeLeft;
                    try {
                        timeLeft = RoyaleEconomy.messageHelper.formatTimeShort(booster.personalPlayers.get(p.getUniqueId().toString()) - ZonedDateTime.now().toInstant().toEpochMilli());
                    }catch(Exception x){
                        timeLeft=noExpire;
                    }
                    sender.sendMessage(structure
                            .replace("%booster-name%", booster.boosterName)
                            .replace("%category-name%", shopSellCategoryName)
                            .replace("%time-left%", timeLeft)
                    );
                }
            }
        }
        if(killCoinsCategoryName!=null) {
            for (Map.Entry<Booster, Long> boosterEntry : killCoinsActiveBoosters.entrySet()) {
                Booster booster = boosterEntry.getKey();
                if (booster.permission == null || p.hasPermission(booster.permission)) {
                    oneIsActive = true;
                    String timeLeft = RoyaleEconomy.messageHelper.formatTimeShort(boosterEntry.getValue() - ZonedDateTime.now().toInstant().toEpochMilli());
                    sender.sendMessage(structure
                            .replace("%booster-name%", booster.boosterName)
                            .replace("%category-name%", killCoinsCategoryName)
                            .replace("%time-left%", timeLeft)
                    );
                }
            }
            for(Booster booster : killCoinsBoosters){
                if(booster.playerHasBooster(p, date)){
                    oneIsActive = true;
                    String timeLeft;
                    try {
                        timeLeft = RoyaleEconomy.messageHelper.formatTimeShort(booster.personalPlayers.get(p.getUniqueId().toString()) - ZonedDateTime.now().toInstant().toEpochMilli());
                    }catch(Exception x){
                        timeLeft=noExpire;
                    }
                    sender.sendMessage(structure
                            .replace("%booster-name%", booster.boosterName)
                            .replace("%category-name%", killCoinsCategoryName)
                            .replace("%time-left%", timeLeft)
                    );
                }
            }
        }

        if(!oneIsActive)
            for(String line : noActiveBoosters)
                sender.sendMessage(line);
        for(String line : footer)
            sender.sendMessage(line);
    }

    private void loadBoosters(){
        if(RoyaleEconomy.boostersCfg.getBoolean("boosters.shops-buy.use")) {
            shopBuyCategoryName=Utils.chat(RoyaleEconomy.boostersCfg.getString("boosters.shops-buy.category-name"));
            for (String key : RoyaleEconomy.boostersCfg.getConfigurationSection("boosters.shops-buy.boosters").getKeys(false)) {
                shopBuyBoosters.add(new Booster(RoyaleEconomy.boostersCfg.getConfigurationSection("boosters.shops-buy.boosters."+key), key));
            }
            shopBuyLoreAddition=new ArrayList<>();
            for(String line : RoyaleEconomy.boostersCfg.getStringList("boosters.shops-buy.lore-addition"))
                shopBuyLoreAddition.add(Utils.chat(line));
        }
        if(RoyaleEconomy.boostersCfg.getBoolean("boosters.shops-sell.use")) {
            shopSellCategoryName=Utils.chat(RoyaleEconomy.boostersCfg.getString("boosters.shops-sell.category-name"));
            for (String key : RoyaleEconomy.boostersCfg.getConfigurationSection("boosters.shops-sell.boosters").getKeys(false)) {
                shopSellBoosters.add(new Booster(RoyaleEconomy.boostersCfg.getConfigurationSection("boosters.shops-sell.boosters."+key), key));
            }
            shopSellLoreAddition=new ArrayList<>();
            for(String line : RoyaleEconomy.boostersCfg.getStringList("boosters.shops-sell.lore-addition"))
                shopSellLoreAddition.add(Utils.chat(line));
        }
        if(RoyaleEconomy.boostersCfg.getBoolean("boosters.kill-coins.use")) {
            killCoinsCategoryName=Utils.chat(RoyaleEconomy.boostersCfg.getString("boosters.kill-coins.category-name"));
            for (String key : RoyaleEconomy.boostersCfg.getConfigurationSection("boosters.kill-coins.boosters").getKeys(false)) {
                killCoinsBoosters.add(new Booster(RoyaleEconomy.boostersCfg.getConfigurationSection("boosters.kill-coins.boosters."+key), key));
            }
        }
    }

    public BoostersActive(){
        loadBoosters();
        for(String line :RoyaleEconomy.boostersCfg.getStringList("booster-command.active-booster-display.header"))
            header.add(Utils.chat(line));
        for(String line :RoyaleEconomy.boostersCfg.getStringList("booster-command.active-booster-display.footer"))
            footer.add(Utils.chat(line));
        for(String line :RoyaleEconomy.boostersCfg.getStringList("booster-command.active-booster-display.middle-structure-no-boosters"))
            noActiveBoosters.add(Utils.chat(line));
        structure=Utils.chat(RoyaleEconomy.boostersCfg.getString("booster-command.active-booster-display.middle-structure"));
        permission=RoyaleEconomy.boostersCfg.getString("booster-command.permission");
        noExpire=Utils.chat(RoyaleEconomy.boostersCfg.getString("never-expire-message"));
    }

    public class Booster{
        String boosterKey;
        String permission;
        String permissionPermanent;
        public double percent;
        public String boosterName;
        final ConcurrentHashMap<String, Long> personalPlayers=new ConcurrentHashMap<>();
        ArrayList<String> shops;

        public boolean playerHasBooster(Player p, long time){
            if(personalPlayers.containsKey(p.getUniqueId().toString())){
                if(personalPlayers.get(p.getUniqueId().toString())<=time)
                    personalPlayers.remove(p.getUniqueId().toString());
                else
                    return true;
            }
            return hasPermanentPermission(p);
        }

        public void addPlayerToBooster(Player p, int minutes){
            personalPlayers.put(p.getUniqueId().toString(), ZonedDateTime.now().toInstant().toEpochMilli()+minutes*60000);
        }

        public void removePlayerBooster(Player p){
            personalPlayers.remove(p.getUniqueId().toString());
        }

        public boolean isInShop(String shopName){
            if(shops==null)
                return true;
            return shops.contains(shopName);
        }

        private boolean hasPermanentPermission(Player p){
            if(permissionPermanent==null)
                return false;
            return p.hasPermission(permissionPermanent);
        }

        public Booster(ConfigurationSection section, String key){
            boosterKey=key;
            String permission = section.getString("permission");
            if(permission!=null && !permission.equalsIgnoreCase("none"))
                this.permission=permission;
            String permission2 = section.getString("permission-permanent");
            if(permission2!=null && !permission2.equalsIgnoreCase("none"))
                this.permissionPermanent=permission2;
            percent=section.getDouble("percent-boost");
            boosterName=Utils.chat(section.getString("booster-name"));
            List<String> strings = section.getStringList("exclusive-for-shops");
            if(strings!=null && !strings.isEmpty())
                shops=(ArrayList<String>)section.getStringList("exclusive-for-shops");
        }
    }

    public Booster getShopBuyBoosterByKey(String key){
        for(Booster booster : shopBuyBoosters)
            if(booster.boosterKey.equalsIgnoreCase(key))
                return booster;
        return null;
    }

    public Booster getShopSellBoosterByKey(String key){
        for(Booster booster : shopSellBoosters)
            if(booster.boosterKey.equalsIgnoreCase(key))
                return booster;
        return null;
    }

    public Booster getKillCoinsBoosterByKey(String key){
        for(Booster booster : killCoinsBoosters)
            if(booster.boosterKey.equalsIgnoreCase(key))
                return booster;
        return null;
    }

    public static double getValueFromPercent(double initialAmount, double percent){
        double  toReturn = initialAmount / 100 * percent;
        return RoyaleEconomy.messageHelper.useDecimals?toReturn:Math.floor(toReturn);
    }

    public Booster getBoosterPercentShopBuy(Player p, String shop){
        if(shopBuyCategoryName==null)
            return null;
        Booster toReturn=null;
        double toReturnValue=0d;
        long date = ZonedDateTime.now().toInstant().toEpochMilli();
        for(Map.Entry<Booster, Long> boosterEntry : shopBuyActiveBoosters.entrySet()){
            if(!boosterEntry.getKey().isInShop(shop))
                continue;
            if(boosterEntry.getValue()<=date)
                shopBuyActiveBoosters.remove(boosterEntry.getKey());
            else{
                Booster booster = boosterEntry.getKey();
                if((booster.permission==null || p.hasPermission(booster.permission)) && booster.percent>toReturnValue) {
                    toReturn=booster;
                    toReturnValue=booster.percent;
                }
            }
        }
        for(Booster booster : shopBuyBoosters)
            if(booster.isInShop(shop)) {
                if (booster.playerHasBooster(p, date) && booster.percent > toReturnValue) {
                    toReturn = booster;
                    toReturnValue = booster.percent;
                }
            }
        return toReturn;
    }

    public Booster getBoosterPercentShopSell(Player p, String shop){
        if(shopSellCategoryName==null)
            return null;
        Booster toReturn=null;
        double toReturnValue=0d;
        long date = ZonedDateTime.now().toInstant().toEpochMilli();
        for(Map.Entry<Booster, Long> boosterEntry : shopSellActiveBoosters.entrySet()){
            if(!boosterEntry.getKey().isInShop(shop))
                continue;
            if(boosterEntry.getValue()<=date)
                shopSellActiveBoosters.remove(boosterEntry.getKey());
            else{
                Booster booster = boosterEntry.getKey();
                if((booster.permission==null || p.hasPermission(booster.permission)) && booster.percent>toReturnValue) {
                    toReturn = booster;
                    toReturnValue=booster.percent;
                }
            }
        }
        for(Booster booster : shopSellBoosters) {
            if(booster.isInShop(shop)) {
                if (booster.playerHasBooster(p, date) && booster.percent > toReturnValue) {
                    toReturn = booster;
                    toReturnValue = booster.percent;
                }
            }
        }
        return toReturn;
    }

    public Booster getBoosterPercentKillCoins(Player p){
        if(killCoinsCategoryName==null)
            return null;
        Booster toReturn=null;
        double toReturnValue=0d;
        long date = ZonedDateTime.now().toInstant().toEpochMilli();
        for(Map.Entry<Booster, Long> boosterEntry : killCoinsActiveBoosters.entrySet()){
            if(boosterEntry.getValue()<=date)
                killCoinsActiveBoosters.remove(boosterEntry.getKey());
            else{
                Booster booster = boosterEntry.getKey();
                if((booster.permission==null || p.hasPermission(booster.permission)) && booster.percent>toReturnValue) {
                    toReturn = booster;
                    toReturnValue=booster.percent;
                }
            }
        }
        for(Booster booster : killCoinsBoosters)
            if(booster.playerHasBooster(p, date) && booster.percent>toReturnValue) {
                toReturn = booster;
                toReturnValue=booster.percent;
            }
        return toReturn;
    }

}
