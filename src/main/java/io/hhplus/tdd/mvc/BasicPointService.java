package io.hhplus.tdd.mvc;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.UserPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static io.hhplus.tdd.point.TransactionType.CHARGE;
import static io.hhplus.tdd.point.TransactionType.USE;

@Service
@RequiredArgsConstructor
public class BasicPointService implements PointService {

    private final PointHistoryTable pointHistoryTable;
    private final UserPointTable userPointTable;

    @Override
    public UserPoint charge(long id, long amount) {
        UserPoint existing = userPointTable.selectById(id);
        long totalPoint = existing.add(amount).point();

        pointHistoryTable.insert(id, amount, CHARGE, System.currentTimeMillis());
        return userPointTable.insertOrUpdate(id, totalPoint);
    }

    @Override
    public UserPoint get(long id) {
        return userPointTable.selectById(id);
    }

    @Override
    public List<PointHistory> history(long id) {
        List<PointHistory> pointHistories = pointHistoryTable.selectAllByUserId(id);
        return pointHistories;
    }

    @Override
    public UserPoint use(long id, long amount) {
        UserPoint existing = userPointTable.selectById(id);
        long totalPoint = existing.use(amount).point();

        pointHistoryTable.insert(id, amount, USE, System.currentTimeMillis());
        return userPointTable.insertOrUpdate(id, totalPoint);
    }


}
