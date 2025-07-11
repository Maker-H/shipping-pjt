package io.hhplus.tdd.mvc;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class MvcTestConfig {
    @Bean
    @Primary
    public UserPointTable userPointTable() {
        return new UserPointTable();
    }

    @Bean
    @Primary
    public PointHistoryTable pointHistoryTable() {
        return new PointHistoryTable();
    }

    @Bean
    @Primary
    public PointController pointController(PointService pointService) {
        return new PointController(pointService);
    }
}
