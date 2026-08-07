package me.qKing12.RoyaleEconomy.PlaceholderAPISupport;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.qKing12.RoyaleEconomy.BlackMarket.BlackMarket;
import me.qKing12.RoyaleEconomy.Commands.BalanceTopCommand;
import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import me.qKing12.RoyaleEconomy.TimeRewards.TimeRewardPlayerData;
import me.qKing12.RoyaleEconomy.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

public class PlaceholderRegisterNoEconomy extends PlaceholderExpansion implements Listener {

    private RoyaleEconomy plugin;

    public PlaceholderRegisterNoEconomy(RoyaleEconomy plugin){
        Bukkit.getPluginManager().registerEvents(this, plugin);
        this.plugin = plugin;
    }

    @Override
    public boolean persist(){
        return true;
    }

    @Override
    public boolean canRegister(){
        return true;
    }

    @Override
    public String getAuthor(){
        return plugin.getDescription().getAuthors().toString();
    }

    @Override
    public String getIdentifier(){
        return "royaleeconomy";
    }

    @Override
    public String getVersion(){
        return plugin.getDescription().getVersion();
    }

    private String blackMarketCooldown="";
    /*private final HashMap<Player, Double> bankBalance = new HashMap<>();
    private final HashMap<Player, Double> sharedBankBalance = new HashMap<>();
    private final HashMap<Player, Integer> bankUpgrade = new HashMap<>();
    private final HashMap<Player, Integer> sharedBankUpgrade = new HashMap<>();*/

    private final ConcurrentHashMap<String, Double> bankBalance = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Double> sharedBankBalance = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Integer> bankUpgrade = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Integer> sharedBankUpgrade = new ConcurrentHashMap<>();

    @EventHandler
    public void playerLeave(PlayerQuitEvent e){
        RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runAsync((task) -> {
            bankBalance.remove(e.getPlayer().getUniqueId().toString());
            sharedBankBalance.remove(e.getPlayer().getUniqueId().toString());
            bankUpgrade.remove(e.getPlayer().getUniqueId().toString());
            sharedBankUpgrade.remove(e.getPlayer().getUniqueId().toString());
        });
    }

    private void keepPlaceholdersUpdated(){
        RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runTimerAsync(() -> {
            if(BlackMarket.date!=0)
                blackMarketCooldown=RoyaleEconomy.messageHelper.formatTimeDetailed((BlackMarket.date-ZonedDateTime.now().toEpochSecond())*1000);
            for(Player player1 : Bukkit.getOnlinePlayers()) {
                String player=player1.getUniqueId().toString();
                bankUpgrade.put(player, RoyaleEconomy.dataManager.getBankUpgrade(player));
                try {
                    sharedBankUpgrade.put(player, RoyaleEconomy.dataManager.getSharedBankManager().getSharedBankUpgradePlayer(player));
                } catch (Exception x) {
                    sharedBankUpgrade.put(player, -1);
                }

                try {
                    bankBalance.put(player, RoyaleEconomy.dataManager.getBankMoneyFromFile(player));
                }catch(NullPointerException x){

                }
                try {
                    sharedBankBalance.put(player, RoyaleEconomy.dataManager.getSharedBankManager().getSharedBankMoneyPlayer(player));
                } catch (Exception x) {
                    sharedBankBalance.put(player, -1d);
                }
            }
        }, 20, 20);
    }

    private boolean keepUpdated=false;

