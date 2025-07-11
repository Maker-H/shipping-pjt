package io.hhplus.tdd.order;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import static io.hhplus.tdd.order.OrderException.OrderErrorType.ALREADY_ORDERED;
import static io.hhplus.tdd.order.OrderException.OrderErrorType.TOO_MANY_REQUEST;


@Component
public class InMemoryIdempotentOrderExecutor implements IdempotentOrderExecutor {

    private static final int DEFAULT_WAIT_SECONDS = 5;
    private static final TimeUnit DEFAULT_WAIT_UNIT = TimeUnit.SECONDS;

    ConcurrentMap<OrderKey, Instant> recentRequests = new ConcurrentHashMap<>();
    ConcurrentMap<OrderKey, ReentrantLock> lockMap = new ConcurrentHashMap<>();

    private final int waitSeconds;
    private final TimeUnit waitUnit;

    public InMemoryIdempotentOrderExecutor() {
        this(DEFAULT_WAIT_SECONDS, DEFAULT_WAIT_UNIT);
    }

    public InMemoryIdempotentOrderExecutor(int waitSeconds, TimeUnit waitUnit) {
        this.waitSeconds = waitSeconds;
        this.waitUnit = waitUnit;
    }

    public <T> T executeWithLock (PointOrder pointOrder, Supplier<T> task) {
        OrderKey orderKey = pointOrder.toKey();

        Instant now = Instant.now();
        Instant lastRequestTime = recentRequests.get(orderKey);

        if (lastRequestTime != null) {
            boolean isRequestDelayed = Duration.between(lastRequestTime, now).toMillis() < waitUnit.toMillis(waitSeconds);

            if (isRequestDelayed) {
                throw new OrderException(ALREADY_ORDERED);
            }
        }

        ReentrantLock lock = lockMap.computeIfAbsent(orderKey, key -> new ReentrantLock());
        boolean acquired = false;
        try {
            acquired = lock.tryLock(waitSeconds, waitUnit);
            if (acquired) {
                recentRequests.put(orderKey, now);
                return task.get();
            } else {
                throw new OrderException(TOO_MANY_REQUEST);
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

        // TODO: 일정 시간 이후 request 객체 쌓이기에 메모리 관리 해줘야 함
    }

    public int getWaitSeconds() {
        return waitSeconds;
    }

    public TimeUnit getWaitUnit() {
        return waitUnit;
    }
}
