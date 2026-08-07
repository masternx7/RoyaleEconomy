package me.qKing12.RoyaleEconomy.DataManager;

import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import me.qKing12.RoyaleEconomy.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.Statement;

import static me.qKing12.RoyaleEconomy.DataManager.DataManagerMySQL.database;

public class ExtendCoinsMaximum implements CommandExecutor {

    public ExtendCoinsMaximum(){
        RoyaleEconomy.plugin.getCommand("rec_extend_coins_maximum_value").setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if(commandSender instanceof ConsoleCommandSender){
            if(args.length>0) {
                Integer digits=50;
                try {
                    digits = Integer.parseInt(args[0]);
                }catch(Exception x){
                    commandSender.sendMessage(Utils.chat("&aThe number of digits is invalid!"));
                    return false;
                }
                if(digits>=300){
                    commandSender.sendMessage(Utils.chat("&aThe number of digits is too big."));
                    return false;
                }
                try (Connection Database = HikariCPDataSource.getConnection();
                     Statement stmt = Database.createStatement();
                ) {

                    stmt.addBatch("ALTER TABLE " + database + ".PlayerPurse MODIFY COLUMN coins DOUBLE("+digits+", 2)");
                    stmt.addBatch("ALTER TABLE " + database + ".PersonalBank MODIFY COLUMN coins DOUBLE("+digits+", 2)");
                    stmt.addBatch("ALTER TABLE " + database + ".ExternalGeneratedData MODIFY COLUMN coins DOUBLE("+digits+", 2)");
                    stmt.addBatch("ALTER TABLE " + database + ".SharedBank MODIFY COLUMN coins DOUBLE("+digits+", 2)");
                    stmt.executeBatch();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                commandSender.sendMessage(Utils.chat("&aBigger numbers register attempted!"));
            }
            else
                commandSender.sendMessage(Utils.chat("&aSpecify the maximum number of digits you want the plugin to handle! (Maximum 300)"));

        }
        else
            commandSender.sendMessage(Utils.chat("&cFor safety reasons, this command is executable by console only."));
        return false;
    }
}
