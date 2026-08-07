package me.qKing12.RoyaleEconomy.Commands;

import me.qKing12.RoyaleEconomy.Commands.dynamic.DynamicCommandsSetup;
import me.qKing12.RoyaleEconomy.KillCoins.KillCoinsLimitInterface;
import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import me.qKing12.RoyaleEconomy.utils.PermissionChecker;
import me.qKing12.RoyaleEconomy.utils.PlayerMessageHandler;
import me.qKing12.RoyaleEconomy.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static me.qKing12.RoyaleEconomy.RoyaleEconomy.utilsAPI;



public class KillCoinsCommand implements TabExecutor {

    private boolean hasKillEvents;
    private boolean hasCustomKillEvents;
    private boolean hasMythicMobs;

    public static boolean canReceiveMessages(String uuid){
        return !toggledKillCoins.contains(uuid);
    }

    static ArrayList<String> toggledKillCoins=new ArrayList<>();
    private KillCoinsLimitInterface killCoinsIntern;

    public KillCoinsCommand() {
        //RoyaleEconomy.plugin.getCommand("killcoins").setExecutor(this);
        DynamicCommandsSetup.getCommandSettings().put("kill-coins", this);
    }

    public void extraSetup(){
        hasKillEvents=RoyaleEconomy.killCoinsAndPurseDeathCfg.getConfigurationSection("kill-coins.kill-events") != null;
        hasCustomKillEvents= RoyaleEconomy.killCoinsAndPurseDeathCfg.getConfigurationSection("kill-coins.custom-kill-events") != null;
        hasMythicMobs=RoyaleEconomy.killCoinsAndPurseDeathCfg.getConfigurationSection("kill-coins.mythic-mobs-events") != null;
        killCoinsIntern=RoyaleEconomy.killCoinsMainHandle.killCoins;
        FileConfiguration data = killCoinsIntern.getData();
        if(data.contains("toggle")){
            toggledKillCoins=(ArrayList<String>)data.getStringList("toggle");
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            if(args.length>0 && (args[0].equalsIgnoreCase("messages") || args[0].equalsIgnoreCase("message"))){
                if(toggledKillCoins.contains(p.getUniqueId().toString())){
                    toggledKillCoins.remove(p.getUniqueId().toString());
                    p.sendMessage(RoyaleEconomy.staticValues.killCoinsMessagesOn);
                }
                else{
                    toggledKillCoins.add(p.getUniqueId().toString());
                    p.sendMessage(RoyaleEconomy.staticValues.killCoinsMessagesOff);
                }
                if(toggledKillCoins.isEmpty())
                    killCoinsIntern.getData().set("toggle", null);
                else
                    killCoinsIntern.getData().set("toggle", toggledKillCoins);
                return false;
            }
            if(!PermissionChecker.checkPermission("commands.killcoins", p))
                return true;
            Utils.playSound(p, "commands.killcoins");
            Double totalCoins = RoyaleEconomy.killCoinsMainHandle.killCoins.getTotalCoins(p);
            if(totalCoins==0){
                for(String line : RoyaleEconomy.commandsCfg.getStringList("commands.killcoins.structure.no-coins-message"))
                    PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, line));
                return true;
            }
            for(String line : RoyaleEconomy.commandsCfg.getStringList("commands.killcoins.structure.header"))
                PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, line.replace("%total%", RoyaleEconomy.messageHelper.numberFormat(totalCoins))));
            String structure = RoyaleEconomy.commandsCfg.getString("commands.killcoins.structure.kill-structure");
            if(hasKillEvents) {
                for (String key : RoyaleEconomy.killCoinsAndPurseDeathCfg.getConfigurationSection("kill-coins.kill-events").getKeys(false)) {
                    int count = RoyaleEconomy.killCoinsMainHandle.killCoins.getCount(p, key);
                    if (count == 0)
                        continue;
                    PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, structure
                            .replace("%mob-display-name%", RoyaleEconomy.killCoinsAndPurseDeathCfg.getString("kill-coins.kill-events." + key + ".display-name"))
                            .replace("%kill-count%", RoyaleEconomy.messageHelper.numberFormat((double) count))
                            .replace("%amount%", RoyaleEconomy.messageHelper.numberFormat(RoyaleEconomy.killCoinsMainHandle.killCoins.getMoney(p, key)))
                    ));
                }
            }
            if (hasCustomKillEvents) {
                for (String key : RoyaleEconomy.killCoinsAndPurseDeathCfg.getConfigurationSection("kill-coins.custom-kill-events").getKeys(false)) {
                    int count = RoyaleEconomy.killCoinsMainHandle.killCoins.getCount(p, "custom-kill-" + key);
                    if (count == 0)
                        continue;
                    PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, structure
                            .replace("%mob-display-name%", RoyaleEconomy.killCoinsAndPurseDeathCfg.getString("kill-coins.custom-kill-events." + key + ".display-name"))
                            .replace("%kill-count%", RoyaleEconomy.messageHelper.numberFormat((double) count))
                            .replace("%amount%", RoyaleEconomy.messageHelper.numberFormat(RoyaleEconomy.killCoinsMainHandle.killCoins.getMoney(p, "custom-kill-" + key)))
                    ));
                }
            }
            if(hasMythicMobs) {
                for (String key : RoyaleEconomy.killCoinsAndPurseDeathCfg.getConfigurationSection("kill-coins.mythic-mobs-events").getKeys(false)) {
                    int count = RoyaleEconomy.killCoinsMainHandle.killCoins.getCount(p, "mythic-mobs-" + key);
                    if (count == 0)
                        continue;
                    PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, structure
                            .replace("%mob-display-name%", RoyaleEconomy.killCoinsAndPurseDeathCfg.getString("kill-coins.mythic-mobs-events." + key + ".display-name"))
                            .replace("%kill-count%", RoyaleEconomy.messageHelper.numberFormat((double) count))
                            .replace("%amount%", RoyaleEconomy.messageHelper.numberFormat(RoyaleEconomy.killCoinsMainHandle.killCoins.getMoney(p, "mythic-mobs-" + key)))
                    ));
                }
            }
            int count= RoyaleEconomy.killCoinsMainHandle.killCoins.getCount(p, "default");
            if(count==0)
                return true;
            for(String line : RoyaleEconomy.commandsCfg.getStringList("commands.killcoins.structure.footer"))
                PlayerMessageHandler.messageSend(p, utilsAPI.chat(p, line
                        .replace("%other-kills%", RoyaleEconomy.messageHelper.numberFormat((double)count))
                        .replace("%other-amount%", RoyaleEconomy.messageHelper.numberFormat(RoyaleEconomy.killCoinsMainHandle.killCoins.getMoney(p, "default")))
                ));
        }
        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return null;
    }
}
