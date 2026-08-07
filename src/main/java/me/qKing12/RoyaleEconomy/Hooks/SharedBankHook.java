package me.qKing12.RoyaleEconomy.Hooks;

import java.util.ArrayList;

public interface SharedBankHook {

    String getSharedBankId(String playerUUID);

    ArrayList<String> getMembers(String bankID, boolean owner);

    String getOwner(String bankId);
}
