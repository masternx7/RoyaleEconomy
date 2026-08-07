package me.qKing12.RoyaleEconomy.MultiCurrency.internal;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.qKing12.RoyaleEconomy.Commands.dynamic.DynamicCommand;
import me.qKing12.RoyaleEconomy.Commands.dynamic.DynamicCommandsSetup;
import me.qKing12.RoyaleEconomy.DataManager.Cache.BungeeMessagingCacheMySQL;
import me.qKing12.RoyaleEconomy.DataManager.Cache.redis.RedisHandler;
import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import me.qKing12.RoyaleEconomy.utils.PlayerMessageHandler;
import me.qKing12.RoyaleEconomy.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static me.qKing12.RoyaleEconomy.RoyaleEconomy.plugin;

public class Currency implements CommandExecutor, Listener {

    private double coinsReference;
    private String currencyId;
    private String currencyName;
    private String color;
    private boolean decimals;

    private ItemStack icon;
    private double exchangePercent;

    //comenzi si permisiuni
    private ArrayList<String> commands;
    private ArrayList<String> commandHelp;

    private String ownPermission;
    private String sendPermission;
    private String exchangePermission;
    private String bypassExchangePermission;

    private String sellPermission;
    private String buyPermission;

    private String balanceMessage;
    private String sendCurrencyMessage;
    private String receiveCurrencyMessage;

    private boolean useBalanceTop;
    private CurrencyBalanceTop currencyBalanceTop;

    private final boolean cacheDisabled;

    public CurrencyBalanceTop getCurrencyBalanceTop() {
        return currencyBalanceTop;
    }

    public boolean hasDecimals() {
        return decimals;
    }

    public String getCurrencyId() {
        return currencyId;
    }

    public String getReceiveCurrencyMessage() {
        return receiveCurrencyMessage;
    }

    public String getCurrencyName() {
        return currencyName;
    }

    public String getColor() {
        return color;
    }

    public double getCoinsReference() {
        return coinsReference;
    }

    public double getExchangePercent() {
        return exchangePercent;
    }

    public String getBalanceMessage() {
        return balanceMessage;
    }

    public String getBypassExchangePermission() {
        return bypassExchangePermission;
    }

    public ArrayList<String> getCommandHelp() {
        return commandHelp;
    }

    public ArrayList<String> getCommands() {
        return commands;
    }

    public String getExchangePermission() {
        return exchangePermission;
    }

    public String getOwnPermission() {
        return ownPermission;
    }

    public String getSendCurrencyMessage() {
        return sendCurrencyMessage;
    }

    public String getSendPermission() {
        return sendPermission;
    }

    public ItemStack getIcon() {
        return icon;
    }

    private NumberFormat numberFormat;

    public String formatMoney(double amount){
        return numberFormat.format(amount).replace("\u00A0", ",");
    }

