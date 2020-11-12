import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class AppProperties {
    private Properties properties = new Properties();

    public AppProperties() {
        try {
            properties.load(new FileReader("config.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getUserUsername() {
        return properties.getProperty("user-username");
    }

    public String getUserPassword() {
        return properties.getProperty("user-password");
    }

    public String getSystemUsername() {
        return properties.getProperty("system-username");
    }

    public String getSystemPassword() {
        return properties.getProperty("system-password");
    }

    public String getPlansFilename() {
        return properties.getProperty("plans-filename");
    }

    public Mode getMode(){
        switch (properties.getProperty("mode")){
            case "PLANER":
                return Mode.PLANER;
            case "METER":
                return Mode.TRANSACTION_METER;
        }
        throw new IllegalArgumentException("Bad mode");
    }
}
