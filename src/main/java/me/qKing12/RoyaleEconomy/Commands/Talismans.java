package me.qKing12.RoyaleEconomy.Commands;

import de.tr7zw.changeme.nbtapi.NBTItem;
import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import me.qKing12.RoyaleEconomy.Menus.TalismanListMenu;
import me.qKing12.RoyaleEconomy.utils.PermissionChecker;
import me.qKing12.RoyaleEconomy.utils.PlayerMessageHandler;
import me.qKing12.RoyaleEconomy.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static me.qKing12.RoyaleEconomy.RoyaleEconomy.coinBagsAndTalismansCfg;


public class Talismans implements TabExecutor {
    public static ArrayList<ItemStack> randomCoinsTalismans;
    public static ArrayList<ItemStack> purseSaver;
    public static ArrayList<ItemStack> deathSaver;

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {
        if(args.length==1) {
            if (PermissionChecker.checkPermissionSilent("commands.talismans.give-command", commandSender))
                return new ArrayList<>(Arrays.asList("give", ""));
        }
        else if(args.length==3)
                if(PermissionChecker.checkPermissionSilent("commands.talismans.give-command", commandSender))
                    return new ArrayList<>(Arrays.asList("random-coins", "death-save", "percent-reducer"));
        return null;
    }

    public Talismans() {
        //RoyaleEconomy.plugin.getCommand("retalismans").setExecutor(this);
        //RoyaleEconomy.plugin.getCommand("retalismans").setTabCompleter(this);
    }

