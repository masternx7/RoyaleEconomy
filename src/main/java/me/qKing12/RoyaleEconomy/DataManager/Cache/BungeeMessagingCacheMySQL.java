package me.qKing12.RoyaleEconomy.DataManager.Cache;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import me.qKing12.RoyaleEconomy.DataManager.DataManagerMySQL;
import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import me.qKing12.RoyaleEconomy.utils.PlayerMessageHandler;
import me.qKing12.RoyaleEconomy.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import static me.qKing12.RoyaleEconomy.RoyaleEconomy.utilsAPI;

public class BungeeMessagingCacheMySQL implements PluginMessageListener {
    public static boolean bungeecord=false;

    public BungeeMessagingCacheMySQL(){
        Bukkit.getServer().getMessenger().registerIncomingPluginChannel(RoyaleEconomy.plugin, "my:reconomybungee", this);
        Bukkit.getServer().getMessenger().registerOutgoingPluginChannel(RoyaleEconomy.plugin, "BungeeCord");
        Bukkit.getServer().getMessenger().registerIncomingPluginChannel(RoyaleEconomy.plugin, "royaleeconomy:main", this);
        Bukkit.getServer().getMessenger().registerOutgoingPluginChannel(RoyaleEconomy.plugin, "royaleeconomy:main");
        bungeecord=true;
    }


    @Override
    public synchronized void onPluginMessageReceived(String channel, Player player, byte[] bytes)
    {
        //if (!channel.equalsIgnoreCase( "my:royaleeconomybungee" ) )
        //    return;
        ByteArrayDataInput in = ByteStreams.newDataInput( bytes );
        String subChannel=in.readUTF();
        if(subChannel.equalsIgnoreCase("RoyaleEconomyMoneyTransfer")) {
            PlayerMoneyCacheMySQL.RECPlayer recPlayer = ((PlayerMoneyCacheMySQL) RoyaleEconomy.playerMoneyCache).getRecPlayer(player);
            if (recPlayer.previousAmount == -1) {
                RoyaleEconomy.plugin.getLogger().info("Received a message from BungeeCord before the player's cache was loaded ("+player.getName()+"). Ignoring it.");
                return;
            }

            if (recPlayer.syncMilliseconds != null && recPlayer.syncMilliseconds + 1500 > System.currentTimeMillis()) {
                RoyaleEconomy.plugin.getLogger().info("Received a message from BungeeCord concurrent with player's cache loading ("+player.getName()+"). Delaying it.");

                RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runLaterAsync(() -> onPluginMessageReceived(channel, player, bytes), 30);
                return;
            }

            double difference = recPlayer.getNewAmount() - recPlayer.previousAmount;
            recPlayer.setAmount(((PlayerMoneyCacheMySQL) RoyaleEconomy.playerMoneyCache).getMoneyFromFile(player.getUniqueId().toString()) + difference);
//            String action = in.readUTF();
//            if (action.equalsIgnoreCase("addMoney")) {
//                in.readUTF();
//                double amount = in.readDouble();
//
//                recPlayer.newAmount += amount;
//            } else if (action.equalsIgnoreCase("removeMoney")) {
//                in.readUTF();
//                double amount = in.readDouble();
//
//                PlayerMoneyCacheMySQL.RECPlayer recPlayer = ((PlayerMoneyCacheMySQL) RoyaleEconomy.playerMoneyCache).getRecPlayer(player);
//                if (recPlayer.newAmount < amount)
//                    recPlayer.newAmount = 0;
//                else
//                    recPlayer.newAmount -= amount;
//            } else if (action.equalsIgnoreCase("setMoney")) {
//                in.readUTF();
//                double amount = in.readDouble();
//
//                PlayerMoneyCacheMySQL.RECPlayer recPlayer = ((PlayerMoneyCacheMySQL) RoyaleEconomy.playerMoneyCache).getRecPlayer(player);
//                recPlayer.newAmount = amount;
//            } else if (action.equalsIgnoreCase("payMoney")) {
//                in.readUTF();
//                double amount = in.readDouble();
//
//                PlayerMoneyCacheMySQL.RECPlayer recPlayer = ((PlayerMoneyCacheMySQL) RoyaleEconomy.playerMoneyCache).getRecPlayer(player);
//                recPlayer.newAmount += amount;
//
//                Utils.playSound(player, "commands.pay.to-player");
//                PlayerMessageHandler.messageSend(player, utilsAPI.chat(player, in.readUTF()));
//            }
//            else if(action.equalsIgnoreCase("currencyUpdate")){
//                String uuid = in.readUTF();
//                String currencyId = in.readUTF();
//                Currency currency = MultiCurrencyHandler.findCurrencyById(currencyId);
//                if(currency!=null){
//                    currency.removeAmountCache(uuid, false);
//                }
//            }
        }
        else if(subChannel.equalsIgnoreCase("RoyaleEconomyBankInterest")){
            DataManagerMySQL.bankCache.clearCache(false);
        }
        else if(subChannel.equalsIgnoreCase("RoyaleEconomyBankTransfer")){
            String action = in.readUTF();
            if(action.equalsIgnoreCase("bankModification"))
                DataManagerMySQL.bankCache.bankModification(in.readUTF(), false);
            else if(action.equalsIgnoreCase("sharedBankModification"))
                DataManagerMySQL.bankCache.sharedBankModification(in.readUTF(), false);
            else if(action.equalsIgnoreCase("sharedBankMemberModification"))
                DataManagerMySQL.bankCache.sharedBankMemberModification(in.readUTF(), false);
            else if(action.equalsIgnoreCase("bankCreation"))
                DataManagerMySQL.bankCache.deleteSharedBankCache(in.readUTF(), false);
        }
    }

}
