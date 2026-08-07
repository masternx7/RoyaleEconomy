package me.qKing12.RoyaleEconomy.Hooks;

import me.qKing12.RoyaleEconomy.Economy.VaultHook;
import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;

import static org.bukkit.Bukkit.getServer;

public class Initializer {
    public static void economyEnabled(Plugin vault){
        RoyaleEconomy.plugin.getLogger().info("Vault detected, economy register attempted.");
        if(RoyaleEconomy.economy==null) {
            RoyaleEconomy.economy = new VaultHook();
            Bukkit.getServicesManager().register(Economy.class, RoyaleEconomy.economy, vault, ServicePriority.Highest);
        }
    }
}