    public void extraSetup(){
        randomCoinsTalismans=new ArrayList<>();
        purseSaver=new ArrayList<>();
        deathSaver=new ArrayList<>();
        int last_index = RoyaleEconomy.coinBagsAndTalismansCfg.getInt("talismans.random-coins-talisman.last-index");
        for(int i=0; i<=last_index;i++){
            ConfigurationSection talisman = RoyaleEconomy.coinBagsAndTalismansCfg.getConfigurationSection("talismans.random-coins-talisman."+i);
            ItemStack talismanItem = Utils.getSkull(talisman.getString("skull-texture"));
            ItemMeta meta = talismanItem.getItemMeta();
            meta.setDisplayName(Utils.chat(talisman.getString("name")));
            ArrayList<String> lore = new ArrayList<>();
            for(String line : talisman.getStringList("lore"))
                lore.add(Utils.chat(line));
            meta.setLore(lore);
            talismanItem.setItemMeta(meta);
            NBTItem nbt = new NBTItem(talismanItem);
            nbt.setString("RoyaleEconomyTalisman", "random-coins-talisman");
            nbt.setInteger("RoyaleEconomyIndex", i);
            if(i!=last_index)
                nbt.setInteger("RoyaleEconomyNextUpgrade", i+1);
            randomCoinsTalismans.add(nbt.getItem());
        }

        last_index = RoyaleEconomy.coinBagsAndTalismansCfg.getInt("talismans.percent-reducer-talisman.last-index");
        for(int i=0; i<=last_index;i++){
            ConfigurationSection talisman = RoyaleEconomy.coinBagsAndTalismansCfg.getConfigurationSection("talismans.percent-reducer-talisman."+i);
            ItemStack talismanItem = Utils.getSkull(talisman.getString("skull-texture"));
            ItemMeta meta = talismanItem.getItemMeta();
            meta.setDisplayName(Utils.chat(talisman.getString("name")));
            ArrayList<String> lore = new ArrayList<>();
            for(String line : talisman.getStringList("lore"))
                lore.add(Utils.chat(line));
            meta.setLore(lore);
            talismanItem.setItemMeta(meta);
            NBTItem nbt = new NBTItem(talismanItem);
            nbt.setString("RoyaleEconomyTalisman", "percent-reducer-talisman");
            nbt.setInteger("RoyaleEconomyIndex", i);
            if(i!=last_index)
                nbt.setInteger("RoyaleEconomyNextUpgrade", i+1);
            purseSaver.add(nbt.getItem());
        }

        last_index = RoyaleEconomy.coinBagsAndTalismansCfg.getInt("talismans.death-save-talisman.last-index");
        for(int i=0; i<=last_index;i++){
            ConfigurationSection talisman = RoyaleEconomy.coinBagsAndTalismansCfg.getConfigurationSection("talismans.death-save-talisman."+i);
            ItemStack talismanItem = Utils.getSkull(talisman.getString("skull-texture"));
            ItemMeta meta = talismanItem.getItemMeta();
            meta.setDisplayName(Utils.chat(talisman.getString("name")));
            ArrayList<String> lore = new ArrayList<>();
            for(String line : talisman.getStringList("lore"))
                lore.add(Utils.chat(line));
            meta.setLore(lore);
            talismanItem.setItemMeta(meta);
            NBTItem nbt = new NBTItem(talismanItem);
            nbt.setString("RoyaleEconomyTalisman", "death-save-talisman");
            nbt.setInteger("RoyaleEconomyIndex", i);
            if(i!=last_index)
                nbt.setInteger("RoyaleEconomyNextUpgrade", i+1);
            deathSaver.add(nbt.getItem());
        }
    }


    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            if(args.length>0) {
                if (args[0].equalsIgnoreCase("give")) {
                    if(PermissionChecker.checkPermission("commands.talismans.give-command", p)) {
                        if (args.length > 1) {
                            Player p2 = Bukkit.getPlayerExact(args[1]);
                            if (p2 != null) {
                                if (args.length > 2) {
                                    Utils.playSound(p, "commands.talismans.give-command.from-player");
                                    Utils.playSound(p2, "commands.talismans.give-command.to-player");
                                    if (args[2].equalsIgnoreCase("random-coins")) {
                                        try {
                                            int tier = Integer.parseInt(args[3]);
                                            ItemStack talisman = randomCoinsTalismans.get(tier);
                                            p2.getInventory().addItem(Utils.addNonStackablePropertyToItem(talisman));
                                            RoyaleEconomy.talismanHandler.updateRandomTalisman(p2);
                                        } catch (Exception x) {
                                            PlayerMessageHandler.messageSend(p, Utils.chat(RoyaleEconomy.commandsCfg.getString("commands.talismans.tier-not-found")));
                                        }
                                        return true;
                                    } else if (args[2].equalsIgnoreCase("death-save")) {
                                        try {
                                            int tier = Integer.parseInt(args[3]);
                                            ItemStack talisman = deathSaver.get(tier);
                                            p2.getInventory().addItem(Utils.addNonStackablePropertyToItem(talisman));
                                        } catch (Exception x) {
                                            PlayerMessageHandler.messageSend(p, Utils.chat(RoyaleEconomy.commandsCfg.getString("commands.talismans.tier-not-found")));
                                        }
                                        return true;
                                    } else if (args[2].equalsIgnoreCase("percent-reducer")) {
                                        try {
                                            int tier = Integer.parseInt(args[3]);
                                            ItemStack talisman = purseSaver.get(tier);
                                            p2.getInventory().addItem(Utils.addNonStackablePropertyToItem(talisman));
                                            RoyaleEconomy.talismanHandler.updateRandomTalisman(p2);
                                        } catch (Exception x) {
                                            PlayerMessageHandler.messageSend(p, Utils.chat(RoyaleEconomy.commandsCfg.getString("commands.talismans.tier-not-found")));
                                        }
                                        return true;
                                    }
                                }
                            } else
                                PlayerMessageHandler.messageSend(p, Utils.chat(RoyaleEconomy.commandsCfg.getString("commands.talismans.player-not-found")));
                        }
                    }
                }
            }
            else{
                if(PermissionChecker.checkPermission("commands.talismans.menu-open", p)) {
                    Utils.playSound(p, "commands.talismans.menu-open");
                    ArrayList<ItemStack> talismans = new ArrayList<>();
                    talismans.addAll(randomCoinsTalismans);
                    talismans.addAll(purseSaver);
                    talismans.addAll(deathSaver);
                    new TalismanListMenu(p, talismans);
                }
                return true;
            }
            if(PermissionChecker.checkPermission("commands.talismans.command-help", p)) {
                Utils.playSound(p, "commands.talismans.command-help");
                for (String line : RoyaleEconomy.commandsCfg.getStringList("commands.talismans.command-help"))
                    PlayerMessageHandler.messageSend(p, Utils.chat(line));
            }
        }
        else if(sender.isOp()){
            CommandSender p = sender;
            if(args.length>0) {
                if (args[0].equalsIgnoreCase("give")) {
                    if (args.length > 1) {
                        Player p2 = Bukkit.getPlayerExact(args[1]);
                        if (p2 != null) {
                            if (args.length > 2) {
                                Utils.playSound(p2, "commands.talismans.give-command.to-player");
                                if (args[2].equalsIgnoreCase("random-coins")) {
                                    try {
                                        int tier = Integer.parseInt(args[3]);
                                        ItemStack talisman = randomCoinsTalismans.get(tier);
                                        p2.getInventory().addItem(Utils.addNonStackablePropertyToItem(talisman));
                                        RoyaleEconomy.talismanHandler.updateRandomTalisman(p2);
                                    } catch (Exception x) {
                                        PlayerMessageHandler.messageSend(p, Utils.chat(RoyaleEconomy.commandsCfg.getString("commands.talismans.tier-not-found")));
                                    }
                                    return true;
                                } else if (args[2].equalsIgnoreCase("death-save")) {
                                    try {
                                        int tier = Integer.parseInt(args[3]);
                                        ItemStack talisman = deathSaver.get(tier);
                                        p2.getInventory().addItem(Utils.addNonStackablePropertyToItem(talisman));
                                    } catch (Exception x) {
                                        PlayerMessageHandler.messageSend(p, Utils.chat(RoyaleEconomy.commandsCfg.getString("commands.talismans.tier-not-found")));
                                    }
                                    return true;
                                } else if (args[2].equalsIgnoreCase("percent-reducer")) {
                                    try {
                                        int tier = Integer.parseInt(args[3]);
                                        ItemStack talisman = purseSaver.get(tier);
                                        p2.getInventory().addItem(Utils.addNonStackablePropertyToItem(talisman));
                                        RoyaleEconomy.talismanHandler.updateRandomTalisman(p2);
                                    } catch (Exception x) {
                                        PlayerMessageHandler.messageSend(p, Utils.chat(RoyaleEconomy.commandsCfg.getString("commands.talismans.tier-not-found")));
                                    }
                                    return true;
                                }
                            }
                        } else
                            PlayerMessageHandler.messageSend(p, Utils.chat(RoyaleEconomy.commandsCfg.getString("commands.talismans.player-not-found")));
                    }
                }
            }

            for(String line : RoyaleEconomy.commandsCfg.getStringList("commands.talismans.command-help"))
                PlayerMessageHandler.messageSend(p, Utils.chat(line));
        }
        return true;
    }
}
