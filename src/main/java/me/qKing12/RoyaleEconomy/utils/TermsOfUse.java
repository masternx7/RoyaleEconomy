package me.qKing12.RoyaleEconomy.utils;

import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.*;
import java.time.ZonedDateTime;


public class TermsOfUse implements CommandExecutor, Listener {
    private boolean accepted=false;

    public boolean getAccepted(){
        return accepted;
    }

    public TermsOfUse(){
        RoyaleEconomy.plugin.getLogger().info("-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-");
        RoyaleEconomy.plugin.getLogger().info("  Plugin: RoyaleEconomy");
        RoyaleEconomy.plugin.getLogger().info("  Version: 2.5");
        RoyaleEconomy.plugin.getLogger().info("  Author: qKing12 (Dragos-Dumitru Ghinea)");
        RoyaleEconomy.plugin.getLogger().info("  Copyright: 2020-"+ ZonedDateTime.now().getYear() +" All Rights Reserved");
        RoyaleEconomy.plugin.getLogger().info("-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-");

        File toCreate = new File(RoyaleEconomy.plugin.getDataFolder(), "TermsOfUse.txt");
        if (!toCreate.exists()) {
            try {
                toCreate.createNewFile();
                InputStream input = this.getClass().getResourceAsStream("/TermsOfUse.txt");
                OutputStream output = new FileOutputStream(toCreate);
                int realLength;
                byte[] buffer = new byte[1024];

                while (input != null && (realLength = input.read(buffer)) > 0) {
                    output.write(buffer, 0, realLength);
                }
                output.flush();
                output.close();
                RoyaleEconomy.plugin.getLogger().info("Loading terms of use...");
            } catch (IOException e) {
                RoyaleEconomy.plugin.getLogger().info("Could not load terms of use");
            }
        }

        try {
            BufferedReader input = new BufferedReader(new FileReader(toCreate));
            String line,lastLine="";

            while ((line = input.readLine()) != null) {
                lastLine = line;
            }

            lastLine=lastLine.replace(" ", "");
            if(lastLine.contains("false") && !lastLine.contains("true")) {
                redirectCommands();
                Bukkit.getPluginManager().registerEvents(this, RoyaleEconomy.plugin);
                RoyaleEconomy.plugin.getLogger().warning("Terms Of Use are not accepted inside TermsOfUse.txt!");
                RoyaleEconomy.plugin.getLogger().warning("Plugin will not work until the last line in TermsOfUse.txt is set to true.");
            }
            else accepted=true;
        }catch(Exception x){
            x.printStackTrace();
        }

    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e){
        if(e.getPlayer().isOp()){
            Player p = e.getPlayer();
            p.sendMessage(Utils.chat("&c[RoyaleEconomy] You need to accept &c&lTerms of Use &cbefore using the plugin."));
            p.sendMessage(Utils.chat("&c[RoyaleEconomy] Please read them again inside TermsOfUse.txt from the plugin's folder."));
            p.sendMessage(Utils.chat("&c[RoyaleEconomy] After you read them set Accept Terms Of Use (last line) to true and restart to get the plugin working!"));
        }
    }

    private void redirectCommands(){
        RoyaleEconomy.plugin.getCommand("royaleeconomy").setExecutor(this);
        //RoyaleEconomy.plugin.getCommand("pay").setExecutor(this);
        //RoyaleEconomy.plugin.getCommand("balance").setExecutor(this);
        //RoyaleEconomy.plugin.getCommand("balancetop").setExecutor(this);
        //RoyaleEconomy.plugin.getCommand("reshop").setExecutor(this);
        //RoyaleEconomy.plugin.getCommand("bank").setExecutor(this);
        //RoyaleEconomy.plugin.getCommand("sharedbank").setExecutor(this);
        //RoyaleEconomy.plugin.getCommand("moneybag").setExecutor(this);
        //RoyaleEconomy.plugin.getCommand("piggybank").setExecutor(this);
        //RoyaleEconomy.plugin.getCommand("retalismans").setExecutor(this);
        //RoyaleEconomy.plugin.getCommand("killcoins").setExecutor(this);
        //RoyaleEconomy.plugin.getCommand("interest").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        sender.sendMessage(Utils.chat("&cYou need to accept &c&lTerms of Use &cbefore using the plugin."));
        sender.sendMessage(Utils.chat("&cPlease read them again inside TermsOfUse.txt from the plugin's folder."));
        sender.sendMessage(Utils.chat("&cAfter you read them set Accept Terms Of Use (last line) to true and restart to get the plugin working!"));
        return true;
    }

}
