package me.qKing12.RoyaleEconomy.Economy;

import me.qKing12.RoyaleEconomy.DataManager.DataManagerSQL;
import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import net.milkbowl.vault.economy.AbstractEconomy;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;

import java.util.ArrayList;
import java.util.List;


public class VaultHook extends AbstractEconomy implements Economy {

    public static ArrayList<String> banks=new ArrayList<>();

    public boolean createPlayerAccount(final String name) {
        return RoyaleEconomy.dataManager.createAccount(name);
    }

    public boolean createPlayerAccount(OfflinePlayer player, String worldName){
        return createPlayerAccount(player.getUniqueId().toString());
    }

    public boolean createPlayerAccount(final String name, final String arg1) {
        return this.createPlayerAccount(name);
    }

    public String currencyNamePlural() {
        return "";
    }

    public String currencyNameSingular() {
        return "";
    }

    public EconomyResponse depositPlayer(final String name, final double amount) {
        try{
            RoyaleEconomy.dataManager.addMoneyToFile(name, amount);
            return new EconomyResponse(amount, this.getBalance(name), EconomyResponse.ResponseType.SUCCESS, "");
        }catch(Exception x){
            return new EconomyResponse(0d, 0d, EconomyResponse.ResponseType.FAILURE, "Failed to deposit money to "+name);
        }
    }

    @Override
    public EconomyResponse depositPlayer(final OfflinePlayer player, final double amount) {
        try{
            RoyaleEconomy.dataManager.addMoneyToFile(player.getUniqueId().toString(), amount);
            return new EconomyResponse(amount, this.getBalance(player.getUniqueId().toString()), EconomyResponse.ResponseType.SUCCESS, "");
        }catch(Exception x){
            return new EconomyResponse(0d, 0d, EconomyResponse.ResponseType.FAILURE, "Failed to deposit money to "+player.getName());
        }
    }

    public EconomyResponse depositPlayer(final String name, final String arg1, final double amount) {
        return this.depositPlayer(name, amount);
    }

    @Override
    public EconomyResponse depositPlayer(final OfflinePlayer player, final String arg1, final double amount) {
        return this.depositPlayer(player.getUniqueId().toString(), amount);
    }

    public String format(final double summ) {
        return RoyaleEconomy.messageHelper.numberFormat(summ);
    }

    public int fractionalDigits() {
        return -1;
    }

    public double getBalance(final String name) {
        try {
            Double balance = RoyaleEconomy.dataManager.getMoneyFromFile(name);
            if (balance == null)
                return 0d;
            else {
                return Math.floor(balance * 100) / 100;
            }
        }catch(NullPointerException x){
            return 0d;
        }
    }

    @Override
    public double getBalance(final OfflinePlayer player) {
        try {
            Double balance = RoyaleEconomy.dataManager.getMoneyFromFile(player.getUniqueId().toString());
            if (balance == null)
                return 0d;
            else {
                return Math.floor(balance * 100) / 100;
            }
        }catch(NullPointerException x){
            return 0d;
        }
    }

    public double getBalance(final String name, final String arg1) {
        return getBalance(name);
    }

    @Override
    public double getBalance(final OfflinePlayer player, final String arg1) {
        return getBalance(player.getUniqueId().toString());
    }

    public List<String> getBanks() {
        if(RoyaleEconomy.dataManager instanceof DataManagerSQL)
            return banks;
        else
            return RoyaleEconomy.dataManager.getIdsFromExternal();
    }

    public String getName() {
        return "RoyaleEconomy";
    }

    public boolean has(final String name, final double amount) {
        return this.getBalance(name) >= amount;
    }

    @Override
    public boolean has(final OfflinePlayer player, final double amount) {
        return this.getBalance(player.getUniqueId().toString()) >= amount;
    }

    public boolean has(final String name, final String arg1, final double amount) {
        return this.has(name, amount);
    }

    @Override
    public boolean has(final OfflinePlayer player, final String arg1, final double amount) {
        return this.has(player.getUniqueId().toString(), amount);
    }

    public boolean hasAccount(final String name) {
        return RoyaleEconomy.dataManager.getMoneyFromFile(name) != null;
    }

    @Override
    public boolean hasAccount(final OfflinePlayer player) {
        return RoyaleEconomy.dataManager.getMoneyFromFile(player.getUniqueId().toString()) != null;
    }

    public boolean hasAccount(final String name, final String arg1) {
        return hasAccount(name);
    }

    @Override
    public boolean hasAccount(final OfflinePlayer player, final String arg1) {
        return hasAccount(player.getUniqueId().toString());
    }

    public boolean isEnabled() {
        return true;
    }

    public EconomyResponse withdrawPlayer(final String name, final double amount) {
        try{
            if(RoyaleEconomy.dataManager.removeMoneyFromFile(name, amount))
                return new EconomyResponse(amount, this.getBalance(name), EconomyResponse.ResponseType.SUCCESS, "");
            else{
                return new EconomyResponse(0d, 0d, EconomyResponse.ResponseType.FAILURE, "Failed to withdraw money from "+name);
            }
        }catch(Exception x){
            return new EconomyResponse(0d, 0d, EconomyResponse.ResponseType.FAILURE, "Failed to withdraw money from "+name);
        }
    }

