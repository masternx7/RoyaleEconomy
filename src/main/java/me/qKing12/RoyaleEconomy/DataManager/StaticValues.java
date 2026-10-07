package me.qKing12.RoyaleEconomy.DataManager;

import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import me.qKing12.RoyaleEconomy.utils.UpdateFiles;
import me.qKing12.RoyaleEconomy.utils.Utf8YamlConfiguration;
import me.qKing12.RoyaleEconomy.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;


public class StaticValues {

    public  String serverExecutorName;
    public  int defaultCoins;
    public  int defaultBankCoins;

    public boolean onlineInterestOnly;
    
    //time units
    public  String days;
    public  String day;
    public  String hours;
    public  String hour;
    public  String minutes;
    public  String minute;
    public  String seconds;
    public  String second;
    public  String soon;
    
    public  String short_day;
    public  String short_hour;
    public  String short_minute;
    public  String short_second;
    //end time units

    //main bank menu values
    public  int mainBankMenuSize;
    public List<Integer> depositItemSlot;
    public  List<Integer> withdrawItemSlot;
    public  List<Integer> transactionItemSlot;
    public  List<Integer> closeItemSlot;
    public  List<Integer> informationSlot;
    public  List<Integer> bankUpgradesSlot;
    public  ItemStack backgroundGlass;
    public  ArrayList<String> backCommandsMainBank;
    //end of main bank menu values

    //bank upgrades menu values
    public  int bankUpgradesMenuSize;
    public  ItemStack backgroundGlassBankUpgrades;
    public  List<Integer> goBackItemSlot;
    //end of bank upgrades menu values

    //bank deposit menu values
    public  int bankDepositMenuSize;
    public  ItemStack backgroundGlassDeposit;
    public  List<Integer> goBackItemSlotDeposit;
    public  List<Integer> customAmountItemSlot;
    //end of bank deposit menu values

    //bank withdraw menu values
    public  int bankWithdrawMenuSize;
    public  ItemStack backgroundGlassWithdraw;
    public  List<Integer> goBackItemSlotWithdraw;
    public  List<Integer> customAmountItemSlotWithdraw;
    public  List<Integer> withdrawAsBagSlot;
    //end of bank withdraw menu values

    //shared main bank menu values
    public  int sharedmainBankMenuSize;
    public  List<Integer> shareddepositItemSlot;
    public  List<Integer> sharedwithdrawItemSlot;
    public  List<Integer> sharedtransactionItemSlot;
    public  List<Integer> sharedcloseItemSlot;
    public  List<Integer> sharedinformationSlot;
    public  List<Integer> sharedbankUpgradesSlot;
    public  ItemStack sharedbackgroundGlass;
    public  List<Integer> sharedPlayersInformationSlot;
    public  ArrayList<String> backCommandsSharedMainBank;
    //end of shared main bank menu values

    //shared bank upgrades menu values
    public  int sharedbankUpgradesMenuSize;
    public  ItemStack sharedbackgroundGlassBankUpgrades;
    public  List<Integer> sharedgoBackItemSlot;
    //end of shared bank upgrades menu values

    //shared bank deposit menu values
    public  int sharedbankDepositMenuSize;
    public  ItemStack sharedbackgroundGlassDeposit;
    public  List<Integer> sharedgoBackItemSlotDeposit;
    public  List<Integer> sharedcustomAmountItemSlot;
    //end of shared bank deposit menu values

    //shared bank withdraw menu values
    public  int sharedbankWithdrawMenuSize;
    public  ItemStack sharedbackgroundGlassWithdraw;
    public  List<Integer> sharedgoBackItemSlotWithdraw;
    public  List<Integer> sharedcustomAmountItemSlotWithdraw;
    public  List<Integer> sharedwithdrawAsBagSlot;
    //end of shared bank withdraw menu values

    //moneybag & talismans related values
    public  ItemStack moneyBagItem;
    //end of moneybag & talismans related values

    //shop edit values

