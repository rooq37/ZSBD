import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class Utils {
    private static final AppProperties props = new AppProperties();
    private static final String urlUser = String.format("jdbc:oracle:thin:%s/%s@localhost:1521/XEPDB1", props.getUserUsername(), props.getUserPassword());
    private static final String urlAdmin = String.format("jdbc:oracle:thin:%s/\"%s\"@localhost:1521/XEPDB1", props.getSystemUsername(), props.getSystemPassword());
    private static final List<Path> transactionsPaths = SqlCollections.ACTUAL;

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

    public static void clearPlanFile() throws IOException {
        FileWriter fileWriter = new FileWriter(props.getPlansFilename());
        fileWriter.close();
    }

    public static void savePlanToFile(String name, boolean hasIndex, boolean hasPartition, boolean hasInMemory) throws SQLException, IOException {
        FileWriter fileWriter = new FileWriter(props.getPlansFilename(), true);
        PrintWriter printWriter = new PrintWriter(fileWriter);
        printWriter.println("");
        printWriter.println(" **** Indexes: " + hasIndex + ", Partitions: " + hasPartition + ", InMemory: " + hasInMemory +" **** " + name + " **********");

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

    private static HashMap<String, String> getIndexes() {
        HashMap<String, String> indexes = new HashMap<>();
        try {
            String content = Files.readString(Path.of(props.getIndexFilename())).replace("\n", " ");
            for (String indexQuery : content.split(";")) {
                String indexName = indexQuery.toLowerCase().split("index")[1].trim().split(" ")[0];
                if (indexName.isBlank()) throw new IllegalStateException("Index name is empty!");
                indexes.put(indexName, indexQuery);
            }
            return indexes;
        } catch (IOException e) {
            e.printStackTrace();
        }
        throw new IllegalStateException("Cannot import index file");
    }

    public static void setAllIndexes() throws SQLException {
        HashMap<String, String> indexes = getIndexes();
        Connection userConnection = getUserConnection();
        for (String query : indexes.values()) {
            Statement statement = userConnection.createStatement();
            statement.executeQuery(query);
            statement.close();
        }
        userConnection.commit();
    }

    public static void removeAllIndexes() throws SQLException {
        HashMap<String, String> indexes = getIndexes();
        Connection userConnection = getUserConnection();
        for (String indexName : indexes.keySet()) {
            Statement statement = userConnection.createStatement();
            statement.executeQuery(String.format("DROP INDEX %s", indexName));
            statement.close();
        }
        userConnection.commit();
    }

    public static void loadPartitions() throws IOException, SQLException {
        String content = Files.readString(Paths.get(props.getPartitionFilename()));
        for (String query : content.split(";")) {
            Statement statement = Utils.getSystemConnection().createStatement();
            statement.executeQuery(query);
            statement.close();
        }
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
            userConnection = null;
        }
        if (adminConnection != null) {
            adminConnection.close();
            adminConnection = null;
        }
    }

    public static AppProperties getProps() {
        return props;
    }

    public static void clearCaches() throws SQLException {
        Statement statement = Utils.getSystemConnection().createStatement();
        statement.executeQuery("alter system flush buffer_cache");
        statement.executeQuery("alter system flush shared_pool");
        statement.close();
    }

    public static void calculateStats() throws SQLException, IOException {
        CallableStatement statement = Utils.getSystemConnection().prepareCall(Files.readString(Paths.get("calculate_stats")));
        statement.execute();
        statement.close();
    }

    public static void restoreBackup() throws SQLException, IOException {
        String content = Files.readString(Paths.get("restore_backup"));
        for (String query : content.split(";")) {
            Statement statement = Utils.getSystemConnection().createStatement();
            statement.executeQuery(query);
            statement.close();
        }
    }

    public static void addInMemory() throws SQLException, IOException {
        String content = Files.readString(Paths.get("inmemory"));
        for (String query : content.split(";")) {
            Statement statement = Utils.getUserConnection().createStatement();
            statement.executeQuery(query);
            statement.close();
        }
    }

    public static void parsePlansToReport() throws IOException {
        String[] plans = Files.readString(Paths.get(props.getPlansFilename())).split("\n \\*\\*\\*\\* ");
        int n = transactionsPaths.size();
        String[][] report = new String[n][5];
        for(int i = 1; i < plans.length; i++) {
            String plan = plans[i];
            String name = plan.substring(plan.indexOf("transaction"), plan.indexOf(".sql"));
            int costColNum = Arrays.stream(plan.split("\n")[4].split("\\|")).collect(Collectors.toList()).indexOf(" Cost (%CPU)");
            String cost = plan.split("\n")[6].split("\\|")[costColNum].trim().split(" ")[0].trim();
            report[(i - 1) % n][0] = name;
            if(plan.contains("Indexes: true")) {
                report[(i - 1) % n][2] = cost;
            } else if (plan.contains("Partitions: true")) {
                report[(i - 1) % n][3] = cost;
            } else if (plan.contains("InMemory: true")) {
                report[(i - 1) % n][4] = cost;
            } else {
                report[(i - 1) % n][1] = cost;
            }
        }
        System.out.println("###############*********** GENERATE FINAL COSTS REPORT **********###################");

        FileWriter fileWriter = new FileWriter(props.getPlanerReportFilename());
        PrintWriter printWriter = new PrintWriter(fileWriter);

        printWriter.println("Name;Clean;Indexes;Partitions;In memory");
        for (String[] strings : report) {
            printWriter.println(String.join(";", strings));
        }
        printWriter.close();

        System.out.printf("###############*********** %s GENERATED **********###################%n", props.getPlanerReportFilename());
    }
}
