package io.hhplus.tdd.point;

public record UserPoint(
        long id,
        long point,
        long updateMillis
) {

    public static UserPoint empty(long id) {
        return new UserPoint(id, 0, System.currentTimeMillis());
    }

    public UserPoint add(long amount) {
        return new UserPoint(id, point + amount, System.currentTimeMillis());
    }

    public UserPoint minus(long amount) {
        if (point < amount) {
            throw new PointException("포인트 잔고가 부족합니다");
        }
        return new UserPoint(id, point - amount, System.currentTimeMillis());
    }
}
