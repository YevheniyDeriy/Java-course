package com.eugene.notesapp;

import com.eugene.notesapp.db.DBConnection;
import com.eugene.notesapp.model.Category;
import com.eugene.notesapp.service.JdbcCategoryService;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Reproducible end-to-end comparison on a separate, temporary file database. */
public final class BatchBenchmark {
    private BatchBenchmark() { }

    public static void main(String[] args) throws Exception {
        int count = args.length == 0 ? 5000 : Integer.parseInt(args[0]);
        if (count < 1 || count > 100_000) throw new IllegalArgumentException("count must be 1..100000");
        Path directory = Files.createTempDirectory("notes-benchmark-");
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:file:" + directory.resolve("benchmark").toString().replace('\\', '/'));
        config.setUsername("sa");
        config.setPassword("");
        config.setMaximumPoolSize(1);
        try (HikariDataSource source = new HikariDataSource(config)) {
            DBConnection.initialize(source);
            JdbcCategoryService service = new JdbcCategoryService(source);
            List<Category> entities = new ArrayList<>();
            for (int i = 0; i < count; i++) entities.add(new Category("Category " + i));
            for (Category entity : entities.subList(0, Math.min(500, count))) service.save(entity);
            clear(source);
            service.saveBatch(entities.subList(0, Math.min(500, count)));
            clear(source);
            double[] single = new double[3];
            double[] batch = new double[3];
            for (int run = 0; run < 3; run++) {
                // Alternate order to reduce warmup/order bias; cleanup is outside timing.
                if (run % 2 == 0) {
                    single[run] = measure(() -> entities.forEach(service::save));
                    verify(service, count);
                    clear(source);
                    batch[run] = measure(() -> service.saveBatch(entities));
                } else {
                    batch[run] = measure(() -> service.saveBatch(entities));
                    verify(service, count);
                    clear(source);
                    single[run] = measure(() -> entities.forEach(service::save));
                }
                verify(service, count);
                clear(source);
            }
            System.out.println("Java: " + System.getProperty("java.version") + "; OS: " + System.getProperty("os.name"));
            System.out.println("H2 file; records=" + count + "; warmup=500; repeats=3; batchSize=500");
            System.out.println("Single ms: " + Arrays.toString(single));
            System.out.println("Batch ms: " + Arrays.toString(batch));
            Arrays.sort(single);
            Arrays.sort(batch);
            System.out.printf(java.util.Locale.ROOT, "Median single=%.2f ms; batch=%.2f ms; speedup=%.2fx%n",
                    single[1], batch[1], single[1] / batch[1]);
            System.out.println("Single: one transaction per record; batch: one transaction for all records.");
        } finally {
            // Only files in this invocation's freshly created temporary directory.
            Files.deleteIfExists(directory.resolve("benchmark.mv.db"));
            Files.deleteIfExists(directory.resolve("benchmark.trace.db"));
            Files.deleteIfExists(directory);
        }
    }

    private static double measure(Runnable action) {
        long start = System.nanoTime();
        action.run();
        return (System.nanoTime() - start) / 1_000_000.0;
    }

    private static void verify(JdbcCategoryService service, int count) {
        if (service.findAll().size() != count) throw new AssertionError("Wrong inserted count");
    }

    private static void clear(HikariDataSource source) throws Exception {
        try (Connection connection = source.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM categories")) {
            statement.executeUpdate();
        }
    }
}
