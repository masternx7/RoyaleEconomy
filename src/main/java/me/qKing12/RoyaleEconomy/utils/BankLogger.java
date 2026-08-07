package me.qKing12.RoyaleEconomy.utils;

import me.qKing12.RoyaleEconomy.RoyaleEconomy;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class BankLogger {
    private final Logger logger = Logger.getLogger(BankLogger.class.getName());
    private FileHandler fh = null;

    public BankLogger() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-M-d_HH-mm-ss");
        try {
            File file = new File(RoyaleEconomy.plugin.getDataFolder() + "/logs/banklog_"
                    + format.format(Calendar.getInstance().getTime()) + ".log");
            if(!file.getParentFile().exists())
                file.getParentFile().mkdirs();
            if(!file.exists())
                file.createNewFile();
            fh = new FileHandler(file.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }

        fh.setFormatter(new Formatter() {
            @Override
            public String format(LogRecord record) {
                SimpleDateFormat logTime = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
                Calendar cal = new GregorianCalendar();
                cal.setTimeInMillis(record.getMillis());
                return record.getLevel() + " "
                        + logTime.format(cal.getTime())
                        + ": "
                        + record.getMessage() + "\n";
            }
        });
        logger.addHandler(fh);
        logger.setUseParentHandlers(false);
    }

    public Logger getLogger() {
        return logger;
    }

    public void cleanup(){
        fh.close();
    }
}
