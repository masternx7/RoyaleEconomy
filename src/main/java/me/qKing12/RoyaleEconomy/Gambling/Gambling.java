package me.qKing12.RoyaleEconomy.Gambling;

import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import me.qKing12.RoyaleEconomy.utils.Utils;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;

public class Gambling {
    public static ArrayList<GamblingChance> gamblingChances;
    static String permission;
    static String noPermission;

    static double defaultAmount;
    static double minimumAmount;
    static double maximumAmount;

    static String notEnoughCoins;
    static String losingMessage;
    static String winningMessage;
    static String noRushMessage;

    //menu stuff
    static ArrayList<ItemStack> animationColors;
    static ItemStack winningBackground;
    static ItemStack losingBackground;

    static ArrayList<String> closingCommands;

    static ItemStack purseInfoMaterial;
    static ItemStack pullItemMaterial;
    static ItemStack editGambleItemMaterial;
    static ItemStack gambleHistoryItemMaterial;

    static String purseInfoName;
    static String pullItemName;
    static String editGambleName;
    static String gambleHistoryName;
    static String playerHeadName;

    static ArrayList<String> purseInfoLore;
    static ArrayList<String> pullItemLore;
    static ArrayList<String> editGambleLore;
    static String gambleHistoryLoreStructureWin;
    static String gambleHistoryLoreStructureLose;
    static ArrayList<String> gambleHistoryLoreNone;
    static ArrayList<String> playerHeadLore;

    static ArrayList<ItemStack> shuffleItems;

    static String menuName;

    static ItemStack mainBackground;
    static ItemStack smallBackground;
    static ItemStack defaultItem;

    static ItemStack pullItemWaiting;

    static String minimumMess;
    static String maximumMess;

    static ArrayList<String> gambleInputMessage;

