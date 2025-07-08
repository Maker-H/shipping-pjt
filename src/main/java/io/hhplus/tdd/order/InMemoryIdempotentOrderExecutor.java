package io.hhplus.tdd.order;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import static io.hhplus.tdd.order.OrderException.*;
import static io.hhplus.tdd.order.OrderException.Message.*;

@Component
public class InMemoryIdempotentOrderExecutor implements IdempotentOrderExecutor {

    private final ConcurrentHashMap<UserOrder, ReentrantLock> lockMap = new ConcurrentHashMap<>();

    private final int WAIT_SECONDS = 5;
    private final TimeUnit WAIT_UNIT = TimeUnit.SECONDS;

    public <T> T executeWithLock (UserOrder orderKey, Supplier<T> task) {
        ReentrantLock lock = lockMap.computeIfAbsent(orderKey, key -> new ReentrantLock());
        boolean acquired = false;
        try {
            acquired = lock.tryLock(WAIT_SECONDS, WAIT_UNIT);
            if (acquired) {
                return task.get();
            } else {
                throw new OrderException(ALREADY_ORDERED);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } finally {
            if (acquired) {
                lock.unlock();
                lockMap.remove(orderKey, lock);
            }
        }
    }

    public int getWAIT_SECONDS() {
        return WAIT_SECONDS;
    }

    public TimeUnit getWAIT_UNIT() {
        return WAIT_UNIT;
    }
}
