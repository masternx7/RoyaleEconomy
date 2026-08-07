package me.qKing12.RoyaleEconomy.API;

public class APIHandler {
    public Balance balance;
    public BalanceTop balanceTop;
    public Bank bank;
    public CoinsBags coinsBags;
    public Interest interest;
    public PiggyBank piggyBank;
    public SharedBank sharedBank;
    public Shops shops;
    public Talismans talismans;

    public APIHandler(){
        balance = new Balance();
        balanceTop = new BalanceTop();
        bank = new Bank();
        coinsBags = new CoinsBags();
        interest = new Interest();
        piggyBank = new PiggyBank();
        sharedBank = new SharedBank();
        shops = new Shops();
        talismans = new Talismans();
    }

    public Balance getBalance() {
        return balance;
    }

    public BalanceTop getBalanceTop() {
        return balanceTop;
    }

    public Bank getBank() {
        return bank;
    }

    public CoinsBags getCoinsBags() {
        return coinsBags;
    }

    public Interest getInterest() {
        return interest;
    }

    public PiggyBank getPiggyBank() {
        return piggyBank;
    }

    public SharedBank getSharedBank() {
        return sharedBank;
    }

    public Shops getShops() {
        return shops;
    }

    public Talismans getTalismans() {
        return talismans;
    }
}
