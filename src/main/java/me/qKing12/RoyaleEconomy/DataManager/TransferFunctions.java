package me.qKing12.RoyaleEconomy.DataManager;

import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import me.qKing12.RoyaleEconomy.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.sql.*;

public class TransferFunctions implements CommandExecutor {

    public TransferFunctions(){
        RoyaleEconomy.plugin.getCommand("rec_sqlite_to_mysql").setExecutor(this);
        RoyaleEconomy.plugin.getCommand("rec_mysql_to_sqlite").setExecutor(this);
    }

    void transferFromSQLiteToMySQL(CommandSender sender){
        File sqlBase = new File(RoyaleEconomy.plugin.getDataFolder(), "database/royaleEconomyData.db");
        if(!sqlBase.exists()){
            sender.sendMessage(Utils.chat("&cThe SQL Database does not exist. Nothing to transfer from."));
        }
        else if(!RoyaleEconomy.plugin.getConfig().getBoolean("mysql.use-mysql")){
            sender.sendMessage(Utils.chat("&cMySQL is not connected. No location to transfer to."));
        }
        else{
            sender.sendMessage(Utils.chat("&aClearing MySQL Data..."));
            String tableSqlite = RoyaleEconomy.dataManager.getSharedBankManager().getTable().split("\\.")[1];
            try(Connection connection = HikariCPDataSource.getConnection();
                Statement statement = connection.createStatement();
            ){
                statement.addBatch("DELETE FROM "+DataManagerMySQL.database+".PlayerPurse");
                statement.addBatch("DELETE FROM "+DataManagerMySQL.database+".PersonalBank");
                statement.addBatch("DELETE FROM "+RoyaleEconomy.dataManager.getSharedBankManager().getTable());
                statement.addBatch("DELETE FROM "+DataManagerMySQL.database+".ExternalGeneratedData");
                statement.executeBatch();
            }catch (Exception x){
                sender.sendMessage(Utils.chat("&cCouldn't clear MySQL Data... Abandoning Transfer"));
                x.printStackTrace();
                return;
            }
            sender.sendMessage(Utils.chat("&aMySql Cleared! Importing data..."));
            try(Connection connectionMySQL = HikariCPDataSource.getConnection();
                Connection connectionSQLite = DriverManager.getConnection("jdbc:sqlite:" + RoyaleEconomy.plugin.getDataFolder() + "/database/royaleEconomyData.db");
                ResultSet playerPurse = connectionSQLite.prepareStatement("SELECT * FROM PlayerPurse").executeQuery();
                ResultSet bank = connectionSQLite.prepareStatement("SELECT * FROM PersonalBank").executeQuery();
                ResultSet sharedBank = connectionSQLite.prepareStatement("SELECT * FROM "+tableSqlite).executeQuery();
                ResultSet externalData = connectionSQLite.prepareStatement("SELECT * FROM ExternalGeneratedData").executeQuery();
                PreparedStatement stmt1 = connectionMySQL.prepareStatement("INSERT INTO "+DataManagerMySQL.database+".PlayerPurse VALUES (?, ? ,?, ?)");
                PreparedStatement stmt2 = connectionMySQL.prepareStatement("INSERT INTO "+DataManagerMySQL.database+".PersonalBank VALUES (?, ? , ?, ?)");
                PreparedStatement stmt3 = connectionMySQL.prepareStatement("INSERT INTO "+RoyaleEconomy.dataManager.getSharedBankManager().getTable()+" VALUES (?, "+(RoyaleEconomy.hooked!=null?"":"? , ?,")+" ?, ?, ?)");
                PreparedStatement stmt4 = connectionMySQL.prepareStatement("INSERT INTO "+DataManagerMySQL.database+".ExternalGeneratedData VALUES (?,?)");
            ) {
                connectionMySQL.setAutoCommit(false);
                while (playerPurse.next()) {
                    stmt1.setString(1, playerPurse.getString(1));
                    stmt1.setString(2, playerPurse.getString(2));
                    stmt1.setDouble(3, playerPurse.getDouble(3));
                    stmt1.setString(4, playerPurse.getString(4));
                    stmt1.addBatch();
                }

                while (bank.next()) {
                    stmt2.setString(1, bank.getString(1));
                    stmt2.setDouble(2, bank.getDouble(2));
                    stmt2.setInt(3, bank.getInt(3));
                    stmt2.setString(4, bank.getString(4));
                    stmt2.addBatch();
                }

                if (RoyaleEconomy.hooked != null)
                    while (sharedBank.next()) {
                        stmt3.setString(1, sharedBank.getString(1));
                        stmt3.setDouble(2, sharedBank.getDouble(2));
                        stmt3.setInt(3, sharedBank.getInt(3));
                        stmt3.setString(4, sharedBank.getString(4));
                        stmt3.addBatch();
                    }
                else
                    while (sharedBank.next()) {
                        stmt3.setString(1, sharedBank.getString(1));
                        stmt3.setString(2, sharedBank.getString(2));
                        stmt3.setString(3, sharedBank.getString(3));
                        stmt3.setDouble(4, sharedBank.getDouble(4));
                        stmt3.setInt(5, sharedBank.getInt(5));
                        stmt3.setString(6, sharedBank.getString(6));
                        stmt3.addBatch();
                    }

                while (externalData.next()) {
                    stmt4.setString(1, externalData.getString(1));
                    stmt4.setDouble(2, externalData.getDouble(2));
                    stmt4.addBatch();
                }

                stmt1.executeBatch();
                stmt2.executeBatch();
                stmt3.executeBatch();
                stmt4.executeBatch();
                connectionMySQL.setAutoCommit(true);
            }catch (Exception x){
                x.printStackTrace();
                sender.sendMessage(Utils.chat("&cImport failed."));
                return;
            }
            sender.sendMessage(Utils.chat("&aImport done!"));
        }
    }