    @Override
    public String onPlaceholderRequest(Player player, String identifier){

        if(player == null){
            return "";
        }

        if(!keepUpdated){
            keepUpdated=true;
            keepPlaceholdersUpdated();
        }

        if(identifier.equals("dynamic_coins")){
            Double coins = DynamicCoinsPlaceholder.dynamicCoins.getOrDefault(player, 0d);
            if(coins!=0d){
                return DynamicCoinsPlaceholder.displayFormat.replace("%amount%", RoyaleEconomy.messageHelper.numberFormat(coins));
            }
            return "";
        }
        else if(identifier.equals("bank_upgrade_name")){
            return Utils.chat(RoyaleEconomy.bankUpgradesCfg.getString("bank-upgrades."+bankUpgrade.getOrDefault(player.getUniqueId().toString(), 0)+".name"));
        }
        else if(identifier.equals("bank_upgrade_number")){
            return String.valueOf(bankUpgrade.getOrDefault(player.getUniqueId().toString(), 0));
        }
        else if(identifier.equals("sharedbank_upgrade_name")){
            int upgrade=sharedBankUpgrade.getOrDefault(player.getUniqueId().toString(), -1);
            return upgrade==-1?"":Utils.chat(RoyaleEconomy.bankUpgradesCfg.getString("shared-bank-upgrades." + upgrade + ".name"));
        }
        else if(identifier.equals("sharedbank_upgrade_number")){
            int upgrade=sharedBankUpgrade.getOrDefault(player.getUniqueId().toString(), -1);
            return upgrade==-1?"":String.valueOf(upgrade);
        }
        else if(identifier.equals("balance_purse")) {
            return RoyaleEconomy.messageHelper.numberFormat(RoyaleEconomy.dataManager.getMoneyFromFile(player.getUniqueId().toString()));
        }
        else if(identifier.equals("balance_bank")){
            return RoyaleEconomy.messageHelper.numberFormat(bankBalance.getOrDefault(player.getUniqueId().toString(), 0d));
        }
        else if(identifier.equals("balance_bank_short")){
            return RoyaleEconomy.messageHelper.formatCoinsShort(bankBalance.getOrDefault(player.getUniqueId().toString(), 0d));
        }
        else if(identifier.equals("balance_bank_no_commas")) {
            return String.format("%.2f", bankBalance.getOrDefault(player.getUniqueId().toString(), 0d));
        }
        else if(identifier.equals("balance_sharedbank")){
            try {
                double balance = sharedBankBalance.getOrDefault(player.getUniqueId().toString(), -1d);
                return balance == -1 ? "" : RoyaleEconomy.messageHelper.numberFormat(balance);
            }catch(Exception x){
                return "N/A";
            }
        }
        else if(identifier.equals("balance_sharedbank_short")){
            try {
                double balance = sharedBankBalance.getOrDefault(player.getUniqueId().toString(), -1d);
                return balance == -1 ? "" : RoyaleEconomy.messageHelper.formatCoinsShort(balance);
            }catch(Exception x){
                return "N/A";
            }
        }
        else if(identifier.equals("balance_sharedbank_no_commas")) {
            try {
                double balance = sharedBankBalance.getOrDefault(player.getUniqueId().toString(), -1d);
                return balance == -1 ? "" : String.format("%.2f", balance);
            }catch(Exception x){
                return "N/A";
            }
        }
        else if(identifier.equals("interest_cooldown_detailed")){
            return RoyaleEconomy.messageHelper.formatTimeDetailed(RoyaleEconomy.dataManager.getInterestDate()- ZonedDateTime.now().toInstant().toEpochMilli());
        }
        else if(identifier.equals("sharedbank_member_count")){
            return String.valueOf(RoyaleEconomy.dataManager.getSharedBankManager().getMembersSharedBankPlayer(player.getUniqueId().toString(), true).size());
        }
        else if(identifier.equals("interest_cooldown_not_detailed")){
            return RoyaleEconomy.messageHelper.formatTimeNotDetailed(RoyaleEconomy.dataManager.getInterestDate()- ZonedDateTime.now().toInstant().toEpochMilli());
        }
        else if(identifier.equals("interest_cooldown_short")){
            return RoyaleEconomy.messageHelper.formatTimeShort(RoyaleEconomy.dataManager.getInterestDate()- ZonedDateTime.now().toInstant().toEpochMilli());
        }
        else if(identifier.equals("killcoins_total")){
            return RoyaleEconomy.messageHelper.numberFormat(RoyaleEconomy.killCoinsMainHandle.killCoins.getTotalCoins(player));
        }
        else if(identifier.equals("blackmarket_cooldown")){
            return blackMarketCooldown;
        }
        else if(identifier.startsWith("balancetop_purse_name_")){
            try{
                int position = Integer.valueOf(identifier.substring(22))-1;
                return BalanceTopCommand.purseTop.get(position*2+1);
            }catch(Exception x){
                return "N/A";
            }
        }
        else if(identifier.startsWith("balancetop_purse_balance_")){
            try{
                int position = Integer.valueOf(identifier.substring(25))-1;
                return BalanceTopCommand.purseTop.get(position*2);
            }catch(Exception x){
                return "N/A";
            }
        }
        else if(identifier.startsWith("balancetop_bank_name_")){
            try{
                int position = Integer.valueOf(identifier.substring(21))-1;
                return BalanceTopCommand.bankTop.get(position*2+1);
            }catch(Exception x){
                return "N/A";
            }
        }
        else if(identifier.startsWith("balancetop_bank_balance_")){
            try{
                int position = Integer.valueOf(identifier.substring(24))-1;
                return BalanceTopCommand.bankTop.get(position*2);
            }catch(Exception x){
                return "N/A";
            }
        }
        else if(identifier.startsWith("balancetop_sharedbank_name_")){
            try{
                int position = Integer.valueOf(identifier.substring(27))-1;
                return BalanceTopCommand.sharedBankTop.get(position*2+1);
            }catch(Exception x){
                return "N/A";
            }
        }
        else if(identifier.startsWith("balancetop_sharedbank_balance_")){
            try{
                int position = Integer.valueOf(identifier.substring(30))-1;
                return BalanceTopCommand.sharedBankTop.get(position*2);
            }catch(Exception x){
                return "N/A";
            }
        }
        else if(identifier.equals("purse_death_amount")){
            double coins= RoyaleEconomy.dataManager.getMoneyFromFile(player.getUniqueId().toString());
            double percent = RoyaleEconomy.talismanHandler.reducePercent(RoyaleEconomy.killCoinsAndPurseDeathCfg.getDouble("purse-coins-handle.default-percent"), player, Arrays.asList(player.getInventory().getContents()));
            double toTake = RoyaleEconomy.messageHelper.useDecimals ? coins * percent / 100 : Math.floor(coins * percent / 100);
            return RoyaleEconomy.messageHelper.numberFormat(toTake);
        }
        else if(identifier.equals("purse_death_amount_no_commas")){
            double coins= RoyaleEconomy.dataManager.getMoneyFromFile(player.getUniqueId().toString());
            double percent = RoyaleEconomy.talismanHandler.reducePercent(RoyaleEconomy.killCoinsAndPurseDeathCfg.getDouble("purse-coins-handle.default-percent"), player, Arrays.asList(player.getInventory().getContents()));
            double toTake = RoyaleEconomy.messageHelper.useDecimals ? coins * percent / 100 : Math.floor(coins * percent / 100);
            return String.format("%.2f", toTake);
        }
        else if(identifier.startsWith("timerewards_streak_")){
            String rewardName = identifier.substring(19);
            TimeRewardPlayerData.TimeRewardData data = TimeRewardPlayerData.playerData.getOrDefault(player, null);
            if(data==null){
                return "Rejoin Server";
            }
            else{
                return String.valueOf(data.getStreak(rewardName));
            }
        }
        else if(identifier.startsWith("timerewards_cooldown_")){
            String rewardName = identifier.substring(21);
            TimeRewardPlayerData.TimeRewardData data = TimeRewardPlayerData.playerData.getOrDefault(player, null);
            if(data==null){
                return "Rejoin Server";
            }
            else{
                long cooldown=data.getCooldown(rewardName);
                return RoyaleEconomy.messageHelper.formatTimeDetailed(cooldown * 1000);
            }
        }

        return null;
    }
}