    //de generat filele
    public Currency(ConfigurationSection section, String currencyId){
        RoyaleEconomy.dataManager.createCurrency(currencyId);

        this.currencyId=currencyId;
        cacheDisabled=section.getBoolean("no-cache");
        coinsReference=section.getDouble("coins-value");
        decimals=section.getBoolean("decimals");
        currencyName=Utils.chat(section.getString("currency-name"));
        color=Utils.chat(section.getString("currency-color"));
        icon=RoyaleEconomy.itemConstructor.getItemFromMaterial(section.getString("item-icon"));
        exchangePercent=section.getDouble("exchange-percent");
        useBalanceTop=section.getBoolean("use-balance-top");
        if(useBalanceTop){
            currencyBalanceTop=new CurrencyBalanceTop(
                    this,
                    Utils.chat(section.getString("balance-top.footer")),
                    Utils.chat(section.getString("balance-top.header")),
                    Utils.chat(section.getString("balance-top.top-player-structure"))
            );
        }

        ownPermission=section.getString("permissions.own");
        sendPermission=section.getString("permissions.send");
        exchangePermission=section.getString("permissions.exchange");
        bypassExchangePermission=section.getString("permissions.bypass-exchange");
        sellPermission=section.getString("permissions.sell");
        buyPermission=section.getString("permissions.buy");

        commands=(ArrayList<String>)section.getStringList("command-manager.commands");

        balanceMessage=Utils.chat(section.getString("command-manager.command-balance-message"));
        sendCurrencyMessage=Utils.chat(section.getString("command-manager.command-send-message"));
        receiveCurrencyMessage=Utils.chat(section.getString("command-manager.command-receive-message"));

        commandHelp=new ArrayList<>();
        for(String line : section.getStringList("command-manager.command-help"))
            commandHelp.add(Utils.chat(line));

        this.numberFormat = NumberFormat.getInstance();
        numberFormat.setGroupingUsed(true);
        if(decimals){
            numberFormat.setMaximumFractionDigits(2);
        }
        else {
            numberFormat.setRoundingMode(RoundingMode.FLOOR);
            numberFormat.setMaximumFractionDigits(0);
        }

        DynamicCommandsSetup.registerCommand(new DynamicCommand(commands.get(0), commands.stream().skip(1).collect(Collectors.toList()), this));
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(!(sender instanceof Player)){
            return false;
        }

        Player p=(Player)sender;

        if(!"none".equalsIgnoreCase(ownPermission) && !p.hasPermission(ownPermission)){
            PlayerMessageHandler.messageSend(p, MultiCurrencyHandler.noPermissionMessage);
            return false;
        }

        if(args.length>0){
            if(args[0].equalsIgnoreCase("balance")){
                PlayerMessageHandler.messageSend(p, balanceMessage
                        .replace("%amount%", formatMoney(getAmount(p.getUniqueId().toString()))
                        ));
                return false;
            }
            else if(useBalanceTop && args[0].equalsIgnoreCase("balancetop")){
                if(args.length>1 && p.isOp() && args[1].equalsIgnoreCase("refresh")){
                    currencyBalanceTop.triggerRefresh();
                    PlayerMessageHandler.messageSend(p, Utils.chat("&aRefreshed!"));
                }
                else{
                    currencyBalanceTop.displayTop(p);
                }
                return false;
            }
            else if(args[0].equalsIgnoreCase("send")){
                if(!"none".equalsIgnoreCase(sendPermission) && !p.hasPermission(sendPermission)){
                    PlayerMessageHandler.messageSend(p, MultiCurrencyHandler.noPermissionMessage);
                    return false;
                }
                if(args.length>2) {
                    Player p2 = Bukkit.getPlayerExact(args[1]);
                    if(p2==null){
                        PlayerMessageHandler.messageSend(p, MultiCurrencyHandler.noPlayerMessage);
                        return false;
                    }

                    if(p.equals(p2)){
                        return false;
                    }

                    if(!p2.hasPermission(ownPermission)){
                        PlayerMessageHandler.messageSend(p, MultiCurrencyHandler.noOwnOtherPlayer);
                        return false;
                    }

                    double amount=0;
                    try{
                        amount=Double.parseDouble(args[2]);
                        if(amount<0){
                            PlayerMessageHandler.messageSend(p, MultiCurrencyHandler.invalidAmountMessage);
                            return false;
                        }
                    }catch(Exception x){
                        PlayerMessageHandler.messageSend(p, MultiCurrencyHandler.invalidAmountMessage);
                        return false;
                    }
                    double ownAmount = getAmount(p.getUniqueId().toString());
                    if(amount>ownAmount){
                        PlayerMessageHandler.messageSend(p, MultiCurrencyHandler.notEnoughMessage);
                        return false;
                    }

                    removeAmount(p.getUniqueId().toString(), amount);
                    addAmount(p2.getUniqueId().toString(), amount);
                    PlayerMessageHandler.messageSend(p, sendCurrencyMessage
                            .replace("%amount%", formatMoney(amount))
                            .replace("%player%", p2.getName())
                    );
                    PlayerMessageHandler.messageSend(p2, receiveCurrencyMessage
                            .replace("%amount%", formatMoney(amount))
                            .replace("%player%", p.getName())
                    );
                    return false;
                }
            }
            else if(args[0].equalsIgnoreCase("sell")){
                if(!sellPermission.equalsIgnoreCase("none") && !p.hasPermission(sellPermission)){
                    PlayerMessageHandler.messageSend(p, MultiCurrencyHandler.noPermissionMessage);
                    return false;
                }

                new CurrencyExchangeMenu(p, this, true);
                return false;
            }
            else if(args[0].equalsIgnoreCase("buy")){
                if(!buyPermission.equalsIgnoreCase("none") && !p.hasPermission(buyPermission)){
                    PlayerMessageHandler.messageSend(p, MultiCurrencyHandler.noPermissionMessage);
                    return false;
                }

                new CurrencyExchangeMenu(p, this, false);
                return false;
            }
            else if(args[0].equalsIgnoreCase("exchange")){
                if(!sellPermission.equalsIgnoreCase("none") && !p.hasPermission(sellPermission)){
                    PlayerMessageHandler.messageSend(p, MultiCurrencyHandler.noPermissionMessage);
                    return false;
                }

                if(!buyPermission.equalsIgnoreCase("none") && !p.hasPermission(buyPermission)){
                    PlayerMessageHandler.messageSend(p, MultiCurrencyHandler.noPermissionMessage);
                    return false;
                }

                new CurrencyExchangeSellBuyMenu(p, this);
                return false;
            }

        }
        for(String line : commandHelp)
            PlayerMessageHandler.messageSend(p, line);
        return false;
    }


    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event){
        amounts.remove(event.getPlayer().getUniqueId().toString());
    }

    private ConcurrentHashMap<String, Double> amounts=new ConcurrentHashMap<>();

    public void addAmount(String uuid, double amount){
        removeAmountCache(uuid, true);
        RoyaleEconomy.dataManager.addCurrencyMoney(uuid, amount, currencyId);
    }

    public void removeAmountCache(String uuid, boolean sending){
        if(sending && RedisHandler.useRedis){
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF(uuid);
            out.writeUTF("RoyaleEconomyMoneyTransfer"); // the channel could be whatever you want
            out.writeUTF("currencyUpdate");
            out.writeUTF(uuid);
            out.writeUTF(currencyId);
            RedisHandler.sendData(out.toByteArray());
        }
        else if(sending && BungeeMessagingCacheMySQL.bungeecord){
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("RoyaleEconomyMoneyTransfer"); // the channel could be whatever you want
            out.writeUTF("currencyUpdate");
            out.writeUTF(uuid);
            out.writeUTF(currencyId);

            if(Bukkit.getOnlinePlayers().isEmpty()) {
                plugin.getLogger().warning("There were no players online so the message wasn't sent to the bungeecord!");
                plugin.getLogger().warning("This can cause synchronization problems and currency loss.");
                plugin.getLogger().warning("Potential Fixes:");
                plugin.getLogger().warning(" - Make sure you don't give currency through console unless the player is not online on any server or it is in the same server.");
                plugin.getLogger().warning(" - For multi currency you can disable the cache by adding 'no-cache: true' in the currency's config zone.");
            }
            else {
                Bukkit.getOnlinePlayers().stream().findAny().get().sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
                Bukkit.getOnlinePlayers().stream().findAny().get().sendPluginMessage(plugin, "royaleeconomy:main", out.toByteArray());
            }
        }
        amounts.remove(uuid);
    }

    public boolean removeAmount(String uuid, double amount){
        double has=getAmount(uuid);
        if(amount>has)
            return false;

        removeAmountCache(uuid, true);

        RoyaleEconomy.dataManager.removeCurrencyMoney(uuid, amount, currencyId);
        return true;
    }

    public void setAmount(String uuid, double amount){
        removeAmountCache(uuid, true);
        if(Bukkit.getPlayer(UUID.fromString(uuid))!=null)
            amounts.put(uuid, amount);
        RoyaleEconomy.dataManager.setCurrencyMoney(uuid, amount, currencyId);
    }

    public ConcurrentHashMap<String, Double> getCacheAmounts() {
        return amounts;
    }

    public double getAmount(String uuid){
        if(cacheDisabled){
            return RoyaleEconomy.dataManager.getCurrencyMoney(uuid, currencyId);
        }
        Double amount = amounts.getOrDefault(uuid, null);
        if(amount==null){
            amount=RoyaleEconomy.dataManager.getCurrencyMoney(uuid, currencyId);
            if(Bukkit.getPlayer(UUID.fromString(uuid))!=null)
                amounts.put(uuid, amount);
        }
        return amount;
    }

    public double calculateBuyFee(double coins){
        return coins*exchangePercent/100;
    }

}
