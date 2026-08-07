package me.qKing12.RoyaleEconomy.DataManager.SellLimitGlobal;

import me.qKing12.RoyaleEconomy.DataManager.HikariCPDataSource;
import me.qKing12.RoyaleEconomy.RoyaleEconomy;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static me.qKing12.RoyaleEconomy.RoyaleEconomy.shopsCfg;

public class SellLimitGlobalSQL implements ISellLimitGlobal{
    private ZoneId zoneId;

    {
        try {
            if (shopsCfg.getBoolean("shop-sell-limit.use")) {
                try {
                    zoneId = ZoneId.of(shopsCfg.getString("shop-sell-limit.reset-time-zone"));
                } catch (Exception x) {
                    RoyaleEconomy.plugin.getLogger().warning("Invalid time zone in shops.yml, using default time zone.");
                    zoneId = ZoneId.systemDefault();
                }
            }
            else
                zoneId=ZoneId.systemDefault();
        }catch(Exception x){
            zoneId=ZoneId.systemDefault();
        }
    }

    @Override
    public ZoneId getZoneId() {
        return zoneId;
    }

    @Override
    public int loadSellLimit() {

        try(Connection PlayerPurse = HikariCPDataSource.getConnection();
            Statement stmt = PlayerPurse.createStatement();
        ) {

            String sql = "CREATE TABLE IF NOT EXISTS RECSellLimit" +
                    "(id VARCHAR(36) not NULL, " +
                    " coins DOUBLE(25, 2), " +
                    " PRIMARY KEY ( id ))";

            stmt.execute(sql);
            RoyaleEconomy.plugin.getLogger().info("Succesfully connected to the SellLimit SQL.");

        } catch (Exception e) {
            RoyaleEconomy.plugin.getLogger().info("An error has occured while connecting to the SellLimit SQL");
            RoyaleEconomy.plugin.getLogger().info(e.getMessage());
        }

        try (
                Connection Database = HikariCPDataSource.getConnection();
                ResultSet r = Database.prepareStatement("SELECT coins FROM RECSellLimit WHERE id = 'day'").executeQuery();
                PreparedStatement stmt = Database.prepareStatement("INSERT INTO RECSellLimit VALUES(?, ?)");

        ) {
            if (r.next())
                return (int)r.getDouble("coins");
            else{
                double day= ZonedDateTime.now(zoneId).getDayOfMonth();
                stmt.setString(1, "day");
                stmt.setDouble(2, day);
                stmt.executeUpdate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ZonedDateTime.now(zoneId).getDayOfMonth();
    }

    @Override
    public double getLimit(String playerUUID) {
        double toReturn = 0;
        try (
                Connection Database = HikariCPDataSource.getConnection();
                ResultSet r = Database.prepareStatement("SELECT coins FROM RECSellLimit WHERE id = '" + playerUUID + "'").executeQuery();

        ) {
            if (r.next())
                toReturn = r.getDouble("coins");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return toReturn;
    }

    @Override
    public void setLimit(String playerUUID, double limit) {
        try (
                Connection Database = HikariCPDataSource.getConnection();
                PreparedStatement stmt = Database.prepareStatement("UPDATE RECSellLimit SET coins = ? WHERE id = ?");
                PreparedStatement stmt2 = Database.prepareStatement("INSERT INTO RECSellLimit VALUES(?, ?)");
        ) {
            stmt.setDouble(1, limit);
            stmt.setString(2, playerUUID);
            int c=stmt.executeUpdate();
            if(c==0){
                stmt2.setString(1, playerUUID);
                stmt2.setDouble(2, limit);
                stmt2.executeUpdate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setLimit(List<Object[]> pairPlayerUuidAndLimit) {
        for(Object[] pair : pairPlayerUuidAndLimit)
            setLimit((String)pair[0], (double)pair[1]);
    }

    @Override
    public void resetLimit(String playerUUID) {
        try (Connection Database = HikariCPDataSource.getConnection();
             PreparedStatement stmt = Database.prepareStatement("DELETE FROM RECSellLimit WHERE id = ?");
        ) {
            stmt.setString(1, playerUUID);
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void resetLimit() {
        try (Connection Database = HikariCPDataSource.getConnection();
             PreparedStatement stmt = Database.prepareStatement("DELETE FROM RECSellLimit");
             PreparedStatement stmt2 = Database.prepareStatement("INSERT INTO RECSellLimit VALUES(?, ?)");
        ) {
            stmt.executeUpdate();
            stmt2.setString(1, "day");
            stmt2.setDouble(2, ZonedDateTime.now(zoneId).getDayOfMonth());
            stmt2.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
