package me.qKing12.RoyaleEconomy.DataManager.SharedBank;

import me.qKing12.RoyaleEconomy.API.Events.SharedBankCreateEvent;
import me.qKing12.RoyaleEconomy.API.Events.SharedBankDeleteEvent;
import me.qKing12.RoyaleEconomy.DataManager.HikariCPDataSource;
import me.qKing12.RoyaleEconomy.DataManager.StaticValues;
import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import me.qKing12.RoyaleEconomy.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

import static me.qKing12.RoyaleEconomy.Commands.BalanceTopCommand.*;
import static me.qKing12.RoyaleEconomy.Commands.BalanceTopCommand.sharedBankTop;
import static me.qKing12.RoyaleEconomy.RoyaleEconomy.*;

public class SharedBankSQL implements SharedBank{
    private String sharedBankTable;

    @Override
    public String getTable() {
        return sharedBankTable;
    }

    @Override
    public void setTable(String table) {
        sharedBankTable=table;
    }

    public SharedBankSQL(String sharedBankTable){
        this.sharedBankTable=sharedBankTable;
    }

    public void loadTops() {
        int limit= staticValues.balanceTopMaximumPages*staticValues.balanceTopDisplayPerPage;
        RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runAsync((task) -> {
            purseTop = new ArrayList<>();
            bankTop = new ArrayList<>();
            sharedBankTop = new ArrayList<>();
            HashMap<String, String> uuidsToNames = new HashMap<>();
            boolean hookedUse= hooked!=null;
            String sharedBankSelect="SELECT "+(hookedUse?"id,":"owner,")+"coins FROM "+sharedBankTable+" ORDER BY coins DESC LIMIT "+limit;
            if(noEconomy){
                purseTop=RoyaleEconomy.plugin.balanceTopNoEconomy.getTop();

                try (
                        Connection Database = HikariCPDataSource.getConnection();
                        ResultSet r2 = Database.prepareStatement("SELECT id,coins FROM PersonalBank ORDER BY coins DESC LIMIT "+limit).executeQuery();
                        ResultSet r3 = Database.prepareStatement(sharedBankSelect).executeQuery();

                ) {

                    while (r2.next()) {
                        String uuid = r2.getString(1);
                        String username = uuidsToNames.getOrDefault(uuid, RoyaleEconomy.messageHelper.getPlayerName(uuid));
                        if (!hiddenPlayers.contains(username)) {
                            bankTop.add(RoyaleEconomy.messageHelper.numberFormat(r2.getDouble(2)));
                            bankTop.add(username);
                        }
                    }

                    if (hookedUse)
                        while (r3.next()) {
                            String noBank=r3.getString(1);
                            if(noBank==null || noBank.equals(""))
                                continue;
                            String uuid = RoyaleEconomy.hooked.getOwner(noBank);
                            String username = uuidsToNames.getOrDefault(uuid, RoyaleEconomy.messageHelper.getPlayerName(uuid));
                            if (!hiddenPlayers.contains(username)) {
                                sharedBankTop.add(RoyaleEconomy.messageHelper.numberFormat(r3.getDouble(2)));
                                sharedBankTop.add(username);
                            }
                        }
                    else
                        while (r3.next()) {
                            String uuid = r3.getString(1);
                            String username = uuidsToNames.getOrDefault(uuid, RoyaleEconomy.messageHelper.getPlayerName(uuid));
                            if (!hiddenPlayers.contains(username)) {
                                sharedBankTop.add(RoyaleEconomy.messageHelper.numberFormat(r3.getDouble(2)));
                                sharedBankTop.add(username);
                            }
                        }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else {
                try (
                        Connection Database = HikariCPDataSource.getConnection();
                        ResultSet r1 = Database.prepareStatement("SELECT username,id,coins FROM PlayerPurse ORDER BY coins DESC LIMIT "+limit).executeQuery();
                        ResultSet r2 = Database.prepareStatement("SELECT id,coins FROM PersonalBank ORDER BY coins DESC LIMIT "+limit).executeQuery();
                        ResultSet r3 = Database.prepareStatement(sharedBankSelect).executeQuery();

                ) {
                    while (r1.next()) {
                        String username = r1.getString(1);
                        if(username!=null) {
                            String uuid = r1.getString(2);
                            uuidsToNames.put(uuid, username);
                            if (!hiddenPlayers.contains(username)) {
                                purseTop.add(RoyaleEconomy.messageHelper.numberFormat(r1.getDouble(3)));
                                purseTop.add(username);
                            }
                        }
                    }

                    while (r2.next()) {
                        String uuid = r2.getString(1);
                        String username = uuidsToNames.getOrDefault(uuid, RoyaleEconomy.messageHelper.getPlayerName(uuid));
                        if (!hiddenPlayers.contains(username)) {
                            bankTop.add(RoyaleEconomy.messageHelper.numberFormat(r2.getDouble(2)));
                            bankTop.add(username);
                        }
                    }

                    if (hookedUse)
                        while (r3.next()) {
                            String noBank=r3.getString(1);
                            if(noBank==null || noBank.equals(""))
                                continue;
                            String uuid = RoyaleEconomy.hooked.getOwner(noBank);
                            String username = uuidsToNames.getOrDefault(uuid, RoyaleEconomy.messageHelper.getPlayerName(uuid));
                            if (!hiddenPlayers.contains(username)) {
                                sharedBankTop.add(RoyaleEconomy.messageHelper.numberFormat(r3.getDouble(2)));
                                sharedBankTop.add(username);
                            }
                        }
                    else
                        while (r3.next()) {
                            String uuid = r3.getString(1);
                            String username = uuidsToNames.getOrDefault(uuid, RoyaleEconomy.messageHelper.getPlayerName(uuid));
                            if (!hiddenPlayers.contains(username)) {
                                sharedBankTop.add(RoyaleEconomy.messageHelper.numberFormat(r3.getDouble(2)));
                                sharedBankTop.add(username);
                            }
                        }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public ArrayList<String> getSharedTransactionLogFromFile(String bankID) {
        ArrayList<String> toReturn = new ArrayList<>();
        try (
                Connection Database = HikariCPDataSource.getConnection();
                ResultSet r = Database.prepareStatement("SELECT transactionlog FROM "+sharedBankTable+" WHERE id = '" + bankID + "'").executeQuery();
        ) {
            if (r.next()) {
                if (r.getString("transactionlog").equals("")) {
                    for (String line : RoyaleEconomy.menusCfg.getStringList("shared-menus.main-bank-menu.transaction-history.no-transaction-lore"))
                        toReturn.add(Utils.chat(line));
                } else {
                    String[] temp = r.getString("transactionlog").substring(4).split("%nl%");
                    for (String transaction : temp) {
                        Long date = Long.parseLong(transaction.split("###")[0]);
                        transaction = transaction.split("###")[1];
                        toReturn.add(0, transaction.replace("%transaction-countdown%", RoyaleEconomy.messageHelper.formatTimeNotDetailed(ZonedDateTime.now().toInstant().toEpochMilli() - date)));
                    }
                }
            } else {
                for (String line : RoyaleEconomy.menusCfg.getStringList("shared-menus.main-bank-menu.transaction-history.no-transaction-lore"))
                    toReturn.add(Utils.chat(line));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return toReturn;
    }

    public ArrayList<String> getSharedTransactionLogPlayer(String playerUUID) {
        ArrayList<String> toReturn = new ArrayList<>();
        try (
                Connection Database = HikariCPDataSource.getConnection();
                ResultSet r = Database.prepareStatement("SELECT transactionlog FROM "+sharedBankTable+" WHERE owner = '" + playerUUID + "' OR members LIKE '%"+playerUUID+"%'").executeQuery();
        ) {
            if (r.next()) {
                if (r.getString("transactionlog").equals("")) {
                    for (String line : RoyaleEconomy.menusCfg.getStringList("shared-menus.main-bank-menu.transaction-history.no-transaction-lore"))
                        toReturn.add(Utils.chat(line));
                } else {
                    String[] temp = r.getString("transactionlog").substring(4).split("%nl%");
                    for (String transaction : temp) {
                        Long date = Long.parseLong(transaction.split("###")[0]);
                        transaction = transaction.split("###")[1];
                        toReturn.add(0, transaction.replace("%transaction-countdown%", RoyaleEconomy.messageHelper.formatTimeNotDetailed(ZonedDateTime.now().toInstant().toEpochMilli() - date)));
                    }
                }
            } else {
                for (String line : RoyaleEconomy.menusCfg.getStringList("shared-menus.main-bank-menu.transaction-history.no-transaction-lore"))
                    toReturn.add(Utils.chat(line));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return toReturn;
    }

    public void addSharedTransactionLog(String bankID, String byWho, String symbol, double amount) {
        //RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runAsync((task) -> {
        String toInsert = RoyaleEconomy.menusCfg.getString("shared-menus.main-bank-menu.transaction-history.lore-structure");
        toInsert = Utils.chat("%nl%" + ZonedDateTime.now().toInstant().toEpochMilli() + "###" + toInsert
                .replace("%symbol%", symbol)
                .replace("%transaction-coins%", RoyaleEconomy.messageHelper.numberFormat(amount))
                .replace("%player-name%", byWho)
        );

        try (
                Connection Database = HikariCPDataSource.getConnection();
                PreparedStatement stmt = Database.prepareStatement("UPDATE "+sharedBankTable+" SET transactionlog = ? WHERE id = ?");
                ResultSet r = Database.prepareStatement("SELECT transactionlog FROM "+sharedBankTable+" WHERE id = '" + bankID + "'").executeQuery();
        ) {
            String transactionLog = "";
            if (r.next()) {
                transactionLog = r.getString("transactionlog");
            }

            if (transactionLog.split("%nl%").length == 11)
                transactionLog = transactionLog.substring(4 + transactionLog.split("%nl%")[1].length());

            stmt.setString(1, transactionLog+toInsert);
            stmt.setString(2, bankID);
            stmt.executeUpdate();
        } catch (Exception e) {
            if(e.getMessage()!=null && e.getMessage().startsWith("[SQLITE_BUSY]")) {
                RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runLaterAsync(() -> addSharedTransactionLog(bankID, byWho, symbol, amount), 7);
            }
            else
                e.printStackTrace();
        }
        //});
    }

    public void addSharedTransactionLogPlayer(String playerUUID, String byWho, String symbol, double amount) {
        //RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runAsync((task) -> {
        String toInsert = RoyaleEconomy.menusCfg.getString("shared-menus.main-bank-menu.transaction-history.lore-structure");
        toInsert = Utils.chat("%nl%" + ZonedDateTime.now().toInstant().toEpochMilli() + "###" + toInsert
                .replace("%symbol%", symbol)
                .replace("%transaction-coins%", RoyaleEconomy.messageHelper.numberFormat(amount))
                .replace("%player-name%", byWho)
        );

        try (
                Connection Database = HikariCPDataSource.getConnection();
                PreparedStatement stmt = Database.prepareStatement("UPDATE "+sharedBankTable+" SET transactionlog = ? WHERE id = ?");
                ResultSet r = Database.prepareStatement("SELECT transactionlog,id FROM "+sharedBankTable+" WHERE owner = '" + playerUUID + "' OR members LIKE '%"+playerUUID+"%'").executeQuery();
        ) {
            String transactionLog = "";
            if (r.next()) {
                transactionLog = r.getString("transactionlog");
            }

            if (transactionLog.split("%nl%").length == 11)
                transactionLog = transactionLog.substring(4 + transactionLog.split("%nl%")[1].length());

            stmt.setString(1, transactionLog+toInsert);
            stmt.setString(2, r.getString(2));
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setSharedBankMoney(String bankID, Double amount) {
        //RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runAsync((task) -> {
        try (
                Connection Database = HikariCPDataSource.getConnection();
                PreparedStatement stmt = Database.prepareStatement("UPDATE "+sharedBankTable+" SET coins = ? WHERE id = ?");
        ) {
            stmt.setDouble(1, amount);
            stmt.setString(2, bankID);
            stmt.executeUpdate();

        } catch (Exception e) {
            if(e.getMessage()!=null && e.getMessage().startsWith("[SQLITE_BUSY]")) {
                RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runLaterAsync(() -> setSharedBankMoney(bankID, amount), 7);
            }
            else
                e.printStackTrace();
        }
        //});
    }

    public void setSharedBankMoneyPlayer(String playerUUID, Double amount) {
        //RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runAsync((task) -> {
        try (
                Connection Database = HikariCPDataSource.getConnection();
                PreparedStatement stmt = Database.prepareStatement("UPDATE "+sharedBankTable+" SET coins = ? WHERE owner = ? OR members LIKE ?");
        ) {
            stmt.setDouble(1, amount);
            stmt.setString(2, playerUUID);
            stmt.setString(3, "%"+playerUUID+"%");
            stmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
        //});
    }

    public void addSharedBankMoneyToFile(String bankID, Double amount) {
        //RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runAsync((task) -> {
        try (
                Connection Database = HikariCPDataSource.getConnection();
                PreparedStatement stmt = Database.prepareStatement("UPDATE "+sharedBankTable+" SET coins = coins +? WHERE id = ?");
        ) {
            stmt.setDouble(1, amount);
            stmt.setString(2, bankID);
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            if(e.getMessage()!=null && e.getMessage().startsWith("[SQLITE_BUSY]")) {
                RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runLaterAsync(() -> addSharedBankMoneyToFile(bankID, amount), 7);
            }
            else
                e.printStackTrace();
        }
        //});
    }

    public void addSharedBankMoneyPlayer(String playerUUID, Double amount) {
        if(hooked!=null) {
            addSharedBankMoneyToFile(hooked.getSharedBankId(playerUUID), amount);
            return;
        }
        //RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runAsync((task) -> {
        try (
                Connection Database = HikariCPDataSource.getConnection();
                PreparedStatement stmt = Database.prepareStatement("UPDATE "+sharedBankTable+" SET coins = coins + ? WHERE owner = ? OR members LIKE ?");
        ) {
            stmt.setDouble(1, amount);
            stmt.setString(2, playerUUID);
            stmt.setString(3, "%"+playerUUID+"%");
            stmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
        //});
    }

    public boolean removeSharedBankMoneyToFile(String bankID, Double amount) {
        boolean toReturn=false;
        try (
                Connection Database = HikariCPDataSource.getConnection();
                PreparedStatement stmt = Database.prepareStatement("UPDATE "+sharedBankTable+" SET coins = coins - ? WHERE id = ? AND coins >= ?");
        ) {
            stmt.setDouble(1, amount);
            stmt.setString(2, bankID);
            stmt.setDouble(3, amount);
            int c=stmt.executeUpdate();
            if(c!=0)
                toReturn=true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return toReturn;
    }

    public void removeSharedBankMoneyPlayer(String playerUUID, Double amount) {
        if(hooked!=null) {
            removeSharedBankMoneyToFile(hooked.getSharedBankId(playerUUID), amount);
            return;
        }
        //RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runAsync((task) -> {
        try (
                Connection Database = HikariCPDataSource.getConnection();
                PreparedStatement stmt = Database.prepareStatement("UPDATE "+sharedBankTable+" SET coins = coins - ? WHERE owner = ? OR members LIKE ?");
        ) {
            stmt.setDouble(1, amount);
            stmt.setString(2, playerUUID);
            stmt.setString(3, "%"+playerUUID+"%");
            stmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
        //});
    }

    public Double getSharedBankMoneyFromFile(String bankID) {
        Double toReturn = null;

        try (Connection Database = HikariCPDataSource.getConnection();
             ResultSet r=Database.prepareStatement("SELECT coins FROM "+sharedBankTable+" WHERE id = '" + bankID + "'").executeQuery();
        ) {
            if (r.next())
                toReturn = r.getDouble("coins");
            else if(hooked!=null){
                createSharedBank(bankID);
                return (double) RoyaleEconomy.staticValues.defaultBankCoins;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return toReturn;
    }

    public Double getSharedBankMoneyPlayer(String playerUUID) {
        if(hooked!=null)
            return getSharedBankMoneyFromFile(hooked.getSharedBankId(playerUUID));
        Double toReturn = null;

        try (Connection Database = HikariCPDataSource.getConnection();
             ResultSet r=Database.prepareStatement("SELECT coins FROM "+sharedBankTable+" WHERE owner = '" + playerUUID + "' OR members LIKE '%"+playerUUID+"%'").executeQuery();
        ) {
            if (r.next())
                toReturn = r.getDouble("coins");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return toReturn;
    }

    public boolean createSharedBank(Player p) {
        if(RoyaleEconomy.hooked!=null)
            return false;
        boolean success = false;
        try (Connection Database = HikariCPDataSource.getConnection();
             ResultSet r = Database.prepareStatement("SELECT sharedBank FROM PlayerPurse WHERE id = '" + p.getUniqueId() + "'").executeQuery();
             PreparedStatement stmt1 = Database.prepareStatement("INSERT INTO "+sharedBankTable+" VALUES (?, ?, '' ,? ,0, '')");
             PreparedStatement stmt2 = Database.prepareStatement("UPDATE PlayerPurse SET sharedBank = ? WHERE id = ? AND sharedBank = ''");
        ) {
            if (r.next()) {
                if (r.getString("sharedBank").equals("")) {
                    success = true;
                    String newBank = UUID.randomUUID().toString();
                    SharedBankCreateEvent event = new SharedBankCreateEvent(p, newBank);
                    Bukkit.getPluginManager().callEvent(event);
                    if (!event.isCancelled()) {
                        stmt1.setString(1, newBank);
                        stmt1.setString(2, p.getUniqueId().toString());
                        stmt1.setDouble(3, RoyaleEconomy.staticValues.defaultBankCoins);
                        stmt1.executeUpdate();

                        stmt2.setString(1, newBank);
                        stmt2.setString(2, p.getUniqueId().toString());
                        stmt2.executeUpdate();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return success;
    }

    public boolean createSharedBank(String bankID) {
        boolean success = false;
        try (Connection Database = HikariCPDataSource.getConnection();
             PreparedStatement stmt1 = Database.prepareStatement("INSERT OR IGNORE INTO "+sharedBankTable+" VALUES (?, ? ,0, '')");
        ) {
            stmt1.setString(1, bankID);
            stmt1.setDouble(2, RoyaleEconomy.staticValues.defaultBankCoins);
            int c = stmt1.executeUpdate();
            if(c!=0)
                success=true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return success;
    }

    public boolean deleteSharedBank(Player p) {
        if(RoyaleEconomy.hooked!=null)
            return false;
        boolean success = false;
        try (
                Connection Database = HikariCPDataSource.getConnection();
                ResultSet r = Database.prepareStatement("SELECT sharedBank FROM PlayerPurse WHERE id = '" + p.getUniqueId() + "'").executeQuery();
                PreparedStatement stmt1 = Database.prepareStatement("DELETE FROM "+sharedBankTable+" WHERE id=?");
                PreparedStatement stmt2 = Database.prepareStatement("UPDATE PlayerPurse SET sharedBank = '' WHERE sharedBank = ?");
        ) {
            if (r.next()) {
                String bank = r.getString("sharedBank");
                if (!bank.equals("")) {
                    success = true;
                    SharedBankDeleteEvent event = new SharedBankDeleteEvent(p, bank);
                    Bukkit.getPluginManager().callEvent(event);
                    if (!event.isCancelled()) {
                        stmt1.setString(1, bank);
                        stmt1.executeUpdate();

                        stmt2.setString(1, bank);
                        stmt2.executeUpdate();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return success;
    }

    public void deleteSharedBank(String bank) {
        //RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runAsync((task) -> {
        try (Connection Database = HikariCPDataSource.getConnection();
             PreparedStatement stmt1 = Database.prepareStatement("DELETE FROM "+sharedBankTable+" WHERE id=?");
             PreparedStatement stmt2 = Database.prepareStatement("UPDATE PlayerPurse SET sharedBank = '' WHERE sharedBank = ?");
        ) {
            if (!bank.equals("")) {
                stmt1.setString(1, bank);
                stmt1.executeUpdate();

                if(RoyaleEconomy.hooked==null) {
                    stmt2.setString(1, bank);
                    stmt2.executeUpdate();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        //});
    }

    public String getSharedBankId(String player) {
        if(RoyaleEconomy.hooked!=null)
            if(player.length()>16)
                return RoyaleEconomy.hooked.getSharedBankId(player);
            else{
                return RoyaleEconomy.hooked.getSharedBankId(RoyaleEconomy.dataManager.getUUIDfromName(player));
            }
        String toReturn = null;

        try (
                Connection Database = HikariCPDataSource.getConnection();
                ResultSet r = Database.prepareStatement(player.length()>16?"SELECT sharedBank FROM PlayerPurse WHERE id = '" + player + "'":"SELECT sharedBank FROM PlayerPurse WHERE upper(username) = '" + player.toUpperCase() + "'").executeQuery()
        ) {
            if (r.next())
                toReturn = r.getString("sharedBank");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return toReturn;
    }

    public boolean isOwnerSharedBank(Player p) {
        if(RoyaleEconomy.hooked!=null)
            return false;
        boolean isOwner = false;
        try (
                Connection Database = HikariCPDataSource.getConnection();
                ResultSet r = Database.prepareStatement("SELECT owner FROM "+sharedBankTable+" WHERE owner = '" + p.getUniqueId() + "' LIMIT 1").executeQuery();
        ) {
            if (r.next()) {
                isOwner = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return isOwner;
    }

    public boolean transferOwnershipSharedBank(String oldOwner, String newOwner) {
        if(RoyaleEconomy.hooked!=null)
            return false;
        boolean success = false;
        try (Connection Database = HikariCPDataSource.getConnection();
             PreparedStatement stmt = Database.prepareStatement("UPDATE "+sharedBankTable+" SET owner=?, members = REPLACE(members, ?, ?) WHERE owner = ? AND members LIKE ?");) {

            stmt.setString(1, newOwner);
            stmt.setString(2, newOwner);
            stmt.setString(3, oldOwner);
            stmt.setString(4, oldOwner);
            stmt.setString(5, "%"+newOwner+"%");
            int c=stmt.executeUpdate();
            if(c!=0)
                success=true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return success;
    }

    public boolean transferOwnershipSharedBank(String newOwner) {
        if(RoyaleEconomy.hooked!=null)
            return false;
        boolean success = false;
        String bankID = getSharedBankId(newOwner);
        ArrayList<String> members = getMembersSharedBank(bankID, true);
        //String oldOwner = members.get(0);
        try (Connection Database = HikariCPDataSource.getConnection();
             PreparedStatement stmt = Database.prepareStatement("UPDATE "+sharedBankTable+" SET owner=?, members = ? WHERE id = ?");) {
            members.remove(newOwner);
            success = true;
            String finalMembers = "";
            for (String member : members)
                finalMembers = finalMembers.concat("." + member);
            finalMembers = finalMembers.substring(1);
            stmt.setString(1, newOwner);
            stmt.setString(2, finalMembers);
            stmt.setString(3, bankID);
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return success;
    }

    public ArrayList<String> getMembersSharedBank(String bankID, boolean owner) {
        if(RoyaleEconomy.hooked!=null)
            return RoyaleEconomy.hooked.getMembers(bankID, owner);
        ArrayList<String> members = new ArrayList<>();
        try (
                Connection Database = HikariCPDataSource.getConnection();
                ResultSet r = Database.prepareStatement(owner?"SELECT owner,members FROM "+sharedBankTable+" WHERE id = '" + bankID + "'":"SELECT members FROM "+sharedBankTable+" WHERE id = '" + bankID + "'").executeQuery();
        ) {
            if (r.next()) {
                if (owner)
                    members.add(r.getString("owner"));
                String membersString = r.getString("members");
                if (!membersString.equals(""))
                    members.addAll(Arrays.asList(membersString.split("\\.")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return members;
    }

    public ArrayList<String> getMembersSharedBankPlayer(String playerUUID, boolean owner) {
        if(RoyaleEconomy.hooked!=null){
            String bankID=RoyaleEconomy.hooked.getSharedBankId(playerUUID);
            return RoyaleEconomy.hooked.getMembers(bankID, owner);
        }
        ArrayList<String> members = new ArrayList<>();
        try (
                Connection Database = HikariCPDataSource.getConnection();
                ResultSet r = Database.prepareStatement(owner?"SELECT owner,members FROM "+sharedBankTable+" WHERE owner = '" + playerUUID + "' OR members LIKE '%"+playerUUID+"%'":"SELECT members FROM "+sharedBankTable+" WHERE owner = '" + playerUUID + "' OR members LIKE '%"+playerUUID+"%'").executeQuery();
        ) {
            if (r.next()) {
                if (owner)
                    members.add(r.getString("owner"));
                String membersString = r.getString("members");
                if (!membersString.equals(""))
                    members.addAll(Arrays.asList(membersString.split("\\.")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return members;
    }

    @Override
    public String getOwnerByPlayerId(String playerUUID) {
        if(RoyaleEconomy.hooked!=null)
            return RoyaleEconomy.hooked.getOwner(RoyaleEconomy.hooked.getSharedBankId(playerUUID));
        String owner = null;
        try (
                Connection Database = HikariCPDataSource.getConnection();
                ResultSet r = Database.prepareStatement("SELECT owner FROM "+sharedBankTable+" WHERE owner = '" + playerUUID + "' OR members LIKE '%"+playerUUID+"%'").executeQuery();
        ) {
            if (r.next()) {
                owner = r.getString("owner");
            }
        } catch (Exception e) {
            return null;
        }

        return owner;
    }

    public boolean addPlayerToSharedBank(Player p, String bankID) {
        if(RoyaleEconomy.hooked!=null)
            return false;
        boolean added = false;
        int upgrade;
        ArrayList<String> members = new ArrayList<>();
        try (
                Connection Database = HikariCPDataSource.getConnection();
                ResultSet r = Database.prepareStatement("SELECT members,upgrade FROM "+sharedBankTable+" WHERE id = '" + bankID + "'").executeQuery();
                PreparedStatement stmt1 = Database.prepareStatement("UPDATE "+sharedBankTable+" SET members = ? WHERE id = ?");
                PreparedStatement stmt2 = Database.prepareStatement("UPDATE PlayerPurse SET sharedBank = ? WHERE id = ?");
        ) {
            if (r.next()) {
                upgrade = r.getInt("upgrade");
                String membersString = r.getString("members");
                if (!membersString.equals(""))
                    members.addAll(Arrays.asList(membersString.split("\\.")));
                if (members.size() < bankUpgradesCfg.getInt("shared-bank-upgrades." + upgrade + ".player-limit")) {
                    if (!members.contains(p.getUniqueId().toString())) {
                        added = true;
                        members.add(p.getUniqueId().toString());
                        String finalMembers = "";
                        for (String member : members)
                            finalMembers = finalMembers.concat("." + member);
                        finalMembers = finalMembers.substring(1);
                        stmt1.setString(1, finalMembers);
                        stmt1.setString(2, bankID);
                        stmt1.executeUpdate();

                        stmt2.setString(1, bankID);
                        stmt2.setString(2, p.getUniqueId().toString());
                        stmt2.executeUpdate();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return added;
    }

    public boolean addPlayerToSharedBank(String pUUID, String bankID) {
        if(RoyaleEconomy.hooked!=null)
            return false;
        boolean added = false;
        int upgrade;
        ArrayList<String> members = new ArrayList<>();
        try (
                Connection Database = HikariCPDataSource.getConnection();
                ResultSet r = Database.prepareStatement("SELECT members,upgrade FROM "+sharedBankTable+" WHERE id = '" + bankID + "'").executeQuery();
                PreparedStatement stmt1 = Database.prepareStatement("UPDATE "+sharedBankTable+" SET members = ? WHERE id = ?");
                PreparedStatement stmt2 = Database.prepareStatement("UPDATE PlayerPurse SET sharedBank = ? WHERE id = ?");
        ) {
            if (r.next()) {
                upgrade = r.getInt("upgrade");
                String membersString = r.getString("members");
                if (!membersString.equals(""))
                    members.addAll(Arrays.asList(membersString.split("\\.")));
                if (members.size() < bankUpgradesCfg.getInt("shared-bank-upgrades." + upgrade + ".player-limit")) {
                    if (!members.contains(pUUID)) {
                        added = true;
                        members.add(pUUID);
                        String finalMembers = "";
                        for (String member : members)
                            finalMembers = finalMembers.concat("." + member);
                        finalMembers = finalMembers.substring(1);
                        stmt1.setString(1, finalMembers);
                        stmt1.setString(2, bankID);
                        stmt1.executeUpdate();

                        stmt2.setString(1, bankID);
                        stmt2.setString(2, pUUID);
                        stmt2.executeUpdate();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return added;
    }


    public boolean removePlayerFromSharedBank(String player, String bankID) {
        if(RoyaleEconomy.hooked!=null)
            return false;
        boolean removed = false;
        ArrayList<String> members;
        if (player.length() < 17)
            player = RoyaleEconomy.dataManager.getUUIDfromName(player);
        final String finalPlayer = player;
        members = getMembersSharedBank(bankID, false);
        try (
                Connection Database = HikariCPDataSource.getConnection();
                PreparedStatement stmt1 = Database.prepareStatement("UPDATE "+sharedBankTable+" SET members = ? WHERE id = ?");
                PreparedStatement stmt2 = Database.prepareStatement("UPDATE PlayerPurse SET sharedBank = '' WHERE id = ?");
        ) {
            if (members.contains(player)) {
                removed = true;
                members.remove(player);
                String finalMembers = "";
                for (String member : members)
                    finalMembers = finalMembers.concat("." + member);
                if (!members.isEmpty())
                    finalMembers = finalMembers.substring(1);
                stmt1.setString(1, finalMembers);
                stmt1.setString(2, bankID);
                stmt1.executeUpdate();

                stmt2.setString(1, player);
                stmt2.executeUpdate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return removed;
    }

    public int getSharedBankUpgrade(String bankID) {
        int upgrade = 0;
        try (
                Connection Database = HikariCPDataSource.getConnection();
                ResultSet r = Database.prepareStatement("SELECT upgrade FROM "+sharedBankTable+" WHERE id = '" + bankID + "'").executeQuery();
        ) {
            if (r.next()) {
                upgrade = r.getInt("upgrade");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return upgrade;
    }

    public int getSharedBankUpgradePlayer(String playerUUID) {
        if(hooked!=null) {
            return getSharedBankUpgrade(getSharedBankId(playerUUID));
        }
        int upgrade = 0;
        try (
                Connection Database = HikariCPDataSource.getConnection();
                ResultSet r = Database.prepareStatement("SELECT upgrade FROM "+sharedBankTable+" WHERE owner = '" + playerUUID + "' OR members LIKE '%"+playerUUID+"%'").executeQuery();
        ) {
            if (r.next()) {
                upgrade = r.getInt("upgrade");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return upgrade;
    }

    public void setSharedBankUpgrade(String bankID, int bankUpgrade) {
        //RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runAsync((task) -> {
        try (
                Connection Database = HikariCPDataSource.getConnection();
                PreparedStatement stmt = Database.prepareStatement("UPDATE "+sharedBankTable+" SET upgrade = ? WHERE id = ?");
        ) {
            stmt.setInt(1, bankUpgrade);
            stmt.setString(2, bankID);
            stmt.executeUpdate();

        } catch (Exception e) {
            if(e.getMessage()!=null && e.getMessage().startsWith("[SQLITE_BUSY]")) {
                RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runLaterAsync(() -> setSharedBankUpgrade(bankID, bankUpgrade), 7);
            }
            else
                e.printStackTrace();
        }
        //});
    }

    public void setSharedBankUpgradePlayer(String playerUUID, int bankUpgrade) {
        if(hooked!=null) {
            setSharedBankUpgrade(getSharedBankId(playerUUID), bankUpgrade);
            return;
        }
        //RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runAsync((task) -> {
        try (
                Connection Database = HikariCPDataSource.getConnection();
                PreparedStatement stmt = Database.prepareStatement("UPDATE "+sharedBankTable+" SET upgrade = ? WHERE owner = ? OR members LIKE ?");
        ) {
            stmt.setInt(1, bankUpgrade);
            stmt.setString(2, playerUUID);
            stmt.setString(3, "%"+playerUUID+"%");
            stmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
        //});
    }


}
