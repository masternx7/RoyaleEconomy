package me.qKing12.RoyaleEconomy;

import com.Zrips.CMI.CMI;
import com.earth2me.essentials.Essentials;
import com.tcoded.folialib.FoliaLib;
import de.rapha149.signgui.SignGUI;
import me.qKing12.RoyaleEconomy.API.APIHandler;
import me.qKing12.RoyaleEconomy.API.Events.*;
import me.qKing12.RoyaleEconomy.BlackMarket.BlackMarket;
import me.qKing12.RoyaleEconomy.BlackMarket.BlackMarketCommand;
import me.qKing12.RoyaleEconomy.BlackMarket.BlackMarketMenu;
import me.qKing12.RoyaleEconomy.Boosters.*;
import me.qKing12.RoyaleEconomy.Commands.*;
import me.qKing12.RoyaleEconomy.Commands.dynamic.DynamicCommandsSetup;
import me.qKing12.RoyaleEconomy.CustomMenuItems.CustomItemsHandler;
import me.qKing12.RoyaleEconomy.DataManager.Cache.PlayerMoneyCache;
import me.qKing12.RoyaleEconomy.DataManager.Cache.PlayerMoneyCacheMySQL;
import me.qKing12.RoyaleEconomy.DataManager.Cache.PlayerMoneyCacheSQL;
import me.qKing12.RoyaleEconomy.DataManager.*;
import me.qKing12.RoyaleEconomy.DataManager.Cache.redis.RedisHandler;
import me.qKing12.RoyaleEconomy.Economy.BalanceTopNoEconomy;
import me.qKing12.RoyaleEconomy.Events.Events;
import me.qKing12.RoyaleEconomy.Gambling.Gambling;
import me.qKing12.RoyaleEconomy.Gambling.GamblingCommand;
import me.qKing12.RoyaleEconomy.Hooks.*;
import me.qKing12.RoyaleEconomy.Hooks.TradeMe.TradeMeHook;
import me.qKing12.RoyaleEconomy.Hooks.bStats.Metrics;
import me.qKing12.RoyaleEconomy.InputGUIs.ChatListener;
import me.qKing12.RoyaleEconomy.KillCoins.KillCoinsMainHandle;
import me.qKing12.RoyaleEconomy.KillCoins.MythicMobsHook;
import me.qKing12.RoyaleEconomy.Menus.*;
import me.qKing12.RoyaleEconomy.MultiCurrency.internal.MultiCurrencyHandler;
import me.qKing12.RoyaleEconomy.PlaceholderAPISupport.*;
import me.qKing12.RoyaleEconomy.Shops.SellAll.SellAllManager;
import me.qKing12.RoyaleEconomy.Shops.ShopBuyLimit;
import me.qKing12.RoyaleEconomy.Shops.ShopPlayerCache;
import me.qKing12.RoyaleEconomy.Shops.ShopSellLimit;
import me.qKing12.RoyaleEconomy.Shops.ShopsLoad;
import me.qKing12.RoyaleEconomy.TimeRewards.TimeRewardsManager;
import me.qKing12.RoyaleEconomy.utils.*;
import net.milkbowl.vault.economy.Economy;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class RoyaleEconomy extends JavaPlugin implements Listener {

    public static RoyaleEconomy plugin;
    public TimeRewardsManager timeRewardsManager;

    public static CustomItemsHandler customItemsHandler;

    public static DataManager dataManager;

    public static PlayerMoneyCache playerMoneyCache;
    public static StaticValues staticValues;

    public static Economy economy;
    public static MessageHelper messageHelper;
    public static ItemConstructor itemConstructor;
    public static PlaceholderAPISupport utilsAPI;
    public static TalismanHandler talismanHandler;
    public static KillCoinsMainHandle killCoinsMainHandle;
    public static boolean upperVersion;
    public static SharedBankHook hooked;
    public static Boosters boosters;

    public static FileConfiguration bankUpgradesCfg;
    public static FileConfiguration coinBagsAndTalismansCfg;
    public static FileConfiguration commandsCfg;
    public static FileConfiguration killCoinsAndPurseDeathCfg;
    public static FileConfiguration menusCfg;
    public static FileConfiguration shopsCfg;
    public static FileConfiguration permissionsCfg;
    public static FileConfiguration soundsCfg;
    public static FileConfiguration boostersCfg;
    public static FileConfiguration blackMarketCfg;
    public static FileConfiguration gamblingCfg;
    public static FileConfiguration timeRewardsCfg;
    public static FileConfiguration multiCurrencyCfg;

    public static FileConfiguration multiCurrencyShopsCfg;
    public static FileConfiguration customItemsCfg;

    public static APIHandler apiHandler;

    public static APIHandler getApiHandler() {
        return apiHandler;
    }

    private boolean searchSkyBlockHook;
    private boolean searchOtherPluginsHook;

    public static boolean noEconomy;
    public BalanceTopNoEconomy balanceTopNoEconomy;

    public DataManager backupDA;

    @EventHandler
    public void enablePluginListener(PluginEnableEvent e){
        if(e.getPlugin().getName().equals("Vault") && !noEconomy){
            Initializer.economyEnabled(e.getPlugin());
        }
        else if(e.getPlugin().getName().equals("PlaceholderAPI")){
            if(utilsAPI==null || utilsAPI instanceof PlaceholderAPISupportNo) {
                if (getConfig().getBoolean("use-dynamic-placeholder")) {
                    new DynamicCoinsPlaceholder();
                }
                if(noEconomy)
                    new PlaceholderRegisterNoEconomy(this).register();
                else
                    new PlaceholderRegister(this).register();
                utilsAPI = new PlaceholderAPISupportYes();
            }
        }
        else if(e.getPlugin().getName().equals("Essentials")){
            if(noEconomy) {
                try {
                    balanceTopNoEconomy.essentials = (Essentials) e.getPlugin();
                    getLogger().info("Hooked into Essentials Economy");
                }catch(Exception x){
                    x.printStackTrace();
                }
            }
            else{
                Collection<RegisteredServiceProvider<Economy>> econs = Bukkit.getPluginManager().getPlugin("Vault").getServer().getServicesManager().getRegistrations(Economy.class);
                for (RegisteredServiceProvider<Economy> economy : econs) {
                    if (economy.getProvider().getName().equalsIgnoreCase("Essentials Economy")|| economy.getProvider().getName().equalsIgnoreCase("EssentialsX Economy") || economy.getProvider().getName().equalsIgnoreCase("CMIEconomy")) {
                        RoyaleEconomy.plugin.getServer().getServicesManager().unregister(economy.getProvider());
                    }
                }
            }
        }
        else if(e.getPlugin().getName().equals("CMI")){
            if(noEconomy)
                balanceTopNoEconomy.cmi=CMI.getInstance();
        }
        else if(e.getPlugin().getName().equalsIgnoreCase("MythicMobs")){
            if(RoyaleEconomy.killCoinsAndPurseDeathCfg.getBoolean("kill-coins.use-kill-coins")) {
                new MythicMobsHook();
                getLogger().info("Hooked into MythicMobs");
            }
        }
        else if(searchSkyBlockHook) {
            if (e.getPlugin().getName().equals("SuperiorSkyblock2")) {
                new SuperiorSkyBlock();
                getLogger().info("Hooked into SuperiorSkyblock2");
            }
            else if (e.getPlugin().getName().equals("BentoBox")) {
                new BentoBox();
                getLogger().info("Hooked into BentoBox");
            }
            else if(e.getPlugin().getName().equals("FabledSkyBlock")){
                new FabledSkyBlock();
                getLogger().info("Hooked into FabledSkyBlock");
            }
            else if(e.getPlugin().getName().equals("IridiumSkyBlock")){
                new IridiumSkyBlock();
                getLogger().info("Hooked into IridiumSkyBlock");
            }
            else if(e.getPlugin().getName().equals("GalacticSkyBlock")){
                new GalacticSkyBlock();
                getLogger().info("Hooked into GalacticSkyBlock");
            }
        }
        else if(searchOtherPluginsHook){
            try {
                if (e.getPlugin().getName().equals("TradeMe")) {
                    if(RoyaleEconomy.multiCurrencyCfg.getBoolean("use-multicurrency")) {
                        new TradeMeHook();
                        getLogger().info("Hooked into TradeMe with MultiCurrency");
                    }
                }
            }catch(Exception x){

            }
        }
        /*else if(searchOtherPluginsHook){
            try {
                if (e.getPlugin() instanceof FactionsPlugin) {
                    new SaberFactions();
                    getLogger().info("Hooked into SaberFactions");
                }
            }catch(Exception x){

            }
        }*/
    }
    
    public String InputMethod;

    public void selectInputMethod(Player p, String inventoryType, double arg1, double arg2, String bankID){
        if(InputMethod.equalsIgnoreCase("sign")){
            try {
                ArrayList<String> lore = new ArrayList<>();
                lore.add("");
                lore.addAll(this.getConfig().getStringList("input-guis." + inventoryType + ".lines"));
                if(inventoryType.equals("deposit-coins")){
                    SignGUI.builder().setLine(1, lore.get(0))
                            .setLine(2, lore.get(1))
                            .setLine(3, lore.get(2))
                            .setHandler((player, lines) -> {
                                String reply = lines.getLine(0);

                                {
                                    boolean mainMenu=false;
                                    try {
                                        Double toDeposit = Math.abs(RoyaleEconomy.messageHelper.getCoinsFromFormat(reply));
                                        toDeposit = messageHelper.useDecimals ? Math.floor(toDeposit*100) / 100 : Math.floor(toDeposit);
                                        if(toDeposit>arg1)
                                            PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.plugin.getConfig().getString("input-guis.deposit-coins.fail-message").replace("%amount%", messageHelper.numberFormat(toDeposit))));
                                        else{
                                            Double bankBalance;
                                            if(bankID!=null){
                                                PreSharedBankDepositEvent event = new PreSharedBankDepositEvent(bankID, p, toDeposit, true);
                                                Bukkit.getPluginManager().callEvent(event);
                                                if(event.isCancelled())
                                                    return Collections.emptyList();

                                                bankBalance = dataManager.getSharedBankManager().getSharedBankMoneyFromFile(bankID);
                                                if (bankBalance + toDeposit > arg2) {
                                                    toDeposit = arg2 - bankBalance;
                                                    mainMenu=true;
                                                }
                                                if(dataManager.removeMoneyFromFile(p.getUniqueId().toString(), toDeposit)) {
                                                    dataManager.getSharedBankManager().addSharedBankMoneyToFile(bankID, toDeposit);
                                                    if(plugin.bankLogger != null)
                                                        plugin.bankLogger.getLogger().info("[DEPOSIT SHARED VIA CUSTOM AMOUNT] " + p.getName() + " (" + p.getUniqueId() +")" + " deposited " + toDeposit + " coins.");
                                                    final double toDepositFinal = toDeposit;
                                                    RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runAsync((task) -> Bukkit.getPluginManager().callEvent(new SharedBankDepositEvent(p, toDepositFinal)));
                                                    RoyaleEconomy.dataManager.getSharedBankManager().addSharedTransactionLog(bankID, p.getName(), "&a+", toDeposit);
                                                }
                                            }
                                            else {
                                                PreBankDepositEvent event = new PreBankDepositEvent(p, toDeposit, true);
                                                Bukkit.getPluginManager().callEvent(event);
                                                if(event.isCancelled())
                                                    return Collections.emptyList();

                                                bankBalance = dataManager.getBankMoneyFromFile(p.getUniqueId().toString());
                                                if (bankBalance + toDeposit > arg2) {
                                                    toDeposit = arg2 - bankBalance;
                                                    mainMenu=true;
                                                }
                                                if(dataManager.removeMoneyFromFile(p.getUniqueId().toString(), toDeposit)) {
                                                    dataManager.addBankMoneyToFile(p.getUniqueId().toString(), toDeposit);
                                                    if(plugin.bankLogger != null)
                                                        plugin.bankLogger.getLogger().info("[DEPOSIT VIA CUSTOM AMOUNT] " + p.getName() + " (" + p.getUniqueId() +")" + " deposited " + toDeposit + " coins.");
                                                    final double toDepositFinal = toDeposit;
                                                    RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runAsync((task) -> Bukkit.getPluginManager().callEvent(new BankDepositEvent(p, toDepositFinal)));
                                                    RoyaleEconomy.dataManager.addTransactionLog(p.getUniqueId().toString(), p.getName(), "&a+", toDeposit);
                                                }
                                            }
                                            Utils.playSound(p, "menus.bank-deposit");
                                            PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.plugin.getConfig().getString("input-guis.deposit-coins.success-message").replace("%amount%", messageHelper.numberFormat(toDeposit))));
                                        }
                                    }catch(NumberFormatException e){
                                        //e.printStackTrace();
                                        PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.plugin.getConfig().getString("input-guis.deposit-coins.invalid-number")));
                                    }
                                    if(mainMenu){
                                        RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runLaterAsync(() -> {
                                            if (bankID == null)
                                                new MainBankMenu(p);
                                            else
                                                new SharedMainBankMenu(p);
                                        }, 5);
                                    }
                                    else {
                                        RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runLaterAsync(() -> {
                                            if (bankID == null)
                                                new BankDepositMenu(p);
                                            else
                                                new SharedBankDepositMenu(p);
                                        }, 5);
                                    }
                                    return Collections.emptyList();
                                }
                            })
                            .build()
                            .open(p);

                }
                else if(inventoryType.equals("withdraw-coins")){
                    SignGUI.builder().setLine(1, lore.get(0))
                            .setLine(2, lore.get(1))
                            .setLine(3, lore.get(2))
                            .setHandler((player, lines) -> {
                                String reply = lines.getLine(0);

                                {
                                    boolean mainMenu=false;
                                    try {
                                        double toWithdraw2 = Math.abs(RoyaleEconomy.messageHelper.getCoinsFromFormat(reply));
                                        double toWithdraw = messageHelper.useDecimals ? Math.floor(toWithdraw2*100) / 100 : Math.floor(toWithdraw2);
                                        if(toWithdraw>arg1)
                                            PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.plugin.getConfig().getString("input-guis.withdraw-coins.fail-message").replace("%amount%", messageHelper.numberFormat(toWithdraw))));
                                        else{
                                            if(toWithdraw==arg1)
                                                mainMenu=true;
                                            if(bankID!=null){
                                                PreSharedBankWithdrawEvent event = new PreSharedBankWithdrawEvent(bankID, p, toWithdraw, true);
                                                Bukkit.getPluginManager().callEvent(event);
                                                if(event.isCancelled())
                                                    return Collections.emptyList();

                                                if (arg2 == 1 && p.getInventory().firstEmpty()!=-1)
                                                    SharedBankWithdrawMenu.tryBagCreator(toWithdraw, p, bankID);
                                                else {
                                                    if(dataManager.getSharedBankManager().removeSharedBankMoneyToFile(bankID, toWithdraw))
                                                        dataManager.addMoneyToFile(p.getUniqueId().toString(), toWithdraw);
                                                    if(plugin.bankLogger != null)
                                                        plugin.bankLogger.getLogger().info("[WITHDRAW SHARED VIA CUSTOM AMOUNT] " + p.getName() + " (" + p.getUniqueId() +")" + " withdrew " + toWithdraw + " coins.");
                                                }
                                                RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runNextTick((task) -> Bukkit.getPluginManager().callEvent(new SharedBankWithdrawEvent(p, toWithdraw)));
                                                RoyaleEconomy.dataManager.getSharedBankManager().addSharedTransactionLog(bankID, p.getName(), "&c-", toWithdraw);
                                            }
                                            else {
                                                PreBankWithdrawEvent event = new PreBankWithdrawEvent(p, toWithdraw, true);
                                                Bukkit.getPluginManager().callEvent(event);
                                                if(event.isCancelled())
                                                    return Collections.emptyList();

                                                if (arg2 == 1 && p.getInventory().firstEmpty()!=-1)
                                                    BankWithdrawMenu.tryBagCreator(toWithdraw, p);
                                                else {
                                                    dataManager.addMoneyToFile(p.getUniqueId().toString(), toWithdraw);
                                                    dataManager.removeBankMoneyToFile(p.getUniqueId().toString(), toWithdraw);
                                                    if(plugin.bankLogger != null)
                                                        plugin.bankLogger.getLogger().info("[WITHDRAW VIA CUSTOM AMOUNT] " + p.getName() + " (" + p.getUniqueId() +")" + " withdrew " + toWithdraw + " coins.");
                                                }
                                                RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runNextTick((task) -> Bukkit.getPluginManager().callEvent(new BankWithdrawEvent(p, toWithdraw)));
                                                RoyaleEconomy.dataManager.addTransactionLog(p.getUniqueId().toString(), p.getName(), "&c-", toWithdraw);
                                            }
                                            Utils.playSound(p, "menus.bank-withdraw");
                                            PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.plugin.getConfig().getString("input-guis.withdraw-coins.success-message").replace("%amount%", messageHelper.numberFormat(toWithdraw))));
                                        }
                                    }catch(NumberFormatException e){
                                        //e.printStackTrace();
                                        PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.plugin.getConfig().getString("input-guis.withdraw-coins.invalid-number")));
                                    }

                                    if(mainMenu){
                                        RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runLaterAsync(() -> {
                                            if (bankID == null)
                                                new MainBankMenu(p);
                                            else
                                                new SharedMainBankMenu(p);
                                        }, 5);
                                    }
                                    else {
                                        RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runLaterAsync(() -> {
                                            if (bankID == null)
                                                new BankWithdrawMenu(p);
                                            else
                                                new SharedBankWithdrawMenu(p);
                                        }, 5);
                                    }
                                    return Collections.emptyList();
                                }
                            })
                            .build()
                            .open(p);}}catch(Exception x){
                x.printStackTrace();
                this.getLogger().warning("An error has occured on the Sign GUIs. Make sure you have ProtocolLib installed.");
            }
        }
        else if(InputMethod.equalsIgnoreCase("anvil")){
            ArrayList<String> lore = new ArrayList<>();
            for(String line : this.getConfig().getStringList("input-guis."+inventoryType+".lines"))
                lore.add(utilsAPI.chat(p, line));
            ItemStack paper = RoyaleEconomy.itemConstructor.getItem(Material.PAPER, " ", lore);
            if(inventoryType.equals("deposit-coins")){
                new AnvilGUI.Builder()
                        .onClose(player -> {
                            RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runLaterAsync(() -> {
                                if (bankID == null)
                                    new BankDepositMenu(player.getPlayer());
                                else
                                    new SharedBankDepositMenu(player.getPlayer());
                            }, 5);
                        })
                        .onClick((slot, reply) -> {
                            try {
                                if(slot != AnvilGUI.Slot.OUTPUT) {
                                    return Collections.emptyList();
                                }

                                Double toDeposit = Math.abs(RoyaleEconomy.messageHelper.getCoinsFromFormat(reply.getText()));
                                toDeposit = messageHelper.useDecimals ? Math.floor(toDeposit*100) / 100 : Math.floor(toDeposit);
                                if(toDeposit>arg1)
                                    PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.plugin.getConfig().getString("input-guis.deposit-coins.fail-message").replace("%amount%", messageHelper.numberFormat(toDeposit))));
                                else{
                                    Double bankBalance;
                                    if(bankID!=null){
                                        bankBalance = dataManager.getSharedBankManager().getSharedBankMoneyFromFile(bankID);
                                        if (bankBalance + toDeposit > arg2)
                                            toDeposit = arg2 - bankBalance;
                                        if(dataManager.removeMoneyFromFile(p.getUniqueId().toString(), toDeposit)) {
                                            dataManager.getSharedBankManager().addSharedBankMoneyToFile(bankID, toDeposit);
                                            if(plugin.bankLogger != null)
                                                plugin.bankLogger.getLogger().info("[DEPOSIT SHARED VIA CUSTOM AMOUNT] " + p.getName() + " (" + p.getUniqueId() +")" + " deposited " + toDeposit + " coins.");
                                            final double toDepositFinal = toDeposit;
                                            RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runAsync((task) -> Bukkit.getPluginManager().callEvent(new SharedBankDepositEvent(p, toDepositFinal)));
                                            RoyaleEconomy.dataManager.getSharedBankManager().addSharedTransactionLog(bankID, p.getName(), "&a+", toDeposit);
                                        }
                                    }
                                    else {
                                        bankBalance = dataManager.getBankMoneyFromFile(p.getUniqueId().toString());
                                        if (bankBalance + toDeposit > arg2)
                                            toDeposit = arg2 - bankBalance;
                                        if(dataManager.removeMoneyFromFile(p.getUniqueId().toString(), toDeposit)) {
                                            dataManager.addBankMoneyToFile(p.getUniqueId().toString(), toDeposit);
                                            if(plugin.bankLogger != null)
                                                plugin.bankLogger.getLogger().info("[DEPOSIT VIA CUSTOM AMOUNT] " + p.getName() + " (" + p.getUniqueId() +")" + " deposited " + toDeposit + " coins.");
                                            final double toDepositFinal = toDeposit;
                                            RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runAsync((task) -> Bukkit.getPluginManager().callEvent(new BankDepositEvent(p, toDepositFinal)));
                                            RoyaleEconomy.dataManager.addTransactionLog(p.getUniqueId().toString(), p.getName(), "&a+", toDeposit);
                                        }
                                    }
                                    Utils.playSound(p, "menus.bank-deposit");
                                    PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.plugin.getConfig().getString("input-guis.deposit-coins.success-message").replace("%amount%", messageHelper.numberFormat(toDeposit))));
                                }
                            }catch(Exception e){
                                PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.plugin.getConfig().getString("input-guis.deposit-coins.invalid-number")));
                            }
                            return Collections.singletonList(AnvilGUI.ResponseAction.close());
                        })
                        .itemLeft(paper)
                        .title(" ")
                        .text(" ")
                        .plugin(this)
                        .open(p);
            }
            else if(inventoryType.equals("withdraw-coins")){
                new AnvilGUI.Builder()
                        .onClose(player -> {
                            RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runLaterAsync(() -> {
                                if (bankID == null)
                                    new BankWithdrawMenu(p);
                                else
                                    new SharedBankWithdrawMenu(p);
                            }, 5);
                        })
                        .onClick((slot, reply) -> {
                            try {
                                if(slot != AnvilGUI.Slot.OUTPUT) {
                                    return Collections.emptyList();
                                }

                                double toWithdraw2 = Math.abs(RoyaleEconomy.messageHelper.getCoinsFromFormat(reply.getText()));
                                double toWithdraw = messageHelper.useDecimals ? Math.floor(toWithdraw2*100) / 100 : Math.floor(toWithdraw2);
                                if(toWithdraw>arg1)
                                    PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.plugin.getConfig().getString("input-guis.withdraw-coins.fail-message").replace("%amount%", messageHelper.numberFormat(toWithdraw))));
                                else{
                                    if(bankID!=null){
                                        if (arg2 == 1 && p.getInventory().firstEmpty()!=-1)
                                            SharedBankWithdrawMenu.tryBagCreator(toWithdraw, p, bankID);
                                        else {
                                            if(dataManager.getSharedBankManager().removeSharedBankMoneyToFile(bankID, toWithdraw))
                                                dataManager.addMoneyToFile(p.getUniqueId().toString(), toWithdraw);
                                            if(plugin.bankLogger != null)
                                                plugin.bankLogger.getLogger().info("[WITHDRAW SHARED VIA CUSTOM AMOUNT] " + p.getName() + " (" + p.getUniqueId() +")" + " withdrew " + toWithdraw + " coins.");
                                        }
                                        RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runNextTick((task) -> Bukkit.getPluginManager().callEvent(new SharedBankWithdrawEvent(p, toWithdraw)));
                                        RoyaleEconomy.dataManager.getSharedBankManager().addSharedTransactionLog(bankID, p.getName(), "&c-", toWithdraw);
                                    }
                                    else {
                                        if (arg2 == 1 && p.getInventory().firstEmpty()!=-1)
                                            BankWithdrawMenu.tryBagCreator(toWithdraw, p);
                                        else {
                                            dataManager.addMoneyToFile(p.getUniqueId().toString(), toWithdraw);
                                            dataManager.removeBankMoneyToFile(p.getUniqueId().toString(), toWithdraw);
                                            if(plugin.bankLogger != null)
                                                plugin.bankLogger.getLogger().info("[WITHDRAW VIA CUSTOM AMOUNT] " + p.getName() + " (" + p.getUniqueId() +")" + " withdrew " + toWithdraw + " coins.");
                                        }
                                        RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runNextTick((task) -> Bukkit.getPluginManager().callEvent(new BankWithdrawEvent(p, toWithdraw)));
                                        RoyaleEconomy.dataManager.addTransactionLog(p.getUniqueId().toString(), p.getName(), "&c-", toWithdraw);
                                    }
                                    Utils.playSound(p, "menus.bank-withdraw");
                                    PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.plugin.getConfig().getString("input-guis.withdraw-coins.success-message").replace("%amount%", messageHelper.numberFormat(toWithdraw))));
                                }
                            }catch(Exception e){
                                PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.plugin.getConfig().getString("input-guis.withdraw-coins.invalid-number")));
                            }
                            return Collections.singletonList(AnvilGUI.ResponseAction.close());
                        })
                        .itemLeft(paper)
                        .title(" ")
                        .text(" ")
                        .plugin(this)
                        .open(p);
            }
        }
        else{
            p.closeInventory();
            for(String line : this.getConfig().getStringList("input-guis."+inventoryType+".lines"))
                PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, line));
            if(inventoryType.equals("deposit-coins")){
                new ChatListener(p, (reply) -> {
                    try {
                        Double toDeposit = Math.abs(RoyaleEconomy.messageHelper.getCoinsFromFormat(reply));
                        toDeposit = messageHelper.useDecimals ? Math.floor(toDeposit*100) / 100 : Math.floor(toDeposit);
                        if(toDeposit>arg1)
                            PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.plugin.getConfig().getString("input-guis.deposit-coins.fail-message").replace("%amount%", messageHelper.numberFormat(toDeposit))));
                        else{
                            Double bankBalance;
                            if(bankID!=null){
                                bankBalance = dataManager.getSharedBankManager().getSharedBankMoneyFromFile(bankID);
                                if (bankBalance + toDeposit > arg2)
                                    toDeposit = arg2 - bankBalance;
                                if(dataManager.removeMoneyFromFile(p.getUniqueId().toString(), toDeposit)) {
                                    dataManager.getSharedBankManager().addSharedBankMoneyToFile(bankID, toDeposit);
                                    if(plugin.bankLogger != null)
                                        plugin.bankLogger.getLogger().info("[DEPOSIT SHARED VIA CUSTOM AMOUNT] " + p.getName() + " (" + p.getUniqueId() +")" + " deposited " + toDeposit + " coins.");
                                    final double toDepositFinal = toDeposit;
                                    RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runAsync((task) -> Bukkit.getPluginManager().callEvent(new SharedBankDepositEvent(p, toDepositFinal)));
                                    RoyaleEconomy.dataManager.getSharedBankManager().addSharedTransactionLog(bankID, p.getName(), "&a+", toDeposit);
                                }
                            }
                            else {
                                bankBalance = dataManager.getBankMoneyFromFile(p.getUniqueId().toString());
                                if (bankBalance + toDeposit > arg2)
                                    toDeposit = arg2 - bankBalance;
                                if(dataManager.removeMoneyFromFile(p.getUniqueId().toString(), toDeposit)) {
                                    dataManager.addBankMoneyToFile(p.getUniqueId().toString(), toDeposit);
                                    if(plugin.bankLogger != null)
                                        plugin.bankLogger.getLogger().info("[DEPOSIT VIA CUSTOM AMOUNT] " + p.getName() + " (" + p.getUniqueId() +")" + " deposited " + toDeposit + " coins.");
                                    final double toDepositFinal = toDeposit;
                                    RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runAsync((task) -> Bukkit.getPluginManager().callEvent(new BankDepositEvent(p, toDepositFinal)));
                                    RoyaleEconomy.dataManager.addTransactionLog(p.getUniqueId().toString(), p.getName(), "&a+", toDeposit);
                                }
                            }
                            Utils.playSound(p, "menus.bank-deposit");
                            PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.plugin.getConfig().getString("input-guis.deposit-coins.success-message").replace("%amount%", messageHelper.numberFormat(toDeposit))));
                        }
                    }catch(Exception e){
                        PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.plugin.getConfig().getString("input-guis.deposit-coins.invalid-number")));
                    }
                    RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runLaterAsync(() -> {
                        if (bankID == null)
                            new BankDepositMenu(p);
                        else
                            new SharedBankDepositMenu(p);
                    }, 5);
                });
            }
            else if(inventoryType.equals("withdraw-coins")){
                new ChatListener(p, (reply) ->{
                    try {
                        double toWithdraw2 = Math.abs(RoyaleEconomy.messageHelper.getCoinsFromFormat(reply));
                        double toWithdraw = messageHelper.useDecimals ? Math.floor(toWithdraw2*100) / 100 : Math.floor(toWithdraw2);
                        if(toWithdraw>arg1)
                            PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.plugin.getConfig().getString("input-guis.withdraw-coins.fail-message").replace("%amount%", messageHelper.numberFormat(toWithdraw))));
                        else{
                            if(bankID!=null){
                                if (arg2 == 1 && p.getInventory().firstEmpty()!=-1)
                                    SharedBankWithdrawMenu.tryBagCreator(toWithdraw, p, bankID);
                                else {
                                    if(dataManager.getSharedBankManager().removeSharedBankMoneyToFile(bankID, toWithdraw))
                                        dataManager.addMoneyToFile(p.getUniqueId().toString(), toWithdraw);
                                    if(plugin.bankLogger != null)
                                        plugin.bankLogger.getLogger().info("[WITHDRAW SHARED VIA CUSTOM AMOUNT] " + p.getName() + " (" + p.getUniqueId() +")" + " withdrew " + toWithdraw + " coins.");
                                    else {
                                        PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.plugin.getConfig().getString("input-guis.withdraw-coins.fail-message").replace("%amount%", messageHelper.numberFormat(toWithdraw))));
                                    }
                                }
                                RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runNextTick((task) -> Bukkit.getPluginManager().callEvent(new SharedBankWithdrawEvent(p, toWithdraw)));
                                RoyaleEconomy.dataManager.getSharedBankManager().addSharedTransactionLog(bankID, p.getName(), "&c-", toWithdraw);
                            }
                            else {
                                if (arg2 == 1 && p.getInventory().firstEmpty()!=-1)
                                    BankWithdrawMenu.tryBagCreator(toWithdraw, p);
                                else {
                                    dataManager.addMoneyToFile(p.getUniqueId().toString(), toWithdraw);
                                    dataManager.removeBankMoneyToFile(p.getUniqueId().toString(), toWithdraw);
                                    if(plugin.bankLogger != null)
                                        plugin.bankLogger.getLogger().info("[WITHDRAW VIA CUSTOM AMOUNT] " + p.getName() + " (" + p.getUniqueId() +")" + " withdrew " + toWithdraw + " coins.");
                                }
                                RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runNextTick((task) -> Bukkit.getPluginManager().callEvent(new BankWithdrawEvent(p, toWithdraw)));
                                RoyaleEconomy.dataManager.addTransactionLog(p.getUniqueId().toString(), p.getName(), "&c-", toWithdraw);
                            }
                            Utils.playSound(p, "menus.bank-withdraw");
                            PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.plugin.getConfig().getString("input-guis.withdraw-coins.success-message").replace("%amount%", messageHelper.numberFormat(toWithdraw))));
                        }
                    }catch(Exception e){
                        PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.plugin.getConfig().getString("input-guis.withdraw-coins.invalid-number")));
                    }
                    RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runLaterAsync(() -> {
                        if (bankID == null)
                            new BankWithdrawMenu(p);
                        else
                            new SharedBankWithdrawMenu(p);
                    }, 5);
                });
            }
        }
    }

    private void setupItemConstructor(){
        String version = Bukkit.getVersion();
        if(!version.contains("1.21") && (version.contains("1.8") || version.contains("1.9") || version.contains("1.10") || version.contains("1.11") || version.contains("1.12"))) {
            itemConstructor = new ItemConstructorLegacy();
            upperVersion=false;
        }
        else {
            itemConstructor = new ItemConstructorNew();
            upperVersion=true;
        }
    }

    private void setupDataManager(){
        if(getConfig().getBoolean("mysql.use-mysql")){
            dataManager=new DataManagerMySQL(noEconomy);
            if(!noEconomy)
                playerMoneyCache=new PlayerMoneyCacheMySQL();
            new ExtendCoinsMaximum();
        }
        else {
            new SQLLoad();
            dataManager = new DataManagerSQL(noEconomy);
            playerMoneyCache =new PlayerMoneyCacheSQL();
            RoyaleEconomy.plugin.getCommand("rec_extend_coins_maximum_value").setExecutor(disabledCommand);
        }
    }

    public void setupBoosters(){
        if(boostersCfg.getBoolean("use-boosters")) {
            boosters = new BoostersActive();
            new BoosterCommand();
            new BoosterAdmin();
        }
        else{
            boosters=new DisabledBoosters();
        }
    }

    public void setupBlackMarket(){
        if(blackMarketCfg.getBoolean("use-blackmarket")){
            new BlackMarket();
            new BlackMarketCommand();
        }
    }

    public void setupGambling(){
        if(gamblingCfg.getBoolean("use-gambling")){
            new Gambling();
            new GamblingCommand();
        }
    }

    public void setupTimeRewards(){
        if(timeRewardsCfg.getBoolean("use-time-rewards")){
            timeRewardsManager=new TimeRewardsManager();
        }
    }

    public static DisabledCommand disabledCommand;

    public BankLogger bankLogger;

    private final FoliaLib schedulerLib = new FoliaLib(this);

    public FoliaLib getSchedulerLib() {
        return schedulerLib;
    }

    @Override
    public void onEnable(){
        this.plugin=this;
        saveDefaultConfig();
        new GenerateFiles();
        if(!this.getConfig().getKeys(false).contains("config-version") || this.getConfig().getDouble("config-version")<2.43){
            getLogger().info("Update detected, triggered file conversion...");
            new UpdateFiles();
            saveDefaultConfig();
            killCoinsAndPurseDeathCfg = Utf8YamlConfiguration.loadConfiguration(new File(RoyaleEconomy.plugin.getDataFolder(), "killCoinsAndPurseDeath.yml"));
            shopsCfg = Utf8YamlConfiguration.loadConfiguration(new File(RoyaleEconomy.plugin.getDataFolder(), "shops.yml"));
            permissionsCfg = Utf8YamlConfiguration.loadConfiguration(new File(RoyaleEconomy.plugin.getDataFolder(), "permissions.yml"));
            commandsCfg = Utf8YamlConfiguration.loadConfiguration(new File(RoyaleEconomy.plugin.getDataFolder(), "commands.yml"));
            menusCfg = Utf8YamlConfiguration.loadConfiguration(new File(RoyaleEconomy.plugin.getDataFolder(), "menus.yml"));
            boostersCfg = Utf8YamlConfiguration.loadConfiguration(new File(RoyaleEconomy.plugin.getDataFolder(), "boosters.yml"));
        }
        TermsOfUse terms = new TermsOfUse();
        if(!terms.getAccepted())
            return;
        disabledCommand=new DisabledCommand();
        noEconomy=getConfig().getBoolean("no-economy");
        InputMethod=getConfig().getString("input-guis.input-type");

        if(getConfig().getBoolean("use-bank-logs")) {
            bankLogger = new BankLogger();
        }

        searchSkyBlockHook=getConfig().getBoolean("search-skyblock-hook");
        searchOtherPluginsHook=getConfig().getBoolean("other-plugins-hook");
        Bukkit.getPluginManager().registerEvents(this, this);
        new Metrics(this, 8598);
        File directory = new File(this.getDataFolder(), "database");
        if(!directory.exists())
            directory.mkdir();

        setupDataManager();
        setupItemConstructor();
        customItemsHandler = new CustomItemsHandler();

        Bukkit.getPluginManager().registerEvents(BankMenuCooldown.getInstance(), this);

        new Utils();
        staticValues=new StaticValues();
        new PlayerMessageHandler();
        messageHelper=new MessageHelper();
        talismanHandler=new TalismanHandler();


        if(RoyaleEconomy.killCoinsAndPurseDeathCfg.getBoolean("kill-coins.use-kill-coins")) {
            killCoinsMainHandle = new KillCoinsMainHandle();
            new KillCoinsCommand();
        }

        new ShopsLoad();
        new ShopPlayerCache();
        new MultiCurrencyHandler();

        new AdminCommands();
        /*
        try {
            new BalanceCommand();
        }catch(Exception x){

        }
        if(this.getConfig().getBoolean("use-only-one-bank"))
            new OnlyBankCommand();
        else
            new BankCommand();
        if(getConfig().getBoolean("use-interest"))
            new InterestCommand();
        try{
            new PayCommand();
        }catch(Exception x){

        }
        try {
            new BalanceTopCommand();
        }catch(Exception x){

        }
        new MoneyBagCommand();
        if(RoyaleEconomy.coinBagsAndTalismansCfg.getBoolean("talismans.use-talismans"))
            new Talismans();
        else
            getCommand("retalismans").setExecutor(disabledCommand);
        new SharedBankCommand();
        new ReShopCommand();
         */

        if(noEconomy){
            new ExportCommand();
            getCommand("royaleeconomyimport_essentials").setExecutor(disabledCommand);
            getCommand("royaleeconomyimport_cmi").setExecutor(disabledCommand);
        }
        else {
            new EssentialsImport();
            new CMIImport();
            getCommand("royaleeconomy_export").setExecutor(disabledCommand);
        }

        //if(getConfig().getBoolean("piggy-bank.use-piggy-banks"))
        //    new PiggyBankCommand();

        if( Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            if(utilsAPI==null) {

                if (getConfig().getBoolean("use-dynamic-placeholder")) {
                    new DynamicCoinsPlaceholder();
                }
                if(noEconomy)
                    new PlaceholderRegisterNoEconomy(this).register();
                else
                    new PlaceholderRegister(this).register();
                utilsAPI = new PlaceholderAPISupportYes();
            }
        }
        else
            utilsAPI=new PlaceholderAPISupportNo();

        if(shopsCfg.getBoolean("shop-sell-limit.use"))
            new ShopSellLimit();
        if(shopsCfg.getBoolean("shop-buy-limit.use"))
            new ShopBuyLimit();

        if(searchSkyBlockHook) {

            if (Bukkit.getPluginManager().getPlugin("SuperiorSkyblock2") != null) {
                new SuperiorSkyBlock();
                getLogger().info("Hooked into SuperiorSkyblock2");
            }
            else if (Bukkit.getPluginManager().getPlugin("BentoBox") != null) {
                new BentoBox();
                getLogger().info("Hooked into BentoBox");
            }
            else if(Bukkit.getPluginManager().getPlugin("FabledSkyBlock")!=null){
                new FabledSkyBlock();
                getLogger().info("Hooked into FabledSkyBlock");
            }
            else if(Bukkit.getPluginManager().getPlugin("IridiumSkyBlock")!=null){
                new IridiumSkyBlock();
                getLogger().info("Hooked into IridiumSkyBlock");
            }
            else if(Bukkit.getPluginManager().getPlugin("GalacticSkyBlock")!=null){
                new GalacticSkyBlock();
                getLogger().info("Hooked into GalacticSkyBlock");
            }
        }
        /*else if(searchOtherPluginsHook){
            try {
                if (Bukkit.getPluginManager().getPlugin("Factions") instanceof FactionsPlugin) {
                    new SaberFactions();
                    getLogger().info("Hooked into SaberFactions");
                }
            }catch(Exception x){

            }
        }*/

        if(noEconomy) {
            new NoEconomyHandler();
            balanceTopNoEconomy=new BalanceTopNoEconomy();
        }
        else {
            Plugin vault = Bukkit.getPluginManager().getPlugin("Vault");
            if (vault != null && economy == null) {
                Initializer.economyEnabled(vault);
            }
        }

        apiHandler=new APIHandler();
        new Events();
        setupBoosters();
        setupBlackMarket();
        setupGambling();
        setupTimeRewards();
        new SellAllManager();

        new TransferFunctions();

        new DynamicCommandsSetup();
    }

    @Override
    public void onDisable() {
//        Bukkit.getScheduler().cancelTasks(this);
        schedulerLib.getScheduler().cancelAllTasks();
        DynamicCommandsSetup.unregisterCommands();
        if(bankLogger != null)
            bankLogger.cleanup();
        if (killCoinsMainHandle != null)
            if (killCoinsAndPurseDeathCfg.getBoolean("kill-coins.use-kill-coins"))
                killCoinsMainHandle.killCoins.saveData();
        if(ShopBuyLimit.playerValues!=null)
            ShopBuyLimit.saveData();
        ShopSellLimit.saveLimits();
        if(playerMoneyCache !=null) {
            playerMoneyCache.finalSave();
            getLogger().info("Safety save of user balances done!");
        }
        if(BlackMarket.refreshCheck!=null)
            BlackMarketMenu.saveCache();
        try {
            PayCommand.saveToFile();
        }catch(Exception x){

        }
        RedisHandler.close();
        getLogger().info("Plugin disabled!");
    }
}
