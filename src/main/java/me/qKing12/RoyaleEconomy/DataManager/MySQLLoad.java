package me.qKing12.RoyaleEconomy.DataManager;

import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import org.bukkit.Bukkit;

import java.io.File;
import java.sql.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;



public class MySQLLoad {

    public MySQLLoad(String database){
        loadPlayerPurse(database);
        loadPersonalBanks(database);
        loadSharedBanks(database);
        loadExternalData(database);
    }

    /*public void updateTables(String database){
        try(Connection Database = HikariCPDataSource.getConnection();
            Statement stmt = Database.createStatement();
        ) {

            stmt.addBatch("ALTER TABLE "+database+".PlayerPurse MODIFY COLUMN coins DOUBLE(25, 2)");
            stmt.addBatch("ALTER TABLE "+database+".PersonalBank MODIFY COLUMN coins DOUBLE(25, 2)");
            stmt.addBatch("ALTER TABLE "+database+".ExternalGeneratedData MODIFY COLUMN coins DOUBLE(25, 2)");
            stmt.addBatch("ALTER TABLE "+database+".SharedBank MODIFY COLUMN coins DOUBLE(25, 2)");
            stmt.addBatch("ALTER TABLE "+database+".SharedBank MODIFY COLUMN owner VARCHAR(36)");

            stmt.addBatch("ALTER TABLE "+database+".PlayerPurse CONVERT TO CHARACTER SET utf8");
            stmt.addBatch("ALTER TABLE "+database+".PersonalBank CONVERT TO CHARACTER SET utf8");
            stmt.addBatch("ALTER TABLE "+database+".ExternalGeneratedData CONVERT TO CHARACTER SET utf8");
            stmt.addBatch("ALTER TABLE "+database+".SharedBank CONVERT TO CHARACTER SET utf8");
            stmt.executeBatch();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/

    private void loadPlayerPurse(String database) {

        try(Connection PlayerPurse = HikariCPDataSource.getConnection();
            Statement stmt = PlayerPurse.createStatement();
        ) {

                String sql = "CREATE TABLE IF NOT EXISTS "+database+".PlayerPurse" +
                        "(id VARCHAR(36) not NULL, " +
                        " username VARCHAR(16), " +
                        " coins DOUBLE(25, 2), " +
                        " sharedBank VARCHAR(36)," +
                        " PRIMARY KEY ( id )) DEFAULT CHARACTER SET=utf8";

                stmt.execute(sql);
                RoyaleEconomy.plugin.getLogger().info("Succesfully connected to the Player Purse MySQL.");

        } catch (Exception e) {
            RoyaleEconomy.plugin.getLogger().info("An error has occured while connecting to the Player Purse MySQL");
            RoyaleEconomy.plugin.getLogger().info(e.getMessage());
        }
    }

    public static int loadSellLimit(String database, ZoneId zone) {

        try(Connection PlayerPurse = HikariCPDataSource.getConnection();
            Statement stmt = PlayerPurse.createStatement();
        ) {

            String sql = "CREATE TABLE IF NOT EXISTS "+database+".RECSellLimit" +
                    "(id VARCHAR(36) not NULL, " +
                    " coins DOUBLE(25, 2), " +
                    " PRIMARY KEY ( id )) DEFAULT CHARACTER SET=utf8";

            stmt.execute(sql);
            RoyaleEconomy.plugin.getLogger().info("Succesfully connected to the SellLimit MySQL.");

        } catch (Exception e) {
            RoyaleEconomy.plugin.getLogger().info("An error has occured while connecting to the SellLimit MySQL");
            RoyaleEconomy.plugin.getLogger().info(e.getMessage());
        }

        try (
                Connection Database = HikariCPDataSource.getConnection();
                ResultSet r = Database.prepareStatement("SELECT coins FROM " + database + ".RECSellLimit WHERE id = 'day'").executeQuery();
                PreparedStatement stmt = Database.prepareStatement("INSERT INTO "+database+".RECSellLimit VALUES(?, ?)");

        ) {
            if (r.next())
                return (int)r.getDouble("coins");
            else{
                double day=ZonedDateTime.now(zone).getDayOfMonth();
                stmt.setString(1, "day");
                stmt.setDouble(2, day);
                stmt.executeUpdate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ZonedDateTime.now(zone).getDayOfMonth();
    }

    private void loadExternalData(String database) {

        try(
                Connection ExternalData = HikariCPDataSource.getConnection();
                Statement stmt = ExternalData.createStatement();
                ) {

                String sql = "CREATE TABLE IF NOT EXISTS "+database+".ExternalGeneratedData" +
                        "(id VARCHAR(36) not NULL, " +
                        " coins DOUBLE(25, 2), " +
                        " PRIMARY KEY ( id )) DEFAULT CHARACTER SET=utf8";

                stmt.execute(sql);
                stmt.close();
                RoyaleEconomy.plugin.getLogger().info("Succesfully connected to the External Generated Data MySQL.");

        } catch (Exception e) {
            RoyaleEconomy.plugin.getLogger().info("An error has occured while connecting to the Player Purse MySQL");
            RoyaleEconomy.plugin.getLogger().info(e.getMessage());
        }
    }

    private void loadPersonalBanks(String database) {

        try(
                Connection PersonalBank = HikariCPDataSource.getConnection();
                Statement stmt = PersonalBank.createStatement();
                ) {

                String sql = "CREATE TABLE IF NOT EXISTS "+database+".PersonalBank" +
                        "(id VARCHAR(36) not NULL, " +
                        " coins DOUBLE(25, 2), " +
                        " upgrade INT(2), "+
                        " transactionlog MEDIUMTEXT,"+
                        " PRIMARY KEY ( id )) DEFAULT CHARACTER SET=utf8";

                stmt.execute(sql);
                RoyaleEconomy.plugin.getLogger().info("Succesfully connected to the Personal Banks MySQL.");

                sql = "CREATE TABLE IF NOT EXISTS "+database+".Interest " +
                        "(id VARCHAR(36) not NULL, " +
                        " server VARCHAR(32) not NULL," +
                        " date BIGINT(15), " +
                        " PRIMARY KEY ( id ))";

                stmt.execute(sql);

                try {

                    Long time = ZonedDateTime.now().toInstant().toEpochMilli();
                    time += RoyaleEconomy.plugin.getConfig().getInt("interest-cooldown") * 3600000;

                    String ip = Bukkit.getIp()+":"+Bukkit.getPort();

                    try {
                        sql = "INSERT INTO "+database+".Interest VALUES ('personalInterest','" + ip + "'," + time + ")";
                        stmt.execute(sql);
                    }catch(Exception x){
                        sql = "UPDATE "+database+".Interest SET server = '" + ip + "'";
                        stmt.execute(sql);
                    }


                }catch (Exception x){
                    stmt.close();
                }
                RoyaleEconomy.plugin.getLogger().info("Succesfully loaded the interest.");

        } catch (Exception e) {
            RoyaleEconomy.plugin.getLogger().info("An error has occured while connecting to the Personal Banks MySQL");
            RoyaleEconomy.plugin.getLogger().info(e.getMessage());
        }
    }

    private void loadSharedBanks(String database) {
        try(
                Connection SharedBank = HikariCPDataSource.getConnection();
                Statement stmt = SharedBank.createStatement();
                ) {

                String sql = "CREATE TABLE IF NOT EXISTS "+database+".SharedBank" +
                        "(id VARCHAR(36) not NULL, " +
                        " owner VARCHAR(36), "+
                        " members MEDIUMTEXT, "+
                        " coins DOUBLE(25, 2), " +
                        " upgrade INT(2), "+
                        " transactionlog MEDIUMTEXT,"+
                        " PRIMARY KEY ( id )) DEFAULT CHARACTER SET=utf8";

                stmt.execute(sql);
                stmt.close();
                RoyaleEconomy.plugin.getLogger().info("Succesfully connected to the Shared Banks MySQL.");

        } catch (Exception e) {
            RoyaleEconomy.plugin.getLogger().info("An error has occured while connecting to the Shared Banks MySQL");
            RoyaleEconomy.plugin.getLogger().info(e.getMessage());
        }
    }

    public static void loadSharedBanksCustom(String custom) {
        try (Connection SharedBank = HikariCPDataSource.getConnection()) {
            if (SharedBank != null) {
                Statement stmt = SharedBank.createStatement();

                String sql = "CREATE TABLE IF NOT EXISTS "+DataManagerMySQL.database+"."+custom+" " +
                        "(id VARCHAR(100) not NULL, " +
                        " coins DOUBLE(20, 2), " +
                        " upgrade INT(2), "+
                        " transactionlog MEDIUMTEXT,"+
                        " PRIMARY KEY ( id ))";

                stmt.execute(sql);
                stmt.close();
                RoyaleEconomy.plugin.getLogger().info("Succesfully connected to the Shared Banks SQL.");
            }

        } catch (SQLException e) {
            RoyaleEconomy.plugin.getLogger().info("An error has occured while connecting to the Shared Banks SQL");
            RoyaleEconomy.plugin.getLogger().info(e.getMessage());
        }
    }
}
