package io.hhplus.tdd.order;

import java.util.function.Supplier;

public interface IdempotentOrderExecutor {
    <T> T executeWithLock(UserOrder order, Supplier<T> task);
}
