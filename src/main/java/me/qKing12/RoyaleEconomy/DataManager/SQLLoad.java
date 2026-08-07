package me.qKing12.RoyaleEconomy.DataManager;

import me.qKing12.RoyaleEconomy.RoyaleEconomy;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.ZonedDateTime;



public class SQLLoad {

    public SQLLoad(){
        loadPlayerPurse();
        loadPersonalBanks();
        loadSharedBanks();
        loadExternalData();
    }

     private void loadPlayerPurse() {
         File database = new File(RoyaleEconomy.plugin.getDataFolder(), "database/royaleEconomyData.db");
         try {
             Class.forName("org.sqlite.JDBC");
         } catch (Exception x) {
             x.printStackTrace();
         }
         String url = "jdbc:sqlite:" + database.getPath();

         try (Connection PlayerPurse = DriverManager.getConnection(url)) {
             if (PlayerPurse != null) {
                 Statement stmt = PlayerPurse.createStatement();

                 String sql = "CREATE TABLE IF NOT EXISTS PlayerPurse " +
                         "(id VARCHAR(36) not NULL, " +
                         " username VARCHAR(16), " +
                         " coins DOUBLE(20, 2), " +
                         " sharedBank VARCHAR(36)," +
                         " PRIMARY KEY ( id ))";

                 stmt.execute(sql);
                 stmt.close();
                 RoyaleEconomy.plugin.getLogger().info("Succesfully connected to the Player Purse SQL.");
             }

         } catch (SQLException e) {
             RoyaleEconomy.plugin.getLogger().info("An error has occured while connecting to the Player Purse SQL");
             RoyaleEconomy.plugin.getLogger().info(e.getMessage());
         }
     }

    private void loadExternalData() {
        File database = new File(RoyaleEconomy.plugin.getDataFolder(), "database/royaleEconomyData.db");
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (Exception x) {
            x.printStackTrace();
        }
        String url = "jdbc:sqlite:" + database.getPath();

        try (Connection PlayerPurse = DriverManager.getConnection(url)) {
            if (PlayerPurse != null) {
                Statement stmt = PlayerPurse.createStatement();

                String sql = "CREATE TABLE IF NOT EXISTS ExternalGeneratedData " +
                        "(id VARCHAR(36) not NULL, " +
                        " coins DOUBLE(20, 2), " +
                        " PRIMARY KEY ( id ))";

                stmt.execute(sql);
                stmt.close();
                RoyaleEconomy.plugin.getLogger().info("Succesfully connected to the External Generated Data SQL.");
            }

        } catch (SQLException e) {
            RoyaleEconomy.plugin.getLogger().info("An error has occured while connecting to the Player Purse SQL");
            RoyaleEconomy.plugin.getLogger().info(e.getMessage());
        }
    }

    private void loadPersonalBanks() {
        File database = new File(RoyaleEconomy.plugin.getDataFolder(), "database/royaleEconomyData.db");
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (Exception x) {
            x.printStackTrace();
        }
        String url = "jdbc:sqlite:" + database.getPath();

        try (Connection PersonalBank = DriverManager.getConnection(url)) {
            if (PersonalBank != null) {
                Statement stmt = PersonalBank.createStatement();

                String sql = "CREATE TABLE IF NOT EXISTS PersonalBank " +
                        "(id VARCHAR(36) not NULL, " +
                        " coins DOUBLE(20, 2), " +
                        " upgrade INT(2), "+
                        " transactionlog MEDIUMTEXT,"+
                        " PRIMARY KEY ( id ))";

                stmt.execute(sql);
                stmt.close();
                RoyaleEconomy.plugin.getLogger().info("Succesfully connected to the Personal Banks SQL.");

                stmt = PersonalBank.createStatement();

                sql = "CREATE TABLE IF NOT EXISTS Interest " +
                        "(id VARCHAR(36) not NULL, " +
                        " date BIGINT(15), " +
                        " PRIMARY KEY ( id ))";

                stmt.execute(sql);
                stmt.close();

                try {
                    stmt = PersonalBank.createStatement();

                    Long time = ZonedDateTime.now().toInstant().toEpochMilli();
                    time += RoyaleEconomy.plugin.getConfig().getInt("interest-cooldown") * 3600000;

                    sql = "INSERT INTO Interest VALUES ('personalInterest'," + time + ")";

                    stmt.execute(sql);
                    stmt.close();
                }catch (Exception x){
                    stmt.close();
                }
                RoyaleEconomy.plugin.getLogger().info("Succesfully loaded the interest.");
            }

        } catch (SQLException e) {
            RoyaleEconomy.plugin.getLogger().info("An error has occured while connecting to the Personal Banks SQL");
            RoyaleEconomy.plugin.getLogger().info(e.getMessage());
        }
    }

    private void loadSharedBanks() {
        File database = new File(RoyaleEconomy.plugin.getDataFolder(), "database/royaleEconomyData.db");
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (Exception x) {
            x.printStackTrace();
        }
        String url = "jdbc:sqlite:" + database.getPath();

        try (Connection SharedBank = DriverManager.getConnection(url)) {
            if (SharedBank != null) {
                Statement stmt = SharedBank.createStatement();

                String sql = "CREATE TABLE IF NOT EXISTS SharedBank " +
                        "(id VARCHAR(36) not NULL, " +
                        " owner VARCHAR(36), "+
                        " members MEDIUMTEXT, "+
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

    public static void loadSharedBanks(String custom) {
        File database = new File(RoyaleEconomy.plugin.getDataFolder(), "database/royaleEconomyData.db");
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (Exception x) {
            x.printStackTrace();
        }
        String url = "jdbc:sqlite:" + database.getPath();

        try (Connection SharedBank = DriverManager.getConnection(url)) {
            if (SharedBank != null) {
                Statement stmt = SharedBank.createStatement();

                String sql = "CREATE TABLE IF NOT EXISTS "+custom+" " +
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
