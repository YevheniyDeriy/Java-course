package com.eugene.notesapp;

import com.eugene.notesapp.db.DBConnection;
import com.eugene.notesapp.orm.JpaUtil;
import com.eugene.notesapp.orm.demo.OrmPitfalls;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.util.UUID;

public final class OrmPitfallsDemo {
    private OrmPitfallsDemo() { }
    public static void main(String[] args) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:mem:pitfalls-" + UUID.randomUUID());
        config.setUsername("sa");
        config.setPassword("");
        config.setMaximumPoolSize(2);
        try (HikariDataSource source = new HikariDataSource(config)) {
            DBConnection.initialize(source);
            try (JpaUtil jpa = JpaUtil.open(source)) {
                OrmPitfalls.demonstrate(jpa, "category-");
                System.out.println("PASS: LazyInitializationException and N+1 reproduced and fixed.");
            }
        }
    }
}
