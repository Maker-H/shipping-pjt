package io.hhplus.tdd.order;

import org.junit.jupiter.api.*;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;


class InMemoryIdempotentOrderExecutorTest {

    private InMemoryIdempotentOrderExecutor indempotentExecutor;
    private ExecutorService threadPool;
    private PointOrder firstOrder;

    @BeforeEach
    void beforeEach() {
        indempotentExecutor = new InMemoryIdempotentOrderExecutor(1, TimeUnit.SECONDS);
        threadPool = Executors.newFixedThreadPool(50);
        firstOrder = PointOrder.empty(1L);
    }

    @AfterEach
    void afterEach() {
        threadPool.shutdown();
    }

    @Test
    void 동시요청시_중복_실행_방지() throws Exception {

        AtomicInteger CALL_COUNT = new AtomicInteger(0);

        TimeUnit waitUnit = indempotentExecutor.getWaitUnit();
        long waitSeconds = indempotentExecutor.getWaitSeconds();

        CountDownLatch latch = new CountDownLatch(1);

        Supplier<PointOrder> supplier = () -> {
            CALL_COUNT.incrementAndGet();
            threadSleep(waitUnit, waitSeconds);
            return firstOrder;
        };

        Callable<PointOrder> taskLogic = () -> {
            latch.await();
            return indempotentExecutor.executeWithLock(firstOrder, supplier);
        };

        List<Callable<PointOrder>> callables = createConcurrentTasks(10, taskLogic);
        List<Future<PointOrder>> futures = submitCallable(callables);
        latch.countDown();

        for (Future<PointOrder> future : futures) {
            try {
                future.get();
            } catch (Exception e) {

            }
        }

        assertEquals(1, CALL_COUNT.get(), "Supplier는 단 한 번만 호출되어야 함");
    }

    @Test
    void TTL_보장_확인() {
        AtomicInteger CALL_COUNT = new AtomicInteger(0);

        indempotentExecutor.executeWithLock(
                this.firstOrder,
                () -> {
                    CALL_COUNT.incrementAndGet();
                    return firstOrder;
                }
        );

        TimeUnit waitUnit = indempotentExecutor.getWaitUnit();
        long waitSeconds = indempotentExecutor.getWaitSeconds();
        threadSleep(waitUnit, waitSeconds);

        PointOrder secondOrder = PointOrder.empty(1L);

        indempotentExecutor.executeWithLock(
                secondOrder,
                () -> {
                    CALL_COUNT.incrementAndGet();
                    return secondOrder;
                }
        );

        assertEquals(2, CALL_COUNT.get(), "TTL 만료 후 supplier가 다시 실행돼야 함");
    }

    @Test
    void  시간내_중복_주문시_예외() {
        indempotentExecutor.executeWithLock(this.firstOrder, () -> firstOrder);

        PointOrder secondOrder = PointOrder.empty(1L);
        System.out.println("hash: " + firstOrder.toKey().hashCode() + " vs " + secondOrder.toKey().hashCode());
        Assertions.assertThrows(
                OrderException.class,
                () -> indempotentExecutor.executeWithLock(secondOrder, () -> secondOrder)
        );

    }

    @Test
    void 동시_요청시_예외_발생() throws ExecutionException, InterruptedException {

        int EXPECTED_SUCCESS = 1;
        int EXPECTED_FAIL = 9;
        int totalThreadCount = EXPECTED_FAIL + EXPECTED_SUCCESS;

        TimeUnit waitUnit = indempotentExecutor.getWaitUnit();
        long waitSeconds = indempotentExecutor.getWaitSeconds();

        CountDownLatch latch = new CountDownLatch(1);

        Supplier<PointOrder> supplier = () -> {
            threadSleep(waitUnit, waitSeconds);
            return firstOrder;
        };

        Callable<Object> taskLogic = () -> {
            try {
                latch.await();
                return indempotentExecutor.executeWithLock(firstOrder, supplier);
            } catch (OrderException e) {
                return e;
            }
        };

        List<Callable<Object>> tasks = createConcurrentTasks(totalThreadCount, taskLogic);
        List<Future<Object>> futures = submitCallable(tasks);
        latch.countDown();

        AtomicInteger ACTUAL_SUCCESS = new AtomicInteger(0);
        AtomicInteger ACTUAL_FAIL = new AtomicInteger(0);

        for (Future<Object> future : futures) {
            Object result = future.get();
            if (result instanceof OrderException) {
                ACTUAL_FAIL.incrementAndGet();
            } else {
                ACTUAL_SUCCESS.incrementAndGet();
            }
        }

        assertEquals(EXPECTED_FAIL, ACTUAL_FAIL.get(), "실패 스레드는 AlreadyProcessedOrderException을 받아야 함");
        assertEquals(EXPECTED_SUCCESS, ACTUAL_SUCCESS.get());
    }

    private <T> List<Callable<T>> createConcurrentTasks(
            int threadCount,
            Callable<T> taskLogic
    ) {
        return IntStream.range(0, threadCount)
                .mapToObj(i -> taskLogic)
                .toList();
    }

    private <T> List<Future<T>> submitCallable(List<Callable<T>> callables) {
        return callables.stream()
                .map(callable -> threadPool.submit(callable))
                .toList();
    }

    private void threadSleep(TimeUnit waitUnit, long waitSeconds) {
        try {
            waitUnit.sleep(waitSeconds + 1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}