    void transferFromMySQLToSQLite(CommandSender sender){
        File sqlBase = new File(RoyaleEconomy.plugin.getDataFolder(), "database/royaleEconomyData.db");
        if(!RoyaleEconomy.plugin.getConfig().getBoolean("mysql.use-mysql")){
            sender.sendMessage(Utils.chat("&cMySQL is not connected. No location to transfer to."));
        }
        else{
            sender.sendMessage(Utils.chat("&aRegenerating SQLite File..."));
            if(sqlBase.exists()){
                sqlBase.delete();
                try {
                    sqlBase.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            new SQLLoad();
            sender.sendMessage(Utils.chat("&aSQLite Data Cleared! Importing data..."));
            String tableSqlite = RoyaleEconomy.dataManager.getSharedBankManager().getTable().split("\\.")[1];
            try(Connection connectionMySQL = HikariCPDataSource.getConnection();
                Connection connectionSQLite = DriverManager.getConnection("jdbc:sqlite:" + RoyaleEconomy.plugin.getDataFolder() + "/database/royaleEconomyData.db");
                ResultSet playerPurse = connectionMySQL.prepareStatement("SELECT * FROM "+DataManagerMySQL.database+".PlayerPurse").executeQuery();
                ResultSet bank = connectionMySQL.prepareStatement("SELECT * FROM "+DataManagerMySQL.database+".PersonalBank").executeQuery();
                ResultSet sharedBank = connectionMySQL.prepareStatement("SELECT * FROM "+RoyaleEconomy.dataManager.getSharedBankManager().getTable()).executeQuery();
                ResultSet externalData = connectionMySQL.prepareStatement("SELECT * FROM "+DataManagerMySQL.database+".ExternalGeneratedData").executeQuery();
                PreparedStatement stmt1 = connectionSQLite.prepareStatement("INSERT INTO PlayerPurse VALUES (?, ? ,?, ?)");
                PreparedStatement stmt2 = connectionSQLite.prepareStatement("INSERT INTO PersonalBank VALUES (?, ? , ?, ?)");
                PreparedStatement stmt3 = connectionSQLite.prepareStatement("INSERT INTO "+tableSqlite+" VALUES (?, ? , "+(RoyaleEconomy.hooked!=null?"":"?, ?,")+" ?, ?)");
                PreparedStatement stmt4 = connectionSQLite.prepareStatement("INSERT INTO ExternalGeneratedData VALUES (?,?)");
            ){
                connectionSQLite.setAutoCommit(false);
                while(playerPurse.next()){
                    stmt1.setString(1, playerPurse.getString(1));
                    stmt1.setString(2, playerPurse.getString(2));
                    stmt1.setDouble(3, playerPurse.getDouble(3));
                    stmt1.setString(4, playerPurse.getString(4));
                    stmt1.addBatch();
                }

                while(bank.next()){
                    stmt2.setString(1, bank.getString(1));
                    stmt2.setDouble(2, bank.getDouble(2));
                    stmt2.setInt(3, bank.getInt(3));
                    stmt2.setString(4, bank.getString(4));
                    stmt2.addBatch();
                }

                if (RoyaleEconomy.hooked != null)
                    while (sharedBank.next()) {
                        stmt3.setString(1, sharedBank.getString(1));
                        stmt3.setDouble(2, sharedBank.getDouble(2));
                        stmt3.setInt(3, sharedBank.getInt(3));
                        stmt3.setString(4, sharedBank.getString(4));
                        stmt3.addBatch();
                    }
                else
                    while (sharedBank.next()) {
                        stmt3.setString(1, sharedBank.getString(1));
                        stmt3.setString(2, sharedBank.getString(2));
                        stmt3.setString(3, sharedBank.getString(3));
                        stmt3.setDouble(4, sharedBank.getDouble(4));
                        stmt3.setInt(5, sharedBank.getInt(5));
                        stmt3.setString(6, sharedBank.getString(6));
                        stmt3.addBatch();
                    }

                while(externalData.next()){
                    stmt4.setString(1, externalData.getString(1));
                    stmt4.setDouble(2, externalData.getDouble(2));
                    stmt4.addBatch();
                }

                stmt1.executeBatch();
                stmt2.executeBatch();
                stmt3.executeBatch();
                stmt4.executeBatch();
                connectionSQLite.setAutoCommit(true);
            }catch (Exception x){
                x.printStackTrace();
                sender.sendMessage(Utils.chat("&cImport failed."));
                return;
            }
            sender.sendMessage(Utils.chat("&aImport done!"));
        }
    }


    private static boolean confirmSqliteToMysql=false;
    private static boolean confirmMySQLtoSqlite=false;

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(!(sender instanceof ConsoleCommandSender)){
            sender.sendMessage(Utils.chat("&cFor safety reasons this command can only be executed by console."));
            return false;
        }

        if(label.equalsIgnoreCase("rec_sqlite_to_mysql")){
            if(args.length>0){
                if(args[0].equalsIgnoreCase("confirm") && confirmSqliteToMysql){
                    transferFromSQLiteToMySQL(sender);

                    confirmSqliteToMysql=false;
                    return false;
                }
            }
            sender.sendMessage(Utils.chat("&c&lATTENTION! &fAre you sure you want to transfer"));
            sender.sendMessage(Utils.chat("&ffrom &cSQLite &fto &cMySQL&f?"));
            sender.sendMessage(Utils.chat("&fIf there is any data inside MySQL it will be &c&lDELETED&f!"));
            sender.sendMessage(Utils.chat("&fType &e/rec_sqlite_to_mysql confirm &fto confirm SQLite -> MySQL Transfer."));
            confirmSqliteToMysql=true;
        }
        else {
            if(args.length>0){
                if(args[0].equalsIgnoreCase("confirm") && confirmMySQLtoSqlite){
                    transferFromMySQLToSQLite(sender);
                    confirmMySQLtoSqlite=false;
                    return false;
                }
            }
            sender.sendMessage(Utils.chat("&c&lATTENTION! &fAre you sure you want to transfer"));
            sender.sendMessage(Utils.chat("&ffrom &cMySQL &fto &cSQLite&f?"));
            sender.sendMessage(Utils.chat("&fIf there is any data inside SQLite it will be &c&lDELETED&f!"));
            sender.sendMessage(Utils.chat("&fType &e/rec_mysql_to_sqlite confirm &fto confirm MySQL -> SQLite Transfer."));
            confirmMySQLtoSqlite=true;
        }
        return false;
    }
}
