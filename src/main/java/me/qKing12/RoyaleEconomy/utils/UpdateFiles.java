package me.qKing12.RoyaleEconomy.utils;

import com.google.common.base.Charsets;
import me.qKing12.RoyaleEconomy.RoyaleEconomy;

import java.io.*;
import java.util.Set;

public class UpdateFiles {

    public static void loadFiles(){
        RoyaleEconomy.bankUpgradesCfg = Utf8YamlConfiguration.loadConfiguration(new File(RoyaleEconomy.plugin.getDataFolder(), "bankUpgrades.yml"));
        RoyaleEconomy.coinBagsAndTalismansCfg = Utf8YamlConfiguration.loadConfiguration(new File(RoyaleEconomy.plugin.getDataFolder(), "coinBagsAndTalismans.yml"));
        RoyaleEconomy.commandsCfg = Utf8YamlConfiguration.loadConfiguration(new File(RoyaleEconomy.plugin.getDataFolder(), "commands.yml"));
        RoyaleEconomy.menusCfg = Utf8YamlConfiguration.loadConfiguration(new File(RoyaleEconomy.plugin.getDataFolder(), "menus.yml"));
        RoyaleEconomy.permissionsCfg = Utf8YamlConfiguration.loadConfiguration(new File(RoyaleEconomy.plugin.getDataFolder(), "permissions.yml"));
        RoyaleEconomy.soundsCfg = Utf8YamlConfiguration.loadConfiguration(new File(RoyaleEconomy.plugin.getDataFolder(), "sounds.yml"));
    }

