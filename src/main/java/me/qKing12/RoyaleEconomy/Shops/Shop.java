package me.qKing12.RoyaleEconomy.Shops;

import me.qKing12.RoyaleEconomy.Commands.dynamic.DynamicCommand;
import me.qKing12.RoyaleEconomy.Commands.dynamic.DynamicCommandsSetup;
import me.qKing12.RoyaleEconomy.InputGUIs.ChatListener;
import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import me.qKing12.RoyaleEconomy.utils.PlayerMessageHandler;
import me.qKing12.RoyaleEconomy.utils.Utf8YamlConfiguration;
import me.qKing12.RoyaleEconomy.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;



public class Shop {
    private String shopName;
    private String title;
    private String permission;
    FileConfiguration config;
    File file;
    private ArrayList<String> exitCommands;
    private ArrayList<ShopItem> items = new ArrayList<>();
    private ArrayList<ShopItem> notExclusiveItems = new ArrayList<>();
    private ArrayList<ShopItem> exclusiveItems = new ArrayList<>();
    private ItemStack backgroundGlass;
    private String shopDetailedTitle;

    public String getShopDetailedTitle() {
        if(shopDetailedTitle==null)
            return title;
        return shopDetailedTitle;
    }

    public ArrayList<String> getCommands(){
        return exitCommands;
    }

    private DynamicCommand openCommand;

    public DynamicCommand getOpenCommand() {
        return openCommand;
    }

    public void setCommands(ArrayList<String> commands){
        if(commands.isEmpty())
            this.exitCommands=null;
        else
            this.exitCommands=commands;
        config.set("exit-commands", this.exitCommands);
    }

