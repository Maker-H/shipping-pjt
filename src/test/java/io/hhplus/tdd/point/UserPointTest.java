package io.hhplus.tdd.point;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class UserPointTest {

    static Stream<Arguments> addSuccessCases() {
        return Stream.of(
                Arguments.of(new UserPoint(1L, 0, 0), 1),
                Arguments.of(new UserPoint(1L, 6, 400L), 6),
                Arguments.of(new UserPoint(1L, 100L, 50L), 100),
                Arguments.of(new UserPoint(1L, 500L, 100L), 500)
        );
    }

    static Stream<Arguments> addFailCases() {
        return Stream.of(
                Arguments.of(new UserPoint(1L, 0, 0), 0),
                Arguments.of(new UserPoint(1L, 6, 400L), -1),
                Arguments.of(new UserPoint(1L, 100L, 50L), -100),
                Arguments.of(new UserPoint(1L, 500L, 100L), -50)
        );
    }

    static Stream<Arguments> useSuccessCases() {
        return Stream.of(
                Arguments.of(new UserPoint(1L, 100L, 0), 10),
                Arguments.of(new UserPoint(1L, 500L, 0), 10)
        );
    }

    static Stream<Arguments> useFailCases() {
        return Stream.of(
                Arguments.of(new UserPoint(1L, 0, 0), 0),
                Arguments.of(new UserPoint(1L, 50, 0), -51)
        );
    }

    @ParameterizedTest
    @MethodSource("addSuccessCases")
    void charge_sucess(UserPoint userPoint, long addAmount) {
        long prevAmount = userPoint.point();
        UserPoint result = userPoint.add(addAmount);

        assertAll(
                () -> assertEquals(prevAmount + addAmount, result.point()),
                () -> assertTrue(
                        result.updateMillis() > userPoint.updateMillis(),
                        "업데이트 시간이 갱신되어야 한다"
                )
        );
    }

    @ParameterizedTest(name = "충전 포인트가 0 이하인 경우")
    @MethodSource("addFailCases")
    void charge_fail(UserPoint userPoint, long addAmount) {
        assertThrows(PointException.class, () -> userPoint.add(addAmount));
    }

    @ParameterizedTest(name = "포인트 차감 성공 케이스")
    @MethodSource("useSuccessCases")
    void use_success(UserPoint userPoint, long minusAmount) {
        long prevAmount = userPoint.point();

        UserPoint result = userPoint.use(minusAmount);

        assertAll(
                () -> assertEquals(prevAmount - minusAmount, result.point()),
                () -> assertTrue(result.updateMillis() > userPoint.updateMillis())
        );
    }

    @ParameterizedTest(name = "포인트가 부족하여 차감 실패하는 케이스")
    @MethodSource("useFailCases")
    void use_fail(UserPoint userPoint) {
        long minusAmount = 100L;
        assertThrows(PointException.class, () -> userPoint.use(minusAmount));
    }


}