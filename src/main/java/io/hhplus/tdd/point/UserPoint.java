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
        if (amount <= 0) {
            throw new PointException("포인트 충전은 0보다 큰 금액만 가능합니다");
        }
        return new UserPoint(id, point + amount, System.currentTimeMillis());
    }

    public UserPoint use(long amount) {
        if (amount <= 0) {
            throw new PointException("포인트 사용 금액은 0보다 커야 합니다");
        }
        if (point < amount) {
            throw new PointException("포인트 잔고가 부족합니다");
        }
        return new UserPoint(id, point - amount, System.currentTimeMillis());
    }
}