    public Gambling() {
        permission=RoyaleEconomy.gamblingCfg.getString("gambling-permission");
        noPermission=RoyaleEconomy.gamblingCfg.getString("no-permission");

        defaultAmount=RoyaleEconomy.gamblingCfg.getDouble("default-gamble-amount");
        minimumAmount=RoyaleEconomy.gamblingCfg.getDouble("minimum-gamble-amount");
        maximumAmount=RoyaleEconomy.gamblingCfg.getDouble("maximum-gamble-amount");

        notEnoughCoins=Utils.chat(RoyaleEconomy.gamblingCfg.getString("not-enough-coins"));
        losingMessage=Utils.chat(RoyaleEconomy.gamblingCfg.getString("default-losing-message"));
        winningMessage=Utils.chat(RoyaleEconomy.gamblingCfg.getString("default-winning-message"));
        noRushMessage=Utils.chat(RoyaleEconomy.gamblingCfg.getString("no-rush"));

        closingCommands=(ArrayList)RoyaleEconomy.gamblingCfg.getStringList("gambling-menu.closing-commands");

        purseInfoMaterial=RoyaleEconomy.itemConstructor.getItemFromMaterial(RoyaleEconomy.gamblingCfg.getString("gambling-menu.purse-info.material"));
        purseInfoName=Utils.chat(RoyaleEconomy.gamblingCfg.getString("gambling-menu.purse-info.name"));
        purseInfoLore=new ArrayList<>();
        for(String line : RoyaleEconomy.gamblingCfg.getStringList("gambling-menu.purse-info.lore"))
            purseInfoLore.add(Utils.chat(line));

        pullItemMaterial=RoyaleEconomy.itemConstructor.getItemFromMaterial(RoyaleEconomy.gamblingCfg.getString("gambling-menu.pull-item.material"));
        pullItemName=Utils.chat(RoyaleEconomy.gamblingCfg.getString("gambling-menu.pull-item.name"));
        pullItemLore=new ArrayList<>();
        for(String line : RoyaleEconomy.gamblingCfg.getStringList("gambling-menu.pull-item.lore"))
            pullItemLore.add(Utils.chat(line));

        editGambleItemMaterial=RoyaleEconomy.itemConstructor.getItemFromMaterial(RoyaleEconomy.gamblingCfg.getString("gambling-menu.edit-gamble.material"));
        editGambleName=Utils.chat(RoyaleEconomy.gamblingCfg.getString("gambling-menu.edit-gamble.name"));
        editGambleLore=new ArrayList<>();
        for(String line : RoyaleEconomy.gamblingCfg.getStringList("gambling-menu.edit-gamble.lore"))
            editGambleLore.add(Utils.chat(line));

        playerHeadName=Utils.chat(RoyaleEconomy.gamblingCfg.getString("gambling-menu.player-head-info.name"));
        playerHeadLore=new ArrayList<>();
        for(String line : RoyaleEconomy.gamblingCfg.getStringList("gambling-menu.player-head-info.lore"))
            playerHeadLore.add(Utils.chat(line));

        gambleHistoryItemMaterial=RoyaleEconomy.itemConstructor.getItemFromMaterial(RoyaleEconomy.gamblingCfg.getString("gambling-menu.gamble-history.material"));
        gambleHistoryName=Utils.chat(RoyaleEconomy.gamblingCfg.getString("gambling-menu.gamble-history.name"));
        gambleHistoryLoreNone=new ArrayList<>();
        for(String line : RoyaleEconomy.gamblingCfg.getStringList("gambling-menu.gamble-history.no-history"))
            gambleHistoryLoreNone.add(Utils.chat(line));
        gambleHistoryLoreStructureLose=Utils.chat(RoyaleEconomy.gamblingCfg.getString("gambling-menu.gamble-history.lore-structure.lost-gamble"));
        gambleHistoryLoreStructureWin=Utils.chat(RoyaleEconomy.gamblingCfg.getString("gambling-menu.gamble-history.lore-structure.won-gamble"));

        shuffleItems=new ArrayList<>();
        for(String item : RoyaleEconomy.gamblingCfg.getStringList("shuffle-items"))
            shuffleItems.add(RoyaleEconomy.itemConstructor.getItem(item, " ", null));

        menuName=Utils.chat(RoyaleEconomy.gamblingCfg.getString("gambling-menu.menu-name"));

        mainBackground=RoyaleEconomy.itemConstructor.getItemFromMaterial(RoyaleEconomy.gamblingCfg.getString("gambling-menu.main-background-material"));
        smallBackground=RoyaleEconomy.itemConstructor.getItemFromMaterial(RoyaleEconomy.gamblingCfg.getString("gambling-menu.small-background-material"));

        ItemMeta meta;
        if(!mainBackground.getType().equals(Material.AIR)) {
            meta = mainBackground.getItemMeta();
            meta.setDisplayName(" ");
            mainBackground.setItemMeta(meta);
        }

        ArrayList<String> lore=new ArrayList<>();
        for(String line : RoyaleEconomy.gamblingCfg.getStringList("gambling-menu.not-pulled-yet.lore"))
            lore.add(Utils.chat(line));
        defaultItem=RoyaleEconomy.itemConstructor.getItem(RoyaleEconomy.gamblingCfg.getString("gambling-menu.not-pulled-yet.material"), Utils.chat(RoyaleEconomy.gamblingCfg.getString("gambling-menu.not-pulled-yet.name")), lore);

        gambleAmounts=new HashMap<>();
        gambles=new HashMap<>();

        minimumMess=Utils.chat(RoyaleEconomy.gamblingCfg.getString("minimum-message"));
        maximumMess=Utils.chat(RoyaleEconomy.gamblingCfg.getString("maximum-message"));

        gambleInputMessage=(ArrayList)RoyaleEconomy.gamblingCfg.getStringList("gambling-input-message");

        animationColors=new ArrayList<>();
        for(String line : RoyaleEconomy.gamblingCfg.getStringList("animation-settings.shuffle-background-items"))
            animationColors.add(RoyaleEconomy.itemConstructor.getItem(line, " ", null));

        winningBackground=RoyaleEconomy.itemConstructor.getItem(RoyaleEconomy.gamblingCfg.getString("animation-settings.winning-mini-background"), " ", null);
        losingBackground=RoyaleEconomy.itemConstructor.getItem(RoyaleEconomy.gamblingCfg.getString("animation-settings.losing-mini-background"), " ", null);

        lore=new ArrayList<>();
        for(String line : RoyaleEconomy.gamblingCfg.getStringList("gambling-menu.pull-item-waiting.lore"))
            lore.add(Utils.chat(line));
        pullItemWaiting=RoyaleEconomy.itemConstructor.getItem(RoyaleEconomy.gamblingCfg.getString("gambling-menu.pull-item-waiting.material"), Utils.chat(RoyaleEconomy.gamblingCfg.getString("gambling-menu.pull-item-waiting.name")), lore);

        winningSound=RoyaleEconomy.gamblingCfg.getString("sounds.winning-sound");
        losingSound=RoyaleEconomy.gamblingCfg.getString("sounds.losing-sound");
        whileGambleSound=RoyaleEconomy.gamblingCfg.getString("sounds.while-gambling-sound");

        loadChances();
    }


