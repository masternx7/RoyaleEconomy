package me.qKing12.RoyaleEconomy.DataManager;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.qKing12.RoyaleEconomy.RoyaleEconomy;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class HikariCPDataSource {

    private static HikariConfig config;
    private static HikariDataSource ds;

    static void loadSettings(String url, String user, String password, boolean MySql) {
        config=new HikariConfig();
        config.setJdbcUrl(url);
        if(MySql) {
            try{
                Class.forName("com.mysql.cj.jdbc.Driver");
                config.setDriverClassName("com.mysql.cj.jdbc.Driver");
            }catch(Exception x){
                RoyaleEconomy.plugin.getLogger().info("Tried to load com.mysql.cj.jdbc.Driver driver but failed. Will try using something else.");
            }
            config.setPoolName("RoyaleEconomyMySQLPool");
            //config.setDriverClassName("com.mysql.jdbc.Driver");
            config.setUsername(user);
            config.setPassword(password);
            //config.addDataSourceProperty("cachePrepStmts", "true");
            //config.addDataSourceProperty("prepStmtCacheSize", "250");
            //config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.setMaxLifetime(60000); // 60 Sec
            config.setMinimumIdle(10);
            config.setIdleTimeout(45000); // 45 Sec
            config.setMaximumPoolSize(50);
            config.setConnectionTestQuery("SELECT 1");
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.addDataSourceProperty("useServerPrepStmts", "true");
            config.addDataSourceProperty("useLocalSessionState", "true");
            config.addDataSourceProperty("rewriteBatchedStatements", "true");
            config.addDataSourceProperty("cacheResultSetMetadata", "true");
            config.addDataSourceProperty("cacheServerConfiguration", "true");
            config.addDataSourceProperty("elideSetAutoCommits", "true");
            config.addDataSourceProperty("maintainTimeStats", "false");
            config.addDataSourceProperty("alwaysSendSetIsolation", "false");
            config.addDataSourceProperty("cacheCallableStmts", "true");
        }
        else{
            try{
                Class.forName("org.sqlite.JDBC");
            }catch(Exception x){
                x.printStackTrace();
            }
            config.setPoolName("RoyaleEconomySQLitePool");
            config.setDriverClassName("org.sqlite.JDBC");
            config.setMaxLifetime(60000); // 60 Sec
            config.setMinimumIdle(10);
            config.setIdleTimeout(45000); // 45 Sec
            config.setDriverClassName("org.sqlite.JDBC");
            config.setConnectionTestQuery("SELECT 1");
            config.setMaximumPoolSize(50);
        }
        ds = new HikariDataSource(config);
    }

    public static Connection getConnection() throws SQLException {
        return ds.getConnection();
    }

    private HikariCPDataSource(){}
}
