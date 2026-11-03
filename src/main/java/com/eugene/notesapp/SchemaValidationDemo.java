package com.eugene.notesapp;

import com.eugene.notesapp.db.DBConnection;
import com.eugene.notesapp.orm.JpaUtil;
import com.eugene.notesapp.orm.demo.BrokenNoteMapping;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.tool.schema.spi.SchemaManagementException;
import javax.sql.DataSource;
import java.util.UUID;

public final class SchemaValidationDemo {
    private SchemaValidationDemo() { }

    public static String demonstrateMismatch(DataSource source) {
        StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                .applySetting("jakarta.persistence.nonJtaDataSource", source)
                .applySetting("hibernate.hbm2ddl.auto", "validate")
                .build();
        try {
            try (var unexpected = new MetadataSources(registry).addAnnotatedClass(BrokenNoteMapping.class)
                    .buildMetadata().buildSessionFactory()) {
                throw new AssertionError("Expected validation failure for @Column(name = name_typo)");
            }
        } catch (SchemaManagementException expected) {
            if (!expected.getMessage().contains("name_typo")) throw expected;
            return expected.getMessage();
        } finally {
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }

    public static void main(String[] args) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:mem:validate-" + UUID.randomUUID());
        config.setUsername("sa");
        config.setPassword("");
        config.setMaximumPoolSize(2);
        try (HikariDataSource source = new HikariDataSource(config)) {
            DBConnection.initialize(source);
            try (JpaUtil correct = JpaUtil.open(source)) {
                if (!correct.isOpen()) throw new AssertionError("Correct mapping failed");
                System.out.println("Correct @Column(name = name): validate passed on init.sql.");
            }
            System.out.println("Changed @Column(name = name_typo): " + demonstrateMismatch(source));
            try (JpaUtil correctAgain = JpaUtil.open(source)) {
                if (!correctAgain.isOpen()) throw new AssertionError("Schema was changed");
                System.out.println("PASS: validate detects bad @Column and leaves the schema unchanged.");
            }
        }
    }
}
