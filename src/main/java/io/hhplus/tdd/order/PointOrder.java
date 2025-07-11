package io.hhplus.tdd.order;

import io.hhplus.tdd.point.TransactionType;

import java.time.Instant;
import java.util.Objects;

public record PointOrder(
        long id,
        long point,
        TransactionType type,
        Instant orderTime
) {

    public static PointOrder empty(long id) {
        return new PointOrder(id, 0, TransactionType.CHARGE, Instant.now());
    }

    public OrderKey toKey() {
        return new OrderKey(id, type, point);
    }

}
