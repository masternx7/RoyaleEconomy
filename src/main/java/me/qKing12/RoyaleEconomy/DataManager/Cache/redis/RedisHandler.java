package me.qKing12.RoyaleEconomy.DataManager.Cache.redis;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import me.qKing12.RoyaleEconomy.DataManager.Cache.PlayerMoneyCacheMySQL;
import me.qKing12.RoyaleEconomy.DataManager.DataManagerMySQL;
import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import me.qKing12.RoyaleEconomy.utils.PlayerMessageHandler;
import me.qKing12.RoyaleEconomy.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import redis.clients.jedis.*;
import redis.clients.jedis.exceptions.JedisException;

import java.util.UUID;

import static me.qKing12.RoyaleEconomy.RoyaleEconomy.utilsAPI;

public class RedisHandler {

    private static JedisPool jedisPool;
    public static boolean useRedis=false;
    private static JedisPoolConfig jedisPoolConfig;
    private String password;
    private String ip;
    private int port;

    private static BinaryJedisPubSub subscriber;

    private static String channel;

    public static void close(){
        if(!useRedis)
            return;

        if(subscriber!=null)
            subscriber.unsubscribe();

        if(jedisPool!=null && !jedisPool.isClosed()) {
            jedisPool.close();
        }
    }

    public RedisHandler(){
        useRedis=true;
        password= RoyaleEconomy.plugin.getConfig().getString("redis.password");
        channel=RoyaleEconomy.plugin.getConfig().getString("redis.channel");
        ip=RoyaleEconomy.plugin.getConfig().getString("redis.host");
        port=RoyaleEconomy.plugin.getConfig().getInt("redis.port");

        jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxIdle(0);
        jedisPoolConfig.setTestOnBorrow(true);
        jedisPoolConfig.setTestOnReturn(true);

        if(password.isEmpty()){
            jedisPool = new JedisPool(jedisPoolConfig, ip, port, 0, false);
        }
        else {
            jedisPool = new JedisPool(jedisPoolConfig, ip, port, 0, password, false);
        }

        subscriber = new BinaryJedisPubSub() {
            @Override
            public void onMessage(byte[] channel, byte[] message) {
                Player player = null;

                ByteArrayDataInput in = ByteStreams.newDataInput(message);
                String uuidPrim = in.readUTF();
                if (!uuidPrim.equals("bankInterest.") && !uuidPrim.equals("sharedBankModification.") && !uuidPrim.equals("bankCreation.")) {
                    try {
                        player = uuidPrim.length() > 16 ? Bukkit.getPlayer(UUID.fromString(uuidPrim)) : Bukkit.getPlayerExact(uuidPrim);
                    } catch (Exception x) {

                    }

                    if (player == null)
                        return;
                }


                String subChannel=in.readUTF();
                if(subChannel.equalsIgnoreCase("RoyaleEconomyMoneyTransfer")) {
                    String action = in.readUTF();
                    if (action.equalsIgnoreCase("addMoney")) {
                        in.readUTF();
                        double amount = in.readDouble();

                        PlayerMoneyCacheMySQL.RECPlayer recPlayer = ((PlayerMoneyCacheMySQL) RoyaleEconomy.playerMoneyCache).getRecPlayer(player);
                        recPlayer.addAmount(amount);
                    } else if (action.equalsIgnoreCase("removeMoney")) {
                        in.readUTF();
                        double amount = in.readDouble();

                        //RoyaleEconomy.plugin.getLogger().info("[DEBUG] Received remove money signal " + player.getName() + " " + amount);

                        PlayerMoneyCacheMySQL.RECPlayer recPlayer = ((PlayerMoneyCacheMySQL) RoyaleEconomy.playerMoneyCache).getRecPlayer(player);

                        recPlayer.removeAmount(amount);
                    } else if (action.equalsIgnoreCase("setMoney")) {
                        in.readUTF();
                        double amount = in.readDouble();

                        PlayerMoneyCacheMySQL.RECPlayer recPlayer = ((PlayerMoneyCacheMySQL) RoyaleEconomy.playerMoneyCache).getRecPlayer(player);
                        recPlayer.setAmount(amount);
                    } else if (action.equalsIgnoreCase("payMoney")) {
                        in.readUTF();
                        double amount = in.readDouble();

                        PlayerMoneyCacheMySQL.RECPlayer recPlayer = ((PlayerMoneyCacheMySQL) RoyaleEconomy.playerMoneyCache).getRecPlayer(player);
                        recPlayer.addAmount(amount);

                        Utils.playSound(player, "commands.pay.to-player");
                        PlayerMessageHandler.messageSend(player, utilsAPI.chat(player, in.readUTF()));
                    }
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
        };

        try {
            jedisPool.getResource().ping();
        } catch (JedisException e) {
            RoyaleEconomy.plugin.getLogger().info("Could not connect to Redis.");
            e.printStackTrace();
            return;
        }

        RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runAsync((task) -> {
            try(Jedis j = jedisPool.getResource()){
                j.connect();
                j.subscribe(subscriber, channel.getBytes());
                RoyaleEconomy.plugin.getLogger().info("Loaded and connected to Redis.");
            }
            catch(Exception x){
                x.printStackTrace();
            }
        });
    }

    public static void sendData(byte[] message){
        try(Jedis j = jedisPool.getResource()){
            j.publish(channel.getBytes(), message);
        }
        catch(Exception x){
            x.printStackTrace();
        }
    }
}
