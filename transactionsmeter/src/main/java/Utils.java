import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.Arrays;
import java.util.List;

public class Utils {
    private static final String urlUser = "jdbc:oracle:thin:zsbduser/pw_zsbduser@localhost:1521/XEPDB1";
    private static final String urlAdmin = "jdbc:oracle:thin:system/" + "\"2sPItlNGCW4=1\"" + "@localhost:1521/XEPDB1";
    // TODO ustawic haslo w 'haslo_do_system'


    private static final List<Path> transactionsPaths =
            Arrays.asList(
//                    Paths.get("transaction-1.sql"),
//                    Paths.get("transaction-2.sql"),
//                    Paths.get("transaction-3.sql"),
//                    Paths.get("transaction-4.sql"),
//                    Paths.get("transaction-5.sql"),
//                    Paths.get("transaction-6.sql"),
//                    Paths.get("transaction-6a.sql"),
//                    Paths.get("transaction-6b.sql"),
//                    Paths.get("transaction-7.sql"),
//                    Paths.get("transaction-8.sql"),
                    Paths.get("transaction-8a.sql"),
                    Paths.get("transaction-8b.sql"),
                    Paths.get("transaction-8c.sql")
            );

    private static Connection userConnection;
    private static Connection adminConnection;

    public static void establishConnection() throws SQLException {
        if (userConnection == null) {
            userConnection = DriverManager.getConnection(urlUser);
            userConnection.setAutoCommit(false);
        }
        if (adminConnection == null) {
            adminConnection = DriverManager.getConnection(urlAdmin);
        }

    }

    public static void getStatistic(String name) throws SQLException, IOException {
        FileWriter fileWriter = new FileWriter("plans.txt", true);
        PrintWriter printWriter = new PrintWriter(fileWriter);
        printWriter.println("");
        printWriter.println(" ********** " + name + " **********");

        Statement statement = Utils.getUserConnection().createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT * FROM TABLE(DBMS_XPLAN.DISPLAY())");
        ResultSetMetaData metadata = resultSet.getMetaData();
        int columnCount = metadata.getColumnCount();
        for (int i = 1; i <= columnCount; i++) {
            while (resultSet.next()) {
                printWriter.println(resultSet.getString(1));
            }
        }

        printWriter.close();
    }

    public static void checkDatabase() throws SQLException {
        Statement statement1 = Utils.getUserConnection().createStatement();
        ResultSet resultSet1 = statement1.executeQuery("SELECT SUM(\"price\") FROM \"books\"");
        resultSet1.next();
        Statement statement2 = Utils.getUserConnection().createStatement();
        ResultSet resultSet2 = statement2.executeQuery("SELECT SUM(\"total_price\") FROM \"orders\"");
        resultSet2.next();
        Statement statement3 = Utils.getUserConnection().createStatement();
        ResultSet resultSet3 = statement3.executeQuery("SELECT COUNT(*) FROM \"orders\"\n" +
                "WHERE\n" +
                "    \"order_id\" IN (SELECT \"order_id\" FROM \"orders\" WHERE \"status\"='ORDER_PLACED')\n" +
                "AND\n" +
                "    \"order_id\" IN (SELECT \"order_id\" FROM \"order_items\" WHERE \"isbn\" IN ( SELECT \"isbn\" FROM \"books\" WHERE \"publisher_id\" IN (SELECT \"publisher_id\" FROM \"publishers\" WHERE \"city\" = 'Yutou') ))");
        resultSet3.next();

        double sumBooksPrices = resultSet1.getDouble(1);
        double sumOrdersTotalPrices = resultSet2.getDouble(1);
        int sumOrdersWherePublisherIsFromSelectedCity = resultSet3.getInt(1);

        if (sumBooksPrices != 251772.89 || sumOrdersTotalPrices != 1.346111165E7 || sumOrdersWherePublisherIsFromSelectedCity != 41) {
            statement1.close();
            statement2.close();
            statement3.close();
            throw new IllegalStateException();
        }

        statement1.close();
        statement2.close();
        statement3.close();
    }

    public static Connection getUserConnection() {
        return userConnection;
    }

    public static Connection getSystemConnection() {
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