    public  ItemStack colorChange;
    public  ItemStack goBackShopEdit;
    public  ItemStack renameShop;
    public  ItemStack deleteShop;
    public  ItemStack deleteShopItem;
    public  ItemStack nextPageShopItem;
    public  ItemStack previousPageShopItem;
    public  ItemStack goBackShopItem;
    public  ItemStack sellValue;
    public ItemStack buyPermissionItem;
    public  ItemStack buyValue;
    public  ItemStack detailed;
    public  ItemStack notDetailed;
    public ItemStack detailedMenuModeNotOnly;
    public ItemStack detaimedMenuModeOnly;
    public  ItemStack exclusive;
    public  ItemStack notExclusive;
    public  ItemStack sellItemsGetBack;
    public  ItemStack shopCloseCommands;
    //end of shop edit values

    //piggy bank
    public  ItemStack piggyBank;
    public  int piggyBankCooldown;

    public  long cooldownBank;
    public  long cooldownSharedBank;

    public  String bankFull;
    public  String bankEmpty;

    public  boolean useShopBackItem;
    public  List<Integer> shopBackItemSlot;
    public  ItemStack shopBackItem;
    public  ArrayList<String> shopBackItemCommands;

    public String togglePayOn;
    public String togglePayOff;
    public String toggleMessage;

    public int balanceTopMaximumPages;
    public int balanceTopDisplayPerPage;

    public String killCoinsMessagesOn;
    public String killCoinsMessagesOff;

    public List<Integer> blackMarketRefreshItemSlot;
    public ItemStack blackMarketRefreshItem;

    public boolean disableBuyBack;

    public List<Integer> loadSlots(FileConfiguration config, String setting){
        List<Integer> list = config.getIntegerList(setting);

        if (!list.isEmpty())
            return list;

        list.add(config.getInt(setting));
        return list;
    }

    public void reloadValues(){
        RoyaleEconomy.plugin.reloadConfig();
        UpdateFiles.loadFiles();

        loadValues();
    }

