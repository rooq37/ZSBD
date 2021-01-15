import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class App {
    private static final AppProperties props = Utils.getProps();
    private static final Mode APP_MODE = props.getMode();
    private static final HashMap<String, List<Double>> averageTimes = new HashMap<>();
    private static final List<String> csvHeaders = new ArrayList<>(List.of("Name"));

    public static void main(String[] args) throws SQLException, IOException {
        Utils.establishConnection();
        if (props.isRestoreBackupOnStart()) Utils.restoreBackup();
        if (APP_MODE.equals(Mode.PLANER)) Utils.clearPlanFile();

        if (props.isClearRunEnabled()) {
            System.out.println();
            System.out.printf("###############*********** Clean RUN || %s mode **********###################%n", APP_MODE);
            runAllTransactions(false, false, false);
            csvHeaders.add("Clean [s]");
        }

        if (props.isIndexEnabled()) {
            System.out.println();
            Utils.setAllIndexes();
            System.out.printf("###############*********** RUN || %s mode **********###################%n", APP_MODE);
            System.out.println("############*********** INDEXES ADDED!!! **********###############");
            runAllTransactions(true, false, false);
            csvHeaders.add("Indexes [s]");
            Utils.removeAllIndexes();
            System.out.println("############*********** INDEXES REMOVED!!! **********###############");
        }

        if (props.isPartitionsEnabled()) {
            System.out.println();
            Utils.loadPartitions();
            System.out.printf("###############*********** RUN || %s mode **********###################%n", APP_MODE);
            System.out.println("############*********** PARTITONS ADDED!!! **********###############");
            runAllTransactions(false, false, true);
            csvHeaders.add("Partitions [s]");
            Utils.restoreBackup();
            System.out.println("############*********** PARTITIONS REMOVED!!! **********###############");
        }

        if (props.isInMemoryEnabled()) {
            System.out.println();
            Utils.addInMemory();
            System.out.printf("###############*********** RUN || %s mode **********###################%n", APP_MODE);
            System.out.println("############*********** IN MEMORY ADDED!!! **********###############");
            runAllTransactions(false, false, true);
            csvHeaders.add("In memory");
            Utils.restoreBackup();
            System.out.println("############*********** IN MEMORY REMOVED!!! **********###############");
        }

        System.out.println("###############*********** GENERATE FINAL AVERAGES REPORT **********###################");

        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator(',');

        FileWriter fileWriter = new FileWriter(props.getReportFilename());
        PrintWriter printWriter = new PrintWriter(fileWriter);

        printWriter.println(csvHeaders.stream().map(header -> header + ";").collect(Collectors.joining()));
        averageTimes.forEach((transactionName, values) ->
                printWriter.println(transactionName + ";" +
                        values.stream().
                                map(v -> new DecimalFormat("#.###", symbols).format(v / 1000) + ";").collect(Collectors.joining())));
        printWriter.close();

        System.out.printf("###############*********** %s GENERATED **********###################%n", props.getReportFilename());

        if (props.isRestoreBackupOnEnd()) Utils.restoreBackup();
        Utils.closeConnections();
    }

    public static void runAllTransactions(boolean hasIndex, boolean hasPartitions, boolean hasInMemory) throws IOException, SQLException {
        if (props.isCalculateStats()) Utils.calculateStats();
        for (Path transactionsPath : Utils.getTransactionsPaths()) {
            try {
                String query = Files.readString(transactionsPath);
                executeTransactionAndPrintResults(transactionsPath.toString(), query, props.getNumberOfIteration(), hasIndex, hasPartitions, hasInMemory);
            } catch (SQLException | IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void executeTransactionAndPrintResults(String name, String query, int count, boolean hasIndex, boolean hasPartition, boolean hasInMemory) throws SQLException, InterruptedException, IOException {
        List<Long> times = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            if (APP_MODE.equals(Mode.PLANER)) {
                times.add(executeUserQuery(name, String.format("explain plan set statement_id= '%s' for %s", name, query), hasIndex, hasPartition, hasInMemory));
            } else {
                times.add(executeUserQuery(name, query, hasIndex, hasPartition, hasInMemory));
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

        if (!averageTimes.containsKey(name)) {
            averageTimes.put(name, new ArrayList<>());
        }
        averageTimes.get(name).add(average);
    }

    public static long executeUserQuery(String name, String query, boolean hasIndex, boolean hasPartition, boolean hasInMemory) throws SQLException, IOException {
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
            Utils.savePlanToFile(name, hasIndex, hasPartition, hasInMemory);
        }
        if (my_savepoint != null) {
            Utils.getUserConnection().rollback(my_savepoint);
        }
        return duration;
    }


}
