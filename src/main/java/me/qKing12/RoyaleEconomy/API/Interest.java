package me.qKing12.RoyaleEconomy.API;

import me.qKing12.RoyaleEconomy.Commands.InterestCommand;
import me.qKing12.RoyaleEconomy.RoyaleEconomy;

public class Interest {

    public String getInterestDateDetailed(){
        return RoyaleEconomy.messageHelper.formatTimeDetailed(RoyaleEconomy.dataManager.getInterestDate());
    }

    public String getInterestDateNotDetailed(){
        return RoyaleEconomy.messageHelper.formatTimeNotDetailed(RoyaleEconomy.dataManager.getInterestDate());
    }

    public String getInterestDateShort(){
        return RoyaleEconomy.messageHelper.formatTimeShort(RoyaleEconomy.dataManager.getInterestDate());
    }

    public Long getLongDate(){
        return RoyaleEconomy.dataManager.getInterestDate();
    }

    public void forceInterest(){
        InterestCommand.isForced=true;
        RoyaleEconomy.dataManager.triggerInterest();
        InterestCommand.isForced=false;
    }

    public void setInterestDate(Long date){
        RoyaleEconomy.dataManager.updateInterestDate(date);
    }
}