    @Override
    public EconomyResponse withdrawPlayer(final OfflinePlayer player, final double amount) {
        try{
            if(RoyaleEconomy.dataManager.removeMoneyFromFile(player.getUniqueId().toString(), amount))
                return new EconomyResponse(amount, this.getBalance(player.getUniqueId().toString()), EconomyResponse.ResponseType.SUCCESS, "");
            else{
                return new EconomyResponse(0d, 0d, EconomyResponse.ResponseType.FAILURE, "Failed to withdraw money from "+player.getName());
            }
        }catch(Exception x){
            return new EconomyResponse(0d, 0d, EconomyResponse.ResponseType.FAILURE, "Failed to withdraw money from "+player.getName());
        }
    }

    public EconomyResponse withdrawPlayer(final String name, final String arg1, final double amount) {
        return this.withdrawPlayer(name, amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, String worldName, double amount) {
        return withdrawPlayer(player.getUniqueId().toString(), worldName, amount);
    }

    public boolean hasBankSupport() {
        return true;
    }

    public EconomyResponse isBankMember(final String arg0, final String arg1) {
        return new EconomyResponse(0,0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "RoyaleEconomy does not store bank members, this function is disabled.");
    }

    public EconomyResponse isBankOwner(final String arg0, final String arg1) {
        return new EconomyResponse(0,0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "RoyaleEconomy does not store bank members, this function is disabled.");
    }

    public EconomyResponse bankBalance(final String player) {
        Double externalBalance=RoyaleEconomy.dataManager.getMoneyFromExternal(player);
        if(externalBalance==null){
            return new EconomyResponse(0,0, EconomyResponse.ResponseType.FAILURE, "Bank was not found");
        }
        else{
            return new EconomyResponse(externalBalance, externalBalance, EconomyResponse.ResponseType.SUCCESS, "");

        }
        /*String uuid;
        if(player.length()<17)
            uuid = RoyaleEconomy.dataManager.getUUIDfromName(player);
        else
            uuid=player;
        try {
            if (uuid != null) {
                double balance = Math.floor(RoyaleEconomy.dataManager.getBankMoneyFromFile(uuid) * 100) / 100;
                return new EconomyResponse(balance, balance, EconomyResponse.ResponseType.SUCCESS, "");
            }
        }catch(Exception x){

        }
        return new EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.FAILURE, "No personal bank or shared bank found");
        */
    }

    public EconomyResponse bankDeposit(final String arg0, final double arg1) {
        try{
            RoyaleEconomy.dataManager.addMoneyToExternal(arg0, arg1);
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.SUCCESS, "");
        }catch(Exception x){
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "");
        }
        /*try{
            final double arg1Final=Math.floor(arg1*100)/100;
            String uuid;
            if(arg0.length()<17)
                uuid = RoyaleEconomy.dataManager.getUUIDfromName(arg0);
            else
                uuid=arg0;
            if(RoyaleEconomy.dataManager.removeMoneyFromFile(uuid, arg1Final)) {
                RoyaleEconomy.dataManager.addBankMoneyToFile(uuid, arg1Final);
                return new EconomyResponse(arg1Final, bankBalance(uuid).balance, EconomyResponse.ResponseType.SUCCESS, "");
            }
            else
                return new EconomyResponse(0d, 0d, EconomyResponse.ResponseType.FAILURE, "Failed to deposit money to "+arg0);
        }catch(Exception x){
            return new EconomyResponse(0d, 0d, EconomyResponse.ResponseType.FAILURE, "Failed to deposit money to "+arg0);
        }
        */
    }

    public EconomyResponse bankHas(final String arg0, final double arg1) {
        double balance = bankBalance(arg0).balance;
        if(balance>=arg1)
            return new EconomyResponse(arg1, balance, EconomyResponse.ResponseType.SUCCESS, "");
        return new EconomyResponse(arg1, balance, EconomyResponse.ResponseType.FAILURE, "Not enough coins.");
    }

    public EconomyResponse bankWithdraw(final String arg0, final double arg1) {
        try{
            RoyaleEconomy.dataManager.addMoneyToExternal(arg0, -1*arg1);
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.SUCCESS, "");
        }catch(Exception x){
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "");
        }
        /*try{
            String uuid;
            if(arg0.length()<17)
                uuid = RoyaleEconomy.dataManager.getUUIDfromName(arg0);
            else
                uuid=arg0;
            RoyaleEconomy.dataManager.removeBankMoneyToFile(uuid, arg1);
            RoyaleEconomy.dataManager.addMoneyToFile(uuid, arg1);
            return new EconomyResponse(arg1, bankBalance(uuid).balance, EconomyResponse.ResponseType.SUCCESS, "");
        }catch(Exception x){
            return new EconomyResponse(0d, 0d, EconomyResponse.ResponseType.FAILURE, "Failed to deposit money to "+arg0);
        }
        */
    }

    public EconomyResponse createBank(final String arg0, final String arg1) {
        if(RoyaleEconomy.dataManager.createAccount(arg0)){
            return new EconomyResponse(0,0, EconomyResponse.ResponseType.SUCCESS, "");
        }
        else
            return new EconomyResponse(0,0, EconomyResponse.ResponseType.FAILURE, "Bank already exists");
    }

    public EconomyResponse createBank(String name, OfflinePlayer player) {
        return this.createBank(name, player.getUniqueId().toString());
    }

    public EconomyResponse deleteBank(final String arg0) {
        if(RoyaleEconomy.dataManager.deleteAccount(arg0)){
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.SUCCESS, "");
        }
        else
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "");
    }

    public EconomyResponse isBankOwner(String name, OfflinePlayer player) {
        return this.isBankOwner(name, player.getUniqueId().toString());
    }

    public EconomyResponse isBankMember(String name, OfflinePlayer player) {
        return this.isBankMember(name, player.getUniqueId().toString());
    }

}
