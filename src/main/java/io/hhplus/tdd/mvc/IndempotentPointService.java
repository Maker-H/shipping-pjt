package io.hhplus.tdd.mvc;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.order.IdempotentOrderExecutor;
import io.hhplus.tdd.order.PointOrder;
import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.TransactionType;
import io.hhplus.tdd.point.UserPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

import static io.hhplus.tdd.point.TransactionType.CHARGE;
import static io.hhplus.tdd.point.TransactionType.USE;

@Service
@RequiredArgsConstructor
public class IndempotentPointService implements PointService {

    private final PointHistoryTable pointHistoryTable;
    private final UserPointTable userPointTable;
    private final IdempotentOrderExecutor orderExecutor;

    @Override
    public UserPoint charge(long id, long amount) {

        PointOrder pointOrder = new PointOrder(id, amount, TransactionType.CHARGE, Instant.now());

        return orderExecutor.executeWithLock(pointOrder, () -> {
            UserPoint existing = userPointTable.selectById(id);
            long totalPoint = existing.add(amount).point();

            pointHistoryTable.insert(id, amount, CHARGE, System.currentTimeMillis());
            return userPointTable.insertOrUpdate(id, totalPoint);
        });

    }

    @Override
    public UserPoint get(long id) {
        return userPointTable.selectById(id);
    }

    @Override
    public List<PointHistory> history(long id) {
        List<PointHistory> pointHistories = pointHistoryTable.selectAllByUserId(id);
        pointHistories.sort(Comparator.comparingLong(PointHistory::updateMillis).reversed());
        return pointHistories;
    }

    @Override
    public UserPoint use(long id, long amount) {

        PointOrder pointOrder = new PointOrder(id, amount, TransactionType.USE, Instant.now());

        return orderExecutor.executeWithLock(pointOrder, () -> {
            UserPoint existing = userPointTable.selectById(id);
            UserPoint result = existing.use(amount);

            pointHistoryTable.insert(id, amount, USE, System.currentTimeMillis());
            return userPointTable.insertOrUpdate(id, result.point());
        });

    }


}
