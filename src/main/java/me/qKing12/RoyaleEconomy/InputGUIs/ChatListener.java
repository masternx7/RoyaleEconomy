package me.qKing12.RoyaleEconomy.InputGUIs;

import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import me.qKing12.RoyaleEconomy.utils.PlayerMessageHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import static me.qKing12.RoyaleEconomy.RoyaleEconomy.utilsAPI;

public class ChatListener {

    private Player p;
    private ListenUp listener;

    private boolean listening;

    private chatHandler handler;

    private boolean forceNumber;

    public ChatListener(Player p, chatHandler handler){
        this(p, handler, true);
    }

    public ChatListener(Player p, chatHandler handler, boolean forceNumber){
        this.forceNumber = forceNumber;
        this.p=p;
        this.listener=new ListenUp();
        Bukkit.getPluginManager().registerEvents(listener, RoyaleEconomy.plugin);
        this.listening=true;
        this.handler=handler;
        RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runLater(() -> {
            if(this.listening){
                HandlerList.unregisterAll(listener);
                this.handler=null;
                PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.plugin.getConfig().getString("input-guis.chat-listener-end-message")));
            }
        }, 500);
    }

    private class ListenUp implements Listener {

        @EventHandler(priority=EventPriority.LOWEST)
        public void chatListener(AsyncPlayerChatEvent e){
            if(e.getPlayer().equals(p)){
                e.setCancelled(true);
                HandlerList.unregisterAll(listener);
                listening=false;
                String message= ChatColor.stripColor(e.getMessage());
                if(message.endsWith("."))
                    message=message.substring(0, message.length()-1);
                int index = -1;

                if (forceNumber) {
                    for (int i = 0; i < message.length(); i++) {
                        if (Character.isDigit(message.charAt(i)) || message.charAt(i) == '-') {
                            index = i;
                            break;
                        }
                    }
                }

                if (index != -1) {
                    message = message.substring(index);
                }
                handler.onChat(message);
                handler=null;

            }
        }

        @EventHandler
        public void inventoryOpen(InventoryOpenEvent event){
            if(event.getPlayer().equals(p))
                event.setCancelled(true);
        }

        @EventHandler
        public void onCommand(PlayerCommandPreprocessEvent event){
            if(event.getPlayer().equals(p))
                event.setCancelled(true);
        }
    }

    public interface chatHandler{
        void onChat(String input);
    }
}
