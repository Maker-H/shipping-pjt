package io.hhplus.tdd.point;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class UserPointTest {

    static Stream<Arguments> addCases() {
        return Stream.of(
                Arguments.of(new UserPoint(1L, 0, 0), 0),
                Arguments.of(new UserPoint(1L, 6, 400L), 6),
                Arguments.of(new UserPoint(1L, 100L, 50L), 100),
                Arguments.of(new UserPoint(1L, 500L, 100L), 500)
        );
    }

    static Stream<Arguments> minusSuccessCases() {
        return Stream.of(
                Arguments.of(new UserPoint(1L, 100L, 0), 100),
                Arguments.of(new UserPoint(1L, 500L, 0), 500)
        );
    }

    static Stream<UserPoint> minusFailCases() {
        return Stream.of(
                new UserPoint(1L, 0, 0),
                new UserPoint(1L, 50, 0)
        );
    }

    @ParameterizedTest
    @MethodSource("addCases")
    void add(UserPoint userPoint, long prevAmount) {
        long addAmount = 1_000L;
        UserPoint addedUserPoint = userPoint.add(addAmount);

        assertAll(
                () -> assertEquals(prevAmount + addAmount, addedUserPoint.point()),
                () -> assertTrue(
                        addedUserPoint.updateMillis() > userPoint.updateMillis(),
                        "업데이트 시간이 갱신되어야 한다"
                )
        );
    }

    @ParameterizedTest(name = "포인트 차감 성공 케이스")
    @MethodSource("minusSuccessCases")
    void minus_success(UserPoint userPoint, long prevAmount) {
        long minusAmount = 100L;

        UserPoint minusUserPoint = userPoint.minus(minusAmount);

        assertAll(
                () -> assertEquals(prevAmount - minusAmount, minusUserPoint.point()),
                () -> assertTrue(minusUserPoint.updateMillis() > userPoint.updateMillis())
        );
    }

    @ParameterizedTest(name = "포인트가 부족하여 차감 실패하는 케이스")
    @MethodSource("minusFailCases")
    void minus_fail(UserPoint userPoint) {
        long minusAmount = 100L;
        assertThrows(PointException.class, () -> userPoint.minus(minusAmount));
    }

}