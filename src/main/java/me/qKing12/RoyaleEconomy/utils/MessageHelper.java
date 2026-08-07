package me.qKing12.RoyaleEconomy.utils;

import me.qKing12.RoyaleEconomy.Commands.InterestCommand;
import me.qKing12.RoyaleEconomy.DataManager.StaticValues;
import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.ZonedDateTime;
import java.util.*;


public class MessageHelper {

    public boolean useDecimals;

    private interface RoyaleEconomyNumberFormat {
        String getNumber(double number);
    }

    private interface RoyaleEconomyInterestCooldown {
        String getInterestCooldown(String type);
    }

    private RoyaleEconomyNumberFormat royaleEconomyNumberFormat;
    private RoyaleEconomyInterestCooldown interestCooldown;

    private static final NavigableMap<Double, String> suffixes = new TreeMap<>();

    public Double getCoinsFromFormat(String format) {
        format = format.replace(",", "").replace(" ", "");
        for (Map.Entry<Double, String> entry : suffixes.entrySet()) {
            String unit = entry.getValue();
            if (format.toUpperCase().endsWith(unit.toUpperCase())) {
                Double toReturn = Double.parseDouble(format.replace(unit.toUpperCase(), "").replace(unit.toLowerCase(), "")) * entry.getKey();
                if (toReturn.isInfinite() || toReturn.isNaN())
                    return -1d;
                return toReturn;
            }
        }

        Double toReturn = Double.parseDouble(format);
        if (toReturn.isInfinite() || toReturn.isNaN())
            return -1d;
        return toReturn;
    }

    public String formatCoinsNotShort(double number) {
        return numberFormat.format(number).replace("\u00A0", ",");
    }

    public String formatCoinsShort(double value) {
        if (value == Double.MIN_VALUE) return formatCoinsShort(Double.MIN_VALUE + 1);
        if (value < 0) return "-" + formatCoinsShort(-value);
        if (value < 1000) {
            return numberFormat.format(value);
        }//return Double.toString(Math.floor(value)).replace(".0", ""); //deal with easy case

        Map.Entry<Double, String> e = suffixes.floorEntry(value);
        if (e == null) return numberFormat.format(value);
        Double divideBy = e.getKey();
        String suffix = e.getValue();

        double truncated = value / (divideBy / 10); //the number part of the output times 10
        boolean hasDecimal = truncated < 100 && (truncated / 10d) != (truncated / 10);
        //return hasDecimal ? (truncated / 10d) + suffix : (truncated / 10) + suffix;
        //return Double.toString(Math.floor(truncated / 10)).replace(".0", "")+suffix;
        return new DecimalFormat("#.##").format(truncated / 10) + suffix;
    }

    private NumberFormat numberFormat;

    public MessageHelper() {
        if (RoyaleEconomy.plugin.getConfig().getBoolean("number-format.short-format.use")) {
            royaleEconomyNumberFormat = this::formatCoinsShort;
            useDecimals = false;
        } else {
            royaleEconomyNumberFormat = this::formatCoinsNotShort;
        }

        this.numberFormat = NumberFormat.getInstance();
        numberFormat.setGroupingUsed(true);
        if (RoyaleEconomy.plugin.getConfig().getBoolean("number-format.use-decimals")) {
            numberFormat.setMaximumFractionDigits(2);
            useDecimals = true;
        } else {
            numberFormat.setRoundingMode(RoundingMode.FLOOR);
            numberFormat.setMaximumFractionDigits(0);
            useDecimals = false;
        }


        if (RoyaleEconomy.plugin.getConfig().getBoolean("use-interest")) {
            interestCooldown = this::getInterestCooldownYes;
        } else
            interestCooldown = this::getInterestCooldownNo;
        for (String key : RoyaleEconomy.plugin.getConfig().getConfigurationSection("number-format.short-format.units").getKeys(false)) {
            ConfigurationSection unit = RoyaleEconomy.plugin.getConfig().getConfigurationSection("number-format.short-format.units." + key);
            suffixes.put(Math.pow(10, unit.getDouble("number-of-zeros")), unit.getString("unit-name"));
        }
    }

    public String numberFormat(double number) {
        return royaleEconomyNumberFormat.getNumber(number);
    }

    public String formatTimeDetailed(Long time) {
        long zi = time / 86400000;
        time %= 86400000;
        long ora = time / 3600000;
        time %= 3600000;
        long minut = time / 60000;
        String timpFinal = "";
        if (zi > 0) {
            timpFinal = timpFinal.concat(zi + " " + (zi == 1 ? RoyaleEconomy.staticValues.day : RoyaleEconomy.staticValues.days) + " ");
        }
        if (ora > 0) {
            timpFinal = timpFinal.concat(ora + " " + (ora == 1 ? RoyaleEconomy.staticValues.hour : RoyaleEconomy.staticValues.hours) + " ");
        }
        if (minut > 0) {
            timpFinal = timpFinal.concat(minut + " " + (minut == 1 ? RoyaleEconomy.staticValues.minute : RoyaleEconomy.staticValues.minutes));
        }
        if (timpFinal.equals(""))
            timpFinal = RoyaleEconomy.staticValues.soon;
        //if(secunda>0){
        //    timpFinal=timpFinal.concat(secunda+RoyaleEconomy.staticValues.short_second);
        //}
        return timpFinal;
    }

