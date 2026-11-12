package me.qKing12.RoyaleEconomy;

import com.earth2me.essentials.Essentials;
import com.tcoded.folialib.FoliaLib;
import me.qKing12.RoyaleEconomy.API.APIHandler;
import me.qKing12.RoyaleEconomy.API.Events.*;
import me.qKing12.RoyaleEconomy.Commands.*;
import me.qKing12.RoyaleEconomy.Commands.dynamic.DynamicCommandsSetup;
import me.qKing12.RoyaleEconomy.DataManager.Cache.PlayerMoneyCache;
import me.qKing12.RoyaleEconomy.DataManager.Cache.PlayerMoneyCacheMySQL;
import me.qKing12.RoyaleEconomy.DataManager.Cache.PlayerMoneyCacheSQL;
import me.qKing12.RoyaleEconomy.DataManager.*;
import me.qKing12.RoyaleEconomy.DataManager.Cache.redis.RedisHandler;
import me.qKing12.RoyaleEconomy.Economy.BalanceTopNoEconomy;
import me.qKing12.RoyaleEconomy.Events.Events;
import me.qKing12.RoyaleEconomy.Hooks.*;
import me.qKing12.RoyaleEconomy.Hooks.bStats.Metrics;
import me.qKing12.RoyaleEconomy.InputGUIs.PaperAmountDialog;
import me.qKing12.RoyaleEconomy.InputGUIs.PaperDialogSupport;
import me.qKing12.RoyaleEconomy.Menus.*;
import me.qKing12.RoyaleEconomy.PlaceholderAPISupport.*;
import me.qKing12.RoyaleEconomy.utils.*;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

public class RoyaleEconomy extends JavaPlugin implements Listener {

    public static RoyaleEconomy plugin;

    public static DataManager dataManager;

    public static PlayerMoneyCache playerMoneyCache;
    public static StaticValues staticValues;

    public static Economy economy;
    public static MessageHelper messageHelper;
    public static ItemConstructor itemConstructor;
    public static PlaceholderAPISupport utilsAPI;
    public static boolean upperVersion;
    public static SharedBankHook hooked;

    public static FileConfiguration bankUpgradesCfg;
    public static FileConfiguration coinBagsAndTalismansCfg;
    public static FileConfiguration commandsCfg;
    public static FileConfiguration menusCfg;
    public static FileConfiguration permissionsCfg;
    public static FileConfiguration soundsCfg;

    public static APIHandler apiHandler;

    public static APIHandler getApiHandler() {
        return apiHandler;
    }

    private boolean searchSkyBlockHook;

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
        else if(searchSkyBlockHook) {
            if (e.getPlugin().getName().equals("SuperiorSkyblock2")) {
                new SuperiorSkyBlock();
                getLogger().info("Hooked into SuperiorSkyblock2");
            }
            else if (e.getPlugin().getName().equals("BentoBox")) {
                new BentoBox();
                getLogger().info("Hooked into BentoBox");
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
    
    public void selectInputMethod(Player p, String inventoryType, double arg1, double arg2, String bankID){
        if(!inventoryType.equals("deposit-coins") && !inventoryType.equals("withdraw-coins"))
            return;

        if(!PaperDialogSupport.isAvailable()) {
            PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, getConfig().getString("input-guis.dialog.unsupported-server",
                    "&cThis server does not support Paper dialogs. Please use Paper 1.21.7 or newer.")));
            return;
        }

        try {
            PaperAmountDialog.open(p, inventoryType, arg1, arg2, bankID);
        } catch (Throwable throwable) {
            getLogger().warning("Could not open the Paper amount dialog: " + throwable.getMessage());
            PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, getConfig().getString("input-guis.dialog.unsupported-server",
                    "&cThis server does not support Paper dialogs. Please use Paper 1.21.7 or newer.")));
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
            UpdateFiles.loadFiles();
        }
        TermsOfUse terms = new TermsOfUse();
        if(!terms.getAccepted())
            return;
        disabledCommand=new DisabledCommand();
        noEconomy=getConfig().getBoolean("no-economy");

        if(getConfig().getBoolean("use-bank-logs")) {
            bankLogger = new BankLogger();
        }

        searchSkyBlockHook=getConfig().getBoolean("search-skyblock-hook");
        Bukkit.getPluginManager().registerEvents(this, this);
        new Metrics(this, 8598);
        File directory = new File(this.getDataFolder(), "database");
        if(!directory.exists())
            directory.mkdir();

        setupDataManager();
        setupItemConstructor();

        Bukkit.getPluginManager().registerEvents(BankMenuCooldown.getInstance(), this);

        new Utils();
        staticValues=new StaticValues();
        new PlayerMessageHandler();
        messageHelper=new MessageHelper();

        new AdminCommands();

        if(noEconomy){
            new ExportCommand();
            getCommand("royaleeconomyimport_essentials").setExecutor(disabledCommand);
            getCommand("royaleeconomyimport_cmi").setExecutor(disabledCommand);
        }
        else {
            new EssentialsImport();
            getCommand("royaleeconomyimport_cmi").setExecutor(disabledCommand);
            getCommand("royaleeconomy_export").setExecutor(disabledCommand);
        }

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

        if(searchSkyBlockHook) {

            if (Bukkit.getPluginManager().getPlugin("SuperiorSkyblock2") != null) {
                new SuperiorSkyBlock();
                getLogger().info("Hooked into SuperiorSkyblock2");
            }
            else if (Bukkit.getPluginManager().getPlugin("BentoBox") != null) {
                new BentoBox();
                getLogger().info("Hooked into BentoBox");
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
        if(playerMoneyCache !=null) {
            playerMoneyCache.finalSave();
            getLogger().info("Safety save of user balances done!");
        }
        RedisHandler.close();
        getLogger().info("Plugin disabled!");
    }
}
