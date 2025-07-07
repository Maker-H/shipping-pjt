package io.hhplus.tdd.mvc;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.PointException;
import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.TransactionType;
import io.hhplus.tdd.point.UserPoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        PointServiceBasicImplTest.OverrideConfig.class,
        MvcTestConfig.class
})
class PointServiceBasicImplTest {

    @Autowired
    private PointController pointController;

    @Autowired
    private UserPointTable pointTable;

    @Autowired
    private PointHistoryTable historyTable;
    @Autowired
    private PointHistoryTable pointHistoryTable;

    static Stream<Arguments> 충전_성공_케이스() {
        // userId, 초기 포인트, 충전 예정 포인트
        return Stream.of(
                Arguments.of(1L, 1000L, 1000L),
                Arguments.of(2L, 2000L, 2000L),
                Arguments.of(3L, 0L, 100L)
        );
    }

    // 0이하 포인트를 충전하려고 할 때 실패
    static Stream<Arguments> 충전_실패_케이스() {
        // userId, 초기 포인트, 충전 예정 포인트
        return Stream.of(
                Arguments.of(1L, 1000L, 0L),
                Arguments.of(2L, 2000L, -1L),
                Arguments.of(3L, 5L, -10L)
        );
    }

    @Test
    void get_테스트() {
        long userId = 1L;
        long amount = 2L;
        pointTable.insertOrUpdate(1L, 2L);

        UserPoint result = pointController.point(userId);

        assertEquals(amount, result.point());
    }

    @Test
    void get시_초기화_테스트() {
        long userId = 1L;
        UserPoint result = pointController.point(userId);

        assertEquals(UserPoint.empty(userId).point(), result.point());
    }

    @ParameterizedTest
    @MethodSource("충전_성공_케이스")
    void 충전_성공_테스트(long userId, long initialAmount, long chargeAmount) {

        pointTable.insertOrUpdate(userId, initialAmount);
        long now = System.currentTimeMillis();

        UserPoint result = pointController.charge(userId, chargeAmount);

        assertThat(result.point()).isEqualTo(initialAmount + chargeAmount);

        List<PointHistory> pointHistories = historyTable.selectAllByUserId(userId);
        assertAll(
                () -> assertFalse(pointHistories.isEmpty()),
                () -> assertThat(pointHistories).hasSize(1)
        );

        PointHistory pointHistory = pointHistories.get(0);
        assertAll(
                () -> assertEquals(userId, pointHistory.userId()),
                () -> assertEquals(chargeAmount, pointHistory.amount()),
                () -> assertEquals(TransactionType.CHARGE, pointHistory.type()),
                () -> assertThat(pointHistory.updateMillis()).isBetween(now - 1000, now + 1000)
        );
    }

    @ParameterizedTest
    @MethodSource("충전_실패_케이스")
    void 충전요청이_마이너스인_경우_테스트(long userId, long initialAmount, long chargeAmount) {

        pointTable.insertOrUpdate(userId, initialAmount);

        assertThrows(PointException.class, () -> pointController.charge(userId, chargeAmount));
    }

    @TestConfiguration
    static class OverrideConfig {

        @Autowired
        private UserPointTable userPointTable;

        @Autowired
        private PointHistoryTable pointHistoryTable;

        @Bean
        @Primary
        public PointService pointService() {
            return new PointServiceBasicImpl(pointHistoryTable, userPointTable);
        }

    }

}

