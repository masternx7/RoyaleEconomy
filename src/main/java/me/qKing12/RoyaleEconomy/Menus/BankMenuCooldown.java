package me.qKing12.RoyaleEconomy.Menus;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BankMenuCooldown implements Listener {
    private static final BankMenuCooldown instance = new BankMenuCooldown();

    public static BankMenuCooldown getInstance() {
        return instance;
    }
    private final ConcurrentHashMap<UUID, Long> cooldowns = new ConcurrentHashMap<>();

    public boolean tryAccess(UUID uuid) {
        if (cooldowns.containsKey(uuid)) {
            long time = cooldowns.get(uuid);
            if (System.currentTimeMillis() - time < 1000) {
                return false;
            }
        }
        cooldowns.put(uuid, System.currentTimeMillis());
        return true;
    }

    public boolean removeCooldown(UUID uuid) {
        return cooldowns.remove(uuid) != null;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent quitEvent) {
        UUID uuid = quitEvent.getPlayer().getUniqueId();
        cooldowns.remove(uuid);
    }
}
