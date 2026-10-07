package me.qKing12.RoyaleEconomy.API;

public class APIHandler {
    public Balance balance;
    public BalanceTop balanceTop;
    public Bank bank;
    public Interest interest;
    public SharedBank sharedBank;

    public APIHandler(){
        balance = new Balance();
        balanceTop = new BalanceTop();
        bank = new Bank();
        interest = new Interest();
        sharedBank = new SharedBank();
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

    public Interest getInterest() {
        return interest;
    }

    public SharedBank getSharedBank() {
        return sharedBank;
    }
}
