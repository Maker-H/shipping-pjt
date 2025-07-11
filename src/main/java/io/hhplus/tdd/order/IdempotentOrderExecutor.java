package io.hhplus.tdd.order;

import java.util.function.Supplier;

public interface IdempotentOrderExecutor {
    <T> T executeWithLock(PointOrder order, Supplier<T> task);
}