    public UpdateFiles(){
        File inFile = new File(RoyaleEconomy.plugin.getDataFolder(), "config.yml");
        File outFile = new File(RoyaleEconomy.plugin.getDataFolder(), "$$$$$$$$.tmp");

        try {
            // input
            FileInputStream fis = new FileInputStream(inFile);
            BufferedReader in = new BufferedReader(new InputStreamReader(fis, Charsets.UTF_8));

            // output
            FileOutputStream fos = new FileOutputStream(outFile);
            PrintWriter out = new PrintWriter(new OutputStreamWriter(fos, Charsets.UTF_8));

            Set<String> keys = RoyaleEconomy.plugin.getConfig().getKeys(true);

            boolean cfgString=false;
            String thisLine = "";
            if(!keys.contains("no-economy")) {
                out.println("no-economy: false");
                out.println(" ");
            }
            while ((thisLine = in.readLine()) != null) {
                if (thisLine.startsWith("version:") || thisLine.startsWith("Version:") || thisLine.startsWith("config-version:")) {
                    out.println("config-version: 2.43");
                    cfgString=true;
                }
                else if(thisLine.startsWith("time-units:")){
                    if(!keys.contains("redis")) {
                        out.println("#For redis you don't need the bungee addon");
                        out.println("redis:");
                        out.println("  #If you want to sync separate server groups");
                        out.println("  #within the same redis server, change this channel's name");
                        out.println("  channel: 'RoyaleEconomy'");
                        out.println("  use-redis: false");
                        out.println("  host: 'localhost'");
                        out.println("  port: 6379");
                        out.println("  password: '12345'");
                        out.println("");
                    }
                    out.println(thisLine);
                }
                else if(thisLine.startsWith("server-executor-name:")){
                    out.println(thisLine);
                    if(!keys.contains("use-bank-logs")) {
                        out.println("use-bank-logs: false");
                        out.println("");
                    }
                }
                else
                    out.println(thisLine);
            }

            if(!cfgString){
                out.println("config-version: 2.43");
            }

            out.flush();
            out.close();
            in.close();

            inFile.delete();
            outFile.renameTo(inFile);



            inFile = new File(RoyaleEconomy.plugin.getDataFolder(), "commands.yml");

            // input
            fis = new FileInputStream(inFile);
            in = new BufferedReader(new InputStreamReader(fis, Charsets.UTF_8));

            // output
            fos = new FileOutputStream(outFile);
            out = new PrintWriter(new OutputStreamWriter(fos, Charsets.UTF_8));

            keys = RoyaleEconomy.commandsCfg.getKeys(true);

            if(!keys.contains("definitions")){
                out.println("#Please note that the permission field in all these commands");
                out.println("#is redundant for some of them");
                out.println("#but if you define it, you will add an extra permission check");
                out.println("#to the command.");
                out.println("#Setting the command to 'none' will make it disappear! (useful for default commands which can't be disabled)");
                out.println("#The commands also disappear if their setting is disabled");
                out.println("definitions:");
                out.println("  multi-currency:");
                out.println("    command: 'recurrency'");
                out.println("    aliases: []");
                out.println("    permission: 'op'");
                out.println("    no-permission: '&cYou do not have permission to use this command.'");
                out.println("  sell-all:");
                out.println("    command: 'recsellall'");
                out.println("    aliases:");
                out.println("      - 'sellall'");
                out.println("  gambling:");
                out.println("    command: 'gambling'");
                out.println("    aliases:");
                out.println("      - 'gamble'");
                out.println("  black-market:");
                out.println("    command: 'blackmarket'");
                out.println("    aliases:");
                out.println("      - 'reblackmarket'");
                out.println("      - 'recblackmarket'");
                out.println("  boosters:");
                out.println("    command: 'boosters'");
                out.println("    aliases:");
                out.println("      - 'booster'");
                out.println("  boosters-admin:");
                out.println("    command: 'boosteradmin'");
                out.println("    aliases: []");
                out.println("  reshop:");
                out.println("    command: 'reshop'");
                out.println("    aliases: []");
                out.println("  balance:");
                out.println("    command: 'balance'");
                out.println("    aliases:");
                out.println("      - 'bal'");
                out.println("      - 'purse'");
                out.println("      - 'money'");
                out.println("      - 'coins'");
                out.println("  balancetop:");
                out.println("    command: 'balancetop'");
                out.println("    aliases:");
                out.println("      - 'baltop'");
                out.println("  pay:");
                out.println("    command: 'pay'");
                out.println("    aliases: []");
                out.println("  moneybag:");
                out.println("    command: 'moneybag'");
                out.println("    aliases:");
                out.println("      - 'mbag'");
                out.println("      - 'coinsbag'");
                out.println("      - 'cbag'");
                out.println("      - 'coinbag'");
                out.println("      - 'moneybags'");
                out.println("      - 'cbags'");
                out.println("      - 'coinbags'");
                out.println("      - 'coinsbags'");
                out.println("  piggybank:");
                out.println("    command: 'piggybank'");
                out.println("    aliases: []");
                out.println("  talismans:");
                out.println("    command: 'talismans'");
                out.println("    aliases:");
                out.println("      - 'retalismans'");
                out.println("      - 'talisman'");
                out.println("  time-rewards:");
                out.println("    command: 'rerewards'");
                out.println("    aliases:");
                out.println("      - 'recrewards'");
                out.println("      - 'rewards'");
                out.println("  kill-coins:");
                out.println("    command: 'killcoins'");
                out.println("    aliases: []");
                out.println("  bank:");
                out.println("    command: 'bank'");
                out.println("    aliases: []");
                out.println("  shared-bank:");
                out.println("    command: 'sharedbank'");
                out.println("    aliases: []");
                out.println("  interest:");
                out.println("    command: 'interest'");
                out.println("    aliases: []");
                out.println("");
            }

            while ((thisLine = in.readLine()) != null) {
                out.println(thisLine);
            }
            out.flush();
            out.close();
            in.close();

            inFile.delete();
            outFile.renameTo(inFile);

            RoyaleEconomy.commandsCfg = Utf8YamlConfiguration.loadConfiguration(new File(RoyaleEconomy.plugin.getDataFolder(), "commands.yml"));

        }catch(Exception x){
            x.printStackTrace();
        }
    }
}
