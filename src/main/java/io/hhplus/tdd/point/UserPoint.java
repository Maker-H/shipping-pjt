package io.hhplus.tdd.point;


import static io.hhplus.tdd.point.PointException.*;

public record UserPoint(
        long id,
        long point,
        long updateMillis
) {

    public static UserPoint empty(long id) {
        return new UserPoint(id, 0, System.currentTimeMillis());
    }

    public UserPoint add(long amount) {
        if (amount <= 0) {
            throw new PointException(PointErrorType.CHARGE_AMOUNT_TOO_LOW);
        }
        return new UserPoint(id, point + amount, System.currentTimeMillis());
    }

    public UserPoint use(long amount) {
        if (amount <= 0) {
            throw new PointException(PointErrorType.USE_AMOUNT_TOO_LOW);
        }
        if (point < amount) {
            throw new PointException(PointErrorType.USE_AMOUNT_TOO_MUCH);
        }
        return new UserPoint(id, point - amount, System.currentTimeMillis());
    }
}
