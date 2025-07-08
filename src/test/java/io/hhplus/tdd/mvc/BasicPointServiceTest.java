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
import org.springframework.test.annotation.DirtiesContext;
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
        BasicPointServiceTest.OverrideConfig.class,
        MvcTestConfig.class
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class BasicPointServiceTest {

    @Autowired
    private PointController pointController;

    @Autowired
    private UserPointTable pointTable;

    @Autowired
    private PointHistoryTable historyTable;

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

    static Stream<Arguments> 충전_성공_케이스() {
        // userId, 초기 포인트, 충전 예정 포인트
        return Stream.of(
                Arguments.of(1L, 1000L, 1000L),
                Arguments.of(2L, 2000L, 2000L),
                Arguments.of(3L, 0L, 100L)
        );
    }

    @ParameterizedTest(name = "userID:{0}, 초기포인트:{1}, 충전포인트:{2}")
    @MethodSource("충전_성공_케이스")
    void 충전_성공_테스트(long userId, long initialAmount, long chargeAmount) {

        pointTable.insertOrUpdate(userId, initialAmount);

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
                () -> assertEquals(TransactionType.CHARGE, pointHistory.type())
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

    @ParameterizedTest(name = "userID:{0}, 초기포인트:{1}, 충전포인트:{2}")
    @MethodSource("충전_실패_케이스")
    void 충전요청이_마이너스인_경우_테스트(long userId, long initialAmount, long chargeAmount) {

        pointTable.insertOrUpdate(userId, initialAmount);

        assertThrows(PointException.class, () -> pointController.charge(userId, chargeAmount));

    }

    static Stream<Arguments> 사용_성공_케이스() {
        // userId, 초기 포인트, 사용 예정 포인트
        return Stream.of(
                Arguments.of(1L, 1000L, 1000L),
                Arguments.of(2L, 2000L, 1999L)
        );
    }
    @ParameterizedTest(name = "userID:{0}, 초기포인트:{1}, 사용포인트:{2}")
    @MethodSource("사용_성공_케이스")
    void 사용_성공_테스트(long userId, long initialAmount, long useAmount) {

        pointTable.insertOrUpdate(userId, initialAmount);

        UserPoint result = pointController.use(userId, useAmount);

        assertThat(result.point()).isEqualTo(initialAmount - useAmount);

        List<PointHistory> pointHistories = historyTable.selectAllByUserId(userId);
        assertAll(
                () -> assertFalse(pointHistories.isEmpty()),
                () -> assertThat(pointHistories).hasSize(1)
        );

        PointHistory pointHistory = pointHistories.get(0);
        assertAll(
                () -> assertEquals(userId, pointHistory.userId()),
                () -> assertEquals(useAmount, pointHistory.amount()),
                () -> assertEquals(TransactionType.USE, pointHistory.type())
        );
    }

    static Stream<Arguments> 사용_실패_케이스() {
        // userId, 초기 포인트, 충전 예정 포인트
        return Stream.of(
                Arguments.of(1L, 1000L, 1001L), // 잔고가 부족할 경우
                Arguments.of(2L, 0L, -1L), // 마이너스를 사용하려고 하는 경우
                Arguments.of(3L, 5L, 0L) // 0을 사용하려고 하는 경우
        );
    }
    @ParameterizedTest(name = "userID:{0}, 초기포인트:{1}, 사용포인트:{2}")
    @MethodSource("사용_실패_케이스")
    void 사용요청이_마이너스인_경우_테스트(long userId, long initialAmount, long useAmount) {

        pointTable.insertOrUpdate(userId, initialAmount);

        assertThrows(PointException.class, () -> pointController.use(userId, useAmount));
    }

    @Test
    void 히스토리가_없는_유저는_빈히스토리_반환() {
        List<PointHistory> histories = pointController.history(1L);
        assertThat(histories).isEmpty();
    }

    @Test
    void 히스토리가_정상적으로_모두_쌓일_경우() {
        long userId = 1L;
        long chargeAmount = 1_000L;
        long useAmount = 1_000L;

        pointController.charge(userId, chargeAmount);

        pointController.use(userId, useAmount);

        List<PointHistory> histories = pointController.history(userId);
        assertThat(histories).hasSize(2);

        PointHistory firstHistory = histories.get(0);
        PointHistory secondHistory = histories.get(1);
        assertAll(
                () -> assertEquals(chargeAmount, firstHistory.amount()),
                () -> assertEquals(TransactionType.CHARGE, firstHistory.type()),
                () -> assertEquals(userId, firstHistory.userId())
        );

        assertAll(
                () -> assertEquals(useAmount, secondHistory.amount()),
                () -> assertEquals(TransactionType.USE, secondHistory.type()),
                () -> assertEquals(userId, secondHistory.userId())
        );
    }

    @Test
    void 포인트_잔액부족시_성공내역만_히스토리에_남는다() {
        long userId = 1L;
        long chargeAmount = 1_000L;
        long useAmount = 1_001L;

        pointController.charge(userId, chargeAmount);

        PointException pointException = assertThrows(PointException.class, () -> pointController.use(userId, useAmount));
        assertThat(pointException.getMessage()).contains("잔고가 부족");
        List<PointHistory> histories = pointController.history(userId);
        assertThat(histories).hasSize(1);

        PointHistory firstHistory = histories.get(0);
        assertAll(
                () -> assertEquals(chargeAmount, firstHistory.amount()),
                () -> assertEquals(TransactionType.CHARGE, firstHistory.type()),
                () -> assertEquals(userId, firstHistory.userId())
        );
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
            return new BasicPointService(pointHistoryTable, userPointTable);
        }

    }

}