    private void writeDefaultShop(File shopFile){
        file=shopFile;
        config = Utf8YamlConfiguration.loadConfiguration(shopFile);
        config.set("title", "&a"+shopFile.getName().replace(".yml", ""));
        this.title="&a"+shopFile.getName().replace(".yml", "");
        this.shopName=shopFile.getName().replace(".yml", "");
        config.set("permission-to-open", "none");
        config.set("background-color", 15);
        backgroundGlass = RoyaleEconomy.itemConstructor.getItem("160:"+15, " ", null);
        ItemStack item = new ItemStack(Material.GLASS, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(Utils.chat("&aExample Item"));
        meta.setLore(Arrays.asList("Line1", "Line2"));
        item.setItemMeta(meta);
        config.set("items.exampleItem.itemStack", item);
        config.set("items.exampleItem.sellingValue", 5);
        config.set("items.exampleItem.buyValue", 10);
        config.set("items.exampleItem.detailed", true);
        config.set("items.exampleItem.amount1", 1);
        config.set("items.exampleItem.amount2", 5);
        config.set("items.exampleItem.amount3", 20);
        config.set("items.exampleItem.amount4", 32);
        config.set("items.exampleItem.amount5", 64);

        config.set("open-commands.use", false);
        config.set("open-commands.permission", "rec.command."+shopName);
        config.set("open-commands.permission-message", "&cYou don't have permission to open this shop!");
        config.set("open-commands.commands", Arrays.asList("reshop_main_command_"+shopName, "reshop_alias_command_"+shopName));

        items.add(new ShopItem(config.getConfigurationSection("items.exampleItem"), "exampleItem"));
        try {
            config.save(shopFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ShopsLoad.shops.add(this);
    }

    public void loadShopOpenCommand(){
        if (config.contains("open-commands") && config.getBoolean("open-commands.use")){
            List<String> commands = config.getStringList("open-commands.commands");
            String cmdPermission = config.getString("open-commands.permission");
            String cmdPermissionMsg = config.getString("open-commands.permission-message");
            String mainCommand;

            if (!commands.isEmpty()) {
                mainCommand = commands.get(0);
                commands.remove(0);

                CommandExecutor executor = new CommandExecutor() {
                    @Override
                    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
                        if (!(sender instanceof Player))
                            return false;

                        new ShopMenu((Player) sender, Shop.this, 0, null);
                        return false;
                    }
                };

                if (cmdPermission == null || cmdPermission.equalsIgnoreCase("none")){
                    openCommand = new DynamicCommand(
                            mainCommand,
                            commands,
                            executor
                    );
                }
                else {
                    openCommand = new DynamicCommand(
                            mainCommand,
                            cmdPermission,
                            cmdPermissionMsg,
                            commands,
                            executor
                    );
                }

                DynamicCommandsSetup.registerCommand(openCommand);
            }
        }
    }

    public ItemStack getBackgroundGlass(){
        return backgroundGlass;
    }

    public void setBackgroundGlass(int color){
        backgroundGlass= RoyaleEconomy.itemConstructor.getItem("160:"+color, " ", null);
        config.set("background-color", color);
        try {
            config.save(file);
        }catch (Exception x){
            x.printStackTrace();
        }
    }

    public void setModelData(int data){
        ItemMeta meta = backgroundGlass.getItemMeta();
        meta.setCustomModelData(data);
        backgroundGlass.setItemMeta(meta);
        config.set("custom-model-data", data);
        try {
            config.save(file);
        }catch (Exception x){
            x.printStackTrace();
        }
    }

    public String getShopName(){
        return shopName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title){
        this.title=title;
        config.set("title", title);
        try {
            config.save(file);
        }catch(Exception x){
            x.printStackTrace();
        }
    }

    public String getPermission(){
        if(permission==null)
            return "none";
        else
            return permission;
    }

    public void setPermission(String permission){
        this.permission=permission;
        if(permission.equals("none"))
            this.permission=null;
        config.set("permission-to-open", permission);
        try{
            config.save(file);
        }catch(Exception x){
            x.printStackTrace();
        }
    }

    public ArrayList<ShopItem> getItems(){
        return items;
    }

    public ArrayList<ShopItem> getExclusiveItems(){
        return exclusiveItems;
    }

    public ArrayList<ShopItem> getNotExclusiveItems(){
        return notExclusiveItems;
    }

    public void renameShop(File newFile){
        file.renameTo(newFile);
        shopName=newFile.getName().replace(".yml", "");
    }

    public void createShopItem(String newItem, Player p){
        if(config.getKeys(true).contains("items."+newItem))
            PlayerMessageHandler.messageSend(p, Utils.chat("&cThere already exists an item with this name."));
        else {
            ItemStack item = new ItemStack(Material.DIRT, 1);
            config.set("items." + newItem + ".itemStack", item);
            config.set("items." + newItem + ".sellingValue", 5);
            config.set("items." + newItem + ".buyValue", 10);
            config.set("items." + newItem + ".detailed", true);
            config.set("items." + newItem + ".amount1", 1);
            config.set("items." + newItem + ".amount2", 5);
            config.set("items." + newItem + ".amount3", 20);
            config.set("items." + newItem + ".amount4", 32);
            config.set("items." + newItem + ".amount5", 64);
            ShopItem itemShop = new ShopItem(config.getConfigurationSection("items."+newItem), newItem);
            items.add(itemShop);
            ShopsLoad.shopItems.add(itemShop);
            PlayerMessageHandler.messageSend(p, Utils.chat("&aItem was successfully created!"));
            try {
                config.save(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public Shop(File shopFile, Player p){
        if(shopFile==null){
            PlayerMessageHandler.messageSend(p, Utils.chat("&7&m-------------------------"));
            PlayerMessageHandler.messageSend(p, Utils.chat("Please enter the name of the &ashop&f!"));
            PlayerMessageHandler.messageSend(p, Utils.chat("It needs to be a single word"));
            PlayerMessageHandler.messageSend(p, Utils.chat("and contain valid chars."));
            new ChatListener(p, (reply) ->{
                reply = reply.replaceAll("[^A-Za-z0-9]+", "");
                    try {
                        File newShopFile = new File(RoyaleEconomy.plugin.getDataFolder(), "/shops/" + reply + ".yml");
                        if (newShopFile.createNewFile()) {
                            writeDefaultShop(newShopFile);
                            PlayerMessageHandler.messageSend(p, Utils.chat("&aThe shop file was created!"));
                        }
                        else
                            PlayerMessageHandler.messageSend(p, Utils.chat("&cThis shop aleady exists."));
                    }catch(Exception x){
                        x.printStackTrace();
                    }

            }, false);
        }
        else{
            file=shopFile;
            config = Utf8YamlConfiguration.loadConfiguration(shopFile);

            this.backgroundGlass = RoyaleEconomy.itemConstructor.getItem("160:" + config.getInt("background-color"), " ", null);
            int customModelData = config.getInt("custom-model-data");
            if (customModelData != 0) {
                backgroundGlass.getItemMeta().setCustomModelData(customModelData);
                ItemMeta meta = backgroundGlass.getItemMeta();
                meta.setCustomModelData(customModelData);
                backgroundGlass.setItemMeta(meta);
            }


            if (config.contains("exit-commands")) {
                this.exitCommands = (ArrayList<String>) config.getStringList("exit-commands");
            }

            if(config.contains("detailed-title"))
                shopDetailedTitle=config.getString("detailed-title");

            this.shopName=shopFile.getName().replace(".yml", "");
            this.title=config.getString("title");
            this.permission = config.getString("permission-to-open");
            if(permission!=null && permission.equals("none"))
                permission=null;
            if(config.getConfigurationSection("items")!=null)
                for(String key : config.getConfigurationSection("items").getKeys(false)){
                    try {
                        ShopItem item = new ShopItem(config.getConfigurationSection("items." + key), key);
                        if (item.isExclusive())
                            exclusiveItems.add(item);
                        else
                            notExclusiveItems.add(item);
                        items.add(item);
                    }catch(Exception x) {
                        RoyaleEconomy.plugin.getLogger().warning("Error loading item "+key+" from file. Please check your "+file.getName()+" file.");
                        throw x;
                    }
            }
        }

        if(config != null)
            loadShopOpenCommand();
    }

    public void rotateItems(int i1, int i2){
        ShopItem aux = items.get(i1);
        items.set(i1, items.get(i2));
        items.set(i2, aux);
    }

    public void deleteShop(){
        ShopsLoad.shops.remove(this);
        ShopsLoad.shopItems.removeAll(items);
        config=null;
        file.delete();
        file=null;
        items=null;
    }

    public void saveItems(){
        config.set("items", null);
        for(ShopItem item : items)
            item.addToConfiguration();
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveConfig(){
        try{
            config.save(file);
        }catch(Exception x){
            x.printStackTrace();
        }
    }

    public class ShopItem{
        private ItemStack item;
        private double sellingValue;
        private double buyValue;
        private boolean detailed;
        private String name;
        private boolean customDataModel;

        private int amount1;
        private int amount2;
        private int amount3;
        private int amount4;
        private int amount5;

        private ArrayList<ItemStack> defaultItemRequirements;
        private ArrayList<ItemStack> amount1Items;
        private ArrayList<ItemStack> amount2Items;
        private ArrayList<ItemStack> amount3Items;
        private ArrayList<ItemStack> amount4Items;
        private ArrayList<ItemStack> amount5Items;

        private ArrayList<String> commands;
        private boolean giveItemOnBuy;
        private boolean exclusive;
        private String buyPermission;

        private int detailedMenuMode = 0;

        public int getDetailedMenuMode() {
            return detailedMenuMode;
        }

        public void setDetailedMenuMode(int detailedMenuMode) {
            this.detailedMenuMode = detailedMenuMode;
            config.set("items."+name+".detailed-menu-mode", detailedMenuMode);
        }

        public boolean getGiveItemOnBuy(){
            return giveItemOnBuy;
        }

        public void setGiveItemOnBuy(boolean toSet){
            giveItemOnBuy=toSet;
            config.set("items."+name+".give-item-on-buy", giveItemOnBuy);
        }

        public boolean isExclusive(){
            return exclusive;
        }

        public void setExclusive(boolean toSet){
            exclusive=toSet;
            config.set("items."+name+".exclusive", exclusive);
            if(toSet) {
                notExclusiveItems.remove(this);
                exclusiveItems.add(this);
                ShopsLoad.shopItems.remove(this);
            }
            else {
                notExclusiveItems.add(this);
                exclusiveItems.remove(this);
                ShopsLoad.shopItems.add(this);
            }
        }

        public ArrayList<String> getCommands(){
            return commands;
        }

        public void setCommands(ArrayList<String> commands){
            if(commands.isEmpty())
                this.commands=null;
            else
                this.commands=commands;
            config.set("items."+name+".commands", this.commands);
        }

        public ArrayList<ItemStack> getDefaultItemRequirements() {
            return defaultItemRequirements;
        }

        public void setDefaultItemRequirements(ArrayList<ItemStack> toSet){
            if(toSet.isEmpty())
                defaultItemRequirements=null;
            else
                defaultItemRequirements=toSet;
        }

        public ArrayList<ItemStack> getAmount1Items(){
            return amount1Items;
        }

        public void setAmount1Items(ArrayList<ItemStack> toSet){
            amount1Items=toSet;
        }

        public ArrayList<ItemStack> getAmount2Items(){
            return amount2Items;
        }

        public void setAmount2Items(ArrayList<ItemStack> toSet){
            amount2Items=toSet;
        }

        public ArrayList<ItemStack> getAmount3Items(){
            return amount3Items;
        }

        public void setAmount3Items(ArrayList<ItemStack> toSet){
            amount3Items=toSet;
        }

        public ArrayList<ItemStack> getAmount4Items(){
            return amount4Items;
        }

        public void setAmount4Items(ArrayList<ItemStack> toSet){
            amount4Items=toSet;
        }

        public ArrayList<ItemStack> getAmount5Items(){
            return amount5Items;
        }

        public void setAmount5Items(ArrayList<ItemStack> toSet){
            amount5Items=toSet;
        }

        public boolean hasRightClick(){
            return amount1 + amount2 + amount3 + amount4 + amount5 != 0;
        }

        public String getName(){
            return this.name;
        }

        public ItemStack getItemStack(){
            return item;
        }

        public void setItemStack(ItemStack item){
            this.item=item;
            config.set("items."+name+".itemStack", item);
        }

        public double getSellingValue(){
            return sellingValue;
        }

        public void setSellingValue(double value){
            sellingValue=value;
            config.set("items."+name+".sellingValue", value);
        }

        public boolean getDetailed(){
            return detailed;
        }

        public void setDetailed(boolean detailed){
            this.detailed=detailed;
            config.set("items."+name+".detailed", detailed);
        }

        public void setBuyPermission(String permission){
            if (permission.equalsIgnoreCase("none"))
                buyPermission=null;
            else
                buyPermission=permission;

            config.set("items."+name+".buy-permission", permission);
            saveConfig();
        }

        public String getBuyPermission(){
            return buyPermission;
        }

        public int getAmount(int position){
            switch(position){
                case 1:
                    return amount1;
                case 2:
                    return amount2;
                case 3:
                    return amount3;
                case 4:
                    return amount4;
                default:
                    return amount5;
            }
        }

        public void setAmount(int position, int amount){
            switch(position){
                case 1:
                    amount1=amount;
                    config.set("items."+name+".amount1", amount);
                    break;
                case 2:
                    amount2=amount;
                    config.set("items."+name+".amount2", amount);
                    break;
                case 3:
                    amount3=amount;
                    config.set("items."+name+".amount3", amount);
                    break;
                case 4:
                    amount4=amount;
                    config.set("items."+name+".amount4", amount);
                    break;
                default:
                    amount5=amount;
                    config.set("items."+name+".amount5", amount);
            }
        }

        public double getBuyValue(){
            return buyValue;
        }

        public void setBuyValue(double value){
            buyValue=value;
            config.set("items."+name+".buyValue", value);
            saveConfig();
        }

        public boolean getCustomDataModel() {
            return customDataModel;
        }

        public void setCustomDataModel(boolean value){
            customDataModel=value;
            config.set("items."+name+".check-custom-model-data", value);
            saveConfig();
        }

        public void remove(){
            items.remove(this);
            config.set("items."+name, null);
            saveItems();
            ShopsLoad.shopItems.remove(this);
        }

        public void addToConfiguration(){
            config.set("items." + name + ".itemStack", item);
            config.set("items." + name + ".sellingValue", sellingValue);
            config.set("items." + name + ".buyValue", buyValue);
            config.set("items." + name + ".detailed", detailed);
            config.set("items." + name + ".amount1", amount1);
            config.set("items." + name + ".amount2", amount2);
            config.set("items." + name + ".amount3", amount3);
            config.set("items." + name + ".amount4", amount4);
            config.set("items." + name + ".amount5", amount5);

            config.set("items." + name + ".defaultItemRequirements", defaultItemRequirements);
            config.set("items." + name + ".amount1Items", amount1Items);
            config.set("items." + name + ".amount2Items", amount2Items);
            config.set("items." + name + ".amount3Items", amount3Items);
            config.set("items." + name + ".amount4Items", amount4Items);
            config.set("items." + name + ".amount5Items", amount5Items);

            config.set("items."+name+".commands", commands);
            config.set("items."+name+".give-item-on-buy", giveItemOnBuy);
            config.set("items."+name+".exclusive", exclusive);
            config.set("items."+name+".buy-permission", buyPermission);
        }

        public ShopItem(ConfigurationSection fromFile, String name) {
            try {
                this.name = name;
                this.item = fromFile.getItemStack("itemStack");
                amount1 = fromFile.getInt("amount1");
                amount2 = fromFile.getInt("amount2");
                amount3 = fromFile.getInt("amount3");
                amount4 = fromFile.getInt("amount4");
                amount5 = fromFile.getInt("amount5");
                detailed = fromFile.getBoolean("detailed");
                buyValue = fromFile.getDouble("buyValue");
                sellingValue = fromFile.getDouble("sellingValue");
                buyPermission = fromFile.getString("buy-permission");

                if (fromFile.getKeys(false).contains("commands")) {
                    this.commands = (ArrayList<String>) fromFile.getStringList("commands");
                }

                if (RoyaleEconomy.upperVersion && !Bukkit.getVersion().contains("1.13"))
                    customDataModel = fromFile.getBoolean("check-custom-model-data");

                if (fromFile.contains("detailed-menu-mode"))
                    detailedMenuMode = fromFile.getInt("detailed-menu-mode");

                giveItemOnBuy = fromFile.getBoolean("give-item-on-buy");

                exclusive = fromFile.getBoolean("exclusive");

                List<ItemStack> listToLoad = (List<ItemStack>) fromFile.getList("defaultItemRequirements");
                if (listToLoad != null) {
                    defaultItemRequirements = (ArrayList<ItemStack>) listToLoad;
                }

                listToLoad = (List<ItemStack>) fromFile.getList("amount1Items");
                if (listToLoad != null) {
                    amount1Items = (ArrayList<ItemStack>) listToLoad;
                }

                listToLoad = (List<ItemStack>) fromFile.getList("amount2Items");
                if (listToLoad != null) {
                    amount2Items = (ArrayList<ItemStack>) listToLoad;
                }

                listToLoad = (List<ItemStack>) fromFile.getList("amount3Items");
                if (listToLoad != null) {
                    amount3Items = (ArrayList<ItemStack>) listToLoad;
                }

                listToLoad = (List<ItemStack>) fromFile.getList("amount4Items");
                if (listToLoad != null) {
                    amount4Items = (ArrayList<ItemStack>) listToLoad;
                }

                listToLoad = (List<ItemStack>) fromFile.getList("amount5Items");
                if (listToLoad != null) {
                    amount5Items = (ArrayList<ItemStack>) listToLoad;
                }
            }catch(Exception e) {
                RoyaleEconomy.plugin.getLogger().warning("Error loading item "+name+" from file. Please check your "+file.getName()+" file.");
                throw e;
            }
        }
    }
}
