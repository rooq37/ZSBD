import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class App {

    public static void main(String[] args) throws SQLException {
        Utils.establishConnection();
        int count = 10;
        for (Path transactionsPath : Utils.getTransactionsPaths()) {
            try {
                String query = Files.readString(transactionsPath);
                executeTransactionAndPrintResults(transactionsPath.toString(), query, count);
            } catch (SQLException | IOException e) {
                e.printStackTrace();
            }
        }

        Utils.closeConnections();
    }

    public static void executeTransactionAndPrintResults(String name, String query, int count) throws SQLException {
        List<Long> times = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            times.add(executeUserQuery(query));
            System.out.println("*");
        }
        double average = times.stream().mapToLong(val -> val).average().orElse(0);
        double min = times.stream().mapToLong(val -> val).min().orElse(0);
        double max = times.stream().mapToLong(val -> val).max().orElse(0);

        System.out.printf("*********** TRANSACTION || %s **********%n", name);
        System.out.println("Count: " + count);
        System.out.println("Average: " + average + "ms");
        System.out.println("Min: " + min + "ms");
        System.out.println("Max: " + max + "ms");
        System.out.println("Times: " + times);
    }

    public static long executeUserQuery(String query) throws SQLException {
        clearCaches();  //TODO uncomment

        Instant start = Instant.now();

        Statement statement = Utils.getUserConnection().createStatement();
        statement.executeQuery(query);

        Instant finish = Instant.now();
        return Duration.between(start, finish).toMillis();
    }

    public static void clearCaches() throws SQLException {
        Statement statement = Utils.getSystemConnection().createStatement();
        statement.executeQuery("alter system flush buffer_cache");
        statement.executeQuery("alter system flush shared_pool");
    }
}
