package me.qKing12.RoyaleEconomy.Hooks;

import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

public class NoEconomyHandler implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e){
        if(RoyaleEconomy.economy==null){
            Plugin vault = Bukkit.getPluginManager().getPlugin("Vault");
            try {
                if (vault != null) {
                    RoyaleEconomy.economy = Bukkit.getServer().getServicesManager().getRegistration(Economy.class).getProvider();
                } else {
                    RoyaleEconomy.plugin.getLogger().warning("Vault was not found!");
                    RoyaleEconomy.plugin.getLogger().warning("You need Vault Plugin so RoyaleEconomy can work.");
                    Bukkit.getPluginManager().disablePlugin(RoyaleEconomy.plugin);
                }
            }catch(Exception x){
                RoyaleEconomy.plugin.getLogger().warning("An error has occured!\n");
                RoyaleEconomy.plugin.getLogger().warning("No economy found, the plugin will shut down.");
                RoyaleEconomy.plugin.getLogger().warning("NOTICE: If you want RoyaleEconomy to manage the economy");
                RoyaleEconomy.plugin.getLogger().warning("set no-economy: false inside CONFIG.YML!!!");
                RoyaleEconomy.plugin.getLogger().warning("Avoid using it with economies like CMI or EssentialsX Eco");
                RoyaleEconomy.plugin.getLogger().warning("since it can cause data problems when no-economy is false!");
                //x.printStackTrace();
                Bukkit.getPluginManager().disablePlugin(RoyaleEconomy.plugin);
            }
        }
        HandlerList.unregisterAll(this);
    }

    public NoEconomyHandler(){
        Bukkit.getPluginManager().registerEvents(this, RoyaleEconomy.plugin);
    }
}
