import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class AppProperties {
    private Properties config = new Properties();
    private Properties users = new Properties();

    public AppProperties() {
        try {
            config.load(new FileReader("properties/config.properties"));
            users.load(new FileReader("properties/users.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getUserUsername() {
        return users.getProperty("user-username");
    }

    public String getUserPassword() {
        return users.getProperty("user-password");
    }

    public String getSystemUsername() {
        return users.getProperty("system-username");
    }

    public String getSystemPassword() {
        return users.getProperty("system-password");
    }

    public String getPlansFilename() {
        return config.getProperty("plans-filename");
    }

    public String getIndexFilename() {
        return config.getProperty("index-filename");
    }

    public String getPartitionFilename() {
        return config.getProperty("partition-filename");
    }

    public int getNumberOfIteration() {
        Mode mode = getMode();
        if (mode == Mode.TRANSACTION_METER) {
            return Integer.parseInt(config.getProperty("iteration-meter"));
        }
        return Integer.parseInt(config.getProperty("iteration-planer"));
    }

    public Mode getMode() {
        switch (config.getProperty("mode")) {
            case "PLANER":
                return Mode.PLANER;
            case "METER":
                return Mode.TRANSACTION_METER;
        }
        throw new IllegalArgumentException("Error while reading MODE from config");
    }

    public boolean isIndexEnabled() {
        return Boolean.parseBoolean(config.getProperty("index"));
    }

    public boolean isClearRunEnabled() {
        return Boolean.parseBoolean(config.getProperty("clear-run"));
    }

    public boolean isCalculateStats() {
        return Boolean.parseBoolean(config.getProperty("calculate-stats"));
    }

    public boolean isPartitionsEnabled() {
        return Boolean.parseBoolean(config.getProperty("partition"));
    }

    public boolean isRestoreBackupOnStart() {
        return Boolean.parseBoolean(config.getProperty("restore-backup-on-start"));
    }

    public boolean isRestoreBackupOnEnd() {
        return Boolean.parseBoolean(config.getProperty("restore-backup-on-end"));
    }

    public int getSleepBetweenIterations() {
        return Integer.parseInt(config.getProperty("sleep-between-iterations"));
    }
}
