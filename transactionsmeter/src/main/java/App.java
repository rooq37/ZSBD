import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class App {
    private static final AppProperties props = Utils.getProps();
    private static final Mode APP_MODE = props.getMode();

    public static void main(String[] args) throws SQLException, IOException {
        Utils.establishConnection();
        if (props.isRestoreBackupOnStart()) Utils.restoreBackup();
        if (APP_MODE.equals(Mode.PLANER)) Utils.clearPlanFile();

        if (props.isClearRunEnabled()) {
            System.out.println();
            System.out.printf("###############*********** Clean RUN || %s mode **********###################%n", APP_MODE);
            runAllTransactions(false, false);
        }

        if (props.isIndexEnabled()) {
            System.out.println();
            Utils.setAllIndexes();
            System.out.printf("###############*********** RUN || %s mode **********###################%n", APP_MODE);
            System.out.println("############*********** INDEXES ADDED!!! **********###############");
            runAllTransactions(true, false);
            Utils.removeAllIndexes();
            System.out.println("############*********** INDEXES REMOVED!!! **********###############");
        }

        if (props.isPartitionsEnabled()) {
            System.out.println();
            Utils.loadPartitions();
            System.out.printf("###############*********** RUN || %s mode **********###################%n", APP_MODE);
            System.out.println("############*********** PARTITONS ADDED!!! **********###############");
            runAllTransactions(false, true);
            Utils.restoreBackup();
            System.out.println("############*********** PARTITIONS REMOVED!!! **********###############");
        }

        if (props.isRestoreBackupOnEnd()) Utils.restoreBackup();
        Utils.closeConnections();
    }

    public static void runAllTransactions(boolean hasIndex, boolean hasPartitions) throws IOException, SQLException {
        if (props.isCalculateStats()) Utils.calculateStats();
        for (Path transactionsPath : Utils.getTransactionsPaths()) {
            try {
                String query = Files.readString(transactionsPath);
                executeTransactionAndPrintResults(transactionsPath.toString(), query, props.getNumberOfIteration(), hasIndex, hasPartitions);
            } catch (SQLException | IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void executeTransactionAndPrintResults(String name, String query, int count, boolean hasIndex, boolean hasPartition) throws SQLException, InterruptedException, IOException {
        List<Long> times = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            if (APP_MODE.equals(Mode.PLANER)) {
                times.add(executeUserQuery(name, String.format("explain plan set statement_id= '%s' for %s", name, query), hasIndex, hasPartition));
            } else {
                times.add(executeUserQuery(name, query, hasIndex, hasPartition));
                Thread.sleep(props.getSleepBetweenIterations());
                System.out.print("*");
            }
        }
        double average = times.stream().mapToLong(val -> val).average().orElse(0);
        double min = times.stream().mapToLong(val -> val).min().orElse(0);
        double max = times.stream().mapToLong(val -> val).max().orElse(0);

        System.out.printf("*********** TRANSACTION || %s **********%n", name);
        System.out.println("Count: " + count);
        System.out.println("Average: " + average / 1000 + "s");
        System.out.println("Min: " + min / 1000 + "s");
        System.out.println("Max: " + max / 1000 + "s");
        System.out.println("Times: " + times);
    }

    public static long executeUserQuery(String name, String query, boolean hasIndex, boolean hasPartition) throws SQLException, IOException {
        Utils.checkDatabase();
        Utils.clearCaches();

        Savepoint my_savepoint = null;
        if (APP_MODE.equals(Mode.TRANSACTION_METER)) {
            my_savepoint = Utils.getUserConnection().setSavepoint();
        }

        CallableStatement statement = Utils.getUserConnection().prepareCall(query);

        Instant start = Instant.now();
        statement.execute();
        Instant finish = Instant.now();
        long duration = Duration.between(start, finish).toMillis();

        statement.close();

        if (APP_MODE.equals(Mode.PLANER)) {
            Utils.savePlanToFile(name, hasIndex, hasPartition);
        }
        if (my_savepoint != null) {
            Utils.getUserConnection().rollback(my_savepoint);
        }
        return duration;
    }


}
