package me.qKing12.RoyaleEconomy.API;

import me.qKing12.RoyaleEconomy.Commands.BalanceTopCommand;

public class BalanceTop {

    public class TopBalanceElement{
        private String name;
        private String coinsDisplay;
        private int position;

        public int getPosition(){
            return position;
        }

        public String getCoinsDisplay(){
            return coinsDisplay;
        }

        public String getName(){
            return name;
        }

        public TopBalanceElement(String name, String coinsDisplay, int position){
            this.name=name;
            this.coinsDisplay=coinsDisplay;
            this.position=position;
        }
    }

    public TopBalanceElement getTopPurse(int position){
        position--;
        return new TopBalanceElement(BalanceTopCommand.purseTop.get(position*2), BalanceTopCommand.purseTop.get(position*2+1), position);
    }

    public TopBalanceElement getTopBank(int position){
        position--;
        return new TopBalanceElement(BalanceTopCommand.bankTop.get(position*2), BalanceTopCommand.bankTop.get(position*2+1), position);
    }

    public TopBalanceElement getTopSharedBank(int position){
        position--;
        return new TopBalanceElement(BalanceTopCommand.sharedBankTop.get(position*2), BalanceTopCommand.sharedBankTop.get(position*2+1), position);
    }

}
