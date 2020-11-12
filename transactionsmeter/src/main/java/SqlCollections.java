import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SqlCollections {
    public static List<Path> ACTUAL = new ArrayList<>();
    private static List<Path> ALL_METRICS =
            Arrays.asList(
                    Paths.get("transaction-1.sql"),
                    Paths.get("transaction-2.sql"),
                    Paths.get("transaction-3.sql"),
                    Paths.get("transaction-4.sql"),
                    Paths.get("transaction-5.sql"),
                    Paths.get("transaction-6.sql"),
                    Paths.get("transaction-7.sql"),
                    Paths.get("transaction-8.sql")
            );
    private static List<Path> ALL_PLANS =
            Arrays.asList(
                    Paths.get("transaction-1.sql"),
                    Paths.get("transaction-2.sql"),
                    Paths.get("transaction-3.sql"),
                    Paths.get("transaction-4.sql"),
                    Paths.get("transaction-5.sql"),
                    Paths.get("transaction-6a.sql"),
                    Paths.get("transaction-6b.sql"),
                    Paths.get("transaction-7.sql"),
                    Paths.get("transaction-8a.sql"),
                    Paths.get("transaction-8b.sql"),
                    Paths.get("transaction-8c.sql")
            );
    private static List<Path> CUSTOM_PLAN = Arrays.asList(Paths.get("transaction-1.sql"));

    static {
        ACTUAL = CUSTOM_PLAN;
    }


}
