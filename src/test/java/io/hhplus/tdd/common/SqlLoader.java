package io.hhplus.tdd.common;

import org.springframework.jdbc.core.JdbcTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class SqlLoader {

    public static void initializeDatabase(Class<?> clazz, String sqlFileName, JdbcTemplate jdbcTemplate) {
        try (InputStream inputStream = clazz.getResourceAsStream(sqlFileName)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("InjectJson sql File not found - " + sqlFileName);
            }

            String content = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

            Arrays.stream(content.split(";"))
                    .map(String::trim)
                    .filter(sql -> !sql.isEmpty())
                    .filter(sql -> !sql.startsWith("--"))
                    .forEach(sql -> {
                        try {
                            jdbcTemplate.execute(sql);
                            System.out.println("Executed: " + sql.substring(0, Math.min(50, sql.length())));
                        } catch (Exception e) {
                            throw new RuntimeException("Failed to execute SQL: " + sql, e);
                        }
                    });
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to read or parse file: " + sqlFileName, e);
        }
    }

    public static void insertRows(Scenario.Table table, JdbcTemplate jdbcTemplate) {
        String tableName = table.getTableName();
        List<Map<String, Object>> rows = table.getRows();

        for (Map<String, Object> row : rows) {
            insertRow(tableName, row, jdbcTemplate);
        }

    }

    private static void insertRow(String tableName, Map<String, Object> row, JdbcTemplate jdbcTemplate) {
        if (row == null || row.isEmpty()) {
            return;
        }
        List<String> columns = new ArrayList<>(row.keySet());
        List<Object> values = new ArrayList<>(row.values());

        // SQL 생성: INSERT INTO table_name (col1, col2, col3) VALUES (?, ?, ?)
        String sql = String.format("INSERT INTO %s (%s) VALUES (%s)",
                tableName,
                String.join(", ", columns),
                String.join(", ", Collections.nCopies(columns.size(), "?")));

        try {
            int rowsAffected = jdbcTemplate.update(sql, values.toArray());
            System.out.println("Executed " + sql);
        } catch (Exception e) {
            throw new RuntimeException("Failed to insert row into " + tableName + ": " + row, e);
        }
    }

}