    public static void loadChances() {
        gamblingChances = new ArrayList<>();
        for (String key : RoyaleEconomy.gamblingCfg.getConfigurationSection("chances.losing").getKeys(false)) {
            gamblingChances.add(new GamblingChance(true, RoyaleEconomy.gamblingCfg.getConfigurationSection("chances.losing." + key)));
        }

        for (String key : RoyaleEconomy.gamblingCfg.getConfigurationSection("chances.winning").getKeys(false)) {
            gamblingChances.add(new GamblingChance(false, RoyaleEconomy.gamblingCfg.getConfigurationSection("chances.winning." + key)));
        }
    }

    private static HashMap<String, Double> gambleAmounts;

    static double getGambleAmount(Player player){
        return gambleAmounts.getOrDefault(player.getUniqueId().toString(), defaultAmount);
    }

    public static void setGambleAmount(Player player, double amount){
        if(amount<minimumAmount){
            player.sendMessage(minimumMess);
        }
        else if(maximumAmount!=0 && amount>maximumAmount){
            player.sendMessage(maximumMess);
        }
        else
            gambleAmounts.put(player.getUniqueId().toString(), amount);
    }

    private static HashMap<String, ArrayList<String>> gambles;

    public static void addGambleLog(Player player, boolean lose, double initialAmount, double finalAmount, double percent){
        ArrayList<String> pGambles=gambles.getOrDefault(player.getUniqueId().toString(), new ArrayList<>());
        if(pGambles.size()==10)
            pGambles.remove(0);

        String toAdd;
        if(lose)
            toAdd=gambleHistoryLoreStructureLose;
        else
            toAdd=gambleHistoryLoreStructureWin;

        toAdd=toAdd.replace("%from-amount%", RoyaleEconomy.messageHelper.numberFormat(initialAmount)).replace("%to-amount%", RoyaleEconomy.messageHelper.numberFormat(finalAmount)).replace("%difference-amount%", Double.toString(Math.abs(finalAmount-initialAmount))).replace("%percent%", Double.toString(percent).replace(".0", ""));
        toAdd=ZonedDateTime.now().toInstant().toEpochMilli()+"&&-&&"+toAdd;
        pGambles.add(toAdd);
        gambles.put(player.getUniqueId().toString(), pGambles);
    }

    public static ItemStack getGambleLog(Player player){
        ItemStack toReturn=gambleHistoryItemMaterial.clone();
        ItemMeta meta=toReturn.getItemMeta();
        meta.setDisplayName(gambleHistoryName);
        ArrayList<String> history;
        ArrayList<String> historyCopy=gambles.getOrDefault(player.getUniqueId().toString(), new ArrayList<>());
        if(historyCopy.isEmpty()) {
            history = gambleHistoryLoreNone;
        }
        else{
            history=new ArrayList<>();
            for(String line : historyCopy){
                String toAdd=line.split("&&-&&")[1];
                history.add(toAdd.replace("%time%", RoyaleEconomy.messageHelper.formatTimeNotDetailed(ZonedDateTime.now().toInstant().toEpochMilli()-Long.parseLong(line.split("&&-&&")[0]))));
            }
        }
        meta.setLore(history);
        toReturn.setItemMeta(meta);
        return toReturn;
    }

    public static GamblingChance getRandomChance() {
        double completeWeight = 0.0;
        for (GamblingChance gamblingChance : gamblingChances)
            completeWeight += gamblingChance.getChance();
        double r = Math.random() * completeWeight;
        double countWeight = 0.0;
        for (GamblingChance gamblingChance : gamblingChances) {
            countWeight += gamblingChance.getChance();
            if (countWeight >= r)
                return gamblingChance;
        }
        throw new RuntimeException("Should never be shown.");
    }

    static String winningSound;
    static String losingSound;
    static String whileGambleSound;

    static void playSound(Player p, String sound) {
        try {
            if (sound == null || sound.equalsIgnoreCase("none"))
                return;
            if (sound.contains(":")) {
                String[] soundArgs = sound.split(":");
                if(soundArgs.length==2)
                    p.playSound(p.getLocation(), Sound.valueOf(soundArgs[0]), 2, Float.valueOf(soundArgs[1]));
                else
                    p.playSound(p.getLocation(), Sound.valueOf(soundArgs[0]), Float.valueOf(soundArgs[2]), Float.valueOf(soundArgs[1]));
            } else
                p.playSound(p.getLocation(), Sound.valueOf(sound), 2, 2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