    public void loadValues(){
        onlineInterestOnly=RoyaleEconomy.plugin.getConfig().getBoolean("interest-online-only");

        balanceTopMaximumPages=RoyaleEconomy.commandsCfg.getInt("commands.balancetop.maximum-pages");
        balanceTopDisplayPerPage = RoyaleEconomy.commandsCfg.getInt("commands.balancetop.display-per-page");

        FileConfiguration config = RoyaleEconomy.plugin.getConfig();

        bankFull=Utils.chat(RoyaleEconomy.menusCfg.getString("extra-messages.bank-is-full"));
        bankEmpty=Utils.chat(RoyaleEconomy.menusCfg.getString("extra-messages.bank-is-empty"));

        cooldownBank=RoyaleEconomy.plugin.getConfig().getLong("bank-cooldown")*1000;
        cooldownSharedBank=RoyaleEconomy.plugin.getConfig().getLong("sharedbank-cooldown")*1000;

        serverExecutorName = Utils.chat(config.getString("server-executor-name"));
        defaultCoins = RoyaleEconomy.plugin.getConfig().getInt("default-balance");
        defaultBankCoins = RoyaleEconomy.plugin.getConfig().getInt("default-bank-balance");

        days = config.getString("time-units.days");
        day = config.getString("time-units.day");
        hours = config.getString("time-units.hours");
        hour = config.getString("time-units.hour");
        minutes = config.getString("time-units.minutes");
        minute = config.getString("time-units.minute");
        seconds = config.getString("time-units.seconds");
        second = config.getString("time-units.second");
        soon = config.getString("time-units.soon");

        short_day = config.getString("short-time-units.day");
        short_hour = config.getString("short-time-units.hour");
        short_minute = config.getString("short-time-units.minute");
        short_second = config.getString("short-time-units.second");

        mainBankMenuSize= RoyaleEconomy.menusCfg.getInt("menus.main-bank-menu.size");
        depositItemSlot = loadSlots(RoyaleEconomy.menusCfg, "menus.main-bank-menu.deposit-item.slot");
        withdrawItemSlot = loadSlots(RoyaleEconomy.menusCfg, "menus.main-bank-menu.withdraw-item.slot");
        transactionItemSlot = loadSlots(RoyaleEconomy.menusCfg, "menus.main-bank-menu.transaction-history.slot");
        informationSlot = loadSlots(RoyaleEconomy.menusCfg, "menus.main-bank-menu.information-item.slot");
        bankUpgradesSlot = loadSlots(RoyaleEconomy.menusCfg, "menus.main-bank-menu.bank-upgrades.slot");
        closeItemSlot = loadSlots(RoyaleEconomy.menusCfg, "menus.main-bank-menu.close-item.slot");

        backCommandsMainBank=(ArrayList)RoyaleEconomy.menusCfg.getStringList("menus.main-bank-menu.close-item.commands");
        backCommandsSharedMainBank=(ArrayList)RoyaleEconomy.menusCfg.getStringList("shared-menus.main-bank-menu.close-item.commands");

        boolean customModelData=RoyaleEconomy.upperVersion && !Bukkit.getVersion().contains("1.13");

        if(RoyaleEconomy.menusCfg.getBoolean("menus.main-bank-menu.use-background-glass")) {
            String color=RoyaleEconomy.menusCfg.getString("menus.main-bank-menu.background-glass-color");
            if(color.startsWith("item:")){
                backgroundGlass = RoyaleEconomy.itemConstructor.getItem(color.substring(5), " ", null);
            }
            else {
                int data = 0;
                if (color.split("#").length > 1) {
                    data = Integer.parseInt(color.split("#")[1]);
                    color = color.split("#")[0];
                }
                backgroundGlass = RoyaleEconomy.itemConstructor.getItem("160:" + color, " ", null);
                if (customModelData && data != 0) {
                    ItemMeta meta = backgroundGlass.getItemMeta();
                    meta.setCustomModelData(data);
                    backgroundGlass.setItemMeta(meta);
                }
            }
        }

        bankUpgradesMenuSize = RoyaleEconomy.menusCfg.getInt("menus.bank-upgrades-menu.size");
        goBackItemSlot = loadSlots(RoyaleEconomy.menusCfg, "menus.bank-upgrades-menu.go-back-item.slot");
        if(RoyaleEconomy.menusCfg.getBoolean("menus.bank-upgrades-menu.use-background-glass")) {
            String color=RoyaleEconomy.menusCfg.getString("menus.bank-upgrades-menu.background-glass-color");
            if(color.startsWith("item:")){
                backgroundGlassBankUpgrades = RoyaleEconomy.itemConstructor.getItem(color.substring(5), " ", null);
            }
            else {
                int data = 0;
                if (color.split("#").length > 1) {
                    data = Integer.parseInt(color.split("#")[1]);
                    color = color.split("#")[0];
                }
                backgroundGlassBankUpgrades = RoyaleEconomy.itemConstructor.getItem("160:" + color, " ", null);
                if (customModelData && data != 0) {
                    ItemMeta meta = backgroundGlassBankUpgrades.getItemMeta();
                    meta.setCustomModelData(data);
                    backgroundGlassBankUpgrades.setItemMeta(meta);
                }
            }
        }

        bankDepositMenuSize = RoyaleEconomy.menusCfg.getInt("menus.deposit-coins-menu.size");
        goBackItemSlotDeposit = loadSlots(RoyaleEconomy.menusCfg, "menus.deposit-coins-menu.go-back-item.slot");
        customAmountItemSlot = loadSlots(RoyaleEconomy.menusCfg, "menus.deposit-coins-menu.custom-amount-item.slot");
        if(RoyaleEconomy.menusCfg.getBoolean("menus.deposit-coins-menu.use-background-glass")) {
            String color=RoyaleEconomy.menusCfg.getString("menus.deposit-coins-menu.background-glass-color");
            if(color.startsWith("item:")){
                backgroundGlassDeposit = RoyaleEconomy.itemConstructor.getItem(color.substring(5), " ", null);
            }
            else {
                int data = 0;
                if (color.split("#").length > 1) {
                    data = Integer.parseInt(color.split("#")[1]);
                    color = color.split("#")[0];
                }
                backgroundGlassDeposit = RoyaleEconomy.itemConstructor.getItem("160:" + color, " ", null);
                if (customModelData && data != 0) {
                    ItemMeta meta = backgroundGlassDeposit.getItemMeta();
                    meta.setCustomModelData(data);
                    backgroundGlassDeposit.setItemMeta(meta);
                }
            }
        }

        bankWithdrawMenuSize = RoyaleEconomy.menusCfg.getInt("menus.withdraw-coins-menu.size");
        goBackItemSlotWithdraw = loadSlots(RoyaleEconomy.menusCfg, "menus.withdraw-coins-menu.go-back-item.slot");
        customAmountItemSlotWithdraw = loadSlots(RoyaleEconomy.menusCfg, "menus.withdraw-coins-menu.custom-amount-item.slot");
        withdrawAsBagSlot = loadSlots(RoyaleEconomy.menusCfg, "menus.withdraw-coins-menu.withdraw-as-bag.slot");
        if(RoyaleEconomy.menusCfg.getBoolean("menus.withdraw-coins-menu.use-background-glass")) {
            String color=RoyaleEconomy.menusCfg.getString("menus.withdraw-coins-menu.background-glass-color");
            if(color.startsWith("item:")){
                backgroundGlassWithdraw = RoyaleEconomy.itemConstructor.getItem(color.substring(5), " ", null);
            }
            else {
                int data = 0;
                if (color.split("#").length > 1) {
                    data = Integer.parseInt(color.split("#")[1]);
                    color = color.split("#")[0];
                }
                backgroundGlassWithdraw = RoyaleEconomy.itemConstructor.getItem("160:" + color, " ", null);
                if (customModelData && data != 0) {
                    ItemMeta meta = backgroundGlassWithdraw.getItemMeta();
                    meta.setCustomModelData(data);
                    backgroundGlassWithdraw.setItemMeta(meta);
                }
            }
        }

        sharedmainBankMenuSize= RoyaleEconomy.menusCfg.getInt("shared-menus.main-bank-menu.size");
        shareddepositItemSlot = loadSlots(RoyaleEconomy.menusCfg, "shared-menus.main-bank-menu.deposit-item.slot");
        sharedwithdrawItemSlot = loadSlots(RoyaleEconomy.menusCfg, "shared-menus.main-bank-menu.withdraw-item.slot");
        sharedtransactionItemSlot = loadSlots(RoyaleEconomy.menusCfg, "shared-menus.main-bank-menu.transaction-history.slot");
        sharedinformationSlot = loadSlots(RoyaleEconomy.menusCfg, "shared-menus.main-bank-menu.information-item.slot");
        sharedbankUpgradesSlot = loadSlots(RoyaleEconomy.menusCfg, "shared-menus.main-bank-menu.bank-upgrades.slot");
        sharedcloseItemSlot = loadSlots(RoyaleEconomy.menusCfg, "shared-menus.main-bank-menu.close-item.slot");
        sharedPlayersInformationSlot = loadSlots(RoyaleEconomy.menusCfg, "shared-menus.main-bank-menu.shared-information-item.slot");

        if(RoyaleEconomy.menusCfg.getBoolean("shared-menus.main-bank-menu.use-background-glass")) {
            String color=RoyaleEconomy.menusCfg.getString("shared-menus.main-bank-menu.background-glass-color");
            if(color.startsWith("item:")){
                sharedbackgroundGlass = RoyaleEconomy.itemConstructor.getItem(color.substring(5), " ", null);
            }
            else {
                int data = 0;
                if (color.split("#").length > 1) {
                    data = Integer.parseInt(color.split("#")[1]);
                    color = color.split("#")[0];
                }
                sharedbackgroundGlass = RoyaleEconomy.itemConstructor.getItem("160:" + color, " ", null);
                if (customModelData && data != 0) {
                    ItemMeta meta = sharedbackgroundGlass.getItemMeta();
                    meta.setCustomModelData(data);
                    sharedbackgroundGlass.setItemMeta(meta);
                }
            }
        }

        sharedbankUpgradesMenuSize = RoyaleEconomy.menusCfg.getInt("shared-menus.bank-upgrades-menu.size");
        sharedgoBackItemSlot = loadSlots(RoyaleEconomy.menusCfg, "shared-menus.bank-upgrades-menu.go-back-item.slot");
        if(RoyaleEconomy.menusCfg.getBoolean("shared-menus.bank-upgrades-menu.use-background-glass")) {
            String color=RoyaleEconomy.menusCfg.getString("shared-menus.bank-upgrades-menu.background-glass-color");
            if(color.startsWith("item:")){
                sharedbackgroundGlassBankUpgrades = RoyaleEconomy.itemConstructor.getItem(color.substring(5), " ", null);
            }
            else {
                int data = 0;
                if (color.split("#").length > 1) {
                    data = Integer.parseInt(color.split("#")[1]);
                    color = color.split("#")[0];
                }
                sharedbackgroundGlassBankUpgrades = RoyaleEconomy.itemConstructor.getItem("160:" + color, " ", null);
                if (customModelData && data != 0) {
                    ItemMeta meta = sharedbackgroundGlassBankUpgrades.getItemMeta();
                    meta.setCustomModelData(data);
                    sharedbackgroundGlassBankUpgrades.setItemMeta(meta);
                }
            }
        }

        sharedbankDepositMenuSize = RoyaleEconomy.menusCfg.getInt("shared-menus.deposit-coins-menu.size");
        sharedgoBackItemSlotDeposit = loadSlots(RoyaleEconomy.menusCfg, "shared-menus.deposit-coins-menu.go-back-item.slot");
        sharedcustomAmountItemSlot = loadSlots(RoyaleEconomy.menusCfg, "shared-menus.deposit-coins-menu.custom-amount-item.slot");

        if(RoyaleEconomy.menusCfg.getBoolean("shared-menus.deposit-coins-menu.use-background-glass")) {
            String color=RoyaleEconomy.menusCfg.getString("shared-menus.deposit-coins-menu.background-glass-color");
            if(color.startsWith("item:")){
                sharedbackgroundGlassDeposit = RoyaleEconomy.itemConstructor.getItem(color.substring(5), " ", null);
            }
            else {
                int data = 0;
                if (color.split("#").length > 1) {
                    data = Integer.parseInt(color.split("#")[1]);
                    color = color.split("#")[0];
                }
                sharedbackgroundGlassDeposit = RoyaleEconomy.itemConstructor.getItem("160:" + color, " ", null);
                if (customModelData && data != 0) {
                    ItemMeta meta = sharedbackgroundGlassDeposit.getItemMeta();
                    meta.setCustomModelData(data);
                    sharedbackgroundGlassDeposit.setItemMeta(meta);
                }
            }
        }

        sharedbankWithdrawMenuSize = RoyaleEconomy.menusCfg.getInt("shared-menus.withdraw-coins-menu.size");
        sharedgoBackItemSlotWithdraw = loadSlots(RoyaleEconomy.menusCfg, "shared-menus.withdraw-coins-menu.go-back-item.slot");
        sharedcustomAmountItemSlotWithdraw = loadSlots(RoyaleEconomy.menusCfg, "shared-menus.withdraw-coins-menu.custom-amount-item.slot");
        sharedwithdrawAsBagSlot = loadSlots(RoyaleEconomy.menusCfg, "shared-menus.withdraw-coins-menu.withdraw-as-bag.slot");

        if(RoyaleEconomy.menusCfg.getBoolean("shared-menus.withdraw-coins-menu.use-background-glass")) {
            String color=RoyaleEconomy.menusCfg.getString("shared-menus.withdraw-coins-menu.background-glass-color");
            if(color.startsWith("item:")){
                sharedbackgroundGlassWithdraw = RoyaleEconomy.itemConstructor.getItem(color.substring(5), " ", null);
            }
            else {
                int data = 0;
                if (color.split("#").length > 1) {
                    data = Integer.parseInt(color.split("#")[1]);
                    color = color.split("#")[0];
                }
                sharedbackgroundGlassWithdraw = RoyaleEconomy.itemConstructor.getItem("160:" + color, " ", null);
                if (customModelData && data != 0) {
                    ItemMeta meta = sharedbackgroundGlassWithdraw.getItemMeta();
                    meta.setCustomModelData(data);
                    sharedbackgroundGlassWithdraw.setItemMeta(meta);
                }
            }
        }

        moneyBagItem= Utils.getSkull(RoyaleEconomy.coinBagsAndTalismansCfg.getString("money-bags.money-bag-skin"));
        ItemMeta meta = moneyBagItem.getItemMeta();
        meta.setDisplayName(Utils.chat(RoyaleEconomy.coinBagsAndTalismansCfg.getString("money-bags.money-bag-name")));
        ArrayList<String> lore = new ArrayList<>();
        for(String line : RoyaleEconomy.coinBagsAndTalismansCfg.getStringList("money-bags.money-bag-lore"))
            lore.add(Utils.chat(line));
        meta.setLore(lore);
        moneyBagItem.setItemMeta(meta);
    }


    public StaticValues(){
        loadValues();
    }
}
