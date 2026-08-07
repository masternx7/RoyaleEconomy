package me.qKing12.RoyaleEconomy.DataManager.SellLimitGlobal;

import java.time.ZoneId;
import java.util.List;

public interface ISellLimitGlobal {

    double getLimit(String playerUUID);

    void setLimit(String playerUUID, double limit);

    void setLimit(List<Object[]> pairPlayerUuidAndLimit);

    void resetLimit(String playerUUID);

    void resetLimit();

    int loadSellLimit();

    ZoneId getZoneId();
}
