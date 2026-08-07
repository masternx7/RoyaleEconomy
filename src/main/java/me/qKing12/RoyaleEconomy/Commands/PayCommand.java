package me.qKing12.RoyaleEconomy.Commands;

import me.qKing12.RoyaleEconomy.DataManager.Cache.BungeeMessagingCacheMySQL;
import me.qKing12.RoyaleEconomy.DataManager.Cache.redis.RedisHandler;
import me.qKing12.RoyaleEconomy.DataManager.DataManagerMySQL;
import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import me.qKing12.RoyaleEconomy.utils.PermissionChecker;
import me.qKing12.RoyaleEconomy.utils.PlayerMessageHandler;
import me.qKing12.RoyaleEconomy.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static me.qKing12.RoyaleEconomy.RoyaleEconomy.utilsAPI;



public class PayCommand implements TabExecutor {
    private static ArrayList<String> toggled;
    private static File payToggles;
    private static FileConfiguration payTogglesCfg;
    private static boolean updated=false;

    public static void saveToFile(){
        if(updated) {
            payTogglesCfg.set("pay-toggled", toggled);
            try {
                payTogglesCfg.save(payToggles);
            } catch (IOException e) {
                e.printStackTrace();
            }
            updated=false;
        }
    }

    public PayCommand() {
        //RoyaleEconomy.plugin.getCommand("pay").setExecutor(this);
    }

    public void extraSetup(){
        payToggles=new File(RoyaleEconomy.plugin.getDataFolder(), "database/payData.yml");
        if(!payToggles.exists()) {
            try {
                payToggles.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        payTogglesCfg=YamlConfiguration.loadConfiguration(payToggles);

        if(payTogglesCfg.contains("pay-toggled"))
            toggled=(ArrayList<String>)payTogglesCfg.getStringList("pay-toggled");
        else
            toggled=new ArrayList<>();

        RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runTimerAsync(PayCommand::saveToFile, 12000, 12000);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            if(!PermissionChecker.checkPermission("commands.pay", p))
                return true;

            if(args.length<2){
                if(args.length>0 && args[0].equals("toggle")){
                    if(toggled.contains(p.getUniqueId().toString())){
                        toggled.remove(p.getUniqueId().toString());
                        p.sendMessage(RoyaleEconomy.staticValues.togglePayOff);
                    }
                    else{
                        toggled.add(p.getUniqueId().toString());
                        p.sendMessage(RoyaleEconomy.staticValues.togglePayOn);
                    }
                    updated=true;
                }
                else
                    PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.pay.no-args-message")));
            }
            else{
                try {
                    Double coins = RoyaleEconomy.messageHelper.useDecimals ? Math.floor(RoyaleEconomy.messageHelper.getCoinsFromFormat(args[1])*100)/100 : Math.floor(RoyaleEconomy.messageHelper.getCoinsFromFormat(args[1]));
                    if(coins<=0){
                        PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.pay.invalid-number")));
                        return true;
                    }
                    Player p2 = Bukkit.getPlayerExact(args[0]);
                    if(p2==null){
                        String uuid=RoyaleEconomy.dataManager.getUUIDfromName(args[0]);

                        if(uuid!=null){
                            if(toggled.contains(uuid)){
                                p.sendMessage(RoyaleEconomy.staticValues.toggleMessage);
                                return true;
                            }
                            Double totalCoins = RoyaleEconomy.dataManager.getMoneyFromFile(p.getUniqueId().toString());
                            if(totalCoins<coins){
                                PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.pay.no-coins-message")));
                                return true;
                            }

                            RoyaleEconomy.dataManager.removeMoneyFromFile(p.getUniqueId().toString(), coins);
                            if(BungeeMessagingCacheMySQL.bungeecord || RedisHandler.useRedis){
                                ((DataManagerMySQL)RoyaleEconomy.dataManager).getEconomyFunctions().addMoneyFromPay(uuid, coins, RoyaleEconomy.commandsCfg.getString("commands.pay.coins-received-message").replace("%from-player-display-name%", p.getDisplayName()).replace("%to-player-display-name%", args[0]).replace("%from-player%", p.getName()).replace("%to-player%", args[0]).replace("%coins%", RoyaleEconomy.messageHelper.numberFormat(coins)));
                            }
                            else
                                RoyaleEconomy.dataManager.addMoneyToFile(uuid, coins);

                            Utils.playSound(p, "commands.pay.from-player");
                            PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.pay.coins-given-message").replace("%from-player-display-name%", p.getDisplayName()).replace("%to-player-display-name%", args[0]).replace("%from-player%", p.getName()).replace("%to-player%", args[0]).replace("%coins%", RoyaleEconomy.messageHelper.numberFormat(coins))));

                            return true;
                        }

                        PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.pay.invalid-player")));
                        return true;
                    }
                    else if(toggled.contains(p2.getUniqueId().toString())){
                        p.sendMessage(RoyaleEconomy.staticValues.toggleMessage);
                        return true;
                    }
                    Double totalCoins = RoyaleEconomy.dataManager.getMoneyFromFile(p.getUniqueId().toString());
                    if(totalCoins<coins){
                        PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.pay.no-coins-message")));
                        return true;
                    }
                    if(!p.getName().equals(p2.getName())) {
                        RoyaleEconomy.dataManager.removeMoneyFromFile(p.getUniqueId().toString(), coins);
                        RoyaleEconomy.dataManager.addMoneyToFile(p2.getUniqueId().toString(), coins);
                    }
                    else if(RoyaleEconomy.commandsCfg.getBoolean("commands.pay.disable-self-pay")){
                        PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.pay.pay-self-message")));
                        return true;
                    }
                    Utils.playSound(p, "commands.pay.from-player");
                    Utils.playSound(p2, "commands.pay.to-player");
                    PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.pay.coins-given-message").replace("%from-player-display-name%", p.getDisplayName()).replace("%to-player-display-name%", p2.getDisplayName()).replace("%from-player%", p.getName()).replace("%to-player%", p2.getName()).replace("%coins%", RoyaleEconomy.messageHelper.numberFormat(coins))));
                    PlayerMessageHandler.messageSend(p2, utilsAPI.chat(p2, RoyaleEconomy.commandsCfg.getString("commands.pay.coins-received-message").replace("%from-player-display-name%", p.getDisplayName()).replace("%to-player-display-name%", p2.getDisplayName()).replace("%from-player%", p.getName()).replace("%to-player%", p2.getName()).replace("%coins%", RoyaleEconomy.messageHelper.numberFormat(coins))));
                }catch(Exception x){
                    PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, RoyaleEconomy.commandsCfg.getString("commands.pay.invalid-number")));
                }
            }
        }
        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return null;
    }
}
