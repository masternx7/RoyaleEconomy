package me.qKing12.RoyaleEconomy.PlaceholderAPISupport;

import me.qKing12.RoyaleEconomy.API.Events.CoinsAddToPurseEvent;
import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import me.qKing12.RoyaleEconomy.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DynamicCoinsPlaceholder implements Listener {

    protected static ConcurrentHashMap<Player, Double> dynamicCoins = new ConcurrentHashMap<>(300);
    protected static String displayFormat;

    public DynamicCoinsPlaceholder() {
        displayFormat = Utils.chat(RoyaleEconomy.plugin.getConfig().getString("dynamic-placeholder-format"));
        Bukkit.getPluginManager().registerEvents(this, RoyaleEconomy.plugin);
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        dynamicCoins.remove(e.getPlayer());
    }

    @EventHandler
    public void onCoinsGet(CoinsAddToPurseEvent e) {
        String playerAsString = e.getPlayerString();
        Player p;

        try {
            p = Bukkit.getPlayer(UUID.fromString(playerAsString));
        } catch (Exception x) {
            p = Bukkit.getPlayerExact(playerAsString);
        }

        if (p != null) {
            Player finalP = p;
            final double amount = e.getAddedCoins();
            dynamicCoins.put(p, dynamicCoins.getOrDefault(p, 0d) + amount);
//            RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runLaterAsync(() -> {
            RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runLaterAsync(() -> {
                double coins = Math.round(dynamicCoins.getOrDefault(finalP, 0d) - amount * 100) / 100d;
                if (coins <= 0) {
                    dynamicCoins.remove(finalP);
                    return;
                }

                dynamicCoins.put(finalP, coins);
            }, 40);
        }
    }

}
