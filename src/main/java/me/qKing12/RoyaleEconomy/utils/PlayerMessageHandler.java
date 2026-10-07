package me.qKing12.RoyaleEconomy.utils;

import me.qKing12.RoyaleEconomy.PlaceholderAPISupport.PlaceholderAPISupport;
import me.qKing12.RoyaleEconomy.PlaceholderAPISupport.PlaceholderAPISupportYes;
import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PlayerMessageHandler {
    private interface actionBarSend{
        void sendActionBar(Player p, String message);
    }

    private static actionBarSend actionBarHandler;

    private class actionBar implements actionBarSend{
        @Override
        public void sendActionBar(Player p, String message) {
            p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
        }
    }

    public PlayerMessageHandler(){
        actionBarHandler=new actionBar();
    }

    public static void messageSend(Player p, String message){
        if(message.startsWith("actionbar:")){
            actionBarHandler.sendActionBar(p, message.substring(10));
        }
        else if(!message.isEmpty())
            p.sendMessage(message);
    }

    public static void messageSend(CommandSender p, String message){
        if((p instanceof Player) && message.startsWith("actionbar:")){
            actionBarHandler.sendActionBar((Player)p, message.substring(10));
        }
        else if(!message.isEmpty())
            p.sendMessage(message);
    }

//    public static void sendShopMessage(Player p, String message, ItemStack item){
//        if(item.getItemMeta().hasDisplayName()) {
//            messageSend(p, message.replace("%item-display-name%", item.getItemMeta().getDisplayName()));
//            return;
//        }
//
//        String[] messages = message.split("%item-display-name%");
//        if(messages.length==1){
//            messageSend(p, message);
//            return;
//        }
//
//
//        System.out.println(Utils.getNameForTranslate(item));
//
//        p.spigot().sendMessage( new TextComponent(messages[0]),
//                                new TranslatableComponent(Utils.getNameForTranslate(item)),
//                                new TextComponent(messages[1])
//        );
//    }
}