    public String formatTimeShort(Long time) {
        long zi = time / 86400000;
        time %= 86400000;
        long ora = time / 3600000;
        time %= 3600000;
        long minut = time / 60000;
        time %= 60000;
        long secunda = time / 1000;
        String timpFinal = "";
        if (zi > 0) {
            timpFinal = timpFinal.concat(zi + RoyaleEconomy.staticValues.short_day);
        }
        if (ora > 0) {
            timpFinal = timpFinal.concat(ora + RoyaleEconomy.staticValues.short_hour);
        }
        if (minut > 0) {
            timpFinal = timpFinal.concat(minut + RoyaleEconomy.staticValues.short_minute);
        }
        if (secunda > 0) {
            timpFinal = timpFinal.concat(secunda + RoyaleEconomy.staticValues.short_second);
        }
        if (timpFinal.equals(""))
            timpFinal = RoyaleEconomy.staticValues.soon;

        return timpFinal;
    }

    public String formatTimeNotDetailed(Long time) {
        long zi = time / 86400000;
        long ora = time / 3600000;
        long minut = time / 60000;
        long secunda = time / 1000;
        if (zi > 0) {
            if (zi == 1) return "1 " + RoyaleEconomy.staticValues.day;
            return zi + " " + RoyaleEconomy.staticValues.days;
        } else if (ora > 0) {
            if (ora == 1) return "1 " + RoyaleEconomy.staticValues.hour;
            return ora + " " + RoyaleEconomy.staticValues.hours;
        } else if (minut > 0) {
            if (minut == 1) return "1 " + RoyaleEconomy.staticValues.minute;
            return minut + " " + RoyaleEconomy.staticValues.minutes;
        } else if (secunda > 0) {
            if (secunda == 1) return "1 ".concat(RoyaleEconomy.staticValues.second);
            return secunda + " " + RoyaleEconomy.staticValues.seconds;
        } else
            return RoyaleEconomy.staticValues.soon;
    }

    public String getPlayerName(String UUIDString) {
        try {
            String toReturn;
            toReturn = Bukkit.getOfflinePlayer(UUID.fromString(UUIDString)).getName();
            if (toReturn == null)
                toReturn = "Null Player";
            return toReturn;
        } catch (Exception x) {
            return "Null Player";
        }
    }

    public String getInterestCooldown(String type) {
        return interestCooldown.getInterestCooldown(type);
    }

    private String getInterestCooldownYes(String type) {
        if (type.equals("short"))
            return formatTimeShort(InterestCommand.interestDate - ZonedDateTime.now().toInstant().toEpochMilli());
        else if (type.equals("normal"))
            return formatTimeNotDetailed(InterestCommand.interestDate - ZonedDateTime.now().toInstant().toEpochMilli());
        else
            return formatTimeDetailed(InterestCommand.interestDate - ZonedDateTime.now().toInstant().toEpochMilli());
    }

    private String getInterestCooldownNo(String type) {
        return "Interest Disabled";
    }

    public String getBalanceMessage(double coins) {
        String fromConfig = Utils.chat(RoyaleEconomy.commandsCfg.getString("commands.balance.output"));
        return fromConfig.replace("%balance%", numberFormat(coins));
    }

    public String getBalanceMessage(String p, double coins) {
        String fromConfig = Utils.chat(RoyaleEconomy.commandsCfg.getString("commands.balance.output-other"));
        return fromConfig.replace("%balance%", numberFormat(coins))
                .replace("%player-name%", p);
    }

    public String getBankBalanceMessage(double coins) {
        String fromConfig = Utils.chat(RoyaleEconomy.commandsCfg.getString("commands.bank.output"));
        return fromConfig.replace("%balance%", numberFormat(coins));
    }

    public String getBankBalanceMessage(String p, double coins) {
        String fromConfig = Utils.chat(RoyaleEconomy.commandsCfg.getString("commands.bank.output-other"));
        return fromConfig.replace("%balance%", numberFormat(coins))
                .replace("%player-name%", p);
    }

    public String getSharedBankBalanceMessage(Double coins) {
        String fromConfig;
        if (coins == null) {
            fromConfig = Utils.chat(RoyaleEconomy.commandsCfg.getString("commands.sharedbank.no-shared-bank"));
            return fromConfig;
        } else
            fromConfig = Utils.chat(RoyaleEconomy.commandsCfg.getString("commands.sharedbank.output"));
        return fromConfig.replace("%balance%", numberFormat(coins));
    }

    public String getSharedBankBalanceMessage(String p, Double coins) {
        String fromConfig;
        if (coins == null) {
            fromConfig = Utils.chat(RoyaleEconomy.commandsCfg.getString("commands.royaleeconomy.sharedbank.not-shared-bank"));
            return fromConfig;
        } else
            fromConfig = Utils.chat(RoyaleEconomy.commandsCfg.getString("commands.sharedbank.output-other"));
        return fromConfig.replace("%balance%", numberFormat(coins))
                .replace("%player-name%", p);
    }
}
