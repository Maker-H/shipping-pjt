package io.hhplus.tdd.point;


import io.hhplus.tdd.order.OrderException;

/**
 * 포인트 트랜잭션 종류
 * - CHARGE : 충전
 * - USE : 사용
 */
public enum TransactionType {
    CHARGE, USE;

    public static TransactionType from (String value) {
        for (TransactionType type : values()) {
            if (type.name().equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("No enum constant " + value);
    }
}