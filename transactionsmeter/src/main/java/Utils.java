import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class Utils {
    private static final String urlUser = "jdbc:oracle:thin:zsbduser/pw_zsbduser@localhost:1521/XEPDB1";
    private static final String urlAdmin = "jdbc:oracle:thin:system/haslo_do_system@localhost:1521/XEPDB1";
    // TODO ustawic haslo w 'haslo_do_system'

    private static final List<Path> transactionsPaths =
            Arrays.asList(
                    Paths.get("transaction-1.sql"),
                    Paths.get("transaction-2.sql")
            );

    private static Connection userConnection;
    private static Connection adminConnection;

    public static Connection getUserConnection() throws SQLException {
        if (userConnection == null) {
            userConnection = DriverManager.getConnection(urlUser);
        }
        return userConnection;
    }

    public static Connection getSystemConnection() throws SQLException {
        if (adminConnection == null) {
            adminConnection = DriverManager.getConnection(urlAdmin);
        }
        return adminConnection;
    }

    public static List<Path> getTransactionsPaths() {
        return transactionsPaths;
    }

    public static void closeConnections() throws SQLException {
        if (userConnection != null) {
            userConnection.close();
        }
        if (adminConnection != null) {
            adminConnection.close();
        }
    }
}
